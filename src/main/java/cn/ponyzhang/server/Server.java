package cn.ponyzhang.server;

public abstract class Server {

    /**
     * start server
     * @throws Exception
     */
    public abstract void start() throws Exception;

    /**
     * stop server
     * @throws Exception
     */
    public abstract void stop() throws Exception;
}
