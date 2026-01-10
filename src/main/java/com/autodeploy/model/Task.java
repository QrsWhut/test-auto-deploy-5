package com.autodeploy.model;

import lombok.Data;
import java.util.List;
import java.util.Map;

/**
 * 任务模型 - 对应 YAML 配置文件
 */
@Data
public class Task {

    /**
     * 任务名称
     */
    private String name;

    /**
     * 目标 URL
     */
    private String url;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 是否需要登录
     */
    private boolean requireAuth = true;

    /**
     * 变量定义（可在执行时覆盖）
     * 格式: variableName: defaultValue
     */
    private Map<String, String> variables;

    /**
     * 操作步骤列表
     */
    private List<Action> steps;
}
