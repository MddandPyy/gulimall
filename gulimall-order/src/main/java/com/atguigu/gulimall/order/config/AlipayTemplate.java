package com.atguigu.gulimall.order.config;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gulimall.order.vo.PayVo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "alipay")
@Component
@Data
public class AlipayTemplate {

    //在支付宝创建的应用的id
    private String app_id = "2021000119672773";

    // 商户私钥，您的PKCS8格式RSA2私钥
    private String merchant_private_key = "MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQCLJav8rr5fjSJ6OwbHvN/olW9U/KnjUfHS2cXVCNlooOEuFDP5ihiHVusHwGp2bEu26dTOH2jV+73YGnHf69gwgso5FNjObA2q23J6GlWdz081Ssu8XysHYRHUHkXKz6n1WnQdszG06ByaarBDSGcnI0ANgAg3tFswUXnh56UMY8hgVQDg89kVeJ1cXFHxsSU1wsazv3T8wqWWFObXac0AUSFzGtLvRcUeQ1cMEPekG//0sD9W4gV30RYtUKNfPGl/SXzoB8XghbuA+rH9gybNLy2kPm8nD+xVzameEY7SCttlFGudPFZpnw0B6WCUMq8eBXFqD+th4wFpPCf3gbyTAgMBAAECggEANuDBpuNoE5k/CsPy1Z2pFTqycOtBOXUdEZJv0qgtznT8ukafhQAw0le2VBVQJ6poZhhspGqA1zv00KEFXlHOrrs2dDLDmw3EiikISh3OvHGBhLweBAnol31yIZ1a2zTxi+GDwB7nMLkGZuRC5hZNEIN3hJuFIisQd8r6bd0FHBA5HAYJ/yTCPj08uRDaJw/nTwHtPK5GcVowHjsauCzm63Yw5JYAcU6QbLe40wXb083l5y02AruwpHkVQtgZ2ItdYl3MCnYjVLlMk2iECXjivFvIy4m1tK45h3hH009Vlnf/xlgbnsNgS4ooJkmrsECY429hHixdliMKVGIUg+KR0QKBgQDxMxLwlmqdgsL8HfFXV+IIyyRmyPehgLu4Oe083PiNodRL3uil17/YlAV47MAQ59DFKMPbZvfWD2KvaY0wBLLVBu+m1J9gftToslA7JhApgAJXu9RGZeiNyFlGmmhlMplGQAejr30bVfnO6fPBJvKzKBDVO3Vn8GMbuv2XO1Wn6wKBgQCTr31zUFy/gA3/ZHSDxKo7rP7wxf2L6zAvNd9AzOA0vO9s1O608rA/kOz7j5N4c/Re4zo3tFo6+YYqMJwS2Sxo3RE+GXzC+FIxiUZEOuP3ojqzRNP3RKBAPPQIQQwCiC6n6QUxzGspTJJbnUPbr6V+UBA87PPBO29aJxMQ6yr7+QKBgB3zY6SwaOjKNgAsj61y4kSJRSnmRqmtXKIRv13RaqPbtor9URJW6iGwr8VGUMtInen9l+SEsr3IB08U4k1WKAa/575lpKzM0w9aJRBTN6qPlLrkPgXH3crNWBimT9RRJitNBCXVQsnEMVL6RiRz+6s3jG+OBJQ1i3kEDT1cRtSlAoGAK7XkFm4xlUhMXEuaJKnfjJwONKsU+QWaFE8IjcBVl6ZX4sk/AXVW7ulxCp+bESqYqEDuqTALCKtzpycN+pImj77mbI/Umkwu35R2pJkdgdyVLRvw9J1uJLUlsQ+3g0RVMsNIE04iZW3RX6YzQGFDRRhjRfUHzChQM8me3gIMjdkCgYEA2yEhEFJ8VeR2gKaOliEnzFfRCbs1xodff3el1K+Vdm8bTwonEZmtE0yUPR7IA5ErJpO1uuJ+iNcCRVXL+o3t5KSjr9P0ysvokajxpxW+92zeY5Vrgf7HiRrpBD0A7H3EpJ+no8i7tqUyWkr+gI93OHKrcXWYTdNWFiH1yrGKZvQ=";
	// 支付宝公钥,查看地址：https://openhome.alipay.com/platform/keyManage.htm 对应APPID下的支付宝公钥。
    private String alipay_public_key = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAxZuIihBlLrDu7SiKloZLeQG0r+w86P4SbMRsFHU4jYdbYDHIKGZMqfZXo/7CHvbX0kBEITuOFN3TRSRwEKao06+DHs609VNostB/spJLYVPv6r87yy+TaQCdtt7i3BdfefdDBNG7vw2XNFq9lB87K06QShkHvfTQmX4dm516ZEoE0FjOqNaiZ1s6o/TYCsf+n4KzUzSTyhVFSrQBGSvDjKjCxqvbroqkddIFUVwOGJfX8Ez/BJK/oUTxRfWBi2K9EbZB+ULfq7NDzwI/Cg6ocAD1DYb54H3lyjd1GxQYG9S/8sIZIu5/6pbTpRVsgzSo0h/B5H9HlWO+EWsCSODbBQIDAQAB";
	// 服务器[异步通知]页面路径  需http://格式的完整路径，不能加?id=123这类自定义参数，必须外网可以正常访问
    // 支付宝会悄悄的给我们发送一个请求，告诉我们支付成功的信息
    private String notify_url = "http://member.gulimall.com/memberOrder.html";

