package com.ruoyi.web.controller.pay;

import com.alibaba.fastjson.JSONObject;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.utils.Base64;
import com.ruoyi.common.utils.ResultInfo;
import com.ruoyi.common.config.WeixinConfig;

import com.ruoyi.common.utils.StreamUtil;
import com.wechat.pay.contrib.apache.httpclient.WechatPayHttpClientBuilder;
import com.wechat.pay.contrib.apache.httpclient.auth.PrivateKeySigner;
import com.wechat.pay.contrib.apache.httpclient.auth.ScheduledUpdateCertificatesVerifier;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Credentials;
import com.wechat.pay.contrib.apache.httpclient.auth.WechatPay2Validator;
import com.wechat.pay.contrib.apache.httpclient.util.AesUtil;
import com.wechat.pay.contrib.apache.httpclient.util.PemUtil;
import io.swagger.annotations.ApiOperation;
import jodd.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.wechat.pay.java.core.Config;
import com.wechat.pay.java.core.RSAAutoCertificateConfig;
import com.wechat.pay.java.service.payments.jsapi.JsapiService;
import com.wechat.pay.java.service.payments.jsapi.model.Amount;
import com.wechat.pay.java.service.payments.jsapi.model.Payer;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayRequest;
import com.wechat.pay.java.service.payments.jsapi.model.PrepayResponse;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.PrivateKey;
import java.security.Signature;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.fasterxml.jackson.databind.type.LogicalType.DateTime;

@Controller
@RequestMapping(value = "/payment")
public class PaymentController extends BaseController{

    /*** appid */
    //public static final String appid = "wx43ddc18649800920";
    /** 商户号 */
    //public static final String merchantId = "1530763191";
    /** 商户API私钥路径 */
    //public static final String privateKeyPath = "classpath:apiclient_key.pem";
    /** 商户证书序列号 */
    //public static final String merchantSerialNumber = "4238A6C980C461A503F6E83061517E413C7C8239";
    /** 商户APIV3密钥 */
    //public static final String apiV3key = "f9b959f2063d50134184119f08359549";
    /** 授权（必填）固定 */
    //public static final String grantType = "authorization_code";

    /**
     * login
     * @throws IOException
     * @throws
     */
    @RequestMapping(value="/login")
    @ResponseBody
    public Object login(String nickName, String avatarUrl, String code){
        try {
            CloseableHttpClient httpclient = HttpClients.createDefault();
            String url = "https://api.weixin.qq.com/sns/jscode2session?appid="+WeixinConfig.getWeixinMiniAppid()+"&secret="+WeixinConfig.getWeixinMiniSecret()+"&js_code="+code+"&grant_type=authorization_code";
            HttpGet get = new HttpGet(url);
            CloseableHttpResponse response = httpclient.execute(get);
            String result = EntityUtils.toString(response.getEntity(), "utf-8");
            JSONObject json = JSONObject.parseObject(result);
            if(!json.containsKey("errcode")) {
                String openid = json.getString("openid");
				/*User user = this.findByOpenid(openid);
				if(user != null) {

					return "success";
				}
				user = new User();
				this.insert(user);*/
                return openid;
            }

        }catch (Exception e) {
            e.printStackTrace();
        }
        return "error";
    }

