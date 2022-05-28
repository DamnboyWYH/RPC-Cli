package customProtocol;


import Common.Body;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

@Data
public class RPCResponse<T> implements Body {
    private static final long serialVersionUID = 715745410605631233L;
    @JSONField(name = "RequestID")
    private String RequestID;//消息ID
    @JSONField(name = "code")
    private Integer code;//状态码
    @JSONField(name = "backMessage")
    private String backMessage;//返回的消息
    @JSONField(name = "data")
    private T data;//计算的结果


    public static <T> RPCResponse<T> success(T data, String requestID){
        RPCResponse<T> response = new RPCResponse<>();
        response.RequestID = requestID;
        response.code = RpcResponseCodeEnum.SUCCSEE.getCode();
        response.backMessage = RpcResponseCodeEnum.SUCCSEE.getMessage();
        if(null != data){
            response.data = data;
        }
        return response;
    }

    public static <T> RPCResponse<T> fail(String requestID){
        RPCResponse<T> response = new RPCResponse<>();
        response.RequestID = requestID;
        response.code = RpcResponseCodeEnum.FAIL.getCode();
        response.backMessage = RpcResponseCodeEnum.FAIL.getMessage();
        response.data = null;
        return response;
    }
}
