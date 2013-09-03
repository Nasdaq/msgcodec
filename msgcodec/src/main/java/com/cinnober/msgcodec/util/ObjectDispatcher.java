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
package com.cinnober.msgcodec.util;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.regex.Pattern;

import com.cinnober.msgcodec.GroupBinding;
import com.cinnober.msgcodec.GroupDef;
import com.cinnober.msgcodec.ProtocolDictionary;

/**
 * An object dispatcher can dispatch objects based on their types to methods in a list of delegates.
 *
 * <p>The dispatcher has a single entry point, {@link #dispatch(Object)}. The dispatcher will delegate
 * the call to the most specific method in one of the delegates. The delegates should declare methods
 * with one parameter, which will be matched against the object type, and optionally a return value.
 * The name of the methods must match the specified pattern.
 *
 * <p><b>Example:</b> You have the following input types:
 * <pre>
 * class Base {}
 * class Ping extends Base {}
 * class Pong extends Base {}
 * class Thing {}
 * </pre>
 *
 * You have the following delegates:
 * <pre>
 * class MyService {
 *   public Pong onPing(Ping ping) { }
 *   public void onBase(Base base) { }
 * }
 * class MyErrorHandler {
 *   public void onUnhandledType(Object any) { }
 * }
 * </pre>
 *
 * Then create a dispatcher:
 * <pre>
 * MyService myService = ...;
 * MyErrorHandler myErrorHandler = ...;
 * ObjectDispatcher dispatcher = new ObjectDispatcher(
 *     Arrays.asList(Base.class, Ping.class, Pong.class, Thing.class),
 *     Arrays.asList(myService, myErrorHandler));
 *
 * Object result;
 * result = dispatcher.dispatch(new Ping()); // calls MyService.onPing(..)
 * result = dispatcher.dispatch(new Pong()); // calls MyService.onBase(..)
 * result = dispatcher.dispatch(new Thing()); // calls MyErrorHandler.onUnhandledType(..)
 * result = dispatcher.dispatch(new Unknown()); // throws NoSuchMethodException
 * </pre>
 *
 *
 * @author Mikael Brannstrom
 *
 */
public class ObjectDispatcher {
    private static final Pattern DEFAULT_PATTERN = Pattern.compile("((on)|(process)|(handle)|(do))([A-Z0-9_].*)?");

    private final Map<Class<?>, Target> targets;


    /**
     * Create an object dispatcher for the types using the delegates.
     * The method names should start with "on", "process", "handle" or "do".
     *
     * @param types the types that should be supported, not null.
     * @param delegates the delegates that should be called, not null.
     */
    public ObjectDispatcher(Collection<Class<?>> types, Collection<Object> delegates) {
        this(types, delegates, DEFAULT_PATTERN);
    }

    /**
     * Create an object dispatcher for the types using the delegates with the pattern.
     *
     * @param types the types that should be supported, not null.
     * @param delegates the delegates that should be called, not null.
     * @param pattern the pattern of the methods in the delegates.
     */
    public ObjectDispatcher(Collection<Class<?>> types, Collection<Object> delegates, Pattern pattern) {
        targets = buildTargets(types, delegates, pattern);
    }

    /** Delegate the object to any of the delegates.
     *
     * @param obj the object to dispatch, not null.
     * @return the return value, or null if no return value.
     * @throws NoSuchMethodException if no delegate could handle this object type
     * @throws InvocationTargetException if the delegate throwed an exception
     */
    public Object dispatch(Object obj) throws InvocationTargetException, NoSuchMethodException {
        Target target = targets.get(obj.getClass());
        if (target == null) {
            throw new NoSuchMethodException("Unhandled type " + obj.getClass());
        }
        return target.process(obj);
    }

    /** Returns the all group classes in the specified protocol dictionary.
     * The dictionary must be bound if any classes should be found.
     *
     * @param dictionary the dictionary to search in, not null.
     * @return the found group classes, not null.
     */
    public static Collection<Class<?>> getGroupClasses(ProtocolDictionary dictionary) {
        Collection<Class<?>> result = new LinkedList<>();
        for (GroupDef groupDef : dictionary.getGroups()) {
            GroupBinding groupBinding = groupDef.getBinding();
            if (groupBinding != null) {
                Object groupType = groupBinding.getGroupType();
                if (groupType instanceof Class<?>) {
                    result.add((Class<?>) groupType);
                }
            }
        }
        return result;
    }

    /** Build a map from class to target.
     * @param types the types, not null
     * @param delegates the delegates, not null
     * @param pattern the pattern
     * @return the map, not null
     */
    private static Map<Class<?>, Target> buildTargets(Collection<Class<?>> types,
            Collection<Object> delegates, Pattern pattern) {
        Map<Class<?>, Target> targets = new HashMap<>(types.size() * 2);
        for (Class<?> type : types) {
            Target target = findTarget(type, delegates, pattern);
            if (target != null) {
                targets.put(type, target);
            }
        }

        return targets;
    }

    /** Find a target for the type.
     * @param delegates
     * @param pattern
     * @return the target, or null
     */
    private static Target findTarget(Class<?> type, Collection<Object> delegates,
            Pattern pattern) {
        Method bestMethod = null;
        Object bestDelegate = null;
        Class<?> bestType = null;


        for (Object delegate : delegates) {
            for (Method method : delegate.getClass().getMethods()) {
                if (Modifier.isStatic(method.getModifiers())) {
                    continue;
                }

                if (pattern != null && !pattern.matcher(method.getName()).matches()) {
                    continue;
                }

                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 1 || !parameterTypes[0].isAssignableFrom(type)) {
                    continue;
                }
                Class<?> currentType = parameterTypes[0];
                if (bestType != null) {
                    if (currentType.isAssignableFrom(bestType)) {
                        // current type is wider than best type
                        continue;
                    }
                    if (!Modifier.isInterface(bestType.getModifiers()) &&
                            Modifier.isInterface(currentType.getModifiers())) {
                        // current type is interface and best type is class
                        continue;
                    }
                    if (!bestType.isAssignableFrom(currentType)) {
                        // best type is not wider than current type, i.e. unrelated (interfaces)
                        continue;
                    }
                    // ok, current is the best
                }
                bestType = currentType;
                bestMethod = method;
                bestDelegate = delegate;
            }
        }
        if (bestType != null) {
            return new Target(bestMethod, bestDelegate);
        } else {
            return null;
        }
    }

    private static class Target {
        private final Method method;
        private final Object instance;

        /**
         * @param method
         * @param instance
         */
        Target(Method method, Object instance) {
            this.method = method;
            this.instance = instance;
        }


        public Object process(Object obj) throws InvocationTargetException {
            try {
                return method.invoke(instance, obj);
            } catch (IllegalAccessException | IllegalArgumentException e) {
                throw new Error("Bug"); // should not happen
            }
        }
    }
}
