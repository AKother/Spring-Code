package service;

public class Test {

    public static void main(String[] args) {

        // 创建一个Spring容器
        ApplicationContext applicationContext = new ApplicationContext(AppConfig.class);

        UserInterface userService = (UserInterface) applicationContext.getBean("userService");
        userService.test();


    }
}
