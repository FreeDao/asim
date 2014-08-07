package com.view.asim.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.view.asim.sip.api.SipCallSession.StatusCode;

import android.widget.EditText;
import android.widget.TextView;

/**
 * Voice Call相关工具函数
 * 
 * @author xuweinan
 * 
 */
public class CallUtil {
    
    public static String getStringByStatusCode(int code) {
    	String statusTxt = null;
    	switch(code) {
    	case StatusCode.OK:
    		statusTxt = "正常通话";
    		break;

    	case StatusCode.NOT_FOUND:
    	case StatusCode.SEND_FAILED:
    		statusTxt = "对方不在线";
    		break;
    		
    	case StatusCode.NO_REPSONSE:
    		statusTxt = "无应答";
    		break;
    		
    	case StatusCode.REQUEST_TERMINATED:
    		statusTxt = "主动取消";
    		break;
    		
    	case StatusCode.BUSY_HERE:
    		statusTxt = "对方正在通话";
    		break;
    		
    	case StatusCode.DECLINE:
    		statusTxt = "拒绝";
    		break;	
    	
    	case StatusCode.ASIM_SECURITY_STATE_CHANGED:
    		statusTxt = "密盾状态变化";
    		break;	
    		
    	default:
    		statusTxt = "线路异常";
    		break;
    		
    	}
    	return statusTxt;
    }
}
