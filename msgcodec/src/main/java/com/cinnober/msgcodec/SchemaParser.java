/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 The MsgCodec Authors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.cinnober.msgcodec;

import com.cinnober.msgcodec.TypeDef.Symbol;
import com.cinnober.msgcodec.visitor.AnnotatedVisitor;
import com.cinnober.msgcodec.visitor.FieldDefVisitor;
import com.cinnober.msgcodec.visitor.GroupDefVisitor;
import com.cinnober.msgcodec.visitor.NamedTypeVisitor;
import com.cinnober.msgcodec.visitor.SchemaVisitor;
import com.cinnober.msgcodec.visitor.SchemaProducer;
import java.io.IOException;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;


/**
 * Schema parser can parse the schema string format.
 *
 * @see Schema#toString() 
 *
 * @author mikael.brannstrom
 */
public class SchemaParser {
    /*
    == GRAMMAR (sort of) ==

    schema := (incrAnnot | groupDef | namedType)*
    incrAnnot := name '<-' annot
    annot := '@' name '=' string
    name := [_a-zA-Z] [_a-zA-Z0-9]*
    groupDef := annot* name id? (':' name)? groupBody?
    groupBody := '->' fieldDefs
    fieldDefs :=  fieldDef ',' fieldDefs | fieldDef
    feieldDef := annot* type name id? '?'?
    id := '/' [0-9]+
    namedType := annot* name '=' type
    enumId := '/' '-'? [0-9]+
    
    */

    private final Reader reader;
    private int nextChar = -1;
    private final Map<String,String> annotations = new LinkedHashMap<>();

    /**
     * Create a new schema parser using the specified reader.
     * @param reader the reader to parse from, not null.
     */
    public SchemaParser(Reader reader) {
        this.reader = Objects.requireNonNull(reader);
    }

    /**
     * Parse a schema.
     * @return the schema, not null.
     * @throws IOException if the schema could not be parsed.
     */
    public Schema parse() throws IOException {
        SchemaProducer producer = new SchemaProducer();
        Annotations incrAnnotations = new Annotations();
        parse(producer, incrAnnotations);
        return producer.getSchema().addAnnotations(incrAnnotations);
    }

    /**
     * Parse a schema and notify the specified schema visitor about the parse events.
     * @param sv the schema visitor, not null.
     * @param incrAnnotations where incremental annotations (except for the schema) will be stored, or null if ignore.
     * @throws IOException if the schema could not be parsed.
     */
    public void parse(SchemaVisitor sv, Annotations incrAnnotations) throws IOException {
        annotations.clear();
        for (;;) {
            ws();
            switch (peek()) {
                case -1:
                    sv.visitEnd();
                    return;
                case '@':
                    parseAnnotation();
                    break;
                case '.':
                    pop();
                    parseIncrAnnot(sv, incrAnnotations, null);
                    break;
                default:
                    parseNamedStuff(sv, incrAnnotations);
                    break;
            }
        }
    }

    private SchemaParser ws() throws IOException {
        for(;;) {
            switch (peek()) {
                case ' ':
                case '\t':
                case '\n':
                case '\r':
                    pop();
                    break;
                default:
                    return this;
            }
        }
    }

    private int peek() throws IOException {
        if (nextChar == -1) {
            nextChar = reader.read();
        }
        return nextChar;
    }
    private int pop() throws IOException {
        if (nextChar == -1) {
            return reader.read();
        } else {
            int c = nextChar;
            nextChar = -1;
            return c;
        }
    }
    private SchemaParser pop(int exp) throws IOException {
        int c = pop();
        if (c != exp) {
            throw new IOException("Expected '" + (char)exp+"' found '" + (char)c+"'");
        }
        return this;
    }
    private boolean tryPop(int exp) throws IOException {
        if (peek() == exp) {
            pop();
            return true;
        }
        return false;
    }

    private String parseKey() throws IOException {
        return parseName(':');
    }

    private String parseDotName() throws IOException {
        return parseName('.');
    }
    
