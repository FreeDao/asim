package com.view.asim.dbg;


import java.io.BufferedInputStream;
import java.io.BufferedReader;  
import java.io.File;  
import java.io.FileNotFoundException;  
import java.io.FileOutputStream;  
import java.io.IOException;  
import java.io.InputStream;
import java.io.InputStreamReader;  
import java.util.ArrayList;
import java.util.List;

import com.view.asim.comm.Constant;
import com.view.asim.utils.DateUtil;
import com.view.asim.utils.FileUtil;
  
import android.content.Context;  
import android.util.Log;
  
/** 
 * log��־ͳ�Ʊ��� 
 *  
 * @author xuweinan 
 *  
 */  
  
public class LogcatHelper {  
  
    private static final String TAG = "LogcatHelper";
	private static LogcatHelper INSTANCE = null;  
    private static String PATH_LOGCAT;  
    private Context mCntx = null;
    private LogDumper mLogDumper = null;  
    private int mPId;  
  
    /** 
     *  
     * ��ʼ��Ŀ¼ 
     *  
     * */  
    public void init(Context context) {
    	mCntx = context;
        PATH_LOGCAT = FileUtil.getGlobalLogPath();  
        File file = new File(PATH_LOGCAT);  
        FileUtil.createDir(file);
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
    	List<String> orgProcessList = getAllProcess();  
        List<ProcessInfo> processInfoList = getProcessInfoList(orgProcessList);  
        killLogcatProc(processInfoList);  
        
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
                out = new FileOutputStream(new File(dir, FileUtil.genLogFileName()));  
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
    
    /** 
     * �ر��ɱ���������logcat���̣� 
     * �����û�����ɱ������(����Ǳ�������̿�����Logcat�ռ�������ô���ߵ�USERһ��) 
     * ������رջ��ж�����̶�ȡlogcat��־������Ϣд����־�ļ� 
     *  
     * @param allProcList 
     * @return 
     */  
    private void killLogcatProc(List<ProcessInfo> allProcList) {  
         
        String packName = mCntx.getPackageName();
        String myUser = getAppUser(packName, allProcList);  
        
        Log.d(TAG, "package name: " + packName + ", user: " + myUser);
        
        for (ProcessInfo processInfo : allProcList) {  
            if (processInfo.name.toLowerCase().equals("logcat")  
                    && processInfo.user.equals(myUser)) { 
                Log.d(TAG, "old logcat pid " + processInfo.pid + " will be killed");

                android.os.Process.killProcess(Integer  
                        .parseInt(processInfo.pid));
            }  
        }  
    }
    
    /** 
     * ��ȡ��������û����� 
     *  
     * @param packName 
     * @param allProcList 
     * @return 
     */  
    private String getAppUser(String packName, List<ProcessInfo> allProcList) {  
        for (ProcessInfo processInfo : allProcList) {  
            if (processInfo.name.equals(packName)) {  
                return processInfo.user;  
            }  
        }  
        return null;  
    }  
  
    /** 
     * ����ps����õ������ݻ�ȡPID��User��name����Ϣ 
     *  
     * @param orgProcessList 
     * @return 
     */  
    private List<ProcessInfo> getProcessInfoList(List<String> orgProcessList) {  
        List<ProcessInfo> procInfoList = new ArrayList<ProcessInfo>();  
        for (int i = 1; i < orgProcessList.size(); i++) {  
            String processInfo = orgProcessList.get(i);  
            String[] proStr = processInfo.split(" ");  
            // USER PID PPID VSIZE RSS WCHAN PC NAME  
            // root 1 0 416 300 c00d4b28 0000cd5c S /init  
            List<String> orgInfo = new ArrayList<String>();  
            for (String str : proStr) {  
                if (!"".equals(str)) {  
                    orgInfo.add(str);  
                }  
            }  
            if (orgInfo.size() == 9) {  
                ProcessInfo pInfo = new ProcessInfo();  
                pInfo.user = orgInfo.get(0);  
                pInfo.pid = orgInfo.get(1);  
                pInfo.ppid = orgInfo.get(2);  
                pInfo.name = orgInfo.get(8);  
                procInfoList.add(pInfo);  
            }  
        }  
        return procInfoList;  
    }  
  
    /** 
     * ����PS����õ�������Ϣ 
     *  
     * @return 
     *          USER PID PPID VSIZE RSS WCHAN PC NAME 
     *          root 1 0 416 300 c00d4b28 0000cd5c S /init 
     */  
    private List<String> getAllProcess() {  
        List<String> orgProcList = new ArrayList<String>();  
        Process proc = null;  
        try {  
            proc = Runtime.getRuntime().exec("ps");  
            
            BufferedInputStream in = new BufferedInputStream(proc.getInputStream());
            BufferedReader inBr = new BufferedReader(new InputStreamReader(in));
            
            String lineStr;
            while ((lineStr = inBr.readLine()) != null) {
        		orgProcList.add(lineStr);  
            }
            
            if (proc.waitFor() != 0 && proc.exitValue() == 1) {  
                Log.e(TAG, "getAllProcess proc.waitFor() != 0");  
            }  
            inBr.close();
            in.close();
            
        } catch (Exception e) {  
            Log.e(TAG, "getAllProcess failed", e);  
        } finally {  
            try {  
                proc.destroy();  
            } catch (Exception e) {  
                Log.e(TAG, "getAllProcess failed", e);  
            }  
        }  
        return orgProcList;  
    }  
  
    class ProcessInfo {  
        public String user;  
        public String pid;  
        public String ppid;  
        public String name;  
  
        @Override  
        public String toString() {  
            String str = "user=" + user + " pid=" + pid + " ppid=" + ppid  
                    + " name=" + name;  
            return str;  
        }  
    }
    
}  
