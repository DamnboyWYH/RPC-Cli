package Codec;

import Serializer.Protostuff.ProtostuffSerializer;
import Serializer.Serializer;
import customProtocol.RPCConstant;
import customProtocol.RPCMessage;
import customProtocol.RPCRequest;
import customProtocol.RPCResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class DeCoder extends LengthFieldBasedFrameDecoder {
    public DeCoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
    }
    public DeCoder() {
        // lengthFieldOffset: magic code is 6B, and then full length. so value is 6
        // lengthFieldLength: full length is 4B. so value is 4
        // lengthAdjustment: full length include all data and read 10 bytes before, so the left length is (fullLength-10). so values is -10
        // initialBytesToStrip: we will check magic code and version manually, so do not strip any bytes. so values is 0
        this(RPCConstant.MAX_FRAME_LENGTH, 6, 4, -10, 0);
    }
    private Serializer serializer = new ProtostuffSerializer<>();

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        Object decoded = super.decode(ctx, in);
        if(decoded instanceof ByteBuf){
            ByteBuf frame = (ByteBuf) decoded;
            if(frame.readableBytes() >= RPCConstant.Head_Length){
                try{
                    return decodeMessage(frame);
                }finally {
                    frame.release();
                }
            }
        }
        return decoded;
    }
    private Object decodeMessage(ByteBuf in){
        if(!check(in)){
            throw new IllegalArgumentException("Unknown magic code");
        }
        int fullLength = in.readInt();//读取消息总长度
        int ID = in.readInt();//读取消息ID
        byte messageType = in.readByte();//读取消息类型
        byte messageCodec = in.readByte();//读取消息编解码方式
        RPCMessage message = new RPCMessage();
        message.setMessageType(messageType);
        message.setCodec(messageCodec);
        message.setRequestID(ID);
        if(messageType == RPCConstant.HEART_BEAT_REQUEST_TYPE || messageType == RPCConstant.HEART_BEAT_RESPONSE_TYPE){
            return message;
        }
        int bodyLength = fullLength - RPCConstant.Head_Length;
        if(bodyLength > 0){
            byte[] bs = new byte[bodyLength];
            in.readBytes(bs);
            if(messageType == RPCConstant.REQUEST_TYPE){
                RPCRequest request = serializer.DeSerialize(bs,RPCRequest.class);
                message.setRequest(request);
            }else{
                RPCResponse response = serializer.DeSerialize(bs,RPCResponse.class);
                message.setResponse(response);
            }
        }
        return message;
    }
    private boolean check(ByteBuf in){//需要先check一下
        int len = RPCConstant.Magic_Number.length;
        byte[] input = new byte[len];
        in.readBytes(input);
        for(int i = 0; i < len; i++){
            if(input[i] != RPCConstant.Magic_Number[i]){
                return false;
            }
        }
        return true;
    }
}
