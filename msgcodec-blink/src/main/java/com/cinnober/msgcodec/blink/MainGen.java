/*
 * Copyright (c) 2014 Cinnober Financial Technology AB, Stockholm,
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

package com.cinnober.msgcodec.blink;

import com.cinnober.msgcodec.MsgObject;
import com.cinnober.msgcodec.ProtocolDictionary;
import com.cinnober.msgcodec.ProtocolDictionaryBuilder;
import com.cinnober.msgcodec.anot.Dynamic;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Required;
import com.cinnober.msgcodec.anot.Sequence;
import com.cinnober.msgcodec.util.ByteArrays;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.logging.LogManager;

/**
 *
 * @author mikael.brannstrom
 */
public class MainGen {
    public static void main(String... args) throws Exception {
        System.clearProperty("java.util.logging.config.class");
        System.setProperty("java.util.logging.config.file", "logging.properties");
        LogManager.getLogManager().readConfiguration();


        ProtocolDictionary dict = new ProtocolDictionaryBuilder().build(Foo.class, Bar.class, Foo2.class);
        System.out.println("dict: " + dict);
//        CodeGenerator codeGen = new CodeGenerator();
//        byte[] generateClass = codeGen.generateClass(dict, 3);
//        System.out.println("done! " + generateClass.length + " bytes");

        BlinkCodec codec = new BlinkCodec(dict);
        Thread.sleep(100); // just make sure the logging output comes before stdout
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Foo foo = new Foo();
        foo.i32 = 123;
        foo.stringsReq = Arrays.asList("one", "two");
        foo.doublesReq = new double[1];
        codec.encode(foo, out);
        System.out.println("HEX: " + ByteArrays.toHex(out.toByteArray()));
        System.out.println("Decoded: "+codec.decode(new ByteArrayInputStream(out.toByteArray())));


        System.out.println("Yay!");

    }

    @Id(123)
    public static class Foo extends MsgObject {
        public int i32;
        public Integer i32Obj;
        public Bar bar;
        public double[] doubles;
        @Required
        public double[] doublesReq;

        @Sequence(String.class)
        public List<String> strings;
        @Required @Sequence(String.class)
        public List<String> stringsReq;
        public String[] strings2;
    }
    public static class Bar extends MsgObject {
        public Bar bar;
    }
    @Id(456)
    public static class Foo2 extends Foo {
        public String string;

        @Dynamic
        public Foo anyFoo;
    }

}
