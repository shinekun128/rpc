package cn.ponyzhang.server;

import cn.ponyzhang.common.*;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class RpcServerInitializer extends ChannelInitializer<SocketChannel>{

    private Map<String,Object> handlerMap;

    public RpcServerInitializer(Map<String,Object> handlerMap){
        this.handlerMap = handlerMap;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        Serializer serializer = new Serializer();
        pipeline.addLast(new IdleStateHandler(0,0, Beat.BEAT_TIMEOUT, TimeUnit.SECONDS));
        pipeline.addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0));
        pipeline.addLast(new RpcDecode(RpcRequest.class,serializer));
        pipeline.addLast(new RpcEncode(RpcResponse.class,serializer));
        pipeline.addLast(new RpcServerHandler(handlerMap));
    }
}
