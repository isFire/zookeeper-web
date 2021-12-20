package com.zk.web.freemarker;

import com.zk.web.util.AuthUtils;
import freemarker.template.TemplateMethodModelEx;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class IsLogin implements TemplateMethodModelEx {

	@SuppressWarnings("rawtypes")
	@Override
	public Object exec(List arg0) {
		return AuthUtils.isLogin();
	}

}
