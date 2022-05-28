package NettyTransport;

import Codec.DeCoder;
import Codec.Encoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

public class ClientChannelInitializer extends ChannelInitializer<SocketChannel>  {

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new DeCoder());
        pipeline.addLast(new Encoder());
        pipeline.addLast(new IdleStateHandler(0, 10, 0, TimeUnit.SECONDS));//心跳控制器
        pipeline.addLast(new ClientHandler());
    }
}
