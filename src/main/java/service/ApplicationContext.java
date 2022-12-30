package service;

import spring.*;

import java.beans.Introspector;
import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationContext {

    private Class configClass;

    private ConcurrentHashMap<String, BeanDefinition> beanDefinitionMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, Object> singletonObjects = new ConcurrentHashMap<>();// 大名鼎鼎单例池

    public ApplicationContext(Class configClass){
        this.configClass = configClass;
        // 扫描，如果有ComponentScan注解 --> 得到一堆beanDefinition
        if (configClass.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan componentScanAnnotation = (ComponentScan) configClass.getAnnotation(ComponentScan.class);
            String path = componentScanAnnotation.value(); // 扫描路径 com.xxx.xxx....
            path = path.replace(".", "/"); // 相对路径
            ClassLoader classLoader = ApplicationContext.class.getClassLoader();
            URL resource = classLoader.getResource(path);// 获取路径下的资源
            File file = new File(resource.getFile());

            // 如果是文件夹
            if (file.isDirectory()) {
                File[] files = file.listFiles();
                for (File f : files) {
                    String fileName = f.getAbsolutePath();
                    if(fileName.endsWith(".class")){
                        // 接下来判断这个类 是不是一个bean
                        // 逻辑：判断一个类有没有Component注解
                        // 方式：反射
                        String className = fileName.substring(fileName.indexOf("service"), fileName.indexOf(".class"));
                        className = className.replace("/", ".");
                        try {
                            Class<?> clazz = classLoader.loadClass(className);
                            if (clazz.isAnnotationPresent(Component.class)) {
                                // 是Bean
                                // 1. 拿到bean的名字
                                Component componentAnnotation = clazz.getAnnotation(Component.class);
                                String beanName = componentAnnotation.value();
                                if(beanName.equals("")){
                                    // Spring中默认的名字生成规则，如下
                                    // Thus "FooBah" becomes "fooBah" and "X" becomes "x", but "URL" stays as "URL".
                                    beanName = Introspector.decapitalize(clazz.getSimpleName());
                                }
                                // 2. 设置BeanDefinition
                                BeanDefinition beanDefinition = new BeanDefinition();
                                beanDefinition.setType(clazz);
                                // 2.1. 判断单例？多例？
                                if (clazz.isAnnotationPresent(Scope.class)) {
                                    Scope scopeAnnotation = clazz.getAnnotation(Scope.class);
                                    beanDefinition.setScope(scopeAnnotation.value());

                                }else{
                                    beanDefinition.setScope("singleton");
                                }
                                beanDefinitionMap.put(beanName, beanDefinition);
                            }
                        } catch (ClassNotFoundException e) {

                        }

                    }
                }
            }
        }

        // 实例化单例Bean，放到单例池
        for (String beanName : beanDefinitionMap.keySet()) {
            BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
            if(beanDefinition.getScope().equals("singleton")){
                Object bean = createBean(beanName, beanDefinition);
                singletonObjects.put(beanName, bean);
            }
        }
    }

    // 仅在内部调用
    private Object createBean(String beanName, BeanDefinition beanDefinition){
        Class clazz = beanDefinition.getType();
        try {
            Object bean = clazz.getConstructor().newInstance(); // 无参创建

            // 处理依赖注入(Autowired)
            for (Field f : clazz.getDeclaredFields()) {
                if (f.isAnnotationPresent(Autowired.class)) {
                    f.setAccessible(true);
                    f.set(bean, getBean(f.getName()));
                }
            }

            // Aware回调
            if(bean instanceof BeanNameAware){
                ((BeanNameAware) bean).setBeanName(beanName);
            }

            return bean;
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    public Object getBean(String beanName){
        BeanDefinition beanDefinition = beanDefinitionMap.get(beanName);
        if(beanDefinition == null){
            // 1.beanName错误，不存在该bean
        }else{
            // 2.存在bean的定义
            String scope = beanDefinition.getScope();
            // 2.1 需要的bean为单例bean --> 从单例池里面拿
            if(scope.equals("singleton")){
                Object bean = singletonObjects.get(beanName);
                // 可能为null的原因:
                // beanA(singleton)中依赖注入了beanB(singleton)，此时先创建beanA，beanB在单例池里面还不存在
                if(bean == null){
                    bean = createBean(beanName, beanDefinition);
                    singletonObjects.put(beanName, bean);
                }
                return bean;
            }
            // 2.2 需要的bean为多例bean --> 每次都创建
            else{
                return createBean(beanName, beanDefinition);
            }
        }
        return null;
    }

}
