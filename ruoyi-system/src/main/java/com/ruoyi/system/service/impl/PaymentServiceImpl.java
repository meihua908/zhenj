package com.ruoyi.system.service.impl;

import com.ruoyi.system.service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.TreeMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

@Service(value = "paymentService")

public class PaymentServiceImpl implements PaymentService {
/*
    private static Logger LOGGER = LoggerFactory.getLogger(PaymentServiceImpl.class);
    @Value("${spring.profiles.active}")
    private String PROJECT_ENV;

    @Value("${hcc.wx.domain}")
    private String orderDomain;

    @Autowired
    private PaymentRecordMapper paymentRecordMapper;
    @Autowired
    private PaymentNotifyMapper paymentNotifyMapper;

    public PaymentServiceImpl() {
    }

    @Override
    public Map<String, String> xcxPayment(String orderNum, double money, String openId) throws Exception {
        LOGGER.info("【小程序支付】 统一下单开始, 订单编号=" + orderNum);
        SortedMap<String, String> resultMap = new TreeMap<String, String>();
        //生成支付金额，开发环境处理支付金额数到0.01、0.02、0.03元
        double payAmount = PayUtil.getPayAmountByEnv(PROJECT_ENV, money);
        //添加或更新支付记录(参数跟进自己业务需求添加)
        int flag = this.addOrUpdatePaymentRecord(orderNum, payAmount,.....);
        if (flag < 0) {
            resultMap.put("returnCode", "FAIL");
            resultMap.put("returnMsg", "此订单已支付！");
            LOGGER.info("【小程序支付】 此订单已支付！");
        } else if (flag == 0) {
            resultMap.put("returnCode", "FAIL");
            resultMap.put("returnMsg", "支付记录生成或更新失败！");
            LOGGER.info("【小程序支付】 支付记录生成或更新失败！");
        } else {
            Map<String, String> resMap = this.xcxUnifieldOrder(orderNum, PayConfig.TRADE_TYPE_JSAPI, payAmount, openId);
            if (PayConstant.SUCCESS.equals(resMap.get("return_code")) && PayConstant.SUCCESS.equals(resMap.get("result_code"))) {
                resultMap.put("appId", PayConfig.XCX_APP_ID);
                resultMap.put("timeStamp", PayUtil.getCurrentTimeStamp());
                resultMap.put("nonceStr", PayUtil.makeUUID(32));
                resultMap.put("package", "prepay_id=" + resMap.get("prepay_id"));
                resultMap.put("signType", "MD5");
                resultMap.put("sign", PayUtil.createSign(resultMap, PayConfig.XCX_KEY));
                resultMap.put("returnCode", "SUCCESS");
                resultMap.put("returnMsg", "OK");
                LOGGER.info("【小程序支付】统一下单成功，返回参数:" + resultMap);
            } else {
                resultMap.put("returnCode", resMap.get("return_code"));
                resultMap.put("returnMsg", resMap.get("return_msg"));
                LOGGER.info("【小程序支付】统一下单失败，失败原因:" + resMap.get("return_msg"));
            }
        }
        return resultMap;
    }

    *//**
     * 小程序支付统一下单
     *//*
    private Map<String, String> xcxUnifieldOrder(String orderNum, String tradeType, double payAmount, String openid) throws Exception {
        //封装参数
        SortedMap<String, String> paramMap = new TreeMap<String, String>();
        paramMap.put("appid", PayConfig.XCX_APP_ID);
        paramMap.put("mch_id", PayConfig.XCX_MCH_ID);
        paramMap.put("nonce_str", PayUtil.makeUUID(32));
        paramMap.put("body", BaseConstants.PLATFORM_COMPANY_NAME);
        paramMap.put("out_trade_no", orderNum);
        paramMap.put("total_fee", PayUtil.moneyToIntegerStr(payAmount));
        paramMap.put("spbill_create_ip", PayUtil.getLocalIp());
        paramMap.put("notify_url", this.getNotifyUrl());
        paramMap.put("trade_type", tradeType);
        paramMap.put("openid", openid);
        paramMap.put("sign", PayUtil.createSign(paramMap, PayConfig.XCX_KEY));
        //转换为xml
        String xmlData = PayUtil.mapToXml(paramMap);
        //请求微信后台，获取预支付ID
        String resXml = HttpUtils.postData(PayConfig.WX_PAY_UNIFIED_ORDER, xmlData);
        LOGGER.info("【小程序支付】 统一下单响应：\n" + resXml);
        return PayUtil.xmlStrToMap(resXml);
    }

    private String getNotifyUrl() {
        //服务域名
        return PayConfig.PRO_SERVER_DOMAIN + "/wxapp/payment/xcxNotify";
    }

    *//**
     * 添加或更新支付记录
     *//*
    @Override
    public int addOrUpdatePaymentRecord(String orderNo, double payAmount,......) throws Exception {
        //写自己的添加或更新支付记录的业务代码
        return 0;
    }

    @Override
    @Transactional(readOnly = false, rollbackFor = {Exception.class})
    public int xcxNotify(Map<String, Object> map) throws Exception {
        int flag = 0;
        //支付订单编号
        String orderNo = (String) map.get("out_trade_no");
        //检验是否需要再次回调刷新数据
        //TODO 微信后台回调，刷新订单支付状态等相关业务

        return flag;
    }*/
}
