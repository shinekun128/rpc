package cn.ponyzhang.server;

import cn.ponyzhang.common.ServiceUtil;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class NettyServer extends Server{

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);
    private Thread thread;
    private ServiceRegistry serviceRegistry;
    private String serverAddress;
    private Map<String,Object> serviceMap = new HashMap<>();

    public NettyServer(String serverAddress, String registerAddress) {
        this.serviceRegistry = new ServiceRegistry(registerAddress);
        this.serverAddress = serverAddress;
    }

    public void addService(String interfaceName,String version,Object serviceBean){
        logger.info("Adding service, interface: {}, version: {}, bean：{}", interfaceName, version, serviceBean);
        String serviceKey = ServiceUtil.makeServiceKey(interfaceName,version);
        serviceMap.put(serviceKey,serviceBean);
    }

    @Override
    public void start() throws Exception {
        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                EventLoopGroup bossGroup = new NioEventLoopGroup(1);
                EventLoopGroup workerGroup = new NioEventLoopGroup();
                try {
                    ServerBootstrap bootstrap = new ServerBootstrap();
                    bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                            .childHandler(new RpcServerInitializer(serviceMap))
                            .option(ChannelOption.SO_BACKLOG, 128)
                            .childOption(ChannelOption.SO_KEEPALIVE, true);

                    String[] array = serverAddress.split(":");
                    String host = array[0];
                    int port = Integer.parseInt(array[1]);
                    ChannelFuture future = bootstrap.bind(host, port).sync();

                    if (serviceRegistry != null) {
                        serviceRegistry.register(host, port, serviceMap);
                    }
                    logger.info("Server started on port {}", port);
                    future.channel().closeFuture().sync();
                } catch (Exception e) {
                    if (e instanceof InterruptedException) {
                        logger.info("Rpc server remoting server stop");
                    } else {
                        logger.error("Rpc server remoting server error", e);
                    }
                } finally {
                    try {
                        serviceRegistry.unregisterService();
                        workerGroup.shutdownGracefully();
                        bossGroup.shutdownGracefully();
                    } catch (Exception ex) {
                        logger.error(ex.getMessage(), ex);
                    }
                }
            }
        });
        thread.start();
    }

    @Override
    public void stop() throws Exception {
        if(thread!=null && thread.isAlive()){
            thread.interrupt();
        }
    }
}
