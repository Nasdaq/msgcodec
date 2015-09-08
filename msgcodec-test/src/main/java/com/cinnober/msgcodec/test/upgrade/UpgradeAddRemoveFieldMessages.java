package com.cinnober.msgcodec.test.upgrade;

import java.util.LinkedHashMap;
import java.util.Map;

import com.cinnober.msgcodec.MsgObject;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Name;
import com.cinnober.msgcodec.anot.Required;
import com.cinnober.msgcodec.test.upgrade.PairedTestProtocols.PairedMessages;


public class UpgradeAddRemoveFieldMessages {

    
    public static Map<String, PairedMessages> createMessages() {
        Map<String, PairedMessages> messages = new LinkedHashMap<>();

        AddAndRemoveOptionalV1 addAndRemoveOptionalV1 = new AddAndRemoveOptionalV1(24, Numbers.TWO, 1.2f);
        AddAndRemoveOptionalV2 addRemoveOptionalV2 = new AddAndRemoveOptionalV2(24, null, null, null);
        messages.put("_addRemoveOptional", new PairedMessages(addAndRemoveOptionalV1, addRemoveOptionalV2));
        
        ChangeRequiredToOptionalV1 changeRequiredToOptionalV1 = new ChangeRequiredToOptionalV1(24, Numbers.TWO, 1.2f);
        ChangeRequiredToOptionalV2 changeRequiredToOptionalV2 = new ChangeRequiredToOptionalV2(24, Numbers.TWO, 1.2f);
        messages.put("_changeRequiredToOptional", new PairedMessages(changeRequiredToOptionalV1, changeRequiredToOptionalV2));
        
        RemoveRequiredV1 removeRequiredV1 = new RemoveRequiredV1(24, Numbers.TWO, 1.2f);
        RemoveRequiredV2 removeRequiredV2 = new RemoveRequiredV2(Numbers.TWO, 1.2f);
        messages.put("_removeRequired", new PairedMessages(removeRequiredV1, removeRequiredV2));
        
        return messages;
    }
    
    public static enum Numbers {
        ONE, TWO, THREE,
    }

    public static enum Words {
        SUCCESS, FAILURE,
    }
    
    @Name("AddAndRemoveOptional")
    @Id(1)
    public static class AddAndRemoveOptionalV1 extends MsgObject {
        @Required
        public Integer number;
        public Numbers enumeration;
        public Float decimal;

        public AddAndRemoveOptionalV1() {
        }

        public AddAndRemoveOptionalV1(int value, Numbers eValue, float d) {
            number = value;
            enumeration = eValue;
            decimal = d;
        }
    }

    @Name("AddAndRemoveOptional")
    @Id(1)
    public static class AddAndRemoveOptionalV2 extends MsgObject {
        @Required
        public Integer number;
        public Short newfield1;
        public Double newfield2;
        public Words newEnum;
        
        public AddAndRemoveOptionalV2() {
        }
        
        public AddAndRemoveOptionalV2(int v1, Short v2, Double v3, Words v4) {
            number = v1;
            newfield1 = v2;
            newfield2 = v3;
            newEnum = v4;
        }

    }

    @Name("ChangeRequiredToOptional")
    @Id(2)
    public static class ChangeRequiredToOptionalV1 extends MsgObject {
        @Required
        public Integer number;
        public Numbers enumeration;
        public Float decimal;

        public ChangeRequiredToOptionalV1() {
        }

        public ChangeRequiredToOptionalV1(int value, Numbers eValue, float d) {
            number = value;
            enumeration = eValue;
            decimal = d;
        }
    }

    @Name("ChangeRequiredToOptional")
    @Id(2)
    public static class ChangeRequiredToOptionalV2 extends MsgObject {
        public Integer number;
        public Numbers enumeration;
        public Float decimal;

        public ChangeRequiredToOptionalV2() {
        }

        public ChangeRequiredToOptionalV2(Integer number, Numbers eValue, float d) {
            this.number = number;
            enumeration = eValue;
            decimal = d;
        }
    }

    @Name("RemoveRequired")
    @Id(3)
    public static class RemoveRequiredV1 extends MsgObject {
        @Required
        public Integer number;
        public Numbers enumeration;
        public Float decimal;

        public RemoveRequiredV1() {
        }

        public RemoveRequiredV1(int value, Numbers eValue, float d) {
            number = value;
            enumeration = eValue;
            decimal = d;
        }
    }

    @Name("RemoveRequired")
    @Id(3)
    public static class RemoveRequiredV2 extends MsgObject {
        // Removed the required field number
        public Numbers enumeration;
        public Float decimal;

        public RemoveRequiredV2() {
        }

        public RemoveRequiredV2(Numbers eValue, float d) {
            enumeration = eValue;
            decimal = d;
        }
    }
}
