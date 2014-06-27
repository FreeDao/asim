package com.view.asim.manager;

/**
 * 短信验证码管理
 * @author xuweinan
 */

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;


import com.view.asim.comm.Constant;
import com.view.asim.util.CryptoUtil;
import com.view.asim.util.ImageUtil;

import android.content.Context;
import android.util.Log;

public class SMSVerifyManager {
	private static final String TAG = "SMSVerifyManager";
	private static SMSVerifyManager INSTANCE = null;
	private Map<String, String> mVerificationCodeMap = null;
	
	/* 目前使用短信宝(smsbao.com)的服务 */
	private static final String SMS_API_URL = "http://www.smsbao.com/sms?";
	private static final String SMS_API_USERNAME_TAG = "u";
	private static final String SMS_API_PASSWORD_TAG = "p";
	private static final String SMS_API_CELLPHONE_TAG = "m";
	private static final String SMS_API_CONTENT_TAG = "c";
	private static final String SMS_API_RESULT_OK = "0";

	
	private static final String SMS_API_USERNAME = "allenforrest";
	private static final String SMS_API_PASSWORD = "xuweinan";
	private static final String SMS_API_CONTENT_PREFIX = "您好，您正在注册密信帐号，验证码：";
	private static final String SMS_API_CONTENT_SUFFIX = "，请勿泄露。【密信】";


	private SMSVerifyManager() {
		mVerificationCodeMap = new HashMap<String, String>();
	}

	public static SMSVerifyManager getInstance(Context context) {
		if (INSTANCE == null) {
			INSTANCE = new SMSVerifyManager();
		}
		return INSTANCE;
	}
	
	private String genCode() {
		int min = 1000;
		int max = 9999;

		Random r = new Random();
		return String.valueOf(r.nextInt(max - min + 1) + min);
	}

	public boolean sendSMSCode(String cellphone) {
		String code = genCode();
		mVerificationCodeMap.put(cellphone, code);
		
		try {
			String password = CryptoUtil.md5(SMS_API_PASSWORD);
			String verficationInfo = URLEncoder.encode(SMS_API_CONTENT_PREFIX + code + SMS_API_CONTENT_SUFFIX, "utf-8");
			String smsSendUrl = SMS_API_URL + 
					SMS_API_USERNAME_TAG + "=" + SMS_API_USERNAME + "&" +
					SMS_API_PASSWORD_TAG + "=" + password + "&" +
					SMS_API_CELLPHONE_TAG + "=" + cellphone + "&" +
					SMS_API_CONTENT_TAG + "=" + verficationInfo;
			Log.i(TAG, "send verfication SMS to " + cellphone + ": " + smsSendUrl);
			
			byte[] resp = ImageUtil.getBytesFromUrl(smsSendUrl, Constant.SMS_SEND_RESULT_TIMEOUT);
			String result = new String(resp);
			if (!result.trim().equals(SMS_API_RESULT_OK)) {
				Log.i(TAG, "send verfication SMS to " + cellphone + " failed, return: " + result);
				return false;
			}
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		
		Log.i(TAG, "send verfication SMS to " + cellphone + " succ");
		return true;
	}
	
	public boolean verification(String cellphone, String code) {
		boolean pass = false;
		if(mVerificationCodeMap.containsKey(cellphone) && 
		   mVerificationCodeMap.get(cellphone).equals(code)) {
			pass = true;
		}
		
		return pass;
	}
}
