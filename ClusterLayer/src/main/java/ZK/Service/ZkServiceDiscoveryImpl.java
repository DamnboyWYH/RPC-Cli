package ZK.Service;

import Exception.*;
import ExtensionSPIImpl.ExtensionLoader;
import LoadBalance.DefaultLoadBalance;
import LoadBalance.LoadBalance;
import LoadBalance.ConsistHashLoadBalance;
import ZK.Util.CuratorUtils;
import ZK.ZkServiceDiscovery;
import customProtocol.RPCRequest;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Value;

import java.net.InetSocketAddress;
import java.util.List;

/*
 *目的是为了实现服务发现的功能，先要从zookeeper中获取到我们的服务地址，随后通过负载均衡均衡一下，然后才获得最终的服务地址。
 */
public class ZkServiceDiscoveryImpl implements ZkServiceDiscovery {
    private LoadBalance loadBalance;
    private CuratorFramework zkClient;
    private static final String loadBalanceName = "ConsistHashLoadBalance";

    public ZkServiceDiscoveryImpl(CuratorFramework zkClient) {
        this.zkClient = zkClient;
    }

    @Override
    public InetSocketAddress lookupService(RPCRequest rpcRequest) {
        LoadBalance loader = ExtensionLoader.getExtensionLoader(LoadBalance.class,new DefaultLoadBalance())
                .getExtension(loadBalanceName);
        String serviceName = rpcRequest.getMethodName();
        zkClient = CuratorUtils.getZkClient();
        List<String> serviceList = CuratorUtils.getChildrenNode(zkClient,serviceName);
        if(serviceList == null || serviceList.size() == 0){
            throw new RpcException(serviceName, RpcErrorMessageEnum.SERVICE_CAN_NOT_BE_FOUND);
        }
        String URL = loader.selectServiceAddress(serviceList,rpcRequest);
        InetSocketAddress address = null;
        String[] addr = URL.split(":");
        String host = addr[0];
        int port = Integer.parseInt(addr[1]);
        address = new InetSocketAddress(host,port);
        return address;
    }

}
