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
package com.cinnober.msgcodec.examples.upgrade;

import com.cinnober.msgcodec.Annotatable;
import com.cinnober.msgcodec.IncompatibleSchemaException;
import com.cinnober.msgcodec.MsgCodec;
import com.cinnober.msgcodec.MsgObject;
import com.cinnober.msgcodec.Schema;
import com.cinnober.msgcodec.SchemaBinder;
import com.cinnober.msgcodec.SchemaBinder.Direction;
import com.cinnober.msgcodec.SchemaBuilder;
import com.cinnober.msgcodec.anot.Annotate;
import com.cinnober.msgcodec.anot.Name;
import com.cinnober.msgcodec.anot.Required;
import com.cinnober.msgcodec.anot.Time;
import com.cinnober.msgcodec.anot.Unsigned;
import com.cinnober.msgcodec.blink.BlinkCodecFactory;
import com.cinnober.msgcodec.io.ByteArrayBuf;
import com.cinnober.msgcodec.io.ByteBuf;
import com.cinnober.msgcodec.messages.MetaProtocol;
import com.cinnober.msgcodec.messages.MetaSchema;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Example demonstrating the SchemaBinder in the context of
 * schema exchange and upgrades between client and server.
 *
 * @author mikael.brannstrom
 */
public class ProtocolUpgradeExample {

    public static void main(String[] args) throws Exception {

        ProtocolUpgradeExample example = new ProtocolUpgradeExample();

        // exchange protocols
        example.handshake(); // <-- have a look inside

        // client sends its first request
        CreateUserReq1 cliReq1 = new CreateUserReq1(123, new User1("Bob", 40));
        example.clientSend(cliReq1);

        // server see the version 2 message, and responds
        CreateUserReq2 srvReq1 = (CreateUserReq2) example.serverReceive();
        CreateUserRsp2 srvRsp1 = new CreateUserRsp2(srvReq1.requestId, srvReq1.user, 666);
        example.serverSend(srvRsp1);

        // client receives the response, in version 1
        @SuppressWarnings("unused")
		CreateUserRsp1 cliRsp1 = (CreateUserRsp1) example.clientReceive();

        // server creates a version 2 response of some request2
        GetUserRsp2 srvRsp2 = new GetUserRsp2(456, 666, new User2("Bob", "bob@example.com", 0));
        example.serverSend(srvRsp2);

        // client recieves an unknown response, it is however mapped to the best known super class
        @SuppressWarnings("unused")
		Response1 cliRsp2 = (Response1) example.clientReceive();
    }

    /** Codec used for both client and server for encoding/decoding the meta (handshake) protocol. */
    private final MsgCodec metaCodec;
    /** Buffer used for communication between client and server. */
    private final ByteBuf buf;

    /** The schema the server was built with. */
    private final Schema serverSchema;
    /** Server side codec. */
    private final MsgCodec serverCodec;
    
    /** The schema the client was built with. */
    private final Schema clientSchema;
    /** The client side schema after handshake. */
    private Schema upgradedClientSchema;
    /** Client side codec, after handshake, using the upgradedClientSchema. */
    private MsgCodec clientCodec;
    /** Create a message codec for the specified schema. */
    private Function<Schema, MsgCodec> codecFactory = s -> new BlinkCodecFactory(s).createCodec();

    public ProtocolUpgradeExample() {
        metaCodec = codecFactory.apply(MetaProtocol.getSchema());
        buf = new ByteArrayBuf(new byte[1000_000]);

        serverSchema = new SchemaBuilder().addMessages(
                Request2.class,
                Response2.class,
                User2.class,
                CreateUserReq2.class,
                CreateUserRsp2.class,
                GetUserReq2.class,
                GetUserRsp2.class).build().assignGroupIds();
        serverCodec = codecFactory.apply(serverSchema);

        clientSchema = new SchemaBuilder().addMessages(
                Request1.class,
                Response1.class,
                User1.class,
                CreateUserReq1.class,
                CreateUserRsp1.class).build().assignGroupIds();
    }

    public void handshake() throws IOException, IncompatibleSchemaException {
        // server sends its schema
        metaCodec.encode(serverSchema.toMessage(), buf);
        buf.flip();

        // client decodes and upgrades
        MetaSchema schemaMessage = (MetaSchema) metaCodec.decode(buf);
        upgradedClientSchema = new SchemaBinder(clientSchema).bind(
                schemaMessage.toSchema(),
                ProtocolUpgradeExample::getClientDir);
        clientCodec = codecFactory.apply(upgradedClientSchema);
    }

