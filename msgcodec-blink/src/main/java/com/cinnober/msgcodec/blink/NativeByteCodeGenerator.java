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

import com.cinnober.msgcodec.GroupDef;
import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.TypeDef;
import java.util.List;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import static org.objectweb.asm.Opcodes.*;
import org.objectweb.asm.Type;

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
            mv.visitMaxs(3, nextWriteidVar);
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
            // cast bytesink to ByteBuf, set position to data area, etc
            throw new UnsupportedOperationException("Not implemented yet"); // TODO: add support for data area
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
            throw new UnsupportedOperationException("Not implemented yet"); // TODO: add support for data area
        }
    }

    @Override
    protected void generateEncodeBigDecimalValue(boolean required, MethodVisitor mv) {
        throw new UnsupportedOperationException("Not implemented yet"); // TODO: add support for data area
    }

    @Override
    protected void generateEncodeBigIntValue(boolean required, MethodVisitor mv) {
        throw new UnsupportedOperationException("Not implemented yet"); // TODO: add support for data area
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
        throw new UnsupportedOperationException("Not implemented yet"); // TODO: add support for data area
    }

    @Override
    protected void generateEncodeSequenceValue(
            Class<?> javaClass,
            LocalVariable nextVar,
            MethodVisitor mv,
            boolean required,
            int outputStreamVar,
            Class<?> componentJavaClass,
            TypeDef type,
            Schema schema,
            String genClassInternalName,
            String debugValueLabel,
            boolean javaClassCodec) throws IllegalArgumentException {

        throw new UnsupportedOperationException("Not implemented yet"); // TODO: add support for data area
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
            throw new UnsupportedOperationException("Not implemented yet"); // TODO: add support for data area
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
            throw new UnsupportedOperationException("Not implemented yet"); // TODO: add support for data area
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
        throw new UnsupportedOperationException("Not implemented yet"); // TODO: add support for data area
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
        throw new UnsupportedOperationException("Not implemented yet"); // TODO: add support for data area
    }

    @Override
    protected void generateDecodeDynRefValue(
            MethodVisitor mv,
            boolean required,
            GroupDef refGroup,
            Class<?> javaClass) {
        throw new UnsupportedOperationException("Not implemented yet"); // TODO: add support for data area
    }

    @Override
    protected void generateDecodeSequenceValue(
            Class<?> javaClass, LocalVariable nextVar, boolean required, MethodVisitor mv,
            Class<?> componentJavaClass, int byteSourceVar, TypeDef type, Schema schema,
            String genClassInternalName, String debugValueLabel, boolean javaClassCodec)
            throws IllegalArgumentException {

        throw new UnsupportedOperationException("Not implemented yet"); // TODO: add support for data area
    }
}
