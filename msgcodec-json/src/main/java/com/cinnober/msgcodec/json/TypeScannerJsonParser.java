/*
 * Copyright (c) 2015 Cinnober Financial Technology AB, Stockholm,
 * Sweden. All rights reserved.
 * 
 * This software is the confidential and proprietary information of
 * Cinnober Financial Technology AB, Stockholm, Sweden. You shall not
 * disclose such Confidential Information and shall use it only in
 * accordance with the terms of the license agreement you entered into
 * with Cinnober.
 * 
 * Cinnober makes no representations or warranties about the suitability
 * of the software, either expressed or implied, including, but not limited
 * to, the implied warranties of merchantibility, fitness for a particular
 * purpose, or non-infringement. Cinnober shall not be liable for any
 * damages suffered by licensee as a result of using, modifying, or
 * distributing this software or its derivatives.
 */
package com.cinnober.msgcodec.json;

import com.cinnober.msgcodec.DecodeException;
import static com.cinnober.msgcodec.json.JsonValueHandler.TYPE_FIELD;
import com.fasterxml.jackson.core.Base64Variant;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonStreamContext;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.core.Version;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.NoSuchElementException;

/**
 * JsonParser with look-ahead to find the "$type" field value.
 * This class is used when the "$type" field is not the first field in a dynamic object.
 * 
 * @author mikael.brannstrom
 */
class TypeScannerJsonParser extends JsonParser {

    private final JsonParser p;
    private final LinkedList<Token> tokens = new LinkedList<>();
    private Token currentToken;
    TypeScannerJsonParser(JsonParser p) {
        this.p = p;
    }

    /**
     * Finds and removes the type field-value pair.
     *
     * It is expected that currentToken points at the (the first) field name in an object.
     * After this call, nextToken refers to the next field-value pair, or object-end.
     *
     * @return the value of the field "$type".
     */
    String findType() throws IOException {
        if (currentToken == null && tokens.isEmpty()) {
            return findTypeInDelegate();
        } else {
            return findTypeInTokens();
        }
    }

    private String findTypeInDelegate() throws IOException {
        boolean next = false;
        for(;;) {
            JsonToken token = next ? p.nextToken() : p.getCurrentToken();
            next = true;
            switch(token) {
                case FIELD_NAME:
                    if (p.getText().equals(TYPE_FIELD)) {
                        if (p.nextToken() != JsonToken.VALUE_STRING) {
                            throw new DecodeException("Expected string value for field '" + TYPE_FIELD +"'");
                        }
                        return p.getText();
                    } else {
                        tokens.add(new ValueToken(JsonToken.FIELD_NAME, p.getText()));
                        p.nextToken(); // consume field
                        parseValue();
                    }
                    break;
                case END_OBJECT:
                    throw new DecodeException("Reached end of object. Field '" + TYPE_FIELD + "' not found");
                default:
                    throw new DecodeException("Unexpected JSON token " + token);
            }
        }
    }
    private String findTypeInTokens() throws IOException {
        tokens.addFirst(currentToken);
        try {
            for(Iterator<Token> it=tokens.iterator();;) {
                Token token = it.next();
                switch (token.getType()) {
                    case FIELD_NAME:
                        if (token.getText().equals(TYPE_FIELD)) {
                            it.remove();
                            Token typeToken = it.next(); it.remove();
                            if (typeToken.getType() != JsonToken.VALUE_STRING) {
                                throw new DecodeException("Expected string value for field '" + TYPE_FIELD +"'");
                            }
                            return typeToken.getText();
                        } else {
                            skipValue(it);
                        }
                        break;
                    case END_OBJECT:
                        throw new DecodeException("Reached end of object. Field '" + TYPE_FIELD + "' not found");
                    default:
                        throw new DecodeException("Unexpected JSON token " + token.getType());
                }
            }
        } catch(NoSuchElementException e) {
            throw new DecodeException("Unexpected JSON token null", e);
        }
    }

    private void skipValue(Iterator<Token> it) throws NoSuchElementException, DecodeException {
        Token token = it.next();
        switch (token.getType()) {
            case START_ARRAY:
                skipValuesUntil(it, JsonToken.END_ARRAY);
                break;
            case START_OBJECT:
                skipValuesUntil(it, JsonToken.END_OBJECT);
                break;
            case END_ARRAY:
            case END_OBJECT:
                throw new DecodeException("Unexpected JSON token " + token.getType());
            default:
                break;
        }
    }

