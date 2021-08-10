package cn.ponyzhang.common;

public interface Constant {
        int ZK_SESSION_TIMEOUT = 5000;
        int ZK_CONNECTION_TIMEOUT = 5000;
        String ZK_REGISTRY_PATH = "/register";
        String ZK_DATA_PATH = ZK_REGISTRY_PATH + "/data";

        String ZK_NAMESPACE = "netty-rpc";
}
