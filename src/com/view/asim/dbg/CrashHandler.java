package com.view.asim.dbg;

import java.io.File;  
import java.io.FileNotFoundException;  
import java.io.FileOutputStream;  
import java.io.IOException;  
import java.io.UnsupportedEncodingException;
import java.lang.Thread.UncaughtExceptionHandler;  
import java.lang.reflect.Field;  
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.HashMap;  
import java.util.Map;  

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.json.JSONException;
import org.json.JSONObject;

import com.view.asim.comm.Constant;
import com.view.asim.manager.AUKeyManager;
import com.view.asim.manager.ContacterManager;
import com.view.asim.model.Attachment;
import com.view.asim.model.CtrlMessage;
import com.view.asim.model.IMMessage;
import com.view.asim.model.User;
import com.view.asim.qiniu.auth.JSONObjectRet;
import com.view.asim.qiniu.io.IO;
import com.view.asim.qiniu.io.PutExtra;
import com.view.asim.util.DateUtil;
import com.view.asim.util.FileUtil;
  
import android.app.AlertDialog;
import android.content.Context;  
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;  
import android.content.pm.PackageManager;  
import android.content.pm.PackageManager.NameNotFoundException;  
import android.net.Uri;
import android.os.Build;  
import android.os.Environment;
import android.os.Looper;  
import android.util.Base64;
import android.util.Log;  
import android.view.WindowManager;
import android.widget.Toast;  
  
/** 
 * UncaughtException������,��������Uncaught�쳣��ʱ��,�ɸ������ӹܳ���,����¼���ʹ��󱨸�. 
 *  
 * @author way 
 *  
 */  
public class CrashHandler implements UncaughtExceptionHandler {  
    private static final String TAG = "CrashHandler";  
    private static String PATH_CRASH = null;
    private Thread.UncaughtExceptionHandler mDefaultHandler;// ϵͳĬ�ϵ�UncaughtException������  
    private static CrashHandler INSTANCE = new CrashHandler();// CrashHandlerʵ��  
    private Context mContext;// �����Context����  
    //private Map<String, String> info = new HashMap<String, String>();// �����洢�豸��Ϣ���쳣��Ϣ  
  
    /** ��ֻ֤��һ��CrashHandlerʵ�� */  
    private CrashHandler() {  
  
    }  
  
    /** ��ȡCrashHandlerʵ�� ,����ģʽ */  
    public static CrashHandler getInstance() {  
        return INSTANCE;  
    }  
  
    /** 
     * ��ʼ�� 
     *  
     * @param context 
     */  
    public void init(Context context) {  
        mContext = context;  
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();// ��ȡϵͳĬ�ϵ�UncaughtException������  
        Thread.setDefaultUncaughtExceptionHandler(this);// ���ø�CrashHandlerΪ�����Ĭ�ϴ�����  
    }  
  
    /** 
     * ��UncaughtException����ʱ��ת�����д�ķ��������� 
     */  
    public void uncaughtException(Thread thread, Throwable ex) {  
        if (!handleException(ex) && mDefaultHandler != null) {  
            // ����Զ����û�д�������ϵͳĬ�ϵ��쳣������������  
            mDefaultHandler.uncaughtException(thread, ex);  
        }  else {  
            try {  
                Thread.sleep(5000);// ��������ˣ��ó����������5�����˳�����֤�ļ����沢�ϴ���������  
            } catch (InterruptedException e) {  
                e.printStackTrace();  
            }  
            // �˳�����  
            android.os.Process.killProcess(android.os.Process.myPid());  
            System.exit(1);  
        }
    }  
  
    /** 
     * �Զ��������,�ռ�������Ϣ ���ʹ��󱨸�Ȳ������ڴ����. 
     *  
     * @param ex 
     *            �쳣��Ϣ 
     * @return true ��������˸��쳣��Ϣ;���򷵻�false. 
     */  
    public boolean handleException(Throwable ex) {  
        if (ex == null || mContext == null)  
            return false;  
        final String crashReport = getCrashReport(mContext, ex);  
        Log.e(TAG, crashReport);  
        new Thread() {  
            public void run() {  
                Looper.prepare();  
                File file = save2File(crashReport);  
                //sendAppCrashReport(mContext, crashReport, file);  
                Toast.makeText(mContext, "��Ǹ�������˵�С������˳���", Toast.LENGTH_LONG).show(); 
                new UploadCrashDumpsThread(file).start();
                Looper.loop();  
            }  
  
        }.start();  
        return true;  
    }     
  
