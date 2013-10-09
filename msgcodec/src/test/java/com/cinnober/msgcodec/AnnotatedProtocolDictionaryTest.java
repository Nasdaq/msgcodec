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

import java.util.Collections;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 *
 * @author fredrik.bromee, Cinnober Financial Technology
 */
public class AnnotatedProtocolDictionaryTest {

    String executionReportGroupName = "ExecutionReport";
    String clientOrderIdFieldName = "clOrdID";
    String annotationName = "doc";

    @Test
    public void testRawGroupDef() throws Exception {
        GroupDef groupDef = ExecutionReport.groupDef();
        ProtocolDictionary dict = new ProtocolDictionary(Collections.singletonList(groupDef),null);
        String groupLevelDocForExecReport =
                "An execution report is used for a bunch of things, like confirming the receipt of an order";
        Annotations annotations = new Annotations();
        annotations.path(executionReportGroupName).put(annotationName, groupLevelDocForExecReport);
        String fieldLevelDocForClientOrderId = "Unique identifier for Order as assigned by the buy-side";
        annotations.path(executionReportGroupName, clientOrderIdFieldName)
            .put(annotationName, fieldLevelDocForClientOrderId);

        annotations.toProperties().store(System.out, "Annotations");
        System.out.println();

        dict = dict.replaceAnnotations(annotations);
        System.out.println(dict.toString());
        Assert.assertEquals(groupLevelDocForExecReport, dict.getGroup(executionReportGroupName)
                .getAnnotation(annotationName));
        Assert.assertEquals(fieldLevelDocForClientOrderId, getClientOrderId(dict).getAnnotation(annotationName));
    }

    private FieldDef getClientOrderId(ProtocolDictionary dictionary) {
        return (FieldDef) dictionary.getNode(executionReportGroupName, clientOrderIdFieldName);
    }

}
