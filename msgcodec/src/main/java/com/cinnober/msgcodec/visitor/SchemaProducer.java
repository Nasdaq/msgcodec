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
 * Schema visitor that can produce a schema instance.
 * 
 * @see #getSchema() 
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

    /**
     * Returns the created schema for the most recently visited schema.
     * The schema is created when {@link #visitEnd()} is called.
     * 
     * @return the schema, or null if no schema has been visited.
     */
    public Schema getSchema() {
        return schema;
    }

    private Schema createSchema() {
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
