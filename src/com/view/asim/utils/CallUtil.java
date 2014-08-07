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
 * Voice Call��ع��ߺ���
 * 
 * @author xuweinan
 * 
 */
public class CallUtil {
    
    public static String getStringByStatusCode(int code) {
    	String statusTxt = null;
    	switch(code) {
    	case StatusCode.OK:
    		statusTxt = "����ͨ��";
    		break;

    	case StatusCode.NOT_FOUND:
    	case StatusCode.SEND_FAILED:
    		statusTxt = "�Է�������";
    		break;
    		
    	case StatusCode.NO_REPSONSE:
    		statusTxt = "��Ӧ��";
    		break;
    		
    	case StatusCode.REQUEST_TERMINATED:
    		statusTxt = "����ȡ��";
    		break;
    		
    	case StatusCode.BUSY_HERE:
    		statusTxt = "�Է�����ͨ��";
    		break;
    		
    	case StatusCode.DECLINE:
    		statusTxt = "�ܾ�";
    		break;	
    	
    	case StatusCode.ASIM_SECURITY_STATE_CHANGED:
    		statusTxt = "�ܶ�״̬�仯";
    		break;	
    		
    	default:
    		statusTxt = "��·�쳣";
    		break;
    		
    	}
    	return statusTxt;
    }
}
