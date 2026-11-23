# wanxinp2p —— 万信金融 P2P 平台

基于 Spring Boot 3 + Spring Cloud 微服务架构的 P2P 金融平台，包含统一账户服务、用户（C 端）服务等微服务，配套网关、注册中心与配置中心。

## 核心组件

### 基础框架

- **Java 21（LTS）**：项目的运行时与编译目标。带来虚拟线程、模式匹配、`switch` 表达式、`record` 等现代语言特性，在并发与代码表达力上都有明显提升。

- **Spring Boot 3.2.12**：整个项目的基础框架，负责自动配置、依赖管理与内嵌 Web 容器，并全面切换到 Jakarta EE 命名空间（`jakarta.*`）。

### 微服务基础设施

- **Nacos**（注册中心 + 配置中心）：统一的微服务治理平台。一方面负责服务的注册与发现，服务启动即自动注册、彼此通过服务名互相发现；另一方面集中管理各服务的配置，支持动态刷新，避免配置散落在各服务中。

- **Spring Cloud Gateway**（网关）：基于 WebFlux / Netty 的响应式网关，作为系统统一入口，负责路由转发、跨域（CORS）处理与请求过滤，配合 Nacos 实现按服务名的动态路由。

- **OpenFeign + Spring Cloud LoadBalancer**（服务调用）：用声明式接口描述服务间调用，配合 LoadBalancer 与 Nacos 自动完成服务发现和客户端负载均衡，让远程调用像调用本地方法一样简洁。

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
| `wanxinp2p-gateway-server` | 网关服务：统一入口、路由转发、跨域处理 |
| `wanxinp2p-account-service` | 统一账户服务：注册、短信验证码、登录等 |
| `wanxinp2p-consumer-service` | 用户（C 端）服务 |

## 环境依赖

| 组件 | 版本 | 启动方式 |
| --- | --- | --- |
| Nacos Server | 2.5.5 | 本地解压，单机模式 |
| MySQL | 8 | Docker |
| Redis | 8.6-alpine | Docker |

