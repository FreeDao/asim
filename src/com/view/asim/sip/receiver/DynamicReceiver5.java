
package com.view.asim.sip.receiver;

import android.annotation.TargetApi;
import android.content.Intent;

import com.view.asim.sip.service.SipService;

@TargetApi(5)
public class DynamicReceiver5 extends DynamicReceiver4 {

    public DynamicReceiver5(SipService aService) {
        super(aService);
    }

    @Override
    public boolean compatIsInitialStickyBroadcast(Intent it) {
        return isInitialStickyBroadcast();
    }
}
