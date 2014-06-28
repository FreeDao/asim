package com.view.asim.worker;

import com.view.asim.manager.SMSVerifyManager;

public class SmsCodeSendHandler implements BaseHandler {
    
    private final String mCellphone;
    private final CommonResultListener mListener;
                                                                                                                                                                                                                                                                          
    public SmsCodeSendHandler(String cellphone, CommonResultListener listener){
    	mCellphone = cellphone;
        mListener  = listener;
    }
                                                                                                                                                                                                                                                                          
    @Override
    public void execute() {
    	mListener.onResult(SMSVerifyManager.getInstance().sendSMSCode(mCellphone));
    }
}
