# 同学电商项目代码对比静态巡检

巡检时间：2026-06-06

巡检范围：

- 本项目：`/Users/oohyees/Projects/NEU/sem6/web`
- 对比目录：`/Users/oohyees/Desktop/students`
- 依据文件：`docs/course/A0801051040-Web开发技术-实验指导书-2026.md`

说明：本报告基于静态扫描、README、`pom.xml`、`package.json`、Docker/SQL 文件、源文件命名和接口命名做对比。没有逐个启动运行，所以功能覆盖属于“代码与文档证据层面”的判断，不等价于实际可演示通过。

## 指导书核心评分点

课程要求可压缩为四类风险：

1. 基础 20 分：客户端商品、购物车、订单；后台管理员、数据、用户、商品、订单。
2. 进阶 20 分：Element Plus、Pinia、认证鉴权、界面、用户认证、首页、地址、规格/优惠券/秒杀、轮播、系统管理、导入导出、响应式、上传、分页。
3. 拓展 10 分：Redis、微服务、Docker。
4. 报告 10 分：实验目的、内容、环境、过程与分析、创新点、总结和提交规范。

## 目录到同学映射

| 同学/目录 | 代码目录 | 备注 |
| --- | --- | --- |
| 董耕赫 | `commerce-web-course-code` | zip 顶层也是该目录，轻量微服务示例风格明显。 |
| 董新畅 | `JavaWebExp` | zip 顶层是 `JavaWebExp`，README 记录从骨架逐轮演进。 |
| 代宇霆 | `20236917-代宇霆-软件2401-大作业-工程源码` | 多模块后端、双前端、图片资源较多。 |
| 周梦玲 | `e_commerce` | zip 顶层是 `e_commerce`，包含 RabbitMQ、WebSocket、OSS/SMTP/支付配置说明。 |
| 李博韬 | `web-dev-experiment` | zip 顶层是 `web-dev-experiment`，体积异常大，包含大量不应提交内容。 |
| 杨浩伟 | `20246152-杨浩伟-软件2403-大作业-工程压缩包` | 精简微服务，单前端聚合客户端和后台。 |
| 李旋 | `20231985-李旋-软件2303班-代码` | 包含 `.git`、`.npm-cache`、数据库数据文件，提交包体积严重超标。 |
| 李伟翔 | `20246176-李伟翔-软件2402-大作业-工程压缩包` | Nacos/OpenFeign 证据较强，但 zip 包含 `.git`、`.idea`、`.claude`。 |
| 郭振顺 | `20246047-郭振顺-软件2402-大作业-工程压缩包` | 多个后端服务，README 基本还是脚手架模板，缺 SQL 顶层证据。 |
| 张臻 | `20245738-张臻-软件2402-大作业-工程压缩包` | ShopSphere，服务拆分较完整，源码包很精简。 |

## 总览对比

| 项目 | 后端形态 | 前端形态 | 数据/部署 | 主要强项 | 主要风险 |
| --- | --- | --- | --- | --- | --- |
| 本项目 | 当前 Vue 前端 + Gateway 主验收，legacy 单体作传统 Web 对照证据 | shop/admin 双 Vue3 | MySQL、Redis、Docker、MailHog | 主线可验收、证据和报告矩阵完整，SMTP 验证真实 | 需要继续做浏览器人工巡检和截图新鲜度检查 |
| 董耕赫 | `api` 兼容层 + user/catalog/order 轻量拆分 | store/admin 双 Vue3 | Docker Compose、MySQL、Redis、Flyway | README 与评分点覆盖清楚，接口/页面命名完整 | 邮箱验证码 README 写固定 `123456`，微服务偏展示型 |
| 董新畅 | user/product/order/admin/marketing/gateway 多模块 | user/admin 双 Vue3 | 多 SQL 迁移、Dockerfile | 功能点非常接近指导书，代码量足 | 无顶层 compose；README 前半仍保留“未实现”历史叙述，需防文档自相矛盾 |
| 代宇霆 | auth/user/product/order/admin/gateway | user/admin 双 Vue3 | deploy compose、Redis、Excel | README 功能覆盖最满之一，上传/客服/调研/DeepSeek 亮点多 | AI/DeepSeek 属额外亮点但也可能引入无法演示配置风险 |
| 周梦玲 | gateway + auth/user/product/cart/order/chat | client/admin 双 Vue3 | Docker Compose、RabbitMQ、Redis、MySQL 外置 | 功能和创新点最多，含消息队列、WebSocket、6 库拆分 | 引入 RabbitMQ、OSS、支付宝等复杂依赖，课堂演示失败面更大 |
| 李博韬 | hmall 风格多服务，含 Nacos/OpenFeign | hmall-web/admin | Docker、Nacos、SQL、e2e | 微服务工程化最重，测试/文档多 | zip 215M 严重超过 50M，且包含大量缓存/生成物 |
| 杨浩伟 | gateway + user/product/order/common | 单 Vue3 聚合前后台 | Docker Compose、Redis、SQL | 源码包小，功能点覆盖面广 | 服务转发像手写 GatewayController，不一定是真 Spring Cloud Gateway；Java 23 环境要求偏高 |
| 李旋 | gateway + user/product/order/common-web | shop/admin 双 Vue3 | Docker、Nacos、SQL、JSP 演示 | 同时补了 Servlet/JSP/JDBC 课程传统点 | zip 532M 严重超标，包含 `.git`、`.npm-cache`、`.ibd/.sdi` 数据文件 |
| 李伟翔 | gateway + user/product/cart/order/coupon/admin | front/admin 双 Vue3 | Docker、Nacos/OpenFeign、Redis | 服务拆分细，优惠券、秒杀、操作日志等较全 | zip 包含 `.git`、`.idea`、`.claude`，提交规范风险 |
| 郭振顺 | user/product/order/trade 四后端 | 单 Vue3 | 多 compose、Redis | 控制器/页面命名显示基础功能覆盖较广 | README 仍是 Vue/Spring Initializr 模板；未发现 SQL 文件，数据库可复现性风险高 |
| 张臻 | gateway + auth/user/product/cart/order/payment | client/admin 双 Vue3 | Docker Compose、Nacos/OpenFeign、SQL | 支付服务单独拆分，README 清晰，包很小 | 前端 `.vue` 页面数量偏少，管理端/客户端是否完整需运行确认 |

