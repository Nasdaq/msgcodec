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

import com.cinnober.msgcodec.SchemaBinder.Direction;
import com.cinnober.msgcodec.anot.Annotate;
import com.cinnober.msgcodec.anot.Name;
import com.cinnober.msgcodec.anot.Unsigned;
import org.junit.Test;

/**
 *
 * @author mikael.brannstrom
 */
public class SchemaBinderTest {

    public SchemaBinderTest() {
    }

    @Test
    public void testUpgradeOk() throws IncompatibleSchemaException {
        Schema schema1 = new SchemaBuilder().addMessages(FooReqV1.class, FooRspV1.class).build();
        Schema schema2 = new SchemaBuilder().addMessages(FooReqV2.class, FooRspV2.class).build();
        Schema schema = new SchemaBinder(schema1).bind(schema2, SchemaBinderTest::getDirAtClient);
    }

//    @Test(expected = IncompatibleSchemaException.class)
//    public void testUpgradeFail() throws IncompatibleSchemaException {
//        Schema schema1 = new SchemaBuilder().addMessages(FooReqV1.class, FooRspV1.class).build();
//        Schema schema2 = new SchemaBuilder().addMessages(FooReqV2.class, FooRspV2.class).build();
//        Schema schema = new SchemaBinder(schema1).bind(schema2, g -> Direction.BOTH);
//    }

    @Test
    public void testUpgradeBoundToGroupOk() throws IncompatibleSchemaException {
        Schema schema1 = new SchemaBuilder().addMessages(FooReqV1.class, FooRspV1.class).build();
        Schema schema2 = new SchemaBuilder().addMessages(FooReqV2.class, FooRspV2.class).build();
        schema1 = Group.bind(schema1);
        schema2 = Group.bind(schema2);
        Schema schema = new SchemaBinder(schema1).bind(schema2, SchemaBinderTest::getDirAtClient);
    }

//    @Test(expected = IncompatibleSchemaException.class)
//    public void testUpgradeBoundToGroupFail() throws IncompatibleSchemaException {
//        Schema schema1 = new SchemaBuilder().addMessages(FooReqV1.class, FooRspV1.class).build();
//        Schema schema2 = new SchemaBuilder().addMessages(FooReqV2.class, FooRspV2.class).build();
//        schema1 = Group.bind(schema1);
//        schema2 = Group.bind(schema2);
//        Schema schema = new SchemaBinder(schema1).bind(schema2, g -> Direction.BOTH);
//    }

    public static Direction getDirAtClient(Annotatable<?> a) {
        String s = a.getAnnotation("dir");
        if (s == null) {
            return Direction.BOTH;
        }
        switch (s) {
            case "c2s":
                return Direction.OUTBOUND;
            case "s2c":
                return Direction.INBOUND;
            default:
                return Direction.BOTH;
        }
    }

    @Annotate("dir=both")
    @Name("Foo")
    public static class FooV1 extends MsgObject {
        public String text;
    }

    @Annotate("dir=c2s")
    @Name("FooReq")
    public static class FooReqV1 extends MsgObject {
        public FooV1 foo;
        @Unsigned
        public long reqId;
    }

    @Annotate("dir=s2c")
    @Name("FooRsp")
    public static class FooRspV1 extends MsgObject {
        public FooV1 foo;
        @Unsigned
        public long reqId;
    }

    @Annotate("dir=both")
    @Name("Foo")
    public static class FooV2 extends MsgObject {
        public String text;
    }

    @Annotate("dir=c2s")
    @Name("FooReq")
    public static class FooReqV2 extends MsgObject {
        public FooV2 foo;
        @Unsigned
        public long reqId;

        public Long newOptionalField;
    }

    @Annotate("dir=s2c")
    @Name("FooRsp")
    public static class FooRspV2 extends MsgObject {
        public FooV2 foo;
        @Unsigned
        public long reqId;

        public long newRequiredField;
    }

}
