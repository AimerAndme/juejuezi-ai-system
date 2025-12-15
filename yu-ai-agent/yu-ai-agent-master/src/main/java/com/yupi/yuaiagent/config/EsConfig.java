package com.yupi.yuaiagent.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import javax.net.ssl.SSLContext;
import java.net.URI;
import java.security.cert.X509Certificate;

@Configuration
public class EsConfig {

    @Value("${spring.elasticsearch.uris}")
    private String uris; // 例如: http://localhost:9200,https://another-host:9201

    @Value("${spring.elasticsearch.username:elastic}") // 注意命名空间一致性
    private String username;

    @Value("${spring.elasticsearch.password:}") // 注意命名空间一致性
    private String password;

    @Bean
    public ElasticsearchClient elasticsearchClient() throws Exception { // 抛出异常或内部处理

        // 解析 URIs
        String[] uriStrings = StringUtils.commaDelimitedListToStringArray(uris); // 支持多个URI
        HttpHost[] hosts = new HttpHost[uriStrings.length];
        for (int i = 0; i < uriStrings.length; i++) {
            URI uri = URI.create(uriStrings[i].trim()); // 使用 URI 类解析
            hosts[i] = new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme());
        }


        // 创建低级客户端构建器
        RestClientBuilder builder = RestClient.builder(hosts); // 传递 HttpHost 数组

        // 设置基本认证和SSL (如果提供了用户名)
        if (StringUtils.hasText(username)) {
            final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(AuthScope.ANY,
                    new UsernamePasswordCredentials(username, password));

            builder.setHttpClientConfigCallback(httpClientBuilder -> {
                httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);

                // --- SSL/TLS 配置 ---
                // 注意：忽略证书验证仅适用于开发环境，生产环境必须正确配置证书！
                try {
                    SSLContext sslContext = SSLContextBuilder
                            .create()
                            .loadTrustMaterial(null, (chain, authType) -> true) // Trust all certs
                            .build();

                    httpClientBuilder.setSSLContext(sslContext);
                    // httpClientBuilder.setSSLHostnameVerifier((hostname, session) -> true); // Accept all hostnames (Alternative to NoopHostnameVerifier)
                    httpClientBuilder.setSSLHostnameVerifier(org.apache.http.conn.ssl.NoopHostnameVerifier.INSTANCE); // Ignore hostname verification

                } catch (Exception e) {
                    // Handle exception appropriately (e.g., log it)
                    // For simplicity, we rethrow as RuntimeException here.
                    throw new RuntimeException("Failed to create SSL context", e);
                }
                // --- End SSL/TLS ---

                return httpClientBuilder;
            });
        }

        RestClient restClient = builder.build();

        // 创建传输层
        ElasticsearchTransport transport = new RestClientTransport(
                restClient, new JacksonJsonpMapper()
        );

        // 返回高级客户端
        return new ElasticsearchClient(transport);
    }
}