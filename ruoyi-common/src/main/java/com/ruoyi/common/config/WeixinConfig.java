package com.ruoyi.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:config.properties")
public class WeixinConfig {
	
	private static String weixinMiniAppid;
	private static String weixinMiniSecret;
    private static String weixinMiniMchId;
    private static String weixinMiniApiV3Key;
    private static String weixinMiniMerchantSerialNumber;
    private static String weixinMiniNotifyUrl;
    private static String weixinMiniRefundNotifyUrl;
    
    
	@Value("${weixin.mini.appid}")
	public void setWeixinMiniAppid(String weixinMiniAppid) {
		WeixinConfig.weixinMiniAppid = weixinMiniAppid;
	}
	
	@Value("${weixin.mini.secret}")
	public void setWeixinMiniSecret(String weixinMiniSecret) {
		WeixinConfig.weixinMiniSecret = weixinMiniSecret;
	}

	public static String getWeixinMiniSecret() {
		return weixinMiniSecret;
	}

	@Value("${weixin.mini.mch_id}")
	public void setWeixinMiniMchId(String weixinMiniMchId) {
		WeixinConfig.weixinMiniMchId = weixinMiniMchId;
	}

	@Value("${weixin.mini.apiV3Key}")
	public void setWeixinMiniApiV3Key(String weixinMiniApiV3Key) {
		WeixinConfig.weixinMiniApiV3Key = weixinMiniApiV3Key;
	}

	@Value("${weixin.mini.merchantSerialNumber}")
	public void setWeixinMiniMerchantSerialNumber(String weixinMiniMerchantSerialNumber) {
		WeixinConfig.weixinMiniMerchantSerialNumber = weixinMiniMerchantSerialNumber;
	}

	@Value("${weixin.mini.notify_url}")
	public void setWeixinMiniNotifyUrl(String weixinMiniNotifyUrl) {
		WeixinConfig.weixinMiniNotifyUrl = weixinMiniNotifyUrl;
	}

	@Value("${weixin.mini.refund_notify_url}")
	public void setWeixinMiniRefundNotifyUrl(String weixinMiniRefundNotifyUrl) {
		WeixinConfig.weixinMiniRefundNotifyUrl = weixinMiniRefundNotifyUrl;
	}
	
	

	public static String getWeixinMiniAppid() {
		return weixinMiniAppid;
	}

	public static String getWeixinMiniMchId() {
		return weixinMiniMchId;
	}

	public static String getWeixinMiniApiV3Key() {
		return weixinMiniApiV3Key;
	}

	public static String getWeixinMiniMerchantSerialNumber() {
		return weixinMiniMerchantSerialNumber;
	}

	public static String getWeixinMiniNotifyUrl() {
		return weixinMiniNotifyUrl;
	}

	public static String getWeixinMiniRefundNotifyUrl() {
		return weixinMiniRefundNotifyUrl;
	}
	
	


	
	
	
	
}
