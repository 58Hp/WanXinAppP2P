# wanxinp2p —— 万信金融 P2P 平台

基于 Spring Boot 3 + Spring Cloud 微服务架构的 P2P 金融平台，包含统一账户服务、用户（C 端）服务、统一认证授权（UAA）服务等微服务，配套网关、注册中心与配置中心，并通过 Seata 实现跨服务的分布式事务。

## 核心组件

### 基础框架

- **Java 21（LTS）**：项目的运行时与编译目标。带来虚拟线程、模式匹配、`switch` 表达式、`record` 等现代语言特性，在并发与代码表达力上都有明显提升。

- **Spring Boot 3.2.12**：整个项目的基础框架，负责自动配置、依赖管理与内嵌 Web 容器，并全面切换到 Jakarta EE 命名空间（`jakarta.*`）。

- **Spring Cloud 2023.0.3 / Spring Cloud Alibaba 2023.0.3.4**：微服务生态组件版本集合，统一管理网关、Nacos、OpenFeign、LoadBalancer 等组件的版本，保证各组件之间的兼容性。

### 微服务基础设施

- **Nacos**（注册中心 + 配置中心）：统一的微服务治理平台。一方面负责服务的注册与发现，服务启动即自动注册、彼此通过服务名互相发现；另一方面集中管理各服务的配置，支持动态刷新，避免配置散落在各服务中。

- **Spring Cloud Gateway**（网关）：基于 WebFlux / Netty 的响应式网关，作为系统统一入口，负责路由转发、跨域（CORS）处理与请求过滤，配合 Nacos 实现按服务名的动态路由。同时作为**资源服务器**，在入口处统一校验 JWT，放行登录/短信等匿名接口，其余请求需携带有效令牌。

- **OpenFeign + Spring Cloud LoadBalancer**（服务调用）：用声明式接口描述服务间调用，配合 LoadBalancer 与 Nacos 自动完成服务发现和客户端负载均衡，让远程调用像调用本地方法一样简洁。

### 认证与授权

- **Spring Authorization Server 1.2.7**（授权服务器）：UAA 服务的核心，负责颁发 JWT 令牌、暴露 OAuth2 / OIDC 端点（令牌、JWK、内省、撤销等），并基于 JDBC 持久化客户端与授权信息。

- **短信验证码扩展授权**：由于 OAuth 2.1 移除了密码模式，项目自定义了一个扩展授权类型 `urn:wanxinp2p:oauth:grant-type:sms`，用于「手机号 + 短信验证码」登录。短信验证码的校验由账户服务完成，UAA 按手机号查询账户后签发 JWT。

- **Spring Security OAuth2 Resource Server**（资源服务器）：用于校验 JWT。网关作为统一入口验签；UAA 自身的过滤链也具备资源服务器能力，保护 `/userinfo` 等自身端点。

### 分布式事务

- **Seata 2.0.0**（AT 模式）：跨服务数据一致性的解决方案。以 Nacos 为注册中心，基于「两阶段提交 + undo 日志」实现自动回滚。业务方法标注 `@GlobalTransactional` 即可将多个服务的本地事务串成全局事务（如用户注册时 Consumer 与 Account 的双写），各参与方数据库需维护 `undo_log` 表。

### 数据层与开发效率

- **MyBatis-Plus 3.5.5**（ORM）：在 MyBatis 之上提供无侵入增强，内置通用 CRUD、条件构造器（`LambdaQueryWrapper`）、分页插件，大幅减少手写 SQL，提升数据访问层开发效率。

- **MapStruct 1.5.5.Final**（对象映射）：在编译期生成类型安全、高性能的对象转换代码（DTO ↔ Entity），相比反射式映射性能更好，且映射错误在编译期即可暴露。

- **springdoc-openapi 2.5.0**（接口文档）：自动扫描 Spring MVC 接口生成 OpenAPI 3.0 文档，并提供 swagger-ui 页面，支持在线查看与调试接口，减少接口文档维护成本。

- **Lombok**（样板代码精简）：通过 `@Data`、`@Slf4j` 等注解在编译期自动生成 getter/setter、日志对象等样板代码，让代码更聚焦业务逻辑。

## 模块结构

| 模块 | 职责 |
| --- | --- |
| `wanxinp2p-common` | 公共工具类、统一返回体（RestResponse）、异常与错误码 |
| `wanxinp2p-api` | 对外服务接口定义（Feign 契约）与 DTO 模型 |
| `wanxinp2p-gateway-server` | 网关服务：统一入口、路由转发、跨域处理、JWT 验签（资源服务器） |
| `wanxinp2p-account-service` | 统一账户服务：注册、短信验证码、按手机号查账户、登录等 |
| `wanxinp2p-consumer-service` | 用户（C 端）服务：注册（与账户服务组成分布式事务）等 |
| `wanxinp2p-uaa-service` | 统一认证授权服务：基于 Spring Authorization Server 签发 JWT，支持短信验证码扩展授权登录 |

## 环境依赖

| 组件 | 版本 | 启动方式 |
| --- | --- | --- |
| Nacos Server | 2.5.5 | 本地解压，单机模式 |
| Seata Server | 2.0.0 | 本地解压，注册到 Nacos |
| MySQL | 8 | Docker |
| Redis | 8.6-alpine | Docker |
