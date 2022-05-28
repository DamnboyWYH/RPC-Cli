package ZK;

import customProtocol.RPCRequest;

import java.net.InetSocketAddress;

public interface ZkServiceDiscovery {
    InetSocketAddress lookupService(RPCRequest rpcRequest);
}
