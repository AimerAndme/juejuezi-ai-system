package com.yupi.yuaiagent.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class QueryRewritingNode implements NodeAction {
    private final ChatClient chatClient;

    public QueryRewritingNode(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        if (state.value("query").isEmpty()) {
            log.error("无法获取用户输入内容");
        }
        String query = (String) state.value("query").get();
        PromptTemplate promptTemplate = new PromptTemplate("""
                【任务】对用户输入的Query进行润色改写，为后续意图识别提供清晰、规范的文本。
                【核心规则】
                1. 绝对保留原意：不增删用户核心诉求，不添加未提及的信息（如用户没说“矿山设备”，绝不能凭空补充）。
                2. 补全模糊信息：对口语化、省略式表达，结合上下文逻辑补全（如“那个考勤咋算”→“请问公司的考勤计算规则是什么？”）。
                3. 规范表达：将语序混乱、重复冗余的表述调整为通顺的书面语，同时保留场景专属关键词（如矿山场景的“掘进机”“顶板支护”，数据场景的“产量”“设备运行指标”）。
                4. 场景适配：
                   - 闲聊场景：保留口语化温度，仅修正不通顺表述（如“嘿，今天天气真不错哈”无需过度规范）；
                   - 矿山专业场景：精准保留技术术语，补充省略的专业主体（如“咋修啊”→“请问矿山开采用的掘进机该如何维修？”）；
                   - 公司制度场景：明确“公司”“制度”等关联主体，补全省略的规则指向（如“请假要多久批”→“请问公司的请假审批流程需要多长时间才能完成？”）；
                   - 业务数据场景：突出数据相关要素（指标、时间、对象），如“昨天的量”→“请问昨天矿山现场的生产产量数据是多少？”。
                5. 输出要求：仅返回润色后的完整Query，无需任何解释说明、标签或额外内容。
                
                【场景示例参考】
                1. 原输入（矿山专业）：“矿山那个掘进机，就是老卡壳，咋处理？” → 润色后：“请问矿山开采使用的掘进机频繁卡壳，该如何处理？”
                2. 原输入（公司制度）：“迟到扣钱不？扣多少？” → 润色后：“请问公司对于迟到的处罚规则是什么？迟到会扣多少钱？”
                3. 原输入（业务数据）：“3号设备，上周的运行数据” → 润色后：“请问3号矿山设备上周的运行状态数据是什么？”
                4. 原输入（闲聊）：“哎，你说咱这儿啥时候能降温啊” → 润色后：“你觉得咱们这里什么时候会降温呢？”
                5. 原输入（模糊表述）：“那个…就是制度里说的福利，咋领？” → 润色后：“请问公司制度中规定的员工福利该如何领取？”
                
                【待润色Query】
                {query}
                """);
        promptTemplate.add("query", query);
        log.info("开始查询重写，原始query为：{}", query);
        String content = chatClient.prompt(promptTemplate.render()).call().content();
        if (content != null) {
            log.info("查询重写完成，结果query为：{}", content);
            query = content;
        }
        return Map.of("query", query);
    }
}
