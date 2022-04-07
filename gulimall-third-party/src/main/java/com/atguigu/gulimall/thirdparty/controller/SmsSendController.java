package com.atguigu.gulimall.thirdparty.controller;

import com.aliyuncs.DefaultAcsClient;
import com.aliyuncs.IAcsClient;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsRequest;
import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.aliyuncs.exceptions.ServerException;
import com.aliyuncs.profile.DefaultProfile;
import com.atguigu.common.exception.BizCodeEnum;
import com.atguigu.common.utils.R;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * <p>Title: SmsSendController</p>
 * Description：
 * date：2020/6/25 14:53
 */
@Controller
@RequestMapping("/sms")
public class SmsSendController {

	@Value("${spring.cloud.alicloud.access-key}")
	private String accessKeyId;

	@Value("${spring.cloud.alicloud.secret-key}")
	private String secretKey;

	/*** 提供给别的服务进行调用的
	 */
	@GetMapping("/sendcode")
	public R sendCode(@RequestParam("phone") String phone, @RequestParam("code") String code){
		System.out.println("开始发送短信");
		DefaultProfile profile = DefaultProfile.getProfile("cn-hangzhou", accessKeyId, secretKey);
		/** use STS Token
		 DefaultProfile profile = DefaultProfile.getProfile(
		 "<your-region-id>",           // The region ID
		 "<your-access-key-id>",       // The AccessKey ID of the RAM account
		 "<your-access-key-secret>",   // The AccessKey Secret of the RAM account
		 "<your-sts-token>");          // STS Token
		 **/
		IAcsClient client = new DefaultAcsClient(profile);

		SendSmsRequest request = new SendSmsRequest();
		request.setSignName("阿里云短信测试");
		request.setTemplateCode("SMS_154950909");
		request.setPhoneNumbers(phone);
		request.setTemplateParam("{\"code\":\""+code+"\"}");

		try {
			SendSmsResponse response = client.getAcsResponse(request);
			if("OK".equals(response.getCode())){
				return R.ok();
			}
		} catch (ServerException e) {
			e.printStackTrace();

		} catch (ClientException e) {
			System.out.println("ErrCode:" + e.getErrCode());
			System.out.println("ErrMsg:" + e.getErrMsg());
			System.out.println("RequestId:" + e.getRequestId());
		}
		return R.error(BizCodeEnum.SMS_SEND_CODE_EXCEPTION.getCode(), BizCodeEnum.SMS_SEND_CODE_EXCEPTION.getMsg());
	}
}
