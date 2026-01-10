package com.autodeploy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 自动化部署工具启动类
 */
@SpringBootApplication
public class AutoDeployApplication {

    public static void main(String[] args) {
        SpringApplication.run(AutoDeployApplication.class, args);
    }
}
