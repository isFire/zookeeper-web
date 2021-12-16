package com.zk.web.interceptor;

import com.zk.web.util.AuthUtils;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
		if (!AuthUtils.isLogin()) {
			throw new RuntimeException("没有登录");
		}
		return true;
	}
}
