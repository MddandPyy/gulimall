package com.atguigu.gulimall.seckill.interceptor;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.vo.MemberRespVo;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * <p>Title: LoginUserInterceptor</p>
 * Description：
 * date：2020/7/9 15:58
 */
@Component
public class LoginUserInterceptor implements HandlerInterceptor {

	public static ThreadLocal<MemberRespVo> threadLocal = new ThreadLocal<>();

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		String uri = request.getRequestURI();
		boolean match = new AntPathMatcher().match("/kill", uri);
		if(match){
			HttpSession session = request.getSession();
			MemberRespVo MemberRespVo = (com.atguigu.common.vo.MemberRespVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
			if(MemberRespVo != null){
				threadLocal.set(MemberRespVo);
				return true;
			}else{
				// 没登陆就去登录
				session.setAttribute("msg", AuthServerConstant.NOT_LOGIN);
				response.sendRedirect("http://auth.gulimall.com/login.html");
				return false;
			}
		}
		return true;
	}
}
