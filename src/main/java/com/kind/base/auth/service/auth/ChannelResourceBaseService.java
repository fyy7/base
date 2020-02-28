package com.kind.base.auth.service.auth;

import org.springframework.stereotype.Service;

import com.kind.framework.core.DbHelper;

/**
 * 渠道系统接入的服务基类
 * 
 * @author yanhang
 */
@Service
public class ChannelResourceBaseService extends DbHelper {

	/**
	 * 验证接入系统的合法性
	 * 
	 * @param channelId
	 * @param token
	 * @return
	 */
	public boolean validateChannelSystem(String channelId, String token) {
		return true;
	}

}
