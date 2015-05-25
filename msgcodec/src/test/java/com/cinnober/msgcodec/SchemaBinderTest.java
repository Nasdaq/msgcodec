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
package com.cinnober.msgcodec;

import com.cinnober.msgcodec.SchemaBinder.Direction;
import com.cinnober.msgcodec.anot.Annotate;
import com.cinnober.msgcodec.anot.Name;
import com.cinnober.msgcodec.anot.Unsigned;
import org.junit.Test;
import static org.junit.Assert.*;

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

    @Test(expected = IncompatibleSchemaException.class)
    public void testUpgradeFail() throws IncompatibleSchemaException {
        Schema schema1 = new SchemaBuilder().addMessages(FooReqV1.class, FooRspV1.class).build();
        Schema schema2 = new SchemaBuilder().addMessages(FooReqV2.class, FooRspV2.class).build();
        Schema schema = new SchemaBinder(schema1).bind(schema2, g -> Direction.BOTH);
    }

    @Test
    public void testUpgradeBoundToGroupOk() throws IncompatibleSchemaException {
        Schema schema1 = new SchemaBuilder().addMessages(FooReqV1.class, FooRspV1.class).build();
        Schema schema2 = new SchemaBuilder().addMessages(FooReqV2.class, FooRspV2.class).build();
        schema1 = Group.bind(schema1);
        schema2 = Group.bind(schema2);
        Schema schema = new SchemaBinder(schema1).bind(schema2, SchemaBinderTest::getDirAtClient);
    }

    @Test(expected = IncompatibleSchemaException.class)
    public void testUpgradeBoundToGroupFail() throws IncompatibleSchemaException {
        Schema schema1 = new SchemaBuilder().addMessages(FooReqV1.class, FooRspV1.class).build();
        Schema schema2 = new SchemaBuilder().addMessages(FooReqV2.class, FooRspV2.class).build();
        schema1 = Group.bind(schema1);
        schema2 = Group.bind(schema2);
        Schema schema = new SchemaBinder(schema1).bind(schema2, g -> Direction.BOTH);
    }

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
