/*
 * Copyright (c) 2013 Cinnober Financial Technology AB, Stockholm,
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
package com.cinnober.msgcodec.util;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import org.junit.Test;

/**
 * @author Mikael Brannstrom
 *
 */
public class ObjectDispatcherTest {

    @Test
    public void test() throws Exception {
        ObjectDispatcher dispatcher = new ObjectDispatcher(
                Arrays.asList((Class<?>)String.class, Integer.class, Double.class, Date.class),
                Arrays.asList(new MyService(), new MyErrorHandler()));

        assertEquals("*Hello*", dispatcher.dispatch("Hello")); // text
        assertEquals(2, dispatcher.dispatch(1)); // integer
        assertEquals(null, dispatcher.dispatch(1.0)); // number
        try {
            dispatcher.dispatch(new Date()); // object
            fail("Expected exception");
        } catch (InvocationTargetException e) {}

    }


    public static class MyService {
        public void onNumber(Number number) {
            System.out.println("onNumber: " + number);;
        }
        public int onInteger(Integer number) {
            System.out.println("onInteger: " + number);;
            return number + 1;
        }
        public String onText(String s) {
            System.out.println("onText: " + s);
            return "*" + s + "*";
        }
        public void fooDate(Date date) {
            System.out.println("fooDate: " + date);
            fail("Should be ignored");
        }
    }

    public static class MyErrorHandler {
        public void handleAny(Object any) {
            System.out.println("handleAny: " + any);
            throw new RuntimeException("Unhandled object: " + any);
        }
    }
}
