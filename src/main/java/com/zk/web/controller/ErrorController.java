package com.zk.web.controller;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/error")
public class ErrorController {

   private static final String[] ERROR_CODE = new String[] { "404", "500" };

   @RequestMapping("/{code}")
   public String error404(String code) {
      code = StringUtils.trimToNull(code);
      if (!ArrayUtils.contains(ERROR_CODE, code)) {
         code = "404";
      }
      return "common/error" + code;
   }
}