    private File save2File(String crashReport) {  
        String fileName = FileUtil.genCrashFileName();  
        
        try {   
        	PATH_CRASH = FileUtil.getGlobalLogPath();  
            File dir = new File(PATH_CRASH);
            FileUtil.createDir(dir);
            
            File file = new File(PATH_CRASH, fileName);  
            FileOutputStream fos = new FileOutputStream(file);  
            fos.write(crashReport.toString().getBytes());  
            fos.close();  
            return file;  
        } catch (FileNotFoundException e) {  
            e.printStackTrace();  
        } catch (IOException e) {  
            e.printStackTrace();  
        }
        
        return null;  
    }  
  
    private void sendAppCrashReport(final Context context,  
            final String crashReport, final File file) {  
        AlertDialog mDialog = null;  
        AlertDialog.Builder builder = new AlertDialog.Builder(context);  
        builder.setIcon(android.R.drawable.ic_dialog_info);  
        builder.setTitle("��Ǹ�������˵�С��");  
        builder.setMessage("��Ѵ��󱨸����ʼ�����ʽ�ύ�����ǣ�лл��");  
        builder.setPositiveButton(android.R.string.ok,  
                new DialogInterface.OnClickListener() {  
                    public void onClick(DialogInterface dialog, int which) {  
  
                        // �����쳣����  
                        try {  
                            //�������Ը�����ʽ�����ʼ�  
                            Intent intent = new Intent(Intent.ACTION_SEND);  
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);  
                            String[] tos = { "allenxu@gmail.com" };  
                            intent.putExtra(Intent.EXTRA_EMAIL, tos);  
  
                            intent.putExtra(Intent.EXTRA_SUBJECT,  
                                    "���� Android�ͻ��� - ���󱨸�");
                            if (file != null) {  
                                intent.putExtra(Intent.EXTRA_STREAM,  
                                        Uri.fromFile(file));  
                                intent.putExtra(Intent.EXTRA_TEXT,  
                                        "�뽫�˴��󱨸淢�͸����ǣ��Ա㾡���޸����⣬лл��");  
                            } else {  
                                intent.putExtra(Intent.EXTRA_TEXT,  
                                        "�뽫�˴��󱨸淢�͸����ǣ��Ա㾡���޸����⣬лл��"  
                                                + crashReport);  
                            }  
                            intent.setType("text/plain");  
                            intent.setType("message/rfc882");  
                            Intent.createChooser(intent, "Choose Email Client");  
                            context.startActivity(intent);  
                        } catch (Exception e) {  
                            Toast.makeText(context,  
                                    "There are no email clients installed.",  
                                    Toast.LENGTH_SHORT).show();  
                        } finally {  
                            dialog.dismiss();  
                            // �˳�  
                            android.os.Process.killProcess(android.os.Process  
                                    .myPid());  
                            System.exit(1);  
                        }  
                    }  
                });  
        builder.setNegativeButton(android.R.string.cancel,  
                new DialogInterface.OnClickListener() {  
                    public void onClick(DialogInterface dialog, int which) {  
                        dialog.dismiss();  
                        // �˳�  
                        android.os.Process.killProcess(android.os.Process  
                                .myPid());  
                        System.exit(1);  
                    }  
                });  
        mDialog = builder.create();  
        mDialog.getWindow().setType(  
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);  
        mDialog.show();  
    }  
  
    /** 
     * ��ȡAPP�����쳣���� 
     *  
     * @param ex 
     * @return 
     */  
    private String getCrashReport(Context context, Throwable ex) {  
        PackageInfo pinfo = getPackageInfo(context);  
        StringBuffer exceptionStr = new StringBuffer(); 
        exceptionStr.append("Manufacture: " + android.os.Build.MANUFACTURER + "\n");  
        exceptionStr.append("Product: " + android.os.Build.PRODUCT + "\n");  
        exceptionStr.append("Brand: " + android.os.Build.BRAND + "\n");  
        exceptionStr.append("Board: " + android.os.Build.BOARD + "\n");  
        exceptionStr.append("Device: " + android.os.Build.DEVICE + "\n");  
        exceptionStr.append("Model: " + android.os.Build.MODEL + "\n");  
        exceptionStr.append("App Version: " + pinfo.versionName + "\n");  
        exceptionStr.append("System Version: " + android.os.Build.DISPLAY + "\n");  
        exceptionStr.append("Android Version: " + android.os.Build.VERSION.RELEASE + "\n\n");  
        exceptionStr.append("Exception: " + ex + "\n");  
        StackTraceElement[] elements = ex.getStackTrace();  
        for (int i = 0; i < elements.length; i++) {  
            exceptionStr.append(elements[i].toString() + "\n");  
        }  
        return exceptionStr.toString();  
    }  
  
    /** 
     * ��ȡApp��װ����Ϣ 
     *  
     * @return 
     */  
    private PackageInfo getPackageInfo(Context context) {  
        PackageInfo info = null;  
        try {  
            info = context.getPackageManager().getPackageInfo(  
                    context.getPackageName(), 0);  
        } catch (NameNotFoundException e) {  
        }  
        if (info == null)  
            info = new PackageInfo();  
        return info;  
    }  
    
    
    private class UploadCrashDumpsThread extends Thread {
    	private File dumpFile = null;
    	private String uptoken = null;
    	private String accessKey = "2h4PWQ6V8_OJcMvejQvaa-tFm1-oRcCTNMZ5q_KG";
    	
    	// FIXME: �ϸ���˵ SK ��Ӧ�� hard-code �ڴˣ���Ϊû�а�ȫ����������ʱ�򵥴���
    	private String secretKey = "hxUxU1W37BkNqF--CU48EZwg32ep2Oy9fIAdsIQQ";

    	public UploadCrashDumpsThread(File file) {
    		dumpFile = file;
    	}
    	
    	@Override
        public void run() {
        	Log.d(TAG, "upload dumps files.");
        	
        	try {
    			initToken();
    		} catch (JSONException e) {
    			e.printStackTrace();
    			return;
    		}
        	
        	doUpload(Uri.fromFile(dumpFile));
     
        }
    	
    	private void initToken() throws JSONException {
    		long deadline = (long) Calendar.getInstance().getTimeInMillis() / 1000 + 3600;
    		JSONObject policy = new JSONObject();
    		policy.put("scope", "com-viewiot-mobile-asim-dumps");
    		policy.put("deadline", deadline);
    		policy.put("returnBody", 
    				"{\"key\":$(key),\"hash\":$(etag),\"size\":$(fsize),\"mimeType\":$(mimeType),\"width\":$(imageInfo.width),\"height\":$(imageInfo.height)}");
    		policy.put("detectMime", 0);

    		String policyJson = policy.toString().trim();
    		
    		byte[] policyJsonBase64 = Base64.encode(policyJson.getBytes(), Base64.URL_SAFE);
    		String p = new String(policyJsonBase64).trim();

    		String sign = null;
    		try {
    			sign = hmacSha1(p, secretKey);
    		} catch (InvalidKeyException e) {
    			e.printStackTrace();
    		} catch (UnsupportedEncodingException e) {
    			e.printStackTrace();
    		} catch (NoSuchAlgorithmException e) {
    			e.printStackTrace();
    		}		
    		uptoken = accessKey + ":" + sign + ":" + p;
    	}
    	
    	private String hmacSha1(String value, String key)
    	        throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
    	    String type = "HmacSHA1";
    	    SecretKeySpec secret = new SecretKeySpec(key.getBytes(), type);
    	    Mac mac = Mac.getInstance(type);
    	    mac.init(secret);
    	    byte[] bytes = mac.doFinal(value.getBytes());
    	    byte[] result = Base64.encode(bytes, Base64.URL_SAFE);     
    	    return new String(result).trim();
    	}

    	private void doUpload(Uri uri) {
    		String key = uri.getPath().substring(uri.getPath().lastIndexOf("/") + 1, uri.getPath().length());
    		key = android.os.Build.MANUFACTURER + "-" + android.os.Build.PRODUCT + "-" + key;
    		
    		if (ContacterManager.userMe != null) {
    			key = ContacterManager.userMe.getName() + "-" + key;
    		}
    		
    		PutExtra extra = new PutExtra();
    		extra.params = new HashMap<String, String>();
    		
    		Log.d(TAG, "upload file = " + uri.toString());
    		IO.putFile(mContext, uptoken, key, uri, extra, new JSONObjectRet() {
    			@Override
    			public void onProcess(long current, long total) {
    				
    			}

    			@Override
    			public void onSuccess(JSONObject resp) {
    	    		Log.d(TAG, "upload file success.");
    				// �˳�  
                    android.os.Process.killProcess(android.os.Process  
                            .myPid());  
                    System.exit(1);  
    			}

    			@Override
    			public void onFailure(Exception ex) {
    	    		Log.e(TAG, "upload file failed: " + ex.getMessage());
    			}
    		});
    	}
    }
    
}  
