package com.autodeploy.core;

import com.autodeploy.config.AutoDeployConfig;
import com.microsoft.playwright.*;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 浏览器管理器 - 管理 Playwright 浏览器生命周期
 */
@Slf4j
@Component
public class BrowserManager {

    private final AutoDeployConfig config;
    private Playwright playwright;
    private Browser browser;

    public BrowserManager(AutoDeployConfig config) {
        this.config = config;
    }

    @PostConstruct
    public void init() {
        log.info("初始化 Playwright...");
        playwright = Playwright.create();
        log.info("Playwright 初始化完成");
    }

    @PreDestroy
    public void destroy() {
        log.info("关闭 Playwright...");
        if (browser != null) {
            browser.close();
        }
        if (playwright != null) {
            playwright.close();
        }
        log.info("Playwright 已关闭");
    }

    /**
     * 获取或创建浏览器实例
     */
    public Browser getBrowser() {
        if (browser == null || !browser.isConnected()) {
            browser = launchBrowser();
        }
        return browser;
    }

    /**
     * 创建新的浏览器上下文（带登录状态）
     */
    public BrowserContext createContext() {
        Browser.NewContextOptions options = new Browser.NewContextOptions();

        // 不设置固定视窗，使用浏览器窗口实际大小（配合 --start-maximized）
        options.setViewportSize(null);

        // 如果存在登录状态文件，则加载
        Path storagePath = Paths.get(config.getAuth().getStoragePath());
        if (Files.exists(storagePath)) {
            log.info("加载登录状态: {}", storagePath);
            options.setStorageStatePath(storagePath);
        }

        return getBrowser().newContext(options);
    }

    /**
     * 保存登录状态
     */
    public void saveStorageState(BrowserContext context) {
        try {
            Path storagePath = Paths.get(config.getAuth().getStoragePath());
            Files.createDirectories(storagePath.getParent());
            context.storageState(new BrowserContext.StorageStateOptions()
                    .setPath(storagePath));
            log.info("登录状态已保存: {}", storagePath);
        } catch (Exception e) {
            log.error("保存登录状态失败", e);
        }
    }

    /**
     * 创建新页面
     */
    public Page createPage() {
        return createContext().newPage();
    }

    /**
     * 创建新页面（使用现有上下文）
     */
    public Page createPage(BrowserContext context) {
        return context.newPage();
    }

    private Browser launchBrowser() {
        BrowserType.LaunchOptions options = new BrowserType.LaunchOptions()
                .setHeadless(config.getBrowser().isHeadless())
                .setSlowMo(config.getBrowser().getSlowMo())
                // 启动时最大化窗口
                .setArgs(java.util.List.of("--start-maximized"));

        String browserType = config.getBrowser().getType().toLowerCase();
        log.info("启动浏览器: {} (headless: {}, 最大化窗口)", browserType, config.getBrowser().isHeadless());

        return switch (browserType) {
            case "firefox" -> playwright.firefox().launch(options);
            case "webkit" -> playwright.webkit().launch(options);
            default -> playwright.chromium().launch(options);
        };
    }
}
