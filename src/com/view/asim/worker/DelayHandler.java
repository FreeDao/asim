package com.view.asim.worker;

public class DelayHandler implements BaseHandler {
    
    private final long mDelayTime;
    private final TimeArrivedListener mListener;
                                                                                                                                                                                                                                                                          
    //�ص�������֪ͨ�����ʱʱ�䵽
    public interface TimeArrivedListener {
        public void onDelayTimeArrived();
    }
                                                                                                                                                                                                                                                                          
    //ͨ�����캯����������Ĳ���
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
