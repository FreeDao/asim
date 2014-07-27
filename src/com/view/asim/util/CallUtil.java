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
 * Voice Call��ع��ߺ���
 * 
 * @author xuweinan
 * 
 */
public class CallUtil {
    
    public static String getStringByStatusCode(int code) {
    	String statusTxt = null;
    	switch(code) {
    	case 200:
    		statusTxt = "����ͨ��";
    		break;

    	case 404:
    		statusTxt = "�Է�������";
    		break;
    		
    	case 408:
    		statusTxt = "��Ӧ��";
    		break;
    		
    	case 477:
    		statusTxt = "����ʧ��";
    		break;
    		
    	case 487:
    		statusTxt = "����ȡ��";
    		break;
    		
    	case 486:
    		statusTxt = "�Է�����ͨ��";
    		break;
    		
    	case 603:
    		statusTxt = "�ܾ�";
    		break;	
    	
    	default:
    		statusTxt = "��·�쳣";
    		break;
    		
    	}
    	return statusTxt;
    }
}
