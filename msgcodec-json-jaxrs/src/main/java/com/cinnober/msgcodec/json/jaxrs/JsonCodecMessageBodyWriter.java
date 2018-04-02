package com.cinnober.msgcodec.json.jaxrs;

import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.json.JsonCodec;
import com.cinnober.msgcodec.json.JsonCodecFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.annotation.Priority;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 * MessageBodyWriter implementation for serializing MsgCodec messages in JSON format.
 * 
 * @author mikael.brannstrom@tradedoubler.com
 */
@Produces(MediaType.APPLICATION_JSON)
@Priority(100) // Must be higher than JacksonMessageBodyProvider that is registered by default in e.g. DropWizard
public class JsonCodecMessageBodyWriter implements MessageBodyWriter<Object> {

    private final Schema schema;
    private final JsonCodec codec;

    public JsonCodecMessageBodyWriter(Schema schema) {
        this.schema = schema;
        codec = new JsonCodecFactory(schema).createCodec();
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return mediaType.equals(MediaType.APPLICATION_JSON_TYPE) && schema.getGroup(type) != null;
    }

    @Override
    public long getSize(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(Object t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream) throws IOException, WebApplicationException {
        codec.encodeStatic(t, entityStream);
    }

}
