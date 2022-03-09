package com.atguigu.gulimall.ware;

import com.atguigu.gulimall.ware.service.PurchaseService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GulimallWareApplicationTests {

    @Autowired
    PurchaseService purchaseService;

    @Test
    void contextLoads() {
        System.out.println(purchaseService.count());
    }

}
