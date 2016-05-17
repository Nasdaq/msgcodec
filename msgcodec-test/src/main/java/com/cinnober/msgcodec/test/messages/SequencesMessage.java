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
package com.cinnober.msgcodec.test.messages;


import com.cinnober.msgcodec.MsgObject;
import com.cinnober.msgcodec.anot.Dynamic;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Required;
import com.cinnober.msgcodec.anot.Sequence;
import com.cinnober.msgcodec.anot.Time;
import com.cinnober.msgcodec.anot.Unsigned;
import java.util.Arrays;
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
    public boolean[] arrayBooleans;
    public char[] arrayChars;

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
    @Required
    public boolean[] arrayBooleansReq;
    @Required
    public char[] arrayCharsReq;

    @Required @Time
    public long[] arrayTime;

    @Sequence(Long.class) @Time
    public List<Long> listTime;

    @Sequence(Integer.class)
    public List<Integer> listInts;

    @Sequence(Boolean.class)
    public List<Boolean> listBooleans;

    @Sequence(Character.class)
    public List<Character> listCharacters;

    @Unsigned @Sequence(Integer.class)
    public List<Integer> listUInts;

    @Sequence(Person.class) @Dynamic
    public List<Person> listPeople;

    @Sequence(Employee.class)
    public List<Employee> listEmployees;

    public Employee[] arrayEmployees;

    @Sequence(Color.class)
    public List<Color> listColors;
    public Color[] arrayColors;


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
        msg.arrayBooleansReq = new boolean[]{};
        msg.arrayCharsReq = new char[]{};
        msg.arrayTime = new long[]{};

        msg = new SequencesMessage();
        messages.put("arrayObjs", msg);
        msg.arrayBytesReq = new byte[]{};
        msg.arrayShortsReq = new short[]{};
        msg.arrayIntsReq = new int[]{};
        msg.arrayLongsReq = new long[]{};
        msg.arrayBooleansReq = new boolean[]{};
        msg.arrayCharsReq = new char[]{};
        msg.arrayTime = new long[]{};
        msg.arrayEmployees = new Employee[] { createEmployee("Bob", 123), createEmployee("Alice", 456) };

        msg = new SequencesMessage();
        messages.put("enums0", msg);
        msg.arrayBytesReq = new byte[]{};
        msg.arrayShortsReq = new short[]{};
        msg.arrayIntsReq = new int[]{};
        msg.arrayLongsReq = new long[]{};
        msg.arrayBooleansReq = new boolean[]{};
        msg.arrayCharsReq = new char[]{};
        msg.arrayTime = new long[]{};
        msg.arrayColors = new Color[]{};
        msg.listColors = Arrays.asList();

        msg = new SequencesMessage();
        messages.put("enums1", msg);
        msg.arrayBytesReq = new byte[]{};
        msg.arrayShortsReq = new short[]{};
        msg.arrayIntsReq = new int[]{};
        msg.arrayLongsReq = new long[]{};
        msg.arrayBooleansReq = new boolean[]{};
        msg.arrayCharsReq = new char[]{};
        msg.arrayTime = new long[]{};
        msg.arrayColors = new Color[]{ Color.RED };
        msg.listColors = Arrays.asList(Color.RED);

        msg = new SequencesMessage();
        messages.put("enums2", msg);
        msg.arrayBytesReq = new byte[]{};
        msg.arrayShortsReq = new short[]{};
        msg.arrayIntsReq = new int[]{};
        msg.arrayLongsReq = new long[]{};
        msg.arrayBooleansReq = new boolean[]{};
        msg.arrayCharsReq = new char[]{};
        msg.arrayTime = new long[]{};
        msg.arrayColors = new Color[]{ Color.GREEN, Color.BLUE };
        msg.listColors = Arrays.asList(Color.GREEN, Color.BLUE);
        
        return messages;
    }

    private static Employee createEmployee(String name, long employeeNumber) {
        Employee employee = new Employee();
        employee.name = name;
        employee.employeeNumber = employeeNumber;
        return employee;
    }
}
