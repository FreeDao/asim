package com.view.asim.worker;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.view.asim.util.DateUtil;

import android.util.Log;

/**
 * Worker-Handler ��ˮ����������
 * @author xuweinan
 *
 */
public class Worker {
	
	private String name = null;
    //�����߳�ִ��/�˳��ı�־����
    private volatile boolean mIsRunning = true;
                                                                                                                                                     
    //������������
    private Lock mLock = new ReentrantLock();
    private Condition mCondition = mLock.newCondition();
                                                                                                                                                     
    //�����б�
    private Queue<BaseHandler> mHandlerQueue = new LinkedList<BaseHandler>();
                                                                                                                                                     
    //��ʼ����������������ȴ��߳�
    public void initilize(String name) {
    	this.name = name;
        new Thread(new WorkRunnable()).start();
    	Log.d(name, "worker start");

    }
                                                                                                                                                     
    //���ٺ������ر�����ȴ��߳�
    public void destroy() {
    	Log.d(name, "worker destroy");
                                                                                                                                    
        //�߳��˳�����
        mIsRunning = false;
                                                                                                                                                         
        //���һ�����񣬻���mCondition.await
        addHandler(new DummyHandler());    
    }
                                                                                                                                                     
    //���һ��������
    public void addHandler( BaseHandler handler ) {
        Log.d(name, "add handler " + handler);                                                                                                                             
        mLock.lock();
        mHandlerQueue.add(handler);
        mCondition.signal();
        mLock.unlock();
    }
                                                                                                                                                     
    //��ȡ��һ��������������б�Ϊ�գ�������
    private BaseHandler getNextHandler() {
                                                                                                                                                         
        mLock.lock();
        try {
                                                                                                                                                             
            //����������Ϊ�գ��������ȴ�
            while( mHandlerQueue.isEmpty() ) {            
                mCondition.await();
            }
                                                                                                                                                             
            //���ض��������񣬲��Ӷ���ɾ����
            return mHandlerQueue.poll();      
        }
        catch (InterruptedException e) {
            e.printStackTrace();         
        }
        finally {
            mLock.unlock();
        }    
                                                                                                                                                         
        return null;
    }
                                                                                                                                                     
    //����ȴ�/ִ���߳�
    private class WorkRunnable implements Runnable {
                                                                                                                                                
        @Override
        public void run() {
                                                                                                                                                             
            while(mIsRunning) {          
                BaseHandler handler = getNextHandler();
                Log.d(name, "run handler " + handler + " start on " + DateUtil.getCurDateStr());
                handler.execute();       
                Log.d(name, "run handler " + handler + " end on " + DateUtil.getCurDateStr());
            }
                                                                                                                                                             
            Log.d(name, "WorkRunnable run exit ! ");
        }
                                                                                                                                                         
    }
}
