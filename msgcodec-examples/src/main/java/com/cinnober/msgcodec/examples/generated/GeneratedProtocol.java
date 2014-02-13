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

package com.cinnober.msgcodec.examples.generated;

import com.cinnober.msgcodec.ConstructorFactory;
import com.cinnober.msgcodec.FieldAccessor;
import com.cinnober.msgcodec.FieldBinding;
import com.cinnober.msgcodec.FieldDef;
import com.cinnober.msgcodec.GroupBinding;
import com.cinnober.msgcodec.GroupDef;
import com.cinnober.msgcodec.NamedType;
import com.cinnober.msgcodec.ProtocolDictionary;
import com.cinnober.msgcodec.TypeDef;
import com.cinnober.msgcodec.TypeDef.Symbol;
import java.util.Arrays;

/**
 * Example of a protocol Java class that could have been generated.
 *
 * @author mikael.brannstrom
 */
public class GeneratedProtocol {
    private static final ProtocolDictionary instance = createProtocol();

    public static ProtocolDictionary getInstance() {
        return instance;
    }

    private static ProtocolDictionary createProtocol() {
        try {
            return new ProtocolDictionary(
                Arrays.asList(new GroupDef[] { // allow for comma at the end
                    createGroupUser(),
                    createGroupAnimal(),
                }),
                Arrays.asList(new NamedType[] { // allow for comma at the end
                    createNamedTypeKind(),
                })
            );
        } catch (ReflectiveOperationException e) {
            // Should not happen
            throw new Error("Code generation error", e);
        }
    }

    private static GroupDef createGroupUser() throws ReflectiveOperationException {
        return new GroupDef("User", 1001, null,
                Arrays.asList(new FieldDef[] { // allow for comma at the end
                        new FieldDef("id", 1, true, TypeDef.UINT64, null,
                                new FieldBinding(new FieldAccessor(User.class.getField("id")), long.class, null)),
                        new FieldDef("name", 2, false, TypeDef.STRING, null,
                                new FieldBinding(new FieldAccessor(User.class.getField("name")), String.class, null)),
                        new FieldDef("email", 3, false, TypeDef.STRING, null,
                                new FieldBinding(new FieldAccessor(User.class.getField("email")), String.class, null)),
                    }
                ),
                null,
                new GroupBinding(new ConstructorFactory<User>(User.class.getConstructor()), User.class)
            );
    }

    private static GroupDef createGroupAnimal() throws ReflectiveOperationException {
        return new GroupDef("Animal", 1002, null,
                Arrays.asList(new FieldDef[] { // allow for comma at the end
                        new FieldDef("name", 1, true, TypeDef.UINT64, null,
                                new FieldBinding(new FieldAccessor(Animal.class.getField("name")), String.class, null)),
                        new FieldDef("kind", 2, true, new TypeDef.Reference("Kind"), null,
                                new FieldBinding(new FieldAccessor(Animal.class.getField("kind")), int.class, null)),
                    }
                ),
                null,
                new GroupBinding(new ConstructorFactory<Animal>(Animal.class.getConstructor()), Animal.class)
            );
    }

    private static NamedType createNamedTypeKind() {
        return new NamedType("Kind", new TypeDef.Enum(Arrays.asList(new Symbol[]{ // allow for comma at the end
            new Symbol("CREEPY", Animal.KIND_CREEPY),
            new Symbol("CUTE", Animal.KIND_CUTE),
            new Symbol("ANNOYING", Animal.KIND_ANNOYING),
        })), null);
    }

}
