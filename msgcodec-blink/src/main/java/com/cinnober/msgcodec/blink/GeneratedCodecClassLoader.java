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

package com.cinnober.msgcodec.blink;

import com.cinnober.msgcodec.Schema;
import java.util.WeakHashMap;
import java.util.logging.Logger;

/**
 *
 * @author mikael.brannstrom
 */
class GeneratedCodecClassLoader extends ClassLoader {
    private static final Logger log = Logger.getLogger(GeneratedCodecClassLoader.class.getName());

    private static final GeneratedCodecClassLoader instance = new GeneratedCodecClassLoader();

    public static GeneratedCodecClassLoader getInstance() {
        return instance;
    }

    private final ByteCodeGenerator codeGenerator;
    private final WeakHashMap<Object, Class<GeneratedCodec>> codecClassesBySchemaUID = new WeakHashMap<>();
    private int nextClassSuffix = 0;
    
    private GeneratedCodecClassLoader() {
        super(GeneratedCodecClassLoader.class.getClassLoader());
        codeGenerator = new ByteCodeGenerator();
    }

    public Class<GeneratedCodec> getGeneratedCodecClass(Schema schema) {
        synchronized (this) {
            final Object uid = schema.getUID();
            Class<GeneratedCodec> codecClass = codecClassesBySchemaUID.get(uid);
            if (codecClass == null && !codecClassesBySchemaUID.containsKey(uid)) {
                codecClass = generateCodecClass(schema, nextClassSuffix++);
                codecClassesBySchemaUID.put(uid, codecClass);
            }
            return codecClass;
        }
    }

    @SuppressWarnings("unchecked")
    private Class<GeneratedCodec> generateCodecClass(Schema dictionary, int suffix) {
        String generatedClassName = codeGenerator.getGeneratedClassName(suffix);
        byte[] generatedClassBytes = codeGenerator.generateClass(dictionary, suffix);
        Class<?> generatedClass = defineClass(generatedClassName, generatedClassBytes, 0, generatedClassBytes.length);
        return (Class<GeneratedCodec>) generatedClass;
    }

    
}
