
package com.view.asim.sip.earlylock;

import android.content.Context;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

import com.view.asim.sip.api.SipProfile;
import com.view.asim.sip.stack.PjSipService.PjsipModule;
import com.view.asim.utils.Log;

import org.pjsip.pjsua.EarlyLockCallback;
import org.pjsip.pjsua.pjsua;

public class EarlyLockModule implements PjsipModule {
    private static final String THIS_FILE = "EarlyLockModule";

    private EarlyLocker locker;
    private PowerManager pm;

    /*
     * (non-Javadoc)
     * @see
     * com.csipsimple.pjsip.PjSipService.PjsipModule#setContext(android.content
     * .Context)
     */
    @Override
    public void setContext(Context ctxt) {
        pm = (PowerManager) ctxt.getSystemService(
                Context.POWER_SERVICE);
    }

    /*
     * (non-Javadoc)
     * @see com.csipsimple.pjsip.PjSipService.PjsipModule#onBeforeStartPjsip()
     */
    @Override
    public void onBeforeStartPjsip() {
        pjsua.mod_earlylock_init();
        locker = new EarlyLocker();
        pjsua.mod_earlylock_set_callback(locker);
    }

    /*
     * (non-Javadoc)
     * @see com.csipsimple.pjsip.PjSipService.PjsipModule#
     * onBeforeAccountStartRegistration(int, com.csipsimple.api.SipProfile)
     */
    @Override
    public void onBeforeAccountStartRegistration(int pjId, SipProfile acc) {
        // Nothing to do here
    }

    private class WorkLocker extends Thread {
        private final long lockTime;
        private WakeLock wl;

        WorkLocker(long time) {
            lockTime = time;
            wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "com.csipsimple.earlylock");
            wl.acquire();
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {
            try {
                Log.d(THIS_FILE, "We entered a partial early lock");
                sleep(lockTime);
            } catch (InterruptedException e) {
                Log.e(THIS_FILE, "Unable to lock");
            }
            wl.release();
        }
    }

    private class EarlyLocker extends EarlyLockCallback {
        public void on_create_early_lock() {
            WorkLocker wl = new WorkLocker(2000);
            wl.start();
        }
    }
}