    private void skipValuesUntil(Iterator<Token> it, JsonToken end) throws DecodeException {
        for(;;) {
            Token token = it.next();
            if (token.getType() == end) {
                break;
            } else {
                if (end == JsonToken.END_OBJECT) {
                    skipField(it);
                }
                skipValue(it);
            }
        }
    }
    private void skipField(Iterator<Token> it) throws NoSuchElementException, DecodeException {
        Token token = it.next();
        if (token.getType() != JsonToken.FIELD_NAME) {
            throw new DecodeException("Expected field, got " + token.getType());
        }
    }


    private void parseValue() throws IOException {
        JsonToken token = p.getCurrentToken();
        if (token == null) {
            throw new DecodeException("Unexpected JSON token null");
        }
        switch (token) {
            case START_ARRAY:
                tokens.add(START_ARRAY);
                parseValuesUntil(JsonToken.END_ARRAY);
                tokens.add(END_ARRAY);
                break;
            case START_OBJECT:
                tokens.add(START_OBJECT);
                parseValuesUntil(JsonToken.END_OBJECT);
                tokens.add(END_OBJECT);
                break;
            case VALUE_STRING:
            case VALUE_NUMBER_FLOAT:
            case VALUE_NUMBER_INT:
                tokens.add(new ValueToken(token, p.getText()));
                break;
            case VALUE_TRUE:
            case VALUE_FALSE:
            case VALUE_NULL:
                tokens.add(new Token(token));
                break;
            default:
                throw new DecodeException("Unexpected JSON token " + token + ": '" + p.getText() + "'");
        }
    }
    private void parseValuesUntil(JsonToken end) throws IOException {
        for(;;) {
            JsonToken token = p.nextToken();
            if (token == null) {
                throw new DecodeException("Unexpected JSON token null");
            }
            if (token == end) {
                break;
            } else {
                if (end == JsonToken.END_OBJECT) {
                    parseField();
                }
                parseValue();
            }
        }
    }

    private void parseField() throws DecodeException, IOException {
        JsonToken token = p.getCurrentToken();
        if (token != JsonToken.FIELD_NAME) {
            throw new DecodeException("Expected field, got " + token);
        }
        tokens.add(new ValueToken(JsonToken.FIELD_NAME, p.getText()));
        p.nextToken();
    }
    @Override
    public JsonToken getCurrentToken() {
        return currentToken != null ? currentToken.getType() : p.getCurrentToken();
    }

    @Override
    public boolean hasCurrentToken() {
        return currentToken != null || p.hasCurrentToken();
    }

    @Override
    public JsonToken nextToken() throws IOException, JsonParseException {
        if (tokens.isEmpty()) {
            currentToken = null;
            return p.nextToken();
        } else {
            currentToken = tokens.pop();
            return currentToken.getType();
        }
    }

