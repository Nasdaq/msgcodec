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

package com.cinnober.msgcodec;

import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Unsigned;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mikael.brannstrom
 */
public class MsgObjectTest {

    @Test
    public void testFieldOrderToString() {
        assertEquals("FieldOrder [i1=0, i4=0, i2=0, i5=0, i3=0, i6=0]", new FieldOrder().toString());
    }
    @Test
    public void testFieldOrderEquals() {
        assertEquals(
                new FieldOrder(),
                new FieldOrder());
    }

    @Test
    public void testBasicToString() {
        assertEquals("Basic [int32=123, string=Hello]", new Basic(123, "Hello").toString());
    }
    @Test
    public void testBasicEquals() {
        assertEquals(
                new Basic(123, "Hello"),
                new Basic(123, "Hello"));
        assertEquals(
                new Basic(123, null),
                new Basic(123, null));


        assertNotEquals(
                new Basic(123, "Hello"),
                new Basic(124, "Hello"));
        assertNotEquals(
                new Basic(123, "Hello"),
                new Basic(123, "Helloo"));
        assertNotEquals(
                new Basic(123, "Hello"),
                new Basic(123, null));
        assertNotEquals(
                new Basic(123, null),
                new Basic(123, "Hello"));
    }

    @Test
    public void testArrayMsgToString() {
        assertEquals("ArrayMsg [strings=null, ints=null, basics=null]", new ArrayMsg(null, null, null).toString());
        assertEquals("ArrayMsg [strings=[], ints=[], basics=[]]", 
                new ArrayMsg(new String[0], new int[0], new Basic[0]).toString());
        assertEquals("ArrayMsg [strings=[a], ints=[1], basics=[Basic [int32=123, string=Hello]]]",
                new ArrayMsg(new String[]{"a"}, new int[]{1},
                        new Basic[]{new Basic(123, "Hello")}).toString());
        assertEquals("ArrayMsg [strings=[a, b], ints=[1, 2], basics=[Basic [int32=123, string=Hello], Basic [int32=124, string=Hello]]]",
                new ArrayMsg(new String[]{"a", "b"}, new int[]{1, 2},
                        new Basic[]{new Basic(123, "Hello"), new Basic(124, "Hello")}).toString());
        assertEquals("ArrayMsg [strings=[], ints=[], basics=[null]]",
                new ArrayMsg(new String[0], new int[0], new Basic[]{null}).toString());
        assertEquals("ArrayMsg [strings=[], ints=[], basics=[null, null]]",
                new ArrayMsg(new String[0], new int[0], new Basic[]{null, null}).toString());
    }

    @Test
    public void testArrayMsgEquals() {
        assertEquals(
                new ArrayMsg(null, null, null),
                new ArrayMsg(null, null, null));
        assertEquals(
                new ArrayMsg(new String[0], new int[0], new Basic[0]),
                new ArrayMsg(new String[0], new int[0], new Basic[0]));
        assertEquals(
                new ArrayMsg(new String[]{"a"}, new int[]{1}, new Basic[]{new Basic(123, "Hello")}),
                new ArrayMsg(new String[]{"a"}, new int[]{1}, new Basic[]{new Basic(123, "Hello")}));
        assertEquals(
                new ArrayMsg(new String[]{"a", "b"}, new int[]{1, 2}, new Basic[]{new Basic(123, "Hello"), new Basic(123, "Hello")}),
                new ArrayMsg(new String[]{"a", "b"}, new int[]{1, 2}, new Basic[]{new Basic(123, "Hello"), new Basic(123, "Hello")}));
    }

    public static class FieldOrder extends MsgObject {
        @Id(1)
        public int i1;
        @Id(3)
        public int i2;
        @Id(5)
        public int i3;
        @Id(2)
        public int i4;
        @Id(4)
        public int i5;
        @Id(6)
        public int i6;
    }

    public static class Basic extends MsgObject {
        @Id(1)
        public int int32;
        @Id(2)
        public String string;

        public Basic() {
        }

        public Basic(int int32, String string) {
            this.int32 = int32;
            this.string = string;
        }
    }

    public static class ArrayMsg extends MsgObject {
        @Id(1)
        public String[] strings;
        @Id(2)
        public int[] ints;
        @Id(3)
        public Basic[] basics;

        public ArrayMsg() {
        }

        public ArrayMsg(String[] strings, int[] ints, Basic[] basics) {
            this.strings = strings;
            this.ints = ints;
            this.basics = basics;
        }
    }

}
