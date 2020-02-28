package test;

import cn.hutool.core.io.FileUtil;
import cn.hutool.http.Header;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;

public class PostBinaryTest {

	public static void main(String[] args) {
		String requestUrl = "http://127.0.0.1:10000/jinbao2-dataexchange/restful?action=test";
		
		HttpResponse response = HttpRequest
				.post(requestUrl)
				.header(Header.CONTENT_TYPE, "application/octet-stream")
				.body(FileUtil.readBytes("C:\\Users\\yanhang0610\\Desktop\\SimpleCodeGenerator.rar"))
				.execute();
		
		System.out.println(response.body());
	}

}
