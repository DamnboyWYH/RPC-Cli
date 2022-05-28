package LoadBalance;

import customProtocol.RPCRequest;

import java.util.List;
import java.util.Random;

public class DefaultLoadBalance extends AbstractLoadBalance{
    protected String doSelect(List<String> serviceAddresses, RPCRequest rpcRequest) {
        Random random = new Random();
        return serviceAddresses.get(random.nextInt(serviceAddresses.size()));
    }
}
