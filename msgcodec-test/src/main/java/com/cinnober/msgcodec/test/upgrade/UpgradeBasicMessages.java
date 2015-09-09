package com.cinnober.msgcodec.test.upgrade;

import com.cinnober.msgcodec.MsgObject;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Name;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Codec upgrade test messages for basic type widening.
 *
 * @author Morgan Johansson, Cinnober Financial Technology North AB
 */
public class UpgradeBasicMessages {
    /**
     * Returns message pairs suitable for testing a codec. This includes border cases.
     * Each message is labeled with a name, e.g. "zero" or "border1" that describes what
     * the message tries to test.
     *
     * All messages are encodable, i.e. any required fields are set.
     *
     * @return a map from message label to message pair.
     */
    public static Map<String, PairedTestProtocols.PairedMessages> createMessages() {
        TreeMap<String, PairedTestProtocols.PairedMessages> testMessages = new TreeMap<>();
        testMessages.put("FloatToDouble",
                new PairedTestProtocols.PairedMessages(new DecimalNarrow(3.14f),new DecimalWide(3.14)));
        testMessages.put("BytePrimitiveToByteObject",
                new PairedTestProtocols.PairedMessages(new ByteNum((byte)90),new OptByteNum((byte)90)));
        testMessages.put("ByteToShort",
                new PairedTestProtocols.PairedMessages(new ByteNum2((byte)90),new ShortNum((byte)90)));
        testMessages.put("ByteToInt",
                new PairedTestProtocols.PairedMessages(new ByteNum3((byte)90),new IntNum((byte)90)));
        testMessages.put("ByteToLong",
                new PairedTestProtocols.PairedMessages(new ByteNum4((byte)90),new LongNum((byte)90)));
        testMessages.put("ShortToInt",
                new PairedTestProtocols.PairedMessages(new ShortNum2((byte)90),new IntNum2((byte)90)));
        testMessages.put("ShortToLong",
                new PairedTestProtocols.PairedMessages(new ShortNum3((byte)90),new LongNum2((byte)90)));
        testMessages.put("IntToLong",
                new PairedTestProtocols.PairedMessages(new IntNum3((byte)90),new LongNum3((byte)90)));
        return testMessages;
    }

    public static Collection<Class<?>> getOriginalSchemaClasses() {
        return Arrays.asList(DecimalNarrow.class, ByteNum.class, ByteNum2.class, ByteNum3.class, ByteNum4.class,
                ShortNum2.class, ShortNum3.class, IntNum3.class);

    }

    public static Collection<Class<?>> getUpgradedSchemaClasses() {
        return Arrays.asList(DecimalWide.class, OptByteNum.class, ShortNum.class, IntNum.class, LongNum.class,
                IntNum2.class, LongNum2.class, LongNum3.class);
    }

    @Name("Decimal")
    @Id(999)
    public static class DecimalNarrow extends MsgObject {
        public float decimal;

        public DecimalNarrow() {
        }

        public DecimalNarrow(float decimal) {
            this.decimal=decimal;
        }
    }

    @Name("Decimal")
    @Id(999)
    public static class DecimalWide extends MsgObject {
        public double decimal;

        public DecimalWide() {
        }

        public DecimalWide(double decimal) {
            this.decimal=decimal;
        }

        public boolean equals(Object o) {
            return (o instanceof DecimalWide) && Math.abs(((DecimalWide) o).decimal-decimal)<0.0001;
        }
    }

    @Name("Number")
    @Id(1001)
    public static class OptByteNum extends MsgObject {
        public Byte n;

        public OptByteNum() {}

        public OptByteNum(Byte n) { this.n = n; }
    }

    @Name("Number")
    @Id(1001)
    public static class ByteNum extends MsgObject {
        public byte n;

        public ByteNum() {}

        public ByteNum(byte n) { this.n = n; }
    }

    @Name("Number2")
    @Id(1002)
    public static class ByteNum2 extends MsgObject {
        public byte n;

        public ByteNum2() {}

        public ByteNum2(byte n) { this.n = n; }
    }

    @Name("Number2")
    @Id(1002)
    public static class ShortNum extends MsgObject {
        public short n;

        public ShortNum() {}
        public ShortNum(short n) { this.n = n; }

    }

    @Name("Number3")
    @Id(1003)
    public static class ByteNum3 extends MsgObject {
        public byte n;

        public ByteNum3() {}

        public ByteNum3(byte n) { this.n = n; }
    }

    @Name("Number3")
    @Id(1003)
    public static class IntNum extends MsgObject {
        public int n;

        public IntNum() {}
        public IntNum(int n) { this.n = n; }

    }

    @Name("Number4")
    @Id(1004)
    public static class ByteNum4 extends MsgObject {
        public byte n;

        public ByteNum4() {}

        public ByteNum4(byte n) { this.n = n; }
    }

    @Name("Number4")
    @Id(1004)
    public static class LongNum extends MsgObject {
        public long n;

        public LongNum() {}
        public LongNum(long n) { this.n = n; }

    }

    @Name("Number5")
    @Id(1005)
    public static class ShortNum2 extends MsgObject {
        public short n;

        public ShortNum2() {}
        public ShortNum2(short n) { this.n = n; }
    }

    @Name("Number5")
    @Id(1005)
    public static class IntNum2 extends MsgObject {
        public int n;

        public IntNum2() {}
        public IntNum2(int n) { this.n = n; }

    }

    @Name("Number6")
    @Id(1006)
    public static class ShortNum3 extends MsgObject {
        public short n;

        public ShortNum3() {}
        public ShortNum3(short n) { this.n = n; }
    }

    @Name("Number6")
    @Id(1006)
    public static class LongNum2 extends MsgObject {
        public long n;

        public LongNum2() {}
        public LongNum2(int n) { this.n = n; }
    }

    @Name("Number7")
    @Id(1007)
    public static class IntNum3 extends MsgObject {
        public int n;

        public IntNum3() {}
        public IntNum3(int n) { this.n = n; }
    }

    @Name("Number7")
    @Id(1007)
    public static class LongNum3 extends MsgObject {
        public long n;

        public LongNum3() {}
        public LongNum3(int n) { this.n = n; }
    }

    public enum EnumNarrow {
        VALUE3, VALUE1, VALUE2,
    }

    public enum EnumWide {
        DUMMY_1, VALUE1, VALUE2, VALUE3, ADDITIONAL_VALUE,
    }

    @Name("EnumEnt")
    @Id(1001)
    public static class EnumEntNarrow extends MsgObject {
        public EnumNarrow enumeration;

        public EnumEntNarrow() {
        }

        public EnumEntNarrow(EnumNarrow eValue) {
            enumeration = eValue;
        }
    }

    @Name("EnumEnt")
    @Id(1001)
    public static class EnumEntWide extends MsgObject {
        public EnumWide enumeration;

        public EnumEntWide() {
        }

        public EnumEntWide(EnumWide eValue) {
            enumeration = eValue;
        }
    }
}
