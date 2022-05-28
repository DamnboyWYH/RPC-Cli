package Exception;

public class RpcException extends RuntimeException{
    public RpcException(String msg,RpcErrorMessageEnum rpcErrorMessageEnum){
        super(rpcErrorMessageEnum.getMessage() + ":" + msg);
    }
    public RpcException(){

    }
}