## 技术栈共性

共同点：

- 前端基本都是 Vue 3 + Vite + Element Plus + Pinia + Axios。
- 后端基本都是 Spring Boot 3 + MyBatis/MyBatis-Plus + MySQL。
- 认证普遍使用 JWT，部分使用 Spring Security，部分使用 Shiro。
- 进阶项普遍覆盖 Redis、Docker、上传、Excel、分页、轮播、公告、反馈、优惠券/秒杀。

差异点：

- 微服务真实性差异大：有的是 Spring Cloud Gateway + Nacos + OpenFeign，有的是手写 GatewayController 或 `api` 聚合层展示。
- 数据库拆分差异大：周梦玲、李旋、张臻、李伟翔更偏多库/多服务；董耕赫和本项目更偏主库 + 渐进式拆分。
- 邮箱验证码实现差异大：有的是真 SMTP/邮件服务，有的 README 明示固定验证码或日志模式。
- 提交规范差异很大：源码包最容易丢分的不是功能，而是包含 `.git`、缓存、构建产物、数据库数据文件导致超 50M。

## 相似度与同源线索

静态扫描没有发现高置信的“大量完全相同源码哈希重叠”。但有几类同源/模板线索：

1. `commerce-web-course-code` 与董耕赫 zip 完全对应，属于同一个项目目录。
2. `JavaWebExp` 与董新畅 zip 对应。
3. `e_commerce` 与周梦玲 zip 对应。
4. `web-dev-experiment` 与李博韬 zip 对应。
5. 多数项目都使用课程电商通用命名：`ProductController`、`CartController`、`OrderController`、`AddressController`、`DashboardController`、`Banner`、`Coupon`、`Review`、`Favorite`，这属于需求天然重合，不能单独作为抄袭证据。
6. 李博韬项目明显带有黑马商城/hmall 命名体系；张臻是 ShopSphere 命名体系；这两者与本项目命名体系差异较明显。

## 对本项目的借鉴价值

最值得借鉴但不建议大改的点：

- 代宇霆：README 的功能概览写得很完整，适合借鉴报告表达方式，但不要引入 DeepSeek 等新依赖。
- 张臻：README 的“常用验证”段很适合作为答辩材料结构，验证命令清楚。
- 李伟翔/李旋：Nacos/OpenFeign、JSP/JDBC 证据比较显眼，可提醒我们在报告中强调已有 Nacos/Feign 证据和 legacy-web 课程传统点。
- 周梦玲：WebSocket 客服、RabbitMQ、OSS、支付宝属于展示亮点，但对当前项目是高风险扩展，不建议临近提交加入。
- 董耕赫：轻量微服务作为创新证据路径，与本项目“Vue 前端 + Gateway 主验收，legacy 作对照证据”的策略有可比性，可作为论证参考。

本项目当前相对优势：

- 已有 `docs/course/acceptance/checklist.md` 对照指导书评分点，不只是 README 自述。
- 已跑通过 `mvn test`、前端 build、monolith smoke、microservice smoke、gateway full smoke、submission zip。
- 提交包脚本已验证约 4.4M，明显优于多个同学的超大 zip。
- 邮箱验证码是真 SMTP + Docker MailHog 流程，不是固定验证码。
- 接受路径已收敛：当前 Vue 前端 + Gateway 是主页面验收路径，legacy 用于传统 Java Web 证据和兜底回归。

本项目当前短板：

- 还缺一次浏览器人工视觉巡检，尤其移动宽度和 admin drawer。
- 关键截图要确认与当前 UI/文档一致。
- Git 文档迁移状态需要整理，避免提交时出现旧路径删除/新路径未跟踪的混乱。

## 提交规范风险排名

高风险：

- 李旋：zip 约 532M，含 `.git`、`.npm-cache`、数据库 `.ibd/.sdi` 文件，远超 50M。
- 李博韬：zip 约 215M，含大量缓存/生成物，远超 50M。

中风险：

- 李伟翔：zip 约 12M 未超 50M，但包含 `.git`、`.idea`、`.claude`。
- 杨浩伟：zip 约 0.5M 很小，但 zip 清单显示有少量 `.git` 相关条目。

低风险：

- 董耕赫、董新畅、张臻、周梦玲、郭振顺、代宇霆的 zip 大小基本可控或未发现严重缓存目录；其中郭振顺 24M 接近但仍低于 50M。

## 结论

按“降低我们大作业评分风险”的目标看，不建议再照搬任何同学的大功能。我们的更优路径是继续补齐验收证据：浏览器人工巡检、截图新鲜度、文档迁移清理、最终提交包命名与个人信息确认。

如果只看功能野心，周梦玲、代宇霆、李伟翔、张臻更丰富；如果看提交可控性和评分稳定性，本项目当前路线更稳。多个同学项目虽然功能写得多，但存在依赖复杂、README 与源码状态不一致、提交包超标或不可复现的问题，这些都是答辩和评分时的实际风险。
