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

import com.cinnober.msgcodec.ProtocolDictionary;
import com.cinnober.msgcodec.ProtocolDictionaryBuilder;
import com.cinnober.msgcodec.anot.Id;
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
        CodeGenerator codeGen = new CodeGenerator();
        byte[] generateClass = codeGen.generateClass(dict, 3);
        System.out.println("done! " + generateClass.length + " bytes");
    }

    @Id(123)
    public static class Foo {
        int i32;
        Integer i32Obj;
        Bar bar;
    }
    public static class Bar {
        Bar bar;
    }
    @Id(456)
    public static class Foo2 extends Foo {
        String string;
    }

}
