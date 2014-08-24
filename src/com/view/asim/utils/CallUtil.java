package com.view.asim.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.view.asim.R;
import com.view.asim.comm.ApplicationContext;
import com.view.asim.sip.api.SipCallSession.StatusCode;

import android.content.Context;
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
    	Context context = ApplicationContext.get();
    	String statusTxt = null;
    	switch(code) {
    	case StatusCode.OK:
    		statusTxt = context.getResources().getString(R.string.normal_call);
    		break;

    	case StatusCode.NOT_FOUND:
    	case StatusCode.SEND_FAILED:
    		statusTxt = context.getResources().getString(R.string.opposite_side_off_line);
    		break;
    		
    	case StatusCode.NO_REPSONSE:
    		statusTxt = context.getResources().getString(R.string.no_response);
    		break;
    		
    	case StatusCode.REQUEST_TERMINATED:
    		statusTxt = context.getResources().getString(R.string.initiative_cancel);
    		break;
    		
    	case StatusCode.BUSY_HERE:
    		statusTxt = context.getResources().getString(R.string.opposite_in_call);
    		break;
    		
    	case StatusCode.DECLINE:
    		statusTxt = context.getResources().getString(R.string.refuse);
    		break;	
    	
    	case StatusCode.ASIM_SECURITY_STATE_CHANGED:
    		statusTxt = context.getResources().getString(R.string.secret_state_changed);
    		break;	
    		
    	default:
    		statusTxt = context.getResources().getString(R.string.line_error);
    		break;
    		
    	}
    	return statusTxt;
    }
}
