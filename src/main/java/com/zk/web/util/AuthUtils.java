package com.zk.web.util;

import com.zk.util.ConfUtils;
import java.io.IOException;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class AuthUtils {
	private static final String ZK_USER = "zk_user";
	private static final String USER_FILE_PATH = "conf/user.properties";

	private static Logger log = LoggerFactory.getLogger(ConfUtils.class);

	public static boolean isLogin() {
		HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		Object obj = req.getSession().getAttribute(ZK_USER);
		if (obj != null) {
			return true;
		}
		return false;
	}

	public static boolean login(String userName, String password) {
		HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		userName = StringUtils.trimToNull(userName);
		password = StringUtils.trimToNull(password);
		if (StringUtils.isBlank(userName) || StringUtils.isBlank(password)) {
			return false;
		}
		Properties properties = getUsers();
		String pwd = properties.getProperty(userName);
		if (StringUtils.equals(password, pwd)) {
			req.getSession().setAttribute(ZK_USER, userName);
			return true;
		}
		return false;
	}
	
	public static boolean logout() {
		HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		req.getSession().removeAttribute(ZK_USER);
		return false;
	}

	private static Properties getUsers() {
		try {
			return PropertiesLoaderUtils.loadProperties(new ClassPathResource(USER_FILE_PATH));
		} catch (IOException e) {
			log.error("没有找到帐号配置文件.{}", e.getMessage(), e);
		}
		return null;
	}

}
