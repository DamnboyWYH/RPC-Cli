package Codec;

import Serializer.Protostuff.ProtostuffSerializer;
import Serializer.Serializer;
import customProtocol.RPCConstant;
import customProtocol.RPCMessage;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.util.concurrent.atomic.AtomicInteger;

public class Encoder extends MessageToByteEncoder<RPCMessage> {
    private static final AtomicInteger ATOMIC_INTEGER = new AtomicInteger(0);
    private Serializer serializer = new ProtostuffSerializer<>();
    @Override
    protected void encode(ChannelHandlerContext ctx, RPCMessage msg, ByteBuf out) throws Exception {
        byte[] messageBody = null;
        if(msg.getMessageType() != RPCConstant.HEART_BEAT_REQUEST_TYPE ||
                msg.getMessageType() != RPCConstant.HEART_BEAT_RESPONSE_TYPE){
            serializer = new ProtostuffSerializer<>();
            if(msg.getRequest() != null){
                messageBody = serializer.Serialize(msg.getRequest());
            }else if(msg.getResponse() != null){
                messageBody = serializer.Serialize(msg.getResponse());
            }
        }
        int bodyLength = messageBody.length;
        int fullLength = RPCConstant.Head_Length + bodyLength;
        out.writeBytes(RPCConstant.Magic_Number);//写入魔数 6B
        out.writeInt(fullLength);//写入总长度
        out.writeInt(ATOMIC_INTEGER.getAndIncrement());//写入消息ID 4B
        out.writeByte(msg.getMessageType());//写入消息类型 1B
        out.writeByte(msg.getCodec());//写入编解码方式 1B
        if(messageBody != null){
            out.writeBytes(messageBody);
        }
    }
}
