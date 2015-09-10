package com.cinnober.msgcodec.blink;

import com.cinnober.msgcodec.IncompatibleSchemaException;
import com.cinnober.msgcodec.MsgCodecFactory;
import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.SchemaBinder;
import com.cinnober.msgcodec.SchemaBuilder;
import com.cinnober.msgcodec.io.ByteBuffers;
import com.cinnober.msgcodec.test.upgrade.UpgradeMissingGroupDefs;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import static org.junit.Assert.assertEquals;

/**
 * ToDo: Write description here.
 *
 * @author Morgan Johansson, Cinnober Financial Technology North AB
 */
public class TempTestRemoveMe {
    @Test
    public void testBrokenEncoding() throws IncompatibleSchemaException, IOException {
        Schema schemaWithoutExtraField = new SchemaBuilder().build(UpgradeMissingGroupDefs.BaseGroupV1.class);
        Schema schemaWithExtraField = new SchemaBuilder().build(UpgradeMissingGroupDefs.BaseGroupV2.class,
                UpgradeMissingGroupDefs.SubGroup.class);

        Schema upgradeOutSchema = new SchemaBinder(schemaWithoutExtraField).bind(schemaWithExtraField,
                (e) -> SchemaBinder.Direction.OUTBOUND);


        UpgradeMissingGroupDefs.BaseGroupV1 entityV1 = new UpgradeMissingGroupDefs.BaseGroupV1(15);
        UpgradeMissingGroupDefs.BaseGroupV2 entityV2 = new UpgradeMissingGroupDefs.BaseGroupV2(15);
        entityV2.sub = null;


        System.err.println("ORIGINAL: "+encodeToHex(entityV1, schemaWithoutExtraField));
        System.err.println("ORIGINAL V2: "+encodeToHex(entityV2, schemaWithExtraField));
        System.err.println("UPGRADED: "+encodeToHex(entityV1, upgradeOutSchema));

        assertEquals(encodeToHex(entityV2, schemaWithExtraField), encodeToHex(entityV1, upgradeOutSchema));
    }

    private String encodeToHex(Object entity, Schema schema) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        MsgCodecFactory factory = new BlinkCodecFactory(schema);
        factory.createCodec().encode(entity, out);
        return ByteBuffers.toHex(ByteBuffer.wrap(out.toByteArray()));
    }
}
