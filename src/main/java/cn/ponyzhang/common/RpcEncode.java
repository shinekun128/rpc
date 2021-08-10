package cn.ponyzhang.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RpcEncode extends MessageToByteEncoder {

    private static final Logger logger = LoggerFactory.getLogger(RpcEncode.class);
    private Class<?> genericClass;
    private Serializer serializer;

    public RpcEncode(Class<?> genericClass,Serializer serializer){
        this.genericClass = genericClass;
        this.serializer = serializer;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if (genericClass.isInstance(msg)) {
            try {
                byte[] data = serializer.serialize(msg);
                out.writeInt(data.length);
                out.writeBytes(data);
            } catch (Exception ex) {
                logger.error("Encode error: " + ex.toString());
            }
        }
    }
}
