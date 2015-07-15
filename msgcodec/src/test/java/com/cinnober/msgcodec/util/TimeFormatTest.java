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

import com.cinnober.msgcodec.Epoch;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mikael.brannstrom
 */
public class TimeFormatTest {

    public TimeFormatTest() {
    }

    @Test
    public void testFormatUnixDate() {
        StringBuilder s = new StringBuilder();
        TimeFormat.formatUnixDate(0, s);
        assertEquals("1970-01-01", s.toString());
    }

    @Test
    public void testParseUnixDate() throws ParseException {
        assertEquals(0, TimeFormat.parseUnixDate("1970-01-01", new ParsePosition(0)));
    }

    @Test
    public void testSdfCrossValidation() throws ParseException {
        TimeFormat tf = TimeFormat.getTimeFormat(TimeUnit.MILLISECONDS, Epoch.UNIX);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Random random = new Random(12345678L);
        long minValue = -5364792000_000L; // ~ year 1800
        long maxValue = 32504_328_000_000L; // ~ year 3000

        int iterations = 1000;
        for (int i=0; i<iterations; i++) {
            long value = minValue + (Math.abs(random.nextLong()) % (maxValue-minValue));

            System.out.print("i="+i+", value="+value+", ~year="+(value/(365.25*24*3600*1000)+1970));
            String text = tf.format(value);
            String sdfText = sdf.format(new Date(value));
            System.out.println(", formatted: " + text);
            assertEquals("format", sdfText, text);

            long parseValue = tf.parse(text);
            assertEquals("parse", value, parseValue);
        }
    }

}
