package cn.ponyzhang.client;

public interface AsyncRpcCallback {

    void success(Object result);

    void fail(Exception e);
}
