package com.atguigu.gulimall.auth.vo;

import lombok.Data;

@Data
public class SocialUser {

    private String accessToken;

    private String tokenType;

    private String expiresIn;

    private String refreshToken;

    private String scope;
}