package com.cinnober.msgcodec.json.swagger;

import com.cinnober.msgcodec.FieldDef;
import com.cinnober.msgcodec.GroupDef;
import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.TypeDef;
import com.cinnober.msgcodec.util.TimeFormat;
import com.fasterxml.jackson.databind.type.SimpleType;
import io.swagger.converter.ModelConverter;
import io.swagger.converter.ModelConverterContext;
import io.swagger.models.ComposedModel;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.RefModel;
import io.swagger.models.properties.AbstractProperty;
import io.swagger.models.properties.ArrayProperty;
import io.swagger.models.properties.BaseIntegerProperty;
import io.swagger.models.properties.BinaryProperty;
import io.swagger.models.properties.BooleanProperty;
import io.swagger.models.properties.DateProperty;
import io.swagger.models.properties.DateTimeProperty;
import io.swagger.models.properties.DecimalProperty;
import io.swagger.models.properties.DoubleProperty;
import io.swagger.models.properties.FloatProperty;
import io.swagger.models.properties.IntegerProperty;
import io.swagger.models.properties.LongProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.RefProperty;
import io.swagger.models.properties.StringProperty;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * The class converts a JSON msgcodec schema into a swagger model.
 * 
 * <p>Usage:
 * <pre>
 * ModelConverters.getInstance().addConverter(new JsonCodecSwaggerModelConverter(schema));
 * </pre>
 * 
 * @author mikael.brannstrom@tradedoubler.com
 */
public class JsonCodecSwaggerModelConverter implements ModelConverter {

    private static final String DOC_ANOT = "doc";
    private static final String JSTYPE_FIELD = "$type";
    
    private final Schema schema;

    public JsonCodecSwaggerModelConverter(Schema schema) {
        this.schema = Objects.requireNonNull(schema);
    }

    private Type unbox(Type type) {
        if (type instanceof SimpleType) {
            SimpleType stype = (SimpleType) type;
            return stype.getRawClass();
        }
        return type;
    }

    
    @Override
    public Property resolveProperty(Type type, ModelConverterContext context, Annotation[] annotations, Iterator<ModelConverter> chain) {
        if (chain.hasNext()) {
            return chain.next().resolveProperty(type, context, annotations, chain);
        }
        return null;
    }

    @Override
    public Model resolve(Type type, ModelConverterContext context, Iterator<ModelConverter> chain) {
        Type javaType = unbox(type);
        GroupDef group = schema.getGroup(javaType);
        if (group != null) {
            return createStaticModel(group, context);
        }
        if (chain.hasNext()) {
            return chain.next().resolve(type, context, chain);
        }
        return null;
    }
    
    private ModelImpl createStaticModel(GroupDef group, ModelConverterContext context) {
        ModelImpl model = new ModelImpl()
                .name("static_"+group.getName())
                .type("object")
                .description(group.getAnnotation(DOC_ANOT));
        addProperties(model, group, true, context);
        return model;
    }

    private void addProperties(ModelImpl model, GroupDef group, boolean parents, ModelConverterContext context) {
        if (parents) {
            String superGroupName = group.getSuperGroup();
            if (superGroupName != null) {
                GroupDef superGroup = schema.getGroup(superGroupName);
                addProperties(model, superGroup, parents, context);
                // PENDING: refer to the super group properties using "allOf", instead of copying them
            }
        }
        for (FieldDef field : group.getFields()) {
            Property prop = createProperty(field.getType(), context);
            prop.setName(field.getName());
            prop.setRequired(field.isRequired());
            prop.setDescription(field.getAnnotation(DOC_ANOT));
            model.addProperty(field.getName(), prop);
        }
    }
    
