/*
 * Copyright (c) 2014 Cinnober Financial Technology AB, Stockholm,
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

package com.cinnober.msgcodec.test.messages;

import com.cinnober.msgcodec.anot.Id;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author mikael.brannstrom
 */
@Id(112)
public class GenericEmployeeMessage extends GenericBasePersonMessage<Employee> {


    /**
     * Returns messages suitable for testing a codec. This includes border cases.
     * Each message is labeled with a name, e.g. "zero" or "border1" that describes what
     * the message tries to test.
     *
     * All messages are encodable, i.e. any required fields are set.
     *
     * @return a map from message label to message.
     */
    public static Map<String, GenericEmployeeMessage> createMessages() {
        Map<String, GenericEmployeeMessage> messages = new LinkedHashMap<>();

        GenericEmployeeMessage msg;

        msg = new GenericEmployeeMessage();
        messages.put("clean", msg);
        msg.owner = null;

        msg = new GenericEmployeeMessage();
        messages.put("clean", msg);
        msg.owner = createEmployee("Bob", 12345);

        return messages;
    }

    private static Employee createEmployee(String name, long employeeNumber) {
        Employee employee = new Employee();
        employee.name = name;
        employee.employeeNumber = employeeNumber;
        return employee;
    }
}
