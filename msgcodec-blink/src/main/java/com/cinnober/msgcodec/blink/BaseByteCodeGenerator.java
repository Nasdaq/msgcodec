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
import com.cinnober.msgcodec.CreateAccessor;
import com.cinnober.msgcodec.Factory;
import com.cinnober.msgcodec.FieldAccessor;
import com.cinnober.msgcodec.FieldDef;
import com.cinnober.msgcodec.GroupDef;
import com.cinnober.msgcodec.JavaClassGroupTypeAccessor;
import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.SchemaBinding;
import com.cinnober.msgcodec.SymbolMapping;
import com.cinnober.msgcodec.TypeDef;
import com.cinnober.msgcodec.io.ByteArrays;
import com.cinnober.msgcodec.io.ByteSink;
import com.cinnober.msgcodec.io.ByteSource;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import static org.objectweb.asm.Opcodes.AALOAD;
import static org.objectweb.asm.Opcodes.AASTORE;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PRIVATE;
import static org.objectweb.asm.Opcodes.ACC_PROTECTED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACONST_NULL;
import static org.objectweb.asm.Opcodes.ALOAD;
import static org.objectweb.asm.Opcodes.ANEWARRAY;
import static org.objectweb.asm.Opcodes.ARETURN;
import static org.objectweb.asm.Opcodes.ARRAYLENGTH;
import static org.objectweb.asm.Opcodes.ASTORE;
import static org.objectweb.asm.Opcodes.ATHROW;
import static org.objectweb.asm.Opcodes.BALOAD;
import static org.objectweb.asm.Opcodes.BASTORE;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DALOAD;
import static org.objectweb.asm.Opcodes.DASTORE;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.FALOAD;
import static org.objectweb.asm.Opcodes.FASTORE;
import static org.objectweb.asm.Opcodes.F_SAME;
import static org.objectweb.asm.Opcodes.GETFIELD;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.IALOAD;
import static org.objectweb.asm.Opcodes.IASTORE;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.ICONST_1;
import static org.objectweb.asm.Opcodes.IFEQ;
import static org.objectweb.asm.Opcodes.IFNE;
import static org.objectweb.asm.Opcodes.IFNONNULL;
import static org.objectweb.asm.Opcodes.IFNULL;
import static org.objectweb.asm.Opcodes.IF_ACMPEQ;
import static org.objectweb.asm.Opcodes.IF_ICMPGE;
import static org.objectweb.asm.Opcodes.ILOAD;
import static org.objectweb.asm.Opcodes.INVOKEINTERFACE;
import static org.objectweb.asm.Opcodes.INVOKESPECIAL;
import static org.objectweb.asm.Opcodes.INVOKESTATIC;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.ISTORE;
import static org.objectweb.asm.Opcodes.LADD;
import static org.objectweb.asm.Opcodes.LALOAD;
import static org.objectweb.asm.Opcodes.LASTORE;
import static org.objectweb.asm.Opcodes.LDIV;
import static org.objectweb.asm.Opcodes.LLOAD;
import static org.objectweb.asm.Opcodes.LMUL;
import static org.objectweb.asm.Opcodes.LSTORE;
import static org.objectweb.asm.Opcodes.LSUB;
import static org.objectweb.asm.Opcodes.NEW;
import static org.objectweb.asm.Opcodes.NEWARRAY;
import static org.objectweb.asm.Opcodes.POP;
import static org.objectweb.asm.Opcodes.PUTFIELD;
import static org.objectweb.asm.Opcodes.RETURN;
import static org.objectweb.asm.Opcodes.SALOAD;
import static org.objectweb.asm.Opcodes.SASTORE;
import static org.objectweb.asm.Opcodes.SWAP;
import static org.objectweb.asm.Opcodes.T_BOOLEAN;
import static org.objectweb.asm.Opcodes.T_BYTE;
import static org.objectweb.asm.Opcodes.T_DOUBLE;
import static org.objectweb.asm.Opcodes.T_FLOAT;
import static org.objectweb.asm.Opcodes.T_INT;
import static org.objectweb.asm.Opcodes.T_LONG;
import static org.objectweb.asm.Opcodes.T_SHORT;
import static org.objectweb.asm.Opcodes.V1_7;

/**
 *
 * @author mikael.brannstrom
 */
class BaseByteCodeGenerator {
    protected static final Logger log = Logger.getLogger(BaseByteCodeGenerator.class.getName());
    

    @SuppressWarnings("unused")
    private static final String BYTE_SINK_INAME = Type.getInternalName(ByteSink.class);
    @SuppressWarnings("unused")
    private static final String BYTE_SOURCE_INAME = Type.getInternalName(ByteSource.class);
    private static final String GROUPDEF_INAME = Type.getInternalName(GroupDef.class);
    private static final String SCHEMA_INAME = Type.getInternalName(Schema.class);
    @SuppressWarnings("unused")
    private static final String SCHEMA_BINDING_INAME = Type.getInternalName(SchemaBinding.class);


    protected final String GENERATED_CLASS_INAME = "com/cinnober/msgcodec/blink/GeneratedBlinkCodec";
    protected final String GENERATED_CLASS_NAME = "com.cinnober.msgcodec.blink.GeneratedBlinkCodec";
    protected final String baseclassIName; // = Type.getInternalName(GeneratedCodec.class);
    protected final String blinkCodecIName;
    protected final String blinkInputIName; // = Type.getInternalName(BlinkInput.class);
    protected final String blinkOutputIName; // = Type.getInternalName(BlinkOutput.class);

