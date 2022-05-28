package ZK.Util;

import lombok.extern.slf4j.Slf4j;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.imps.CuratorFrameworkState;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
public class CuratorUtils {
    public static final String ZK_Root_Path = "/wyh-rpc";
    private static final Map<String, List<String>> ServiceAddressCache = new ConcurrentHashMap<>();
    private static CuratorFramework zkClient;
    private static final String DEFAULT_ZOOKEEPER_ADDRESS = "124.222.161.189:2181";

    /*
    我们需要zk做什么？首先是获取到zk的客户端，这样才可以进行我们的查询操作，
    需要可以注册永久节点的API，这样才可以将服务名注册上去，
    需要可以注册临时节点的API，这样才可以将服务提供者的地址提供上去
    需要可以查询子节点的API，这样才能查询对应服务下面的服务器地址
    需要为每一个服务名下面添加watch，这样才可以当地址发生变化的时候可以第一时间收到通知
     */
    private CuratorUtils() {
    }
    public static CuratorFramework getZkClient(){
        if(zkClient != null && zkClient.getState() == CuratorFrameworkState.STARTED){//当已经存在zk后，且zk处于存活状态，那么直接返回
            return zkClient;
        }
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000,3);
        //zkClient有两种生成策略，传入的参数可以为：链接地址，重试策略，会话超时时间，连接创立超时时间
        //而重试策略的传入参数为retryCount-已经重试的次数，elapsedTimes-第一次重试开始已经花费的时间；而BackoffRetry
        zkClient = CuratorFrameworkFactory.newClient(DEFAULT_ZOOKEEPER_ADDRESS,5000,1000,retryPolicy);
        zkClient.start();
        try {
            //此方法的主要作用在于阻塞等待zkClient可以被连接，等待时间有有一个最大值，超过后就说明连接超时了
            if(!zkClient.blockUntilConnected(15, TimeUnit.SECONDS)){
                throw new RuntimeException("Time out waiting to connect to ZK!");
            }
        } catch (InterruptedException e) {
            log.info("连接超时！");
        }
        return zkClient;
    }
    public static void createNode(CuratorFramework zkClient,String path,boolean isPersistent){//又可以创建持久节点又可以创建临时的
        path = ZK_Root_Path + "/" + path;
        try {
            if(zkClient.checkExists().forPath(path) == null){
                if(isPersistent){
                    zkClient.create().withMode(CreateMode.PERSISTENT).forPath(path);
                }else{
                    zkClient.create().withMode(CreateMode.EPHEMERAL).forPath(path);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //获取子节点上的值的同时为子节点添加watch
    public static List<String> getChildrenNode(CuratorFramework zkClient,String serviceName){
        List<String> result = null;
        if(ServiceAddressCache.containsKey(serviceName)){
            return ServiceAddressCache.get(serviceName);
        }
        String dataPath = ZK_Root_Path + "/"+ serviceName;
        try {
            result = zkClient.getChildren().forPath(dataPath);
            ServiceAddressCache.put(serviceName,result);
            //当配置为true的时候，客户端就可以获取到变化的节点的数据内容
            PathChildrenCache cache = new PathChildrenCache(zkClient,dataPath,true);//添加监视器
            cache.getListenable().addListener(new PathChildrenCacheListener() {
                @Override
                public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
                    PathChildrenCacheEvent.Type type = event.getType();
                    if(type == PathChildrenCacheEvent.Type.CHILD_ADDED || type == PathChildrenCacheEvent.Type.CHILD_REMOVED){
                        List<String> changedResult = curatorFramework.getChildren().forPath(dataPath);
                        ServiceAddressCache.put(serviceName,changedResult);
                    }
                }
            });
            cache.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }
    public static void deleteNode(CuratorFramework zkClient,String path){
        path = ZK_Root_Path + "/" + path;
        try {
            zkClient.delete().forPath(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
