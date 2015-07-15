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
package com.cinnober.msgcodec.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import java.io.IOException;
import java.io.StringWriter;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mikael.brannstrom
 */
public class JsonValueHandlerTest {

    private final JsonFactory f;

    public JsonValueHandlerTest() {
        f = new JsonFactory();
    }

    @Test
    public void testSafeUInt64EncodeMaxUInt64() throws IOException {
        StringWriter out = new StringWriter();
        JsonGenerator g = f.createGenerator(out);

        JsonValueHandler.UINT64_SAFE.writeValue(-1L, g);
        g.flush();
        assertEquals("\"18446744073709551615\"", out.toString());
    }
    @Test
    public void testUInt64EncodeMaxUInt64() throws IOException {
        StringWriter out = new StringWriter();
        JsonGenerator g = f.createGenerator(out);

        JsonValueHandler.UINT64.writeValue(-1L, g);
        g.flush();
        assertEquals("18446744073709551615", out.toString());
    }

    @Test
    public void testSafeUInt64EncodeMaxLong() throws IOException {
        StringWriter out = new StringWriter();
        JsonGenerator g = f.createGenerator(out);

        JsonValueHandler.UINT64_SAFE.writeValue(Long.MAX_VALUE, g);
        g.flush();
        assertEquals("\"9223372036854775807\"", out.toString());
    }

    @Test
    public void testUInt64EncodeMaxLong() throws IOException {
        StringWriter out = new StringWriter();
        JsonGenerator g = f.createGenerator(out);

        JsonValueHandler.UINT64.writeValue(Long.MAX_VALUE, g);
        g.flush();
        assertEquals("9223372036854775807", out.toString());
    }

    @Test
    public void testSafeUInt64Encode10() throws IOException {
        StringWriter out = new StringWriter();
        JsonGenerator g = f.createGenerator(out);

        JsonValueHandler.UINT64_SAFE.writeValue(10L, g);
        g.flush();
        assertEquals("10", out.toString());
    }

    @Test
    public void testSafeUInt64DecodeMaxUInt64() throws IOException {
        JsonParser p = f.createParser("\"18446744073709551615\"");
        p.nextToken();
        assertEquals(-1L, JsonValueHandler.UINT64_SAFE.readValue(p).longValue());
    }
    @Test
    public void testUInt64DecodeMaxUInt64() throws IOException {
        JsonParser p = f.createParser("18446744073709551615");
        p.nextToken();
        assertEquals(-1L, JsonValueHandler.UINT64.readValue(p).longValue());
    }

    @Test
    public void testSafeInt64EncodeMinValue() throws IOException {
        StringWriter out = new StringWriter();
        JsonGenerator g = f.createGenerator(out);

        JsonValueHandler.INT64_SAFE.writeValue(Long.MIN_VALUE, g);
        g.flush();
        assertEquals("\"-9223372036854775808\"", out.toString());
    }
    @Test
    public void testInt64EncodeMinValue() throws IOException {
        StringWriter out = new StringWriter();
        JsonGenerator g = f.createGenerator(out);

        JsonValueHandler.INT64.writeValue(Long.MIN_VALUE, g);
        g.flush();
        assertEquals("-9223372036854775808", out.toString());
    }

    @Test
    public void testSafeInt64EncodeMaxLong() throws IOException {
        StringWriter out = new StringWriter();
        JsonGenerator g = f.createGenerator(out);

        JsonValueHandler.INT64_SAFE.writeValue(Long.MAX_VALUE, g);
        g.flush();
        assertEquals("\"9223372036854775807\"", out.toString());
    }

    @Test
    public void testInt64EncodeMaxLong() throws IOException {
        StringWriter out = new StringWriter();
        JsonGenerator g = f.createGenerator(out);

        JsonValueHandler.INT64.writeValue(Long.MAX_VALUE, g);
        g.flush();
        assertEquals("9223372036854775807", out.toString());
    }

    @Test
    public void testSafeInt64Encode10() throws IOException {
        StringWriter out = new StringWriter();
        JsonGenerator g = f.createGenerator(out);

        JsonValueHandler.INT64_SAFE.writeValue(10L, g);
        g.flush();
        assertEquals("10", out.toString());
    }