    // 页面跳转同步通知页面路径 需http://格式的完整路径，不能加?id=123这类自定义参数
    //同步通知，支付成功，一般跳转到成功页
    private String return_url = "http://member.gulimall.com/memberOrder.html";

    // 签名方式
    private String sign_type = "RSA2";

    // 字符编码格式
    private String charset = "utf-8";

    // 自动关单时间
    private String timeout = "15m";

    // 支付宝网关； https://openapi.alipaydev.com/gateway.do
    private String gatewayUrl = "https://openapi.alipaydev.com/gateway.do";

    public String pay(PayVo vo) throws AlipayApiException {

        //AlipayClient alipayClient = new DefaultAlipayClient(AlipayTemplate.gatewayUrl, AlipayTemplate.app_id, AlipayTemplate.merchant_private_key, "json", AlipayTemplate.charset, AlipayTemplate.alipay_public_key, AlipayTemplate.sign_type);
        //1、根据支付宝的配置生成一个支付客户端
        AlipayClient alipayClient = new DefaultAlipayClient(gatewayUrl,
                app_id, merchant_private_key, "json",
                charset, alipay_public_key, sign_type);

        //2、创建一个支付请求 //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(return_url);
        alipayRequest.setNotifyUrl(notify_url);

        //商户订单号，商户网站订单系统中唯一订单号，必填
        String out_trade_no = vo.getOut_trade_no();
        //付款金额，必填
        String total_amount = vo.getTotal_amount();
        //订单名称，必填
        String subject = vo.getSubject();
        //商品描述，可空
        String body = vo.getBody();

        // 30分钟内不付款就会自动关单
        alipayRequest.setBizContent("{\"out_trade_no\":\""+ out_trade_no +"\","
                + "\"total_amount\":\""+ total_amount +"\","
                + "\"subject\":\""+ subject +"\","
                + "\"body\":\""+ body +"\","
                + "\"timeout_express\":\"" + timeout + "\","
                + "\"product_code\":\"FAST_INSTANT_TRADE_PAY\"}");

        String result = alipayClient.pageExecute(alipayRequest).getBody();

        //会收到支付宝的响应，响应的是一个页面，只要浏览器显示这个页面，就会自动来到支付宝的收银台页面
        return result;
    }
}
