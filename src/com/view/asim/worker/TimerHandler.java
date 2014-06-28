package com.view.asim.worker;

public class TimerHandler implements BaseHandler {
    
    private final static int ONE_SECOND_DELAY = 1000;
    private final int mDelayTime;
    private final ExpiryTimerListener mListener;
                                                                                                                                                                                                                                                                          
    //通过构造函数传递所需的参数
    public TimerHandler(int seconds, ExpiryTimerListener listener){
        mDelayTime = seconds;
        mListener  = listener;
    }
                                                                                                                                                                                                                                                                          
    @Override
    public void execute() {
                                                                                                                                                                                                                                                                              
        try {
        	for (int i = mDelayTime; i > 0; i--) {
                Thread.sleep(ONE_SECOND_DELAY);
                mListener.onTick(i - 1);
        	}
            mListener.onEnd();
        }
        catch (InterruptedException e) {         
            e.printStackTrace();         
        }
    }
}
