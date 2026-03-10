# ZhihuNet - 知乎用户行为分析、推荐系统

![项目banner](https://picsum.photos/id/1/1200/300)

## 项目简介

> 不定时更新......

ZhihuNet 是配套 2025 CIKM 会议论文 [IPNet: An Interaction Pattern-aware Neural Network for Temporal Link Prediction](https://doi.org/10.1145/3746252.3761063) 研发的全栈项目，基于 SpringBoot 4.x 微服务架构构建，前端选用 Vue3，是集知乎数据爬虫、用户数据分析、时序链接预测、个性化推荐于一体的完整系统。

### 项目亮点

1. **企业级工程规范**：多模块分层设计、统一异常处理、参数校验、全链路日志归档、单元测试覆盖；
2. **分布式微服务架构**：基于 Dubbo + Nacos 实现服务解耦，模块职责清晰，支持横向扩展与独立部署；
3. **知乎反爬适配**：适配知乎接口加密规则（x-zse-93/x-zse-96 等参数），解决接口校验问题；
4. **完整的权限体系**：基于 JWT + Redis 实现分布式鉴权，AOP 切面拦截，支持多角色权限控制；
5. **数据可视化**：知乎用户数据多维度展示、行为时序分析、互动关系可视化（TODO）

### 项目地址

1. 后端主仓库：https://github.com/ACPasser/ZhihuNet
2. 前端代码仓库：https://github.com/ACPasser/ZhihuNet-frontend
3. IPNet 模型仓库：https://github.com/ACPasser/IPNet

## 目录

- [快速上手](#快速上手)
- [核心模块](#核心模块)
- [技术架构](#技术架构)
- [接口文档](#接口文档)
- [免责声明](#免责声明)

## 快速上手

### 1. 开发环境

| 环境 / 中间件 | 版本要求 |             说明             |
| :-----------: | :------: | :--------------------------: |
|      JDK      |   21+    |              -               |
|     Maven     |   3.8+   |              -               |
|     MySQL     |   8.0+   |              -               |
|     Nacos     |   2.x    |              -               |
|     Redis     |   6.x+   | 缓存 Token、验证码、热点数据 |
|    Node.js    | 22.19.0+ |           前端环境           |

### 2. 准备工作

1. 克隆仓库

```bash
git clone https://github.com/ACPasser/ZhihuNet.git
cd ZhihuNet
```

2. 初始化数据库

​	执行数据库初始化脚本：`zhihu_database_init.sql`

3. 核心中间件
   1. Nacos 服务（默认地址：http://localhost:8848）
   2. Redis 服务（默认地址：[localhost:6379](http://localhost:6379)）

4. 修改配置文件（删除.template后缀 && 按照注释改写）
   1. ZhihuNet-console/src/main/resources/application.yml.template
   2. ZhihuNet-crawler/src/main/resources/application.yml.template

### 3. 项目构建

```bash
# 全量构建（跳过测试，推荐）
mvn clean install -U -DskipTests
```

执行成功后，终端会输出：<font color="#008000" style="font-weight: bold;">BUILD SUCCESS</font>

### 4. 服务启动

1. 启动 Nacos 服务

```bash
# 进入 Nacos 安装目录的 bin 文件夹
cd /usr/local/nacos/bin # 替换为你的 Nacos 实际路径

# Linux/Mac 启动（单机模式）
sh startup.sh -m standalone
```

2. 启动 Redis

```bash
# Linux/Mac 启动（单机模式）
redis-server
```

3. 启动 console 服务和 crawler 服务：IDEA / jar包


```bash
# jar包启动
cd ZhihuNet-console/target
java -jar ZhihuNet-console-0.0.1-SNAPSHOT.jar
```

### 5. 访问验证

- 测试账号：本地部署完成后，可通过注册接口自行创建账号。

- 后端接口地址：http://localhost:8080

- SpringDoc 接口文档：http://localhost:8081/swagger-ui/index.html

- 管理员控制台：http://localhost:8080/console（若有前端页面）

## 核心模块

### 1. 用户管控模块（ZhihuNet-console）

- **权限认证体系**：基于 JWT 实现无状态鉴权，Token 存入 Redis 有效期 24 小时，支持自动续期与登出注销

- **用户全生命周期管理**：注册、登录、个人信息维护、头像上传、密码重置、账号注销

- **邮箱服务**：集成 Spring Mail，通过 QQ 邮箱 SMTP 服务发送注册 / 密码重置验证码，验证码 Redis 缓存 1 分钟

- **安全管控**：密码 MD5 加盐加密存储，基于 AOP 实现请求拦截与鉴权，非法请求自动拦截

- **日志体系**：基于 Log4j2 实现多级别日志分离（INFO/DEBUG/ERROR）、异步写入、按大小 / 时间滚动归档，支持动态调整日志级别

### 2. 爬虫模块（ZhihuNet-crawler）

- **知乎接口适配**：适配知乎最新接口加密规则，解决 x-zse-93/x-zse-96 签名校验问题（参考逆向方案：https://github.com/srx-2000/spider_collection/issues/18）
- **数据爬取**：基于 HttpClient5 + Jsoup 实现知乎用户信息、互动行为、关注关系、问答内容等数据的爬取
- **分布式服务暴露**：基于 Dubbo 实现爬虫服务接口化，通过 Nacos 注册中心供核心业务模块调用
- **定时同步**：支持手动触发 / 定时任务两种模式，同步知乎热门数据、用户行为数据至本地数据库（TODO）
- **单元测试覆盖**：基于 JUnit + Mockito 实现核心爬虫逻辑单测，保证服务稳定性

### 3. 核心业务模块（ZhihuNet-core）

- **业务逻辑编排**：用户行为分析、知乎数据同步、IPNet 模型调用、推荐结果生成等核心业务逻辑实现
- **Dubbo 服务调用**：作为消费者调用爬虫模块的 RPC 服务，实现业务与爬虫逻辑解耦
- **模型集成**：封装 IPNet 时序链接预测模型 API，实现用户交互模式挖掘、时序链接预测、个性化推荐
- **数据处理**：知乎用户行为数据清洗、特征提取、时序序列构建，为模型预测提供标准化输入

### 4. 数据访问模块（ZhihuNet-repository）

- **数据持久化**：基于 MyBatis 实现数据库操作，封装通用 Mapper 接口，支持复杂查询与分页
- **缓存管理**：基于 Spring Data Redis 实现热点数据缓存、分布式锁、Token 与验证码存储

### 5. 工具模块（ZhihuNet-tool）

- **代码生成**：配套 MyBatisGenerator 工具，一键生成实体类、Mapper 接口、XML 映射文件

## 技术架构

### 1. 后端依赖

- **核心框架**：SpringBoot 4.x + Spring Framework 6.x
- **微服务与 RPC**：Dubbo 3.x + Nacos 2.x
- **数据访问**：MyBatis 3.x
- **数据库与缓存**：MySQL 8.0+ + Redis 6.x+
- **权限认证**：JWT 4.x
- **爬虫核心**：HttpClient5 + Jsoup + Fastjson2
- **日志与测试**：Log4j2 + JUnit 5 + Mockito

### 2. 前端技术栈

- 核心框架：Vue3 + Element Plus
- 路由管理：Vue Router
- 状态管理：Pinia
- HTTP 客户端：Axios
- 可视化：ECharts

### 3. 中间件与工具

- 服务注册与配置中心：Nacos 2.x
- 缓存：Redis 6.x+
- 数据库：MySQL 8.0+
- 构建工具：Maven 3.8+
- 版本控制：Git
- 接口文档：SpringDoc OpenAPI 3.0
- 代码生成：MyBatisGenerator

## 接口文档

### 1. 在线文档

项目集成 SpringDoc OpenAPI 3.0，服务启动后可直接访问在线接口文档，支持在线调试：

- Swagger 接口文档：http://localhost:8080/swagger-ui.html
- OpenAPI 规范地址：http://localhost:8080/v3/api-docs

### 2. 离线文档

- Postman 集合：[Zh](https://documenter.getpostman.com/view/xxx/xxx)[ihuNe](https://documenter.getpostman.com/view/xxx/xxx)[t API](https://documenter.getpostman.com/view/xxx/xxx)[ 集合](https://documenter.getpostman.com/view/xxx/xxx)（TODO）

## 免责声明

1. 本项目仅用于**学术研究与学习交流**，严禁用于任何商业用途，请勿滥用接口爬取知乎平台数据。
2. 项目中涉及的知乎接口逆向、爬虫相关代码，仅用于技术研究，使用者需遵守《网络安全法》《数据安全法》等相关法律法规，请勿非法获取、存储、使用他人数据。
3. 使用者因违反本声明或相关法律法规造成的一切后果，由使用者自行承担，项目开发者不承担任何法律责任。
