package com.zk.web.controller;

import com.zk.op.ZkApi;
import com.zk.web.constants.Constants;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Controller
@RequestMapping("/op")
public class ZkOpController {
	private static final String SEPARATOR = "/";
	private static Logger log = LoggerFactory.getLogger(ZkOpController.class);

	@Autowired
	private ZkApi zkApi;

	@RequestMapping("/create")
	public String create(String parent, String name, String data) {
		String cxnstr = getCxnstr();
		if (StringUtils.isBlank(cxnstr)) {
			return "redirect:/";
		}
		parent = StringUtils.isBlank(parent) ? SEPARATOR : StringUtils.trimToEmpty(parent);
		parent = StringUtils.endsWith(parent, SEPARATOR) ? parent : parent + SEPARATOR;
		name = StringUtils.startsWith(name, SEPARATOR) ? StringUtils.substring(name, 1) : name;
		ZkApi zk = new ZkApi();
		String path = parent + name;
		try {
			zk.create(path, data.getBytes(StandardCharsets.UTF_8));
		} catch (InterruptedException | KeeperException  e) {
			log.error("写入数据到 zk 失败：[{}]", e.getMessage(), e);
		}
		return "redirect:/read/node?path=" + path;
	}

	@RequestMapping("/edit")
	public String edit(Model model, String path, String data) {
		String cxnstr = getCxnstr();
		if (StringUtils.isBlank(cxnstr)) {
			return "redirect:/";
		}
		path = StringUtils.isBlank(path) ? SEPARATOR : StringUtils.trimToEmpty(path);
		path = StringUtils.endsWith(path, "/") ? StringUtils.substring(path, 0, path.length()-1) : path;
		try {
			zkApi.edit(path, data.getBytes(StandardCharsets.UTF_8));
		} catch (InterruptedException | KeeperException e) {
			log.error("更新数据到 zk 失败：[{}]", e.getMessage(), e);
		}
		return "redirect:/read/node?path=" + path;
	}

	@RequestMapping("/delete")
	public String delete(Model model, String path, String data) {
		String cxnstr = getCxnstr();
		if (StringUtils.isBlank(cxnstr)) {
			return "redirect:/";
		}
		path = StringUtils.isBlank(path) ? SEPARATOR : StringUtils.trimToEmpty(path);
		path = StringUtils.endsWith(path, "/") ? StringUtils.substring(path, 0, path.length()-1) : path;
		try {
			zkApi.delete(path);
		} catch (InterruptedException | KeeperException e) {
			log.error("删除数据到 zk 失败：[{}]", e.getMessage(), e);
		}
		return "redirect:/read/node?path=" + StringUtils.substring(path, 0, StringUtils.lastIndexOf(path, "/"));
	}

	@RequestMapping("/rmrdel")
	public String rmrdel(Model model, String path, String data) {
		String cxnstr = getCxnstr();
		if (StringUtils.isBlank(cxnstr)) {
			return "redirect:/";
		}
		path = StringUtils.isBlank(path) ? SEPARATOR : StringUtils.trimToEmpty(path);
		path = StringUtils.endsWith(path, "/") ? StringUtils.substring(path, 0, path.length()-1) : path;
		delete(model, path, data);
		// zkApi.deleteRecursive(path);
		log.info("deleteRecursive, cxnstr:{}, path:{}", cxnstr, path);
		return "redirect:/read/node?path=" + StringUtils.substring(path, 0, StringUtils.lastIndexOf(path, "/"));
	}

	private String getCxnstr() {
		HttpServletRequest req = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		return (String) req.getSession().getAttribute(Constants.CX_STR);
	}

}
