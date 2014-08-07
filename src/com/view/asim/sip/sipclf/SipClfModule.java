
package com.view.asim.sip.sipclf;

import android.content.Context;

import com.view.asim.sip.api.SipProfile;
import com.view.asim.sip.stack.PjSipService.PjsipModule;
import com.view.asim.utils.Log;

import org.pjsip.pjsua.pjsua;

public class SipClfModule implements PjsipModule {

    private static final String THIS_FILE = "SipClfModule";
    private boolean enableModule = false;

    public SipClfModule() {
    }

    @Override
    public void setContext(Context ctxt) {
        
    }

    @Override
    public void onBeforeStartPjsip() {
        if(enableModule ) {
            int status = pjsua.sipclf_mod_init();
            Log.d(THIS_FILE, "SipClfModule module added with status " + status);
        }
    }

    @Override
    public void onBeforeAccountStartRegistration(int pjId, SipProfile acc) {
    }

}
