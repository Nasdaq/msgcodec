/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.cinnober.msgcodec.blink;

import com.cinnober.msgcodec.Accessor;
import com.cinnober.msgcodec.DecodeException;
import com.cinnober.msgcodec.Factory;
import com.cinnober.msgcodec.ProtocolDictionary;
import com.cinnober.msgcodec.anot.Id;
import com.cinnober.msgcodec.util.LimitInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Experimental example of a subclass of GeneratedJavaClassCodec.
 * 
 * @author mikael brannstrom
 */
class GeneratedJavaClassCodec1 extends GeneratedJavaClassCodec {

    public GeneratedJavaClassCodec1(BlinkCodec codec, ProtocolDictionary dict) {
        super(codec, dict);
    }
    
    @Override
    protected void writeStaticGroupWithId(OutputStream out, Object obj) throws IOException {
        Class javaClass = obj.getClass();
        switch(javaClass.hashCode()) {
            case 123:
                if (javaClass == Foo.class) {
                    writeStaticGroupWithId(out, (Foo)obj);
                    break;
                }
            default:
                throw unknownObjectType(javaClass);
        }
    }

    @Override
    protected Object readStaticGroup(int groupId, LimitInputStream in) throws IOException {
        switch(groupId) {
            case 123:
                return readStaticGroup_Foo(in);
            default:
                throw unknownGroupId(groupId);
        }
    }

    Factory factory1;
    
    Bar readStaticGroup_Bar(LimitInputStream in) throws IOException {
        return null; // TODO
    }
    Foo readStaticGroup_Foo(LimitInputStream in) throws IOException {
        // -- Constructor factory
        Foo group = new Foo();
        // -- Generic factory
        Foo group2 = (Foo) factory1.newInstance();
        
        // -- primitive required
        int i1 = BlinkInput.readInt32(in);
        group.i32 = i1;
        
        // -- obj required
        int i3 = BlinkInput.readInt32(in);
        group.i32Obj = Integer.valueOf(i3);
        
        // -- obj optional
        Integer i4 = BlinkInput.readInt32Null(in);
        group.i32Obj = i4;
        
        // -- primitive optional -> NPE when null
        Integer i2 = BlinkInput.readInt32Null(in);
        group.i32 = i2.intValue();
        
        // -- bar required static
        Bar bar1 = readStaticGroup_Bar(in);
        group.bar = bar1;
        
        // -- bar optional static
        if (BlinkInput.readBoolean(in)) {
            Bar bar2 = readStaticGroup_Bar(in);
            group.bar = bar2;
        } else {
            group.bar = null;
        }

        // -- bar dynamic required
        Bar bar3 = (Bar) readDynamicGroup(in);
        group.bar = bar3;

        // -- bar dynamic optional
        Bar bar4 = (Bar) readDynamicGroupNull(in);
        group.bar = bar4;

        // -- accessor required int32
        int i5 = BlinkInput.readInt32(in);
        Integer i5obj = Integer.valueOf(i5);
        accessor1.setValue(group, i5obj);

        // -- ignoreaccessor
        BlinkInput.skipInt32(in);

        // -- sequence array
        int len1 = BlinkInput.readUInt32(in);
        int[] a1 = new int[len1];
        double[] a2 = new double[len1];
        String[] a3 = new String[len1];
        
        
        return group;
    }
    
    
    void writeStaticGroupWithId(OutputStream out, Foo foo) throws IOException {
        // if id is not -1
        BlinkOutput.writeUInt32(out, 123);
        writeStaticGroup(out, foo);
    }
    void writeStaticGroupWithId(OutputStream out, Bar bar) throws IOException {
        // if id is -1
        throw new IllegalArgumentException("Missing group id, cannot encode 'Bar' as dynamic group");
        //writeStaticGroup(out, bar);
    }
    
    void writeStaticGroup(OutputStream out, Foo foo) throws IOException {
        
        // -- primitive
        BlinkOutput.writeInt32(out, foo.i32);
        
        // -- wrapper and required
        Integer i2 = foo.i32Obj;
        if (i2 == null) {
            throw new IllegalArgumentException("Required field 'Foo.i32Obj' is null");
        }
        BlinkOutput.writeInt32(out, i2.intValue());
        
        // -- wrapper and optional
        BlinkOutput.writeInt32Null(out, foo.i32Obj);
        
        
        // -- static group field (optional)
        Bar bar1 = foo.bar;
        if (bar1 == null) {
            BlinkOutput.writeBoolean(out, false);
        } else {
            BlinkOutput.writeBoolean(out, true);
            writeStaticGroup(out, bar1);
        }
        
        // -- static group field (required)
        Bar bar2 = foo.bar;
        if (bar2 == null) {
            throw new IllegalArgumentException("Required field 'Foo.bar' is null");
        }
        writeStaticGroup(out, bar2);
        
        // -- dynamic group (optional)
        Bar bar3 = foo.bar;
        if (bar3 == null) {
            BlinkOutput.writeNull(null);
        } else {
            writeDynamicGroup(out, bar3);
        }

        // -- dynamic group (required)
        Bar bar4 = foo.bar;
        if (bar4 == null) {
            throw new IllegalArgumentException("Required field 'Foo.bar' is null");
        }
        writeDynamicGroup(out, bar4);
        
        // -- accessor to int.class
        int i3 = ((Integer)accessor1.getValue(foo)).intValue();

        // -- accessor to Integer.class
        Integer i4 = (Integer)accessor2.getValue(foo);

        // -- sequence
        long[] sequence = null;
        int len = sequence.length;
        BlinkOutput.writeUInt32(out, len);
        for (int i=0; i<len; i++) {
            BlinkOutput.writeUInt64(out, sequence[i]);
        }

        // -- sequence2
        List<Long> sequence2 = null;
        int len2 = sequence2.size();
        BlinkOutput.writeUInt32(out, len2);
        for (long val : sequence2) {
            BlinkOutput.writeUInt64(out, val);
        }
    }

    Accessor accessor1; // Foo.fieldX
    Accessor accessor2; // Foo.fieldY
    
    void writeStaticGroup(OutputStream out, Bar bar) throws IOException {
        
    }
        
    @Id(123)
    public static class Foo {
        int i32;
        Integer i32Obj;
        Bar bar;
    }
    public static class Bar {
        Bar bar;
    }
    
    
}
