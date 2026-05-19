# AGENTS

## 工作原则

- 目标是大作业满分，结果优先
- 允许最大化复用成熟开源资源，最终保留来源说明即可
- 不要继续慢速手搓已有成熟范式
- **全部测试通过才算完成**；新增功能必须带对应验证

## 当前技术决策

- 主应用仍是 Vue 3 + Vite + Element Plus + Pinia / Spring Boot + MyBatis + MySQL
- 已接 Redis、Docker
- 微服务目录已升级为三个独立 Spring Boot 小工程；这是课程验收导向的最小真拆分，不要再退回“同源多实例”的旧路线
- 鉴权仍是 token 方案；后台接口必须区分管理员权限

## 文档职责

- `README.md`：最短运行入口
- `PROJECT_STATE.md`：当前真实状态、已测内容、未完成项
- `MEMORY.md`：用户偏好、路线选择、接手时最重要的上下文
- `ACCEPTANCE_REPORT.md`：按采分表的正式验收结果
- `ISSUE_LOG.md`：本轮已修问题与仍需说明的问题
- 不写流水账，不写冗长过程

## 接手先读

1. `HANDOFF.md`
2. `PROJECT_STATE.md`
3. `ACCEPTANCE_REPORT.md`
4. `ISSUE_LOG.md`
5. `SCORING_MAP.md`
6. `MEMORY.md`
7. 再看指导书与当前代码

## 当前优先级

1. 响应式专项复测
2. 最终截图与视觉收口
3. 真实本机前端构建证明
4. 实验报告、工程压缩包
