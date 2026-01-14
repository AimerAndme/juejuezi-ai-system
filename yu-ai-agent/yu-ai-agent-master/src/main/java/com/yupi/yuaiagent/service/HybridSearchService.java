package com.yupi.yuaiagent.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.Operator;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.yupi.yuaiagent.client.EmbeddingClient;
import com.yupi.yuaiagent.domin.entity.EsDocument;
import com.yupi.yuaiagent.domin.entity.FileUpload;
import com.yupi.yuaiagent.domin.entity.SearchResult;
import com.yupi.yuaiagent.mapper.FileUploadMapper;
import com.yupi.yuaiagent.model.CacheStatistics;
import com.yupi.yuaiagent.model.CachedSearchResult;
import com.yupi.yuaiagent.utils.QueryNormalizer;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 混合搜索服务，结合文本匹配和向量相似度搜索 支持权限过滤，确保用户只能搜索其有权限访问的文档
 */

@Service
@Slf4j
public class HybridSearchService {

    private static final String DOC_VERSION_PREFIX = "doc:version:";
    private static final String SEARCH_CACHE_PREFIX = "search:";
    private static final long CACHE_TTL_SECONDS = 30 * 60;

    private final FileUploadMapper fileUploadMapper;
    private final VectorizationService vectorizationService;
    private final CacheStatistics cacheStatistics = new CacheStatistics();
    @Autowired
    private ElasticsearchClient esClient;
    @Autowired
    private EmbeddingClient embeddingClient;
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    public HybridSearchService(FileUploadMapper fileUploadMapper, VectorizationService vectorizationService) {
        this.fileUploadMapper = fileUploadMapper;

        this.vectorizationService = vectorizationService;
    }

    /**
     * 使用文本匹配和向量相似度进行混合搜索，支持权限过滤 该方法确保用户只能搜索其有权限访问的文档（自己的文档、公开文档、所属组织的文档）
     *
     * @param query 查询字符串
     * @param userId 用户ID
     * @param topK 返回结果数量
     * @return 搜索结果列表
     */
//    public List<SearchResult> searchWithPermission(String query, String userId, int topK) {
//        log.debug("开始带权限搜索，查询: {}, 用户ID: {}", query, userId);
//
//        try {
//            // 获取用户有效的组织标签（包含层级关系）
//            List<String> userEffectiveTags = getUserEffectiveOrgTags(userId);
//            log.debug("用户 {} 的有效组织标签: {}", userId, userEffectiveTags);
//
//            // 获取用户的数据库ID用于权限过滤
//            String userDbId = getUserDbId(userId);
//            log.debug("用户 {} 的数据库ID: {}", userId, userDbId);
//
//            // 生成查询向量
//            final List<Float> queryVector = embedToVectorList(query);
//
//            // 如果向量生成失败，仅使用文本匹配
//            if (queryVector == null) {
//                log.warn("向量生成失败，仅使用文本匹配进行搜索");
//                return textOnlySearchWithPermission(query, userDbId, userEffectiveTags, topK);
//            }
//
//            log.debug("向量生成成功，开始执行混合搜索 KNN");
//
//            SearchResponse<EsDocument> response = esClient.search(s -> {
//                s.index("knowledge_base");
//                // KNN 召回
//                int recallK = topK * 30; // KNN 召回窗口
//                s.knn(kn -> kn
//                        .field("vector")
//                        .queryVector(queryVector)
//                        .k(recallK)
//                        .numCandidates(recallK)
//                );
//                // 必须命中关键词 + 权限过滤
//                s.query(q -> q.bool(b -> b
//                        .must(mst -> mst.match(m -> m.field("textContent").query(query)))
//                        .filter(f -> f.bool(bf -> bf
//                                // 条件1: 用户可访问自己的文档
//                                .should(s1 -> s1.term(t -> t.field("userId").value(userDbId)))
//                                // 条件2: 公开文档
//                                .should(s2 -> s2.term(t -> t.field("public").value(true)))
//                                // 条件3: 组织标签
//                                .should(s3 -> {
//                                    if (userEffectiveTags.isEmpty()) {
//                                        return s3.matchNone(mn -> mn);
//                                    } else if (userEffectiveTags.size() == 1) {
//                                        return s3.term(t -> t.field("orgTag").value(userEffectiveTags.get(0)));
//                                    } else {
//                                        return s3.bool(inner -> {
//                                            userEffectiveTags.forEach(tag -> inner.should(sh2 -> sh2.term(t -> t.field("orgTag").value(tag))));
//                                            return inner;
//                                        });
//                                    }
//                                })
//                        ))
//                ));
//
//                // 第二阶段 BM25 rescore
//                s.rescore(r -> r
//                        .windowSize(recallK)
//                        .query(rq -> rq
//                                .queryWeight(0.2d)               // 保留部分 KNN 分
//                                .rescoreQueryWeight(1.0d)        // BM25 主导
//                                .query(rqq -> rqq.match(m -> m
//                                        .field("textContent")
//                                        .query(query)
//                                        .operator(Operator.And)
//                                ))
//                        )
//                );
//                s.size(topK);
//                return s;
//            }, EsDocument.class);
//
//            log.debug("Elasticsearch查询执行完成，命中数量: {}, 最大分数: {}",
//                    response.hits().total().value(), response.hits().maxScore());
//
//            List<SearchResult> results = response.hits().hits().stream()
//                    .map(hit -> {
//                        assert hit.source() != null;
//                        log.debug("搜索结果 - 文件: {}, 块: {}, 分数: {}, 内容: {}",
//                                hit.source().getFileMd5(), hit.source().getChunkId(), hit.score(),
//                                hit.source().getTextContent().substring(0, Math.min(50, hit.source().getTextContent().length())));
//                        return new SearchResult(
//                                hit.source().getFileMd5(),
//                                hit.source().getChunkId(),
//                                hit.source().getTextContent(),
//                                hit.score(),
//                                hit.source().getUserId(),
//                                hit.source().getOrgTag(),
//                                hit.source().isPublic()
//                        );
//                    })
//                    .toList();
//
//            log.debug("返回搜索结果数量: {}", results.size());
//            attachFileNames(results);
//            return results;
//        } catch (Exception e) {
//            log.error("带权限的搜索失败", e);
//            // 发生异常时尝试使用纯文本搜索作为后备方案
//            try {
//                log.info("尝试使用纯文本搜索作为后备方案");
//                return textOnlySearchWithPermission(query, getUserDbId(userId), getUserEffectiveOrgTags(userId), topK);
//            } catch (Exception fallbackError) {
//                log.error("后备搜索也失败", fallbackError);
//                return Collections.emptyList();
//            }
//        }
//    }

