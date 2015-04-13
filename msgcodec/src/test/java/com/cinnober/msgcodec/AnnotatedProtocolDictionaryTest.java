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
        Schema dict = new Schema(Collections.singletonList(groupDef),null);
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

    private FieldDef getClientOrderId(Schema dictionary) {
        return dictionary.getGroup(executionReportGroupName).getField(clientOrderIdFieldName);
    }

}
