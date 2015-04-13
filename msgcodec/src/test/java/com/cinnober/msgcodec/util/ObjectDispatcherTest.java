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
package com.cinnober.msgcodec.util;

import static org.junit.Assert.*;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
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
                Arrays.asList(new MyService(), new MyErrorHandler()));

        assertEquals("*Hello*", dispatcher.dispatch("Hello")); // text
        assertEquals(2, dispatcher.dispatch(1)); // integer
        assertEquals(null, dispatcher.dispatch(1.0)); // number
        try {
            dispatcher.dispatch(new Date()); // object
            fail("Expected exception");
        } catch (InvocationTargetException e) {}
    }

    @Test
    public void test2Params() throws Exception {
        ObjectDispatcher dispatcher = new ObjectDispatcher(
                Arrays.asList(new MyService2(), new MyErrorHandler2()),
                new Class<?>[] { String.class });

        assertEquals("*Hello*", dispatcher.dispatch("Hello", "Param2")); // text
        assertEquals(2, dispatcher.dispatch(1, "Param2")); // integer
        assertEquals(null, dispatcher.dispatch(1.0, "Param2")); // number
        try {
            dispatcher.dispatch(new Date(), "Param2"); // object
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

    public static class MyService2 {
        public void onNumber(Number number, String arg2) {
            System.out.println("onNumber2: " + number);;
        }
        public int onInteger(Integer number, String arg2) {
            System.out.println("onInteger2: " + number);;
            return number + 1;
        }
        public String onText(String s, String arg2) {
            System.out.println("onText2: " + s);
            return "*" + s + "*";
        }
        public void fooDate(Date date, String arg2) {
            System.out.println("fooDate2: " + date);
            fail("Should be ignored");
        }
    }

    public static class MyErrorHandler2 {
        public void handleAny(Object any, String arg2) {
            System.out.println("handleAny2: " + any);
            throw new RuntimeException("Unhandled object: " + any);
        }
    }

}
