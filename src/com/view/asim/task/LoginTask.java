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
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.MessageManager;
import com.view.asim.manager.NoticeManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.LoginConfig;
import com.view.asim.model.User;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.view.asim.R;

/**
 * 
 * ��¼�첽����.
 * 
 * @author allen
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
		// �����ע�����Զ���¼��������ʾ���ȶԻ���
		if (mUser == null) {
			/*
			pd.setTitle("���Ե�");
			pd.setMessage("���ڵ�¼...");
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
		//pd.dismiss();
		loginAnim.stop();
		switch (result) {
		case Constant.SERVER_SUCCESS: // ��¼�ɹ�
			
			Intent intent = new Intent();
			intent.setClass(activity, MainActivity.class);
			activity.saveLoginConfig(loginConfig);// �����û�������Ϣ
			activity.stopService();
			activity.startService();
			activity.startActivity(intent);
			activity.finish();
			
			break;
		case Constant.LOGIN_ERROR_ACCOUNT_PASS:// �˻������������
			Toast.makeText(
					activity,
					activity.getResources().getString(
							R.string.message_invalid_username_password),
					Toast.LENGTH_SHORT).show();
			break;
		case Constant.SERVER_UNAVAILABLE:// ����������ʧ��
			Toast.makeText(
					activity,
					activity.getResources().getString(
							R.string.message_server_unavailable),
					Toast.LENGTH_SHORT).show();
			break;
		case Constant.UNKNOWN_ERROR:// δ֪�쳣
			Toast.makeText(
					activity,
					activity.getResources().getString(
							R.string.unrecoverable_error), Toast.LENGTH_SHORT)
					.show();
			break;
		}
		super.onPostExecute(result);
	}

	// ��¼
	private Integer login() {
		String username = loginConfig.getUsername();
		String password = loginConfig.getPassword();
		try {
			XMPPConnection connection = XmppConnectionManager.getInstance()
					.getConnection();
			if (mUser == null) {
				connection.connect();
			}
			connection.login(username, password); // ��¼
			
			// ����ǵ�һ��ע�ᣬ��¼ʱ�����û�ע����Ϣ
			if (mUser != null) {
				// ��ʼ����Կ
				AUKeyManager keyMan = AUKeyManager.getInstance();
				keyMan.initKey();
				
				mUser.setPrivateKey(keyMan.encryptPrivateKey(password));
				mUser.setPublicKey(keyMan.encodePublicKey());
				
				VCard r = ContacterManager.saveUserVCard(connection, mUser);
				Log.d(TAG, "register new user succ: " + r);
			} else {
				// ������¼���ӷ�������ȡ��¼�û�����Ϣ
				mUser = ContacterManager.getUserByName(connection, username);
				Log.d(TAG, "login user succ: " + mUser);
			}
			
			// ��¼�ɹ��󣬳�ʼ����ϵ���б�
			ContacterManager.init(connection, activity, mUser);
			loginConfig.setOnline(true);
			initCacheFolder(mUser);
			
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
					return Constant.LOGIN_ERROR_ACCOUNT_PASS;
				}else if (errorCode == 403) {
					return Constant.LOGIN_ERROR_ACCOUNT_PASS;
				} else {
					return Constant.SERVER_UNAVAILABLE;
				}
			} else {
				return Constant.UNKNOWN_ERROR;
			}
		}
	}
	
	private void initCacheFolder(User u) {
		File videoFolder = new File(Constant.SDCARD_ROOT_PATH + Constant.VIDEO_PATH + "/" + u.getName() + "/");
		File imgFolder = new File(Constant.SDCARD_ROOT_PATH + Constant.IMAGE_PATH + "/" + u.getName() + "/");
		File audioFolder = new File(Constant.SDCARD_ROOT_PATH + Constant.AUDIO_PATH + "/" + u.getName() + "/");
		File fileFolder = new File(Constant.SDCARD_ROOT_PATH + Constant.FILE_PATH + "/" + u.getName() + "/");
		File cacheFolder = new File(Constant.SDCARD_ROOT_PATH + Constant.CACHE_PATH + "/" + u.getName() + "/");
		
		if (!videoFolder.exists()) {
			videoFolder.mkdirs();
		}
		
		if (!imgFolder.exists()) {
			imgFolder.mkdirs();
		}
		
		if (!audioFolder.exists()) {
			audioFolder.mkdirs();
		}
		
		if (!fileFolder.exists()) {
			fileFolder.mkdirs();
		}
		
		if (!cacheFolder.exists()) {
			cacheFolder.mkdirs();
		}
	}
}
