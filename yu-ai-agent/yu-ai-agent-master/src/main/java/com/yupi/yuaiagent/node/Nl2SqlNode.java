package com.yupi.yuaiagent.node;

import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class Nl2SqlNode implements NodeAction {
    private final ChatClient nl2SqlChatClient;

    public Nl2SqlNode(ChatClient nl2SqlChatClient) {
        this.nl2SqlChatClient = nl2SqlChatClient;
    }

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        log.info(" Nl2SqlNode 开始执行");
        Optional<Object> query = state.value("reWriteQuery");
        if (query.isEmpty()) {
            log.error("Nl2SqlNode：无法获取用户输入重写内容");
            return Map.of();
        }
        String reWriteQuery = (String) query.get();
        PromptTemplate promptTemplate = new PromptTemplate("""
                你是一名矿山地质与测量领域的 AI 助手，能将用户的自然语言问题精准转换为 SQL 查询语句。
                请严格遵守以下规则：
                ### 核心原则
                1. **只使用下方提供的表和字段**，严禁虚构不存在的表或列。
                2. **仅生成 SELECT 语句**，禁止 INSERT/UPDATE/DELETE/DROP 等操作。
                3. **所有坐标单位为米（m），储量单位为吨（t），角度为度（°）**。
                4. **日期格式统一为 'YYYY-MM-DD'**。
                5. 若问题模糊或存在歧义，请返回 JSON。
                ### 数据库 Schema（仅限以下内容）
                【矿区】mine_area(area_id, area_name)
                【煤层】coal_seam(seam_id, seam_name, average_thickness, dip_angle)
                【钻孔】borehole(hole_id, area_id, x, y, z, total_depth, drill_purpose, drill_date)
                【岩性分层】lithology_log(hole_id, from_depth, to_depth, rock_type)
                【见煤记录】seam_intercept(hole_id, seam_id, from_depth, to_depth, thickness)
                【巷道】roadway(roadway_id, name, start_point, end_point, roadway_type, status)
                【巷道点】roadway_point(point_id, x, y, z)
                【工作面】mining_face(face_id, seam_id, start_date, end_date, status)
                【储量块段】reserve_block(block_id, seam_id, block_type, recoverable_reserve, recovery_rate)
                【月度产量】monthly_production(block_id, report_month, mined_tonnage, actual_recovery_rate)
                【三量台账】three_quantities(calc_date, development_reserve, preparation_reserve, mining_reserve)
                【地表观测】subsidence_observation(station_id, obs_date, subsidence)
                ### 输出格式
                - 若可生成 SQL：直接输出纯 SQL 语句，不要解释。
                - 若需澄清：输出标准 JSON。
                ### 示例
                用户：3#煤层当前可采储量是多少？
                SQL：SELECT SUM(recoverable_reserve) FROM reserve_block WHERE seam_id = '3#' AND block_type = '采区';
                用户：上个月采了多少煤？
                JSON："clarification_needed": true, "message": "请指定是哪个工作面或采区？例如“1301工作面”或“全矿”。"
                用户输入内容：{query}
                """
        );
        promptTemplate.add("query", reWriteQuery);
        String content = nl2SqlChatClient
                .prompt(promptTemplate.render())
                .call()
                .content();
        log.info("Nl2SqlNode 结束执行，结果为：{}", content);
        if (content == null) {
            log.error("Nl2SqlNode：无法获取结果");
            return Map.of();
        }
        return Map.of("nl2SqlResult", content);
    }
}
