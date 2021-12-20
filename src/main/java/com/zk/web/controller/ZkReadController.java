package com.zk.web.controller;

import com.alibaba.fastjson.JSON;
import com.zk.entity.ZkData;
import com.zk.op.ZkApi;
import com.zk.web.constants.Constants;
import com.zk.web.util.AuthUtils;
import com.zk.web.util.SizeUtils;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/read")
public class ZkReadController {

	@Autowired
	private ZkApi zkApi;

	private static final Logger log = LoggerFactory.getLogger(ZkReadController.class);

	@RequestMapping("/addr")
	public String addr(HttpServletRequest request, RedirectAttributes attr, @RequestParam String cxnstr) {
		if (StringUtils.isBlank(cxnstr)) {
			return "redirect:/";
		}
		HttpSession session = request.getSession();
		session.setAttribute(Constants.CX_STR, cxnstr);
		session.setAttribute("cxnStr", cxnstr);
		attr.addFlashAttribute(Constants.CX_STR, StringUtils.trimToEmpty(cxnstr));
		return "redirect:/read/node/";
	}

	@RequestMapping("/node")
	public String node(HttpServletRequest request, Model model, String path) {
		HttpSession session = request.getSession();
		String cxnstr = (String) session.getAttribute(Constants.CX_STR);
		if (StringUtils.isBlank(cxnstr)) {
			return "redirect:/";
		}
		path = StringUtils.endsWith(path, "/") ? StringUtils.substring(path, 0, StringUtils.lastIndexOf(path, "/"))
				: path;
		path = StringUtils.isBlank(path) ? "/" : StringUtils.trimToEmpty(path);
		model.addAttribute("pathList", Arrays.asList(StringUtils.split(path, "/")));


		List<String> children = null;
		try {
			children = zkApi.getChildren(path);
		} catch (InterruptedException | KeeperException e) {
			log.error("从 zk 获取数据失败：[{}]", e.getMessage(), e);
		}
		if (!CollectionUtils.isEmpty(children)) {
			Collections.sort(children);
		}
		model.addAttribute("children", children);

		ZkData zkData = new ZkData();
		try {
			zkData = zkApi.readData(path);
		} catch (Exception e) {
			log.error("读取zk数据失败：[{}]", e.getMessage(), e);
		}
		model.addAttribute("data", zkData.getDataString());
		model.addAttribute("dataSize", SizeUtils.convertBytes(zkData.getData()));
		Map<String, Object> statMap = JSON.parseObject(JSON.toJSONString(zkData.getStat()));
		statMap.remove("class");
		model.addAttribute("stat", statMap);

		model.addAttribute("isLogin", AuthUtils.isLogin());
		return "node";
	}

}