    private static Direction getClientDir(Annotatable<?> a) {
        String s = a.getAnnotation("dir");
        if (s == null) {
            return Direction.BOTH;
        }
        switch (s) {
            case "c2s":
                return Direction.OUTBOUND;
            case "s2c":
                return Direction.INBOUND;
            default:
                return Direction.BOTH;
        }
    }

    public void clientSend(Object msg) throws IOException {
        send("CLI", clientCodec, msg);
    }
    public Object clientReceive() throws IOException {
        return receive("CLI", clientCodec);
    }
    public void serverSend(Object msg) throws IOException {
        send("SRV", serverCodec, msg);
    }
    public Object serverReceive() throws IOException {
        return receive("SRV", serverCodec);
    }
    private void send(String side, MsgCodec codec, Object msg) throws IOException {
        System.out.println(side + " send " + msg);
        buf.clear();
        codec.encode(msg, buf);
        buf.flip();
    }
    private Object receive(String side, MsgCodec codec) throws IOException {
        Object msg = codec.decode(buf);
        System.out.println(side + " recv " + msg);
        return msg;
    }


    // MESSAGES IN PROTOCOL VERSION 1

    @Name("Request")
    @Annotate("dir=c2s")
    public static class Request1 extends MsgObject {
        @Unsigned
        public long requestId;
    }
    
    @Name("Response")
    @Annotate("dir=s2c")
    public static class Response1 extends MsgObject {
        @Unsigned
        public long requestId;
    }

    @Name("User")
    @Annotate("dir=both")
    public static class User1 extends MsgObject {
        @Required
        public String name;

        public Integer age; // removed later on

        public User1() {}

        public User1(String name, Integer age) {
            this.name = name;
            this.age = age;
        }
    }

    @Name("CreateUserReq")
    @Annotate("dir=c2s")
    public static class CreateUserReq1 extends Request1 {
        @Required
        public User1 user;
        public CreateUserReq1() {}

        public CreateUserReq1(long requestId, User1 user) {
            this.requestId = requestId;
            this.user = user;
        }
    }

    @Name("CreateUserRsp")
    public static class CreateUserRsp1 extends Response1 {
        @Required
        public User1 user;
    }


    // MESSAGES IN PROTOCOL VERSION 2
    
    @Name("Request")
    @Annotate("dir=c2s")
    public static class Request2 extends MsgObject {
        @Unsigned
        public long requestId;
    }

    @Name("Response")
    @Annotate("dir=s2c")
    public static class Response2 extends MsgObject {
        @Unsigned
        public long requestId;
    }

    @Name("User")
    @Annotate("dir=both")
    public static class User2 extends MsgObject {
        @Required
        public String name;
        public String email; // added
        @Time(unit = TimeUnit.DAYS)
        public Integer birthday; // added

        public User2() {}

        public User2(String name, String email, Integer birthday) {
            this.name = name;
            this.email = email;
            this.birthday = birthday;
        }
    }

    @Name("CreateUserReq")
    @Annotate("dir=c2s")
    public static class CreateUserReq2 extends Request2 {
        @Required
        public User2 user;
    }

    @Name("CreateUserRsp")
    @Annotate("dir=s2c")
    public static class CreateUserRsp2 extends Response2 {
        @Required
        public User2 user;
        @Required
        public long userId; // added

        public CreateUserRsp2() {}

        public CreateUserRsp2(long requestId, User2 user, long userId) {
            this.requestId = requestId;
            this.user = user;
            this.userId = userId;
        }
    }

    @Name("GetUserReq")
    @Annotate("dir=c2s")
    public static class GetUserReq2 extends Request2 { // added
        @Unsigned
        public long userId;
    }

    @Name("GetUserRsp")
    @Annotate("dir=s2c")
    public static class GetUserRsp2 extends Response2 { // added
        @Unsigned
        public long userId;
        @Required
        public User2 user;

        public GetUserRsp2() {}

        public GetUserRsp2(long requestId, long userId, User2 user) {
            this.requestId = requestId;
            this.userId = userId;
            this.user = user;
        }
    }

}
