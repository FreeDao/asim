package com.view.asim.view;

import com.view.asim.worker.MessageRecvResultListener;

import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;

public class EndAnimationDrawable extends AnimationDrawable {
    Handler finishHandler;      // �жϽ�����Handler
    private final OnAnimationChangeListener mListener;
    
    public EndAnimationDrawable(AnimationDrawable ad,  OnAnimationChangeListener listener) {
    	mListener = listener;
        // ������Լ���ÿһ֡�ӽ�ȥ
        for (int i = 0; i < ad.getNumberOfFrames(); i++) {
            this.addFrame(ad.getFrame(i), ad.getDuration(i));
        }
    }
    @Override
    public void start() {
        super.start();
        /**
         * �����ø����start()
         * Ȼ�������̣߳�������onAnimationEnd()
         */
        finishHandler = new Handler();
        finishHandler.postDelayed(
            new Runnable() {
                public void run() {
                	mListener.onAnimationEnd();
                }
            }, getTotalDuration());
    }
    /**
     * ���������ö����ĳ���ʱ�䣨֮�����onAnimationEnd()��
     */
    public int getTotalDuration() {
        int durationTime = 0;
        for (int i = 0; i < this.getNumberOfFrames(); i++) {
            durationTime += this.getDuration(i);
        }
        return durationTime;
    }
    
}