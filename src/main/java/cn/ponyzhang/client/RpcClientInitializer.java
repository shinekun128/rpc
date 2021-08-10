package cn.ponyzhang.client;

import cn.ponyzhang.common.*;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class RpcClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        Serializer serializer = new Serializer();
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new IdleStateHandler(0,0, Beat.BEAT_INTERVAL, TimeUnit.SECONDS));
        pipeline.addLast(new RpcEncode(RpcRequest.class, serializer));
        pipeline.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
        pipeline.addLast(new RpcDecode(RpcResponse.class, serializer));
        pipeline.addLast(new RpcClientHandler());
    }
}
