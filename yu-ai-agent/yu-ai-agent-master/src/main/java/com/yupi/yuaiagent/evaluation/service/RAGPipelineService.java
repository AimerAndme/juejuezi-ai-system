package com.yupi.yuaiagent.evaluation.service;

import com.yupi.yuaiagent.evaluation.EvaluationTestCase;
import com.yupi.yuaiagent.service.HybridSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RAGPipelineService {

    @Autowired
    private HybridSearchService hybridSearchService;

    @Autowired
    @Qualifier("dashscopeChatModel")
    private ChatModel chatModel;

    private static final String DEFAULT_RAG_PROMPT = """
        你是一个专业的问答助手。请根据以下检索到的上下文信息回答用户的问题。
        
        上下文信息：
        {context}
        
        用户问题：
        {question}
        
        请基于上下文信息准确回答问题。如果上下文中没有相关信息，请明确说明。
        """;

    public EvaluationTestCase runRAGPipeline(EvaluationTestCase testCase, int topK) {
        log.info("执行RAG流程: question={}, topK={}", testCase.getQuestion(), topK);

        try {
            List<String> retrievedDocIds = new ArrayList<>();
            List<String> retrievedDocContents = new ArrayList<>();

            List<Document> documents = hybridSearchService.search(testCase.getQuestion(), topK);

            for (Document doc : documents) {
                Map<String, Object> metadata = doc.getMetadata();
                String docId = metadata != null ? (String) metadata.get("id") : null;
                if (docId == null) {
                    docId = "doc_" + System.identityHashCode(doc.getText());
                }
                retrievedDocIds.add(docId);
                retrievedDocContents.add(doc.getText());
            }

            testCase.setRetrievedDocuments(retrievedDocIds);
            testCase.setRetrievedDocumentContents(retrievedDocContents);

            if (!retrievedDocContents.isEmpty()) {
                String answer = generateAnswer(testCase.getQuestion(), retrievedDocContents);
                testCase.setRetrievedAnswer(answer);
            } else {
                log.warn("未检索到相关文档: {}", testCase.getQuestion());
                testCase.setRetrievedAnswer("未找到相关信息");
            }

        } catch (Exception e) {
            log.error("RAG流程执行失败: {}", testCase.getQuestion(), e);
            testCase.setRetrievedDocuments(new ArrayList<>());
            testCase.setRetrievedDocumentContents(new ArrayList<>());
            testCase.setRetrievedAnswer("生成失败: " + e.getMessage());
        }

        return testCase;
    }

    public List<EvaluationTestCase> runRAGPipeline(List<EvaluationTestCase> testCases, int topK) {
        log.info("批量执行RAG流程: {} 个测试用例", testCases.size());

        return testCases.stream()
                .map(testCase -> runRAGPipeline(testCase, topK))
                .collect(Collectors.toList());
    }

    private List<String> retrieveDocuments(String query, int topK) {
        log.debug("检索文档: query={}, topK={}", query, topK);

        try {
            return hybridSearchService.search(query, topK)
                    .stream()
                    .map(doc -> {
                        String content = doc.getText();
                        return content;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("文档检索失败: {}", query, e);
            return new ArrayList<>();
        }
    }

    private List<String> retrieveDocumentIds(String query, int topK) {
        log.debug("检索文档ID: query={}, topK={}", query, topK);

        try {
            return hybridSearchService.search(query, topK)
                    .stream()
                    .map(doc -> {
                        Map<String, Object> metadata = doc.getMetadata();
                        String docId = metadata != null ? (String) metadata.get("id") : null;
                        if (docId == null) {
                            docId = "doc_" + System.identityHashCode(doc.getText());
                        }
                        return docId;
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("文档ID检索失败: {}", query, e);
            return new ArrayList<>();
        }
    }

    private String generateAnswer(String query, List<String> retrievedDocs) {
        log.debug("生成答案: query={}, docs={}", query, retrievedDocs.size());

        try {
            String context = String.join("\n\n", retrievedDocs);

            String prompt = DEFAULT_RAG_PROMPT
                    .replace("{context}", context)
                    .replace("{question}", query);

            ChatClient chatClient = ChatClient.builder(chatModel).build();

            String answer = chatClient.prompt()
                    .user(prompt)
                    .call()
                    .content();

            return answer;

        } catch (Exception e) {
            log.error("答案生成失败: {}", query, e);
            throw new RuntimeException("答案生成失败", e);
        }
    }

    public EvaluationTestCase runRetrievalOnly(EvaluationTestCase testCase, int topK) {
        log.info("仅执行检索: question={}, topK={}", testCase.getQuestion(), topK);

        List<String> retrievedDocIds = new ArrayList<>();
        List<String> retrievedDocContents = new ArrayList<>();

        try {
            List<Document> documents = hybridSearchService.search(testCase.getQuestion(), topK);

            for (Document doc : documents) {
                Map<String, Object> metadata = doc.getMetadata();
                String docId = metadata != null ? (String) metadata.get("id") : null;
                if (docId == null) {
                    docId = "doc_" + System.identityHashCode(doc.getText());
                }
                retrievedDocIds.add(docId);
                retrievedDocContents.add(doc.getText());
            }
        } catch (Exception e) {
            log.error("文档检索失败: {}", testCase.getQuestion(), e);
        }

        testCase.setRetrievedDocuments(retrievedDocIds);
        testCase.setRetrievedDocumentContents(retrievedDocContents);

        return testCase;
    }

    public List<EvaluationTestCase> runRetrievalOnly(List<EvaluationTestCase> testCases, int topK) {
        log.info("批量执行检索: {} 个测试用例", testCases.size());

        return testCases.stream()
                .map(testCase -> runRetrievalOnly(testCase, topK))
                .collect(Collectors.toList());
    }
}
