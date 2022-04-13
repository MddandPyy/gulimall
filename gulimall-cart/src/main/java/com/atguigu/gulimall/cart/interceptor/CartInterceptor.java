package com.atguigu.gulimall.cart.interceptor;

import com.atguigu.common.constant.AuthServerConstant;
import com.atguigu.common.constant.CartConstant;
import com.atguigu.common.vo.MemberRespVo;
import com.atguigu.gulimall.cart.vo.UserInfoTo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

/**
 * <p>Title: CartInterceptor</p>
 * Description：在执行目标之前 判断用户是否登录,并封装
 */
@Component
public class CartInterceptor implements HandlerInterceptor {

	@Autowired
	private StringRedisTemplate stringRedisTemplate;

	// 存放当前线程用户信息
	public static ThreadLocal<UserInfoTo> threadLocal = new ThreadLocal<>();

	/** userInfoTo包含登录用户和临时用户的信息*/
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

		// 准备好要设置到threadlocal里的user对象
		UserInfoTo userInfoTo = new UserInfoTo();
		HttpSession session = request.getSession();
		// 获取loginUser对应的用户value，没有也不去登录了。登录逻辑放到别的代码里，需要登录时再重定向
		MemberRespVo user = (MemberRespVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
		// 已登录用户，设置userId
		if (user != null){
			userInfoTo.setUsername(user.getUsername());
			userInfoTo.setUserId(user.getId());//明确下登录用的是UserId，临时用户用的是UserKey
		}

		// 将cookie中的临时购物车信息设置到threadlocal中 // 不登录也没关系，可以访问临时用户购物车
		Cookie[] cookies = request.getCookies();
		if( cookies != null && cookies.length > 0){
			for (Cookie cookie : cookies) {
				String name = cookie.getName();
				if(CartConstant.TEMP_USER_COOKIE_NAME.equals(name)){ // 有"user-key";这个cookie
					userInfoTo.setUserKey(cookie.getValue());
					userInfoTo.setTempUser(true);
				}
			}
		}
		// 如果没有临时用户和登录用户，则分配一个临时用户 // 分配的临时用户在postHandle的时候放到cookie里即可
		if (StringUtils.isEmpty(userInfoTo.getUserKey())  // 没有临时用户时
				&& StringUtils.isEmpty(userInfoTo.getUserId())){ // 有登录用户就不生成临时
			String uuid = UUID.randomUUID().toString().replace("-","");
			userInfoTo.setUserKey("GULI-" + uuid);//临时用户
			userInfoTo.setTempUser(true);
		}
		threadLocal.set(userInfoTo);
		return true;
		// 还有一个登录后应该删除临时购物车的逻辑没有实现
	}

	/**
	 * 执行完毕之后分配临时用户让浏览器保存
	 */
	@Override
	public void postHandle(HttpServletRequest request,
						   HttpServletResponse response, Object handler,
						   ModelAndView modelAndView) throws Exception {

		UserInfoTo userInfoTo = threadLocal.get();
		// 如果是临时用户，返回临时购物车的cookie
		if(userInfoTo.isTempUser()){
			Cookie cookie = new Cookie(CartConstant.TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
			// 设置cookie作用域、过期时间
			cookie.setDomain("gulimall.com");
			cookie.setMaxAge(CartConstant.TEMP_USER_COOKIE_TIME_OUT);
			response.addCookie(cookie);
		}
	}
}
