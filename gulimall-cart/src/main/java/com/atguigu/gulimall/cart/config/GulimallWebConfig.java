package com.atguigu.gulimall.cart.config;

import com.atguigu.gulimall.cart.interceptor.CartInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 *
 **/

@Configuration
public class GulimallWebConfig implements WebMvcConfigurer {

    @Autowired
    private CartInterceptor cartInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(cartInterceptor)//注册拦截器
                .addPathPatterns("/**");

        /**
         * registry.addInterceptor(tokenInterceptor).addPathPatterns("/**")
         *                 .excludePathPatterns("/login")
         *                 .excludePathPatterns("/testMessage")
         *                 .excludePathPatterns("/register")
         *                 .excludePathPatterns("/testThread")
         *         ;
         */
    }
}