    /**
     * 仅使用文本匹配的带权限搜索方法
     */
//    private List<SearchResult> textOnlySearchWithPermission(String query, String userDbId, List<String> userEffectiveTags, int topK) {
//        try {
//            log.debug("开始执行纯文本搜索，用户数据库ID: {}, 标签: {}", userDbId, userEffectiveTags);
//
//            SearchResponse<EsDocument> response = esClient.search(s -> s
//                            .index("knowledge_base")
//                            .query(q -> q
//                                    .bool(b -> b
//                                            // 匹配内容相关性
//                                            .must(m -> m
//                                                    .match(ma -> ma
//                                                            .field("textContent")
//                                                            .query(query)
//                                                    )
//                                            )
//                                            // 权限过滤
//                                            .filter(f -> f
//                                                    .bool(bf -> bf
//                                                            // 条件1: 用户可以访问自己的文档
//                                                            .should(s1 -> s1
//                                                                    .term(t -> t
//                                                                            .field("userId")
//                                                                            .value(userDbId)
//                                                                    )
//                                                            )
//                                                            // 条件2: 用户可以访问公开的文档
//                                                            .should(s2 -> s2
//                                                                    .term(t -> t
//                                                                            .field("public")
//                                                                            .value(true)
//                                                                    )
//                                                            )
//                                                            // 条件3: 用户可以访问其所属组织的文档（包含层级关系）
//                                                            .should(s3 -> {
//                                                                if (userEffectiveTags.isEmpty()) {
//                                                                    return s3.matchNone(mn -> mn);
//                                                                } else if (userEffectiveTags.size() == 1) {
//                                                                    // 单个标签使用 term 查询
//                                                                    return s3.term(t -> t
//                                                                            .field("orgTag")
//                                                                            .value(userEffectiveTags.get(0))
//                                                                    );
//                                                                } else {
//                                                                    // 多个标签使用 bool should 组合多个 term 查询
//                                                                    return s3.bool(innerBool -> {
//                                                                        userEffectiveTags.forEach(tag ->
//                                                                                innerBool.should(sh -> sh.term(t -> t
//                                                                                        .field("orgTag")
//                                                                                        .value(tag)
//                                                                                ))
//                                                                        );
//                                                                        return innerBool;
//                                                                    });
//                                                                }
//                                                            })
//                                                    )
//                                            )
//                                    )
//                            )
//                            .minScore(0.3d)
//                            .size(topK),
//                    EsDocument.class
//            );
//
//            log.debug("纯文本查询执行完成，命中数量: {}, 最大分数: {}",
//                    response.hits().total().value(), response.hits().maxScore());
//
//            List<SearchResult> results = response.hits().hits().stream()
//                    .map(hit -> {
//                        assert hit.source() != null;
//                        log.debug("纯文本搜索结果 - 文件: {}, 块: {}, 分数: {}, 内容: {}",
//                                hit.source().getFileMd5(), hit.source().getChunkId(), hit.score(),
//                                hit.source().getTextContent().substring(0, Math.min(50, hit.source().getTextContent().length())));
//                        return new SearchResult(
//                                hit.source().getFileMd5(),
//                                hit.source().getChunkId(),
//                                hit.source().getTextContent(),
//                                hit.score(),
//                                hit.source().getUserId(),
//                                hit.source().getOrgTag(),
//                                hit.source().isPublic()
//                        );
//                    })
//                    .toList();
//
//            log.debug("返回纯文本搜索结果数量: {}", results.size());
//            attachFileNames(results);
//            return results;
//        } catch (Exception e) {
//            log.error("纯文本搜索失败", e);
//            return new ArrayList<>();
//        }
//    }
    @NotNull
    private static Map<String, Object> getMetadata(Hit<EsDocument> hit) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("score", hit.score());
        if (hit.source().getId() != null) {
            metadata.put("id", hit.source().getId());
        }
        if (hit.source().getUserId() != null) {
            metadata.put("userId", hit.source().getUserId());
        }
        if (hit.source().getOrgTag() != null) {
            metadata.put("orgTag", hit.source().getOrgTag());
        }
        metadata.put("isPublic", hit.source().isPublic());
        return metadata;
    }

    /**
     * 原始搜索方法，不包含权限过滤，保留向后兼容性
     */
    public List<Document> search(String query, int topK) {
        try {
            log.debug("开始混合检索，查询: {}, topK: {}", query, topK);
            log.warn("使用了没有权限过滤的搜索方法，建议使用 searchWithPermission 方法");

            // 生成查询向量
            final List<Double> queryVector = vectorizationService.embedToVectorList(query);

            // 如果向量生成失败，仅使用文本匹配
            if (queryVector == null) {
                log.warn("向量生成失败，仅使用文本匹配进行搜索");
                return textOnlySearch(query, topK);
            }
            List<Float> floatList = queryVector.stream().map(Double::floatValue).collect(Collectors.toList());
            SearchResponse<EsDocument> response = esClient.search(s -> {
                s.index("knowledge_base");
                int recallK = topK * 30;
                s.knn(kn -> kn
                        .field("vector")
                        .queryVector(floatList)
                        .k(recallK)
                        .numCandidates(recallK)
                );

                // 过滤仅保留包含关键词的文本
                s.query(q -> q.match(m -> m.field("textContent").query(query)));

                // rescore BM25
                s.rescore(r -> r
                        .windowSize(recallK)
                        .query(rq -> rq
                                .queryWeight(0.5d)
                                .rescoreQueryWeight(0.5d)
                                .query(rqq -> rqq.match(m -> m
                                        .field("textContent")
                                        .query(query)
                                        .operator(Operator.And)
                                ))
                        )
                );
                s.size(topK);
                return s;
            }, EsDocument.class);

            return response.hits().hits().stream()
                    .map(hit -> {
                        if (hit.source() == null) {
                            log.warn("命中结果的 source 为空，跳过该记录");
                            return null;  // 或抛出异常
                        }
                        Map<String, Object> metadata = getMetadata(hit);
                        return Document.builder()
                                .text(hit.source().getTextContent())
                                .metadata(metadata)
                                .build();
//                        return new SearchResult(
//                                hit.source().getFileMd5(),
//                                hit.source().getChunkId(),
//                                hit.source().getTextContent(),
//                                hit.score()
//                        );
                    })
                    .toList();
        } catch (Exception e) {
            log.error("搜索失败", e);
            // 发生异常时尝试使用纯文本搜索作为后备方案
            try {
                log.info("尝试使用纯文本搜索作为后备方案");
                return textOnlySearch(query, topK);
            } catch (Exception fallbackError) {
                log.error("后备搜索也失败", fallbackError);
                throw new RuntimeException("搜索完全失败", fallbackError);
            }
        }
    }

    /**
     * 仅使用文本匹配的搜索方法
     */
    private List<Document> textOnlySearch(String query, int topK) throws Exception {
        SearchResponse<EsDocument> response = esClient.search(s -> s
                        .index("knowledge_base")
                        .query(q -> q
                                .match(m -> m
                                        .field("textContent")
                                        .query(query)
                                )
                        )
                        .size(topK),
                EsDocument.class
        );

        return response.hits().hits().stream()
                .map(hit -> {
                    assert hit.source() != null;
                    Map<String, Object> metadata = getMetadata(hit);
                    return Document.builder()
                            .text(hit.source().getTextContent())
                            .metadata(metadata)
                            .build();
//                    return new SearchResult(
//                            hit.source().getFileMd5(),
//                            hit.source().getChunkId(),
//                            hit.source().getTextContent(),
//                            hit.score()
//                    );
                })
                .toList();
    }

    /**
     * 优化版混合搜索，支持多种策略提高检索质量
     *
     * @param query    查询字符串
     * @param topK     返回结果数量
     * @param strategy 检索策略：0-平衡策略，1-文本优先，2-向量优先，3-严格匹配
     * @param minScore 最小相关性分数阈值，低于此分数的结果将被过滤
     * @return 搜索结果列表
     */
    public List<Document> optimizedSearch(String query, int topK, int strategy, double minScore) {
        try {
            log.debug("优化版混合检索，查询: {}, topK: {}, 策略: {}, 最小分数: {}", query, topK, strategy, minScore);

            final List<Double> queryVector = vectorizationService.embedToVectorList(query);

            if (queryVector == null) {
                log.warn("向量生成失败，仅使用文本匹配进行搜索");
                return textOnlySearch(query, topK);
            }

            List<Float> floatList = queryVector.stream().map(Double::floatValue).collect(Collectors.toList());

            SearchResponse<EsDocument> response = esClient.search(s -> {
                s.index("knowledge_base");

                int recallK = topK * 30;

                s.knn(kn -> kn
                        .field("vector")
                        .queryVector(floatList)
                        .k(recallK)
                        .numCandidates(recallK)
                );

                Operator operator = strategy == 3 ? Operator.And : Operator.Or;

                s.query(q -> q.match(m -> m
                        .field("textContent")
                        .query(query)
                        .operator(operator)
                ));

                double queryWeight = 0.5d;
                double rescoreQueryWeight = 0.5d;

                switch (strategy) {
                    case 1:
                        queryWeight = 0.3d;
                        rescoreQueryWeight = 0.7d;
                        break;
                    case 2:
                        queryWeight = 0.7d;
                        rescoreQueryWeight = 0.3d;
                        break;
                    case 3:
                        queryWeight = 0.2d;
                        rescoreQueryWeight = 0.8d;
                        break;
                    default:
                }

                double finalQueryWeight = queryWeight;
                double finalRescoreQueryWeight = rescoreQueryWeight;
                s.rescore(r -> r
                        .windowSize(recallK)
                        .query(rq -> rq
                                .queryWeight(finalQueryWeight)
                                .rescoreQueryWeight(finalRescoreQueryWeight)
                                .query(rqq -> rqq.match(m -> m
                                        .field("textContent")
                                        .query(query)
                                        .operator(operator)
                                ))
                        )
                );

                s.size(topK);
                return s;
            }, EsDocument.class);

            List<Document> results = response.hits().hits().stream()
                    .filter(hit -> hit.score() >= minScore)
                    .map(hit -> {
                        if (hit.source() == null) {
                            log.warn("命中结果的 source 为空，跳过该记录");
                            return null;
                        }
                        Map<String, Object> metadata = getMetadata(hit);
                        return Document.builder()
                                .text(hit.source().getTextContent())
                                .metadata(metadata)
                                .build();
                    })
                    .filter(Objects::nonNull)
                    .toList();

            log.debug("检索完成，返回 {} 个结果（过滤后）", results.size());
            return results;

        } catch (Exception e) {
            log.error("优化版搜索失败", e);
            try {
                log.info("尝试使用纯文本搜索作为后备方案");
                return textOnlySearch(query, topK);
            } catch (Exception fallbackError) {
                log.error("后备搜索也失败", fallbackError);
                throw new RuntimeException("搜索完全失败", fallbackError);
            }
        }
    }

    //    /**