    private String registerDynamicModel(GroupDef group, ModelConverterContext context) {
        if (group != null) {
            
            ComposedModel model = new ComposedModel();
            model.setDescription(group.getAnnotation(DOC_ANOT));
            String supergroup = group.getSuperGroup();
            model.child(new RefModel(supergroup != null ? supergroup : "any"));            
            
            registerDynamicModel(supergroup != null ? schema.getGroup(supergroup) : null, context);
            
            ModelImpl selfModel = new ModelImpl().type("object");
            addProperties(selfModel, group, false, context);
            model.child(selfModel);

            context.defineModel(group.getName(), model);
            return group.getName();
        } else {
            ModelImpl model = new ModelImpl().type("object").name("any").discriminator(JSTYPE_FIELD);
            Property prop = new StringProperty().required(true);
            prop.setName(JSTYPE_FIELD);
            model.addProperty(JSTYPE_FIELD, prop);

            context.defineModel("any", model);
            return "any";
        }
    }
    
    private Property createProperty(TypeDef type, ModelConverterContext context) {
        type = schema.resolveToType(type, true);
        GroupDef group = schema.resolveToGroup(type);
        switch (type.getType()) {
            case BIGDECIMAL:
                return new DecimalProperty("bigdecimal");
            case DECIMAL:
                return new DecimalProperty("decimal");
            case BIGINT:
                return new BaseIntegerProperty("biginteger");
            case INT8:
                return new BaseIntegerProperty("int8");//.minimum((double) (-1 << 7)).maximum((double) 0x7f);
            case UINT8:
                return new BaseIntegerProperty("uint8");//.minimum(0.0).maximum((double) 0xff);
            case INT16:
                return new BaseIntegerProperty("int16");//.minimum((double) (-1 << 15)).maximum((double) 0x7fff);
            case UINT16:
                return new BaseIntegerProperty("uint16");//.minimum(0.0).maximum((double) 0xffff);
            case INT32:
                return new IntegerProperty();
            case UINT32:
                return new BaseIntegerProperty("uint32");//.minimum(0.0).maximum((double) 0xffffffffL);
            case INT64:
                return new LongProperty();
            case UINT64:
                return new BaseIntegerProperty("uint64");//.minimum(0.0);
            case FLOAT32:
                return new FloatProperty();
            case FLOAT64:
                return new DoubleProperty();
            case BOOLEAN:
                return new BooleanProperty();
            case BINARY:
                return new BinaryProperty();
            case STRING:
                return new StringProperty();
            case TIME: {
                TypeDef.Time timeType = (TypeDef.Time) type;
                AbstractProperty prop;
                if (timeType.getUnit() == TimeUnit.DAYS) {
                    prop = new DateProperty();
                } else {
                    prop = new DateTimeProperty();
                }
                long exampleValue = System.currentTimeMillis();
                if (timeType.getUnit().compareTo(TimeUnit.MILLISECONDS) < 0) {
                    exampleValue *= timeType.getUnit().toMillis(1);
                } else {
                    exampleValue /= timeType.getUnit().toMillis(1);
                }
                String example = TimeFormat.getTimeFormat(timeType.getUnit(), timeType.getEpoch())
                        .format(exampleValue);
                prop.setExample(example);
                if (timeType.getTimeZone() != null) {
                    prop.setVendorExtension("x-timezone", timeType.getTimeZone().getID());
                }
                return prop;
            }
            case ENUM: {
                TypeDef.Enum enumType = (TypeDef.Enum) type;
                return new StringProperty("enum")._enum(
                        enumType.getSymbols().stream().map(TypeDef.Symbol::getName).collect(Collectors.toList()));
            }
            case SEQUENCE: {
                TypeDef.Sequence sequenceType = (TypeDef.Sequence) type;
                return new ArrayProperty(createProperty(sequenceType.getComponentType(), context));
            }
            case REFERENCE: {
                //TypeDef.Reference refType = (TypeDef.Reference) type;
                ModelImpl model = createStaticModel(group, context);
                context.defineModel(model.getName(), model);
                return new RefProperty(model.getName());
            }
            case DYNAMIC_REFERENCE: {
                TypeDef.DynamicReference refType = (TypeDef.DynamicReference) type;
                String name = registerDynamicModel(group, context);
                for (GroupDef instanceGroup: schema.getDynamicGroups(refType.getRefType())) {
                    registerDynamicModel(instanceGroup, context);
                }
                RefProperty prop = new RefProperty(name);
                return prop;
            }
            default:
                throw new RuntimeException("Unhandled type: " + type.getType());
        }
    }

}