    protected BaseByteCodeGenerator(
            Class<? extends GeneratedCodec> generatedSuperClass,
            Class<?> blinkCodecClass,
            Class<?> blinkInputClass,
            Class<?> blinkOutputClass) {
        baseclassIName = Type.getInternalName(generatedSuperClass);
        blinkCodecIName = Type.getInternalName(blinkCodecClass);
        blinkInputIName = Type.getInternalName(blinkInputClass);
        blinkOutputIName = Type.getInternalName(blinkOutputClass);
    }


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
            log.log(Level.FINE, "Generated codec class " + GENERATED_CLASS_INAME + suffix + " for schema UID: " + schema.getUID());
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
    protected void generateCodecJ(ClassVisitor cv, Schema schema, int suffix) {
        generateCodec(cv, schema, suffix, true);
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
    protected void generateCodecG(ClassVisitor cv, Schema schema, int suffix) {
        generateCodec(cv, schema, suffix, false);
    }

    private void generateCodec(ClassVisitor cv, Schema schema, int suffix, boolean javaClassCodec) {
        final String genClassInternalName = GENERATED_CLASS_INAME + suffix;
        cv.visit(V1_7, ACC_PUBLIC + ACC_FINAL, genClassInternalName, null, baseclassIName, null);

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

    protected void generateConstructorAndFields(Schema schema, ClassVisitor cv, String genClassInternalName, boolean javaClassCodec) {
        MethodVisitor ctormv;
        ctormv = cv.visitMethod(ACC_PUBLIC, "<init>",
                "(L" + blinkCodecIName + ";Lcom/cinnober/msgcodec/Schema;)V", null, null);
        int nextCtorVar = 3;
        ctormv.visitCode();
        ctormv.visitVarInsn(ALOAD, 0);
        ctormv.visitVarInsn(ALOAD, 1);
        ctormv.visitMethodInsn(INVOKESPECIAL, baseclassIName, "<init>",
                "(L" + blinkCodecIName + ";)V", false);

        // schema field
        FieldVisitor fv = cv.visitField(ACC_PRIVATE + ACC_FINAL, "schema",
                "Lcom/cinnober/msgcodec/Schema;", null, null);
        fv.visitEnd();
        ctormv.visitVarInsn(ALOAD, 0); // this
        ctormv.visitVarInsn(ALOAD, 2); // schema
        ctormv.visitFieldInsn(PUTFIELD, genClassInternalName, "schema",
                "Lcom/cinnober/msgcodec/Schema;");

        if (!javaClassCodec) {
            // store the group type accessor

            // field
            fv = cv.visitField(ACC_PRIVATE + ACC_FINAL, "groupTypeAccessor",
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
                fv = cv.visitField(ACC_PRIVATE + ACC_FINAL, "groupType_" + group.getName(),
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
                fv = cv.visitField(ACC_PRIVATE + ACC_FINAL,
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
                SymbolMapping<?> symbolMapping = field.getBinding().getSymbolMapping();
                TypeDef type = schema.resolveToType(field.getType(), true);
                TypeDef componentType = null;

                if (type.getType() == TypeDef.Type.SEQUENCE) {
                    componentType = schema.resolveToType(((TypeDef.Sequence) type).getComponentType(), false);
                }

                if (isPublicFieldAccessor(accessor)) {
                    // no accessor needed
                } else {
                    // field
                    fv = cv.visitField(ACC_PRIVATE + ACC_FINAL,
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
                
                if (accessor.getClass() == CreateAccessor.class) {
                    // No symbol map needed
                } else if ((type != null && type.getType() == TypeDef.Type.ENUM) 
                        || (componentType != null && componentType.getType() == TypeDef.Type.ENUM)) {
                    // If there is an enum and we need the symbol map
                    Objects.requireNonNull(symbolMapping);
                    
                    // Create field
                    String symbolMappingFieldName = "symbolMapping_" + group.getName() + "_" + field.getName();
                    
                    fv = cv.visitField(ACC_PRIVATE + ACC_FINAL,
                            symbolMappingFieldName, "Lcom/cinnober/msgcodec/SymbolMapping;", null, null);
                    fv.visitEnd();

                    // Init field in the constructor
                    ctormv.visitVarInsn(ALOAD, 0); // this
                    ctormv.visitVarInsn(ALOAD, 2); // schema
                    ctormv.visitLdcInsn(group.getName());
                    ctormv.visitMethodInsn(INVOKEVIRTUAL, "com/cinnober/msgcodec/Schema", "getGroup",
                            "(Ljava/lang/String;)Lcom/cinnober/msgcodec/GroupDef;", false);
                    ctormv.visitLdcInsn(field.getName());
                    ctormv.visitMethodInsn(INVOKEVIRTUAL, "com/cinnober/msgcodec/GroupDef", "getField",
                            "(Ljava/lang/String;)Lcom/cinnober/msgcodec/FieldDef;", false);
                    ctormv.visitMethodInsn(INVOKEVIRTUAL, "com/cinnober/msgcodec/FieldDef", "getBinding",
                            "()Lcom/cinnober/msgcodec/FieldBinding;", false);
                    ctormv.visitMethodInsn(INVOKEVIRTUAL, "com/cinnober/msgcodec/FieldBinding", "getSymbolMapping",
                            "()Lcom/cinnober/msgcodec/SymbolMapping;", false);
                    
                    ctormv.visitFieldInsn(PUTFIELD, genClassInternalName,
                            symbolMappingFieldName, "Lcom/cinnober/msgcodec/SymbolMapping;");
                }
            }
        }

        ctormv.visitInsn(RETURN);
        ctormv.visitMaxs(3, nextCtorVar);
        ctormv.visitEnd();
    }
    
    // --- GENERATE WRITE ----------------------------------------------------------------------------------------------

    protected void generateWriteStaticGroup(Schema schema, ClassVisitor cv,
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
        Map<Integer,Label> labelsByGroupId = new TreeMap<>();
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
                GroupDef group = schema.getGroup(classCase.object);
                int groupId = schema.getGroup(classCase.object).getId();
                if (groupId != -1) {
                    labelsByGroupId.put(groupId, classCase.label);
                }
                
                mv.visitVarInsn(ALOAD, groupTypeVar);
                if (javaClassCodec) {
                    mv.visitLdcInsn(getJavaType(classCase.object));
                    mv.visitJumpInsn(IF_ACMPEQ, classCase.label);
                } else {
                    mv.visitVarInsn(ALOAD, 0);
                    mv.visitFieldInsn(GETFIELD, genClassInternalName, "groupType_" + group.getName(),
                            "Ljava/lang/Object;");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Object",
                            "equals", "(Ljava/lang/Object;)Z", false);
                    mv.visitJumpInsn(IFNE, classCase.label); // IFNE = if not false
                }
            }
        }
        // Default case for class hashcode switch, do lookup using schema.getGroup(Object)
        {
            Label unknownGroupIdLabel = new Label();
            mv.visitLabel(unknownHashLabel);
            mv.visitFrame(F_SAME, 0, null, 0, null);
            Label[] groupIdLabels = new Label[labelsByGroupId.size()];
            int[] groupIds = new int[groupIdLabels.length];
            
            {
                int i = 0;
                for (Entry<Integer, Label> entry : labelsByGroupId.entrySet()) {
                    groupIds[i] = entry.getKey();
                    groupIdLabels[i] = new Label();
                    i++;
                }
            }
            
            mv.visitVarInsn(ALOAD, 0);
            mv.visitFieldInsn(GETFIELD, genClassInternalName, "schema",
                    "Lcom/cinnober/msgcodec/Schema;");
            
            mv.visitVarInsn(ALOAD, groupTypeVar);
            mv.visitMethodInsn(INVOKEVIRTUAL, SCHEMA_INAME, "getGroup",
                    "(Ljava/lang/Object;)Lcom/cinnober/msgcodec/GroupDef;", false);

            // check for null result from getGroup
            Label noGroupDefLabel = new Label();
            mv.visitInsn(DUP);
            mv.visitJumpInsn(IFNULL, noGroupDefLabel);

            mv.visitMethodInsn(INVOKEVIRTUAL, GROUPDEF_INAME, "getId",
                    "()I", false);
            
            // Switch on the group id
            mv.visitLookupSwitchInsn(unknownHashLabel, groupIds, groupIdLabels);
            
            // Cases for the group ids
            for (int i = 0; i < groupIds.length; i++) {
                mv.visitLabel(groupIdLabels[i]);
                mv.visitFrame(F_SAME, 0, null, 0, null);
                mv.visitJumpInsn(GOTO, labelsByGroupId.get(groupIds[i]));
            }

            mv.visitLabel(noGroupDefLabel);
            mv.visitFrame(F_SAME, 0, null, 0, null);
            mv.visitInsn(POP);


            // Throw exception if there is no match on class or group id
            mv.visitLabel(unknownGroupIdLabel);
            mv.visitFrame(F_SAME, 0, null, 0, null);
            mv.visitVarInsn(ALOAD, groupTypeVar);
            mv.visitMethodInsn(INVOKESTATIC, baseclassIName, "unknownGroupType",
                    "(Ljava/lang/Object;)Ljava/lang/IllegalArgumentException;", false);
            mv.visitInsn(ATHROW);
        }

        // Generate the labeled calls to group writer methods
        for (ObjectHashCodeSwitchCase<Object> hashCase : casesByHashCode.values()) {
            for (ObjectSwitchCase<Object> classCase : hashCase.cases) {
                Object groupType = classCase.object;
                GroupDef group = schema.getGroup(groupType);
                String groupDescriptor = getTypeDescriptor(groupType, javaClassCodec);
                mv.visitLabel(classCase.label);
                mv.visitFrame(F_SAME, 0, null, 0, null);
                mv.visitVarInsn(ALOAD, 0); // this
                mv.visitVarInsn(ALOAD, 1); // out
                mv.visitVarInsn(ALOAD, 2); // obj
                if (javaClassCodec) {
                    mv.visitTypeInsn(CHECKCAST, getTypeInternalName(groupType, javaClassCodec));
                }
                mv.visitMethodInsn(INVOKEVIRTUAL, genClassInternalName, "writeStaticGroupWithId_" + group.getName(),
                        "(Lcom/cinnober/msgcodec/io/ByteSink;" + groupDescriptor + ")V", false);
                mv.visitInsn(RETURN);
            }
        }

        mv.visitMaxs(4, nextVar);
        mv.visitEnd();
    }
    
    protected void generateWriteStaticGroupForTypeWithId(Schema schema, ClassVisitor cv,
            String genClassInternalName, boolean javaClassCodec) {
        for (GroupDef group : schema.getGroups()) {
            Object groupType = group.getGroupType();
            String groupDescriptor = getTypeDescriptor(groupType, javaClassCodec);
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
                mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeUInt32",
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
            mv.visitMaxs(3, nextWriteidVar);
            mv.visitEnd();
        }
    }
    
    protected void generateWriteStaticGroupForType(Schema schema, ClassVisitor cv, String genClassInternalName,
            boolean javaClassCodec) {
        for (GroupDef group : schema.getGroups()) {
            Object groupType = group.getGroupType();
            String groupDescriptor = getTypeDescriptor(groupType, javaClassCodec);
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
                String superGroupDescriptor = getTypeDescriptor(superGroupType, javaClassCodec);
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
                
                if(accessor.getClass() == CreateAccessor.class) {
                    if(field.getType().getType() == TypeDef.Type.REFERENCE) {
                        if(field.isRequired()) {
                            writemv.visitInsn(ICONST_0);
                            generateEncodeInt32Value(true, writemv);
                        }
                        else {
                            writemv.visitInsn(ACONST_NULL);
                            generateEncodeInt32Value(false, writemv);
                        }
                        continue;
                    }
                }
                
                if (isPublicFieldAccessor(accessor)) {                   
                    Field f = ((FieldAccessor)accessor).getField();
                    writemv.visitVarInsn(ALOAD, 2);
                    writemv.visitFieldInsn(GETFIELD, Type.getInternalName(f.getDeclaringClass()), f.getName(),
                            Type.getDescriptor(f.getType()));
                    if (!f.getType().equals(field.getJavaClass())) {
                        // this can happen when the field is a generic type variable in a super-class.
                        writemv.visitTypeInsn(CHECKCAST, Type.getInternalName(javaClass));
                    }
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
                        group.getName() + "_" + field.getName(), 
                        group.getName() + "." + field.getName(), javaClassCodec);
            }

            // end write
            writemv.visitInsn(RETURN);
            writemv.visitMaxs(4, nextWriteVar.get());
            writemv.visitEnd();
        }
    }

    // --- GENERATE READ -----------------------------------------------------------------------------------------------

    protected void generateReadStaticGroup(Schema schema, ClassVisitor cv, String genClassInternalName,
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
            String groupDescriptor = getTypeDescriptor(groupType, javaClassCodec);

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
        mv.visitMethodInsn(INVOKESTATIC, baseclassIName, "unknownGroupId",
                "(I)Lcom/cinnober/msgcodec/DecodeException;", false);
        mv.visitInsn(ATHROW);
        mv.visitMaxs(2, nextVar);
        mv.visitEnd();
    }
    
    protected void generateReadStaticGroupForTypeAndCreate(Schema schema, ClassVisitor cv,
            String genClassInternalName, boolean javaClassCodec) {
        for (GroupDef group : schema.getGroups()) {
            Object groupType = group.getGroupType();
            String groupDescriptor = getTypeDescriptor(groupType, javaClassCodec);
            String groupInternalName = getTypeInternalName(groupType, javaClassCodec);
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
            readmv.visitMaxs(4, nextReadVar);
            readmv.visitEnd();
        }
    }
    protected void generateReadStaticGroupForType(final Schema schema, ClassVisitor cv,
            final String genClassInternalName, final boolean javaClassCodec) {
        for (final GroupDef group : schema.getGroups()) {
            Object groupType = group.getGroupType();
            String groupDescriptor = getTypeDescriptor(groupType, javaClassCodec);
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
                String superGroupDescriptor = getTypeDescriptor(superGroupType, javaClassCodec);
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

                Runnable readValue = () -> {
                    Label tryStart = new Label();
                    Label tryEnd = new Label();
                    Label tryCatch = new Label();
                    Label tryAfter = new Label();
                    mv.visitTryCatchBlock(tryStart, tryEnd, tryCatch, "java/lang/Exception");
                    
                    mv.visitLabel(tryStart);
                    mv.visitVarInsn(ALOAD, 1); // input stream
                    generateDecodeValue(mv, 1, nextVar, field.isRequired(), field.getType(), javaClass, 
                            field.getComponentJavaClass(), schema, genClassInternalName,
                            group.getName() + "_" + field.getName(),
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
                } else if (accessor.getClass() == CreateAccessor.class) {
                    mv.visitVarInsn(ALOAD, 2); // instance

                    Label tryStart = new Label();
                    Label tryEnd = new Label();
                    Label tryCatch = new Label();
                    Label tryAfter = new Label();
                    mv.visitTryCatchBlock(tryStart, tryEnd, tryCatch, "java/lang/Exception");
                    
                    mv.visitLabel(tryStart);
                    mv.visitVarInsn(ALOAD, 1); // input stream
                    
                    generateDecodeDummy(mv, 1, nextVar, field.isRequired(), field.getType(), javaClass,
                            field.getComponentJavaClass(), schema, genClassInternalName,
                            group.getName() + "." + field.getName(), javaClassCodec);
                    
                    mv.visitInsn(POP);
                    
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
            mv.visitMaxs(6, nextVar.get());
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
    protected void generateEncodeValue(
            MethodVisitor mv,
            int outputStreamVar,
            LocalVariable nextVar,
            boolean required,
            TypeDef type,
            Class<?> javaClass,
            Class<?> componentJavaClass,
            Schema schema,
            String genClassInternalName,
            String fieldIdentifier,
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
            mv.visitMethodInsn(INVOKESTATIC, baseclassIName, "missingRequiredValue",
                    "(Ljava/lang/String;)Ljava/lang/IllegalArgumentException;", false);
            mv.visitInsn(ATHROW);
            mv.visitLabel(notNullLabel);
            unbox(mv, javaClass);
        }
        switch (type.getType()) {
            case INT8:
                generateEncodeInt8Value(required, mv);
                break;
            case UINT8:
                generateEncodeUInt8Value(required, mv);
                break;
            case INT16:
                generateEncodeInt16Value(required, mv);
                break;
            case UINT16:
                generateEncodeUInt16Value(required, mv);
                break;
            case INT32:
                generateEncodeInt32Value(required, mv);
                break;
            case UINT32:
                generateEncodeUInt32Value(required, mv);
                break;
            case INT64:
                generateEncodeInt64Value(required, mv);
                break;
            case UINT64:
                generateEncodeUInt64Value(required, mv);
                break;
            case FLOAT32:
                generateEncodeFloat32Value(required, mv);
                break;
            case FLOAT64:
                generateEncodeFloat64Value(required, mv);
                break;
            case BIGINT:
                generateEncodeBigIntValue(required, mv);
                break;
            case DECIMAL:
                generateEncodeDecimalValue(required, mv);
                break;
            case BIGDECIMAL:
                generateEncodeBigDecimalValue(required, mv);
                break;
            case STRING:
                generateEncodeStringValue((TypeDef.StringUnicode) type, required, mv);
                break;
            case BINARY:
                generateEncodeBinaryValue((TypeDef.Binary) type, required, mv);
                break;
            case BOOLEAN:
                generateEncodeBooleanValue(required, mv);
                break;
            case ENUM:
                generateEncodeEnumValue((TypeDef.Enum) type, genClassInternalName, fieldIdentifier, nextVar, 
                        javaClass, required, mv, debugValueLabel);
                break;
            case TIME:
                generateEncodeTimeValue((TypeDef.Time) type, javaClass, required, mv);
                break;
            case SEQUENCE:
                generateEncodeSequenceValue(javaClass, nextVar, mv, required, outputStreamVar,
                        componentJavaClass, type, schema, genClassInternalName, fieldIdentifier, 
                        debugValueLabel, javaClassCodec);
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

    /**
     * Generate value encoding using the blink output.
     *
     * <p>Defaults to <code>writeBoolean[Null].</code>
     *
     * @param required true if the field is required, otherwise false.
     * @param mv the method visitor, not null.
     * @see #generateEncodeValue
     */
    protected void generateEncodeBooleanValue(boolean required, MethodVisitor mv) {
        if (required) {
            mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeBoolean",
                    "(Lcom/cinnober/msgcodec/io/ByteSink;Z)V", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeBooleanNull",
                    "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/Boolean;)V", false);
        }
    }

    /**
     * Generate value encoding using the blink output.
     *
     * <p>Defaults to <code>writeBinary[Null].</code>
     *
     * @param required true if the field is required, otherwise false.
     * @param mv the method visitor, not null.
     * @see #generateEncodeValue
     */
    protected void generateEncodeBinaryValue(TypeDef.Binary type, boolean required, MethodVisitor mv) {
        if (required) {
            mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeBinary",
                    "(Lcom/cinnober/msgcodec/io/ByteSink;[B)V", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeBinaryNull",
                    "(Lcom/cinnober/msgcodec/io/ByteSink;[B)V", false);
        }
    }

    /**
     * Generate value encoding using the blink output.
     *
     * <p>Defaults to <code>writeStringUTF8[Null].</code>
     *
     * @param required true if the field is required, otherwise false.
     * @param mv the method visitor, not null.
     * @see #generateEncodeValue
     */
    protected void generateEncodeStringValue(TypeDef.StringUnicode type, boolean required, MethodVisitor mv) {
        if (required) {
            mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeStringUTF8",
                    "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/String;)V", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeStringUTF8Null",
                    "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/String;)V", false);
        }
    }

    /**
     * Generate value encoding using the blink output.
     *
     * <p>Defaults to <code>writeBigDecimal[Null].</code>
     *
     * @param required true if the field is required, otherwise false.
     * @param mv the method visitor, not null.
     * @see #generateEncodeValue
     */
    protected void generateEncodeBigDecimalValue(boolean required, MethodVisitor mv) {
        if (required) {
            mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeBigDecimal",
                    "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/math/BigDecimal;)V", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeBigDecimalNull",
                    "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/math/BigDecimal;)V", false);
        }
    }

    /**
     * Generate value encoding using the blink output.
     *
     * <p>Defaults to <code>writeDecimal[Null].</code>
     *
     * @param required true if the field is required, otherwise false.
     * @param mv the method visitor, not null.
     * @see #generateEncodeValue
     */
    protected void generateEncodeDecimalValue(boolean required, MethodVisitor mv) {
        if (required) {
            mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeDecimal",
                    "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/math/BigDecimal;)V", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeDecimalNull",
                    "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/math/BigDecimal;)V", false);
        }
    }

    /**
     * Generate value encoding using the blink output.
     *
     * <p>Defaults to <code>writeBigInt[Null].</code>
     *
     * @param required true if the field is required, otherwise false.
     * @param mv the method visitor, not null.
     * @see #generateEncodeValue
     */
    protected void generateEncodeBigIntValue(boolean required, MethodVisitor mv) {
        if (required) {
            mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeBigInt",
                    "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/math/BigInteger;)V", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeBigIntNull",
                    "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/math/BigInteger;)V", false);
        }
    }

    /**
     * Generate value encoding using the blink output.
     *
     * <p>Defaults to <code>writeFloat64[Null].</code>
     *
     * @param required true if the field is required, otherwise false.
     * @param mv the method visitor, not null.
     * @see #generateEncodeValue
     */
    protected void generateEncodeFloat64Value(boolean required, MethodVisitor mv) {
        if (required) {
            mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeFloat64",
                    "(Lcom/cinnober/msgcodec/io/ByteSink;D)V", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeFloat64Null",
                    "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/Double;)V", false);
        }
    }

