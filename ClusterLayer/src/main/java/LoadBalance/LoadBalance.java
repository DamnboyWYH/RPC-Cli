package LoadBalance;

import ExtensionSPIImpl.ExtensionSPI;
import customProtocol.RPCRequest;

import java.util.List;

@ExtensionSPI
public interface LoadBalance {
    String selectServiceAddress(List<String> serviceAddresses, RPCRequest rpcRequest);//这里我们拿到的就是host与port
}
