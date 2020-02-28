package com.kind.base.elasticsearch;

import java.util.ArrayList;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.config.RequestConfig.Builder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.elasticsearch.client.RestClientBuilder.RequestConfigCallback;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.kind.framework.utils.SpringUtil;

@Configuration
public class EsConfiguration {
	private static com.kind.framework.log.Logger logger = com.kind.framework.log.LogService.getLogger(EsConfiguration.class);

	private static RestHighLevelClient client = null;

	@Bean
	public RestHighLevelClient client() {
		if (client != null) {
			return client;
		}

		if (EsConstant.hostList == null) {
			EsConstant.hostList = new ArrayList<>();
			String[] hostStrs = SpringUtil.getEnvProperty(EsConstant.ELASTICSEARCH_HOST_LISTS).split(",");

			logger.debug("ES连接地址=" + SpringUtil.getEnvProperty(EsConstant.ELASTICSEARCH_HOST_LISTS));

			for (String host : hostStrs) {
				EsConstant.hostList.add(new HttpHost(host.split(":")[0], Integer.parseInt(host.split(":")[1]), EsConstant.schema));
			}
		}

		/** 用户认证对象 */
		final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		/** 设置账号密码 */
		credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(SpringUtil.getEnvProperty(EsConstant.ELASTICSEARCH_USERNAME), SpringUtil.getEnvProperty(EsConstant.ELASTICSEARCH_PASSWORD)));
		/** 创建rest client对象 */

		RestClientBuilder builder = RestClient.builder(EsConstant.hostList.toArray(new HttpHost[0]));

		// 认证信息,异步httpclient连接数配置
		builder.setHttpClientConfigCallback(new HttpClientConfigCallback() {
			@Override
			public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
				httpClientBuilder.setMaxConnTotal(EsConstant.maxConnectNum);
				httpClientBuilder.setMaxConnPerRoute(EsConstant.maxConnectPerRoute);
				return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
			}
		});

		// 异步httpclient连接延时配置
		builder.setRequestConfigCallback(new RequestConfigCallback() {
			@Override
			public Builder customizeRequestConfig(Builder requestConfigBuilder) {
				requestConfigBuilder.setConnectTimeout(EsConstant.connectTimeOut);
				requestConfigBuilder.setSocketTimeout(EsConstant.socketTimeOut);
				requestConfigBuilder.setConnectionRequestTimeout(EsConstant.connectionRequestTimeOut);

				return requestConfigBuilder;
			}
		});

		client = new RestHighLevelClient(builder.build());
		return client;
	}

}