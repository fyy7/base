package com.kind.base.nginx;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.stereotype.Service;

import com.kind.base.elasticsearch.EsConfiguration;
import com.kind.base.elasticsearch.EsConstant;
import com.kind.base.elasticsearch.EsUtil;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;

/**
 * 日志详情
 *
 */
@Service
@Action(requireLogin = true, action = "#nginx_detail_log", description = "nginx日志详情", powerCode = "nginx_detail_log", requireTransaction = false)
public class DetailLog extends BaseActionService {
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(DetailLog.class);

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String uuid = dc.getRequestString("uuid");

		try {
			SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
			sourceBuilder.from(0);
			sourceBuilder.size(5);

			// 先分组，再按值进行排序

			BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();

			MatchPhraseQueryBuilder queryBuilder1 = QueryBuilders.matchPhraseQuery("uuid", uuid);
			boolBuilder.must(queryBuilder1);

			sourceBuilder.query(boolBuilder);

			SearchRequest searchRequest2 = new SearchRequest(SpringUtil.getEnvProperty(EsConstant.ELASTICSEARCH_NGINX_INDEX));
			searchRequest2.types(SpringUtil.getEnvProperty(EsConstant.ELASTICSEARCH_NGINX_DOC));

			searchRequest2.source(sourceBuilder);

			EsConfiguration ec = SpringUtil.getBean(EsConfiguration.class);
			SearchResponse response = ec.client().search(searchRequest2);
			List<Map<String, Object>> list = EsUtil.handleSearchResponse(response);
			if (list != null && list.size() == 1) {
				dc.setBusiness("map", list.get(0));
			} else {
				dc.setBusiness("map", null);
			}
		} catch (Exception e) {
			log.error(e);
			e.printStackTrace();
			return setFailInfo(dc);
		}

		return setSuccessInfo(dc);
	}

	@Override
	public String pageAddress(DataContext dc) {
		return "sys/nginx/detail_log";
	}

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}
}
