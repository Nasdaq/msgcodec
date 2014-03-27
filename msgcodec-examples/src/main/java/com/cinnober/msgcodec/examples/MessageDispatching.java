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

package com.cinnober.msgcodec.examples;

import com.cinnober.msgcodec.examples.messages.Carpenter;
import com.cinnober.msgcodec.examples.messages.Hello;
import com.cinnober.msgcodec.examples.messages.Numbers;
import com.cinnober.msgcodec.examples.messages.Person;
import com.cinnober.msgcodec.util.ObjectDispatcher;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Example that shows how message dispatching using the {@link ObjectDispatcher} can be done.
 *
 * @author mikael.brannstrom
 */
public class MessageDispatching {

    private static final Logger log = Logger.getLogger(MessageDispatching.class.getName());

    private final ObjectDispatcher dispatcher;

    public MessageDispatching() {
        Collection<Object> delegates = Arrays.asList(new MyService(), new ErrorHandler());
        dispatcher = new ObjectDispatcher(delegates, new Class<?>[]{MessageContext.class});
    }

    /**
     * Typical parse message loop.
     */
    public void parseLoop() {
        for (;;) {
            Object msg = readObject();
            if (msg == null) {
                break;
            }
            MessageContext context = new MessageContext();
            dispatchObject(msg, context);
        }
    }

    /**
     * Dispatch a message to the service method that should handle it.
     *
     * @param message the message to be dispatched, not null.
     * @param context the message context, capturing e.g. which channel it was received on etc. 
     */
    public void dispatchObject(Object message, MessageContext context) {
        try {
            dispatcher.dispatch(message, context);
        } catch (InvocationTargetException e) {
            log.log(Level.WARNING, "Service throwed an exception", e.getTargetException());
        } catch (NoSuchMethodException e) {
            throw new Error("Bug", e); // the ErrorHandler should capture the "any" case
        }
    }

    public static void main(String... args) {
        MessageDispatching dispatcher = new MessageDispatching();
        dispatcher.parseLoop();
    }

    public static class MessageContext { }

    /**
     * Example service.
     */
    public static class MyService {
        public void onPerson(Person person, MessageContext ctx) {
            System.out.format("%s is here!\n", person.name);
        }

        public void onHello(Hello hello, MessageContext ctx) {
            System.out.format("Someone says \"%s\"\n", hello.greeting);
        }
    }

    /**
     * Error handler service.
     */
    public static class ErrorHandler {
        public void onUnhandled(Object message, MessageContext ctx) {
            System.out.format("Oh no! Unhandled message: %s\n", message);
        }
    }














    

    private final LinkedList<Object> messages = initMessages();
    private static LinkedList<Object> initMessages() {
        LinkedList<Object> list = new LinkedList<>();

        Person alice = new Person();
        alice.name = "Alice";
        list.add(alice);

        Hello hello = new Hello("I'm home!");
        list.add(hello);

        Carpenter bob = new Carpenter();
        bob.name = "Bob";
        bob.tools = new String[] {"hammer", "screwdriver"};
        list.add(bob);

        Numbers numbers = new Numbers();
        numbers.bigIntReq = BigInteger.TEN.pow(30);
        numbers.signedReq = -1;
        numbers.unsignedReq = -1; // 2^32 - 1
        numbers.decimal = BigDecimal.valueOf(123456, 3); // 123.456
        list.add(numbers);

        return list;
    }

    public Object readObject() {
        if (messages.isEmpty()) {
            return null;
        } else {
            return messages.removeFirst();
        }
    }
}
