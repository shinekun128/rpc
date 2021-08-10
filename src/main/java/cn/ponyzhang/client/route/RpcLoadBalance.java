package cn.ponyzhang.client.route;

import cn.ponyzhang.client.RpcClientHandler;
import cn.ponyzhang.common.RpcProtocol;
import cn.ponyzhang.common.RpcServiceInfo;
import cn.ponyzhang.common.ServiceUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class RpcLoadBalance {
    protected Map<String, List<RpcProtocol>> getServiceMap(Map<RpcProtocol, RpcClientHandler> connectedServerNodes){
        Map<String, List<RpcProtocol>> serviceMap = new HashMap<>();
        if(connectedServerNodes!=null && connectedServerNodes.size()>0){
            for(RpcProtocol protocol : connectedServerNodes.keySet()){
                for(RpcServiceInfo serviceInfo : protocol.getServiceInfoList()){
                    String serviceKey = ServiceUtil.makeServiceKey(serviceInfo.getServiceName(), serviceInfo.getVersion());
                    List<RpcProtocol> rpcProtocols = serviceMap.get(serviceKey);
                    if(rpcProtocols == null){
                        rpcProtocols = new ArrayList<>();
                    }
                    rpcProtocols.add(protocol);
                    serviceMap.put(serviceKey,rpcProtocols);
                }
            }
        }
        return serviceMap;
    }
    // Route the connection for service key
    public abstract RpcProtocol route(String serviceKey, Map<RpcProtocol, RpcClientHandler> connectedServerNodes) throws Exception;
}
