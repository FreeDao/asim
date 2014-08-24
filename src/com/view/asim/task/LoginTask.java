package com.view.asim.task;


import java.io.File;
import java.util.Collection;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smackx.packet.VCard;

import com.view.asim.activity.ActivitySupport;
import com.view.asim.activity.IActivitySupport;
import com.view.asim.activity.MainActivity;
import com.view.asim.comm.ApplicationContext;
import com.view.asim.comm.Constant;
import com.view.asim.db.DataBaseHelper;
import com.view.asim.manager.AUKeyManager;
import com.view.asim.manager.AppConfigManager;
import com.view.asim.manager.CallLogManager;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.MessageManager;
import com.view.asim.manager.NoticeManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.LoginConfig;
import com.view.asim.model.User;
import com.view.asim.utils.FaceConversionUtil;
import com.view.asim.utils.FileUtil;
import com.view.asim.utils.StringUtil;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteFullException;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import com.view.asim.R;

/**
 * 
 * 锟斤拷录锟届步锟斤拷锟斤拷.
 * 
 * @author xuweinan
 */
public class LoginTask extends AsyncTask<String, Integer, Integer> {
	private final static String TAG = "LoginTask";
	
	private ActivitySupport activity;
	private User mUser = null;
	private AnimationDrawable loginAnim = null;

	public LoginTask(ActivitySupport activity, AnimationDrawable animationDrawable) {
		this.activity = activity;
		this.loginAnim = animationDrawable;
	}

	public void initUser(User u) {
		this.mUser = u;
	}
	
	@Override
	protected void onPreExecute() {
		// 锟斤拷锟斤拷锟阶�锟斤拷锟斤拷锟皆讹拷锟斤拷录锟斤拷锟斤拷锟斤拷锟斤拷示锟斤拷锟饺对伙拷锟斤拷
		if (mUser == null) {
			loginAnim.start();
		}
		
		super.onPreExecute();
	}

	@Override
	protected Integer doInBackground(String... params) {
		if (mUser == null) {
			resolv();
		}
		return login();
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
	}


	@Override
	protected void onPostExecute(Integer result) {
		loginAnim.stop();
		switch (result) {
		case Constant.SERVER_SUCCESS: // 锟斤拷录锟缴癸拷

			activity.startGeneralService();
			
//			Intent intent = new Intent();
//			intent.setClass(activity, MainActivity.class);
			AppConfigManager.getInstance().saveLoginConfig();// 锟斤拷锟斤拷锟矫伙拷锟斤拷锟斤拷锟斤拷息
//			activity.startActivity(intent);
//			activity.finish();
			if(activity.isLoginActivity()||activity.isSignUpActivity()){
				activity.setResult(activity.RESULT_OK);
				activity.finish();
			}else{
				activity.changeLayout();
			}
			break;
		case Constant.LOGIN_ERROR_ACCOUNT_PASS:// 锟剿伙拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟�
			Toast.makeText(
					activity,
					activity.getResources().getString(
							R.string.message_invalid_username_password),
					Toast.LENGTH_LONG).show();
			
			// 锟剿伙拷锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷锟揭�锟斤拷毡锟斤拷锟斤拷锟斤拷息锟斤拷锟皆憋拷锟铰达拷锟斤拷锟铰碉拷录
			AppConfigManager.getInstance().setUsername(null);
			AppConfigManager.getInstance().setPassword(null);
			AppConfigManager.getInstance().setOnline(false);
			AppConfigManager.getInstance().saveLoginConfig();
			
			loginAnim.setVisible(false, false);
			//activity.finish();

			break;
			
		case Constant.LOGIN_ERROR_DUPLICATED:// 锟截革拷锟斤拷录
			Toast.makeText(
					activity,
					activity.getResources().getString(
							R.string.message_login_conflicted),
					Toast.LENGTH_LONG).show();
			
			AppConfigManager.getInstance().setUsername(null);
			AppConfigManager.getInstance().setPassword(null);
			AppConfigManager.getInstance().setOnline(false);
			AppConfigManager.getInstance().saveLoginConfig();
			
			loginAnim.setVisible(false, false);

			break;			
		case Constant.SERVER_UNAVAILABLE:// 锟斤拷锟斤拷锟斤拷锟斤拷锟斤拷失锟斤拷
			Toast.makeText(
					activity,
					activity.getResources().getString(
							R.string.message_server_unavailable),
					Toast.LENGTH_LONG).show();
			
			activity.finish();
			break;
			
		case Constant.DISK_FULL_ERROR:
			Toast.makeText(
					activity,
					activity.getResources().getString(
							R.string.sdcard_no_enough_space), Toast.LENGTH_LONG)
							.show();
			activity.finish();
			break;
			
		case Constant.UNKNOWN_ERROR:
			Toast.makeText(
					activity,
					activity.getResources().getString(
							R.string.unrecoverable_error), Toast.LENGTH_LONG)
							.show();
			activity.finish();
			break;
		}
		super.onPostExecute(result);
	}

