package com.zk.web.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zk.entity.ZkData;
import com.zk.mapper.ZkDataMapper;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Slf4j
@Controller
public class IndexController {

   @Autowired
   private ZkDataMapper zkDataMapper;

   @RequestMapping(value = { "", "/", "/index" })
   public ModelAndView index() {
      ModelAndView mav = new ModelAndView("index");
      List<ZkData> dataList = zkDataMapper.selectList(Wrappers.emptyWrapper());
      mav.addObject("addrs", dataList);
      return mav;
   }

}
