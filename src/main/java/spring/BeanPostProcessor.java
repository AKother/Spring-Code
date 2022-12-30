package spring;

public interface BeanPostProcessor {

    public Object postProcessBeforeInitializing(String beanName, Object bean);

    public Object postProcessAfterInitializing(String beanName, Object bean);

}