    private String parseName(char... additionalAllowedChars) throws IOException {
        StringBuilder s = new StringBuilder();
        for(;;) {
            int c = peek();
            if (('a' <= c && c <= 'z') ||
                ('A' <= c && c <= 'Z') ||
                c == '_' ||
                (s.length() != 0 && (
                    ('0' <= c && c <= '9') ||
                    contains(additionalAllowedChars, (char)c)))) {
                s.append((char)c);
                pop();
            } else {
                break;
            }
        }
        if (s.length() == 0) {
            throw new IOException("Could not parse name");
        }

        return s.toString();
    }
    private static boolean contains(char[] chars, char ch) {
        for (char c : chars) {
            if (c == ch) {
                return true;
            }
        }
        return false;
    }

    private SchemaParser tryParseAnnotations() throws IOException {
        while (peek() == '@') {
            parseAnnotation();
            ws();
        }
        return this;
    }

    private void parseAnnotation() throws IOException {
        String key = pop('@').parseKey();
        String value = ws().pop('=').ws().parseString();
        annotations.put(key, value);
    }
    private void consumeAnnotations(AnnotatedVisitor av) {
        if (av != null) {
            annotations.forEach(av::visitAnnotation);
        }
        annotations.clear();
    }

    private void parseIncrAnnot(SchemaVisitor sv, Annotations incrAnnotations, String name) throws IOException {
        String key = ws().pop('<').pop('-').ws().pop('@').parseKey();
        String value = ws().pop('=').ws().parseString();

        if (name == null) {
            sv.visitAnnotation(key, value);
        } else if (incrAnnotations != null) {
            incrAnnotations.path(name.split("\\.")).put(key, value);
        }
    }

    private String parseString() throws IOException {
        StringBuilder s = new StringBuilder();
        pop('"');
        for(;;) {
            int c = pop();
            switch(c) {
                case '"':
                    return s.toString();
                case '\\':
                    s.append(parseEscapedChar());
                    break;
                default:
                    s.append((char)c);
                    break;
            }
        }
    }
    private char parseEscapedChar() throws IOException {
        int c = pop();
        switch (c) {
            case 'n':
                return '\n';
            case 'r':
                return '\r';
            case '"':
                return '\"';
            case '\\':
                return '\\';
            case '0':
                return '\0';
            case 't':
                return '\t';
            default:
                throw new IOException("Illegal escape char '" + (char)c + "'");
        }
    }

    private void parseNamedStuff(SchemaVisitor sv, Annotations incrAnnotations) throws IOException {
        String name = parseDotName();
        boolean dotted = name.indexOf('.') != -1;
        ws();
        int c = peek();
        switch (c) {
            case '<':
                parseIncrAnnot(sv, incrAnnotations, name);
                break;
            case '=':
                if (dotted) {
                    throw new IOException("Illegal name of named type");
                }
                parseNamedType(sv, name);
                break;
            case '/':
            {
                if (dotted) {
                    throw new IOException("Illegal name of group def");
                }
                int id = parseId();
                ws();
                parseGroupDef(sv, name, id);
            }
                break;
            default:
                parseGroupDef(sv, name, -1);
        }
    }
    private int tryParseSymbolId(int def) throws IOException {
        if (tryPop('/')) {
            int sign = tryPop('-') ? -1 : 1;
            return sign * parseUInt();
        }
        return def;
    }
    private int parseId() throws IOException {
        int id = pop('/').parseUInt();
        if (id == -1) {
            throw new IOException("Illegal id (2^32-1)");
        }
        return id;
    }

    private int parseUInt() throws IOException {
        int v = 0;
        boolean empty = true;
        for(;;) {
            int c = peek();
            if ('0' <= c && c <= '9') {
                if (empty) {
                    empty = false;
                    v = c - '0';
                } else {
                    v = v * 10 + c - '0';
                }
                pop();
            } else {
                break;
            }
        }
        if (empty) {
            throw new IOException("Expected number");
        }
        return v;
    }

    private void parseGroupDef(SchemaVisitor sv, String name, int id) throws IOException {
        int c = peek();
        String superGroup = null;
        boolean hasFields = false;
        if (c == ':') {
            superGroup = pop(':').ws().parseName();
            c = peek();
        }
        if (c == '-') {
            pop('-').pop('>').ws();
            hasFields = true;
        }

        GroupDefVisitor gv = sv.visitGroup(name, id, superGroup, null);
        consumeAnnotations(gv);

        if (hasFields) {
            parseFieldDefs(gv);
        }

        if (gv != null) {
            gv.visitEnd();
        }
    }

