package com.autodeploy.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Playwright 配置类
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "autodeploy")
public class AutoDeployConfig {

    private BrowserConfig browser = new BrowserConfig();
    private AuthConfig auth = new AuthConfig();
    private TasksConfig tasks = new TasksConfig();

    @Data
    public static class BrowserConfig {
        /**
         * 是否无头模式
         */
        private boolean headless = false;

        /**
         * 浏览器类型: chromium, firefox, webkit
         */
        private String type = "chromium";

        /**
         * 慢动作模式延迟（毫秒）
         */
        private int slowMo = 100;
    }

    @Data
    public static class AuthConfig {
        /**
         * 登录状态存储路径
         */
        private String storagePath = "./auth/storage-state.json";
    }

    @Data
    public static class TasksConfig {
        /**
         * 任务配置目录
         */
        private String directory = "./src/main/resources/tasks";
    }
}