    /**
     * 支付
     * @param totalFee
     * @param code
     * @return
     * @throws ParseException
     * @throws IOException
     * 视频教程参考:https://space.bilibili.com/431152063
     * 密云榛子IT教育 http://it.zhenzikj.com
     */
    @RequestMapping("/pay")
    @ResponseBody
    public ResultInfo<Object> pay(double totalFee, String code) throws ParseException, IOException{
        try {
            JSONObject order = new JSONObject();
            order.put("appid", WeixinConfig.getWeixinMiniAppid());
            order.put("mchid", WeixinConfig.getWeixinMiniMchId());
            order.put("description", "充值-密云榛子IT教育");
            order.put("out_trade_no",  new SimpleDateFormat("yyyyMMddHHmmssSSS").format(new Date()));
            order.put("notify_url", WeixinConfig.getWeixinMiniNotifyUrl());

            JSONObject amount = new JSONObject();
            amount.put("total", (long)(totalFee * 100));
            amount.put("currency", "CNY");
            order.put("amount", amount);

            JSONObject payer = new JSONObject();
            payer.put("openid", this.getOpenId(code));
            order.put("payer", payer);

            logger.info(order.toJSONString());

            PrivateKey merchantPrivateKey = getPrivateKey();

            // 使用定时更新的签名验证器，不需要传入证书
            ScheduledUpdateCertificatesVerifier verifier = new ScheduledUpdateCertificatesVerifier(
                    new WechatPay2Credentials(WeixinConfig.getWeixinMiniMchId(), new PrivateKeySigner(WeixinConfig.getWeixinMiniMerchantSerialNumber(), merchantPrivateKey)),
                    WeixinConfig.getWeixinMiniApiV3Key().getBytes(StandardCharsets.UTF_8));
            WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
                    .withMerchant(WeixinConfig.getWeixinMiniMchId(), WeixinConfig.getWeixinMiniMerchantSerialNumber(), merchantPrivateKey)
                    .withValidator(new WechatPay2Validator(verifier));
            // ... 接下来，你仍然可以通过builder设置各种参数，来配置你的HttpClient

            // 通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签，并进行证书自动更新
            HttpClient httpClient = builder.build();
            HttpPost httpPost = new HttpPost("https://api.mch.weixin.qq.com/v3/pay/transactions/jsapi");
            httpPost.addHeader("Accept", "application/json");
            httpPost.addHeader("Content-type","application/json; charset=utf-8");
            httpPost.setEntity(new StringEntity(order.toJSONString(), "UTF-8"));
            // 后面跟使用Apache HttpClient一样
            HttpResponse response = httpClient.execute(httpPost);
            String bodyAsString = EntityUtils.toString(response.getEntity());

            JSONObject bodyAsJSON = JSONObject.parseObject(bodyAsString);
            logger.info(bodyAsJSON.toJSONString());
            if(bodyAsJSON.containsKey("code")) {
                return new ResultInfo(1, bodyAsJSON.getString("message"));
            }
            final String prepay_id = bodyAsJSON.getString("prepay_id");
            final String timeStamp = String.valueOf(System.currentTimeMillis());
            final String nonceStr = this.getRandomStringByLength(32);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(WeixinConfig.getWeixinMiniAppid() + "\n");
            stringBuffer.append(timeStamp + "\n");
            stringBuffer.append(nonceStr + "\n");
            stringBuffer.append("prepay_id="+prepay_id+"\n");
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(merchantPrivateKey);
            signature.update(stringBuffer.toString().getBytes("UTF-8"));
            byte[] signBytes = signature.sign();
            String paySign = Base64.encodeBytes(signBytes);

            JSONObject params = new JSONObject();
            params.put("appId", WeixinConfig.getWeixinMiniAppid());
            params.put("timeStamp", timeStamp);
            params.put("nonceStr", nonceStr);
            params.put("prepay_id", prepay_id);
            params.put("signType", "RSA");
            params.put("paySign", paySign);

            return new ResultInfo<Object>(0, params);
        } catch (Exception e) {
            logger.info(e.toString());
            e.printStackTrace();
        }
        return null;
    }

/*    @ApiOperation(value = "支付接口")
    @PostMapping(value = "/pay2")
    public PrepayResponse pay2(@RequestBody PrepayRequest request) throws IOException {
        Config config = new RSAAutoCertificateConfig.Builder()
                .merchantId(merchantId)
                .privateKeyFromPath(privateKeyPath)
                .merchantSerialNumber(merchantSerialNumber)
                .apiV3Key(apiV3key)
                .build();
        JsapiService service = new JsapiService.Builder().config(config).build();
        Amount amount = new Amount();
        amount.setTotal(1);


        request.setAmount(amount);
        request.setAppid(appid);
        request.setMchid(merchantId);
        request.setDescription("测试商品标题");
        request.setNotifyUrl("https://notify_url");
        request.setOutTradeNo(UUID.randomUUID().toString());
        Payer payer = new Payer();
        payer.setOpenid(getOpenId("oUpF8uMuAJO_M2pxb1Q9zNjWeS6o"));
        request.setPayer(payer);
        PrepayResponse response = service.prepay(request);
        System.out.println(response.getPrepayId());
        return response;
    }*/


    /**
     * 支付通知(api)
     */
    @RequestMapping(value="/payNotice")
    public void payNotice(
            HttpServletRequest request,
            HttpServletResponse response){
        try {
            String reqParams = StreamUtil.read(request.getInputStream());
            logger.info("-------支付结果:"+reqParams);
            JSONObject json = JSONObject.parseObject(reqParams);
            if(json.getString("event_type").equals("TRANSACTION.SUCCESS")){
                logger.info("-------支付成功");

            }
            String ciphertext = json.getJSONObject("resource").getString("ciphertext");
            final String associated_data = json.getJSONObject("resource").getString("associated_data");
            final String nonce = json.getJSONObject("resource").getString("nonce");
            AesUtil aesUtil = new AesUtil(WeixinConfig.getWeixinMiniApiV3Key().getBytes());
            ciphertext = aesUtil.decryptToString(associated_data.getBytes(), nonce.getBytes(), ciphertext);
            logger.info("-------ciphertext:"+ciphertext);
            logger.info(JSONObject.parseObject(ciphertext).getString("out_trade_no"));
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
        }
    }

