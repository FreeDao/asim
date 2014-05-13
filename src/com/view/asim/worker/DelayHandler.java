package com.view.asim.worker;

public class DelayHandler implements BaseHandler {
    
    private final long mDelayTime;
    private final TimeArrivedListener mListener;
                                                                                                                                                                                                                                                                          
    //回调函数，通知外界延时时间到
    public interface TimeArrivedListener {
        public void onDelayTimeArrived();
    }
                                                                                                                                                                                                                                                                          
    //通过构造函数传递所需的参数
    public DelayHandler( long delayTime, TimeArrivedListener listener){
                                                                                                                                                                                                                                                                              
        mDelayTime = delayTime;
        mListener  = listener;
    }
                                                                                                                                                                                                                                                                          
    @Override
    public void execute() {
                                                                                                                                                                                                                                                                              
        try {        
            Thread.sleep(mDelayTime);
            mListener.onDelayTimeArrived();      
        }
        catch (InterruptedException e) {         
            e.printStackTrace();         
        }
    }
}
