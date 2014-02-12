/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cinnober.msgcodec.blink;

import com.cinnober.msgcodec.DecodeException;
import com.cinnober.msgcodec.util.LimitInputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Base class for a dynamically generated codec for a specific dictionary.
 * There are two sub classes; {@link GeneratedJavaClassCodec} and {@link GeneratedGenericCodec}.
 * 
 * A GeneratedCodec sub class represents a dictionary.
 * A GeneratedCodec instance is tied to a specific {@link BlinkCodec} instance which holds the encode buffers
 * and any max binary size settings.
 * 
 * @author mikael brannstrom
 */
abstract class GeneratedCodec {

    /** Reference to the blink codec. */
    protected final BlinkCodec codec;

    GeneratedCodec(BlinkCodec codec) {
        this.codec = codec;
    }
    
    /**
     * Write a static group and its group id.
     * Method to be generated in a sub class using <b>invoke dynamic</b> based on the group type.
     * 
     * @param out where to write to, not null
     * @param group the group to write, not null.
     * @throws IOException if the underlying stream throws an exception.
     * @throws IllegalArgumentException if an illegal value is encountered, e.g. missing required field value.
     */
    protected abstract void writeStaticGroupWithId(OutputStream out, Object group) 
            throws IOException, IllegalArgumentException;
    
    /**
     * Read a static group.
     * Method to be generated in a sub class using <b>switch</b> based on group id.
     * 
     * @param groupId the group id
     * @param in where to read from, not null.
     * @return the decoded group, not null.
     * @throws IOException if the underlying stream throws an exception.
     * @throws DecodeException if the group could not be decoded.
     */
    protected abstract Object readStaticGroup(int groupId, LimitInputStream in) throws IOException, DecodeException;

    /**
     * Write a dynamic group to the specified output stream.
     * @param out where to write to, not null.
     * @param group the group to encode, not null
     * @throws IOException if the underlying stream throws an exception.
     */
    void writeDynamicGroup(OutputStream out, Object group) throws IOException, IllegalArgumentException {
        // TODO: Preamble PRE Stuff here
        
        writeStaticGroupWithId(out, group);
        
        // TODO: Preamble POST Stuff here
    }

    /**
     * Write a nullable dynamic group to the specified output stream.
     * @param out where to write to, or null.
     * @param group the group to encode, not null
     * @throws IOException if the underlying stream throws an exception.
     */
    void writeDynamicGroupNull(OutputStream out, Object obj) throws IOException {
        if (obj == null) {
            BlinkOutput.writeNull(out);
        } else {
            writeDynamicGroup(out, obj);
        }
    }
    
    /** 
     * Read a dynamic group.
     * @param in the stream to read from.
     * @return the group, not null.
     * @throws IOException if the underlying stream throws an exception.
     */
    Object readDynamicGroup(LimitInputStream in) throws IOException {
        int size = BlinkInput.readUInt32(in);
        return readDynamicGroup(size, in);
    }
    /** 
     * Read a nullable dynamic group.
     * @param in the stream to read from.
     * @return the group, or null.
     * @throws IOException if the underlying stream throws an exception.
     */
    Object readDynamicGroupNull(LimitInputStream in) throws IOException {
        Integer sizeObj = BlinkInput.readUInt32Null(in);
        if (sizeObj == null) {
            return null;
        }
        int size = sizeObj.intValue();
        return readDynamicGroup(size, in);
    }

    private Object readDynamicGroup(int size, LimitInputStream in) throws IOException {
        int limit = in.limit();
        try {
            if (limit >= 0) {
                if (size > limit) {
                    // there is already a limit that is smaller than this message size
                    throw new DecodeException("Dynamic group size preamble (" + size +
                            ") goes beyond current stream limit (" + limit + ").");
                } else {
                    limit -= size;
                    in.limit(size);
                }
            }
            int groupId = BlinkInput.readUInt32(in);
            Object group = readStaticGroup(groupId, in);
            in.skip(in.limit());
            return group;
        } finally {
            in.limit(limit); // restore old limit
        }
    }
    
    
}