    /**
     * Generate value encoding using the blink output.
     *
     * <p>Defaults to <code>writeFloat32[Null].</code>
     *
     * @param required true if the field is required, otherwise false.
     * @param mv the method visitor, not null.
     * @see #generateEncodeValue
     */
    protected void generateEncodeFloat32Value(boolean required, MethodVisitor mv) {
        if (required) {
            mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeFloat32",
                    "(Lcom/cinnober/msgcodec/io/ByteSink;F)V", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeFloat32Null",
                    "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/Float;)V", false);
        }
    }

    /**
     * Generate value encoding using the blink output.
     *
     * <p>Defaults to <code>writeUInt64[Null].</code>
     *
     * @param required true if the field is required, otherwise false.
     * @param mv the method visitor, not null.
     * @see #generateEncodeValue
     */
    protected void generateEncodeUInt64Value(boolean required, MethodVisitor mv) {
        if (required) {
            mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeUInt64",
                    "(Lcom/cinnober/msgcodec/io/ByteSink;J)V", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeUInt64Null",
                    "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/Long;)V", false);
        }
    }

    /**
     * Generate value encoding using the blink output.
     *
     * <p>Defaults to <code>writeInt64[Null].</code>
     *
     * @param required true if the field is required, otherwise false.
     * @param mv the method visitor, not null.
     * @see #generateEncodeValue
     */
    protected void generateEncodeInt64Value(boolean required, MethodVisitor mv) {
        if (required) {
            mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeInt64",
                    "(Lcom/cinnober/msgcodec/io/ByteSink;J)V", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeInt64Null",
                    "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/Long;)V", false);
        }
    }

    /**
     * Generate value encoding using the blink output.
     *
     * <p>Defaults to <code>writeUInt32[Null].</code>
     *
     * @param required true if the field is required, otherwise false.
     * @param mv the method visitor, not null.
     * @see #generateEncodeValue
     */
    protected void generateEncodeUInt32Value(boolean required, MethodVisitor mv) {
        if (required) {
            mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeUInt32",
                    "(Lcom/cinnober/msgcodec/io/ByteSink;I)V", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeUInt32Null",
                    "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/Integer;)V", false);
        }
    }

    /**
     * Generate value encoding using the blink output.
     *
     * <p>Defaults to <code>writeInt32[Null].</code>
     *
     * @param required true if the field is required, otherwise false.
     * @param mv the method visitor, not null.
     * @see #generateEncodeValue
     */
    protected void generateEncodeInt32Value(boolean required, MethodVisitor mv) {
        if (required) {
            mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeInt32",
                    "(Lcom/cinnober/msgcodec/io/ByteSink;I)V", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeInt32Null",
                    "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/Integer;)V", false);
        }
    }

    /**                                                    en
     * Generate value encoding using the blink output.
     *
     * <p>Defaults to <code>writeUInt16Null].</code>
     *
     * @param required true if the field is required, otherwise false.
     * @param mv the method visitor, not null.
     * @see #generateEncodeValue
     */
    protected void generateEncodeUInt16Value(boolean required, MethodVisitor mv) {
        if (required) {
            mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeUInt16",
                    "(Lcom/cinnober/msgcodec/io/ByteSink;S)V", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeUInt16Null",
                    "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/Short;)V", false);
        }
    }

    /**
     * Generate value encoding using the blink output.
     *
     * <p>Defaults to <code>writeInt16[Null].</code>
     *
     * @param required true if the field is required, otherwise false.
     * @param mv the method visitor, not null.
     * @see #generateEncodeValue
     */
    protected void generateEncodeInt16Value(boolean required, MethodVisitor mv) {
        if (required) {
            mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeInt16",
                    "(Lcom/cinnober/msgcodec/io/ByteSink;S)V", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeInt16Null",
                    "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/Short;)V", false);
        }
    }

    /**
     * Generate value encoding using the blink output.
     *
     * <p>Defaults to <code>writeUInt8[Null].</code>
     *
     * @param required true if the field is required, otherwise false.
     * @param mv the method visitor, not null.
     * @see #generateEncodeValue
     */
    protected void generateEncodeUInt8Value(boolean required, MethodVisitor mv) {
        if (required) {
            mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeUInt8",
                    "(Lcom/cinnober/msgcodec/io/ByteSink;B)V", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeUInt8Null",
                    "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/Byte;)V", false);
        }
    }

    /**
     * Generate value encoding using the blink output.
     *
     * <p>Defaults to <code>writeInt8[Null].</code>
     *
     * @param required true if the field is required, otherwise false.
     * @param mv the method visitor, not null.
     * @see #generateEncodeValue
     */
    protected void generateEncodeInt8Value(boolean required, MethodVisitor mv) {
        if (required) {
            mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeInt8",
                    "(Lcom/cinnober/msgcodec/io/ByteSink;B)V", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeInt8Null",
                    "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/Byte;)V", false);
        }
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
    protected void generateEncodeDynRefValue(LocalVariable nextVar, MethodVisitor mv, boolean required) {
        // PENDING: validate that the instance class is a subclass of refGroup (unless null)
        int instanceVar = nextVar.next();
        mv.visitVarInsn(ASTORE, instanceVar);
        mv.visitVarInsn(ALOAD, 0); // this
        mv.visitInsn(SWAP); // this and out
        mv.visitVarInsn(ALOAD, instanceVar);
        if (required) {
            mv.visitMethodInsn(INVOKEVIRTUAL, baseclassIName,
                    "writeDynamicGroup", "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/Object;)V", false);
        } else {
            mv.visitMethodInsn(INVOKEVIRTUAL, baseclassIName,
                    "writeDynamicGroupNull", "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/Object;)V", false);
        }
    }

    protected void generateEncodeRefValue(GroupDef refGroup, boolean required, LocalVariable nextVar, MethodVisitor mv,
            String genClassInternalName, int outputStreamVar, TypeDef.Reference type,
            boolean javaClassCodec) throws IllegalArgumentException {
        if (refGroup != null) {
            Object refGroupType = refGroup.getGroupType();
            String refGroupDescriptor = getTypeDescriptor(refGroupType, javaClassCodec);
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
                mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writePresenceByte", "(Lcom/cinnober/msgcodec/io/ByteSink;Z)V", false);
                mv.visitJumpInsn(GOTO, endLabel);

                // not null
                mv.visitLabel(nonNullLabel);
                // PENDING: mv.visitFrame?
                mv.visitVarInsn(ASTORE, instanceVar);
                mv.visitInsn(ICONST_1); // true
                mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writePresenceByte", "(Lcom/cinnober/msgcodec/io/ByteSink;Z)V", false);
                mv.visitVarInsn(ALOAD, 0); // this
                mv.visitVarInsn(ALOAD, outputStreamVar);
                mv.visitVarInsn(ALOAD, instanceVar);
                mv.visitMethodInsn(INVOKEVIRTUAL, genClassInternalName, "writeStaticGroup_" + refGroup.getName(),
                        "(Lcom/cinnober/msgcodec/io/ByteSink;" + refGroupDescriptor + ")V", false);
                mv.visitLabel(endLabel);
                // PENDING: mv.visitFrame?
            }
        } else {
            throw new IllegalArgumentException("Illegal reference: " + type);
        }
    }

    protected void generateEncodeSequenceValue(
            Class<?> javaClass, LocalVariable nextVar, MethodVisitor mv, boolean required,
            int outputStreamVar,
            Class<?> componentJavaClass, TypeDef type, Schema schema, String genClassInternalName, 
            String fieldIdentifier, String debugValueLabel, boolean javaClassCodec) throws IllegalArgumentException {

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
                // PENDING: mv.visitFrame?
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
            // PENDING: mv.visitFrame?
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
                    null, schema, genClassInternalName, fieldIdentifier, debugValueLabel + ".component", 
                    javaClassCodec);

            mv.visitIincInsn(loopVar, 1);
            mv.visitJumpInsn(GOTO, loopLabel);
            mv.visitLabel(endLabel);
            // PENDING: mv.visitFrame?

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
                // PENDING: mv.visitFrame?
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
            // PENDING: mv.visitFrame?
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
                    null, schema, genClassInternalName, fieldIdentifier, debugValueLabel + ".component", javaClassCodec);

            mv.visitJumpInsn(GOTO, loopLabel);
            mv.visitLabel(endLabel);
            // PENDING: mv.visitFrame?
        } else {
            throw new IllegalArgumentException("Illegal sequence javaClass: " + javaClass);
        }
    }

    protected void generateEncodeTimeValue(TypeDef.Time type,
            Class<?> javaClass, boolean required, MethodVisitor mv) throws RuntimeException {
        if (javaClass == long.class || javaClass == Long.class) {
            generateEncodeInt64Value(required, mv);
        } else if (javaClass == int.class || javaClass == Integer.class) {
            generateEncodeInt32Value(required, mv);
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
            box(mv, long.class);
            mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeInt64Null", "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/Long;)V", false);

            if (!required) {
                mv.visitJumpInsn(GOTO, endLabel);
                mv.visitLabel(nullLabel);
                mv.visitInsn(POP);
                mv.visitInsn(ACONST_NULL);
                mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeInt64Null", "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/Long;)V", false);
                mv.visitLabel(endLabel);
            }
        } else {
            throw new IllegalArgumentException("Illegal time javaClass: " + javaClass);
        }
    }

    protected void generateEncodeEnumValue(TypeDef.Enum enumType, String genClassInternalName, String fieldIdentifier, 
            LocalVariable nextVar, Class<?> javaClass, boolean required, MethodVisitor mv, String debugValueLabel) 
                    throws IllegalArgumentException {

        Label writeValueLabel = new Label();
        
        // Make sure that no unsupported javaClass is used for an enum
        if (javaClass != int.class && javaClass != Integer.class && (javaClass == null || !javaClass.isEnum())) {
            throw new IllegalArgumentException("Illegal enum javaClass: " + javaClass);
        }

        // If null do not look it up
        if (!required) {
            Label lookupIdLabel = new Label();
            mv.visitInsn(DUP);
            mv.visitJumpInsn(IFNONNULL, lookupIdLabel);
            
            // null
            mv.visitInsn(POP);
            mv.visitInsn(ACONST_NULL);
            mv.visitJumpInsn(GOTO, writeValueLabel);
            
            // not null
            mv.visitLabel(lookupIdLabel);
        } 
        
        // SymbolMapping
        if (required && !javaClass.isEnum()) {
            box(mv, int.class);
        }
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, genClassInternalName, "symbolMapping_" + fieldIdentifier, "Lcom/cinnober/msgcodec/SymbolMapping;");
        mv.visitInsn(SWAP);
        mv.visitMethodInsn(INVOKEINTERFACE, "com/cinnober/msgcodec/SymbolMapping", "getId",
                "(Ljava/lang/Object;)Ljava/lang/Integer;", true);

        // Write the value
        mv.visitLabel(writeValueLabel);
        if (required) {
            unbox(mv, Integer.class); // TODO: Should null check before this and throw exception since there was no mapping
        }
        generateEncodeInt32Value(required, mv);

