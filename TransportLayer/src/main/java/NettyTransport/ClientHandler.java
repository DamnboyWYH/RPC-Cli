package NettyTransport;

import Factory.SingletonFactory;
import customProtocol.RPCConstant;
import customProtocol.RPCMessage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;

@Slf4j
public class ClientHandler extends ChannelInboundHandlerAdapter {
    private final NettyBootStrap boot;

    public ClientHandler() {
        this.boot = SingletonFactory.getInstance(NettyBootStrap.class);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {//当接收到服务器端的
        if(msg instanceof RPCMessage){
            RPCMessage message = (RPCMessage) msg;
            if(message.getMessageType() == RPCConstant.HEART_BEAT_RESPONSE_TYPE){//返回的是心跳包
                log.info("心跳包返回");
            }else if(message.getMessageType() == RPCConstant.RESPONSE_TYPE){//当接收到来自服务器端的返回数据的时候，就从未完成队列里面取出值完成
                UnSolvedMessage.complete(message.getResponse().getRequestID(),message.getResponse());
            }
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            IdleStateEvent event = (IdleStateEvent) evt;
            if(event.state() == IdleState.WRITER_IDLE){
                Channel channel = boot.getChannel((InetSocketAddress) ctx.channel().remoteAddress());
                RPCMessage mes = new RPCMessage();
                mes.setCodec(RPCConstant.Protostuff);
                mes.setMessageType(RPCConstant.HEART_BEAT_REQUEST_TYPE);
                channel.writeAndFlush(mes);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("与服务器断开连接");
    }
}
