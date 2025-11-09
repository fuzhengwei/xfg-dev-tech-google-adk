# Google Agent ADK，智能体也有现成的框架了！

作者：小傅哥
<br/>博客：[https://bugstack.cn](https://bugstack.cn)

> 沉淀、分享、成长，让自己和他人都能有所收获！😄

大家好，我是技术UP主小傅哥。

💐 从22年至今，小傅哥已经带着大家做了5个AI类项目，包括；`（22年）问答助手`、`（23年）OpenAI应用（含支付、敏感词过滤）`、`（24年）AI 代码自动评审`、`（25年）Ai Agent 智能体`、`（25年）Ai MCP Gateway 网关`。

这些项目也都是结合这，AI 这一年最新的技术动向和应用方向，而做的设计和落地。所以，每次小傅哥都给大家讲了，接下来 AI 将影响的一些场景，也都陆续的发生了。就像，24年11月发布 MCP 协议后，我给大家说，所有互联网企业都将大量的落地 MCP 服务，并开始 Ai Agent 智能体实现（别看市面有 dify、扣子，各个且用还是要做自己的业务智能体）。

随后，25年年初，小傅哥就带着大家开始了 RAG、MCP、Ai Agent 智能体的开发，并告诉大家，以后 Ai Agent 智能体也会出标准的框架，让开发更加容易。这不，**谷歌的 ADK 就来了**。并且这哥们👬🏻还定义A2A协议。这会让不是那么大型的互联网公司，也会具备 Ai Agent 智能体开发的能力。

接下来的几年，所有的业务项目，都会以 Ai Agent 智能体翻一遍，程序员新增的岗位和工作量仍然会很多。因为在咱们这，你做的越快，你就得做的越多！

>接下来，小傅哥就带着大家做一下 Google ADK 搭建 AI Agent。如果你感兴趣 AI 类项目，还可以在文末获取全部实战项目源码，深度积累此类技术内容。

## 一、官网资料

官网：[https://google.github.io/adk-docs/](https://google.github.io/adk-docs/)

<div align="center">
    <img src="/Users/fuzhengwei/Desktop/road-map-google-adk-01.png" width="750px">
</div>
- ADK 以轻便化构建 Ai Agent 智能体，解决智能体开发的复杂流程而设计。目前支持 Python、Java、Go 3种语言对应的技术框架。
- 整个文档完整的描述了，智能体的创建和运行、工具的调用（tools、function、mcp）、可观测性以及 A2A 协议等。

## 二、工程实践

### 1. 前置说明

本次的 Ai Agent 实践，是以 Google ADK 框架为基础，配和 Github [system-prompts-and-models-of-ai-tools](https://github.com/x1xhlol/system-prompts-and-models-of-ai-tools) 开源提示词项目中的 claude-code-system-prompt 作为必要描述。来实验，ELK 系统日志智能分析场景。

- API Key：[https://ai.google.dev/gemini-api](https://ai.google.dev/gemini-api) 需要申请开发 API 秘钥，是免费的。
- Docker 环境，本项目部署了一套 ELK 日志服务，基于 Docker 部署，之后对 ELK 模拟写入日志，让 Ai Agent 智能体进行分析。`如果暂时配置不了，可以在测试的时候去掉这部分 mcp 服务`
- `JDK 17+`、`Maven 3.8.x`

### 2. 工程说明
