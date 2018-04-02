package com.cinnober.msgcodec.json.jaxrs;

import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.json.JsonCodec;
import com.cinnober.msgcodec.json.JsonCodecFactory;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import javax.annotation.Priority;
import javax.ws.rs.Consumes;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.ext.MessageBodyReader;

/**
 * MessageBodyReader implementation for parsing MsgCodec messages in JSON format.
 * 
 * @author mikael.brannstrom@tradedoubler.com
 */
@Consumes(MediaType.APPLICATION_JSON)
@Priority(100) // Must be higher than JacksonMessageBodyProvider that is registered by default in e.g. DropWizard
public class JsonCodecMessageBodyReader implements MessageBodyReader<Object> {

    private final Schema schema;
    private final JsonCodec codec;

    public JsonCodecMessageBodyReader(Schema schema) {
        this.schema = schema;
        codec = new JsonCodecFactory(schema).createCodec();
    }
    
    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return mediaType.equals(MediaType.APPLICATION_JSON_TYPE) && schema.getGroup(type) != null;
    }

    @Override
    public Object readFrom(Class<Object> type, Type genericType, Annotation[] annotations, MediaType mediaType, MultivaluedMap<String, String> httpHeaders, InputStream entityStream) throws IOException, WebApplicationException {        
        try {
            return codec.decodeStatic(type, entityStream);
        } catch(Exception e) {
            throw new WebApplicationException(Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build());
        }
    }

}
