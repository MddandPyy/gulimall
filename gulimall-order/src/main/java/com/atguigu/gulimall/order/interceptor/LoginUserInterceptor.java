package com.atguigu.gulimall.order.interceptor;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.vo.MemberRespVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 订单功能都需要拦截
 */
@Component
public class LoginUserInterceptor implements HandlerInterceptor {

	public static ThreadLocal<MemberRespVo> threadLocal = new ThreadLocal<>();

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		String uri = request.getRequestURI();
		// 这个请求直接放行
		boolean match = new AntPathMatcher().match("/order/order/status/**", uri);
		if(match){
			return true;
		}
		// 获取session
		HttpSession session = request.getSession();
		// 获取登录用户
		MemberRespVo memberRespVo = (MemberRespVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
		if(memberRespVo != null){
			threadLocal.set(memberRespVo);
			return true;
		}else{
			// 没登陆就去登录
			session.setAttribute("msg", AuthServerConstant.NOT_LOGIN);
			response.sendRedirect("http://auth.gulimall.com/login.html");
			return false;
		}
	}
}
