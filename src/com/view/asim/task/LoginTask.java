package com.view.asim.task;

import java.io.File;
import java.util.Collection;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.XMPPError;
import org.jivesoftware.smackx.packet.VCard;

import com.view.asim.comm.Constant;
import com.view.asim.activity.ActivitySupport;
import com.view.asim.activity.IActivitySupport;
import com.view.asim.activity.MainActivity;
import com.view.asim.manager.AUKeyManager;
import com.view.asim.manager.CallLogManager;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.MessageManager;
import com.view.asim.manager.NoticeManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.LoginConfig;
import com.view.asim.model.User;
import com.view.asim.util.FileUtil;
import com.view.asim.util.StringUtil;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import com.view.asim.R;

/**
 * 
 * 登录异步任务.
 * 
 * @author xuweinan
 */
public class LoginTask extends AsyncTask<String, Integer, Integer> {
	private final static String TAG = "LoginTask";
	
	private ProgressDialog pd;
	private ActivitySupport activity;
	private LoginConfig loginConfig;
	private User mUser = null;
	private AnimationDrawable loginAnim = null;

	public LoginTask(ActivitySupport as, LoginConfig loginConfig, AnimationDrawable animationDrawable) {
		this.activity = as;
		this.loginConfig = loginConfig;
		this.pd = as.getProgressDialog();
		this.loginAnim = animationDrawable;
	}

	public void initUser(User u) {
		this.mUser = u;
	}
	
	@Override
	protected void onPreExecute() {
		// 如果是注册后的自动登录，不再显示进度对话框
		if (mUser == null) {
			/*
			pd.setTitle("请稍等");
			pd.setMessage("正在登录...");
			pd.show();
			*/
			loginAnim.start();
		}
		super.onPreExecute();
	}

	@Override
	protected Integer doInBackground(String... params) {
		return login();
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
	}


	@Override
	protected void onPostExecute(Integer result) {
		loginAnim.stop();
		switch (result) {
		case Constant.SERVER_SUCCESS: // 登录成功

			activity.startGeneralService();
			
			Intent intent = new Intent();
			intent.setClass(activity, MainActivity.class);
			activity.saveLoginConfig(loginConfig);// 保存用户配置信息
			activity.startActivity(intent);
			activity.finish();
			
			break;
		case Constant.LOGIN_ERROR_ACCOUNT_PASS:// 账户或者密码错误
			Toast.makeText(
					activity,
					activity.getResources().getString(
							R.string.message_invalid_username_password),
					Toast.LENGTH_LONG).show();
			
			// 账户或密码错误，需要清空保存的信息，以便下次重新登录
			loginConfig.setUsername(null);
			loginConfig.setPassword(null);
			loginConfig.setOnline(false);
			activity.saveLoginConfig(loginConfig);
			
			loginAnim.setVisible(false, false);
			//activity.finish();

			break;
			
		case Constant.LOGIN_ERROR_DUPLICATED:// 重复登录
			Toast.makeText(
					activity,
					activity.getResources().getString(
							R.string.message_login_conflicted),
					Toast.LENGTH_LONG).show();
			
			loginConfig.setUsername(null);
			loginConfig.setPassword(null);
			loginConfig.setOnline(false);
			activity.saveLoginConfig(loginConfig);
			
			loginAnim.setVisible(false, false);

			break;			
		case Constant.SERVER_UNAVAILABLE:// 服务器连接失败
			Toast.makeText(
					activity,
					activity.getResources().getString(
							R.string.message_server_unavailable),
					Toast.LENGTH_LONG).show();
			
			activity.finish();
			break;
		case Constant.UNKNOWN_ERROR:// 未知异常
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

	// 登录
	private Integer login() {
		String username = loginConfig.getUsername();
		//String password = loginConfig.getPassword();
		try {
			XmppConnectionManager manager = XmppConnectionManager.getInstance(activity);
					//.getConnection();
			
			// 登录成功后，初始化联系人列表
			ContacterManager.init(manager, activity);

			/*
			if (mUser != null) {
				manager.connect();
			}
			else {
				manager.login(username, password); // 登录
			}
			*/
			manager.reconnectForcely(loginConfig);
			
			// 如果是第一次注册，登录时保存用户注册信息
			if (mUser != null) {
				
				/* FIXME:
				// 初始化密钥
				AUKeyManager keyMan = AUKeyManager.getInstance();
				keyMan.initKey();
				
				mUser.setPrivateKey(keyMan.encryptPrivateKey(password));
				mUser.setPublicKey(keyMan.encodePublicKey());
				*/
				
				VCard r = ContacterManager.saveUserVCard(manager.getConnection(), mUser);
				Log.d(TAG, "register new user succ: " + r);
			} else {
				// 正常登录，从服务器获取登录用户的信息
				mUser = ContacterManager.getUserByName(manager.getConnection(), username);
				Log.d(TAG, "login user succ: " + mUser);
			}
			
			ContacterManager.setUserMe(mUser);
			updateSecurityStatus();
			
	        NoticeManager.getInstance(activity, loginConfig).init();
	        MessageManager.getInstance(activity, loginConfig).init();
	        CallLogManager.getInstance(activity, loginConfig).init();
			
			loginConfig.setOnline(true);
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
					// not authorized: 帐号密码错误
					return Constant.LOGIN_ERROR_ACCOUNT_PASS;
				}else if (errorCode == 403) {
					// forbidden: 已登录，不允许重复登录
					return Constant.LOGIN_ERROR_DUPLICATED;
				} else {
					return Constant.SERVER_UNAVAILABLE;
				}
			} else {
				return Constant.UNKNOWN_ERROR;
			}
		}
	}
	
	private void updateSecurityStatus() {
		ContacterManager.userMe.setSecurity(AUKeyManager.getInstance().getAUKeyStatus());
		
		/*
		XMPPConnection conn = XmppConnectionManager.getInstance().getConnection();
		ContacterManager.saveUserVCard(conn, ContacterManager.userMe);
		Presence presence = new Presence(Presence.Type.available);
		presence.setStatus("update");
		
		Collection<RosterEntry> rosters = conn.getRoster()
				.getEntries();
		for (RosterEntry rosterEntry : rosters) {
			Log.d(TAG, "presence updated to " + rosterEntry.getUser());
			presence.setTo(rosterEntry.getUser());
			conn.sendPacket(presence);
		}
		*/

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
