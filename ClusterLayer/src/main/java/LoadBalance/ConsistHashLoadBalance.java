package LoadBalance;

import customProtocol.RPCRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

public class ConsistHashLoadBalance extends AbstractLoadBalance{
    private final ConcurrentHashMap<String, ConsistentHashSelector> selectors = new ConcurrentHashMap<>();

    @Override
    protected String doSelect(List<String> serviceAddresses, RPCRequest rpcRequest) {
        int identityHashCode = System.identityHashCode(serviceAddresses);//返回给定对象的hash码，当服务地址发生变化的时候，hash值就会发生改变
        // build rpc service name by rpcRequest
        String rpcServiceName = rpcRequest.getMethodName();
        ConsistentHashSelector selector = selectors.get(rpcServiceName);


        // check for updates
        if (selector == null || selector.identityHashCode != identityHashCode) {//当hash值不同的时候，说明服务节点发生了变化，发生改变之后就需要重新映射到圆上
            selectors.put(rpcServiceName, new ConsistentHashSelector(serviceAddresses, 160, identityHashCode));
            selector = selectors.get(rpcServiceName);
        }
        return selector.select(rpcServiceName + Arrays.stream(rpcRequest.getArgs()));
    }

    static class ConsistentHashSelector {
        private final TreeMap<Long, String> virtualInvokers;//保存节点用的TreeMap。

        private final int identityHashCode;

        ConsistentHashSelector(List<String> invokers, int replicaNumber, int identityHashCode) {
            this.virtualInvokers = new TreeMap<>();
            this.identityHashCode = identityHashCode;

            for (String invoker : invokers) {
                for (int i = 0; i < replicaNumber / 4; i++) {
                    byte[] digest = md5(invoker + i);
                    for (int h = 0; h < 4; h++) {
                        long m = hash(digest, h);//获取hash值
                        virtualInvokers.put(m, invoker);//将服务节点放置到圆上面
                    }
                }
            }
        }

        static byte[] md5(String key) {
            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
                byte[] bytes = key.getBytes(StandardCharsets.UTF_8);
                md.update(bytes);
            } catch (NoSuchAlgorithmException e) {
                throw new IllegalStateException(e.getMessage(), e);
            }

            return md.digest();
        }//用MD5算法计算摘要

        static long hash(byte[] digest, int idx) {
            return ((long) (digest[3 + idx * 4] & 255) << 24 | (long) (digest[2 + idx * 4] & 255) << 16 | (long) (digest[1 + idx * 4] & 255) << 8 | (long) (digest[idx * 4] & 255)) & 4294967295L;
        }

        public String select(String rpcServiceKey) {
            byte[] digest = md5(rpcServiceKey);//算出一个摘要之后就可以根据该摘要去圆上找节点了
            return selectForKey(hash(digest, 0));
        }

        public String selectForKey(long hashCode) {
            Map.Entry<Long, String> entry = virtualInvokers.tailMap(hashCode, true).firstEntry();//获取第一个比hashcode大的值

            if (entry == null) {
                entry = virtualInvokers.firstEntry();//当找不到第一个比他大的值得时候，就取圆上的第一个
            }

            return entry.getValue();
        }//找到圆上第一个比key大的节点
    }

}
