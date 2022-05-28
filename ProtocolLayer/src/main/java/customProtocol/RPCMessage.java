package customProtocol;

import com.alibaba.fastjson.annotation.JSONField;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RPCMessage{
    private static final long serialVersionUID = 71574541060563L;
    @JSONField(name = "MagicNumber")
    private byte[] MagicNumber;//6B
    @JSONField(name = "Length")
    private int Length;//4B
    @JSONField(name = "requestID")
    private int requestID;//4B，这里的ID是为了保序使用的ID
    @JSONField(name = "MessageType")
    private byte MessageType;//1B
    @JSONField(name = "Codec")
    private byte Codec;//1B
    @JSONField(name = "request")
    private RPCRequest request;
    @JSONField(name = "response")
    private RPCResponse response;


    @Override
    public String toString() {
        return "RPCMessage{" +
                "MagicNumber='" + MagicNumber + '\'' +
                ", Length=" + Length +
                ", requestID=" + requestID +
                ", MessageType=" + MessageType +
                ", Codec=" + Codec +
                ", request=" + request +
                ", response=" + response +
                '}';
    }
}
