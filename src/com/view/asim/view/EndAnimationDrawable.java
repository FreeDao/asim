package com.view.asim.view;

import com.view.asim.worker.MessageRecvResultListener;

import android.graphics.drawable.AnimationDrawable;
import android.os.Handler;

public class EndAnimationDrawable extends AnimationDrawable {
    Handler finishHandler;      // 判断结束的Handler
    private final OnAnimationChangeListener mListener;
    
    public EndAnimationDrawable(AnimationDrawable ad,  OnAnimationChangeListener listener) {
    	mListener = listener;
        // 这里得自己把每一帧加进去
        for (int i = 0; i < ad.getNumberOfFrames(); i++) {
            this.addFrame(ad.getFrame(i), ad.getDuration(i));
        }
    }
    @Override
    public void start() {
        super.start();
        /**
         * 首先用父类的start()
         * 然后启动线程，来调用onAnimationEnd()
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
     * 这个方法获得动画的持续时间（之后调用onAnimationEnd()）
     */
    public int getTotalDuration() {
        int durationTime = 0;
        for (int i = 0; i < this.getNumberOfFrames(); i++) {
            durationTime += this.getDuration(i);
        }
        return durationTime;
    }
    
}