package customProtocol;

public class RPCConstant {
    public static final byte[] Magic_Number = {(byte) 'W',(byte) 'Y',(byte) 'H',(byte) 'R',(byte) 'P',(byte) 'C'};

    public static final byte Head_Length = 16;

    public static final int MAX_FRAME_LENGTH = 8 * 1024 * 1024;

    public static final byte REQUEST_TYPE = 1;
    public static final byte RESPONSE_TYPE = 2;
    public static final byte HEART_BEAT_REQUEST_TYPE = 3;
    public static final byte HEART_BEAT_RESPONSE_TYPE = 4;

    public static final String Ping = "ping!";
    public static final String Pong = "pong!";

    public static final byte Protostuff = 1;
}