//     * 生成查询向量，返回 List<Float>，失败时返回 null
//     */
//    private List<Float> embedToVectorList(String text) {
//        try {
//            List<float[]> vecs = embeddingClient.embed(List.of(text));
//            if (vecs == null || vecs.isEmpty()) {
//                log.warn("生成的向量为空");
//                return null;
//            }
//            float[] raw = vecs.get(0);
//            List<Float> list = new ArrayList<>(raw.length);
//            for (float v : raw) {
//                list.add(v);
//            }
//            return list;
//        } catch (Exception e) {
//            log.error("生成向量失败", e);
//            return null;
//        }
//    }
//    /**
//     * 获取用户的有效组织标签（包含层级关系）
//     */
//    private List<String> getUserEffectiveOrgTags(String userId) {
//        log.debug("获取用户有效组织标签，用户ID: {}", userId);
//        try {
//            // 获取用户名
//            User user;
//            try {
//                Long userIdLong = Long.parseLong(userId);
//                log.debug("解析用户ID为Long: {}", userIdLong);
//                user = userRepository.findById(userIdLong)
//                    .orElseThrow(() -> new CustomException("User not found with ID: " + userId, HttpStatus.NOT_FOUND));
//                log.debug("通过ID找到用户: {}", user.getUsername());
//            } catch (NumberFormatException e) {
//                // 如果userId不是数字格式，则假设它就是username
//                log.debug("用户ID不是数字格式，作为用户名查找: {}", userId);
//                user = userRepository.findByUsername(userId)
//                    .orElseThrow(() -> new CustomException("User not found: " + userId, HttpStatus.NOT_FOUND));
//                log.debug("通过用户名找到用户: {}", user.getUsername());
//            }
//
//            // 通过orgTagCacheService获取用户的有效标签集合
//            List<String> effectiveTags = orgTagCacheService.getUserEffectiveOrgTags(user.getUsername());
//            log.debug("用户 {} 的有效组织标签: {}", user.getUsername(), effectiveTags);
//            return effectiveTags;
//        } catch (Exception e) {
//            log.error("获取用户有效组织标签失败: {}", e.getMessage(), e);
//            return Collections.emptyList(); // 返回空列表作为默认值
//        }
//    }
//
//    /**
//     * 获取用户的数据库ID用于权限过滤
//     */
//    private String getUserDbId(String userId) {
//        log.debug("获取用户数据库ID，用户ID: {}", userId);
//        try {
//            // 获取用户名
//            User user;
//            try {
//                Long userIdLong = Long.parseLong(userId);
//                log.debug("解析用户ID为Long: {}", userIdLong);
//                user = userRepository.findById(userIdLong)
//                    .orElseThrow(() -> new CustomException("User not found with ID: " + userId, HttpStatus.NOT_FOUND));
//                log.debug("通过ID找到用户: {}", user.getUsername());
//                return userIdLong.toString(); // 如果输入已经是数字ID，直接返回
//            } catch (NumberFormatException e) {
//                // 如果userId不是数字格式，则假设它就是username
//                log.debug("用户ID不是数字格式，作为用户名查找: {}", userId);
//                user = userRepository.findByUsername(userId)
//                    .orElseThrow(() -> new CustomException("User not found: " + userId, HttpStatus.NOT_FOUND));
//                log.debug("通过用户名找到用户: {}, ID: {}", user.getUsername(), user.getId());
//                return user.getId().toString(); // 返回用户的数据库ID
//            }
//        } catch (Exception e) {
//            log.error("获取用户数据库ID失败: {}", e.getMessage(), e);
//            throw new RuntimeException("获取用户数据库ID失败", e);
//        }
//    }
    private void attachFileNames(List<SearchResult> results) {
        if (results == null || results.isEmpty()) {
            return;
        }
        try {
            Set<String> md5Set = results.stream()
                    .map(SearchResult::getFileMd5)
                    .collect(Collectors.toSet());
            List<FileUpload> uploads = fileUploadMapper.selectByFileMd5List(new ArrayList<>(md5Set));
            Map<String, String> md5ToName = uploads.stream()
                    .collect(Collectors.toMap(FileUpload::getFileMd5, FileUpload::getFileName));
            results.forEach(r -> r.setFileName(md5ToName.get(r.getFileMd5())));
        } catch (Exception e) {
            log.error("补充文件名失败", e);
        }
    }

    public List<Document> searchWithCache(String query, int topK) {
        return searchWithCache(query, topK, 0, 0.0);
    }

    public List<Document> searchWithCache(String query, int topK, int strategy, double minScore) {
        cacheStatistics.incrementRequests();

        String normalizedQuery = QueryNormalizer.normalizeForCacheKey(query);
        String cacheKey = SEARCH_CACHE_PREFIX + normalizedQuery + ":" + topK + ":" + strategy + ":" + minScore;

        log.debug("搜索查询（带缓存）: {}, 标准化后: {}, 缓存key: {}", query, normalizedQuery, cacheKey);

        if (redisTemplate == null) {
            log.warn("Redis未配置，跳过缓存，直接执行搜索");
            return optimizedSearch(query, topK, strategy, minScore);
        }

        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                CachedSearchResult cachedResult = (CachedSearchResult) cached;

                if (isCacheValid(cachedResult)) {
                    cacheStatistics.incrementHits();
                    log.debug("缓存命中: {}, 返回 {} 个结果", query, cachedResult.getDocumentsAsList().size());
                    return cachedResult.getDocumentsAsList();
                } else {
                    log.debug("缓存已失效（文档版本变化）: {}", query);
                    redisTemplate.delete(cacheKey);
                    cacheStatistics.incrementInvalidations();
                }
            }

            cacheStatistics.incrementMisses();
            log.debug("未命中缓存，执行检索: {}", query);
            List<Document> results = optimizedSearch(query, topK, strategy, minScore);

            CachedSearchResult cacheResult = new CachedSearchResult(
                    results,
                    getDocVersions(results),
                    query,
                    topK,
                    strategy,
                    minScore
            );

            redisTemplate.opsForValue().set(cacheKey, cacheResult, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
            log.debug("缓存已保存: {}, TTL: {}秒", cacheKey, CACHE_TTL_SECONDS);

            return results;

        } catch (Exception e) {
            log.error("缓存操作失败，直接执行搜索: {}", e.getMessage(), e);
            return optimizedSearch(query, topK, strategy, minScore);
        }
    }

    private boolean isCacheValid(CachedSearchResult cachedResult) {
        Map<String, String> cachedVersions = cachedResult.getDocVersions();

        if (cachedVersions == null || cachedVersions.isEmpty()) {
            return true;
        }

        for (Map.Entry<String, String> entry : cachedVersions.entrySet()) {
            String docId = entry.getKey();
            String cachedVersion = entry.getValue();

            String currentVersion = getDocVersion(docId);

            if (!cachedVersion.equals(currentVersion)) {
                log.debug("文档版本变化: {} -> {} -> {}", docId, cachedVersion, currentVersion);
                return false;
            }
        }

        return true;
    }

    private String getDocVersion(String docId) {
        String versionKey = DOC_VERSION_PREFIX + docId;
        String version = (String) redisTemplate.opsForValue().get(versionKey);
        if (version == null) {
            version = "v1";
            redisTemplate.opsForValue().set(versionKey, version);
        }
        return version;
    }

    private Map<String, String> getDocVersions(List<Document> documents) {
        Map<String, String> versions = new HashMap<>();
        for (Document doc : documents) {
            Object docId = doc.getMetadata().get("id");
            if (docId != null) {
                String version = getDocVersion(docId.toString());
                versions.put(docId.toString(), version);
            }
        }
        return versions;
    }

    public void invalidateDocumentCache(String docId) {
        if (redisTemplate == null) {
            log.warn("Redis未配置，无法使缓存失效");
            return;
        }

        try {
            String versionKey = DOC_VERSION_PREFIX + docId;
            String currentVersion = getDocVersion(docId);
            int versionNum = Integer.parseInt(currentVersion.substring(1));
            String newVersion = "v" + (versionNum + 1);
            redisTemplate.opsForValue().set(versionKey, newVersion);

            cacheStatistics.incrementInvalidations();
            log.info("文档缓存已失效: {} -> {}", docId, newVersion);

        } catch (Exception e) {
            log.error("使文档缓存失败: {}", e.getMessage(), e);
        }
    }

    public void invalidateAllDocumentCache() {
        if (redisTemplate == null) {
            log.warn("Redis未配置，无法使缓存失效");
            return;
        }

        try {
            Set<String> keys = redisTemplate.keys(DOC_VERSION_PREFIX + "*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.info("所有文档缓存已失效，共 {} 个", keys.size());
            }
        } catch (Exception e) {
            log.error("使所有文档缓存失败: {}", e.getMessage(), e);
        }
    }

    public CacheStatistics getCacheStatistics() {
        return cacheStatistics;
    }

    public void resetCacheStatistics() {
        cacheStatistics.reset();
        log.info("缓存统计信息已重置");
    }
}