	private void resolv() {
		AppConfigManager.getInstance().resolvServer();
	}
	
	// 锟斤拷录
	private Integer login() {
		String username = AppConfigManager.getInstance().getUsername();
		Log.i("user", "login :"+username);

		try {
			XmppConnectionManager manager = XmppConnectionManager.getInstance();
			
			// 锟斤拷录锟缴癸拷锟襟，筹拷始锟斤拷锟斤拷系锟斤拷锟叫憋拷
			ContacterManager.init(manager);
			
			manager.reconnectForcely();
			
			// 锟斤拷锟斤拷堑锟揭伙拷锟阶�锟结，锟斤拷录时锟斤拷锟斤拷锟矫伙拷注锟斤拷锟斤拷息
			if (mUser != null) {
				
				/* FIXME:
				// 锟斤拷始锟斤拷锟斤拷钥
				AUKeyManager keyMan = AUKeyManager.getInstance();
				keyMan.initKey();
				
				mUser.setPrivateKey(keyMan.encryptPrivateKey(password));
				mUser.setPublicKey(keyMan.encodePublicKey());
				*/

				mUser.setSecurity(AUKeyManager.getInstance().getAUKeyStatus());
				ContacterManager.saveUserVCard(manager.getConnection(), mUser);
				Log.d(TAG, "register new user succ: " + mUser);
			} else {
				// 锟斤拷锟斤拷锟斤拷录锟斤拷锟接凤拷锟斤拷锟斤拷锟斤拷取锟斤拷录锟矫伙拷锟斤拷锟斤拷息

				VCard vcard = ContacterManager.getUserVCard(manager.getConnection(), username);
				mUser = ContacterManager.getUserByNameAndVCard(username, vcard);
				mUser.setSecurity(AUKeyManager.getInstance().getAUKeyStatus());
				XMPPConnection conn = XmppConnectionManager.getInstance().getConnection();
				ContacterManager.saveUserVCard(conn, mUser, vcard);
				Log.d(TAG, "login user succ: " + mUser);
			}
			
			ContacterManager.setUserMe(mUser);
			
			DataBaseHelper helper = DataBaseHelper.getInstance(username, Constant.DB_VERSION);

			NoticeManager nm = NoticeManager.getInstance();
			MessageManager mm = MessageManager.getInstance();
			CallLogManager clm = CallLogManager.getInstance();

	        nm.init(helper);
	        mm.init(helper);
	        clm.init(helper);
	        
	        nm.clearAllMessageNotify();
	        FaceConversionUtil.getInstace().getFileText();

	        //AppConfigManager.getInstance().setOnline(true);
			initUserCacheFolder(mUser);
			
			return Constant.SERVER_SUCCESS;
		} catch (Exception xee) {
			xee.printStackTrace();
			if (xee instanceof XMPPException) {
				XMPPException xe = (XMPPException) xee;
				final XMPPError error = xe.getXMPPError();
				int errorCode = 0;
				if (error != null) {
					errorCode = error.getCode();
				}
				xe.printStackTrace();
				if (errorCode == 401) {
					// not authorized: 锟绞猴拷锟斤拷锟斤拷锟斤拷锟�
					return Constant.LOGIN_ERROR_ACCOUNT_PASS;
				}else if (errorCode == 403) {
					// forbidden: 锟窖碉拷录锟斤拷锟斤拷锟斤拷锟斤拷锟截革拷锟斤拷录
					return Constant.LOGIN_ERROR_DUPLICATED;
				} else {
					return Constant.SERVER_UNAVAILABLE;
				}
			} else if (xee instanceof SQLiteFullException){
				return Constant.DISK_FULL_ERROR;
			}
			else {
				return Constant.UNKNOWN_ERROR;
			}
		}
	}
		
	private void initUserCacheFolder(User u) {
		
		File videoFolder = new File(FileUtil.getUserVideoPath());
		File imgFolder = new File(FileUtil.getUserImagePath());
		File audioFolder = new File(FileUtil.getUserAudioPath());
		File fileFolder = new File(FileUtil.getUserFilePath());
		File tempFolder = new File(FileUtil.getUserTempPath());

		FileUtil.createDir(videoFolder);
		FileUtil.createDir(imgFolder);
		FileUtil.createDir(audioFolder);
		FileUtil.createDir(fileFolder);
		FileUtil.createDir(tempFolder);
		
	}
}
