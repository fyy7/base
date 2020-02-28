package com.kind.base.nginx;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchPhraseQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.index.query.WildcardQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.stereotype.Service;

import com.kind.base.elasticsearch.EsConfiguration;
import com.kind.base.elasticsearch.EsConstant;
import com.kind.base.elasticsearch.EsUtil;
import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;
import com.kind.framework.utils.SpringUtil;

import cn.hutool.core.util.StrUtil;

/**
 * 日志查询列表
 *
 */
@Service
@Action(requireLogin = true, action = "#nginx_query_list", description = "nginx日志查询列表", powerCode = "nginx_query_list", requireTransaction = false)
public class QueryList extends BaseActionService {
	private com.kind.framework.log.Logger log = com.kind.framework.log.LogService.getLogger(QueryList.class);

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		String startDate = dc.getRequestString("START_DATE");
		String endDate = dc.getRequestString("END_DATE");
		// 状态
		String status = dc.getRequestString("STATUS");
		// 来源
		String src_flag = dc.getRequestString("SRC_FLAG");
		// 操作IP
		String ip = dc.getRequestString("IP");

		long startTime = 0l;
		long endTime = 0l;

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

			String page = dc.getRequestString("page");
			String rows = dc.getRequestString("rows");

			if (StrUtil.isBlank(page)) {
				page = "1";
			}
			if (StrUtil.isBlank(rows)) {
				rows = "10";
			}

			sourceBuilder.from((Integer.parseInt(page) - 1) * Integer.parseInt(rows));
			sourceBuilder.size(Integer.parseInt(rows));

			// 先分组，再按值进行排序

			BoolQueryBuilder boolBuilder = QueryBuilders.boolQuery();

			if (StrUtil.isNotBlank(ip)) {
				// // wildcardQuery()模糊查询,模糊查询，?匹配单个字符，*匹配多个字符
				WildcardQueryBuilder queryBuilder1 = QueryBuilders.wildcardQuery("remote_addr", "*" + ip + "*");
				boolBuilder.must(queryBuilder1);
			}

			if (StrUtil.isNotBlank(src_flag)) {
				MatchPhraseQueryBuilder queryBuilder1 = QueryBuilders.matchPhraseQuery("SRC_FLAG", src_flag);
				boolBuilder.must(queryBuilder1);
			}

			if (StrUtil.isNotBlank(status)) {
				MatchPhraseQueryBuilder queryBuilder1 = QueryBuilders.matchPhraseQuery("status", status);
				boolBuilder.must(queryBuilder1);
			}
			if (StrUtil.isNotBlank(startDate) || StrUtil.isNotBlank(endDate)) {
				RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery("time_local");
				if (StrUtil.isNotBlank(startDate)) {
					rangeQueryBuilder.gte(startTime);
				}
				if (StrUtil.isNotBlank(endDate)) {
					rangeQueryBuilder.lte(endTime);
				}

				// 时间范围,起始
				boolBuilder.must(rangeQueryBuilder);
			}

			// 排序
			sourceBuilder.sort(new FieldSortBuilder("time_local").order(SortOrder.DESC));

			sourceBuilder.query(boolBuilder);

			SearchRequest searchRequest2 = new SearchRequest(SpringUtil.getEnvProperty(EsConstant.ELASTICSEARCH_NGINX_INDEX));
			searchRequest2.types(SpringUtil.getEnvProperty(EsConstant.ELASTICSEARCH_NGINX_DOC));

			searchRequest2.source(sourceBuilder);

			SearchResponse response = ec.client().search(searchRequest2);
			List<Map<String, Object>> list = EsUtil.handleSearchResponse(response);

			// System.out.println(list);

			dc.setBusiness("rows", list);
			dc.setBusiness("total", response.getHits().getTotalHits());

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
