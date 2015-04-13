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

package com.cinnober.msgcodec.examples.generated;

import com.cinnober.msgcodec.ConstructorFactory;
import com.cinnober.msgcodec.FieldAccessor;
import com.cinnober.msgcodec.FieldBinding;
import com.cinnober.msgcodec.FieldDef;
import com.cinnober.msgcodec.GroupBinding;
import com.cinnober.msgcodec.GroupDef;
import com.cinnober.msgcodec.NamedType;
import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.TypeDef;
import com.cinnober.msgcodec.TypeDef.Symbol;
import java.util.Arrays;

/**
 * Example of a protocol Java class that could have been generated.
 *
 * @author mikael.brannstrom
 */
public class GeneratedProtocol {
    private static final Schema instance = createProtocol();

    public static Schema getInstance() {
        return instance;
    }

    private static Schema createProtocol() {
        try {
            return new Schema(
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