//        if (javaClass.isEnum()) {
//            Label endLabel = new Label();
//            if (!required) {
//                Label nonNullLabel = new Label();
//                mv.visitInsn(DUP);
//                mv.visitJumpInsn(IFNONNULL, nonNullLabel);
//                // null
//                mv.visitInsn(POP);
//                mv.visitInsn(ACONST_NULL);
//                mv.visitMethodInsn(INVOKESTATIC, blinkOutputIName, "writeInt32Null",
//                        "(Lcom/cinnober/msgcodec/io/ByteSink;Ljava/lang/Integer;)V", false);
//                mv.visitJumpInsn(GOTO, endLabel);
//                // not null
//                mv.visitLabel(nonNullLabel);
//            }
//            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Enum", "ordinal", "()I", false);
//            //switch
//            EnumSymbols enumSymbols = new EnumSymbols(enumType, javaClass);
//            Enum[] enumValues = ((Class<Enum>)javaClass).getEnumConstants();
//            int[] ordinals = new int[enumValues.length];
//            Label[] labels = new Label[enumValues.length];
//            for (int i=0; i<enumValues.length; i++) {
//                ordinals[i] = i;
//                labels[i] = new Label();
//            }
//            Label defaultLabel = new Label();
//            Label writeLabel = new Label();
//            int symbolIdVar = nextVar.next();
//            mv.visitLookupSwitchInsn(defaultLabel, ordinals, labels);
//            for (int i=0; i<enumValues.length; i++) {
//                mv.visitLabel(labels[i]);
//                Symbol symbol = enumSymbols.getSymbol(enumValues[i]);
//                if (symbol != null) {
//                    mv.visitLdcInsn(symbol.getId());
//                    mv.visitJumpInsn(GOTO, writeLabel);
//                } else {
//                    mv.visitLdcInsn(debugValueLabel);
//                    mv.visitVarInsn(ALOAD, symbolIdVar);
//                    mv.visitMethodInsn(INVOKESTATIC, baseclassIName, "unmappableEnumSymbolValue",
//                            "(Ljava/lang/String;Ljava/lang/Enum;)Ljava/lang/IllegalArgumentException;", false);
//                    mv.visitInsn(ATHROW);
//                }
//            }
//            mv.visitLabel(defaultLabel);
//            mv.visitTypeInsn(NEW, "java/lang/RuntimeException");
//            mv.visitInsn(DUP);
//            mv.visitLdcInsn("Should not happen: reached generated switch default case for value " + debugValueLabel);
//            mv.visitMethodInsn(INVOKESPECIAL, "java/lang/RuntimeException", "<init>", "(Ljava/lang/String;)V", false);
//            mv.visitInsn(ATHROW);
//
//            // write
//            mv.visitLabel(writeLabel);
//            if (!required) {
//                box(mv, int.class);
//            }
//            generateEncodeInt32Value(required, mv);
//            // end
//            mv.visitLabel(endLabel);
//        } else if (javaClass == int.class || javaClass == Integer.class) {
//            generateEncodeInt32Value(required, mv);
//        } else {
//            throw new IllegalArgumentException("Illegal enum javaClass: " + javaClass);
//        }
    }

    // --- GENERATE DECODE VALUE ---------------------------------------------------------------------------------------

    /**
     * Generate instructions to decode the specified value type.
     * The value on the stack is the input stream.
     * After this call the input stream is expected to be consumed, and the decoded value be placed on the stack.
     */
    protected void generateDecodeValue(MethodVisitor mv, int byteSourceVar, LocalVariable nextVar,
            boolean required, TypeDef type, Class<?> javaClass, Class<?> componentJavaClass, Schema schema,
            String genClassInternalName, String fieldIdentifier, String debugValueLabel, boolean javaClassCodec) {
        type = schema.resolveToType(type, false);
        GroupDef refGroup = schema.resolveToGroup(type);

        switch (type.getType()) {
            case INT8:
                generateDecodeInt8Value(required, mv);
                break;
            case UINT8:
                generateDecodeUInt8Value(required, mv);
                break;
            case INT16:
                generateDecodeInt16Value(required, mv);
                break;
            case UINT16:
                generateDecodeUInt16Value(required, mv);
                break;
            case INT32:
                generateDecodeInt32Value(required, mv);
                break;
            case UINT32:
                generateDecodeUInt32Value(required, mv);
                break;
            case INT64:
                generateDecodeInt64Value(required, mv);
                break;
            case UINT64:
                generateDecodeUInt64Value(required, mv);
                break;
            case FLOAT32:
                generateDecodeFloat32Value(required, mv);
                break;
            case FLOAT64:
                generateDecodeFloat64Value(required, mv);
                break;
            case BIGINT:
                generateDecodeBigIntValue(required, mv);
                break;
            case DECIMAL:
                generateDecodeDecimalValue(required, mv);
                break;
            case BIGDECIMAL:
                generateDecodeBigDecimalValue(required, mv);
                break;
            case STRING:
                generateDecodeStringValue((TypeDef.StringUnicode) type, mv, required);
                break;
            case BINARY:
                generateDecodeBinaryValue((TypeDef.Binary) type, mv, required);
                break;
            case BOOLEAN:
                generateDecodeBooleanValue(required, mv);
                break;
            case ENUM:
                generateDecodeEnumValue(required, mv, type, genClassInternalName, fieldIdentifier, nextVar, javaClass, debugValueLabel);
                break;
            case TIME:
                generateDecodeTimeValue((TypeDef.Time) type, javaClass, required, mv, nextVar);
                break;
            case SEQUENCE:
                generateDecodeSequenceValue(javaClass, nextVar, required, mv, componentJavaClass,
                        byteSourceVar, type, schema, genClassInternalName, fieldIdentifier, debugValueLabel, javaClassCodec);
                break;
            case REFERENCE:
                generateDecodeRefValue(refGroup, required, mv, byteSourceVar, genClassInternalName, javaClass,
                        type, javaClassCodec);
                break;
            case DYNAMIC_REFERENCE:
                generateDecodeDynRefValue(mv, required, refGroup, javaClass);
                break;
            default:
                throw new RuntimeException("Unhandled case: " + type.getType());
        }

        if (!required && javaClass.isPrimitive()) {
            // PENDING: null check and throw DecodeException (instead of NPE): Cannot represent absent (null) value.
            unbox(mv, javaClass);
        } else if (required && !javaClass.isPrimitive()) {
            box(mv, javaClass);
        }
    }

    
    protected void generateDecodeDummy(MethodVisitor mv, int byteSourceVar, LocalVariable nextVar,
            boolean required, TypeDef type, Class<?> javaClass, Class<?> componentJavaClass, Schema schema,
            String genClassInternalName, String debugValueLabel, boolean javaClassCodec) {
        type = schema.resolveToType(type, false);
        
        switch (type.getType()) {
            case ENUM:
                if (required) {
                    mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readUInt32",
                            "(Lcom/cinnober/msgcodec/io/ByteSource;)I", false);
                } else {
                    mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readUInt32Null",
                            "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/lang/Integer;", false);
                }
                break;
            default:
                generateDecodeValue(mv, byteSourceVar, nextVar, required, type, javaClass, componentJavaClass,
                    schema, genClassInternalName, null, debugValueLabel, javaClassCodec);
        }
    }
    
    /**
     * Generate value decoding using the blink input.
     *
     * <p>Defaults to <code>readBoolean[Null].</code>
     *
     * @param required true if the field is required, otherwise false.
     * @param mv the method visitor, not null.
     * @see #generateDecodeValue
     */
    protected void generateDecodeBooleanValue(boolean required, MethodVisitor mv) {
        if (required) {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readBoolean",
                    "(Lcom/cinnober/msgcodec/io/ByteSource;)Z", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readBooleanNull",
                    "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/lang/Boolean;", false);
        }
    }

    /**
     * Generate value decoding using the blink input.
     *
     * <p>Defaults to <code>readBinary[Null].</code>
     *
     * @param required true if the field is required, otherwise false.
     * @param mv the method visitor, not null.
     * @see #generateDecodeValue
     */
    protected void generateDecodeBinaryValue(TypeDef.Binary type, MethodVisitor mv, boolean required) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, baseclassIName, "getMaxBinarySize",
                "()I", false);
        if (required) {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readBinary", "(Lcom/cinnober/msgcodec/io/ByteSource;I)[B", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readBinaryNull", "(Lcom/cinnober/msgcodec/io/ByteSource;I)[B", false);
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
    protected void generateDecodeStringValue(TypeDef.StringUnicode type, MethodVisitor mv, boolean required) {
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKEVIRTUAL, baseclassIName, "getMaxBinarySize",
                "()I", false);
        if (required) {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readStringUTF8",
                    "(Lcom/cinnober/msgcodec/io/ByteSource;I)Ljava/lang/String;", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readStringUTF8Null",
                    "(Lcom/cinnober/msgcodec/io/ByteSource;I)Ljava/lang/String;", false);
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
    protected void generateDecodeBigDecimalValue(boolean required, MethodVisitor mv) {
        if (required) {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readBigDecimal",
                    "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/math/BigDecimal;", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readBigDecimalNull",
                    "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/math/BigDecimal;", false);
        }
    }

    /**
     * Generate value decoding using the blink input.
     *
     * <p>Defaults to <code>readDecimal[Null].</code>
     *
     * @param required true if the field is required, otherwise false.
     * @param mv the method visitor, not null.
     * @see #generateDecodeValue
     */
    protected void generateDecodeDecimalValue(boolean required, MethodVisitor mv) {
        if (required) {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readDecimal",
                    "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/math/BigDecimal;", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readDecimalNull",
                    "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/math/BigDecimal;", false);
        }
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
    protected void generateDecodeBigIntValue(boolean required, MethodVisitor mv) {
        if (required) {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readBigInt",
                    "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/math/BigInteger;", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readBigIntNull",
                    "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/math/BigInteger;", false);
        }
    }

    /**
     * Generate value decoding using the blink input.
     *
     * <p>Defaults to <code>readFloat64[Null].</code>
     *
     * @param required true if the field is required, otherwise false.
     * @param mv the method visitor, not null.
     * @see #generateDecodeValue
     */
    protected void generateDecodeFloat64Value(boolean required, MethodVisitor mv) {
        if (required) {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readFloat64",
                    "(Lcom/cinnober/msgcodec/io/ByteSource;)D", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readFloat64Null",
                    "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/lang/Double;", false);
        }
    }

    /**
     * Generate value decoding using the blink input.
     *
     * <p>Defaults to <code>readFloat32[Null].</code>
     *
     * @param required true if the field is required, otherwise false.
     * @param mv the method visitor, not null.
     * @see #generateDecodeValue
     */
    protected void generateDecodeFloat32Value(boolean required, MethodVisitor mv) {
        if (required) {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readFloat32",
                    "(Lcom/cinnober/msgcodec/io/ByteSource;)F", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readFloat32Null",
                    "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/lang/Float;", false);
        }
    }

    /**
     * Generate value decoding using the blink input.
     *
     * <p>Defaults to <code>readUInt64[Null].</code>
     *
     * @param required true if the field is required, otherwise false.
     * @param mv the method visitor, not null.
     * @see #generateDecodeValue
     */
    protected void generateDecodeUInt64Value(boolean required, MethodVisitor mv) {
        if (required) {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readUInt64",
                    "(Lcom/cinnober/msgcodec/io/ByteSource;)J", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readUInt64Null",
                    "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/lang/Long;", false);
        }
    }

    /**
     * Generate value decoding using the blink input.
     *
     * <p>Defaults to <code>readInt64[Null].</code>
     *
     * @param required true if the field is required, otherwise false.
     * @param mv the method visitor, not null.
     * @see #generateDecodeValue
     */
    protected void generateDecodeInt64Value(boolean required, MethodVisitor mv) {
        if (required) {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readInt64",
                    "(Lcom/cinnober/msgcodec/io/ByteSource;)J", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readInt64Null",
                    "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/lang/Long;", false);
        }
    }

    /**
     * Generate value decoding using the blink input.
     *
     * <p>Defaults to <code>readUInt32Null].</code>
     *
     * @param required true if the field is required, otherwise false.
     * @param mv the method visitor, not null.
     * @see #generateDecodeValue
     */
    protected void generateDecodeUInt32Value(boolean required, MethodVisitor mv) {
        if (required) {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readUInt32",
                    "(Lcom/cinnober/msgcodec/io/ByteSource;)I", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readUInt32Null",
                    "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/lang/Integer;", false);
        }
    }

    /**
     * Generate value decoding using the blink input.
     *
     * <p>Defaults to <code>readInt32[Null].</code>
     *
     * @param required true if the field is required, otherwise false.
     * @param mv the method visitor, not null.
     * @see #generateDecodeValue
     */
    protected void generateDecodeInt32Value(boolean required, MethodVisitor mv) {
        if (required) {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readInt32",
                    "(Lcom/cinnober/msgcodec/io/ByteSource;)I", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readInt32Null",
                    "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/lang/Integer;", false);
        }
    }

    /**
     * Generate value decoding using the blink input.
     *
     * <p>Defaults to <code>readUInt16[Null].</code>
     *
     * @param required true if the field is required, otherwise false.
     * @param mv the method visitor, not null.
     * @see #generateDecodeValue
     */
    protected void generateDecodeUInt16Value(boolean required, MethodVisitor mv) {
        if (required) {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readUInt16",
                    "(Lcom/cinnober/msgcodec/io/ByteSource;)S", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readUInt16Null",
                    "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/lang/Short;", false);
        }
    }

    /**
     * Generate value decoding using the blink input.
     *
     * <p>Defaults to <code>readInt16[Null].</code>
     *
     * @param required true if the field is required, otherwise false.
     * @param mv the method visitor, not null.
     * @see #generateDecodeValue
     */
    protected void generateDecodeInt16Value(boolean required, MethodVisitor mv) {
        if (required) {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readInt16",
                    "(Lcom/cinnober/msgcodec/io/ByteSource;)S", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readInt16Null",
                    "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/lang/Short;", false);
        }
    }

    /**
     * Generate value decoding using the blink input.
     *
     * <p>Defaults to <code>readUInt8[Null].</code>
     *
     * @param required true if the field is required, otherwise false.
     * @param mv the method visitor, not null.
     * @see #generateDecodeValue
     */
    protected void generateDecodeUInt8Value(boolean required, MethodVisitor mv) {
        if (required) {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readUInt8",
                    "(Lcom/cinnober/msgcodec/io/ByteSource;)B", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readUInt8Null",
                    "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/lang/Byte;", false);
        }
    }

    /**
     * Generate value decoding using the blink input.
     *
     * <p>Defaults to <code>readInt8[Null].</code>
     *
     * @param required true if the field is required, otherwise false.
     * @param mv the method visitor, not null.
     * @see #generateDecodeValue
     */
    protected void generateDecodeInt8Value(boolean required, MethodVisitor mv) {
        if (required) {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readInt8",
                    "(Lcom/cinnober/msgcodec/io/ByteSource;)B", false);
        } else {
            mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readInt8Null",
                    "(Lcom/cinnober/msgcodec/io/ByteSource;)Ljava/lang/Byte;", false);
        }
    }

    protected void generateDecodeTimeValue(TypeDef.Time type, Class<?> javaClass, boolean required, MethodVisitor mv,
            LocalVariable nextVar) throws IllegalArgumentException {
        if (javaClass == long.class || javaClass == Long.class) {
            generateDecodeInt64Value(required, mv);
        } else if (javaClass == int.class || javaClass == Integer.class) {
            generateDecodeInt32Value(required, mv);
        } else if (javaClass == Date.class) {

            int timeVar = nextVar.next(); nextVar.next(); // note: 2 variable slots
            Label endLabel = new Label();
            if (required) {
                generateDecodeInt64Value(true, mv);
            } else {
                generateDecodeInt64Value(false, mv);
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

    /**
     * @see #generateDecodeInt32Value(boolean, org.objectweb.asm.MethodVisitor) 
     */
    protected void generateDecodeEnumValue(boolean required, MethodVisitor mv, TypeDef type,
            String genClassInternalName, String fieldIdentifier, LocalVariable nextVar, 
            Class<?> javaClass, String debugValueLabel)
            throws IllegalArgumentException {

        Label endLabel = new Label();
        if (required) {
            generateDecodeInt32Value(true, mv);
            box(mv, Integer.class);
        } else {
            generateDecodeInt32Value(false, mv);
            mv.visitInsn(DUP);
            mv.visitJumpInsn(IFNULL, endLabel);
            
//            Label nonNullLabel = new Label();
//            mv.visitJumpInsn(IFNONNULL, nonNullLabel);
//            mv.visitTypeInsn(CHECKCAST, Type.getInternalName(javaClass));
//            mv.visitJumpInsn(GOTO, endLabel);

            // not null
//            mv.visitLabel(nonNullLabel);
//            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false); // unbox(mv, Integer.class);
        }
        
        // SymbolMapping
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, genClassInternalName, "symbolMapping_" + fieldIdentifier, "Lcom/cinnober/msgcodec/SymbolMapping;");
        mv.visitInsn(SWAP);
        mv.visitMethodInsn(INVOKEINTERFACE, "com/cinnober/msgcodec/SymbolMapping", "lookup",
                "(Ljava/lang/Integer;)Ljava/lang/Object;", true);
        
//        // switch
//        TypeDef.Enum enumType = (TypeDef.Enum) type;
//        List<Symbol> symbols = enumType.getSymbols();
//        int numSymbols = symbols.size();
//        int[] ids = new int[numSymbols];
//        Label[] labels = new Label[numSymbols];
//        for (int i=0; i<numSymbols; i++) {
//            ids[i] = symbols.get(i).getId();
//            labels[i] = new Label();
//        }
//        Label defaultLabel = new Label();
//        int symbolIdVar = nextVar.next();
//        mv.visitInsn(DUP);
//        mv.visitVarInsn(ISTORE, symbolIdVar);
//        EnumSymbols enumSymbols = null;
//        if (javaClass.isEnum()) {
//            enumSymbols = new EnumSymbols(enumType, javaClass);
//        }
//        mv.visitLookupSwitchInsn(defaultLabel, ids, labels);
//        for (int i=0; i<numSymbols; i++) {
//            boolean addGotoEnd = true;
//            mv.visitLabel(labels[i]);
//            if (javaClass.isEnum()) {
//                Enum enumValue = enumSymbols.getEnum(ids[i]);
//                if (enumValue != null) {
//                    //mv.visitLdcInsn(Type.getType(javaClass));
//                    mv.visitFieldInsn(GETSTATIC, Type.getInternalName(javaClass),
//                            enumValue.name(),
//                            Type.getDescriptor(javaClass));
//                } else {
//                    mv.visitLdcInsn(debugValueLabel);
//                    mv.visitLdcInsn(ids[i]);
//                    mv.visitLdcInsn(Type.getType(javaClass));
//                    mv.visitMethodInsn(INVOKESTATIC, baseclassIName, "unmappableEnumSymbolId",
//                            "(Ljava/lang/String;ILjava/lang/Class;)Lcom/cinnober/msgcodec/DecodeException;", false);
//                    mv.visitInsn(ATHROW);
//                    addGotoEnd = false;
//                }
//            } else if (javaClass == int.class || javaClass == Integer.class) {
//                mv.visitLdcInsn(ids[i]);
//                if (!required) {
//                    box(mv, Integer.class);
//                }
//            } else {
//                throw new IllegalArgumentException("Illegal enum javaClass: " + javaClass);
//            }
//            if (addGotoEnd) {
//                mv.visitJumpInsn(GOTO, endLabel);
//            }
//        }
//        mv.visitLabel(defaultLabel);
//        mv.visitLdcInsn(debugValueLabel);
//        mv.visitVarInsn(ILOAD, symbolIdVar);
//        mv.visitMethodInsn(INVOKESTATIC, baseclassIName, "unknownEnumSymbol",
//                "(Ljava/lang/String;I)Lcom/cinnober/msgcodec/DecodeException;", false);
//        mv.visitInsn(ATHROW);

        // end
        mv.visitLabel(endLabel);

        if (!javaClass.isEnum()) {
            mv.visitTypeInsn(CHECKCAST, Type.getInternalName(Integer.class));
            if (required) {
                unbox(mv, Integer.class);
            }
        } else {
            mv.visitTypeInsn(CHECKCAST, Type.getInternalName(javaClass));
        }
    }

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
    }

    final static String getTypeDescriptor(Object msgClass, boolean javaClassCodec) {
        if (javaClassCodec && msgClass instanceof Class) {
            return Type.getDescriptor((Class<?>) msgClass);
        } else {
            return "Ljava/lang/Object;";
        }
    }

    final static String getTypeInternalName(Object msgClass, boolean javaClassCodec) {
        if (javaClassCodec && msgClass instanceof Class) {
            return Type.getInternalName((Class<?>) msgClass);
        } else if (javaClassCodec) {
            return Type.getInternalName(Object.class);
        } else {
            return null;
        }
    }

    final static Type getJavaType(Object msgClass) {
        if (msgClass instanceof Class) {
            return Type.getType((Class<?>) msgClass);
        } else {
            return Type.getType(Object.class);
        }
    }

    protected void generateDecodeRefValue(GroupDef refGroup, boolean required, MethodVisitor mv, int byteSourceVar,
            String genClassInternalName,
            Class<?> javaClass, TypeDef type, boolean javaClassCodec) throws IllegalArgumentException {
        if (refGroup != null) {
            String groupDescriptor = getTypeDescriptor(javaClass,javaClassCodec);
            if (required) {
                mv.visitInsn(POP); // input stream
                mv.visitVarInsn(ALOAD, 0);
                mv.visitVarInsn(ALOAD, byteSourceVar);
                mv.visitMethodInsn(INVOKEVIRTUAL, genClassInternalName, "readStaticGroup_" + refGroup.getName(),
                        "(Lcom/cinnober/msgcodec/io/ByteSource;)" + groupDescriptor,
                        false);
            } else {
                mv.visitMethodInsn(INVOKESTATIC, blinkInputIName, "readPresenceByte", "(Lcom/cinnober/msgcodec/io/ByteSource;)Z", false);
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
                mv.visitVarInsn(ALOAD, byteSourceVar);
                mv.visitMethodInsn(INVOKEVIRTUAL, genClassInternalName, "readStaticGroup_" + refGroup.getName(),
                        "(Lcom/cinnober/msgcodec/io/ByteSource;)" + groupDescriptor,
                        false);

                mv.visitLabel(endLabel);
                // PENDING: mv.visitFrame?
                mv.visitFrame(F_SAME, 0, null, 0, null);
            }
        } else {
            throw new IllegalArgumentException("Illegal reference: " + type);
        }
    }

    protected void generateDecodeSequenceValue(
            Class<?> javaClass, LocalVariable nextVar, boolean required, MethodVisitor mv,
            Class<?> componentJavaClass, int byteSourceVar, TypeDef type, Schema schema,
            String genClassInternalName, String fieldIdentifier, String debugValueLabel, boolean javaClassCodec)
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
        // PENDING: mv.visitFrame?
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
                schema, genClassInternalName, fieldIdentifier, debugValueLabel + ".component", javaClassCodec);

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

    public static boolean isPublicFieldAccessor(Accessor<?,?> accessor) {
        return accessor.getClass() == FieldAccessor.class &&
                Modifier.isPublic(((FieldAccessor)accessor).getField().getModifiers());
    }

    public static boolean isPublicConstructorFactory(Factory<?> factory) {
        if (factory.getClass() != ConstructorFactory.class) {
            return false;
        }
        Constructor<?> constructor = ((ConstructorFactory<?>)factory).getConstructor();
        return Modifier.isPublic(constructor.getModifiers()) &&
                !Modifier.isAbstract(constructor.getDeclaringClass().getModifiers());
    }

    public static void generateArrayStore(MethodVisitor mv, Class<?> componentJavaClass) {
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
    public static void generateNewArray(MethodVisitor mv, Class<?> componentJavaClass) {
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
    
    public static void unbox(MethodVisitor mv, Class<?> javaClass) {
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
    public static void box(MethodVisitor mv, Class<?> javaClass) {
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

    public static Class<?> unbox(Class<?> javaClass) {
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
    public static Class<?> box(Class<?> javaClass) {
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

    protected static class ObjectHashCodeSwitchCase<T> {
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
    protected static class ObjectSwitchCase<T> {
        final T object;
        final Label label;

        ObjectSwitchCase(T object, Label label) {
            this.object = object;
            this.label = label;
        }

    }

    protected static class LocalVariable {
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
