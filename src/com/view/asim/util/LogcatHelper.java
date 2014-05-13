package com.view.asim.util;

import java.io.BufferedReader;  
import java.io.File;  
import java.io.FileNotFoundException;  
import java.io.FileOutputStream;  
import java.io.IOException;  
import java.io.InputStreamReader;  

import com.view.asim.comm.Constant;
  
import android.content.Context;  
  
/** 
 * log��־ͳ�Ʊ��� 
 *  
 * @author xuweinan 
 *  
 */  
  
public class LogcatHelper {  
  
    private static LogcatHelper INSTANCE = null;  
    private static String PATH_LOGCAT;  
    private LogDumper mLogDumper = null;  
    private int mPId;  
  
    /** 
     *  
     * ��ʼ��Ŀ¼ 
     *  
     * */  
    public void init(Context context) {  
        PATH_LOGCAT = Constant.SDCARD_ROOT_PATH + Constant.LOG_PATH + File.separator;  
        File file = new File(PATH_LOGCAT);  
        if (!file.exists()) {  
            file.mkdirs();  
        }  
    }  
  
    public static LogcatHelper getInstance(Context context) {  
        if (INSTANCE == null) {  
            INSTANCE = new LogcatHelper(context);  
        }  
        return INSTANCE;  
    }
    
    public static LogcatHelper getInstance() {    
        return INSTANCE;  
    }
  
    private LogcatHelper(Context context) {  
        init(context);  
        mPId = android.os.Process.myPid();  
    }  
  
    public void start() {  
        if (mLogDumper == null)  
            mLogDumper = new LogDumper(String.valueOf(mPId), PATH_LOGCAT);  
        mLogDumper.start();  
    }  
  
    public void stop() {  
        if (mLogDumper != null) {  
            mLogDumper.stopLogs();  
            mLogDumper = null;  
        }  
    }  
  
    private class LogDumper extends Thread {  
  
        private Process logcatProc;  
        private BufferedReader mReader = null;  
        private boolean mRunning = true;  
        String cmds = null;  
        private String mPID;  
        private FileOutputStream out = null;  
  
        public LogDumper(String pid, String dir) {  
            mPID = pid;  
            try {  
                out = new FileOutputStream(new File(dir, Constant.LOG_PREFIX  
                        + DateUtil.getCurDateStr("yyyy-MM-dd-HH-mm-ss") + ".log"));  
            } catch (FileNotFoundException e) {  
                e.printStackTrace();  
            }  
  
            /** 
             *  
             * ��־�ȼ���*:v , *:d , *:w , *:e , *:f , *:s 
             *  
             * ��ʾ��ǰmPID����� E��W�ȼ�����־. 
             *  
             * */  
  
            // cmds = "logcat *:e *:w | grep \"(" + mPID + ")\"";  
            // cmds = "logcat  | grep \"(" + mPID + ")\"";//��ӡ������־��Ϣ  
            // cmds = "logcat -s way";//��ӡ��ǩ������Ϣ  
            cmds = "logcat | grep \"(" + mPID + ")\"";  
  
        }  
  
        public void stopLogs() {  
            mRunning = false;  
        }  
  
        @Override  
        public void run() {  
            try {  
                logcatProc = Runtime.getRuntime().exec(cmds);  
                mReader = new BufferedReader(new InputStreamReader(  
                        logcatProc.getInputStream()), 1024);  
                String line = null;  
                while (mRunning && (line = mReader.readLine()) != null) {  
                    if (!mRunning) {  
                        break;  
                    }  
                    if (line.length() == 0) {  
                        continue;  
                    }  
                    if (out != null && line.contains(mPID)) {  
                        out.write((DateUtil.getCurDateStr() + "  " + line + "\n")  
                                .getBytes());  
                    }  
                }  
  
            } catch (IOException e) {  
                e.printStackTrace();  
            } finally {  
                if (logcatProc != null) {  
                    logcatProc.destroy();  
                    logcatProc = null;  
                }  
                if (mReader != null) {  
                    try {  
                        mReader.close();  
                        mReader = null;  
                    } catch (IOException e) {  
                        e.printStackTrace();  
                    }  
                }  
                if (out != null) {  
                    try {  
                        out.close();  
                    } catch (IOException e) {  
                        e.printStackTrace();  
                    }  
                    out = null;  
                }  
  
            }  
  
        }  
  
    }  
  
}  
