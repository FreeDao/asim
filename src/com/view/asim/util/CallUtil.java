package com.view.asim.util;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    	case 200:
    		statusTxt = "正常通话";
    		break;

    	case 404:
    		statusTxt = "对方不在线";
    		break;
    		
    	case 408:
    		statusTxt = "无应答";
    		break;
    		
    	case 477:
    		statusTxt = "呼叫失败";
    		break;
    		
    	case 487:
    		statusTxt = "主动取消";
    		break;
    		
    	case 486:
    		statusTxt = "对方正在通话";
    		break;
    		
    	case 603:
    		statusTxt = "拒绝";
    		break;	
    	
    	default:
    		statusTxt = "线路异常";
    		break;
    		
    	}
    	return statusTxt;
    }
}
