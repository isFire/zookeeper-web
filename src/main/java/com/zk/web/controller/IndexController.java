package com.zk.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import com.zk.util.ConfUtils;

@Slf4j
@Controller
@RequestMapping("")
public class IndexController {

   @RequestMapping(value = { "", "/", "/index" })
   public ModelAndView index() {
      ModelAndView mav = new ModelAndView("index");
      mav.addObject("addrs", ConfUtils.getConxtions());
      return mav;
   }

}
