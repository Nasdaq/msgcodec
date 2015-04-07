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

import com.cinnober.msgcodec.FieldBinding;
import com.cinnober.msgcodec.FieldDef;
import com.cinnober.msgcodec.GroupBinding;
import com.cinnober.msgcodec.GroupDef;
import com.cinnober.msgcodec.NamedType;
import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.SchemaBinding;
import com.cinnober.msgcodec.TypeDef;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author mikael.brannstrom
 */
public class SchemaProducer extends SchemaVisitor {

    private SchemaBinding binding;
    private final Map<String, String> annotations = new LinkedHashMap<>();
    private final List<GroupDefProducer> groups = new ArrayList<>();
    private final List<NamedTypeProducer> namedTypes = new ArrayList<>();
    private Schema schema;

    public SchemaProducer() {
    }

    @Override
    public void visit(SchemaBinding binding) {
        this.binding = binding;
        annotations.clear();
        groups.clear();
        namedTypes.clear();
        schema = null;
    }

    @Override
    public void visitAnnotation(String key, String value) {
        annotations.put(key, value);
    }

    @Override
    public GroupDefVisitor visitGroup(String name, int id, String superGroup, GroupBinding binding) {
        GroupDefProducer group = new GroupDefProducer(name, id, superGroup, binding);
        groups.add(group);
        return group;
    }

    @Override
    public NamedTypeVisitor visitNamedType(String name, TypeDef type) {
        NamedTypeProducer namedType = new NamedTypeProducer(name, type);
        namedTypes.add(namedType);
        return namedType;
    }

    @Override
    public void visitEnd() {
        schema = createSchema();
    }

    public Schema getSchema() {
        return schema;
    }

    Schema createSchema() {
        return new Schema(
                groups.stream().map(GroupDefProducer::createGroupDef).collect(Collectors.toList()),
                namedTypes.stream().map(NamedTypeProducer::createNamedType).collect(Collectors.toList()),
                annotations,
                binding);
    }

    private static class GroupDefProducer extends GroupDefVisitor {

        private final String name;
        private final int id;
        private final String superGroup;
        private final GroupBinding binding;
        private final Map<String, String> annotations = new LinkedHashMap<>();
        private final List<FieldDefProducer> fields = new ArrayList<>();

        public GroupDefProducer(String name, int id, String superGroup, GroupBinding binding) {
            this.name = name;
            this.id = id;
            this.superGroup = superGroup;
            this.binding = binding;
        }

        @Override
        public void visitAnnotation(String key, String value) {
            annotations.put(key, value);
        }

        @Override
        public FieldDefVisitor visitField(String name, int id, boolean required, TypeDef type, FieldBinding binding) {
            FieldDefProducer field = new FieldDefProducer(name, id, required, type, binding);
            fields.add(field);
            return field;
        }
        GroupDef createGroupDef() {
            return new GroupDef(
                    name,
                    id,
                    superGroup,
                    fields.stream().map(FieldDefProducer::createFieldDef).collect(Collectors.toList()),
                    annotations,
                    binding);
        }
    }

    private static class FieldDefProducer extends FieldDefVisitor {
        private final String name;
        private final int id;
        private final boolean required;
        private final TypeDef type;
        private final FieldBinding binding;
        private final Map<String, String> annotations = new LinkedHashMap<>();

        public FieldDefProducer(String name, int id, boolean required, TypeDef type, FieldBinding binding) {
            this.name = name;
            this.id = id;
            this.required = required;
            this.type = type;
            this.binding = binding;
        }

        @Override
        public void visitAnnotation(String key, String value) {
            annotations.put(key, value);
        }
        FieldDef createFieldDef() {
            return new FieldDef(name, id, required, type, annotations, binding);
        }
    }

    private static class NamedTypeProducer extends NamedTypeVisitor {

        private final String name;
        private final TypeDef type;
        private final Map<String, String> annotations = new LinkedHashMap<>();

        public NamedTypeProducer(String name, TypeDef type) {
            this.name = name;
            this.type = type;
        }

        @Override
        public void visitAnnotation(String key, String value) {
            annotations.put(key, value);
        }
        NamedType createNamedType() {
            return new NamedType(name, type, annotations);
        }
    }
    
}
