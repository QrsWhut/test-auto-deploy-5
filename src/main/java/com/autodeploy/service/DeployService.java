package com.autodeploy.service;

import com.autodeploy.config.AutoDeployConfig;
import com.autodeploy.core.AutomationExecutor;
import com.autodeploy.model.Task;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.LoaderOptions;

import java.io.IOException;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * 部署服务 - 管理任务配置和执行
 */
@Slf4j
@Service
public class DeployService {

    private final AutomationExecutor automationExecutor;
    private final AutoDeployConfig config;
    private final Yaml yaml;

    public DeployService(AutomationExecutor automationExecutor, AutoDeployConfig config) {
        this.automationExecutor = automationExecutor;
        this.config = config;

        LoaderOptions loaderOptions = new LoaderOptions();
        this.yaml = new Yaml(new Constructor(Task.class, loaderOptions));
    }

    /**
     * 获取所有可用任务
     */
    public List<Task> listTasks() {
        List<Task> tasks = new ArrayList<>();
        Path tasksDir = Paths.get(config.getTasks().getDirectory());

        if (!Files.exists(tasksDir)) {
            log.warn("任务目录不存在: {}", tasksDir);
            return tasks;
        }

        try (Stream<Path> paths = Files.walk(tasksDir, 1)) {
            paths.filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".yml") || p.toString().endsWith(".yaml"))
                    .forEach(path -> {
                        try {
                            Task task = loadTask(path);
                            if (task != null) {
                                tasks.add(task);
                            }
                        } catch (Exception e) {
                            log.error("加载任务失败: {}", path, e);
                        }
                    });
        } catch (IOException e) {
            log.error("遍历任务目录失败", e);
        }

        return tasks;
    }

    /**
     * 根据名称获取任务
     */
    public Task getTask(String taskName) {
        return listTasks().stream()
                .filter(t -> t.getName().equals(taskName))
                .findFirst()
                .orElse(null);
    }

    /**
     * 从文件加载任务
     */
    public Task loadTask(Path path) throws IOException {
        log.debug("加载任务配置: {}", path);
        String content = Files.readString(path);
        return yaml.load(content);
    }

    /**
     * 执行指定任务
     */
    public AutomationExecutor.TaskResult executeTask(String taskName) {
        return executeTask(taskName, null);
    }

    /**
     * 执行指定任务（传入变量）
     */
    public AutomationExecutor.TaskResult executeTask(String taskName, Map<String, String> variables) {
        Task task = getTask(taskName);
        if (task == null) {
            return AutomationExecutor.TaskResult.failure(taskName, "任务不存在: " + taskName);
        }
        return variables != null ? automationExecutor.execute(task, variables) : automationExecutor.execute(task);
    }

    /**
     * 执行任务对象
     */
    public AutomationExecutor.TaskResult executeTask(Task task) {
        return automationExecutor.execute(task);
    }

    /**
     * 执行任务对象（传入变量）
     */
    public AutomationExecutor.TaskResult executeTask(Task task, Map<String, String> variables) {
        return variables != null ? automationExecutor.execute(task, variables) : automationExecutor.execute(task);
    }

    /**
     * 保存任务配置
     */
    public void saveTask(Task task, String filename) throws IOException {
        Path tasksDir = Paths.get(config.getTasks().getDirectory());
        Files.createDirectories(tasksDir);

        Path taskFile = tasksDir.resolve(filename);
        String content = yaml.dump(task);
        Files.writeString(taskFile, content);
        log.info("任务已保存: {}", taskFile);
    }
}
