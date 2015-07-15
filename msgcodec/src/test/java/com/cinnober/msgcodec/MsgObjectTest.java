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

import com.cinnober.msgcodec.anot.Dynamic;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Time;
import java.util.Date;
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

    @Test
    public void testNumbersToString() {
        assertEquals("Numbers [f32=0.0, f64=0.0]", new Numbers(0,0).toString());
    }

    @Test
    public void testDynamicGroupToString() {
        assertEquals("FooBar [fooOrBar=Foo [x=1]]", new FooBar(new Foo(1)).toString());
        assertEquals("FooBar [fooOrBar=Bar [x=1, y=2]]", new FooBar(new Bar(1, 2)).toString());
    }

    @Test
    public void testDynamicGroupEquals() {
        assertEquals(new FooBar(new Foo(1)), new FooBar(new Foo(1)));
        assertEquals(new FooBar(new Bar(1, 2)), new FooBar(new Bar(1, 2)));
        assertNotEquals(new FooBar(new Bar(1, 2)), new FooBar(new Bar(1, 3)));
    }

    @Test
    public void testDateTimesToString() {
        DateTimes msg = new DateTimes();
        assertEquals("DateTimes [timestamp=1970-01-01 00:00:00.000, dateTimestamp=null]",
                     msg.toString());
        msg.dateTimestamp = new Date(0L);
        assertEquals("DateTimes [timestamp=1970-01-01 00:00:00.000, dateTimestamp=1970-01-01 00:00:00.000]",
                     msg.toString());
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

    public static class Numbers extends MsgObject {
        @Id(1)
        public float f32;
        @Id(2)
        public double f64;

        public Numbers() {
        }

        public Numbers(float f32, double f64) {
            this.f32 = f32;
            this.f64 = f64;
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

    public static class FooBar extends MsgObject {
        @Id(1)
        @Dynamic
        public Foo fooOrBar;

        public FooBar() {
        }
        public FooBar(Foo fooOrBar) {
            this.fooOrBar = fooOrBar;
        }
    }

    public static class Foo extends MsgObject {
        @Id(1)
        public int x;

        public Foo() {
        }

        public Foo(int x) {
            this.x = x;
        }

    }

    public static class Bar extends Foo {
        @Id(2)
        public int y;

        public Bar() {
        }

        public Bar(int x, int y) {
            super(x);
            this.y = y;
        }
    }

    public static class DateTimes extends MsgObject {
        @Id(1)
        @Time
        public int timestamp;

        @Id(2)
        public Date dateTimestamp;
    }
}
