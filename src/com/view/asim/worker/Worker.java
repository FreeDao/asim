package com.view.asim.worker;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.view.asim.util.DateUtil;

import android.util.Log;

/**
 * Worker-Handler 流水命令任务框架
 * @author xuweinan
 *
 */
public class Worker {
	
	private String name = null;
    //控制线程执行/退出的标志变量
    private volatile boolean mIsRunning = true;
                                                                                                                                                     
    //锁和条件变量
    private Lock mLock = new ReentrantLock();
    private Condition mCondition = mLock.newCondition();
                                                                                                                                                     
    //任务列表
    private Queue<BaseHandler> mHandlerQueue = new LinkedList<BaseHandler>();
                                                                                                                                                     
    //初始化函数，开启任务等待线程
    public void initilize(String name) {
    	this.name = name;
        new Thread(new WorkRunnable()).start();
    	Log.d(name, "worker start");

    }
                                                                                                                                                     
    //销毁函数，关闭任务等待线程
    public void destroy() {
    	Log.d(name, "worker destroy");
                                                                                                                                    
        //线程退出命令
        mIsRunning = false;
                                                                                                                                                         
        //添加一个任务，唤醒mCondition.await
        addHandler(new DummyHandler());    
    }
                                                                                                                                                     
    //添加一个新任务
    public void addHandler( BaseHandler handler ) {
        Log.d(name, "add handler " + handler);                                                                                                                             
        mLock.lock();
        mHandlerQueue.add(handler);
        mCondition.signal();
        mLock.unlock();
    }
                                                                                                                                                     
    //获取下一个任务，如果任务列表为空，则阻塞
    private BaseHandler getNextHandler() {
                                                                                                                                                         
        mLock.lock();
        try {
                                                                                                                                                             
            //如果任务队列为空，则阻塞等待
            while( mHandlerQueue.isEmpty() ) {            
                mCondition.await();
            }
                                                                                                                                                             
            //返回队列首任务，并从队列删除掉
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
                                                                                                                                                     
    //任务等待/执行线程
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