    /**
     * 退款(api)
     */
    @RequestMapping(value="/refund")
    public void refund(String out_trade_no, String out_refund_no, String reason, double totalFee, double total){
        try {
            JSONObject order = new JSONObject();
            order.put("out_trade_no", out_trade_no);//商户订单号
            order.put("out_refund_no", out_refund_no);//商户退款单号
            order.put("reason", reason);//退款原因
            order.put("notify_url", WeixinConfig.getWeixinMiniRefundNotifyUrl());//退款通知

            JSONObject amount = new JSONObject();
            amount.put("refund", (long)(totalFee * 100));//退款金额
            amount.put("currency", "CNY");
            amount.put("total", (long)(total * 100));//原订单金额
            order.put("amount", amount);

            PrivateKey merchantPrivateKey = getPrivateKey();

            // 使用定时更新的签名验证器，不需要传入证书
            ScheduledUpdateCertificatesVerifier verifier = new ScheduledUpdateCertificatesVerifier(
                    new WechatPay2Credentials(WeixinConfig.getWeixinMiniMchId(), new PrivateKeySigner(WeixinConfig.getWeixinMiniMerchantSerialNumber(), merchantPrivateKey)),
                    WeixinConfig.getWeixinMiniApiV3Key().getBytes(StandardCharsets.UTF_8));
            WechatPayHttpClientBuilder builder = WechatPayHttpClientBuilder.create()
                    .withMerchant(WeixinConfig.getWeixinMiniMchId(), WeixinConfig.getWeixinMiniMerchantSerialNumber(), merchantPrivateKey)
                    .withValidator(new WechatPay2Validator(verifier));
            // ... 接下来，你仍然可以通过builder设置各种参数，来配置你的HttpClient

            // 通过WechatPayHttpClientBuilder构造的HttpClient，会自动的处理签名和验签，并进行证书自动更新
            HttpClient httpClient = builder.build();
            HttpPost httpPost = new HttpPost("https://api.mch.weixin.qq.com/v3/refund/domestic/refunds");
            httpPost.addHeader("Accept", "application/json");
            httpPost.addHeader("Content-type","application/json; charset=utf-8");
            httpPost.setEntity(new StringEntity(order.toJSONString(), "UTF-8"));
            // 后面跟使用Apache HttpClient一样
            HttpResponse response = httpClient.execute(httpPost);
            String bodyAsString = EntityUtils.toString(response.getEntity());

            JSONObject bodyAsJSON = JSONObject.parseObject(bodyAsString);
            logger.info(bodyAsJSON.toJSONString());

            final String status = bodyAsJSON.getString("status");
            if(status.equals("SUCCESS")){
                logger.info("退款成功");
            }else if(status.equals("CLOSED")){
                logger.info("退款关闭");
            }else if(status.equals("PROCESSING")){
                logger.info("退款处理中");
            }else if(status.equals("ABNORMAL")){
                logger.info("退款异常");
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.info(e.toString());
            e.printStackTrace();
        }
    }
    /**
     * 退款通知(api)
     */
    @RequestMapping(value="/refundNotice")
    public void refundNotice(HttpServletRequest request, HttpServletResponse response){
        try {
            String reqParams = StreamUtil.read(request.getInputStream());
            logger.info("-------支付结果:"+reqParams);
            JSONObject json = JSONObject.parseObject(reqParams);
            final String event_type = json.getString("event_type");
            if(event_type.equals("REFUND.SUCCESS")){
                logger.info("-------退款成功");
            }else if(event_type.equals("REFUND.ABNORMAL")){
                logger.info("-------退款异常");
            }else if(event_type.equals("REFUND.CLOSED")){
                logger.info("-------退款关闭");
            }
            String ciphertext = json.getJSONObject("resource").getString("ciphertext");
            final String associated_data = json.getJSONObject("resource").getString("associated_data");
            final String nonce = json.getJSONObject("resource").getString("nonce");
            AesUtil aesUtil = new AesUtil(WeixinConfig.getWeixinMiniApiV3Key().getBytes());
            ciphertext = aesUtil.decryptToString(associated_data.getBytes(), nonce.getBytes(), ciphertext);
            logger.info("-------ciphertext:"+ciphertext);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error(e.toString());
        }
    }

    /**
     * 获取API私钥
     * @return
     */
    private PrivateKey getPrivateKey(){
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("apiclient_key.pem");
        PrivateKey merchantPrivateKey = PemUtil.loadPrivateKey(inputStream);
        return merchantPrivateKey;
    }


    /**
     * 生成openid
     * @param code 登录code
     * @return
     *
     */
    private String getOpenId(String code) throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();
        String url = "https://api.weixin.qq.com/sns/jscode2session?appid="+WeixinConfig.getWeixinMiniAppid()+"&secret="+WeixinConfig.getWeixinMiniSecret()+"&js_code="+code+"&grant_type=authorization_code";
        HttpGet get = new HttpGet(url);
        CloseableHttpResponse response = httpClient.execute(get);
        String result = EntityUtils.toString(response.getEntity(),"utf-8");
        JSONObject json = JSONObject.parseObject(result);
        String openid = "";
        if(!json.containsKey("errcode")){
            openid = json.getString("openid");
        }
        System.out.println("code换取的openid======" +openid);
        return openid;
    }


    /**
     * 获取一定长度的随机字符串
     * @param length 指定字符串长度
     * @return 一定长度的字符串
     */
    private String getRandomStringByLength(int length) {
        String base = "abcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }


}