package cn.ponyzhang.client.route;

import cn.ponyzhang.client.RpcClientHandler;
import cn.ponyzhang.common.RpcProtocol;
import com.google.common.hash.Hashing;

import java.util.List;
import java.util.Map;

public class RpcLoadBalanceConsistentHash extends RpcLoadBalance{
    @Override
    public RpcProtocol route(String serviceKey, Map<RpcProtocol, RpcClientHandler> connectedServerNodes) throws Exception {
        Map<String, List<RpcProtocol>> serviceMap = getServiceMap(connectedServerNodes);
        List<RpcProtocol> addressList = serviceMap.get(serviceKey);
        if (addressList != null && addressList.size() > 0) {
            return doRoute(serviceKey, addressList);
        } else {
            throw new Exception("Can not find connection for service: " + serviceKey);
        }
    }

    public RpcProtocol doRoute(String serviceKey, List<RpcProtocol> addressList) {
        int index = Hashing.consistentHash(serviceKey.hashCode(), addressList.size());
        return addressList.get(index);
    }
}
