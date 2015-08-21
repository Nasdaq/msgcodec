package com.cinnober.msgcodec.test.upgrade;

import java.util.LinkedHashMap;
import java.util.Map;

import com.cinnober.msgcodec.MsgObject;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Name;
import com.cinnober.msgcodec.test.upgrade.PairedTestProtocols.PairedMessages;


public class UpgradeAddRemoveFieldMessages {

    
    public static Map<String, PairedMessages> createMessages() {
        Map<String, PairedMessages> messages = new LinkedHashMap<>();

        Version1 original = new Version1(24, EnumV1.VALUE1, 1.2f);
        Version2 upgraded = new Version2(24L, (short) 0, 0.0, null);
        messages.put("_test1", new PairedMessages(original, upgraded));

        return messages;
    }
    
    public static enum EnumV1 {
        VALUE3, VALUE1, VALUE2,
    }

    public static enum EnumV2 {
        VALUE1, VALUE2, VALUE3, ADDITIONAL_VALUE,
    }
    
    @Name("Payload")
    @Id(1)
    public static class Version1 extends MsgObject {
        public int number;
        public EnumV1 enumeration;
        public float decimal;

        public Version1() {
        }

        public Version1(int value, EnumV1 eValue, float d) {
            number = value;
            enumeration = eValue;
            decimal = d;
        }
    }

    @Name("Payload")
    @Id(1)
    public static class Version2 extends MsgObject {
        public long number;
        public short newfield1;
        public double newfield2;
        public EnumV2 newEnum;
        
        public Version2() {
        }
        
        public Version2(long v1, short v2, double v3, EnumV2 v4) {
            number = v1;
            newfield1 = v2;
            newfield2 = v3;
            newEnum = v4;
        }

    }
    
    
    
}
