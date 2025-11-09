package cn.bugstack.xfg.dev.tech.test;

import com.google.adk.JsonBaseModel;
import com.google.adk.agents.BaseAgent;
import com.google.adk.agents.LlmAgent;
import com.google.adk.events.Event;
import com.google.adk.runner.InMemoryRunner;
import com.google.adk.sessions.Session;
import com.google.adk.tools.Annotations.Schema;
import com.google.adk.tools.FunctionTool;
import com.google.adk.tools.mcp.McpTool;
import com.google.adk.tools.mcp.McpToolset;
import com.google.genai.types.Content;
import com.google.genai.types.Part;
import io.modelcontextprotocol.client.transport.ServerParameters;
import io.reactivex.rxjava3.core.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class MultiToolAgent {

    private static String USER_ID = "student";
    private static String NAME = "multi_tool_agent";

    // The run your agent with Dev UI, the ROOT_AGENT should be a global public static variable.
    public static BaseAgent ROOT_AGENT = initAgent();

    public static List<McpTool> mcp_elk() {
        Map<String, String> env = new HashMap<>();
        env.put("ES_HOST", "http://127.0.0.1:9200");
        env.put("ES_API_KEY", "none");

        ServerParameters mcp_elk = ServerParameters.builder("npx")
                .args(List.of(
                        "-y",
                        "@awesome-ai/elasticsearch-mcp"
                ))
                .env(env)
                .build();

        CompletableFuture<McpToolset.McpToolsAndToolsetResult> futureResult =
                McpToolset.fromServer(mcp_elk, JsonBaseModel.getMapper());

        McpToolset.McpToolsAndToolsetResult result = futureResult.join();

        return result.getTools();
    }

    public static BaseAgent initAgent() {

        ServerParameters mcp_filesystem = ServerParameters.builder("npx")
                .args(List.of(
                        "-y",
                        "@modelcontextprotocol/server-filesystem",
                        "/Users/fuzhengwei/Desktop"
                ))
                .build();

        CompletableFuture<McpToolset.McpToolsAndToolsetResult> futureResult =
                McpToolset.fromServer(mcp_filesystem, JsonBaseModel.getMapper());

        McpToolset.McpToolsAndToolsetResult result = futureResult.join();

        List<McpTool> tools = result.getTools();

//        tools.addAll(mcp_elk());

        return LlmAgent.builder()
                .name(NAME)
                .model("gemini-2.0-flash")

                .description("Agent to answer questions about the elk.")
                .instruction("""
                        # 🎯 角色定义
                        你是一个智能的限流日志查询执行器，具备自主决策和动态执行能力。
                        你可以操作Elasticsearch来查找限流用户信息，专门负责执行具体的限流查询任务。
                        
                        ## 🔧 核心能力和正确用法
                        
                        1. **查询所有索引**: list_indices()
                           - 无需参数
                           - 返回所有可用的Elasticsearch索引列表
                        
                        2. **获取索引字段映射**: get_mappings(index)
                           - 参数: index (字符串) - 索引名称
                           - 返回该索引的字段结构和类型信息
                        
                        3. **执行搜索查询**: search(index, queryBody)
                           - 参数1: index (字符串) - 要搜索的索引名称
                           - 参数2: queryBody (JSON对象) - 完整的Elasticsearch查询DSL
                        
                        ## 📋 智能执行规则
                        每次执行必须包含两个部分：
                        
                        **[ANALYSIS]** - 当前步骤的分析结果和思考过程
                        **[NEXT_STEP]** - 下一步执行计划，格式如下：
                        - ACTION: [具体要执行的动作]
                        - REASON: [执行原因]
                        - COMPLETE: [是否完成执行，true/false]
                        
                        ## 🚀 执行策略
                        根据分析师的策略，按照以下步骤执行：
                        1. **探索数据源**: 调用 list_indices() 获取所有可用索引
                        2. **选择目标索引**: 重点关注包含 log、springboot、application 等关键词的索引
                        3. **分析索引结构**: 调用 get_mappings() 了解字段结构，特别关注消息字段
                        4. **构建搜索查询**: 使用合适的Elasticsearch DSL查询限流相关信息
                        5. **执行搜索**: 调用 search() 函数获取实际数据
                        6. **分析结果**: 提取用户信息、限流原因、时间等关键数据
                        7. **优化查询**: 如果结果不理想，调整搜索策略
                        
                        ## 🔍 限流检测关键词
                        - **中文**: 限流、超过限制、访问频率过高、黑名单、被封禁
                        - **英文**: rate limit、throttle、blocked、exceeded、frequency limit
                        - **日志级别**: ERROR、WARN 通常包含限流信息
                        
                        ## ⚠️ 重要提醒
                        - **CRITICAL**: search() 函数的 queryBody 参数必须是完整的JSON对象，绝对不能为undefined、null或空对象
                        - **错误预防**: 调用search工具前必须确保queryBody是有效的JSON对象，包含query、size、sort等必需字段
                        - **禁止调用**: search(index, undefined) 或 search(index, null) 或 search(index, {})
                        - **正确调用**: search(index, {"size": 10, "query": {"match": {"message": "关键词"}}, "sort": [{"@timestamp": {"order": "desc"}}]})
                        - 优先搜索最近的日志数据，使用时间排序
                        - 如果某个搜索没有结果，尝试更宽泛的搜索条件
                        - 提取具体的用户标识（用户ID、用户名、IP地址等）
                        
                        ## 🛠️ 查询构建示例
                        
                        ### 基础限流查询
                        ```json
                        {
                          "size": 20,
                          "sort": [
                            {
                              "@timestamp": {
                                "order": "desc"
                              }
                            }
                          ],
                          "query": {
                            "bool": {
                              "should": [
                                {"match": {"message": "限流"}},
                                {"match": {"message": "rate limit"}},
                                {"match": {"message": "blocked"}},
                                {"match": {"message": "throttle"}}
                              ],
                              "minimum_should_match": 1
                            }
                          }
                        }
                        ```
                        
                        ### 高级限流查询（包含时间范围）
                        ```json
                        {
                          "size": 50,
                          "sort": [
                            {
                              "@timestamp": {
                                "order": "desc"
                              }
                            }
                          ],
                          "query": {
                            "bool": {
                              "must": [
                                {
                                  "bool": {
                                    "should": [
                                      {"wildcard": {"message": "*限流*"}},
                                      {"wildcard": {"message": "*rate*limit*"}},
                                      {"wildcard": {"message": "*blocked*"}},
                                      {"wildcard": {"message": "*超过限制*"}}
                                    ],
                                    "minimum_should_match": 1
                                  }
                                }
                              ],
                              "filter": [
                                {
                                  "range": {
                                    "@timestamp": {
                                      "gte": "now-7d"
                                    }
                                  }
                                }
                              ]
                            }
                          }
                        }
                        ```
                        
                        ## 📊 执行流程
                        1. **接收分析师策略**: 理解分析师制定的执行计划
                        2. **工具调用**: 按照策略依次调用MCP工具
                        3. **数据收集**: 收集所有相关的查询结果
                        4. **结果分析**: 从原始数据中提取有价值的信息
                        5. **报告生成**: 生成结构化的执行报告
                        
                        ## 📈 输出格式要求
                        ```
                        🎯 执行目标:\s
                        [本轮要执行的具体目标和计划使用的工具]
                        
                        🔧 执行过程:\s
                        [详细的工具调用步骤，包括：]
                        - 调用的工具名称
                        - 使用的参数（特别是完整的queryBody）
                        - 每一步的执行结果
                        
                        📊 执行结果:\s
                        [工具调用获得的具体数据和信息]
                        
                        ✅ 质量检查:\s
                        [对执行结果的验证，包括：]
                        - 数据完整性检查
                        - 结果准确性验证
                        - 是否需要进一步优化
                        ```
                        
                        现在开始智能执行，严格按照分析师的策略，使用MCP工具获取实际数据。记住每一步都要详细记录执行过程和结果。
                        """)
//                .tools(
//                        FunctionTool.create(MultiToolAgent.class, "getCurrentTime"),
//                        FunctionTool.create(MultiToolAgent.class, "getWeather"))
                .tools(mcp_elk())
                .build();
    }

    public static Map<String, String> getCurrentTime(
            @Schema(name = "city",
                    description = "The name of the city for which to retrieve the current time")
            String city) {
        String normalizedCity =
                Normalizer.normalize(city, Normalizer.Form.NFD)
                        .trim()
                        .toLowerCase()
                        .replaceAll("(\\p{IsM}+|\\p{IsP}+)", "")
                        .replaceAll("\\s+", "_");

        return ZoneId.getAvailableZoneIds().stream()
                .filter(zid -> zid.toLowerCase().endsWith("/" + normalizedCity))
                .findFirst()
                .map(
                        zid ->
                                Map.of(
                                        "status",
                                        "success",
                                        "report",
                                        "The current time in "
                                                + city
                                                + " is "
                                                + ZonedDateTime.now(ZoneId.of(zid))
                                                .format(DateTimeFormatter.ofPattern("HH:mm"))
                                                + "."))
                .orElse(
                        Map.of(
                                "status",
                                "error",
                                "report",
                                "Sorry, I don't have timezone information for " + city + "."));
    }

    public static Map<String, String> getWeather(
            @Schema(name = "city",
                    description = "The name of the city for which to retrieve the weather report")
            String city) {
        if (city.equalsIgnoreCase("new york")) {
            return Map.of(
                    "status",
                    "success",
                    "report",
                    "The weather in New York is sunny with a temperature of 25 degrees Celsius (77 degrees"
                            + " Fahrenheit).");

        } else {
            return Map.of(
                    "status", "error", "report", "Weather information for " + city + " is not available.");
        }
    }

    /**
     * - 需要配置后，才能在单测控制台输入内容
     * IntelliJ IDEA Help -> Edit Custom VM Options -> -Deditable.java.test.console=true
     * <br/>
     * - <a href="https://ai.google.dev/api">ai.google.dev/api</a>
     */
    @Test
    public void test_agent() {
        InMemoryRunner runner = new InMemoryRunner(ROOT_AGENT);

        Session session =
                runner
                        .sessionService()
                        .createSession(NAME, USER_ID)
                        .blockingGet();

        try (Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8)) {
            while (true) {
                System.out.print("\nYou > ");
                String userInput = scanner.nextLine();

                if ("quit".equalsIgnoreCase(userInput)) {
                    break;
                }

                Content userMsg = Content.fromParts(Part.fromText(userInput));
                Flowable<Event> events = runner.runAsync(USER_ID, session.id(), userMsg);

                System.out.print("\nAgent > ");
                events.blockingForEach(event -> System.out.println(event.stringifyContent()));
            }
        }
    }

}
