package com.kind.jinbao2.jinbao2authdemo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

public class TestDemo {
	
	public static void main(String[] args) {
		String jsondata="{\"contend\":[{\"bid\":\"22\",\"carid\":\"0\"},{\"bid\":\"22\",\"carid\":\"0\"}],\"result\":100,\"total\":2}";
		JSONObject obj= JSON.parseObject(jsondata);
		//map对象
		Map<String, Object> data =new HashMap<>();
		//循环转换
		 Iterator it =obj.entrySet().iterator();
		 while (it.hasNext()) {
		       Map.Entry<String, Object> entry = (Entry<String, Object>) it.next();
		       data.put(entry.getKey(), entry.getValue());
		 }
		System.out.println("map对象:"+data.toString());

	}
	
	
}
