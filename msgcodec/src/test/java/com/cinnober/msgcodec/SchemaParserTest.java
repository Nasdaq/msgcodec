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

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mikael.brannstrom
 */
public class SchemaParserTest {

    public SchemaParserTest() {
    }

    @Test
    public void testGroupDefPlain() throws IOException {
        assertEquals(
                new Schema(
                        Arrays.asList(
                                new GroupDef("Message", -1, null, Collections.emptyList(), null, null)
                        ),
                        null),
                SchemaParser.parse("Message"));
    }

    @Test
    public void testGroupDefWithAnnotation() throws IOException {
        Map<String,String> annotations = new HashMap<>();
        annotations.put("key", "value");
        assertEquals(
                new Schema(
                        Arrays.asList(
                                new GroupDef("Message", -1, null, Collections.emptyList(), annotations, null)
                        ),
                        null),
                SchemaParser.parse("@key=\"value\" Message"));
    }

    @Test
    public void testGroupDefWithId1() throws IOException {
        assertEquals(
                new Schema(
                        Arrays.asList(
                                new GroupDef("Message", 1, null, Collections.emptyList(), null, null)
                        ),
                        null),
                SchemaParser.parse("Message/1"));
    }

    @Test
    public void testGroupDefWithId12() throws IOException {
        assertEquals(
                new Schema(
                        Arrays.asList(
                                new GroupDef("Message", 12, null, Collections.emptyList(), null, null)
                        ),
                        null),
                SchemaParser.parse("Message/12"));
    }
    @Test
    public void testGroupDefSuper() throws IOException {
        assertEquals(
                new Schema(
                        Arrays.asList(
                                new GroupDef("Foo", -1, "Bar", Collections.emptyList(), null, null),
                                new GroupDef("Bar", -1, null, Collections.emptyList(), null, null)
                        ),
                        null),
                SchemaParser.parse("Foo:Bar Bar"));
    }
    @Test
    public void testGroupDefSuperWithId1() throws IOException {
        assertEquals(
                new Schema(
                        Arrays.asList(
                                new GroupDef("Foo", 1, "Bar", Collections.emptyList(), null, null),
                                new GroupDef("Bar", -1, null, Collections.emptyList(), null, null)
                        ),
                        null),
                SchemaParser.parse("Foo/1:Bar Bar"));
    }

    @Test
    public void testGroupDefOneField() throws IOException {
        assertEquals(
                new Schema(
                        Arrays.asList(
                                new GroupDef("Message", -1, null, 
                                        Arrays.asList(new FieldDef("i", -1, true, TypeDef.INT8, null, null)),
                                        null, null)
                        ),
                        null),
                SchemaParser.parse("Message -> i8 i"));
    }

    @Test
    public void testGroupDefTwoFields() throws IOException {
        assertEquals(
                new Schema(
                        Arrays.asList(
                                new GroupDef("Message", -1, null,
                                        Arrays.asList(
                                                new FieldDef("i", -1, true, TypeDef.INT8, null, null),
                                                new FieldDef("x", -1, true, TypeDef.STRING, null, null)
                                        ),
                                        null, null)
                        ),
                        null),
                SchemaParser.parse("Message -> i8 i, string x"));
    }

    @Test(expected = IOException.class)
    public void testGroupDefWithErrorId() throws IOException {
        SchemaParser.parse("Message/abc");
    }
    
    @Test(expected = IOException.class)
    public void testGroupDefWithErrorName() throws IOException {
        SchemaParser.parse("123Message");
    }

}
