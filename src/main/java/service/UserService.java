package service;

import spring.Autowired;
import spring.BeanNameAware;
import spring.Component;
import spring.Scope;

@Component("userService")
@Scope("prototype")
public class UserService implements BeanNameAware {

    @Autowired
    private OrderService orderService;

    private String beanName;

    public void test(){
        System.out.println(orderService);
    }

    @Override
    public void setBeanName(String beanName) {
        this.beanName = beanName;
    }
}
