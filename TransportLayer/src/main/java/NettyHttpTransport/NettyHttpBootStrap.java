package NettyHttpTransport;

import Interface.Transport;
import customProtocol.RPCMessage;
import customProtocol.RPCResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.CompletableFuture;

public class NettyHttpBootStrap implements Transport {
    public static void main(String[] args) {
        NettyHttpBootStrap bootStrap = new NettyHttpBootStrap();
        bootStrap.start();
    }
    private void start(){
        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer());
            try {
                ChannelFuture f = bootstrap.connect("127.0.0.1", 10000).sync();
                f.channel().closeFuture().sync();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        } finally {
            eventLoopGroup.shutdownGracefully();
        }
    }



    @Override
    public CompletableFuture<RPCResponse> sendMessage(RPCMessage mes, InetSocketAddress inetSocketAddress) {
        return null;
    }
}
