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
class ByteCodeGenerator {
    private static final Logger log = Logger.getLogger(ByteCodeGenerator.class.getName());
    
    private static final boolean JAVACLASS_CODEC = true;
    private static final boolean GENERIC_CODEC = false;

    private static final String BASECLASS_INTERNALNAME = Type.getInternalName(GeneratedCodec.class);

    private static final String GENERATED_CLASS_INTERNALNAME = "com/cinnober/msgcodec/blink/GeneratedBlinkCodec";
    private static final String GENERATED_CLASS_NAME = "com.cinnober.msgcodec.blink.GeneratedBlinkCodec";

    private static final String BLINK_OUTPUT_INAME = Type.getInternalName(BlinkOutput.class);
    private static final String BLINK_INPUT_INAME = Type.getInternalName(BlinkInput.class);
    private static final String BYTE_SINK_INAME = Type.getInternalName(ByteSink.class);
    private static final String BYTE_SOURCE_INAME = Type.getInternalName(ByteSource.class);
    private static final String SCHEMA_INAME = Type.getInternalName(Schema.class);
    private static final String SCHEMA_BINDING_INAME = Type.getInternalName(SchemaBinding.class);


    public String getGeneratedClassName(int suffix) {
        return GENERATED_CLASS_NAME + suffix;
    }

