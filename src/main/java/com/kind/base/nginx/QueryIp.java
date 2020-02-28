package com.kind.base.nginx;

import java.text.SimpleDateFormat;
import java.util.HashMap;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.aggregations.Aggregation;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.ParsedStringTerms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.Terms.Bucket;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.kind.base.elasticsearch.EsConfiguration;
import com.kind.base.elasticsearch.EsConstant;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;

import cn.hutool.core.util.StrUtil;

/**
 * 按IP相关分析
 *
 */
@Service
@Action(requireLogin = true, action = "#nginx_query_ip", description = "nginx日志IP相关查询", powerCode = "nginx_query_ip", requireTransaction = false)
public class QueryIp extends BaseActionService {
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(QueryIp.class);

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String startDate = dc.getRequestString("START_DATE");
		String endDate = dc.getRequestString("END_DATE");
		long startTime = 0l;
		long endTime = 0l;

		// 统计类型
		String type = dc.getRequestString("type");
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			if (StrUtil.isNotBlank(startDate)) {
				startTime = sdf.parse(startDate).getTime();
			}
			if (StrUtil.isNotBlank(endDate)) {
				endTime = sdf.parse(endDate).getTime();
			}
			EsConfiguration ec = SpringUtil.getBean(EsConfiguration.class);

			SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

			String fieldKey = null;
			if ("2".equals(type)) {
				fieldKey = "request";
			} else {
				fieldKey = "remote_addr";
			}

			// 先分组，再按值进行排序
			TermsAggregationBuilder termsAggregationBuilder = AggregationBuilders.terms("group_column").field(fieldKey + ".keyword").order(Terms.Order.count(false));

			sourceBuilder.aggregation(termsAggregationBuilder);

			BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();

			// 时间范围
			RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("time_local");
			if (StrUtil.isNotBlank(startDate)) {
				rangeQueryBuilder.gte(startTime);
			}
			if (StrUtil.isNotBlank(endDate)) {
				rangeQueryBuilder.lte(endTime);
			}
			boolBuilder.must(rangeQueryBuilder);

			sourceBuilder.query(boolBuilder);

			sourceBuilder.from(0);
			sourceBuilder.size(50);

			SearchRequest searchRequest2 = new SearchRequest(SpringUtil.getEnvProperty(EsConstant.ELASTICSEARCH_NGINX_INDEX));
			searchRequest2.types(SpringUtil.getEnvProperty(EsConstant.ELASTICSEARCH_NGINX_DOC));

			searchRequest2.source(sourceBuilder);

			SearchResponse response = ec.client().search(searchRequest2);

			dc.setBusiness("totalHits", response.getHits().getTotalHits());

			// 聚合分析
			Aggregation aggregation = response.getAggregations().get("group_column");

			ParsedStringTerms teamAgg = (ParsedStringTerms) aggregation;
			JSONArray arr_data = new JSONArray();
			for (Bucket data : teamAgg.getBuckets()) {
				JSONObject j = new JSONObject();
				j.put("column", data.getKeyAsString());
				j.put("value", data.getDocCount());
				arr_data.add(j);
			}

			dc.setBusiness("data", arr_data);

		} catch (Exception e) {
			log.error(e);
			e.printStackTrace();
			return setFailInfo(dc);
		}

		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		return null;
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}
}
