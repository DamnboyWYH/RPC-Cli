package LoadBalance;

import customProtocol.RPCRequest;

import java.util.List;

public abstract class AbstractLoadBalance implements LoadBalance{

    public String selectServiceAddress(List<String> serviceAddresses, RPCRequest rpcRequest) {
        if (serviceAddresses == null || serviceAddresses.size() == 0) {
            return null;
        }
        if (serviceAddresses.size() == 1) {
            return serviceAddresses.get(0);
        }
        return doSelect(serviceAddresses, rpcRequest);
    }

    protected abstract String doSelect(List<String> serviceAddresses, RPCRequest rpcRequest);

}
