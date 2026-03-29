# IDEA 运行后端配置流程

## 前置准备

### 1. 安装必要软件
- **JDK 17** 或更高版本
- **Maven 3.8** 或更高版本（可选，IDEA内置）
- **MySQL 8.0** 或更高版本
- **Redis 7.0** 或更高版本

### 2. 启动数据库服务

#### Windows方式（推荐使用Docker）

如果你已经安装了Docker Desktop，可以单独启动MySQL和Redis：

```bash
# 启动MySQL
docker run -d --name gengbaike-mysql ^
  -p 3306:3306 ^
  -e MYSQL_ROOT_PASSWORD=root123 ^
  -e MYSQL_DATABASE=gengbaike ^
  -e TZ=Asia/Shanghai ^
  mysql:8.0 ^
  --character-set-server=utf8mb4 ^
  --collation-server=utf8mb4_unicode_ci

# 启动Redis
docker run -d --name gengbaike-redis ^
  -p 6379:6379 ^
  redis:7.0-alpine ^
  redis-server --maxmemory 512mb --maxmemory-policy allkeys-lru
```

#### 或者使用Docker Compose启动数据库

```bash
# 在项目根目录执行，只启动数据库
docker-compose up -d mysql redis
```

### 3. 初始化数据库

执行数据库初始化脚本：

```sql
-- 方式一：使用MySQL命令行
mysql -u root -p < backend/src/main/resources/sql/init.sql

-- 方式二：使用Navicat等工具
-- 打开 backend/src/main/resources/sql/init.sql 并执行
```

---

## IDEA 配置步骤

### 第一步：打开项目

1. 打开 IntelliJ IDEA
2. 选择 `File` → `Open`
3. 选择项目根目录 `gengweb`
4. 等待IDEA识别项目（右下角会有进度条）

### 第二步：配置Maven（如需要）

1. 打开 `File` → `Settings` (Windows) 或 `IntelliJ IDEA` → `Preferences` (Mac)
2. 导航到 `Build, Execution, Deployment` → `Build Tools` → `Maven`
3. 检查以下配置：
   - **Maven home path**: 可以使用IDEA内置的Maven，或选择你本地安装的Maven
   - **User settings file**: 默认即可
   - **Local repository**: 默认即可
4. 点击 `Apply` 和 `OK`

### 第三步：导入Maven依赖

1. 在IDEA右侧打开 `Maven` 面板
2. 点击刷新按钮（Reload All Maven Projects）
3. 等待依赖下载完成（首次下载可能需要几分钟）

### 第四步：配置运行环境

#### 方式A：使用Application配置（推荐）

1. 打开 `backend/src/main/java/com/memepedia/MemepediaApplication.java`
2. 点击类名旁边的绿色运行按钮 ▶️
3. 选择 `Edit 'MemepediaApplication'...`
4. 在配置页面中：
   - **Name**: `GengbaikeBackend` (或你喜欢的名字)
   - **Main class**: `com.memepedia.MemepediaApplication`
   - **VM options**: 可以留空，或添加 `-Xmx512m -Xms256m`
   - **Working directory**: `$MODULE_WORKING_DIR$`
   - **Use classpath of module**: `gengbaike-backend`
5. 点击 `Apply` 和 `OK`

#### 方式B：使用Spring Boot配置

1. 点击右上角的运行配置下拉框
2. 选择 `Edit Configurations...`
3. 点击左上角的 `+` 号
4. 选择 `Spring Boot`
5. 配置如下：
   - **Name**: `GengbaikeBackend`
   - **Main class**: `com.memepedia.MemepediaApplication`
   - **Module**: `gengbaike-backend`
   - **Active profiles**: 留空
   - **VM options**: 可选 `-Xmx512m -Xms256m`
6. 点击 `Apply` 和 `OK`

### 第五步：检查数据库配置

确保 `backend/src/main/resources/application.yml` 中的配置正确：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/gengbaike?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password: root123
  data:
    redis:
      host: localhost
      port: 6379
```

如果你的MySQL或Redis密码不同，请修改这里的配置。

### 第六步：运行项目

1. 选择刚才配置的运行配置 `GengbaikeBackend`
2. 点击绿色运行按钮 ▶️ 或按 `Shift + F10`
3. 等待项目启动，看到类似以下日志表示启动成功：

```
Started MemepediaApplication in X.XXX seconds
```

### 第七步：验证运行

打开浏览器访问：`http://localhost:8080/api`

如果返回Whitelabel Error Page，说明后端已经正常启动了！

---

## 常见问题解决

### 问题1：端口被占用

**错误信息**: `Web server failed to start. Port 8080 was already in use.`

**解决方案**:
- 修改 `application.yml` 中的端口号：
  ```yaml
  server:
    port: 8081  # 改成其他端口
  ```
- 或者关闭占用8080端口的程序

### 问题2：数据库连接失败

**错误信息**: `Communications link failure`

**解决方案**:
- 确认MySQL服务已启动
- 检查 `application.yml` 中的数据库地址、用户名、密码是否正确
- 确认数据库 `gengbaike` 已创建

### 问题3：Redis连接失败

**错误信息**: `Unable to connect to Redis`

**解决方案**:
- 确认Redis服务已启动
- 检查Redis地址和端口配置

### 问题4：依赖下载失败

**解决方案**:
- 配置Maven镜像源（阿里云）
- 在 `settings.xml` 或项目的 `pom.xml` 中添加：
  ```xml
  <repositories>
    <repository>
      <id>aliyun</id>
      <url>https://maven.aliyun.com/repository/public</url>
    </repository>
  </repositories>
  ```

---

## 开发调试

### 断点调试
1. 在代码行号左侧点击设置断点
2. 点击调试按钮 🐛 或按 `Shift + F9`
3. 当请求到达断点时会暂停

### 热重载
IDEA配合Spring Boot DevTools可以实现代码修改后自动重启：
1. 修改代码后按 `Ctrl + F9` (Build Project)
2. 项目会自动重新加载

---

## 下一步

后端启动成功后，可以启动前端：

```bash
cd frontend
npm install
npm run dev
```

访问前端地址即可使用完整应用！
