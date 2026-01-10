package com.autodeploy.core;

import com.autodeploy.model.Action;
import com.autodeploy.model.Task;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自动化执行器 - 执行自动化任务
 */
@Slf4j
@Component
public class AutomationExecutor {

    private final BrowserManager browserManager;

    // 变量匹配模式: ${variableName}
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)}");

    public AutomationExecutor(BrowserManager browserManager) {
        this.browserManager = browserManager;
    }

    /**
     * 执行任务（不传入额外变量）
     */
    public TaskResult execute(Task task) {
        return execute(task, new HashMap<>());
    }

    /**
     * 执行任务（传入运行时变量，会覆盖默认值）
     */
    public TaskResult execute(Task task, Map<String, String> runtimeVariables) {
        log.info("开始执行任务: {}", task.getName());
        long startTime = System.currentTimeMillis();

        // 合并变量：默认值 + 运行时变量
        Map<String, String> variables = new HashMap<>();
        if (task.getVariables() != null) {
            variables.putAll(task.getVariables());
        }
        variables.putAll(runtimeVariables);

        log.debug("任务变量: {}", variables);

        BrowserContext context = null;
        Page page = null;

        try {
            context = browserManager.createContext();
            page = browserManager.createPage(context);

            // 导航到目标 URL（支持变量替换）
            String url = replaceVariables(task.getUrl(), variables);
            log.info("导航到: {}", url);
            page.navigate(url);

            // 执行每个步骤
            int stepIndex = 0;
            for (Action action : task.getSteps()) {
                stepIndex++;
                log.info("执行步骤 {}/{}: {} - {}",
                        stepIndex, task.getSteps().size(),
                        action.getAction(),
                        action.getDescription() != null ? action.getDescription() : action.getSelector());

                executeAction(page, action, variables);
            }

            // 保存登录状态
            if (task.isRequireAuth()) {
                browserManager.saveStorageState(context);
            }

            long duration = System.currentTimeMillis() - startTime;
            log.info("任务完成: {}, 耗时: {}ms", task.getName(), duration);

            return TaskResult.success(task.getName(), duration);

        } catch (Exception e) {
            log.error("任务执行失败: {}", task.getName(), e);
            return TaskResult.failure(task.getName(), e.getMessage());
        } finally {
            if (page != null) {
                page.close();
            }
            if (context != null) {
                context.close();
            }
        }
    }

    /**
     * 执行单个操作（支持变量替换）
     */
    private void executeAction(Page page, Action action, Map<String, String> variables) {
        String actionType = action.getAction().toLowerCase();
        String selector = replaceVariables(action.getSelector(), variables);
        String value = replaceVariables(action.getValue(), variables);
        long timeout = action.getTimeout();

        switch (actionType) {
            case "click" -> {
                page.click(selector, new Page.ClickOptions().setTimeout(timeout));
            }
            case "fill" -> {
                page.fill(selector, value, new Page.FillOptions().setTimeout(timeout));
            }
            case "type" -> {
                page.type(selector, value, new Page.TypeOptions().setTimeout(timeout));
            }
            case "wait" -> {
                page.waitForSelector(selector, new Page.WaitForSelectorOptions()
                        .setState(WaitForSelectorState.VISIBLE)
                        .setTimeout(timeout));
            }
            case "wait_hidden" -> {
                page.waitForSelector(selector, new Page.WaitForSelectorOptions()
                        .setState(WaitForSelectorState.HIDDEN)
                        .setTimeout(timeout));
            }
            case "navigate" -> {
                page.navigate(value);
            }
            case "screenshot" -> {
                String path = value != null ? value : "screenshot.png";
                page.screenshot(new Page.ScreenshotOptions().setPath(java.nio.file.Paths.get(path)));
                log.info("截图已保存: {}", path);
            }
            case "sleep" -> {
                try {
                    Thread.sleep(timeout);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            case "select" -> {
                page.selectOption(selector, value);
            }
            case "check" -> {
                page.check(selector);
            }
            case "uncheck" -> {
                page.uncheck(selector);
            }
            default -> {
                log.warn("未知的操作类型: {}", actionType);
            }
        }
    }

    /**
     * 替换字符串中的变量
     * ${variableName} -> variableValue
     */
    private String replaceVariables(String input, Map<String, String> variables) {
        if (input == null || variables == null || variables.isEmpty()) {
            return input;
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(input);
        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String variableName = matcher.group(1);
            String replacement = variables.getOrDefault(variableName, matcher.group(0));
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * 任务执行结果
     */
    public record TaskResult(
            boolean success,
            String taskName,
            String message,
            long duration) {
        public static TaskResult success(String taskName, long duration) {
            return new TaskResult(true, taskName, "执行成功", duration);
        }

        public static TaskResult failure(String taskName, String message) {
            return new TaskResult(false, taskName, message, 0);
        }
    }
}
