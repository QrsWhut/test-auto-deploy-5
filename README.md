# Auto Deployment Tool

基于 Java + Playwright 的网页自动化部署工具。

## 快速开始

### 1. 安装依赖

```bash
# 安装 Maven 依赖
mvn clean install

# 安装 Playwright 浏览器
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium"
```

### 2. 运行服务

```bash
mvn spring-boot:run
```

服务启动后访问：http://localhost:8080

### 3. 使用 Codegen 录制操作

```bash
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="codegen https://your-website.com"
```

这会打开浏览器，录制你的操作并生成 Java 代码。

## API 接口

| 方法 | 路径 | 说明 |
|------|------|------|
| GET | /api/deploy/health | 健康检查 |
| GET | /api/deploy/tasks | 获取任务列表 |
| GET | /api/deploy/tasks/{name} | 获取任务详情 |
| POST | /api/deploy/execute/{name} | 执行指定任务 |
| POST | /api/deploy/execute | 直接执行任务配置 |

## 任务配置示例

创建 `src/main/resources/tasks/my-deploy.yml`:

```yaml
name: "部署到测试站"
url: "http://repo.company.com"
requireAuth: true

steps:
  - action: click
    selector: "text=我的项目"
    description: "选择项目"
    
  - action: click
    selector: "button:has-text('构建')"
    description: "点击构建"
    
  - action: wait
    selector: "text=构建成功"
    timeout: 300000
    description: "等待构建完成"
```

## 支持的操作类型

| 操作 | 说明 | 参数 |
|------|------|------|
| click | 点击元素 | selector |
| fill | 填充输入框 | selector, value |
| type | 逐字符输入 | selector, value |
| wait | 等待元素出现 | selector, timeout |
| wait_hidden | 等待元素消失 | selector, timeout |
| navigate | 导航到 URL | value |
| screenshot | 截图 | value (路径) |
| select | 下拉选择 | selector, value |
| check | 勾选复选框 | selector |
| uncheck | 取消勾选 | selector |
| sleep | 等待指定时间 | timeout |

## 登录状态管理

首次登录后，工具会自动保存登录状态到 `auth/storage-state.json`。
后续执行任务时会自动加载登录状态，无需重复登录。

## 目录结构

```
autoDeployment/
├── src/main/java/com/autodeploy/
│   ├── AutoDeployApplication.java
│   ├── config/AutoDeployConfig.java
│   ├── core/
│   │   ├── BrowserManager.java
│   │   └── TaskExecutor.java
│   ├── model/
│   │   ├── Task.java
│   │   └── Action.java
│   ├── service/DeployService.java
│   └── controller/DeployController.java
├── src/main/resources/
│   ├── application.yml
│   └── tasks/
│       └── example-github-login.yml
├── auth/
│   └── storage-state.json (自动生成)
└── pom.xml
```
