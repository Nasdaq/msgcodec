/*
 * Copyright (c) 2015 Cinnober Financial Technology AB, Stockholm,
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

package com.cinnober.msgcodec.visitor;

import com.cinnober.msgcodec.GroupBinding;
import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.SchemaBinding;
import com.cinnober.msgcodec.TypeDef;
import com.cinnober.msgcodec.messages.MetaProtocol;
import com.cinnober.msgcodec.messages.MetaSchema;

/**
 * A visitor to visit a schema.
 *
 * <p>The methods of this class must be called in the following order:
 * <code>visit | visitAnnotation* | visitNamedType* | visitGroup* | visitEnd</code>
 *
 * @author mikael.brannstrom
 */
public abstract class SchemaVisitor implements AnnotatedVisitor {

    protected SchemaVisitor sv;

    public SchemaVisitor(SchemaVisitor sv) {
        this.sv = sv;
    }

    public SchemaVisitor() {
        this(null);
    }

    public void visit(SchemaBinding binding) {
        if (sv != null) {
            sv.visit(binding);
        }
    }

    @Override
    public void visitAnnotation(String key, String value) {
        if (sv != null) {
            sv.visitAnnotation(key, value);
        }
    }

    public NamedTypeVisitor visitNamedType(String name, TypeDef type) {
        if (sv != null) {
            return sv.visitNamedType(name, type);
        }
        return null;
    }

    public GroupDefVisitor visitGroup(String name, int id, String superGroup, GroupBinding binding) {
        if (sv != null) {
            return sv.visitGroup(name, id, superGroup, binding);
        }
        return null;
    }

    public void visitEnd() {
        if (sv != null) {
            sv.visitEnd();
        }
    }


    public static void accept(Schema schema, SchemaVisitor sv) {
        sv.visit(schema.getBinding());
        schema.getAnnotations().forEach(sv::visitAnnotation);
        schema.getNamedTypes().forEach(t -> {
            NamedTypeVisitor tv = sv.visitNamedType(t.getName(), t.getType());
            if (tv != null) {
                t.getAnnotations().forEach(tv::visitAnnotation);
                tv.visitEnd();
            }
        });
        schema.getGroups().forEach(g -> {
            GroupDefVisitor gv = sv.visitGroup(
                    g.getName(),
                    g.getId(),
                    g.getSuperGroup(),
                    g.getBinding());
            if (gv != null) {
                g.getAnnotations().forEach(gv::visitAnnotation);
                g.getFields().forEach(f -> {
                    FieldDefVisitor fv = gv.visitField(
                            f.getName(),
                            f.getId(),
                            f.isRequired(),
                            f.getType(),
                            f.getBinding());
                    if (fv != null) {
                        f.getAnnotations().forEach(fv::visitAnnotation);
                    }
                    fv.visitEnd();
                });
                gv.visitEnd();
            }
        });
        sv.visitEnd();
    }

    public static void accept(MetaSchema schema, SchemaVisitor sv) {
        sv.visit(null);
        schema.annotations.forEach(a -> sv.visitAnnotation(a.name, a.value));
        schema.namedTypes.forEach(t -> {
            NamedTypeVisitor tv = sv.visitNamedType(t.name, t.type.toTypeDef());
            if (tv != null) {
                t.annotations.forEach(a -> tv.visitAnnotation(a.name, a.value));
                tv.visitEnd();
            }
        });
        schema.groups.forEach(g -> {
            GroupDefVisitor gv = sv.visitGroup(g.name, g.id, g.superGroup, null);
            if (gv != null) {
                g.annotations.forEach(a -> gv.visitAnnotation(a.name, a.value));
                g.fields.forEach(f -> {
                    FieldDefVisitor fv = gv.visitField(f.name, f.id, f.required, f.type.toTypeDef(), null);
                    if (fv != null) {
                        f.annotations.forEach(a -> fv.visitAnnotation(a.name, a.value));
                        fv.visitEnd();
                    }
                });
                gv.visitEnd();
            }
        });
    }

//    public static Schema assignGroupIds(Schema schema) {
//        SchemaProducer sp = new SchemaProducer();
//        SchemaVisitor sv = new SchemaVisitor(sp) {
//            @Override
//            public GroupDefVisitor visitGroup(String name, int id, String superGroup, GroupBinding binding) {
//                if (id == -1) {
//                    id = name.hashCode();
//                    if (id == -1) {
//                        id = -2;
//                    }
//                }
//                return super.visitGroup(name, id, superGroup, binding);
//            }
//        };
//        accept(schema, sv);
//        return sp.getSchema();
//    }
}
