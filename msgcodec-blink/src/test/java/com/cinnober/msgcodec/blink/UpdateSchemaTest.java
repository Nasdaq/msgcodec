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

package com.cinnober.msgcodec.blink;

import com.cinnober.msgcodec.IncompatibleSchemaException;
import com.cinnober.msgcodec.MsgCodec;
import com.cinnober.msgcodec.MsgObject;
import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.SchemaBinder;
import com.cinnober.msgcodec.SchemaBinder.Direction;
import com.cinnober.msgcodec.SchemaBuilder;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Name;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UpdateSchemaTest {
    private void testWideningAndNarrowingSchemaBuild(Class<?>... classes) throws IncompatibleSchemaException {
        for (int i=0;i<classes.length-1;i++) {
            for (int j=i+1;j<classes.length;j++) {
                Schema schemaNarrow = new SchemaBuilder().build(classes[i]);
                Schema schemaWide = new SchemaBuilder().build(classes[j]);

                boolean exceptionThrown = false;
                try {
                    new SchemaBinder(schemaWide).bind(schemaNarrow, g -> Direction.OUTBOUND);
                } catch (IncompatibleSchemaException e) {
                    exceptionThrown = true;
                }
                assertTrue("No IncompatibleSchemaException thrown for "+classes[j]+"->"+classes[i]+"!",exceptionThrown);

                new SchemaBinder(schemaWide).bind(schemaNarrow, g -> Direction.INBOUND);

                exceptionThrown = false;
                try {
                    new SchemaBinder(schemaNarrow).bind(schemaWide, g -> Direction.INBOUND);
                } catch (IncompatibleSchemaException e) {
                    exceptionThrown = true;
                }
                assertTrue("No IncompatibleSchemaException thrown for "+classes[i]+"<-"+classes[j]+"!",exceptionThrown);

                new SchemaBinder(schemaNarrow).bind(schemaWide, g -> Direction.OUTBOUND);

                exceptionThrown = false;
                try {
                    new SchemaBinder(schemaNarrow).bind(schemaWide, g -> Direction.BOTH);
                } catch (IncompatibleSchemaException e) {
                    exceptionThrown = true;
                }
                assertTrue("No IncompatibleSchemaException thrown for "+classes[i]+"<->"+classes[j]+"!",exceptionThrown);
            }
        }
    }

    @Test
    public void testWideningAndNarrowing() throws IOException, IncompatibleSchemaException {
        // wider direction --->
        testWideningAndNarrowingSchemaBuild(DecimalNarrow.class, DecimalWide.class);
        testWideningAndNarrowingSchemaBuild(ByteNum.class, OptByteNum.class);
        testWideningAndNarrowingSchemaBuild(ByteNum.class, ShortNum.class, IntNum.class, LongNum.class);
        testWideningAndNarrowingSchemaBuild(EnumEntNarrow.class, EnumEntWide.class);
        testWideningAndNarrowingSchemaBuild(CarWithRequiredYear.class, Car.class);
    }


    @Test
    public void testWidenDecimalFieldOutbound() throws IOException, IncompatibleSchemaException {
        Schema schemaNarrow = new SchemaBuilder().build(DecimalNarrow.class);
        Schema schemaWide = new SchemaBuilder().build(DecimalWide.class);

        Schema schema = new SchemaBinder(schemaNarrow).bind(schemaWide, g -> Direction.OUTBOUND);

        MsgCodec codec1 = new BlinkCodecFactory(schema).createCodec();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        codec1.encode(new DecimalNarrow(12.0f), bout);

        MsgCodec codec2 = new BlinkCodecFactory(schemaWide).createCodec();
        DecimalWide msg = (DecimalWide) codec2.decode(new ByteArrayInputStream(bout.toByteArray()));
        assertEquals(12.0, msg.decimal, 0.00001);
    }

    @Test
    public void testWidenDecimalFieldInbound() throws IOException, IncompatibleSchemaException {
        Schema schemaNarrow = new SchemaBuilder().build(DecimalNarrow.class);
        Schema schemaWide = new SchemaBuilder().build(DecimalWide.class);

        Schema schema = new SchemaBinder(schemaWide).bind(schemaNarrow, g -> Direction.INBOUND);

        MsgCodec codec1 = new BlinkCodecFactory(schemaNarrow).createCodec();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        codec1.encode(new DecimalNarrow(12.0f), bout);

        MsgCodec codec2 = new BlinkCodecFactory(schema).createCodec();
        DecimalWide msg = (DecimalWide) codec2.decode(new ByteArrayInputStream(bout.toByteArray()));
        assertEquals(12.0, msg.decimal, 0.00001);
    }


    private void reqFieldTest(boolean reqOnSrc, boolean reqOnDst, Direction direction) throws IncompatibleSchemaException, IOException {
        Schema source = new SchemaBuilder().build(reqOnSrc ? CarWithRequiredYear.class : Car.class);
        Schema dest = new SchemaBuilder().build(reqOnDst ? CarWithRequiredYear.class : Car.class);

        Schema sendSchema = new SchemaBinder(source).bind(dest, g -> direction);
        MsgCodec codec = new BlinkCodecFactory(sendSchema).createCodec();
        ByteArrayOutputStream bout = new ByteArrayOutputStream();

        if (direction.equals(Direction.OUTBOUND)) {
            Object entity = reqOnSrc ? new CarWithRequiredYear() : new Car();
            codec.encode(entity, bout);
            MsgCodec codecDest = new BlinkCodecFactory(dest).createCodec();
            Object decoded = codecDest.decode(new ByteArrayInputStream(bout.toByteArray()));
            assertEquals(reqOnDst ? CarWithRequiredYear.class : Car.class, decoded.getClass());
        } else {
            Object entity = reqOnDst ? new CarWithRequiredYear() : new Car();
            MsgCodec codecDest = new BlinkCodecFactory(dest).createCodec();
            codecDest.encode(entity, bout);
            Object decoded = codec.decode(new ByteArrayInputStream(bout.toByteArray()));
            assertEquals(reqOnSrc ? CarWithRequiredYear.class : Car.class, decoded.getClass());
        }
    }

    @Test
    public void testReqOnlyOnSourceOutbound() throws IncompatibleSchemaException, IOException {
        reqFieldTest(true, false, Direction.OUTBOUND);
    }

    @Test(expected = IncompatibleSchemaException.class)
    public void testReqOnlyOnSourceInbound() throws IncompatibleSchemaException, IOException {
        reqFieldTest(true, false, Direction.INBOUND);
    }

    @Test(expected = IncompatibleSchemaException.class)
    public void testReqOnlyOnDstOutbound() throws IncompatibleSchemaException, IOException {
        reqFieldTest(false, true, Direction.OUTBOUND);
    }

    @Test
    public void testReqOnlyOnDstInbound() throws IncompatibleSchemaException, IOException {
        reqFieldTest(false, true, Direction.INBOUND);
    }

    @Test
    public void testSourceWithOptionalFieldInbound() throws IOException, IncompatibleSchemaException {
        Schema schemaWithField = new SchemaBuilder().build(DogWithOptionalSize.class);
        Schema schemaWithoutField = new SchemaBuilder().build(Dog.class);
        new SchemaBinder(schemaWithField).bind(schemaWithoutField, g -> Direction.INBOUND);
    }

    @Test
    public void testDestWithOptionalFieldInbound() throws IOException, IncompatibleSchemaException {
        Schema schemaWithField = new SchemaBuilder().build(DogWithOptionalSize.class);
        Schema schemaWithoutField = new SchemaBuilder().build(Dog.class);
        new SchemaBinder(schemaWithoutField).bind(schemaWithField, g -> Direction.INBOUND);
    }

    @Test
    public void testSourceWithOptionalFieldOutbound() throws IOException, IncompatibleSchemaException {
        Schema schemaWithField = new SchemaBuilder().build(DogWithOptionalSize.class);
        Schema schemaWithoutField = new SchemaBuilder().build(Dog.class);
        new SchemaBinder(schemaWithField).bind(schemaWithoutField, g -> Direction.OUTBOUND);
    }

    @Test
    public void testDestWithOptionalFieldOutbound() throws IOException, IncompatibleSchemaException {
        Schema schemaWithField = new SchemaBuilder().build(DogWithOptionalSize.class);
        Schema schemaWithoutField = new SchemaBuilder().build(Dog.class);
        new SchemaBinder(schemaWithoutField).bind(schemaWithField, g -> Direction.OUTBOUND);
    }

    public enum EnumNarrow {
        VALUE3, VALUE1, VALUE2,
    }

    public enum EnumWide {
        DUMMY_1, VALUE1, VALUE2, VALUE3, ADDITIONAL_VALUE,
    }

    @Name("EnumEnt")
    @Id(1)
    public static class EnumEntNarrow extends MsgObject {
        public EnumNarrow enumeration;

        public EnumEntNarrow() {
        }

        public EnumEntNarrow(EnumNarrow eValue) {
            enumeration = eValue;
        }
    }

    @Name("EnumEnt")
    @Id(1)
    public static class EnumEntWide extends MsgObject {
        public EnumWide enumeration;

        public EnumEntWide() {
        }

        public EnumEntWide(EnumWide eValue) {
            enumeration = eValue;
        }
    }

    @Name("Decimal")
    @Id(2)
    public static class DecimalNarrow extends MsgObject {
        public float decimal;

        public DecimalNarrow() {
        }

        public DecimalNarrow(float decimal) {
            this.decimal=decimal;
        }
    }

    @Name("Decimal")
    @Id(2)
    public static class DecimalWide extends MsgObject {
        public double decimal;

        public DecimalWide() {
        }

        public DecimalWide(double decimal) {
            this.decimal=decimal;
        }
    }


    @Name("Number")
    @Id(3)
    public static class OptByteNum extends MsgObject {
        public Byte n;
    }

    @Name("Number")
    @Id(3)
    public static class ByteNum extends MsgObject {
        public byte n;
    }

    @Name("Number")
    @Id(3)
    public static class ShortNum extends MsgObject {
        public short n;
    }

    @Name("Number")
    @Id(3)
    public static class IntNum extends MsgObject {
        public int n;
    }

    @Name("Number")
    @Id(3)
    public static class LongNum extends MsgObject {
        public long n;
    }


    @Name("Car")
    @Id(10)
    public static class Car extends MsgObject {
        public String number;

        public Car() {
            number = "CAR " + System.nanoTime();
        }
    }

    @Name("Car")
    @Id(10)
    public static class CarWithRequiredYear extends MsgObject {
        public String number;
        public int year;

        public CarWithRequiredYear() {
            number = "CAR " + System.nanoTime();
            year = (int)(System.nanoTime()%3000);
        }
    }

    @Name("Dog")
    @Id(11)
    public static class Dog extends MsgObject {
        public String number;

        public Dog() {
            number = "Dog " + System.nanoTime();
        }
    }

    @Name("Dog")
    @Id(11)
    public static class DogWithOptionalSize extends MsgObject {
        public String number;
        public Integer size;

        public DogWithOptionalSize() {
            number = "Dog " + System.nanoTime();
            size = (int)(System.nanoTime()%3000);
        }
    }



}
