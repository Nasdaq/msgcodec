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
package com.cinnober.msgcodec.examples.messages;

import java.util.Objects;

import com.cinnober.msgcodec.anot.Annotate;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.anot.Required;

/** Simple hello message.
 *
 * @author Mikael Brannstrom
 */
@Id(1) // <- Numeric identifier for this message type
public class Hello {
    private String greeting;

    public Hello() { // <- A default constructor must exist.
    }

    /** Create a new hello message.
     * @param greeting the greeting
     */
    public Hello(String greeting) {
        this.greeting = greeting;
    }

    /** Returns the greeting.
     * @return the greeting
     */
    @Id(1) // <- Numeric identifier for this field
    @Required // <- Make the field required
    @Annotate({"maxLength=100"}) // <- some codecs interpret this as max string length
    public String getGreeting() {
        return greeting;
    }
    public void setGreeting(String greeting) {
        this.greeting = greeting;
    }

    @Override
    public String toString() {
        return "Hello [greeting=" + greeting + "]";
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        Hello other = (Hello) obj;
        return Objects.equals(greeting, other.greeting);
    }
}
