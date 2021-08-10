package cn.ponyzhang.common;



public class RpcServiceInfo {
    private String serviceName;
    private String version;

    public RpcServiceInfo(String serviceName,String version){
        this.serviceName = serviceName;
        this.version = version;
    }

    public RpcServiceInfo(){

    }
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
