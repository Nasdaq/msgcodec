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

package com.cinnober.msgcodec.blink;

import com.cinnober.msgcodec.Accessor;
import com.cinnober.msgcodec.ConstructorFactory;
import com.cinnober.msgcodec.EnumSymbols;
import com.cinnober.msgcodec.Factory;
import com.cinnober.msgcodec.FieldAccessor;
import com.cinnober.msgcodec.FieldDef;
import com.cinnober.msgcodec.GroupDef;
import com.cinnober.msgcodec.IgnoreAccessor;
import com.cinnober.msgcodec.JavaClassGroupTypeAccessor;
import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.SchemaBinding;
import com.cinnober.msgcodec.TypeDef;
import com.cinnober.msgcodec.TypeDef.Symbol;
import com.cinnober.msgcodec.io.ByteArrays;
import com.cinnober.msgcodec.io.ByteSink;
import com.cinnober.msgcodec.io.ByteSource;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.*;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

/**
 *
 * @author mikael.brannstrom
 */
class NativeByteCodeGenerator extends BaseByteCodeGenerator {

    public NativeByteCodeGenerator() {
        super(GeneratedNativeCodec.class, NativeBlinkCodec.class, NativeBlinkInput.class, NativeBlinkOutput.class);
    }


    
    // --- GENERATE WRITE ----------------------------------------------------------------------------------------------

    @Override
    protected void generateWriteStaticGroupForTypeWithId(Schema schema, ClassVisitor cv,
            String genClassInternalName, boolean javaClassCodec) {
        for (GroupDef group : schema.getGroups()) {
            Object groupType = group.getGroupType();
            String groupDescriptor = javaClassCodec ? Type.getDescriptor((Class<?>)groupType) : "Ljava/lang/Object;";
            MethodVisitor mv = cv.visitMethod(
                    ACC_PRIVATE,
                    "writeStaticGroupWithId_" + group.getName(),
                    "(Lcom/cinnober/msgcodec/io/ByteSink;" + groupDescriptor + ")V",
                    null,
                    new String[] { "java/io/IOException" });
            mv.visitCode();
            int nextWriteidVar = 3;

            if (group.getId() != -1) {
                // write with id
                mv.visitVarInsn(ALOAD, 1); // out
                mv.visitLdcInsn(group.getId() & 0xffffffffL);
                mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeUInt64",
                        "(Lcom/cinnober/msgcodec/io/ByteSink;J)V", false);

                // add extension offset (four zero bytes)
                mv.visitVarInsn(ALOAD, 1); // out
                mv.visitInsn(ICONST_0);
                generateEncodeUInt32Value(true, mv);

                mv.visitVarInsn(ALOAD, 0); // this
                mv.visitVarInsn(ALOAD, 1); // out
                mv.visitVarInsn(ALOAD, 2); // obj
                mv.visitMethodInsn(INVOKEVIRTUAL, genClassInternalName, "writeStaticGroup_" + group.getName(),
                        "(Lcom/cinnober/msgcodec/io/ByteSink;" + groupDescriptor + ")V", false);
                mv.visitInsn(RETURN);
            } else {
                // write with id
                mv.visitTypeInsn(NEW, "java/lang/IllegalArgumentException");
                mv.visitInsn(DUP);
                mv.visitLdcInsn("No group id");
                mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalArgumentException", "<init>",
                        "(Ljava/lang/String;)V", false);
                mv.visitInsn(ATHROW);
            }

