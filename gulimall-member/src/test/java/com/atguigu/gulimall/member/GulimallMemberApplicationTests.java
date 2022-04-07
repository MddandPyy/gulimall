package com.atguigu.gulimall.member;

import com.atguigu.gulimall.member.service.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
class GulimallMemberApplicationTests {

    @Autowired
    MemberService memberService;


    @Test
    void contextLoads() {
        //System.out.println(memberService.count());
        // 密码要加密存储
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String str = bCryptPasswordEncoder.encode("123456");
        System.out.println(str);
        System.out.println(bCryptPasswordEncoder.matches("123456",str));
    }

}
