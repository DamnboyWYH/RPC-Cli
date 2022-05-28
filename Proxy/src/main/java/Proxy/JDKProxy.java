package Proxy;


import ExtensionSPIImpl.ExtensionLoader;
import Factory.SingletonFactory;
import Interface.Transport;
import LoadBalance.*;
import NettyTransport.NettyBootStrap;
import ZK.Service.ZkServiceDiscoveryImpl;
import ZK.Util.CuratorUtils;
import customProtocol.RPCConstant;
import customProtocol.RPCMessage;
import customProtocol.RPCRequest;
import customProtocol.RPCResponse;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class JDKProxy implements InvocationHandler, proxy {
    private static final NettyBootStrap bootStrap = new NettyBootStrap();

    public <T> T getProxy(Class<T> clazz){
        return (T) Proxy.newProxyInstance(clazz.getClassLoader(),new Class[]{clazz},this);
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //第一步是先填装request消息
        RPCRequest request = new RPCRequest();
        request.setMethodName(method.getName());
        request.setArgs(args);
        request.setInterfaceName(method.getDeclaringClass().getName());
        request.setParmType(method.getParameterTypes());
        request.setRequestID(UUID.randomUUID().toString());

        //填装Message消息
        RPCMessage mes = new RPCMessage();
        mes.setMessageType(RPCConstant.REQUEST_TYPE);
        mes.setRequest(request);
        mes.setCodec(RPCConstant.Protostuff);
        mes.setMagicNumber(RPCConstant.Magic_Number);

        //构造回应，并将消息发送过去
        RPCResponse<Object> response;
        Transport transport = SingletonFactory.getInstance(NettyBootStrap.class);//开始发送，调取传输框架
        CuratorFramework zkClient = CuratorUtils.getZkClient();
        ZkServiceDiscoveryImpl discovery = new ZkServiceDiscoveryImpl(zkClient);//从服务注册中心拉取服务者的地址
        InetSocketAddress address = discovery.lookupService(request);//不仅仅是注册中心，而且还有负载均衡
        CompletableFuture<RPCResponse> future = transport.sendMessage(mes, address);//发送
        response = (RPCResponse<Object>) future.get();//同步阻塞

        return response.getData();
    }
}
