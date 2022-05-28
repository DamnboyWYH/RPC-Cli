package customProtocol;

public enum RpcResponseCodeEnum {
    SUCCSEE(200,"成功！"),FAIL(400,"失败！");

    private final int code;
    private String message;

    RpcResponseCodeEnum(int code,String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
