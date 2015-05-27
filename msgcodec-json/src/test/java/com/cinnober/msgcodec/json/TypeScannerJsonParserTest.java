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

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * 
 * @author mikael.brannstrom
 */
public class TypeScannerJsonParserTest {

    @Test
    public void test1() throws IOException {
        JsonParser p = new JsonFactory().createParser("{\"a\":1, \"$type\":\"x\", \"b\":1.1}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        TypeScannerJsonParser p2 = new TypeScannerJsonParser(p);
        assertEquals("x", p2.findType());

        assertNextField("a", p2);
        assertNextIntValue(1, p2);

        assertNextField("b", p2);
        assertNextFloatValue(1.1, p2);

        assertEquals(JsonToken.END_OBJECT, p2.nextToken());
        assertNull(p2.nextToken());
    }

    @Test
    public void test2() throws IOException {
        JsonParser p = new JsonFactory().createParser("{\"a\":1, \"b\":1.1, \"$type\":\"x\"}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        TypeScannerJsonParser p2 = new TypeScannerJsonParser(p);
        assertEquals("x", p2.findType());

        assertNextField("a", p2);
        assertNextIntValue(1, p2);

        assertNextField("b", p2);
        assertNextFloatValue(1.1, p2);

        assertEquals(JsonToken.END_OBJECT, p2.nextToken());
        assertNull(p2.nextToken());
    }

    private void assertNextField(String expFieldName, JsonParser p) throws IOException {
        assertEquals(JsonToken.FIELD_NAME, p.nextToken());
        assertEquals(expFieldName, p.getText());
    }
    private void assertNextIntValue(int exp, JsonParser p) throws IOException {
        assertEquals(JsonToken.VALUE_NUMBER_INT, p.nextToken());
        assertEquals(exp, p.getIntValue());
    }
    private void assertNextFloatValue(double exp, JsonParser p) throws IOException {
        assertEquals(JsonToken.VALUE_NUMBER_FLOAT, p.nextToken());
        assertEquals(exp, p.getDoubleValue(), 1e-4);
    }

    @Test
    public void testNested1() throws IOException {
        JsonParser p = new JsonFactory().createParser(
                "{\"a\":{\"c\":2, \"$type\":\"y\", \"d\":2.1}, \"$type\":\"x\", \"b\":1.1}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        TypeScannerJsonParser p2 = new TypeScannerJsonParser(p);
        assertEquals("x", p2.findType());

        assertNextField("a", p2);
        assertEquals(JsonToken.START_OBJECT, p2.nextToken());
        // start nested object
        p2.nextToken(); // FIELD_NAME
        assertEquals("y", p2.findType());
        assertNextField("c", p2);
        assertNextIntValue(2, p2);
        assertNextField("d", p2);
        assertNextFloatValue(2.1, p2);
        assertEquals(JsonToken.END_OBJECT, p2.nextToken());
        // end nested object

        assertNextField("b", p2);
        assertNextFloatValue(1.1, p2);

        assertEquals(JsonToken.END_OBJECT, p2.nextToken());
        assertNull(p2.nextToken());
    }

    @Test
    public void testNested2() throws IOException {
        JsonParser p = new JsonFactory().createParser(
                "{\"a\":{\"c\":2, \"d\":2.1, \"$type\":\"y\"}, \"$type\":\"x\", \"b\":1.1}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        TypeScannerJsonParser p2 = new TypeScannerJsonParser(p);
        assertEquals("x", p2.findType());

        assertNextField("a", p2);
        assertEquals(JsonToken.START_OBJECT, p2.nextToken());
        // start nested object
        p2.nextToken(); // FIELD_NAME
        assertEquals("y", p2.findType());
        assertNextField("c", p2);
        assertNextIntValue(2, p2);
        assertNextField("d", p2);
        assertNextFloatValue(2.1, p2);
        assertEquals(JsonToken.END_OBJECT, p2.nextToken());
        // end nested object

        assertNextField("b", p2);
        assertNextFloatValue(1.1, p2);

        assertEquals(JsonToken.END_OBJECT, p2.nextToken());
        assertNull(p2.nextToken());
    }

    @Test
    public void testNested3() throws IOException {
        JsonParser p = new JsonFactory().createParser(
                "{\"a\":{\"$type\":\"y\", \"c\":2, \"d\":2.1}, \"$type\":\"x\", \"b\":1.1}");
        p.nextToken(); // START_OBJECT
        p.nextToken(); // FIELD_NAME
        TypeScannerJsonParser p2 = new TypeScannerJsonParser(p);
        assertEquals("x", p2.findType());

        assertNextField("a", p2);
        assertEquals(JsonToken.START_OBJECT, p2.nextToken());
        // start nested object
        p2.nextToken(); // FIELD_NAME
        assertEquals("y", p2.findType());
        assertNextField("c", p2);
        assertNextIntValue(2, p2);
        assertNextField("d", p2);
        assertNextFloatValue(2.1, p2);
        assertEquals(JsonToken.END_OBJECT, p2.nextToken());
        // end nested object

        assertNextField("b", p2);
        assertNextFloatValue(1.1, p2);

        assertEquals(JsonToken.END_OBJECT, p2.nextToken());
        assertNull(p2.nextToken());
    }

}
