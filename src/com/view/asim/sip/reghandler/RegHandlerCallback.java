
package com.view.asim.sip.reghandler;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.SparseIntArray;

import com.view.asim.sip.stack.PjSipService;
import com.view.asim.utils.Log;

import org.pjsip.pjsua.MobileRegHandlerCallback;
import org.pjsip.pjsua.pj_str_t;
import org.pjsip.pjsua.pjsua;

public class RegHandlerCallback extends MobileRegHandlerCallback {
    private static final String THIS_FILE = "RegHandlerReceiver";
    private static final String REG_URI_PREFIX = "reg_uri_";
    private static final String REG_EXPIRES_PREFIX = "reg_expires_";
    private SharedPreferences prefs_db;
    
    private SparseIntArray accountCleanRegisters = new SparseIntArray();
    private pj_str_t EMPTY_STR = pjsua.pj_str_copy("");
    private Context mCtxt;
    
    public RegHandlerCallback(Context ctxt) {
        mCtxt = ctxt;
        prefs_db = ctxt.getSharedPreferences("reg_handler_db", Context.MODE_PRIVATE);
    }
    
    public void set_account_cleaning_state(int acc_id, int active) {
        accountCleanRegisters.put(acc_id, active);
    }
    
    @Override
    public pj_str_t on_restore_contact(int acc_id) {
        int active = accountCleanRegisters.get(acc_id, 0);
        if(active == 0) {
            return EMPTY_STR;
        }
        long db_acc_id = PjSipService.getAccountIdForPjsipId(mCtxt, acc_id);
        String key_expires = REG_EXPIRES_PREFIX + Long.toString(db_acc_id);
        String key_uri = REG_URI_PREFIX + Long.toString(db_acc_id);
        int expires = prefs_db.getInt(key_expires, 0);
        int now = (int) Math.ceil(System.currentTimeMillis() / 1000);
        if(expires >= now) {
            String ret = prefs_db.getString(key_uri, "");
            Log.d(THIS_FILE, "We restore " + ret);
            return pjsua.pj_str_copy(ret);
        }
        return EMPTY_STR;
    }
    
    @Override
    public void on_save_contact(int acc_id, pj_str_t contact, int expires) {
        long db_acc_id = PjSipService.getAccountIdForPjsipId(mCtxt, acc_id);
        String key_expires = REG_EXPIRES_PREFIX + Long.toString(db_acc_id);
        String key_uri = REG_URI_PREFIX + Long.toString(db_acc_id);
        Editor edt = prefs_db.edit();
        edt.putString(key_uri, PjSipService.pjStrToString(contact));
        int now = (int) Math.ceil(System.currentTimeMillis() / 1000);
        edt.putInt(key_expires, now + expires);
        // TODO : have this asynchronous
        edt.commit();
    }

}