    @Override
    public String getText() throws IOException, JsonParseException {
        return currentToken != null ? currentToken.getText() : p.getText();
    }
    @Override
    public char[] getTextCharacters() throws IOException, JsonParseException {
        return currentToken != null ? currentToken.getText().toCharArray() : p.getTextCharacters();
    }
    @Override
    public int getTextLength() throws IOException, JsonParseException {
        return currentToken != null ? currentToken.getText().length() : p.getTextLength();
    }
    @Override
    public int getTextOffset() throws IOException, JsonParseException {
        return currentToken != null ? 0 : p.getTextOffset();
    }
    @Override
    public boolean hasTextCharacters() {
        return currentToken != null ? currentToken.getText() != null : p.hasTextCharacters();
    }
    @Override
    public int getIntValue() throws IOException, JsonParseException {
        return currentToken != null ? currentToken.getIntValue() : p.getIntValue();
    }
    @Override
    public long getLongValue() throws IOException, JsonParseException {
        return currentToken != null ? currentToken.getLongValue() : p.getLongValue();
    }
    @Override
    public BigInteger getBigIntegerValue() throws IOException, JsonParseException {
        return currentToken != null ? currentToken.getBigIntegerValue() : p.getBigIntegerValue();
    }
    @Override
    public float getFloatValue() throws IOException, JsonParseException {
        return currentToken != null ? currentToken.getFloatValue() : p.getFloatValue();
    }
    @Override
    public double getDoubleValue() throws IOException, JsonParseException {
        return currentToken != null ? currentToken.getDoubleValue() : p.getDoubleValue();
    }
    @Override
    public BigDecimal getDecimalValue() throws IOException, JsonParseException {
        return currentToken != null ? currentToken.getDecimalValue() : p.getDecimalValue();
    }
    @Override
    public String getValueAsString(String defaultValue) throws IOException, JsonParseException {
        if (currentToken != null) {
            String s = currentToken.getText();
            return s != null ? s : defaultValue;
        } else {
            return p.getValueAsString(defaultValue);
        }
    }
    @Override
    public byte[] getBinaryValue(Base64Variant b64variant) throws IOException, JsonParseException {
        if (currentToken != null) {
            String text = currentToken.getText();
            return Base64.getDecoder().decode(text); // PENDING: howto deal with the variant?
        } else {
            return p.getBinaryValue(b64variant);
        }
    }

    
    @Override
    public ObjectCodec getCodec() {
        return p.getCodec();
    }
    @Override
    public void setCodec(ObjectCodec c) {
        p.setCodec(c);
    }
    @Override
    public Version version() {
        return p.version();
    }
    @Override
    public void close() throws IOException {
        p.close();
    }
    @Override
    public boolean isClosed() {
        return p.isClosed();
    }

    @Override
    public Number getNumberValue() throws IOException, JsonParseException {
        throw new UnsupportedOperationException("Not supported.");
    }
    @Override
    public NumberType getNumberType() throws IOException, JsonParseException {
        throw new UnsupportedOperationException("Not supported.");
    }
    @Override
    public Object getEmbeddedObject() throws IOException, JsonParseException {
        throw new UnsupportedOperationException("Not supported.");
    }
    @Override
    public JsonToken nextValue() throws IOException, JsonParseException {
        throw new UnsupportedOperationException("Not supported.");
    }
    @Override
    public JsonParser skipChildren() throws IOException, JsonParseException {
        throw new UnsupportedOperationException("Not supported.");
    }
    @Override
    public String getCurrentName() throws IOException, JsonParseException {
        throw new UnsupportedOperationException("Not supported.");
    }
    @Override
    public JsonStreamContext getParsingContext() {
        throw new UnsupportedOperationException("Not supported.");
    }
    @Override
    public JsonLocation getTokenLocation() {
        throw new UnsupportedOperationException("Not supported.");
    }
    @Override
    public JsonLocation getCurrentLocation() {
        throw new UnsupportedOperationException("Not supported.");
    }
    @Override
    public void clearCurrentToken() {
        throw new UnsupportedOperationException("Not supported.");
    }
    @Override
    public JsonToken getLastClearedToken() {
        throw new UnsupportedOperationException("Not supported.");
    }
    @Override
    public void overrideCurrentName(String name) {
        throw new UnsupportedOperationException("Not supported.");
    }


    private static final Token START_ARRAY = new Token(JsonToken.START_ARRAY);
    private static final Token END_ARRAY = new Token(JsonToken.END_ARRAY);
    private static final Token START_OBJECT = new Token(JsonToken.START_OBJECT);
    private static final Token END_OBJECT = new Token(JsonToken.END_OBJECT);

    private static class Token {
        final JsonToken type;
        Token(JsonToken type) {
            this.type = type;

        }
        JsonToken getType() {
            return type;
        }
        String getText() {
            return null;
        }
        int getIntValue() {
            return Integer.valueOf(getText());
        }
        long getLongValue() {
            return Long.valueOf(getText());
        }
        float getFloatValue() {
            return Float.valueOf(getText());
        }
        double getDoubleValue() {
            return Double.valueOf(getText());
        }
        BigInteger getBigIntegerValue() {
            return new BigInteger(getText());
        }
        BigDecimal getDecimalValue() {
            return new BigDecimal(getText());
        }
        boolean getBooleanValue() {
            return type == JsonToken.VALUE_TRUE;
        }
    }
    private static class ValueToken extends Token {
        final String text;

        public ValueToken(JsonToken type, String text) {
            super(type);
            this.text = text;
        }
        @Override
        String getText() {
            return text;
        }
    }
}