    private void parseFieldDefs(GroupDefVisitor gv) throws IOException {
        while(ws().tryParseAnnotations().parseFieldDef(gv).ws().peek() == ',') {
            pop(',');
        }
    }

    private SchemaParser parseFieldDef(GroupDefVisitor gv) throws IOException {
        TypeDef type = parseType();
        String name = ws().parseName();
        int id = peek() == '/' ? parseId() : -1;
        boolean required = !tryPop('?');
        FieldDefVisitor fv = gv != null ? gv.visitField(name, id, required, type, null) : null;
        consumeAnnotations(fv);
        if (fv != null) {
            fv.visitEnd();
        }
        return this;
    }

    private TypeDef parseType() throws IOException {
        TypeDef type = parseSimpleType();
        ws();
        if (tryPop('[')) {
            pop(']');
            return new TypeDef.Sequence(type);
        } else {
            return type;
        }
    }
    private TypeDef parseSimpleType() throws IOException {
        String name = parseName();
        switch (name) {
            case "i8":
                return TypeDef.INT8;
            case "u8":
                return TypeDef.UINT8;
            case "i16":
                return TypeDef.INT16;
            case "u16":
                return TypeDef.UINT16;
            case "i32":
                return TypeDef.INT32;
            case "u32":
                return TypeDef.UINT32;
            case "i64":
                return TypeDef.INT64;
            case "u64":
                return TypeDef.UINT64;
            case "f32":
                return TypeDef.FLOAT32;
            case "f64":
                return TypeDef.FLOAT64;
            case "bigInt":
                return TypeDef.BIGINT;
            case "decimal":
                return TypeDef.DECIMAL;
            case "bigDecimal":
                return TypeDef.BIGDECIMAL;
            case "boolean":
                return TypeDef.BOOLEAN;
            case "binary":
                return new TypeDef.Binary(tryParseMaxSize());
            case "string":
                return new TypeDef.StringUnicode(tryParseMaxSize());
            case "time":
                return parseTimeType();
            default:
                return parseRefOrEnumType(name);
        }
    }
    private int tryParseMaxSize() throws IOException {
        if (ws().tryPop('(')) {
            int maxSize = ws().parseUInt();
            ws().pop(')');
            return maxSize;
        }
        return -1;
    }
    private TypeDef.Time parseTimeType() throws IOException {
        try {
            TimeUnit unit = TimeUnit.valueOf(ws().pop('(').parseName());
            Epoch epoch = Epoch.valueOf(ws().pop(',').parseName());
            TimeZone timeZone = null;
            if (ws().tryPop(',')) {
                timeZone = TimeZone.getTimeZone(ws().parseName());
                ws();
            }
            pop(')');
            return new TypeDef.Time(unit, epoch, timeZone);
        } catch(IllegalArgumentException e) {
            throw new IOException(e);
        }
    }
    private TypeDef parseRefOrEnumType(String name) throws IOException {
        if (tryPop('*')) {
            return new TypeDef.DynamicReference(name.equals("object") ? null : name);
        }
        if (peek() == '/' || ws().peek() == '|') {
            return parseEnumType(name);
        }
        return new TypeDef.Reference(name);
    }

    private TypeDef.Enum parseEnumType(String firstSymbolName) throws IOException {
        LinkedList<Symbol> symbols = new LinkedList<>();
        symbols.add(new Symbol(firstSymbolName, tryParseSymbolId(0)));
        while(ws().tryPop('|')) {
            symbols.add(new Symbol(ws().parseName(), ws().tryParseSymbolId(symbols.getLast().getId()+1)));
        }
        return new TypeDef.Enum(symbols);
    }


    private void parseNamedType(SchemaVisitor sv, String name) throws IOException {
        TypeDef type = pop('=').ws().parseType();
        NamedTypeVisitor nv = sv.visitNamedType(name, type);
        consumeAnnotations(nv);
        if (nv != null) {
            nv.visitEnd();
        }
    }

    public static Schema parse(String s) throws IOException {
        return new SchemaParser(new StringReader(s)).parse();
    }

}
