package cn.ponyzhang.server;

import cn.ponyzhang.common.Beat;
import cn.ponyzhang.common.RpcRequest;
import cn.ponyzhang.common.RpcResponse;
import cn.ponyzhang.common.ServiceUtil;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import net.sf.cglib.reflect.FastClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private static final Logger logger = LoggerFactory.getLogger(RpcServerHandler.class);

    private final Map<String, Object> handlerMap;

    public RpcServerHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {
        if(Beat.BEAT_ID.equalsIgnoreCase(msg.getRequestId())){
            logger.info("Server read heartbeat ping");
            return;
        }

        logger.info("Receive request " + msg.getRequestId());
        RpcResponse response = new RpcResponse();
        response.setRequestId(msg.getRequestId());
        try {
            Object result = handle(msg);
            response.setResult(result);
        } catch (Throwable t) {
            response.setError(t.toString());
            logger.error("RPC Server handle request error", t);
        }
        ctx.writeAndFlush(response).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                logger.info("Send response for request " + msg.getRequestId());
            }
        });
    }

    private Object handle(RpcRequest request) throws InvocationTargetException {
        String className = request.getClassName();
        String version = request.getVersion();
        String serviceKey = ServiceUtil.makeServiceKey(className, version);
        Object serviceBean = handlerMap.get(serviceKey);
        if (serviceBean == null) {
            logger.error("Can not find service implement with interface name: {} and version: {}", className, version);
            return null;
        }

        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        logger.debug(serviceClass.getName());
        logger.debug(methodName);

        for (int i = 0; i < parameterTypes.length; ++i) {
            logger.debug(parameterTypes[i].getName());
        }
        for (int i = 0; i < parameters.length; ++i) {
            logger.debug(parameters[i].toString());
        }

        //JDK reflect
//        Method method = serviceClass.getMethod(methodName, parameterTypes);
//        method.setAccessible(true);
//        Object result = method.invoke(serviceBean, parameterTypes);
        //cglib reflect
        FastClass fastClass = FastClass.create(serviceClass);
        int index = fastClass.getIndex(methodName, parameterTypes);
        return fastClass.invoke(index,serviceBean,parameters);

    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            ctx.channel().close();
            logger.warn("Channel idle in last {} seconds, close it", Beat.BEAT_TIMEOUT);
        }else{
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.warn("Server caught exception: " + cause.getMessage());
        ctx.close();
    }
}