            // end
            mv.visitMaxs(3, nextWriteidVar); // PENDING: maxStack
            mv.visitEnd();
        }
    }

    // --- GENERATE ENCODE VALUE ---------------------------------------------------------------------------------------

    @Override
    protected void generateEncodeBinaryValue(TypeDef.Binary type, boolean required, MethodVisitor mv) {
        if (Integer.compareUnsigned(type.getMaxSize(), 255) <= 0) {
            // inline
            mv.visitLdcInsn(type.getMaxSize());
            if (required) {
                mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeInlineBinary",
                        "(Lcom/cinnober/msgcodec/io/ByteSink;[BI)V", false);
            } else {
                mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeInlineBinaryNull",
                        "(Lcom/cinnober/msgcodec/io/ByteSink;[BI)V", false);
            }
        } else {
            // data area
            // TODO: cast bytesink to ByteBuf, set position to data area, etc
            if (required) {
                mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeDataBinary",
                        "(Lcom/cinnober/msgcodec/io/ByteSink;[BI)V", false);
                throw new UnsupportedOperationException("Not implemented yet"); // TODO
            } else {
                throw new UnsupportedOperationException("Not implemented yet"); // TODO
            }
        }
    }

    @Override
    protected void generateEncodeStringValue(TypeDef.StringUnicode type, boolean required, MethodVisitor mv) {
        if (Integer.compareUnsigned(type.getMaxSize(), 255) <= 0) {
            // inline
            mv.visitLdcInsn(type.getMaxSize());
            if (required) {
                mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeInlineStringUTF8",
                        "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/String;I)V", false);
            } else {
                mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeStringUTF8Null",
                        "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/String;I)V", false);
            }
        } else {
            // data area
            throw new UnsupportedOperationException("Not implemented yet"); // TODO
        }
    }

    @Override
    protected void generateEncodeBigDecimalValue(boolean required, MethodVisitor mv) {
        throw new UnsupportedOperationException("Not implemented yet"); // TODO
//        if (required) {
//            mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeBigDecimal",
//                    "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/math/BigDecimal;)V", false);
//        } else {
//            mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeBigDecimalNull",
//                    "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/math/BigDecimal;)V", false);
//        }
    }

    @Override
    protected void generateEncodeBigIntValue(boolean required, MethodVisitor mv) {
        throw new UnsupportedOperationException("Not implemented yet"); // TODO
//        if (required) {
//            mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeBigInt",
//                    "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/math/BigInteger;)V", false);
//        } else {
//            mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeBigIntNull",
//                    "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/math/BigInteger;)V", false);
//        }
    }

    /**
     * Generate value encoding using the blink output.
     *
     * <p>Defaults to <code>writeDynamicGroup[Null]</code> in the generated codec.
     *
     * @param required true if the field is required, otherwise false.
     * @param mv the method visitor, not null.
     * @see #generateEncodeValue
     */
    @Override
    protected void generateEncodeDynRefValue(LocalVariable nextVar, MethodVisitor mv, boolean required) {
        throw new UnsupportedOperationException("Not implemented yet"); // TODO
//        // PENDING: validate that the instance class is a subclass of refGroup (unless null)
//        int instanceVar = nextVar.next();
//        mv.visitVarInsn(ASTORE, instanceVar);
//        mv.visitVarInsn(ALOAD, 0); // this
//        mv.visitInsn(SWAP); // this and out
//        mv.visitVarInsn(ALOAD, instanceVar);
//        if (required) {
//            mv.visitMethodInsn(INVOKEVIRTUAL, baseclassIName,
//                    "writeDynamicGroup", "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/Object;)V", false);
//        } else {
//            mv.visitMethodInsn(INVOKEVIRTUAL, baseclassIName,
//                    "writeDynamicGroupNull", "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/Object;)V", false);
//        }
    }

    // TODO: not reusable
    @Override
    protected void generateEncodeSequenceValue(
            Class<?> javaClass, LocalVariable nextVar, MethodVisitor mv, boolean required,
            int outputStreamVar,
            Class<?> componentJavaClass, TypeDef type, Schema schema, String genClassInternalName,
            String debugValueLabel, boolean javaClassCodec) throws IllegalArgumentException {

        // PENDING: merge the two if-cases, and reuse common code blocks (see generateDecodeSquenceValue)
        if (javaClass.isArray()) {
            int sequenceVar = nextVar.next();
            mv.visitInsn(DUP);
            mv.visitVarInsn(ASTORE, sequenceVar);
            int lengthVar = nextVar.next();
            Label endLabel = new Label();
            if (required) {
                mv.visitInsn(ARRAYLENGTH);
                mv.visitInsn(DUP);
                mv.visitVarInsn(ISTORE, lengthVar);
                mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeUInt32", "(Lcom/cinnober/msgcodec/io/ByteSink;I)V", false);
            } else {
                Label nonNullLabel = new Label();
                mv.visitInsn(DUP);
                mv.visitJumpInsn(IFNONNULL, nonNullLabel);
                // null
                mv.visitInsn(POP);
                mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeNull", "(Lcom/cinnober/msgcodec/io/ByteSink;)V", false);
                mv.visitJumpInsn(GOTO, endLabel);
                // not null
                mv.visitLabel(nonNullLabel);
                // PENDING: mv.visitFrame
                mv.visitInsn(ARRAYLENGTH);
                mv.visitInsn(DUP);
                mv.visitVarInsn(ISTORE, lengthVar);
                mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeUInt32", "(Lcom/cinnober/msgcodec/io/ByteSink;I)V", false);
            }
            // for loop
            int loopVar = nextVar.next();
            mv.visitInsn(ICONST_0);
            mv.visitVarInsn(ISTORE, loopVar);
            Label loopLabel = new Label();
            mv.visitLabel(loopLabel);
            // PENDING: mv.visitFrame
            mv.visitVarInsn(ILOAD, loopVar);
            mv.visitVarInsn(ILOAD, lengthVar);
            mv.visitJumpInsn(IF_ICMPGE, endLabel);
            mv.visitVarInsn(ALOAD, outputStreamVar);
            mv.visitVarInsn(ALOAD, sequenceVar);
            mv.visitVarInsn(ILOAD, loopVar);
            if (componentJavaClass == byte.class || componentJavaClass == boolean.class) {
                mv.visitInsn(BALOAD);
            } else if (componentJavaClass == short.class) {
                mv.visitInsn(SALOAD);
            } else if (componentJavaClass == int.class) {
                mv.visitInsn(IALOAD);
            } else if (componentJavaClass == long.class) {
                mv.visitInsn(LALOAD);
            } else if (componentJavaClass == float.class) {
                mv.visitInsn(FALOAD);
            } else if (componentJavaClass == double.class) {
                mv.visitInsn(DALOAD);
            } else {
                mv.visitInsn(AALOAD);
                mv.visitTypeInsn(CHECKCAST, Type.getInternalName(componentJavaClass));
            }

            // encode the element
            TypeDef.Sequence seqType = (TypeDef.Sequence) type;
            generateEncodeValue(mv, outputStreamVar, nextVar, true, seqType.getComponentType(), componentJavaClass,
                    null, schema, genClassInternalName, debugValueLabel + ".component", javaClassCodec);

            mv.visitIincInsn(loopVar, 1);
            mv.visitJumpInsn(GOTO, loopLabel);
            mv.visitLabel(endLabel);
            // PENDING: mv.visitFrame

        } else if (javaClass == List.class) {
            int sequenceVar = nextVar.next();
            mv.visitInsn(DUP);
            mv.visitVarInsn(ASTORE, sequenceVar);
            int lengthVar = nextVar.next();
            Label endLabel = new Label();
            if (required) {
                mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "size", "()I", true);
                mv.visitInsn(DUP);
                mv.visitVarInsn(ISTORE, lengthVar);
                mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeUInt32", "(Lcom/cinnober/msgcodec/io/ByteSink;I)V", false);
            } else {
                Label nonNullLabel = new Label();
                mv.visitInsn(DUP);
                mv.visitJumpInsn(IFNONNULL, nonNullLabel);
                // null
                mv.visitInsn(POP);
                mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeNull", "(Lcom/cinnober/msgcodec/io/ByteSink;)V", false);
                mv.visitJumpInsn(GOTO, endLabel);
                // not null
                mv.visitLabel(nonNullLabel);
                // PENDING: mv.visitFrame
                mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "size", "()I", true);
                mv.visitInsn(DUP);
                mv.visitVarInsn(ISTORE, lengthVar);
                mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeUInt32", "(Lcom/cinnober/msgcodec/io/ByteSink;I)V", false);
            }
            // for loop, using iterator
            int iteratorVar = nextVar.next();
            mv.visitVarInsn(ALOAD, sequenceVar);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "iterator", "()Ljava/util/Iterator;", true);
            mv.visitVarInsn(ASTORE, iteratorVar);
            Label loopLabel = new Label();
            mv.visitLabel(loopLabel);
            // PENDING: mv.visitFrame
            mv.visitVarInsn(ALOAD, iteratorVar);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "hasNext", "()Z", true);
            mv.visitJumpInsn(IFEQ, endLabel);
            mv.visitVarInsn(ALOAD, outputStreamVar);
            mv.visitVarInsn(ALOAD, iteratorVar);
            mv.visitMethodInsn(INVOKEINTERFACE, "java/util/Iterator", "next", "()Ljava/lang/Object;", true);
            if (componentJavaClass.isPrimitive()) {
                mv.visitTypeInsn(CHECKCAST, Type.getInternalName(box(componentJavaClass)));
                unbox(mv, componentJavaClass);
            } else {
                mv.visitTypeInsn(CHECKCAST, Type.getInternalName(componentJavaClass));
            }

            // encode the element
            TypeDef.Sequence seqType = (TypeDef.Sequence) type;
            generateEncodeValue(mv, outputStreamVar, nextVar, true, seqType.getComponentType(), componentJavaClass,
                    null, schema, genClassInternalName, debugValueLabel + ".component", javaClassCodec);

            mv.visitJumpInsn(GOTO, loopLabel);
            mv.visitLabel(endLabel);
            // PENDING: mv.visitFrame
        } else {
            throw new IllegalArgumentException("Illegal sequence javaClass: " + javaClass);
        }
        throw new UnsupportedOperationException("Not implemented yet"); // TODO
    }

    // --- GENERATE DECODE VALUE ---------------------------------------------------------------------------------------

    /**
     * Generate value decoding using the blink input.
     *
     * <p>Defaults to <code>readBinary[Null].</code>
     *
     * @param required true if the field is required, otherwise false.
     * @param mv the method visitor, not null.
     * @see #generateDecodeValue
     */
    @Override
    protected void generateDecodeBinaryValue(TypeDef.Binary type, MethodVisitor mv, boolean required) {
        if (Integer.compareUnsigned(type.getMaxSize(), 255) <= 0) {
            // inline
            mv.visitLdcInsn(type.getMaxSize());
            if (required) {
                mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readBinary", "(Lcom/cinnober/msgcodec/io/ByteSource;I)[B", false);
            } else {
                mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readBinaryNull", "(Lcom/cinnober/msgcodec/io/ByteSource;I)[B", false);
            }
        } else {
            // data area
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, baseclassIName, "getMaxBinarySize",
                    "()I", false);
            throw new UnsupportedOperationException("Not implemented yet"); // TODO
        }
    }

    /**
     * Generate value decoding using the blink input.
     *
     * <p>Defaults to <code>readStringUTF8[Null].</code>
     *
     * @param required true if the field is required, otherwise false.
     * @param mv the method visitor, not null.
     * @see #generateDecodeValue
     */
    @Override
    protected void generateDecodeStringValue(TypeDef.StringUnicode type, MethodVisitor mv, boolean required) {
        if (Integer.compareUnsigned(type.getMaxSize(), 255) <= 0) {
            // inline
            mv.visitLdcInsn(type.getMaxSize());
            if (required) {
                mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readInlineStringUTF8",
                        "(Lcom/cinnober/msgcodec/io/ByteSource;I)Ljava/lang/String;", false);
            } else {
                mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readInlineStringUTF8Null",
                        "(Lcom/cinnober/msgcodec/io/ByteSource;I)Ljava/lang/String;", false);
            }
        } else {
            // data area
            mv.visitVarInsn(ALOAD, 0);
            mv.visitMethodInsn(INVOKEVIRTUAL, baseclassIName, "getMaxBinarySize",
                    "()I", false);
            throw new UnsupportedOperationException("Not implemented yet"); // TODO
        }
    }

    /**
     * Generate value decoding using the blink input.
     *
     * <p>Defaults to <code>readBigDecimal[Null].</code>
     *
     * @param required true if the field is required, otherwise false.
     * @param mv the method visitor, not null.
     * @see #generateDecodeValue
     */
    @Override
    protected void generateDecodeBigDecimalValue(boolean required, MethodVisitor mv) {
        if (required) {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readBigDecimal",
                    "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/math/BigDecimal;", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readBigDecimalNull",
                    "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/math/BigDecimal;", false);
        }
        throw new UnsupportedOperationException("Not implemented yet"); // TODO
    }

    /**
     * Generate value decoding using the blink input.
     *
     * <p>Defaults to <code>readBigInt[Null].</code>
     *
     * @param required true if the field is required, otherwise false.
     * @param mv the method visitor, not null.
     * @see #generateDecodeValue
     */
    @Override
    protected void generateDecodeBigIntValue(boolean required, MethodVisitor mv) {
        if (required) {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readBigInt",
                    "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/math/BigInteger;", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readBigIntNull",
                    "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/math/BigInteger;", false);
        }
        throw new UnsupportedOperationException("Not implemented yet"); // TODO
    }

    @Override
    protected void generateDecodeDynRefValue(MethodVisitor mv, boolean required, GroupDef refGroup,
            Class<?> javaClass) {
        mv.visitVarInsn(ALOAD, 0); // this
        mv.visitInsn(SWAP); // this and in
        if (required) {
            mv.visitMethodInsn(INVOKEVIRTUAL, baseclassIName,
                    "readDynamicGroup", "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/lang/Object;", false);
        } else {
            mv.visitMethodInsn(INVOKEVIRTUAL, baseclassIName,
                    "readDynamicGroupNull", "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/lang/Object;", false);
        }
        if (refGroup != null) {
            mv.visitTypeInsn(CHECKCAST, Type.getInternalName(javaClass));
        }
        throw new UnsupportedOperationException("Not implemented yet"); // TODO
    }

    // TODO: not reusable?
    @Override
    protected void generateDecodeSequenceValue(
            Class<?> javaClass, LocalVariable nextVar, boolean required, MethodVisitor mv,
            Class<?> componentJavaClass, int byteSourceVar, TypeDef type, Schema schema,
            String genClassInternalName, String debugValueLabel, boolean javaClassCodec)
            throws IllegalArgumentException {
        if (!javaClass.isArray() && javaClass != List.class) {
            throw new IllegalArgumentException("Illegal sequence javaClass: " + javaClass);
        }

        int lengthVar = nextVar.next();
        int sequenceVar = nextVar.next();
        Label finalEndLabel = new Label();
        if (required) {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readUInt32", "(Lcom/cinnober/msgcodec/io/ByteSource;)I", false);
            mv.visitVarInsn(ISTORE, lengthVar);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readUInt32Null",
                    "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/lang/Integer;", false);
            mv.visitInsn(DUP);
            mv.visitJumpInsn(IFNULL, finalEndLabel);
            unbox(mv, Integer.class);
            mv.visitVarInsn(ISTORE, lengthVar);
        }

        if (javaClass.isArray()) {
            mv.visitVarInsn(ILOAD, lengthVar);
            generateNewArray(mv, componentJavaClass);
        } else {
            mv.visitTypeInsn(NEW, "java/util/ArrayList");
            mv.visitInsn(DUP);
            mv.visitVarInsn(ILOAD, lengthVar);
            mv.visitMethodInsn(INVOKESPECIAL, "java/util/ArrayList", "<init>", "(I)V", false);
        }
        mv.visitVarInsn(ASTORE, sequenceVar);


        // for loop
        Label endLabel = new Label();
        int loopVar = nextVar.next();
        mv.visitInsn(ICONST_0);
        mv.visitVarInsn(ISTORE, loopVar);
        Label loopLabel = new Label();
        mv.visitLabel(loopLabel);
        // PENDING: mv.visitFrame
        mv.visitFrame(F_SAME, 0, null, 0, null);
        mv.visitVarInsn(ILOAD, loopVar);
        mv.visitVarInsn(ILOAD, lengthVar);
        mv.visitJumpInsn(IF_ICMPGE, endLabel);

        mv.visitVarInsn(ALOAD, sequenceVar);
        mv.visitVarInsn(ILOAD, loopVar);
        mv.visitVarInsn(ALOAD, byteSourceVar);

        // decode the element
        TypeDef.Sequence seqType = (TypeDef.Sequence) type;
        generateDecodeValue(mv, byteSourceVar, nextVar, true, seqType.getComponentType(), componentJavaClass, null,
                schema, genClassInternalName, debugValueLabel + ".component", javaClassCodec);

        // store the value
        if (javaClass.isArray()) {
            generateArrayStore(mv, componentJavaClass);
        } else {
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/ArrayList", "add", "(ILjava/lang/Object;)V", false);
        }

        mv.visitIincInsn(loopVar, 1);
        mv.visitJumpInsn(GOTO, loopLabel);
        mv.visitLabel(endLabel);
        mv.visitFrame(F_SAME, 0, null, 0, null);
        mv.visitVarInsn(ALOAD, sequenceVar);
        mv.visitLabel(finalEndLabel);
        mv.visitFrame(F_SAME, 0, null, 0, null);
        if (javaClass.isArray()) {
            mv.visitTypeInsn(CHECKCAST, Type.getInternalName(javaClass));
        }
        throw new UnsupportedOperationException("Not implemented yet"); // TODO
    }
}
