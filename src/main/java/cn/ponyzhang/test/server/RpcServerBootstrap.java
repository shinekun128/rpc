package cn.ponyzhang.test.server;

import cn.ponyzhang.server.NettyServer;
import cn.ponyzhang.test.service.*;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class RpcServerBootstrap {
    private static final Logger logger = LoggerFactory.getLogger(RpcServerBootstrap.class);

    public static void main(String[] args)  {
        String serverAddress = "127.0.0.1:18877";
        String registryAddress = "47.94.199.172:2181";
        NettyServer server = new NettyServer(serverAddress, registryAddress);
        HelloService helloService1 = new HelloServiceImpl();
        server.addService(HelloService.class.getName(), "1.0", helloService1);
        HelloService helloService2 = new HelloServiceImpl2();
        server.addService(HelloService.class.getName(), "2.0", helloService2);
        PersonService personService = new PersonServiceImpl();
        server.addService(PersonService.class.getName(), "", personService);
        try {
            server.start();
        } catch (Exception e) {
            logger.error("server start Exception: {}", e.toString());
        }
    }
}