    public byte[] generateClass(Schema schema, int suffix) {
        if (!schema.isBound()) {
            throw new IllegalArgumentException("Schema is not bound");
        }

        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_FRAMES);
        ClassVisitor cv = classWriter;
        StringWriter traceString = null;
        if (log.isLoggable(Level.FINER)) {
            traceString = new StringWriter();
            cv = new TraceClassVisitor(cv, new PrintWriter(traceString));
        }
        cv = new CheckClassAdapter(cv);
        if (schema.getBinding().getGroupTypeAccessor() == JavaClassGroupTypeAccessor.INSTANCE) {
            generateCodecJ(cv, schema, suffix);
        } else {
            generateCodecG(cv, schema, suffix);
        }
        byte[] bytes = classWriter.toByteArray();

        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "Generated codec class " + GENERATED_CLASS_INTERNALNAME + suffix + " for schema UID: " + schema.getUID());
            if (log.isLoggable(Level.FINER)) {
                log.finer("Generated code (#"+suffix+"):\n" + traceString.toString());
            }
            if (log.isLoggable(Level.FINEST)) {
                log.finest("Generated bytecode (#"+suffix+"):\n" + ByteArrays.toHex(bytes, 0, bytes.length, 1, 8, 32));
            }
        }
        return bytes;
    }

    /**
     * Generates a codec class that uses the JavaClassGroupTypeAccessor.
     *
     * <p>Generated layout:
     * <pre>
     * // fields
     * Factory factory_MessageType1; // unless a ConstructorFactory
     * Accessor accessor_MessageType1_field1; // unless a FieldAccessor or an IgnoreAccessor
     * ...
     *
     * // methods
     * Constructor(BlinkCodec, Schema);
     *
     * Object readStaticGroup(int groupId, ByteSource); // switch on groupId
     * ...
     * MessageType1 readStaticGroup_MessageType1(ByteSource);
     * MessageType2 readStaticGroup_MessageType2(ByteSource);
     * ...
     * void readStaticGroup_MessageType1(ByteSource, MessageType1);
     * void readStaticGroup_MessageType2(ByteSource, MessageType2);
     * ...
     * void writeStaticGroup(ByteSink, Object); // switch on class
     * ...
     * void writeStaticGroupWithId_MessageType1(ByteSink, MessageType1);
     * void writeStaticGroupWithId_MessageType2(ByteSink, MessageType2);
     * ...
     * void writeStaticGroup_MessageType1(ByteSink, MessageType1);
     * void writeStaticGroup_MessageType2(ByteSink, MessageType2);
     * ...
     * </pre>
     *
     * @param cv
     * @param schema
     * @param suffix
     */
    private void generateCodecJ(ClassVisitor cv, Schema schema, int suffix) {
        generateCodec(cv, schema, suffix, JAVACLASS_CODEC);
    }

    /**
     * Generates a codec class that does NOT use the JavaClassGroupTypeAccessor.
     *
     * <p>Generated layout:
     * <pre>
     * // fields
     * GroupTypeAccessor groupTypeAccessor;
     * Object groupType_MessageType1;
     * Factory factory_MessageType1;
     * Accessor accessor_MessageType1_field1; // unless an IgnoreAccessor
     * ...
     *
     * // methods
     * Constructor(BlinkCodec, Schema);
     *
     * Object readStaticGroup(int groupId, ByteSource); // switch on groupId
     * ...
     * Object readStaticGroup_MessageType1(ByteSource);
     * Object readStaticGroup_MessageType2(ByteSource);
     * ...
     * void readStaticGroup_MessageType1(ByteSource, Object);
     * void readStaticGroup_MessageType2(ByteSource, Object);
     * ...
     * void writeStaticGroup(ByteSink, Object); // switch on group type
     * ...
     * void writeStaticGroupWithId_MessageType1(ByteSink, Object);
     * void writeStaticGroupWithId_MessageType2(ByteSink, Object);
     * ...
     * void writeStaticGroup_MessageType1(ByteSink, Object);
     * void writeStaticGroup_MessageType2(ByteSink, Object);
     * ...
     * </pre>
     *
     * @param cv
     * @param schema
     * @param suffix
     */
    private void generateCodecG(ClassVisitor cv, Schema schema, int suffix) {
        generateCodec(cv, schema, suffix, GENERIC_CODEC);
    }

    private void generateCodec(ClassVisitor cv, Schema schema, int suffix, boolean javaClassCodec) {
        final String genClassInternalName = GENERATED_CLASS_INTERNALNAME + suffix;
        cv.visit(V1_7, ACC_PUBLIC + ACC_FINAL, genClassInternalName, null, BASECLASS_INTERNALNAME, null);

        generateConstructorAndFields(schema, cv, genClassInternalName, javaClassCodec);

        generateReadStaticGroup(schema, cv, genClassInternalName, javaClassCodec);
        generateReadStaticGroupForTypeAndCreate(schema, cv, genClassInternalName, javaClassCodec);
        generateReadStaticGroupForType(schema, cv, genClassInternalName, javaClassCodec);

        generateWriteStaticGroup(schema, cv, genClassInternalName, javaClassCodec);
        generateWriteStaticGroupForTypeWithId(schema, cv, genClassInternalName, javaClassCodec);
        generateWriteStaticGroupForType(schema, cv, genClassInternalName, javaClassCodec);

        cv.visitEnd();
    }

    // --- GENERATE CONSTRUCTOR ETC ------------------------------------------------------------------------------------

    private void generateConstructorAndFields(Schema schema, ClassVisitor cv, String genClassInternalName, boolean javaClassCodec) {
        MethodVisitor ctormv;
        ctormv = cv.visitMethod(ACC_PUBLIC, "<init>",
                "(Lcom/cinnober/msgcodec/blink/BlinkCodec;Lcom/cinnober/msgcodec/Schema;)V", null, null);
        int nextCtorVar = 3;
        ctormv.visitCode();
        ctormv.visitVarInsn(ALOAD, 0);
        ctormv.visitVarInsn(ALOAD, 1);
        ctormv.visitMethodInsn(INVOKESPECIAL, BASECLASS_INTERNALNAME, "<init>",
                "(Lcom/cinnober/msgcodec/blink/BlinkCodec;)V", false);

        if (!javaClassCodec) {
            // store the group type accessor

            // field
            FieldVisitor fv = cv.visitField(ACC_PRIVATE + ACC_FINAL, "groupTypeAccessor",
                    "Lcom/cinnober/msgcodec/GroupTypeAccessor;", null, null);
            fv.visitEnd();

            // ctor, init field
            ctormv.visitVarInsn(ALOAD, 0); // this
            ctormv.visitVarInsn(ALOAD, 2); // schema
            ctormv.visitMethodInsn(INVOKEVIRTUAL, "com/cinnober/msgcodec/Schema", "getBinding",
                    "()Lcom/cinnober/msgcodec/SchemaBinding;", false);
            ctormv.visitMethodInsn(INVOKEVIRTUAL, "com/cinnober/msgcodec/SchemaBinding", 
                    "getGroupTypeAccessor", "()Lcom/cinnober/msgcodec/GroupTypeAccessor;", false);
            ctormv.visitFieldInsn(PUTFIELD, genClassInternalName, "groupTypeAccessor",
                    "Lcom/cinnober/msgcodec/GroupTypeAccessor;");
        }

        for (GroupDef group : schema.getGroups()) {
            if (!javaClassCodec) {
                // store the group type
                // field
                FieldVisitor fv = cv.visitField(ACC_PRIVATE + ACC_FINAL, "groupType_" + group.getName(),
                        "Ljava/lang/Object;", null, null);
                fv.visitEnd();

                // ctor, init field
                ctormv.visitVarInsn(ALOAD, 0); // this
                ctormv.visitVarInsn(ALOAD, 2); // schema
                ctormv.visitLdcInsn(group.getName());
                ctormv.visitMethodInsn(INVOKEVIRTUAL, "com/cinnober/msgcodec/Schema", "getGroup",
                        "(Ljava/lang/String;)Lcom/cinnober/msgcodec/GroupDef;", false);
                ctormv.visitMethodInsn(INVOKEVIRTUAL, "com/cinnober/msgcodec/GroupDef", "getGroupType",
                        "()Ljava/lang/Object;", false);
                ctormv.visitFieldInsn(PUTFIELD, genClassInternalName,
                        "groupType_" + group.getName(),
                        "Ljava/lang/Object;");
            }

            Factory<?> factory = group.getFactory();
            if (isPublicConstructorFactory(factory)) {
                // no factory is needed
            } else {
                // field
                FieldVisitor fv = cv.visitField(ACC_PRIVATE + ACC_FINAL,
                        "factory_" + group.getName(),
                        "Lcom/cinnober/msgcodec/Factory;", null, null);
                fv.visitEnd();

                // ctor, init field
                ctormv.visitVarInsn(ALOAD, 0); // this
                ctormv.visitVarInsn(ALOAD, 2); // schema
                ctormv.visitLdcInsn(group.getName());
                ctormv.visitMethodInsn(INVOKEVIRTUAL, "com/cinnober/msgcodec/Schema", "getGroup",
                        "(Ljava/lang/String;)Lcom/cinnober/msgcodec/GroupDef;", false);
                ctormv.visitMethodInsn(INVOKEVIRTUAL, "com/cinnober/msgcodec/GroupDef", "getFactory",
                        "()Lcom/cinnober/msgcodec/Factory;", false);
                ctormv.visitFieldInsn(PUTFIELD, genClassInternalName,
                        "factory_" + group.getName(),
                        "Lcom/cinnober/msgcodec/Factory;");
            }

            for (FieldDef field : group.getFields()) {
                Accessor<?,?> accessor = field.getAccessor();
                if (isPublicFieldAccessor(accessor)) {
                    // no accessor needed
                } else if (accessor.getClass() == IgnoreAccessor.class) {
                    // no accessor needed
                } else {
                    // field
                    FieldVisitor fv = cv.visitField(ACC_PRIVATE + ACC_FINAL,
                            "accessor_" + group.getName() + "_" + field.getName(),
                            "Lcom/cinnober/msgcodec/Accessor;", null, null);
                    fv.visitEnd();

                    // ctor, init field
                    ctormv.visitVarInsn(ALOAD, 0); // this
                    ctormv.visitVarInsn(ALOAD, 2); // schema
                    ctormv.visitLdcInsn(group.getName());
                    ctormv.visitMethodInsn(INVOKEVIRTUAL, "com/cinnober/msgcodec/Schema", "getGroup",
                            "(Ljava/lang/String;)Lcom/cinnober/msgcodec/GroupDef;", false);
                    ctormv.visitLdcInsn(field.getName());
                    ctormv.visitMethodInsn(INVOKEVIRTUAL, "com/cinnober/msgcodec/GroupDef", "getField",
                            "(Ljava/lang/String;)Lcom/cinnober/msgcodec/FieldDef;", false);
                    ctormv.visitMethodInsn(INVOKEVIRTUAL, "com/cinnober/msgcodec/FieldDef", "getAccessor",
                            "()Lcom/cinnober/msgcodec/Accessor;", false);
                    ctormv.visitFieldInsn(PUTFIELD, genClassInternalName,
                            "accessor_" + group.getName() + "_" + field.getName(),
                            "Lcom/cinnober/msgcodec/Accessor;");
                }

            }

        }

        ctormv.visitInsn(RETURN);
        ctormv.visitMaxs(3, nextCtorVar); // PENDING: maxStack
        ctormv.visitEnd();
    }
    
    // --- GENERATE WRITE ----------------------------------------------------------------------------------------------

    private void generateWriteStaticGroup(Schema schema, ClassVisitor cv,
            String genClassInternalName, boolean javaClassCodec) {
        // method writeStaticGroupWithId - switch
        MethodVisitor mv = cv.visitMethod(ACC_PROTECTED, "writeStaticGroupWithId",
                "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/Object;)V", null, new String[] { "java/io/IOException" });
        int nextVar = 3;
        mv.visitCode();

        mv.visitVarInsn(ALOAD, 2);
        if (javaClassCodec) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "getClass", "()Ljava/lang/Class;", false);
        } else {
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, genClassInternalName, "groupTypeAccessor",
                    "Lcom/cinnober/msgcodec/GroupTypeAccessor;");
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEINTERFACE, "com/cinnober/msgcodec/GroupTypeAccessor",
                    "getGroupType", "(Ljava/lang/Object;)Ljava/lang/Object;", true);
        }
        int groupTypeVar = nextVar++;
        mv.visitInsn(DUP);
        mv.visitVarInsn(ASTORE, groupTypeVar);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object", "hashCode", "()I", false);

        // switch on class.hashCode()
        Map<Integer,ObjectHashCodeSwitchCase<Object>> casesByHashCode = new TreeMap<>();
        for (GroupDef group : schema.getGroups()) {
            Object groupType = group.getGroupType();
            int groupHash = groupType.hashCode();
            ObjectHashCodeSwitchCase<Object> hashCase = casesByHashCode.get(groupHash);
            if (hashCase == null) {
                hashCase = new ObjectHashCodeSwitchCase<>(groupHash);
                casesByHashCode.put(hashCase.hashCode, hashCase);
            }
            hashCase.add(groupType);
        }

        Label unknownHashLabel = new Label();
        {
            int[] caseValues = new int[casesByHashCode.size()];
            int i = 0;
            for (int hashCode : casesByHashCode.keySet()) {
                caseValues[i++] = hashCode;
            }
            Label[] caseLabels = new Label[casesByHashCode.size()];
            i = 0;
            for (ObjectHashCodeSwitchCase<Object> hashCase : casesByHashCode.values()) {
                caseLabels[i++] = hashCase.label;
            }
            mv.visitLookupSwitchInsn(unknownHashLabel, caseValues, caseLabels);
        }
        for (ObjectHashCodeSwitchCase<Object> hashCase : casesByHashCode.values()) {
            mv.visitLabel(hashCase.label);
            mv.visitFrame(F_SAME, 0, null, 0, null);
            for (ObjectSwitchCase<Object> classCase : hashCase.cases) {
                mv.visitVarInsn(ALOAD, groupTypeVar);
                if (javaClassCodec) {
                    mv.visitLdcInsn(Type.getType((Class<?>)classCase.object));
                    mv.visitJumpInsn(IF_ACMPEQ, classCase.label);
                } else {
                    GroupDef group = schema.getGroup(classCase.object);
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, genClassInternalName, "groupType_" + group.getName(),
                            "Ljava/lang/Object;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object",
                            "equals", "(Ljava/lang/Object;)Z", false);
                    mv.visitJumpInsn(IFNE, classCase.label); // IFNE = if not false
                }
            }
        }
        mv.visitLabel(unknownHashLabel);
        mv.visitFrame(F_SAME, 0, null, 0, null);
        mv.visitVarInsn(ALOAD, groupTypeVar);
        mv.visitMethodInsn(INVOKESTATIC, BASECLASS_INTERNALNAME, "unknownGroupType",
                "(Ljava/lang/Object;)Ljava/lang/IllegalArgumentException;", false);
        mv.visitInsn(ATHROW);


        for (ObjectHashCodeSwitchCase<Object> hashCase : casesByHashCode.values()) {
            for (ObjectSwitchCase<Object> classCase : hashCase.cases) {
                Object groupType = classCase.object;
                GroupDef group = schema.getGroup(groupType);
                String groupDescriptor =
                        javaClassCodec ? Type.getDescriptor((Class<?>)groupType) : "Ljava/lang/Object;";
                mv.visitLabel(classCase.label);
                mv.visitFrame(F_SAME, 0, null, 0, null);
                mv.visitVarInsn(ALOAD, 0); // this
                mv.visitVarInsn(ALOAD, 1); // out
                mv.visitVarInsn(ALOAD, 2); // obj
                if (javaClassCodec) {
                    mv.visitTypeInsn(CHECKCAST, Type.getInternalName((Class<?>)groupType));
                }
                mv.visitMethodInsn(INVOKEVIRTUAL, genClassInternalName, "writeStaticGroupWithId_" + group.getName(),
                        "(Lcom/cinnober/msgcodec/io/ByteSink;" + groupDescriptor + ")V", false);
                mv.visitInsn(RETURN);
            }
        }

        mv.visitMaxs(4, nextVar);
        mv.visitEnd();
    }
    
    private void generateWriteStaticGroupForTypeWithId(Schema schema, ClassVisitor cv,
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
                mv.visitLdcInsn(group.getId());
                mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeUInt32",
                        "(Lcom/cinnober/msgcodec/io/ByteSink;I)V", false);
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

    private void generateWriteStaticGroupForType(Schema schema, ClassVisitor cv, String genClassInternalName,
            boolean javaClassCodec) {
        for (GroupDef group : schema.getGroups()) {
            Object groupType = group.getGroupType();
            String groupDescriptor = javaClassCodec ? Type.getDescriptor((Class<?>)groupType) : "Ljava/lang/Object;";
            MethodVisitor writemv = cv.visitMethod(
                    ACC_PRIVATE,
                    "writeStaticGroup_" + group.getName(),
                    "(Lcom/cinnober/msgcodec/io/ByteSink;" + groupDescriptor + ")V",
                    null,
                    new String[] { "java/io/IOException" });
            writemv.visitCode();
            LocalVariable nextWriteVar = new LocalVariable(3);

            // write fields of super group
            if (group.getSuperGroup() != null) {
                GroupDef superGroup = schema.getGroup(group.getSuperGroup());
                Object superGroupType = superGroup.getGroupType();
                String superGroupDescriptor =
                        javaClassCodec ? Type.getDescriptor((Class<?>)superGroupType) : "Ljava/lang/Object;";
                writemv.visitVarInsn(ALOAD, 0);
                writemv.visitVarInsn(ALOAD, 1);
                writemv.visitVarInsn(ALOAD, 2);
                writemv.visitMethodInsn(INVOKEVIRTUAL,
                        genClassInternalName,
                        "writeStaticGroup_" + superGroup.getName(),
                        "(Lcom/cinnober/msgcodec/io/ByteSink;"+superGroupDescriptor+")V", false);
            }

            // fields
            for (FieldDef field : group.getFields()) {

                writemv.visitVarInsn(ALOAD, 1); // output stream

                Class<?> javaClass = field.getJavaClass();
                Accessor<?,?> accessor = field.getAccessor();
                if (isPublicFieldAccessor(accessor)) {                   
                    Field f = ((FieldAccessor)accessor).getField();
                    writemv.visitVarInsn(ALOAD, 2);
                    writemv.visitFieldInsn(GETFIELD, Type.getInternalName(f.getDeclaringClass()), f.getName(),
                            Type.getDescriptor(f.getType()));
                    if (!f.getType().equals(field.getJavaClass())) {
                        // this can happen when the field is a generic type variable in a super-class.
                        writemv.visitTypeInsn(CHECKCAST, Type.getInternalName(javaClass));
                    }
                } else if (accessor.getClass() == IgnoreAccessor.class) {
                    writemv.visitInsn(NULL);
                } else {
                    writemv.visitVarInsn(ALOAD, 0);
                    writemv.visitFieldInsn(GETFIELD, genClassInternalName,
                            "accessor_" + group.getName() + "_" + field.getName(),
                            "Lcom/cinnober/msgcodec/Accessor;");
                    writemv.visitVarInsn(ALOAD, 2); // instance
                    writemv.visitMethodInsn(INVOKEINTERFACE, "com/cinnober/msgcodec/Accessor", "getValue",
                            "(Ljava/lang/Object;)Ljava/lang/Object;", true);
                    if (javaClass.isPrimitive()) {
                        writemv.visitTypeInsn(CHECKCAST, Type.getInternalName(box(javaClass)));
                        unbox(writemv, javaClass);
                    } else {
                        writemv.visitTypeInsn(CHECKCAST, Type.getInternalName(javaClass));
                    }
                }
                // the output stream and the value is now on the stack
                generateEncodeValue(writemv, 1, nextWriteVar, field.isRequired(), field.getType(), javaClass,
                        field.getComponentJavaClass(), schema, genClassInternalName,
                        group.getName() + "." + field.getName(), javaClassCodec);
            }

            // end write
            writemv.visitInsn(RETURN);
            writemv.visitMaxs(4, nextWriteVar.get()); // PENDING: maxStack
            writemv.visitEnd();
        }
    }

    // --- GENERATE READ -----------------------------------------------------------------------------------------------

    private void generateReadStaticGroup(Schema schema, ClassVisitor cv, String genClassInternalName,
            boolean javaClassCodec) {
        MethodVisitor mv = cv.visitMethod(ACC_PROTECTED, "readStaticGroup",
                "(ILcom/cinnober/msgcodec/io/ByteSource;)Ljava/lang/Object;", null,
                new String[] { "java/io/IOException" });
        int nextVar = 3;
        mv.visitCode();

        Map<Integer,Label> labelsByGroupId = new TreeMap<>();
        for (GroupDef group : schema.getGroups()) {
            if (group.getId() != -1) {
                labelsByGroupId.put(group.getId(), new Label());
            }
        }
        mv.visitVarInsn(ILOAD, 1); // group id
        Label unknownGroupIdLabel = new Label();
        {
            int[] caseValues = new int[labelsByGroupId.size()];
            int i = 0;
            for (int groupId : labelsByGroupId.keySet()) {
                caseValues[i++] = groupId;
            }
            Label[] caseLabels = labelsByGroupId.values().toArray(new Label[labelsByGroupId.size()]);
            mv.visitLookupSwitchInsn(unknownGroupIdLabel, caseValues, caseLabels);
        }

        for (Map.Entry<Integer, Label> caseEntry : labelsByGroupId.entrySet()) {
            GroupDef group = schema.getGroup(caseEntry.getKey().intValue());
            Object groupType = group.getGroupType();
            String groupDescriptor = javaClassCodec ? Type.getDescriptor((Class<?>)groupType) : "Ljava/lang/Object;";

            mv.visitLabel(caseEntry.getValue());
            mv.visitFrame(F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, 0);
            mv.visitVarInsn(ALOAD, 2);
            mv.visitMethodInsn(INVOKEVIRTUAL, genClassInternalName, "readStaticGroup_" + group.getName(),
                    "(Lcom/cinnober/msgcodec/io/ByteSource;)" + groupDescriptor, false);
            mv.visitInsn(ARETURN);
        }
        // default case
        mv.visitLabel(unknownGroupIdLabel);
        mv.visitFrame(F_SAME, 0, null, 0, null);
        mv.visitVarInsn(ILOAD, 1);
        mv.visitMethodInsn(INVOKESTATIC, BASECLASS_INTERNALNAME, "unknownGroupId",
                "(I)Lcom/cinnober/msgcodec/DecodeException;", false);
        mv.visitInsn(ATHROW);
        mv.visitMaxs(2, nextVar); // PENDING: maxStack
        mv.visitEnd();
    }
    
    private void generateReadStaticGroupForTypeAndCreate(Schema schema, ClassVisitor cv,
            String genClassInternalName, boolean javaClassCodec) {
        for (GroupDef group : schema.getGroups()) {
            Object groupType = group.getGroupType();
            String groupDescriptor = javaClassCodec ? Type.getDescriptor((Class<?>)groupType) : "Ljava/lang/Object;";
            String groupInternalName = javaClassCodec ? Type.getInternalName((Class<?>)groupType) : null;
            MethodVisitor readmv = cv.visitMethod(
                    ACC_PRIVATE,
                    "readStaticGroup_" + group.getName(),
                    "(Lcom/cinnober/msgcodec/io/ByteSource;)" + groupDescriptor,
                    null,
                    new String[] { "java/io/IOException" });
            readmv.visitCode();
            int nextReadVar = 2;

            Factory<?> factory = group.getFactory();
            if (isPublicConstructorFactory(factory)) {
                // read, create instance
                readmv.visitTypeInsn(NEW, groupInternalName);
                readmv.visitInsn(DUP);
                readmv.visitMethodInsn(INVOKESPECIAL, groupInternalName, "<init>", "()V", false);
            } else {
                // read, create instance
                readmv.visitVarInsn(ALOAD, 0); // this
                readmv.visitFieldInsn(GETFIELD, genClassInternalName, 
                        "factory_" + group.getName(),
                        "Lcom/cinnober/msgcodec/Factory;");
                readmv.visitMethodInsn(INVOKEINTERFACE, "com/cinnober/msgcodec/Factory", "newInstance",
                        "()Ljava/lang/Object;", true);
                if (javaClassCodec) {
                    readmv.visitTypeInsn(CHECKCAST, groupInternalName);
                }
            }
            final int readInstanceVar = nextReadVar++;
            readmv.visitVarInsn(ASTORE, readInstanceVar);

            readmv.visitVarInsn(ALOAD, 0);
            readmv.visitVarInsn(ALOAD, 1);
            readmv.visitVarInsn(ALOAD, readInstanceVar);
            readmv.visitMethodInsn(INVOKEVIRTUAL, genClassInternalName, "readStaticGroup_" + group.getName(),
                    "(Lcom/cinnober/msgcodec/io/ByteSource;"+groupDescriptor+")V", false);

            // end read
            readmv.visitVarInsn(ALOAD, readInstanceVar);
            readmv.visitInsn(ARETURN);
            readmv.visitMaxs(4, nextReadVar); // PENDING: maxStack
            readmv.visitEnd();
        }
    }
    private void generateReadStaticGroupForType(final Schema schema, ClassVisitor cv,
            final String genClassInternalName, final boolean javaClassCodec) {
        for (final GroupDef group : schema.getGroups()) {
            Object groupType = group.getGroupType();
            String groupDescriptor = javaClassCodec ? Type.getDescriptor((Class<?>)groupType) : "Ljava/lang/Object;";
            final MethodVisitor mv = cv.visitMethod(
                    ACC_PRIVATE,
                    "readStaticGroup_" + group.getName(),
                    "(Lcom/cinnober/msgcodec/io/ByteSource;"+groupDescriptor+")V",
                    null,
                    new String[] { "java/io/IOException" });
            mv.visitCode();
            final LocalVariable nextVar = new LocalVariable(3);

            // read fields of super group
            if (group.getSuperGroup() != null) {
                GroupDef superGroup = schema.getGroup(group.getSuperGroup());
                Object superGroupType = superGroup.getGroupType();
                String superGroupDescriptor =
                        javaClassCodec ? Type.getDescriptor((Class<?>)superGroupType) : "Ljava/lang/Object;";
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, 1);
                mv.visitVarInsn(ALOAD, 2);
                mv.visitMethodInsn(INVOKEVIRTUAL,
                        genClassInternalName,
                        "readStaticGroup_" + superGroup.getName(),
                        "(Lcom/cinnober/msgcodec/io/ByteSource;"+superGroupDescriptor+")V", false);
            }

            // fields
            for (final FieldDef field : group.getFields()) {
                final Class<?> javaClass = field.getJavaClass();

                // "lambda" that can generate code for reading the value
                Runnable readValue = new Runnable() {
                    @Override
                    public void run() {
                        Label tryStart = new Label();
                        Label tryEnd = new Label();
                        Label tryCatch = new Label();
                        Label tryAfter = new Label();
                        mv.visitTryCatchBlock(tryStart, tryEnd, tryCatch, "java/lang/Exception");

                        mv.visitLabel(tryStart);
                        mv.visitVarInsn(ALOAD, 1); // input stream
                        generateDecodeValue(mv, 1, nextVar, field.isRequired(), field.getType(), javaClass,
                                field.getComponentJavaClass(), schema, genClassInternalName,
                                group.getName() + "." + field.getName(), javaClassCodec);
                        mv.visitLabel(tryEnd);
                        mv.visitJumpInsn(GOTO, tryAfter);
                        mv.visitLabel(tryCatch);
                        int caughtExVar = nextVar.next();
                        mv.visitVarInsn(ASTORE, caughtExVar);
                        mv.visitTypeInsn(NEW, "com/cinnober/msgcodec/blink/FieldDecodeException");
                        mv.visitInsn(DUP);
                        mv.visitLdcInsn(field.getName());
                        mv.visitVarInsn(ALOAD, caughtExVar);
                        mv.visitMethodInsn(INVOKESPECIAL, "com/cinnober/msgcodec/blink/FieldDecodeException",
                                "<init>", "(Ljava/lang/String;Ljava/lang/Throwable;)V", false);
                        mv.visitInsn(ATHROW);
                        mv.visitLabel(tryAfter);
                    }
                };

                Accessor<?,?> accessor = field.getAccessor();
                if (isPublicFieldAccessor(accessor)) {
                    Field f = ((FieldAccessor)accessor).getField();
                    mv.visitVarInsn(ALOAD, 2); // instance
                    // value
                    readValue.run();
                    // store
                    mv.visitFieldInsn(PUTFIELD, Type.getInternalName(f.getDeclaringClass()), f.getName(),
                            Type.getDescriptor(f.getType()));
                } else if(accessor.getClass() == IgnoreAccessor.class) {
                    // value
                    readValue.run();
                    // discard
                    mv.visitInsn(POP);
                } else {
                    // accessor
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, genClassInternalName,
                            "accessor_" + group.getName() + "_" + field.getName(),
                            "Lcom/cinnober/msgcodec/Accessor;");
                    // instance
                    mv.visitVarInsn(ALOAD, 2); // instance
                    // value
                    readValue.run();
                    if (javaClass.isPrimitive()) {
                        box(mv, javaClass);
                    }
                    // store
                    mv.visitMethodInsn(INVOKEINTERFACE, "com/cinnober/msgcodec/Accessor", "setValue",
                            "(Ljava/lang/Object;Ljava/lang/Object;)V", true);
                }
            }

            mv.visitInsn(RETURN);
            mv.visitMaxs(6, nextVar.get()); // PENDING: maxStack
            mv.visitEnd();
        }
    }

    // --- GENERATE ENCODE VALUE ---------------------------------------------------------------------------------------

    /**
     * Generate instructions to encode the specified value type.
     * The values on the stack are; the output stream and the value to be encoded.
     * After this call these values are expected to be consumed.
     *
     * @param mv the method visitor, open for code instructions.
     * @param outputStreamVar the variable instance that contains the output stream.
     * @param nextVar the next variable
     * @param required true if the field is required, otherwise false.
     * @param type the field type
     * @param javaClass the field java class
     * @param componentJavaClass the field component java class, or null if not a sequence type
     * @param schema the protocol schema, not null
     * @param genClassInternalName the internal name of the generated class
     */
    private void generateEncodeValue(
            MethodVisitor mv,
            int outputStreamVar,
            LocalVariable nextVar,
            boolean required,
            TypeDef type,
            Class<?> javaClass,
            Class<?> componentJavaClass,
            Schema schema,
            String genClassInternalName,
            String debugValueLabel,
            boolean javaClassCodec) {

        type = schema.resolveToType(type, false);
        GroupDef refGroup = schema.resolveToGroup(type);

        if (javaClass.isPrimitive() && !required) {
            box(mv, javaClass);
        } else if (!javaClass.isPrimitive() && required) {
            mv.visitInsn(DUP);
            Label notNullLabel = new Label();
            mv.visitJumpInsn(IFNONNULL, notNullLabel);
            mv.visitInsn(POP); // output stream
            mv.visitLdcInsn(debugValueLabel);
            mv.visitMethodInsn(INVOKESTATIC, BASECLASS_INTERNALNAME, "missingRequiredValue",
                    "(Ljava/lang/String;)Ljava/lang/IllegalArgumentException;", false);
            mv.visitInsn(ATHROW);
            mv.visitLabel(notNullLabel);
            unbox(mv, javaClass);
        }
        switch (type.getType()) {
            case INT8:
                if (required) {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeInt8",
                            "(Lcom/cinnober/msgcodec/io/ByteSink;B)V", false);
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeInt8Null",
                            "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/Byte;)V", false);
                }
                break;
            case UINT8:
                if (required) {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeUInt8",
                            "(Lcom/cinnober/msgcodec/io/ByteSink;B)V", false);
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeUInt8Null",
                            "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/Byte;)V", false);
                }
                break;
            case INT16:
                if (required) {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeInt16",
                            "(Lcom/cinnober/msgcodec/io/ByteSink;S)V", false);
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeInt16Null",
                            "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/Short;)V", false);
                }
                break;
            case UINT16:
                if (required) {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeUInt16",
                            "(Lcom/cinnober/msgcodec/io/ByteSink;S)V", false);
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeUInt16Null",
                            "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/Short;)V", false);
                }
                break;
            case INT32:
                if (required) {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeInt32",
                            "(Lcom/cinnober/msgcodec/io/ByteSink;I)V", false);
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeInt32Null",
                            "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/Integer;)V", false);
                }
                break;
            case UINT32:
                if (required) {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeUInt32",
                            "(Lcom/cinnober/msgcodec/io/ByteSink;I)V", false);
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeUInt32Null",
                            "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/Integer;)V", false);
                }
                break;
            case INT64:
                if (required) {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeInt64",
                            "(Lcom/cinnober/msgcodec/io/ByteSink;J)V", false);
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeInt64Null",
                            "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/Long;)V", false);
                }
                break;
            case UINT64:
                if (required) {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeUInt64",
                            "(Lcom/cinnober/msgcodec/io/ByteSink;J)V", false);
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeUInt64Null",
                            "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/Long;)V", false);
                }
                break;
            case FLOAT32:
                if (required) {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeFloat32",
                            "(Lcom/cinnober/msgcodec/io/ByteSink;F)V", false);
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeFloat32Null",
                            "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/Float;)V", false);
                }
                break;
            case FLOAT64:
                if (required) {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeFloat64",
                            "(Lcom/cinnober/msgcodec/io/ByteSink;D)V", false);
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeFloat64Null",
                            "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/Double;)V", false);
                }
                break;
            case BIGINT:
                if (required) {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeBigInt",
                            "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/math/BigInteger;)V", false);
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeBigIntNull",
                            "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/math/BigInteger;)V", false);
                }
                break;
            case DECIMAL:
                if (required) {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeDecimal",
                            "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/math/BigDecimal;)V", false);
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeDecimalNull",
                            "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/math/BigDecimal;)V", false);
                }
                break;
            case BIGDECIMAL:
                if (required) {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeBigDecimal",
                            "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/math/BigDecimal;)V", false);
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeBigDecimalNull",
                            "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/math/BigDecimal;)V", false);
                }
                break;
            case STRING:
                if (required) {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeStringUTF8",
                            "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/String;)V", false);
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeStringUTF8Null",
                            "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/String;)V", false);
                }
                break;
            case BINARY:
                if (required) {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeBinary",
                            "(Lcom/cinnober/msgcodec/io/ByteSink;[B)V", false);
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeBinaryNull",
                            "(Lcom/cinnober/msgcodec/io/ByteSink;[B)V", false);
                }
                break;
            case BOOLEAN:
                if (required) {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeBoolean",
                            "(Lcom/cinnober/msgcodec/io/ByteSink;Z)V", false);
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeBooleanNull",
                            "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/Boolean;)V", false);
                }
                break;
            case ENUM:
                generateEncodeEnumValue((TypeDef.Enum) type, nextVar, javaClass, required, mv,
                        debugValueLabel);
                break;
            case TIME:
                generateEncodeTimeValue((TypeDef.Time) type, javaClass, required, mv);
                break;
            case SEQUENCE:
                generateEncodeSequenceValue(javaClass, nextVar, mv, required, outputStreamVar,
                        componentJavaClass, type, schema, genClassInternalName, debugValueLabel, javaClassCodec);
                break;
            case REFERENCE:
                generateEncodeRefValue(refGroup, required, nextVar, mv, genClassInternalName,
                        outputStreamVar, (TypeDef.Reference) type, javaClassCodec);
                break;
            case DYNAMIC_REFERENCE:
                generateEncodeDynRefValue(nextVar, mv, required);
                break;
            default:
                throw new RuntimeException("Unhandled case: " + type.getType());
        }
    }

    private void generateEncodeDynRefValue(LocalVariable nextVar, MethodVisitor mv, boolean required) {
        // PENDING: validate that the instance class is a subclass of refGroup (unless null)
        int instanceVar = nextVar.next();
        mv.visitVarInsn(ASTORE, instanceVar);
        mv.visitVarInsn(ALOAD, 0); // this
        mv.visitInsn(SWAP); // this and out
        mv.visitVarInsn(ALOAD, instanceVar);
        if (required) {
            mv.visitMethodInsn(INVOKEVIRTUAL, BASECLASS_INTERNALNAME,
                    "writeDynamicGroup", "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/Object;)V", false);
        } else {
            mv.visitMethodInsn(INVOKEVIRTUAL, BASECLASS_INTERNALNAME,
                    "writeDynamicGroupNull", "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/Object;)V", false);
        }
    }

    private void generateEncodeRefValue(GroupDef refGroup, boolean required, LocalVariable nextVar, MethodVisitor mv,
            String genClassInternalName, int outputStreamVar, TypeDef.Reference type,
            boolean javaClassCodec) throws IllegalArgumentException {
        if (refGroup != null) {
            Object refGroupType = refGroup.getGroupType();
            String refGroupDescriptor =
                    javaClassCodec ? Type.getDescriptor((Class<?>)refGroupType) : "Ljava/lang/Object;";
            if (required) {
                int instanceVar = nextVar.next();
                mv.visitVarInsn(ASTORE, instanceVar);
                mv.visitVarInsn(ALOAD, 0); // this
                mv.visitInsn(SWAP); // this and out
                mv.visitVarInsn(ALOAD, instanceVar);
                mv.visitMethodInsn(INVOKEVIRTUAL, genClassInternalName, "writeStaticGroup_" + refGroup.getName(),
                        "(Lcom/cinnober/msgcodec/io/ByteSink;" + refGroupDescriptor + ")V", false);
            } else {
                int instanceVar = nextVar.next();
                mv.visitInsn(DUP);
                Label nonNullLabel = new Label();
                Label endLabel = new Label();
                mv.visitJumpInsn(IFNONNULL, nonNullLabel);
                // null
                mv.visitInsn(POP);
                mv.visitInsn(ICONST_0); // false
                mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeBoolean", "(Lcom/cinnober/msgcodec/io/ByteSink;Z)V", false);
                mv.visitJumpInsn(GOTO, endLabel);

                // not null
                mv.visitLabel(nonNullLabel);
                // PENDING: mv.visitFrame
                mv.visitVarInsn(ASTORE, instanceVar);
                mv.visitInsn(ICONST_1); // true
                mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeBoolean", "(Lcom/cinnober/msgcodec/io/ByteSink;Z)V", false);
                mv.visitVarInsn(ALOAD, 0); // this
                mv.visitVarInsn(ALOAD, outputStreamVar);
                mv.visitVarInsn(ALOAD, instanceVar);
                mv.visitMethodInsn(INVOKEVIRTUAL, genClassInternalName, "writeStaticGroup_" + refGroup.getName(),
                        "(Lcom/cinnober/msgcodec/io/ByteSink;" + refGroupDescriptor + ")V", false);
                mv.visitLabel(endLabel);
                // PENDING: mv.visitFrame
            }
        } else {
            throw new IllegalArgumentException("Illegal reference: " + type);
        }
    }

    private void generateEncodeSequenceValue(
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
                mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeUInt32", "(Lcom/cinnober/msgcodec/io/ByteSink;I)V", false);
            } else {
                Label nonNullLabel = new Label();
                mv.visitInsn(DUP);
                mv.visitJumpInsn(IFNONNULL, nonNullLabel);
                // null
                mv.visitInsn(POP);
                mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeNull", "(Lcom/cinnober/msgcodec/io/ByteSink;)V", false);
                mv.visitJumpInsn(GOTO, endLabel);
                // not null
                mv.visitLabel(nonNullLabel);
                // PENDING: mv.visitFrame
                mv.visitInsn(ARRAYLENGTH);
                mv.visitInsn(DUP);
                mv.visitVarInsn(ISTORE, lengthVar);
                mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeUInt32", "(Lcom/cinnober/msgcodec/io/ByteSink;I)V", false);
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
                mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeUInt32", "(Lcom/cinnober/msgcodec/io/ByteSink;I)V", false);
            } else {
                Label nonNullLabel = new Label();
                mv.visitInsn(DUP);
                mv.visitJumpInsn(IFNONNULL, nonNullLabel);
                // null
                mv.visitInsn(POP);
                mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeNull", "(Lcom/cinnober/msgcodec/io/ByteSink;)V", false);
                mv.visitJumpInsn(GOTO, endLabel);
                // not null
                mv.visitLabel(nonNullLabel);
                // PENDING: mv.visitFrame
                mv.visitMethodInsn(INVOKEINTERFACE, "java/util/List", "size", "()I", true);
                mv.visitInsn(DUP);
                mv.visitVarInsn(ISTORE, lengthVar);
                mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeUInt32", "(Lcom/cinnober/msgcodec/io/ByteSink;I)V", false);
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
    }

    private void generateEncodeTimeValue(TypeDef.Time type,
            Class<?> javaClass, boolean required, MethodVisitor mv) throws RuntimeException {
        if (javaClass == long.class || javaClass == Long.class) {
            if (required) {
                mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeInt64", "(Lcom/cinnober/msgcodec/io/ByteSink;J)V", false);
            } else {
                mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeInt64Null",
                        "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/Long;)V", false);
            }
        } else if (javaClass == int.class || javaClass == Integer.class) {
            if (required) {
                mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeInt32", "(Lcom/cinnober/msgcodec/io/ByteSink;I)V", false);
            } else {
                mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeInt32Null",
                        "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/Integer;)V", false);
            }
        } else if (javaClass == Date.class) {

            Label nullLabel = new Label();
            Label endLabel = new Label();
            if (!required) {
                mv.visitInsn(DUP);
                mv.visitJumpInsn(IFNULL, nullLabel);
            }
            
            // not null
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/Date", "getTime", "()J", false);
            // handle differences in UNIT and EPOCH
            long epochOffset = DateUtil.getEpochOffset(type.getEpoch());
            long timeInMillis = DateUtil.getTimeInMillis(type.getUnit());
            // wireTime = (dateTime - epochOffset) / timeUnitInMillis);
            if (epochOffset != 0) {
                mv.visitLdcInsn(epochOffset);
                mv.visitInsn(LSUB);
            }
            if (timeInMillis != 1) {
                mv.visitLdcInsn(timeInMillis);
                mv.visitInsn(LDIV);
            }
            mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeInt64", "(Lcom/cinnober/msgcodec/io/ByteSink;J)V", false);

            if (!required) {
                mv.visitJumpInsn(GOTO, endLabel);
                mv.visitLabel(nullLabel);
                mv.visitInsn(POP);
                mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeNull", "(Lcom/cinnober/msgcodec/io/ByteSink;)V", false);
                mv.visitLabel(endLabel);
            }
        } else {
            throw new IllegalArgumentException("Illegal time javaClass: " + javaClass);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void generateEncodeEnumValue(TypeDef.Enum enumType, LocalVariable nextVar,
            Class<?> javaClass, boolean required, MethodVisitor mv,
            String debugValueLabel) throws IllegalArgumentException {
        if (javaClass.isEnum()) {
            Label endLabel = new Label();
            if (!required) {
                Label nonNullLabel = new Label();
                mv.visitInsn(DUP);
                mv.visitJumpInsn(IFNONNULL, nonNullLabel);
                // null
                mv.visitInsn(POP);
                mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeNull", "(Lcom/cinnober/msgcodec/io/ByteSink;)V", false);
                mv.visitJumpInsn(GOTO, endLabel);
                // not null
                mv.visitLabel(nonNullLabel);
            }
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Enum", "ordinal", "()I", false);
            //switch
            EnumSymbols enumSymbols = new EnumSymbols(enumType, javaClass);
            Enum[] enumValues = ((Class<Enum>)javaClass).getEnumConstants();
            int[] ordinals = new int[enumValues.length];
            Label[] labels = new Label[enumValues.length];
            for (int i=0; i<enumValues.length; i++) {
                ordinals[i] = i;
                labels[i] = new Label();
            }
            Label defaultLabel = new Label();
            Label writeLabel = new Label();
            int symbolIdVar = nextVar.next();
            mv.visitLookupSwitchInsn(defaultLabel, ordinals, labels);
            for (int i=0; i<enumValues.length; i++) {
                mv.visitLabel(labels[i]);
                Symbol symbol = enumSymbols.getSymbol(enumValues[i]);
                if (symbol != null) {
                    mv.visitLdcInsn(symbol.getId());
                    mv.visitJumpInsn(GOTO, writeLabel);
                } else {
                    mv.visitLdcInsn(debugValueLabel);
                    mv.visitVarInsn(ALOAD, symbolIdVar);
                    mv.visitMethodInsn(INVOKESTATIC, BASECLASS_INTERNALNAME, "unmappableEnumSymbolValue",
                            "(Ljava/lang/String;Ljava/lang/Enum;)Ljava/lang/IllegalArgumentException;", false);
                    mv.visitInsn(ATHROW);
                }
            }
            mv.visitLabel(defaultLabel);
            mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
            mv.visitInsn(DUP);
            mv.visitLdcInsn("Should not happen: reached generated switch default case for value " + debugValueLabel);
            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V", false);
            mv.visitInsn(ATHROW);

            // write
            mv.visitLabel(writeLabel);
            mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeUInt32", "(Lcom/cinnober/msgcodec/io/ByteSink;I)V", false);
            // end
            mv.visitLabel(endLabel);
        } else if (javaClass == int.class || javaClass == Integer.class) {
            // PENDING: validate that the value is a correct enum value?
            if (required) {
                mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeUInt32", "(Lcom/cinnober/msgcodec/io/ByteSink;I)V", false);
            } else {
                mv.visitMethodInsn(INVOKESTATIC, BLINK_OUTPUT_INAME, "writeUInt32Null",
                        "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/Integer;)V", false);
            }
        } else {
            throw new IllegalArgumentException("Illegal enum javaClass: " + javaClass);
        }
    }

    // --- GENERATE DECODE VALUE ---------------------------------------------------------------------------------------

    /**
     * Generate instructions to encode the specified value type.
     * The value on the stack is the input stream.
     * After this call the input stream is expected to be consumed, and the decoded value be placed on the stack.
     */
    private void generateDecodeValue(MethodVisitor mv, int inputStreamVar, LocalVariable nextVar,
            boolean required, TypeDef type, Class<?> javaClass, Class<?> componentJavaClass, Schema schema,
            String genClassInternalName, String debugValueLabel, boolean javaClassCodec) {
        type = schema.resolveToType(type, false);
        GroupDef refGroup = schema.resolveToGroup(type);

        switch (type.getType()) {
            case INT8:
                if (required) {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readInt8",
                            "(Lcom/cinnober/msgcodec/io/ByteSource;)B", false);
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readInt8Null",
                            "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/lang/Byte;", false);
                }
                break;
            case UINT8:
                if (required) {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readUInt8",
                            "(Lcom/cinnober/msgcodec/io/ByteSource;)B", false);
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readUInt8Null",
                            "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/lang/Byte;", false);
                }
                break;
            case INT16:
                if (required) {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readInt16",
                            "(Lcom/cinnober/msgcodec/io/ByteSource;)S", false);
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readInt16Null",
                            "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/lang/Short;", false);
                }
                break;
            case UINT16:
                if (required) {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readUInt16",
                            "(Lcom/cinnober/msgcodec/io/ByteSource;)S", false);
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readUInt16Null",
                            "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/lang/Short;", false);
                }
                break;
            case INT32:
                if (required) {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readInt32",
                            "(Lcom/cinnober/msgcodec/io/ByteSource;)I", false);
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readInt32Null",
                            "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/lang/Integer;", false);
                }
                break;
            case UINT32:
                if (required) {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readUInt32",
                            "(Lcom/cinnober/msgcodec/io/ByteSource;)I", false);
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readUInt32Null",
                            "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/lang/Integer;", false);
                }
                break;
            case INT64:
                if (required) {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readInt64",
                            "(Lcom/cinnober/msgcodec/io/ByteSource;)J", false);
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readInt64Null",
                            "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/lang/Long;", false);
                }
                break;
            case UINT64:
                if (required) {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readUInt64",
                            "(Lcom/cinnober/msgcodec/io/ByteSource;)J", false);
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readUInt64Null",
                            "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/lang/Long;", false);
                }
                break;
            case FLOAT32:
                if (required) {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readFloat32",
                            "(Lcom/cinnober/msgcodec/io/ByteSource;)F", false);
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readFloat32Null",
                            "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/lang/Float;", false);
                }
                break;
            case FLOAT64:
                if (required) {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readFloat64",
                            "(Lcom/cinnober/msgcodec/io/ByteSource;)D", false);
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readFloat64Null",
                            "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/lang/Double;", false);
                }
                break;
            case BIGINT:
                if (required) {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readBigInt",
                            "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/math/BigInteger;", false);
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readBigIntNull",
                            "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/math/BigInteger;", false);
                }
                break;
            case DECIMAL:
                if (required) {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readDecimal",
                            "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/math/BigDecimal;", false);
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readDecimalNull",
                            "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/math/BigDecimal;", false);
                }
                break;
            case BIGDECIMAL:
                if (required) {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readBigDecimal",
                            "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/math/BigDecimal;", false);
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readBigDecimalNull",
                            "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/math/BigDecimal;", false);
                }
                break;
            case STRING:
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKEVIRTUAL, BASECLASS_INTERNALNAME, "getMaxBinarySize",
                        "()I", false);
                if (required) {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readStringUTF8",
                            "(Lcom/cinnober/msgcodec/io/ByteSource;I)Ljava/lang/String;", false);
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readStringUTF8Null",
                            "(Lcom/cinnober/msgcodec/io/ByteSource;I)Ljava/lang/String;", false);
                }
                break;
            case BINARY:
                mv.visitVarInsn(ALOAD, 0);
                mv.visitMethodInsn(INVOKEVIRTUAL, BASECLASS_INTERNALNAME, "getMaxBinarySize",
                        "()I", false);
                if (required) {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readBinary", "(Lcom/cinnober/msgcodec/io/ByteSource;I)[B", false);
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readBinaryNull", "(Lcom/cinnober/msgcodec/io/ByteSource;I)[B", false);
                }
                break;
            case BOOLEAN:
                if (required) {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readBoolean",
                            "(Lcom/cinnober/msgcodec/io/ByteSource;)Z", false);
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readBooleanNull",
                            "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/lang/Boolean;", false);
                }
                break;
            case ENUM:
                generateDecodeEnumValue(required, mv, type, nextVar, javaClass, debugValueLabel);
                break;
            case TIME:
                generateDecodeTimeValue((TypeDef.Time) type, javaClass, required, mv, nextVar);
                break;
            case SEQUENCE:
                generateDecodeSequenceValue(javaClass, nextVar, required, mv, componentJavaClass,
                        inputStreamVar, type, schema, genClassInternalName, debugValueLabel, javaClassCodec);
                break;
            case REFERENCE:
                generateDecodeRefValue(refGroup, required, mv, inputStreamVar, genClassInternalName, javaClass,
                        type, javaClassCodec);
                break;
            case DYNAMIC_REFERENCE:
                generateDecodeDynRefValue(mv, required, refGroup, javaClass);
                break;
            default:
                throw new RuntimeException("Unhandled case: " + type.getType());        }

        if (!required && javaClass.isPrimitive()) {
            // PENDING: null check and throw DecodeException: Cannot represent absent (null) value.
            unbox(mv, javaClass);
        } else if (required && !javaClass.isPrimitive()) {
            box(mv, javaClass);
        }
    }

    private void generateDecodeTimeValue(TypeDef.Time type, Class<?> javaClass, boolean required, MethodVisitor mv,
            LocalVariable nextVar) throws IllegalArgumentException {
        if (javaClass == long.class || javaClass == Long.class) {
            if (required) {
                mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readInt64", "(Lcom/cinnober/msgcodec/io/ByteSource;)J", false);
            } else {
                mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readInt64Null",
                        "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/lang/Long;", false);
            }
        } else if (javaClass == int.class || javaClass == Integer.class) {
            if (required) {
                mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readInt32", "(Lcom/cinnober/msgcodec/io/ByteSource;)I", false);
            } else {
                mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readInt32Null",
                        "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/lang/Integer;", false);
            }
        } else if (javaClass == Date.class) {

            int timeVar = nextVar.next(); nextVar.next(); // note: 2 variable slots
            Label endLabel = new Label();
            if (required) {
                mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readInt64", "(Lcom/cinnober/msgcodec/io/ByteSource;)J", false);
            } else {
                mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readInt64Null",
                        "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/lang/Long;", false);
                mv.visitInsn(DUP);
                Label nonNullLabel = new Label();
                mv.visitJumpInsn(IFNONNULL, nonNullLabel);
                // null
                mv.visitTypeInsn(CHECKCAST, "java/util/Date");
                mv.visitJumpInsn(GOTO, endLabel);
                // not null
                mv.visitLabel(nonNullLabel);
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);
            }
            
            mv.visitVarInsn(LSTORE, timeVar);
            mv.visitTypeInsn(NEW, "java/util/Date");
            mv.visitInsn(DUP);
            mv.visitVarInsn(LLOAD, timeVar);
            // handle differences in UNIT and EPOCH
            long epochOffset = DateUtil.getEpochOffset(type.getEpoch());
            long timeInMillis = DateUtil.getTimeInMillis(type.getUnit());
            // dateTime = wireTime * timeUnitInMillis + epochOffset;
            if (timeInMillis != 1) {
                mv.visitLdcInsn(timeInMillis);
                mv.visitInsn(LMUL);
            }
            if (epochOffset != 0) {
                mv.visitLdcInsn(epochOffset);
                mv.visitInsn(LADD);
            }

            mv.visitMethodInsn(INVOKESPECIAL, "java/util/Date", "<init>", "(J)V", false);

            if (!required) {
                // end
                mv.visitLabel(endLabel);
            }
        } else {
            throw new IllegalArgumentException("Illegal time javaClass: " + javaClass);
        }
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void generateDecodeEnumValue(boolean required, MethodVisitor mv, TypeDef type,
            LocalVariable nextVar, Class<?> javaClass, String debugValueLabel)
            throws IllegalArgumentException {

        Label endLabel = new Label();
        if (required) {
            mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readUInt32",
                    "(Lcom/cinnober/msgcodec/io/ByteSource;)I", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readUInt32Null",
                    "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/lang/Integer;", false);
            mv.visitInsn(DUP);
            Label nonNullLabel = new Label();
            mv.visitJumpInsn(IFNONNULL, nonNullLabel);
            mv.visitTypeInsn(CHECKCAST, Type.getInternalName(javaClass));
            mv.visitJumpInsn(GOTO, endLabel);

            // not null
            mv.visitLabel(nonNullLabel);
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
        }
        // switch
        TypeDef.Enum enumType = (TypeDef.Enum) type;
        List<Symbol> symbols = enumType.getSymbols();
        int numSymbols = symbols.size();
        int[] ids = new int[numSymbols];
        Label[] labels = new Label[numSymbols];
        for (int i=0; i<numSymbols; i++) {
            ids[i] = symbols.get(i).getId();
            labels[i] = new Label();
        }
        Label defaultLabel = new Label();
        int symbolIdVar = nextVar.next();
        mv.visitInsn(DUP);
        mv.visitVarInsn(ISTORE, symbolIdVar);
        EnumSymbols enumSymbols = null;
        if (javaClass.isEnum()) {
            enumSymbols = new EnumSymbols(enumType, javaClass);
        }
        mv.visitLookupSwitchInsn(defaultLabel, ids, labels);
        for (int i=0; i<numSymbols; i++) {
            boolean addGotoEnd = true;
            mv.visitLabel(labels[i]);
            if (javaClass.isEnum()) {
                Enum enumValue = enumSymbols.getEnum(ids[i]);
                if (enumValue != null) {
                    //mv.visitLdcInsn(Type.getType(javaClass));
                    mv.visitFieldInsn(GETSTATIC, Type.getInternalName(javaClass),
                            enumValue.name(),
                            Type.getDescriptor(javaClass));
                } else {
                    mv.visitLdcInsn(debugValueLabel);
                    mv.visitLdcInsn(ids[i]);
                    mv.visitLdcInsn(Type.getType(javaClass));
                    mv.visitMethodInsn(INVOKESTATIC, BASECLASS_INTERNALNAME, "unmappableEnumSymbolId",
                            "(Ljava/lang/String;ILjava/lang/Class;)Lcom/cinnober/msgcodec/DecodeException;", false);
                    mv.visitInsn(ATHROW);
                    addGotoEnd = false;
                }
            } else if (javaClass == int.class || javaClass == Integer.class) {
                mv.visitLdcInsn(ids[i]);
                if (!required) {
                    box(mv, Integer.class);
                }
            } else {
                throw new IllegalArgumentException("Illegal enum javaClass: " + javaClass);
            }
            if (addGotoEnd) {
                mv.visitJumpInsn(GOTO, endLabel);
            }
        }
        mv.visitLabel(defaultLabel);
        mv.visitLdcInsn(debugValueLabel);
        mv.visitVarInsn(ILOAD, symbolIdVar);
        mv.visitMethodInsn(INVOKESTATIC, BASECLASS_INTERNALNAME, "unknownEnumSymbol",
                "(Ljava/lang/String;I)Lcom/cinnober/msgcodec/DecodeException;", false);
        mv.visitInsn(ATHROW);

        // end
        mv.visitLabel(endLabel);
    }

    private void generateDecodeDynRefValue(MethodVisitor mv, boolean required, GroupDef refGroup,
            Class<?> javaClass) {
        mv.visitVarInsn(ALOAD, 0); // this
        mv.visitInsn(SWAP); // this and in
        if (required) {
            mv.visitMethodInsn(INVOKEVIRTUAL, BASECLASS_INTERNALNAME,
                    "readDynamicGroup", "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/lang/Object;", false);
        } else {
            mv.visitMethodInsn(INVOKEVIRTUAL, BASECLASS_INTERNALNAME,
                    "readDynamicGroupNull", "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/lang/Object;", false);
        }
        if (refGroup != null) {
            mv.visitTypeInsn(CHECKCAST, Type.getInternalName(javaClass));
        }
    }

    private void generateDecodeRefValue(GroupDef refGroup, boolean required, MethodVisitor mv, int inputStreamVar,
            String genClassInternalName,
            Class<?> javaClass, TypeDef type, boolean javaClassCodec) throws IllegalArgumentException {
        if (refGroup != null) {
            String groupDescriptor = javaClassCodec ? Type.getDescriptor(javaClass) : "Ljava/lang/Object;";
            if (required) {
                mv.visitInsn(POP); // input stream
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, inputStreamVar);
                mv.visitMethodInsn(INVOKEVIRTUAL, genClassInternalName, "readStaticGroup_" + refGroup.getName(),
                        "(Lcom/cinnober/msgcodec/io/ByteSource;)" + groupDescriptor,
                        false);
            } else {
                mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readBoolean", "(Lcom/cinnober/msgcodec/io/ByteSource;)Z", false);
                Label nonNullLabel = new Label();
                Label endLabel = new Label();
                mv.visitJumpInsn(IFNE, nonNullLabel); // not false, i.e. true
                // null
                mv.visitInsn(ACONST_NULL);
                mv.visitJumpInsn(GOTO, endLabel);

                // not null
                mv.visitLabel(nonNullLabel);
                mv.visitFrame(F_SAME, 0, null, 0, null);
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, inputStreamVar);
                mv.visitMethodInsn(INVOKEVIRTUAL, genClassInternalName, "readStaticGroup_" + refGroup.getName(),
                        "(Lcom/cinnober/msgcodec/io/ByteSource;)" + groupDescriptor,
                        false);

                mv.visitLabel(endLabel);
                // PENDING: mv.visitFrame
                mv.visitFrame(F_SAME, 0, null, 0, null);
            }
        } else {
            throw new IllegalArgumentException("Illegal reference: " + type);
        }
    }

    private void generateDecodeSequenceValue(
            Class<?> javaClass, LocalVariable nextVar, boolean required, MethodVisitor mv,
            Class<?> componentJavaClass, int inputStreamVar, TypeDef type, Schema schema,
            String genClassInternalName, String debugValueLabel, boolean javaClassCodec)
            throws IllegalArgumentException {
        if (!javaClass.isArray() && javaClass != List.class) {
            throw new IllegalArgumentException("Illegal sequence javaClass: " + javaClass);
        }

        int lengthVar = nextVar.next();
        int sequenceVar = nextVar.next();
        Label finalEndLabel = new Label();
        if (required) {
            mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readUInt32", "(Lcom/cinnober/msgcodec/io/ByteSource;)I", false);
            mv.visitVarInsn(ISTORE, lengthVar);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, BLINK_INPUT_INAME, "readUInt32Null",
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
        mv.visitVarInsn(ALOAD, inputStreamVar);

        // decode the element
        TypeDef.Sequence seqType = (TypeDef.Sequence) type;
        generateDecodeValue(mv, inputStreamVar, nextVar, true, seqType.getComponentType(), componentJavaClass, null,
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
    }
    
    // --- UTILITIES ---------------------------------------------------------------------------------------------------

    private static boolean isPublicFieldAccessor(Accessor<?,?> accessor) {
        return accessor.getClass() == FieldAccessor.class &&
                Modifier.isPublic(((FieldAccessor)accessor).getField().getModifiers());
    }

    public static boolean isPublicConstructorFactory(Factory<?> factory) {
        return factory.getClass() == ConstructorFactory.class &&
                Modifier.isPublic(((ConstructorFactory)factory).getConstructor().getModifiers());
    }

    private void generateArrayStore(MethodVisitor mv, Class<?> componentJavaClass) {
        if (componentJavaClass == byte.class || componentJavaClass == boolean.class) {
            mv.visitInsn(BASTORE);
        } else if (componentJavaClass == short.class) {
            mv.visitInsn(SASTORE);
        } else if (componentJavaClass == int.class) {
            mv.visitInsn(IASTORE);
        } else if (componentJavaClass == long.class) {
            mv.visitInsn(LASTORE);
        } else if (componentJavaClass == float.class) {
            mv.visitInsn(FASTORE);
        } else if (componentJavaClass == double.class) {
            mv.visitInsn(DASTORE);
        } else {
            mv.visitInsn(AASTORE);
        }
    }

    /**
     * Generate byte code for a new array.
     * The length is expected to be on the stack.
     * @param mv
     * @param componentJavaClass
     */
    private void generateNewArray(MethodVisitor mv, Class<?> componentJavaClass) {
        if (componentJavaClass == boolean.class) {
            mv.visitIntInsn(NEWARRAY, T_BOOLEAN);
        } else if (componentJavaClass == byte.class) {
            mv.visitIntInsn(NEWARRAY, T_BYTE);
        } else if (componentJavaClass == short.class) {
            mv.visitIntInsn(NEWARRAY, T_SHORT);
        } else if (componentJavaClass == int.class) {
            mv.visitIntInsn(NEWARRAY, T_INT);
        } else if (componentJavaClass == long.class) {
            mv.visitIntInsn(NEWARRAY, T_LONG);
        } else if (componentJavaClass == float.class) {
            mv.visitIntInsn(NEWARRAY, T_FLOAT);
        } else if (componentJavaClass == double.class) {
            mv.visitIntInsn(NEWARRAY, T_DOUBLE);
        } else {
            mv.visitTypeInsn(ANEWARRAY, Type.getInternalName(componentJavaClass));
        }
    }
    
    private void unbox(MethodVisitor mv, Class<?> javaClass) {
        if (javaClass == Byte.class || javaClass == byte.class) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B", false);
        } else if (javaClass == Short.class || javaClass == short.class) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S", false);
        } else if (javaClass == Integer.class || javaClass == int.class) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
        } else if (javaClass == Long.class || javaClass == long.class) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);
        } else if (javaClass == Float.class || javaClass == float.class) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false);
        } else if (javaClass == Double.class || javaClass == double.class) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D", false);
        } else if (javaClass == Boolean.class || javaClass == boolean.class) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
        }
    }
    private void box(MethodVisitor mv, Class<?> javaClass) {
        if (javaClass == Byte.class || javaClass == byte.class) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
        } else if (javaClass == Short.class || javaClass == short.class) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
        } else if (javaClass == Integer.class || javaClass == int.class) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        } else if (javaClass == Long.class || javaClass == long.class) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
        } else if (javaClass == Float.class || javaClass == float.class) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
        } else if (javaClass == Double.class || javaClass == double.class) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
        } else if (javaClass == Boolean.class || javaClass == boolean.class) {
            mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
        }
    }

    private Class<?> unbox(Class<?> javaClass) {
        if (javaClass == Byte.class) {
            return byte.class;
        } else if (javaClass == Short.class) {
            return short.class;
        } else if (javaClass == Integer.class) {
            return int.class;
        } else if (javaClass == Long.class) {
            return long.class;
        } else if (javaClass == Float.class) {
            return float.class;
        } else if (javaClass == Double.class) {
            return double.class;
        } else if (javaClass == Boolean.class) {
            return boolean.class;
        }
        return javaClass;
    }
    private Class<?> box(Class<?> javaClass) {
        if (javaClass == byte.class) {
            return Byte.class;
        } else if (javaClass == short.class) {
            return Short.class;
        } else if (javaClass == int.class) {
            return Integer.class;
        } else if (javaClass == long.class) {
            return Long.class;
        } else if (javaClass == float.class) {
            return Float.class;
        } else if (javaClass == double.class) {
            return Double.class;
        } else if (javaClass == boolean.class) {
            return Boolean.class;
        }
        return javaClass;
    }

    private static class ObjectHashCodeSwitchCase<T> {
        final int hashCode;
        final Label label = new Label();
        final List<ObjectSwitchCase<T>> cases = new ArrayList<>();

        ObjectHashCodeSwitchCase(int hashCode) {
            this.hashCode = hashCode;
        }
        void add(T object) {
            cases.add(new ObjectSwitchCase<>(object, new Label()));
        }
    }
    private static class ObjectSwitchCase<T> {
        final T object;
        final Label label;

        ObjectSwitchCase(T object, Label label) {
            this.object = object;
            this.label = label;
        }

    }

    private static class LocalVariable {
        private int nextVariable;
        LocalVariable(int nextVariable) {
            this.nextVariable = nextVariable;
        }

        int next() {
            return nextVariable++;
        }
        int get() {
            return nextVariable;
        }
    }

}
