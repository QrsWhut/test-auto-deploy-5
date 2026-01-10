package com.autodeploy.tool;

import com.microsoft.playwright.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * 登录状态保存工具
 * 
 * 运行方法：
 * mvn exec:java -D exec.mainClass=com.autodeploy.tool.SaveLoginState -D
 * exec.args="https://github.com"
 * 
 * 这个工具会：
 * 1. 打开浏览器访问指定网站
 * 2. 等待你手动登录（包括二次验证）
 * 3. 登录成功后在控制台按 Enter 保存登录状态
 */
public class SaveLoginState {

    private static final String STORAGE_PATH = "./auth/storage-state.json";

    public static void main(String[] args) {
        String url = args.length > 0 ? args[0] : "https://github.com";

        System.out.println("=".repeat(60));
        System.out.println("🔐 登录状态保存工具");
        System.out.println("=".repeat(60));
        System.out.println();
        System.out.println("📋 使用说明：");
        System.out.println("   1. 浏览器将打开 " + url);
        System.out.println("   2. 请在浏览器中完成登录（包括二次验证）");
        System.out.println("   3. 登录成功后，回到这里按 Enter 保存状态");
        System.out.println();
        System.out.println("=".repeat(60));
        System.out.println();

        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(
                    new BrowserType.LaunchOptions().setHeadless(false));

            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            // 导航到目标网站
            page.navigate(url);

            System.out.println("⏳ 请在浏览器中完成登录...");
            System.out.println();
            System.out.println("✅ 登录成功后，按 Enter 保存登录状态");

            // 等待用户按 Enter
            try {
                System.in.read();
            } catch (Exception e) {
                // ignore
            }

            // 保存登录状态
            Path storagePath = Paths.get(STORAGE_PATH);
            Files.createDirectories(storagePath.getParent());
            context.storageState(new BrowserContext.StorageStateOptions()
                    .setPath(storagePath));

            System.out.println();
            System.out.println("=".repeat(60));
            System.out.println("✅ 登录状态已保存到: " + storagePath.toAbsolutePath());
            System.out.println("=".repeat(60));

            browser.close();
        } catch (Exception e) {
            System.err.println("❌ 错误: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
