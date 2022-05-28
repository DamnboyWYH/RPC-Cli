package customProtocol;

public enum RPCMessageTypeEnum {
    RPCRequestMes("RPCRequestMesType"),RPCResponseMes("RPCResponseMesType"),RPCHeartBeat("RPCHeartBeatMesType");
    private String Type;

    RPCMessageTypeEnum(String type) {
        Type = type;
    }
}
