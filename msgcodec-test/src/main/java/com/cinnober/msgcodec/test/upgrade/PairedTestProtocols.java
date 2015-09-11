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
package com.cinnober.msgcodec.test.upgrade;

import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.SchemaBuilder;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Tommy Norling
 * 
 */
public class PairedTestProtocols {
    private static final Class<?>[] originalSchemaClasses = {
            UpgradeAddRemoveFieldMessages.AddAndRemoveOptionalV1.class,
            UpgradeAddRemoveFieldMessages.ChangeRequiredToOptionalV1.class,
            UpgradeAddRemoveFieldMessages.RemoveRequiredV1.class,
            UpgradeEnumArrayMessages.Original.class,
            UpgradeEnumsMessages.Original.class,
            UpgradeEnumSequenceMessages.Original.class,
            UpgradeIntegerEnumsMessages.Original.class};

    private static final Class<?>[] upgradedSchemaClasses = {
            UpgradeAddRemoveFieldMessages.AddAndRemoveOptionalV2.class,
            UpgradeAddRemoveFieldMessages.ChangeRequiredToOptionalV2.class,
            UpgradeAddRemoveFieldMessages.RemoveRequiredV2.class,
            UpgradeEnumArrayMessages.Upgraded.class,
            UpgradeEnumsMessages.Upgraded.class,
            UpgradeEnumSequenceMessages.Upgraded.class,
            UpgradeIntegerEnumsMessages.Upgraded.class,
    };


    public static Schema getOriginalSchema() {
        ArrayList<Class<?>> classes = new ArrayList<>(Arrays.asList(originalSchemaClasses));
        classes.addAll(UpgradeBasicMessages.getOriginalSchemaClasses());
        classes.addAll(UpgradeMissingGroupDefs.getOriginalSchemaClasses());
        classes.addAll(UpgradeWithInheritedGroups.getOriginalSchemaClasses());
        classes.addAll(UpgradeWithInheritedGroups2.getOriginalSchemaClasses());
        classes.addAll(UpgradeDynamicWithLostGroups.getOriginalSchemaClasses());
        return new SchemaBuilder().build(classes.toArray(new Class<?>[classes.size()]));
    }
    
    public static Schema getUpgradedSchema() {
        ArrayList<Class<?>> classes = new ArrayList<>(Arrays.asList(upgradedSchemaClasses));
        classes.addAll(UpgradeBasicMessages.getUpgradedSchemaClasses());
        classes.addAll(UpgradeMissingGroupDefs.getUpgradedSchemaClasses());
        classes.addAll(UpgradeWithInheritedGroups.getUpgradedSchemaClasses());
        classes.addAll(UpgradeWithInheritedGroups2.getUpgradedSchemaClasses());
        classes.addAll(UpgradeDynamicWithLostGroups.getUpgradedSchemaClasses());
        return new SchemaBuilder().build(classes.toArray(new Class<?>[classes.size()]));
    }

    /**
     * Returns messages suitable for testing a codec. This includes border cases.
     * Each message is labeled with a name, e.g. "Dates.zero" or "Decimals.border1" that describes what
     * the message tries to test.
     *
     * All messages are encodable, i.e. any required fields are set.
     *
     * @return a map from message label to message.
     */
    public static Map<String, PairedMessages> createMessages() {
        Map<String, PairedMessages> messages = new LinkedHashMap<>();

        putAll(messages, "EnumArray.", UpgradeEnumArrayMessages.createMessages());
        putAll(messages, "Enums.", UpgradeEnumsMessages.createMessages());
        putAll(messages, "AddRemoveFields", UpgradeAddRemoveFieldMessages.createMessages());
        putAll(messages, "EnumSequence.", UpgradeEnumSequenceMessages.createMessages());
        putAll(messages, "IntegerEnums.", UpgradeIntegerEnumsMessages.createMessages());
        putAll(messages, "BasicMessages.", UpgradeBasicMessages.createMessages());
        putAll(messages, "GroupDefsMissing.", UpgradeMissingGroupDefs.createMessages());
        putAll(messages, "InheritedGroups.", UpgradeWithInheritedGroups.createMessages());
        putAll(messages, "InheritedGroups2.", UpgradeWithInheritedGroups2.createMessages());
        putAll(messages, "DynamicGroups.", UpgradeDynamicWithLostGroups.createMessages());


        return messages;
    }

    private static void putAll(Map<String, PairedMessages> result, String prefix, Map<String, PairedMessages> messages) {
        for (Map.Entry<String, PairedMessages> entry : messages.entrySet()) {
            result.put(prefix + entry.getKey(), entry.getValue());
        }
    }
    
    public static class PairedMessages {
        public final Object originalMessage;
        public final Object upgradedMessage;
        
        public PairedMessages(Object originalMessage, Object upgradedMessage) {
            Objects.requireNonNull(originalMessage);
            Objects.requireNonNull(upgradedMessage);
            
            this.originalMessage = originalMessage;
            this.upgradedMessage = upgradedMessage;
        }
    }
}
