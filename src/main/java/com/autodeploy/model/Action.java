package com.autodeploy.model;

import lombok.Data;
import java.util.Map;

/**
 * 操作模型 - 定义单个自动化操作
 */
@Data
public class Action {
    
    /**
     * 操作类型: click, fill, wait, navigate, screenshot
     */
    private String action;
    
    /**
     * 元素选择器（CSS选择器、XPath、文本等）
     */
    private String selector;
    
    /**
     * 输入值（用于 fill 操作）
     */
    private String value;
    
    /**
     * 超时时间（毫秒）
     */
    private Long timeout = 30000L;
    
    /**
     * 操作描述（用于日志）
     */
    private String description;
    
    /**
     * 额外参数
     */
    private Map<String, Object> options;
}
