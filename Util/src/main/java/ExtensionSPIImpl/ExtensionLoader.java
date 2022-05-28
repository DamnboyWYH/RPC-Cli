package ExtensionSPIImpl;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class ExtensionLoader<T>{
    private final static String PREFIX = "META-INF/extensions/";
    private final Class<T> type;

    private static final ConcurrentHashMap<String,Holder<Object>> NameToInstanceCache = new ConcurrentHashMap<>(64);
    private static final ConcurrentHashMap<Class<?>,ExtensionLoader<?>> ExtensionLoaderCache = new ConcurrentHashMap<>(64);
    private static final ConcurrentHashMap<Class<?>,Holder<Object>> cacheExtensionInstance = new ConcurrentHashMap<>(64);
    private static final Map<Class<?>, Object> ClassToInstanceCache = new ConcurrentHashMap<>(64);
    private static final ConcurrentHashMap<String,Class<?>> NameToClassCache = new ConcurrentHashMap<>(64);
    private final Holder<Map<String,Class<?>>>cachedClass = new Holder<>();

    private Object defaultImplement;
    private ClassLoader loader;

    class InnerProxy implements InvocationHandler {

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if(defaultImplement == null){
                log.info("没有指定默认实现类的同时找不到指定实现类");
                throw new IllegalStateException();
            }
            Object res = method.invoke(defaultImplement,args);
            return res;
        }
    }

    public ExtensionLoader(Class<T> type) {
        this.type = type;
    }

    public ExtensionLoader(Class<T> type, Object defaultImplement) {
        this.defaultImplement = defaultImplement;
        this.type = type;
    }

    public static <T> ExtensionLoader<T> getExtensionLoader(Class<T> type,Object defaultImplement) {
        //首先进行类型判断
        //随后进行extensionSPIloader的懒加载机制
        //先从我们的缓存中获取，找不到才初始化一个
        if(type == null){
            throw new IllegalArgumentException("Extension type should not be null.");
        }
        if(!type.isInterface()){
            throw new IllegalArgumentException("Extension type(" + type + ") is not interface!");
        }
        if(type.getAnnotation(ExtensionSPI.class) == null){
            throw new IllegalArgumentException("Extension type must be annotated by @ExtensionSPI");
        }
        ExtensionLoader<T> extensionLoader = (ExtensionLoader<T>) ExtensionLoaderCache.get(type);
        if(extensionLoader == null){
            if(defaultImplement == null){
                ExtensionLoaderCache.putIfAbsent(type,new ExtensionLoader<T>(type));
            }else{
                ExtensionLoaderCache.putIfAbsent(type,new ExtensionLoader<T>(type,defaultImplement));
            }
            extensionLoader = (ExtensionLoader<T>) ExtensionLoaderCache.get(type);
        }
        return extensionLoader;
    }

    public T getExtension(String name){//通过名字来获取特定的首先类
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("Extension name should not be null or empty.");
        }
        Holder<Object> holder = NameToInstanceCache.get(name);
        if(holder == null){
            NameToInstanceCache.putIfAbsent(name, new Holder<>());
            holder = NameToInstanceCache.get(name);
        }
        Object instance = holder.getValue();
        if(instance == null){
            synchronized (holder){
                instance = holder.getValue();
                if(instance == null){
                    instance = createExtension(name);//根据名字来创建实例
                    holder.setValue(instance);
                }
            }
        }
        return (T) holder.getValue();
    }

    private T createExtension(String name) {//首先建立起name TO class 的关系
        Class<?> clazz = getExtensionClasses().get(name);//先通过名字来获取到类名
        T instance = null;
        if (clazz == null) {
            instance = (T) Proxy.newProxyInstance(ExtensionLoader.class.getClassLoader(),new Class[]{type},new InnerProxy());
            log.info("因为找不到目标类，所以采用默认实现类");
        }
        else{
            instance = (T) ClassToInstanceCache.get(clazz);
            if(instance == null){
                try {
                    ClassToInstanceCache.putIfAbsent(clazz,clazz.newInstance());
                    instance = (T) ClassToInstanceCache.get(clazz);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }
        return instance;
    }

    private Map<String, Class<?>> getExtensionClasses() {
        Map<String, Class<?>> classes = cachedClass.getValue();
        if (classes == null) {
            synchronized (cachedClass) {
                classes = cachedClass.getValue();
                if (classes == null) {
                    classes = new HashMap<>();
                    // load all extensions from our extensions directory
                    loadDirectory(classes);
                    cachedClass.setValue(classes);
                }
            }
        }
        return classes;
    }

    private void loadDirectory(Map<String, Class<?>> extensionClasses) {//对相应的文件进行解析
        String fileName = ExtensionLoader.PREFIX + type.getName();
        try {
            Enumeration<URL> urls;
            ClassLoader classLoader = ExtensionLoader.class.getClassLoader();
            urls = classLoader.getResources(fileName);
            if (urls != null) {
                while (urls.hasMoreElements()) {
                    URL resourceUrl = urls.nextElement();
                    loadResource(extensionClasses, classLoader, resourceUrl);//从文件内容中得到真正的类名
                }
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void loadResource(Map<String, Class<?>> extensionClasses, ClassLoader classLoader, URL resourceUrl) {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resourceUrl.openStream(), UTF_8))) {
            String line;
            // read every line
            while ((line = reader.readLine()) != null) {
                // get index of comment
                final int ci = line.indexOf('#');
                if (ci >= 0) {
                    // string after # is comment so we ignore it
                    line = line.substring(0, ci);
                }
                line = line.trim();
                if (line.length() > 0) {
                    try {
                        final int ei = line.indexOf('=');
                        String name = line.substring(0, ei).trim();
                        String clazzName = line.substring(ei + 1).trim();
                        // our SPI use key-value pair so both of them must not be empty
                        if (name.length() > 0 && clazzName.length() > 0) {
                            Class<?> clazz = classLoader.loadClass(clazzName);
                            extensionClasses.put(name, clazz);
                        }
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        log.error(e.getMessage());
                    }
                }

            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

}
