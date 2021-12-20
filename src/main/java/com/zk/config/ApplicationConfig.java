package com.zk.config;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;

/**
 * @author <a href="mailto:zhao___li@163.com">清汤白面<a/>
 * @description
 * @date 2021-12-17 11:32
 */
@Configuration
public class ApplicationConfig {

    // @Bean
    // public FreeMarkerConfigurer freemarkerConfig() {
    //     FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();
    //     configurer.setDefaultEncoding(StandardCharsets.UTF_8.name());
    //     Properties properties = new Properties();
    //     properties.put("tag_syntax", "auto_detect");
    //     properties.put("template_update_delay", "0");
    //     properties.put("defaultEncoding", StandardCharsets.UTF_8.name());
    //     properties.put("url_escaping_charset", StandardCharsets.UTF_8.name());
    //     properties.put("locale", "zh_CN");
    //     properties.put("boolean_format", "true,false");
    //     properties.put("date_format", "yyyy-MM-dd");
    //     properties.put("time_format", "HH:mm:ss");
    //     properties.put("datetime_format", "yyyy-MM-dd HH:mm:ss");
    //     properties.put("number_format", "#.##");
    //     properties.put("whitespace_stripping", "true");
    //     properties.put("auto_import", "/common/env.ftl as tops_pch_env");
    //     configurer.setFreemarkerSettings(properties);
    //     return configurer;
    // }


}
