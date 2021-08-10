package cn.ponyzhang.test.client;


import cn.ponyzhang.client.AsyncRpcCallback;
import cn.ponyzhang.client.RpcClient;
import cn.ponyzhang.client.RpcFuture;
import cn.ponyzhang.client.proxy.RpcService;
import cn.ponyzhang.test.service.Person;
import cn.ponyzhang.test.service.PersonService;

import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * Created by luxiaoxun on 2016/3/17.
 */
public class RpcCallbackTest {
    public static void main(String[] args) throws InterruptedException {
        final RpcClient rpcClient = new RpcClient("127.0.0.1:2181");
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        try {
            RpcService client = rpcClient.createAsyncService(PersonService.class, "");
            int num = 5;
            RpcFuture helloPersonFuture = client.call("callPerson", "Jerry", num);
            helloPersonFuture.addCallBack(new AsyncRpcCallback() {
                @Override
                public void success(Object result) {
                    List<Person> persons = (List<Person>) result;
                    for (int i = 0; i < persons.size(); ++i) {
                        System.out.println(persons.get(i));
                    }
                    countDownLatch.countDown();
                }

                @Override
                public void fail(Exception e) {
                    System.out.println(e);
                    countDownLatch.countDown();
                }
            });

        } catch (Exception e) {
            System.out.println(e);
        }

        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        rpcClient.stop();

        System.out.println("End");
    }
}
