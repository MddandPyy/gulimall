package com.atguigu.gulimall.member;

import com.atguigu.gulimall.member.service.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class GulimallMemberApplicationTests {

    @Autowired
    MemberService memberService;


    @Test
    void contextLoads() {
        System.out.println(memberService.count());
    }

}
