package com.kind.base.elasticsearch;

import java.util.ArrayList;

import org.apache.http.HttpHost;

/**
 * ES相关参数
 *
 */
public class EsConstant {
	public static String ELASTICSEARCH_HOST_LISTS = "elasticsearch.host.lists";
	public static String ELASTICSEARCH_USERNAME = "elasticsearch.username";
	public static String ELASTICSEARCH_PASSWORD = "elasticsearch.password";

	/**
	 * es中nginx索引库
	 */
	public static String ELASTICSEARCH_NGINX_INDEX = "elasticsearch.nginx.index";
	/**
	 * es中nginx索引文档
	 */
	public static String ELASTICSEARCH_NGINX_DOC = "elasticsearch.nginx.doc";
	/**
	 * es中sys_logs索引库
	 */
	public static String ELASTICSEARCH_SYSLOGS_INDEX = "sys_logs";
	/**
	 * es中sys_logs索引文档
	 */
	public static String ELASTICSEARCH_SYSLOGS_DOC = "sys_logs";

	public static String schema = "http"; // 使用的协议
	public static ArrayList<HttpHost> hostList = null;

	public static int connectTimeOut = 1000; // 连接超时时间
	public static int socketTimeOut = 30000; // 连接超时时间
	public static int connectionRequestTimeOut = 3000; // 获取连接的超时时间

	public static int maxConnectNum = 100; // 最大连接数
	public static int maxConnectPerRoute = 100; // 最大路由连接数
}
