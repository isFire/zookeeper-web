package com.zk;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * @author <a href="mailto:zhao___li@163.com">清汤白面<a/>
 * @description
 * @date 2021-12-17 09:57
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
public class ZkApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZkApplication.class, args);
    }
}


