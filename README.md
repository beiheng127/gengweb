# 梗百科 (GengBaike)

一个轻量化梗百科项目，支持梗的上传、编辑、审核、评论等功能。

## 项目名称

**梗百科 (GengBaike)** - 让每一个梗都有故事

## 项目特点

- 轻量化架构，适配2核4G服务器
- 支持梗的上传（图片/视频/音频）
- 类似维基百科的版本管理和审核机制
- 完整的标签分类系统
- 点赞/点踩/浏览量统计
- 年度梗评选
- 评论系统
- 后台管理和数据分析
- 移动端适配

## 技术栈

### 后端
- Spring Boot 3.2.0
- MyBatis-Plus 3.5.5
- MySQL 8.0
- Redis 7.0
- JWT认证
- Druid连接池

### 前端
- Vue 3 + TypeScript
- Vite 5
- Element Plus
- ECharts
- Markdown编辑器

### 部署
- Docker + Docker Compose
- Nginx

## 快速开始

### 方案一：本地运行（推荐用于开发）

#### 前置条件
- Java 17+
- Maven 3.8+
- MySQL 8.0+
- Redis 7.0+
- Node.js 18+

#### 1. 数据库准备

创建数据库并执行初始化脚本：

```sql
-- 数据库创建
CREATE DATABASE IF NOT EXISTS memepedia DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 执行初始化脚本
source backend/src/main/resources/sql/init.sql;
```

#### 2. 后端运行

```bash
cd backend

# 修改 application.yml 中的数据库和Redis连接配置
# spring.datasource.url
# spring.datasource.username
# spring.datasource.password
# spring.data.redis.host
# spring.data.redis.port

# 编译并运行
mvn clean package -DskipTests
java -jar target/memepedia-backend.jar
```

#### 3. 前端运行

```bash
cd frontend

# 安装依赖
npm install

# 开发模式运行
npm run dev

# 生产构建
npm run build
```

### 方案二：Docker Compose运行（推荐用于部署）

#### 前置条件
- Docker Desktop (Windows/Mac) 或 Docker Engine (Linux)
- Docker Compose

#### 1. 启动所有服务

```bash
# 在项目根目录执行
docker-compose up -d

# 查看服务状态
docker-compose ps

# 查看日志
docker-compose logs -f backend
docker-compose logs -f frontend
```

#### 2. 访问应用

- 前端：http://localhost
- 后端API：http://localhost:8080/api

#### 3. 停止服务

```bash
# 停止并删除容器（保留数据）
docker-compose down

# 停止并删除容器和数据
docker-compose down -v
```

## 默认账号

| 角色 | 用户名 | 密码 |
|------|--------|------|
| 管理员 | admin | admin123 |
| 审核员 | auditor | admin123 |
| 普通用户 | user1 | admin123 |

## 项目结构

```
gengweb/
├── backend/                 # 后端项目
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/memepedia/
│   │   │   │   ├── common/      # 通用类
│   │   │   │   ├── config/      # 配置类
│   │   │   │   ├── controller/  # 控制器
│   │   │   │   ├── entity/      # 实体类
│   │   │   │   ├── mapper/      # 数据访问层
│   │   │   │   └── service/     # 业务逻辑层
│   │   │   └── resources/
│   │   │       ├── mapper/      # MyBatis映射文件
│   │   │       ├── sql/         # 数据库脚本
│   │   │       └── application.yml
│   ├── Dockerfile
│   └── pom.xml
├── frontend/                # 前端项目
│   ├── src/
│   │   ├── api/            # API接口
│   │   ├── components/     # 组件
│   │   ├── composables/    # 组合式函数
│   │   ├── router/         # 路由
│   │   ├── store/          # 状态管理
│   │   ├── types/          # TypeScript类型
│   │   ├── utils/          # 工具函数
│   │   ├── views/          # 页面
│   │   ├── App.vue
│   │   └── main.ts
│   ├── Dockerfile
│   ├── package.json
│   ├── tsconfig.json
│   └── vite.config.ts
└── docker-compose.yml
```

## 功能模块

### 1. 用户模块
- 用户注册/登录
- 用户信息管理
- 权限管理（普通用户/审核员/管理员）
- 登录状态持久化

### 2. 梗管理模块
- 梗的上传（图片/视频/音频）
- 梗的编辑（版本管理）
- 梗的审核
- 梗的搜索（支持模糊搜索）
- 标签分类

### 3. 互动模块
- 点赞/点踩
- 评论系统（支持回复）
- 浏览量统计

### 4. 后台管理模块
- 梗审核
- 用户管理
- 标签管理
- 数据分析（ECharts图表）

## 资源限制（2核4G服务器）

| 服务 | CPU限制 | 内存限制 |
|------|---------|----------|
| MySQL | 0.5核 | 1GB |
| Redis | 0.25核 | 1GB |
| 后端 | 0.75核 | 1GB |
| 前端 | 0.5核 | 512MB |

## 开发计划

- [x] 基础框架搭建
- [x] 用户认证系统
- [x] 数据库设计
- [x] 梗上传功能
- [x] 版本管理和审核
- [x] 评论系统
- [x] 后台管理
- [x] 数据分析
- [x] 移动端适配
- [x] 登录状态持久化

## 贡献

欢迎提交Issue和Pull Request！

## 许可证

MIT License
