package Interface;


import customProtocol.RPCMessage;
import customProtocol.RPCRequest;
import customProtocol.RPCResponse;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

public interface Transport {
    CompletableFuture<RPCResponse> sendMessage(RPCMessage mes, InetSocketAddress inetSocketAddress);
}
