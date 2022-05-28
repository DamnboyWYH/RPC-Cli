package NettyTransport;

import customProtocol.RPCResponse;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class UnSolvedMessage {
    private static final Map<String, CompletableFuture<RPCResponse>> messageStore = new ConcurrentHashMap<>();
    protected static void putMessage(String requestID, CompletableFuture<RPCResponse>response){
        messageStore.put(requestID,response);
    }
    public static void complete(String requestID,RPCResponse response){//异步处理必须依赖他
        if(messageStore.containsKey(requestID)){
            CompletableFuture<RPCResponse> future = messageStore.get(requestID);
            messageStore.remove(requestID);
            future.complete(response);
        }else{
            throw new IllegalStateException();
        }
    }
}
