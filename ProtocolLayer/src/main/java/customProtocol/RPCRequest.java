package customProtocol;

import Common.Body;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.Arrays;

@lombok.Data
@NoArgsConstructor
@AllArgsConstructor
public class RPCRequest implements Body {
    private static final long serialVersionUID = 1905122041950251207L;
    @JSONField(name = "requestID")
    private String requestID;//内部每一次发送的ID
    @JSONField(name = "InterfaceName")
    private String InterfaceName;//接口名
    @JSONField(name = "MethodName")
    private String MethodName;//请求的方法名
    @JSONField(name = "ParmType")
    private Class<?>[] ParmType;//参数类型列表
    @JSONField(name = "args")
    private Object[] args;//参数列表


    @Override
    public String toString() {
        return "RPCRequest{" +
                "requestID='" + requestID + '\'' +
                ", InterfaceName='" + InterfaceName + '\'' +
                ", MethodName='" + MethodName + '\'' +
                ", ParmType=" + Arrays.toString(ParmType) +
                ", args=" + Arrays.toString(args) +
                '}';
    }
}