    @Test
    public void testSafeInt64DecodeMinValue() throws IOException {
        JsonParser p = f.createParser("\"9223372036854775808\"");
        p.nextToken();
        assertEquals(Long.MIN_VALUE, JsonValueHandler.UINT64_SAFE.readValue(p).longValue());
    }
    @Test
    public void testInt64DecodeMinValue() throws IOException {
        JsonParser p = f.createParser("9223372036854775808");
        p.nextToken();
        assertEquals(Long.MIN_VALUE, JsonValueHandler.UINT64.readValue(p).longValue());
    }


    @Test
    public void testFloat64EncodeNaN() throws IOException {
        StringWriter out = new StringWriter();
        JsonGenerator g = f.createGenerator(out);

        JsonValueHandler.FLOAT64.writeValue(Double.NaN, g);
        g.flush();
        assertEquals("\"NaN\"", out.toString());
    }

    @Test
    public void testFloat64DecodeNaN() throws IOException {
        JsonParser p = f.createParser("\"NaN\"");
        p.nextToken();
        assertTrue(Double.isNaN(JsonValueHandler.FLOAT64.readValue(p)));
    }

    @Test
    public void testFloat64EncodePosInfinity() throws IOException {
        StringWriter out = new StringWriter();
        JsonGenerator g = f.createGenerator(out);

        JsonValueHandler.FLOAT64.writeValue(Double.POSITIVE_INFINITY, g);
        g.flush();
        assertEquals("\"Infinity\"", out.toString());
    }

    @Test
    public void testFloat64DecodePosInfinity() throws IOException {
        JsonParser p = f.createParser("\"Infinity\"");
        p.nextToken();
        assertTrue(Double.POSITIVE_INFINITY == JsonValueHandler.FLOAT64.readValue(p));
    }

    @Test
    public void testFloat64EncodeNegInfinity() throws IOException {
        StringWriter out = new StringWriter();
        JsonGenerator g = f.createGenerator(out);

        JsonValueHandler.FLOAT64.writeValue(Double.NEGATIVE_INFINITY, g);
        g.flush();
        assertEquals("\"-Infinity\"", out.toString());
    }

    @Test
    public void testFloat64DecodeNegInfinity() throws IOException {
        JsonParser p = f.createParser("\"-Infinity\"");
        p.nextToken();
        assertTrue(Double.NEGATIVE_INFINITY == JsonValueHandler.FLOAT64.readValue(p));
    }

    @Test
    public void testFloat32EncodeNaN() throws IOException {
        StringWriter out = new StringWriter();
        JsonGenerator g = f.createGenerator(out);

        JsonValueHandler.FLOAT32.writeValue(Float.NaN, g);
        g.flush();
        assertEquals("\"NaN\"", out.toString());
    }

    @Test
    public void testFloat32DecodeNaN() throws IOException {
        JsonParser p = f.createParser("\"NaN\"");
        p.nextToken();
        assertTrue(Double.isNaN(JsonValueHandler.FLOAT32.readValue(p)));
    }

    @Test
    public void testFloat32EncodePosInfinity() throws IOException {
        StringWriter out = new StringWriter();
        JsonGenerator g = f.createGenerator(out);

        JsonValueHandler.FLOAT32.writeValue(Float.POSITIVE_INFINITY, g);
        g.flush();
        assertEquals("\"Infinity\"", out.toString());
    }

    @Test
    public void testFloat32DecodePosInfinity() throws IOException {
        JsonParser p = f.createParser("\"Infinity\"");
        p.nextToken();
        assertTrue(Float.POSITIVE_INFINITY == JsonValueHandler.FLOAT32.readValue(p));
    }

    @Test
    public void testFloat32EncodeNegInfinity() throws IOException {
        StringWriter out = new StringWriter();
        JsonGenerator g = f.createGenerator(out);

        JsonValueHandler.FLOAT32.writeValue(Float.NEGATIVE_INFINITY, g);
        g.flush();
        assertEquals("\"-Infinity\"", out.toString());
    }

    @Test
    public void testFloat32DecodeNegInfinity() throws IOException {
        JsonParser p = f.createParser("\"-Infinity\"");
        p.nextToken();
        assertTrue(Float.NEGATIVE_INFINITY == JsonValueHandler.FLOAT32.readValue(p));
    }

}
