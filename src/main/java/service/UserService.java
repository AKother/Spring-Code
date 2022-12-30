package service;

import spring.Autowired;
import spring.Component;
import spring.Scope;

@Component("userService")
@Scope("prototype")
public class UserService {

    @Autowired
    private OrderService orderService;

    public void test(){
        System.out.println(orderService);
    }

}
