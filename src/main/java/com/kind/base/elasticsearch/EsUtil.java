package com.kind.base.elasticsearch;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.delete.DeleteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestClientBuilder.HttpClientConfigCallback;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.WildcardQueryBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;

import cn.hutool.core.util.StrUtil;

/**
 * 全文检索工具
 */
public class EsUtil {

	private static com.kind.framework.log.Logger logger = com.kind.framework.log.LogService.getLogger(EsUtil.class);

	/**
	 * * 批量插入ES
	 * 
	 * @param indexName
	 *            索引
	 * @param type
	 *            类型
	 * @param idName
	 *            id名称
	 * @param list
	 *            数据集合
	 */
	public static void bulkData(RestHighLevelClient client, String indexName, String type, String idName, List<Map<String, Object>> list) {
		try {

			if (null == list || list.size() <= 0) {
				return;
			}
			if (StrUtil.isBlank(indexName) || StrUtil.isBlank(idName) || StrUtil.isBlank(type)) {
				return;
			}
			BulkRequest request = new BulkRequest();
			for (Map<String, Object> map : list) {
				if (map.get(idName) != null) {
					if ("ADD".equalsIgnoreCase(map.get("INDEX_CURD").toString())) {
						map.remove("INDEX_CURD");
						request.add(new IndexRequest(indexName, type, String.valueOf(map.get(idName))).source(map, XContentType.JSON));
						continue;
					}
					if ("UPDATE".equalsIgnoreCase(map.get("INDEX_CURD").toString())) {
						map.remove("INDEX_CURD");
						request.add(new UpdateRequest(indexName, type, String.valueOf(map.get(idName))).doc(map, XContentType.JSON));
						continue;
					}
					if ("DELETE".equalsIgnoreCase(map.get("INDEX_CURD").toString())) {
						map.remove("INDEX_CURD");
						request.add(new DeleteRequest(indexName, type, String.valueOf(map.get(idName))));
						continue;
					}
				}
			}
			// 2、可选的设置
			/*
			 * request.timeout("2m"); request.setRefreshPolicy("wait_for"); request.waitForActiveShards(2);
			 */
			// 3、发送请求，异步请求
			client.bulkAsync(request, new ActionListener<BulkResponse>() {
				@Override
				public void onResponse(BulkResponse bulkResponse) {
					for (BulkItemResponse bulkItemResponse : bulkResponse) {
						DocWriteResponse itemResponse = bulkItemResponse.getResponse();

						switch (bulkItemResponse.getOpType()) {
						case INDEX:
						case CREATE:
							IndexResponse indexResponse = (IndexResponse) itemResponse;
							// 新增成功的处理
							logger.debug("新增成功：" + indexResponse.toString());
							break;
						case UPDATE:
							UpdateResponse updateResponse = (UpdateResponse) itemResponse;
							if (updateResponse != null) {
								// 修改成功的处理
								logger.debug("修改成功：" + updateResponse.toString());
							} else {
								logger.error(updateResponse);
							}
							break;
						case DELETE:
							DeleteResponse deleteResponse = (DeleteResponse) itemResponse;
							// 删除成功的处理
							logger.debug("删除成功：" + deleteResponse.toString());
							break;
						}
					}
				}

				@Override
				public void onFailure(Exception e) {
					e.printStackTrace();
				}
			});

			// 4、处理响应 同步请求
			// BulkResponse bulkResponse = client.bulk(request);
			// if (bulkResponse != null) {
			// for (BulkItemResponse bulkItemResponse : bulkResponse) {
			// DocWriteResponse itemResponse = bulkItemResponse.getResponse();
			//
			// if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.INDEX || bulkItemResponse.getOpType() == DocWriteRequest.OpType.CREATE) {
			// IndexResponse indexResponse = (IndexResponse) itemResponse;
			// // 新增成功的处理
			// logger.debug("新增成功：" + indexResponse.toString());
			// } else if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.UPDATE) {
			// UpdateResponse updateResponse = (UpdateResponse) itemResponse;
			//
			// if (updateResponse != null) {
			// // 修改成功的处理
			// logger.debug("修改成功：" + updateResponse.toString());
			// }
			// } else if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.DELETE) {
			// DeleteResponse deleteResponse = (DeleteResponse) itemResponse;
			// // 删除成功的处理
			// logger.debug("删除成功：" + deleteResponse.toString());
			// }
			// }
			// }
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	public static List<Map<String, Object>> handleSearchResponse(SearchResponse response) {
		// 获取source
		return Arrays.stream(response.getHits().getHits()).map(b -> {
			return b.getSourceAsMap();
		}).collect(Collectors.toList());
	}

	public static RestHighLevelClient getClient() {
		// http://192.168.0.36:9200/nginx_access_log/_search

		/** 用户认证对象 */
		final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
		/** 设置账号密码 */
		credentialsProvider.setCredentials(AuthScope.ANY, new UsernamePasswordCredentials("elastic", "changeme"));
		/** 创建rest client对象 */
		RestClientBuilder builder = RestClient.builder(new HttpHost("192.168.0.36", 9200)).setHttpClientConfigCallback(new HttpClientConfigCallback() {
			@Override
			public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpClientBuilder) {
				return httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
			}
		});
		RestHighLevelClient client = new RestHighLevelClient(builder.build());
		return client;
	}

	/**
	 * 模糊查询
	 * 
	 * @throws Exception
	 */
	public static void query1() throws Exception {
		RestHighLevelClient client = getClient();
		SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
		sourceBuilder.from(0);
		sourceBuilder.size(1000);

		// wildcardQuery()模糊查询,模糊查询，?匹配单个字符，*匹配多个字符
		// WildcardQueryBuilder queryBuilder1 = QueryBuilders.wildcardQuery("request", "*dataexchange_mq*");
		WildcardQueryBuilder queryBuilder2 = QueryBuilders.wildcardQuery("remote_addr", "*192.168.3.134*");

		// 精确匹配
		MatchQueryBuilder matchBuilder = QueryBuilders.matchQuery("status", "404");

		BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();

		// 时间范围
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		long startTime = sdf.parse("2019-08-12 09:03:00").getTime();
		long endTime = sdf.parse("2019-08-12 18:00:00").getTime();
		// 时间范围,起始
		boolBuilder.must(QueryBuilders.rangeQuery("time_local").gte(startTime).lte(endTime));
		// boolBuilder.must(queryBuilder1);
		boolBuilder.must(queryBuilder2);

		boolBuilder.must(matchBuilder);

		// String[] fields = {"hostIP","pathFile"};
		// FetchSourceContext sourceContext = new FetchSourceContext(fields);
		// sourceBuilder.fetchSource(sourceContext);

		sourceBuilder.query(boolBuilder);

		String[] includes = new String[] {};
		String[] excludes = new String[] { "http_x_forwarded_for" };

		// 包含排除列
		sourceBuilder.fetchSource(includes, excludes);

		// 5秒超时
		sourceBuilder.timeout(new TimeValue(5, TimeUnit.SECONDS));

		// 排序
		sourceBuilder.sort(new FieldSortBuilder("time_local").order(SortOrder.ASC));

		SearchRequest searchRequest = new SearchRequest("nginx_access_log");
		searchRequest.types("access_log");
		searchRequest.source(sourceBuilder);

		SearchResponse response = client.search(searchRequest);

		for (Map<String, Object> data : handleSearchResponse(response)) {
			System.out.println(data);
		}

	}

	/**
	 * 统计查询
	 * 
	 * @throws Exception
	 */
	public static void query2() throws Exception {
		// distinct 统计
		SearchSourceBuilder sourceBuilder2 = new SearchSourceBuilder();
		sourceBuilder2.from(0);
		sourceBuilder2.size(100);

		// ValueCountAggregationBuilder vcb = AggregationBuilders.count("count_remote_addr").field("remote_addr.keyword");
		// sourceBuilder2.aggregation(vcb);

		// 先分组，再按key字段排序
		// TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("group_remote_addr").field("remote_addr.keyword").order(Order.term(true));

		// 先分组，再按值进行排序
		TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("group_remote_addr").field("request.keyword").order(Terms.Order.count(false));

		sourceBuilder2.aggregation(termsAggregationBuilder);

		// wildcardQuery()模糊查询,模糊查询，?匹配单个字符，*匹配多个字符
		WildcardQueryBuilder queryBuilder1 = QueryBuilders.wildcardQuery("status", "*200*");

		BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();
		boolBuilder.must(queryBuilder1);
		sourceBuilder2.query(boolBuilder);

		// 排序
		// sourceBuilder2.sort(new FieldSortBuilder("remote_addr.keyword").order(SortOrder.ASC));

		SearchRequest searchRequest2 = new SearchRequest("nginx_access_log");
		searchRequest2.types("access_log");
		searchRequest2.source(sourceBuilder2);

		SearchResponse response = getClient().search(searchRequest2);

		Aggregation aggregation = response.getAggregations().get("group_remote_addr");

		ParsedStringTerms teamAgg = (ParsedStringTerms) aggregation;
		int start = 0;
		for (Bucket data : teamAgg.getBuckets()) {
			System.out.print(++start);
			System.out.println("、" + data.getKeyAsString() + ":" + data.getDocCount());

		}

	}

	/**
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// 模糊查询
		// query1();

		// 统计查询
		query2();

		System.exit(0);

	}

}