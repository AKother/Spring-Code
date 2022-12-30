package service;

import spring.BeanPostProcessor;
import spring.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

@Component
public class TestBeanPostProcessor implements BeanPostProcessor {

    @Override
    public Object postProcessBeforeInitializing(String beanName, Object bean) {
        if(beanName.equals("userService")){
            System.out.println("userService postProcessBeforeInitializing ..");
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitializing(String beanName, Object bean) {
        if(beanName.equals("userService")){
            // AOP逻辑
            Object proxyInstance = Proxy.newProxyInstance(TestBeanPostProcessor.class.getClassLoader(), bean.getClass().getInterfaces(), new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    System.out.println("AOP逻辑");
                    // 之后再执行原本对象的原本方法
                    return method.invoke(bean, args);
                }
            });
            return proxyInstance;
        }
        return bean;
    }
}
