package com.kind.base.core.login;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Random;

import javax.imageio.ImageIO;

import org.springframework.stereotype.Service;

import com.kind.framework.core.Action;
import com.kind.framework.core.bean.DataContext;
import com.kind.framework.service.BaseActionService;

import cn.hutool.core.codec.Base64;

/**
 * 验证码
 * 
 * @author zhengjiayun
 *
 *         2018年2月8日
 */
@Service
@Action(requireLogin = false, action = "#getVerifyCode", description = "验证码", powerCode = "", requireTransaction = false)
public class GetVerifyCodeService extends BaseActionService {

	final int NUM1 = 40;
	final int NUM2 = 10;

	@Override
	public String verifyParameter(DataContext dc) {
		return null;
	}

	@Override
	public int handleData(HashMap<String, String> hmLogSql, DataContext dc) {
		int width = 75, height = 25;
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		Graphics g = image.getGraphics();

		g.setColor(new Color(0xDCDCDC));
		g.fillRect(0, 0, width, height);

		g.setColor(Color.black);
		g.drawRect(0, 0, width - 1, height - 1);

		String rand = genRand(1, 9999);

		g.setColor(Color.black);

		g.setFont(new Font("Atlantic Inline", Font.PLAIN, 18));
		String str = rand.substring(0, 1);
		g.drawString(str, 8, 17);
		str = rand.substring(1, 2);
		g.drawString(str, 20, 15);
		str = rand.substring(2, 3);
		g.drawString(str, 35, 18);
		str = rand.substring(3, 4);
		g.drawString(str, 45, 15);

		Random random = new Random();
		for (int i = 0; i < NUM1; i++) {
			int x = random.nextInt(width);
			int y = random.nextInt(height);
			g.drawOval(x, y, 0, 0);
		}

		for (int i = 0; i < NUM2; i++) {
			int x = random.nextInt(width);
			int y = random.nextInt(height);
			g.drawOval(x, y, 1, 1);
		}
		g.dispose();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();

		try {
			ImageIO.write(image, "JPEG", baos);
		} catch (IOException e) {
			e.printStackTrace();
		}
		byte[] bytes = baos.toByteArray();

		try {
			if (baos != null) {
				baos.close();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

		// 图片转base64编码
		String imgBase64 = Base64.encode(bytes);

		dc.setSession("verifyCode", rand);
		dc.setBusiness("img_base64", imgBase64);

		return setSuccessInfo(dc);
	}

	private String genRand(int min, int max) {
		// 1~9999
		StringBuffer sbRnd = new StringBuffer(String.valueOf(min + (int) (Math.random() * max)));
		StringBuffer sbTmp = new StringBuffer("");

		int i = 0;
		while ((sbRnd.toString().length() + i) < (String.valueOf(max).length())) {
			sbTmp.append("0");
			i++;
		}

		return sbTmp.append(sbRnd).toString();
	}

	@Override
	public String pageAddress(DataContext dc) {
		return null;
	}
}
