package service;

public class Test {

    public static void main(String[] args) {

        // 创建一个Spring容器
        ApplicationContext applicationContext = new ApplicationContext(AppConfig.class);

        UserService userService = (UserService) applicationContext.getBean("userService");
        userService.test();


    }
}
