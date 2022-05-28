package NettyTransport;


import Factory.SingletonFactory;
import HealthMonitor.HealthMonitor;
import Interface.Transport;
import customProtocol.RPCMessage;
import customProtocol.RPCRequest;
import customProtocol.RPCResponse;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;

@Slf4j
public class NettyBootStrap implements Transport {
    private final Bootstrap ClientBootstrap;
    private final Map<InetSocketAddress,Channel> addressToChannel = new ConcurrentHashMap<>();
    private final HealthMonitor healthMonitor;//在整个RPC初始化的时候就要开始初始化
    public NettyBootStrap() {
        EventLoopGroup eventGroup = new NioEventLoopGroup();
        ClientBootstrap = new Bootstrap();
        ClientBootstrap.group(eventGroup)//设置用于处理所有事件的EventLoopGroup
                .channel(NioSocketChannel.class)//指定对应的channel实现类
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000)//设置channel的Option
                .handler(new ClientChannelInitializer());//绑定对应的channelpipeline
        healthMonitor = SingletonFactory.getInstance(HealthMonitor.class);
    }

    protected Channel getChannel(InetSocketAddress inetSocketAddress){//从缓存中获取channel，如果没有，那么执行doConnect操作
        Channel channel = null;
        if(addressToChannel.containsKey(inetSocketAddress)){
            return addressToChannel.get(inetSocketAddress);
        }else{
            try {
                channel = doConnect(inetSocketAddress);
                addressToChannel.put(inetSocketAddress,channel);
                healthMonitor.newChannelHealthMonitor(channel);
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
            return channel;
        }
    }
    private Channel doConnect(InetSocketAddress inetAddress) throws ExecutionException, InterruptedException {
        //这里是连接,连接到服务器的过程
        CompletableFuture<Channel> complete = new CompletableFuture<>();
        ClientBootstrap.connect(inetAddress).addListener((ChannelFutureListener) future -> {
            if(future.isSuccess()){
                log.info("The client has connected [{}] successful!", inetAddress.toString());
                complete.complete(future.channel());
            }else{
                throw new Exception();
            }
        });
        return complete.get();
    }

    @Override
    public  CompletableFuture<RPCResponse> sendMessage(RPCMessage mes, InetSocketAddress inetSocketAddress){//异步操作必须用future来做
        Channel channel = getChannel(inetSocketAddress);
        String requestID = mes.getRequest().getRequestID();
        CompletableFuture<RPCResponse> completableFuture = new CompletableFuture<>();
        if(channel.isActive()){
            UnSolvedMessage.putMessage(requestID,completableFuture);//这里就先将没有完成的数据放到待做里面
            channel.writeAndFlush(mes).addListener((ChannelFutureListener)future ->{
                if(future.isSuccess()){
                    log.info("client send message: [{}]", mes);
                }else{
                    log.error("Send failed:", future.cause());
                }
            });
        }else{//关闭了就重新连接
            try {
                channel = doConnect(inetSocketAddress);

                channel.writeAndFlush(mes);

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }
        return completableFuture;
    }

}
