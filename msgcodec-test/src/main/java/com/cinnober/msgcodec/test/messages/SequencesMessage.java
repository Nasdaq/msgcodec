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
package com.cinnober.msgcodec.test.messages;


import com.cinnober.msgcodec.MsgObject;
import com.cinnober.msgcodec.anot.Dynamic;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Required;
import com.cinnober.msgcodec.anot.Sequence;
import com.cinnober.msgcodec.anot.Time;
import com.cinnober.msgcodec.anot.Unsigned;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author mikael.brannstrom
 *
 */
@Id(108)
public class SequencesMessage extends MsgObject {

    @Sequence(byte.class)
    public byte[] arrayBytes;
    public short[] arrayShorts;
    public int[] arrayInts;
    public long[] arrayLongs;

    @Unsigned
    public int[] arrayUInts;

    @Required
    @Sequence(byte.class)
    public byte[] arrayBytesReq;
    @Required
    public short[] arrayShortsReq;
    @Required
    public int[] arrayIntsReq;
    @Required
    public long[] arrayLongsReq;

    @Required @Time
    public long[] arrayTime;

    @Sequence(Long.class) @Time
    public List<Long> listTime;

    @Sequence(Integer.class)
    public List<Integer> listInts;

    @Unsigned @Sequence(Integer.class)
    public List<Integer> listUInts;

    @Sequence(Person.class) @Dynamic
    public List<Person> listPeople;

    @Sequence(Employee.class)
    public List<Employee> listEmployees;

    public Employee[] arrayEmployees;

    /**
     * Returns messages suitable for testing a codec. This includes border cases.
     * Each message is labeled with a name, e.g. "zero" or "border1" that describes what
     * the message tries to test.
     *
     * All messages are encodable, i.e. any required fields are set.
     *
     * @return a map from message label to message.
     */
    public static Map<String, SequencesMessage> createMessages() {
        Map<String, SequencesMessage> messages = new LinkedHashMap<>();

        SequencesMessage msg;

        msg = new SequencesMessage();
        messages.put("clean", msg);
        msg.arrayBytesReq = new byte[]{};
        msg.arrayShortsReq = new short[]{};
        msg.arrayIntsReq = new int[]{};
        msg.arrayLongsReq = new long[]{};
        msg.arrayTime = new long[]{};

        msg = new SequencesMessage();
        messages.put("arrayObjs", msg);
        msg.arrayBytesReq = new byte[]{};
        msg.arrayShortsReq = new short[]{};
        msg.arrayIntsReq = new int[]{};
        msg.arrayLongsReq = new long[]{};
        msg.arrayTime = new long[]{};
        msg.arrayEmployees = new Employee[] { createEmployee("Bob", 123), createEmployee("Alice", 456) };

        return messages;
    }

    private static Employee createEmployee(String name, long employeeNumber) {
        Employee employee = new Employee();
        employee.name = name;
        employee.employeeNumber = employeeNumber;
        return employee;
    }
}
