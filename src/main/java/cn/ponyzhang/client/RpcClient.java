package cn.ponyzhang.client;

import cn.ponyzhang.client.proxy.ObjectProxy;
import cn.ponyzhang.client.proxy.RpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class RpcClient {
    private static final Logger logger = LoggerFactory.getLogger(RpcClient.class);

    private ServiceDiscovery serviceDiscovery;
    private static ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(16, 16,
            600L, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(1000));

    public RpcClient(String address)  {
        this.serviceDiscovery = new ServiceDiscovery(address);
    }

    @SuppressWarnings("unchecked")
    public static <T,P> T createService(Class<T> interfaceClass , String version){
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new ObjectProxy<T,P>(interfaceClass,version)
        );
    }

    public static <T, P> RpcService createAsyncService(Class<T> interfaceClass, String version) {
        return new ObjectProxy<T, P>(interfaceClass, version);
    }

    public static void submit(Runnable task) {
        threadPoolExecutor.submit(task);
    }

    public void stop() throws InterruptedException {
        threadPoolExecutor.shutdown();
        serviceDiscovery.stop();
        ConnectionManager.getInstance().stop();
    }
}
