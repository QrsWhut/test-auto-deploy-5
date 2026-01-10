package com.autodeploy.controller;

import com.autodeploy.core.AutomationExecutor;
import com.autodeploy.model.Task;
import com.autodeploy.service.DeployService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 部署控制器 - REST API
 */
@Slf4j
@RestController
@RequestMapping("/api/deploy")
public class DeployController {

    private final DeployService deployService;

    public DeployController(DeployService deployService) {
        this.deployService = deployService;
    }

    /**
     * 获取所有任务列表
     * GET /api/deploy/tasks
     */
    @GetMapping("/tasks")
    public ResponseEntity<List<Task>> listTasks() {
        List<Task> tasks = deployService.listTasks();
        return ResponseEntity.ok(tasks);
    }

    /**
     * 获取指定任务详情
     * GET /api/deploy/tasks/{taskName}
     */
    @GetMapping("/tasks/{taskName}")
    public ResponseEntity<Task> getTask(@PathVariable String taskName) {
        Task task = deployService.getTask(taskName);
        if (task == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(task);
    }

    /**
     * 执行指定任务
     * POST /api/deploy/execute/{taskName}
     * 可选请求参数: variables (JSON对象，用于覆盖任务默认变量)
     */
    @PostMapping("/execute/{taskName}")
    public ResponseEntity<Map<String, Object>> executeTask(
            @PathVariable String taskName,
            @RequestBody(required = false) Map<String, String> variables) {
        log.info("收到执行任务请求: {}, 变量: {}", taskName, variables);

        AutomationExecutor.TaskResult result = deployService.executeTask(taskName, variables);

        Map<String, Object> response = new HashMap<>();
        response.put("success", result.success());
        response.put("taskName", result.taskName());
        response.put("message", result.message());
        response.put("duration", result.duration());

        if (result.success()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 直接执行任务配置（不保存）
     * POST /api/deploy/execute
     */
    @PostMapping("/execute")
    public ResponseEntity<Map<String, Object>> executeTaskDirect(@RequestBody Task task) {
        log.info("收到直接执行任务请求: {}", task.getName());

        AutomationExecutor.TaskResult result = deployService.executeTask(task);

        Map<String, Object> response = new HashMap<>();
        response.put("success", result.success());
        response.put("taskName", result.taskName());
        response.put("message", result.message());
        response.put("duration", result.duration());

        if (result.success()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 健康检查
     * GET /api/deploy/health
     */
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        Map<String, String> status = new HashMap<>();
        status.put("status", "UP");
        status.put("service", "auto-deployment");
        return ResponseEntity.ok(status);
    }
}
