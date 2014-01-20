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

import com.cinnober.msgcodec.Epoch;
import java.text.ParseException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
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
