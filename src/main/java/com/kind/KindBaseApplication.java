package com.kind;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;

/**
 * 主程序入口，注意事项：该类不能 extends SpringBootServletInitializer，否则打war后在tomcat中会启动两次，这个与微服务的环境有点不一样。
 * 
 *
 * 2018年2月8日
 */
@SpringBootApplication(exclude = { DataSourceAutoConfiguration.class })
public class KindBaseApplication {
	public static void main(String[] args) {
		SpringApplication.run(KindApplication.class, args);
	}
}
