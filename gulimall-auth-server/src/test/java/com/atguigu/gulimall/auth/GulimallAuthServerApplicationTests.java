package com.atguigu.gulimall.auth;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Random;

@SpringBootTest
class GulimallAuthServerApplicationTests {

    @Test
    void contextLoads() {
        int radomInt = new Random().nextInt(999999);
        int radomInt2 =(int)((Math.random()*9+1)*100000);
        //int v = (int)(Math.random() * 9 + 1) * 100000;
        System.out.println(radomInt);
        System.out.println(radomInt2);
    }

}
