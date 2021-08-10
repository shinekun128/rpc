package cn.ponyzhang.client.proxy;

import cn.ponyzhang.client.ConnectionManager;
import cn.ponyzhang.client.RpcClientHandler;
import cn.ponyzhang.client.RpcFuture;
import cn.ponyzhang.common.RpcRequest;
import cn.ponyzhang.common.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

public class ObjectProxy<T,P> implements InvocationHandler,RpcService<T, P, SerializableFunction<T>> {
    private static final Logger logger = LoggerFactory.getLogger(ObjectProxy.class);
    private Class<T> clazz;
    private String version;

    public ObjectProxy(Class<T> clazz, String version) {
        this.clazz = clazz;
        this.version = version;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(method.getDeclaringClass() == Object.class){
            String name = method.getName();
            if ("equals".equals(name)) {
                return proxy == args[0];
            } else if ("hashCode".equals(name)) {
                return System.identityHashCode(proxy);
            } else if ("toString".equals(name)) {
                return proxy.getClass().getName() + "@" +
                        Integer.toHexString(System.identityHashCode(proxy)) +
                        ", with InvocationHandler " + this;
            } else {
                throw new IllegalStateException(String.valueOf(method));
            }
        }
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(method.getDeclaringClass().getName());
        request.setMethodName(method.getName());
        request.setParameterTypes(method.getParameterTypes());
        request.setParameters(args);
        request.setVersion(version);

        // Debug
        if (logger.isDebugEnabled()) {
            logger.debug(method.getDeclaringClass().getName());
            logger.debug(method.getName());
            for (int i = 0; i < method.getParameterTypes().length; ++i) {
                logger.debug(method.getParameterTypes()[i].getName());
            }
            for (int i = 0; i < args.length; ++i) {
                logger.debug(args[i].toString());
            }
        }

        String serviceKey = ServiceUtil.makeServiceKey(method.getDeclaringClass().getName(), version);
        RpcClientHandler handler = ConnectionManager.getInstance().chooseHandler(serviceKey);
        RpcFuture rpcFuture = handler.sendRequest(request);
        return rpcFuture.get();
    }

    @Override
    public RpcFuture call(String funcName, Object... args) throws Exception {
        String serviceKey = ServiceUtil.makeServiceKey(this.clazz.getName(), version);
        RpcClientHandler handler = ConnectionManager.getInstance().chooseHandler(serviceKey);
        RpcRequest request = createRequest(this.clazz.getName(), funcName, args);
        RpcFuture rpcFuture = handler.sendRequest(request);
        return rpcFuture;
    }

    private RpcRequest createRequest(String className, String methodName, Object[] args) {
        RpcRequest request = new RpcRequest();
        request.setRequestId(UUID.randomUUID().toString());
        request.setClassName(className);
        request.setMethodName(methodName);
        request.setParameters(args);
        request.setVersion(version);
        Class[] parameterTypes = new Class[args.length];
        // Get the right class type
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = args[i].getClass();
        }
        request.setParameterTypes(parameterTypes);

        // Debug
        if (logger.isDebugEnabled()) {
            logger.debug(className);
            logger.debug(methodName);
            for (int i = 0; i < parameterTypes.length; ++i) {
                logger.debug(parameterTypes[i].getName());
            }
            for (int i = 0; i < args.length; ++i) {
                logger.debug(args[i].toString());
            }
        }

        return request;
    }

    @Override
    public RpcFuture call(SerializableFunction<T> tSerializableFunction, Object... args) throws Exception {
        String serviceKey = ServiceUtil.makeServiceKey(this.clazz.getName(), version);
        RpcClientHandler handler = ConnectionManager.getInstance().chooseHandler(serviceKey);
        RpcRequest request = createRequest(this.clazz.getName(), tSerializableFunction.getName(), args);
        RpcFuture rpcFuture = handler.sendRequest(request);
        return rpcFuture;
    }
}
