package Factory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SingletonFactory {
    private static final Map<Class<?>,Object> InstanceCache = new ConcurrentHashMap<>();
    private static final Object lock = new Object();
    public static <T> T getInstance(Class<T> clazz){
        T instance = null;
        if(InstanceCache.containsKey(clazz)){
            return (T)InstanceCache.get(clazz);
        }else{
            synchronized (lock){
                try {
                    instance = clazz.newInstance();
                    InstanceCache.put(clazz,instance);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return instance;
    }
}
