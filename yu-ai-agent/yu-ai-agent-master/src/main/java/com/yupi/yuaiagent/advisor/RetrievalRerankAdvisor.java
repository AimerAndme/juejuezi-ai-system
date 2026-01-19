package com.yupi.yuaiagent.advisor;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//


import com.alibaba.cloud.ai.document.DocumentWithScore;
import com.alibaba.cloud.ai.model.RerankModel;
import com.alibaba.cloud.ai.model.RerankRequest;
import com.alibaba.cloud.ai.model.RerankResponse;
import com.yupi.yuaiagent.service.HybridSearchService;
import com.yupi.yuaiagent.service.VectorizationService;
import com.yupi.yuaiagent.utils.ExecutionTimeUtils;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionTextParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
public class RetrievalRerankAdvisor implements BaseAdvisor {
    public static final String RETRIEVED_DOCUMENTS = "qa_retrieved_documents";
    public static final String FILTER_EXPRESSION = "qa_filter_expression";
    private static final Logger logger = LoggerFactory.getLogger(RetrievalRerankAdvisor.class);
    private static final PromptTemplate DEFAULT_PROMPT_TEMPLATE = new PromptTemplate("{query}\n\nContext information is below, surrounded by ---------------------\n---------------------\n{question_answer_context}\n---------------------\nGiven the context and provided history information and not prior knowledge,\nreply to the user comment. If the answer is not in the context, inform\nthe user that you can't answer the question.\n");
    private static final Double DEFAULT_MIN_SCORE = 0.1;
    private static final int DEFAULT_ORDER = 0;
    private final VectorStore vectorStore;
    private final RerankModel rerankModel;
    private final PromptTemplate promptTemplate;
    private final SearchRequest searchRequest;
    private final Double minScore;
    private final int order;
    @Autowired
    private HybridSearchService hybridSearchService;
    @Autowired
    private VectorizationService vectorizationService;

    @Autowired
    public RetrievalRerankAdvisor(VectorStore vectorStore, RerankModel rerankModel) {
        this(vectorStore, rerankModel, SearchRequest.builder().build(), DEFAULT_PROMPT_TEMPLATE, DEFAULT_MIN_SCORE);
    }

    public RetrievalRerankAdvisor(VectorStore vectorStore, RerankModel rerankModel, Double score) {
        this(vectorStore, rerankModel, SearchRequest.builder().build(), DEFAULT_PROMPT_TEMPLATE, score);
    }

    public RetrievalRerankAdvisor(VectorStore vectorStore, RerankModel rerankModel, SearchRequest searchRequest) {
        this(vectorStore, rerankModel, searchRequest, DEFAULT_PROMPT_TEMPLATE, DEFAULT_MIN_SCORE);
    }

    public RetrievalRerankAdvisor(VectorStore vectorStore, RerankModel rerankModel, SearchRequest searchRequest, PromptTemplate promptTemplate, Double minScore) {
        this(vectorStore, rerankModel, searchRequest, promptTemplate, minScore, 0);
    }

    public RetrievalRerankAdvisor(VectorStore vectorStore, RerankModel rerankModel, SearchRequest searchRequest, PromptTemplate promptTemplate, Double minScore, int order) {
        Assert.notNull(vectorStore, "The vectorStore must not be null!");
        Assert.notNull(rerankModel, "The rerankModel must not be null!");
        Assert.notNull(searchRequest, "The searchRequest must not be null!");
        Assert.notNull(promptTemplate, "The userTextAdvise must not be null!");
        this.vectorStore = vectorStore;
        this.rerankModel = rerankModel;
        this.promptTemplate = promptTemplate;
        this.searchRequest = searchRequest;
        this.minScore = minScore;
        this.order = order;
    }

    public int getOrder() {
        return this.order;
    }

    protected Filter.Expression doGetFilterExpression(Map<String, Object> context) {
        return context.containsKey("qa_filter_expression") && StringUtils.hasText(context.get("qa_filter_expression").toString()) ? (new FilterExpressionTextParser()).parse(context.get("qa_filter_expression").toString()) : this.searchRequest.getFilterExpression();
    }

    protected List<Document> doRerank(ChatClientRequest request, List<Document> documents) {
        return ExecutionTimeUtils.monitorExecutionTime("rerank", () -> {
            if (CollectionUtils.isEmpty(documents)) {
                return documents;
            } else {
                RerankRequest rerankRequest = new RerankRequest(request.prompt().getUserMessage().getText(), documents);
                RerankResponse response = this.rerankModel.call(rerankRequest);
                logger.debug("reranked documents: {}", response);
                return response != null && response.getResults() != null ? (List) response.getResults().stream().filter((doc) -> {
                    return doc != null && doc.getScore() >= this.minScore;
                }).sorted(Comparator.comparingDouble(DocumentWithScore::getScore).reversed()).map(DocumentWithScore::getOutput).collect(Collectors.toList()) : documents;
            }
        });
    }

    public ChatClientRequest before(ChatClientRequest request, AdvisorChain advisorChain) {
        Map<String, Object> context = request.context();
        UserMessage userMessage = request.prompt().getUserMessage();
        SearchRequest searchRequestToUse = SearchRequest.from(this.searchRequest).query(userMessage.getText()).filterExpression(this.doGetFilterExpression(context)).build();
        //向量库检索
        //List<Document> documents = this.vectorStore.similaritySearch(searchRequestToUse);
        //List<Document> documents = hybridSearchService.optimizedSearch(userMessage.getText(), 5, 1, 0.3);
        List<Document> documents = hybridSearchService.searchWithCache(userMessage.getText(), 5, 1, 0.3);
        log.debug("retrieved documents");
        context.put("qa_retrieved_documents", documents);
        //TODO(可优化点)放置检索信息到上下文

        // RagRequestContextData ragRequestContextData = RagRequestContext.get();
        // ragRequestContextData.setRetrievedDocuments(documents.stream().map(Document::getText).toList());
        //重新排序
        documents = this.doRerank(request, documents);
        String documentContext = (String) documents.stream().map(Document::getText).collect(Collectors.joining(System.lineSeparator()));
        String augmentedUserText = this.promptTemplate.render(Map.of("query", userMessage.getText(), "question_answer_context", documentContext));
        log.info("RetrievalRerankAdvisor before: {}", LocalDateTime.now());
        return request.mutate().prompt(request.prompt().augmentUserMessage(augmentedUserText)).context(context).build();
    }

    public ChatClientResponse after(ChatClientResponse chatClientResponse, AdvisorChain advisorChain) {
        log.info("RetrievalRerankAdvisor after: {}", LocalDateTime.now());
        ChatResponse.Builder chatResponseBuilder;
        if (chatClientResponse.chatResponse() == null) {
            chatResponseBuilder = ChatResponse.builder();
        } else {
            chatResponseBuilder = ChatResponse.builder().from(chatClientResponse.chatResponse());
        }

        chatResponseBuilder.metadata("qa_retrieved_documents", chatClientResponse.context().get("qa_retrieved_documents"));
        return ChatClientResponse.builder().chatResponse(chatResponseBuilder.build()).context(chatClientResponse.context()).build();
    }
}
