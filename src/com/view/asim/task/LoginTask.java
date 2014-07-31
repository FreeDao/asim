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
 * ��¼�첽����.
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
		loginAnim.stop();
		switch (result) {
		case Constant.SERVER_SUCCESS: // ��¼�ɹ�

			activity.startGeneralService();
			
			Intent intent = new Intent();
			intent.setClass(activity, MainActivity.class);
			activity.saveLoginConfig(loginConfig);// �����û�������Ϣ
			activity.startActivity(intent);
			activity.finish();
			
			break;
		case Constant.LOGIN_ERROR_ACCOUNT_PASS:// �˻������������
			Toast.makeText(
					activity,
					activity.getResources().getString(
							R.string.message_invalid_username_password),
					Toast.LENGTH_LONG).show();
			
			// �˻������������Ҫ��ձ������Ϣ���Ա��´����µ�¼
			loginConfig.setUsername(null);
			loginConfig.setPassword(null);
			loginConfig.setOnline(false);
			activity.saveLoginConfig(loginConfig);
			
			loginAnim.setVisible(false, false);
			//activity.finish();

			break;
			
		case Constant.LOGIN_ERROR_DUPLICATED:// �ظ���¼
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
		case Constant.SERVER_UNAVAILABLE:// ����������ʧ��
			Toast.makeText(
					activity,
					activity.getResources().getString(
							R.string.message_server_unavailable),
					Toast.LENGTH_LONG).show();
			
			activity.finish();
			break;
		case Constant.UNKNOWN_ERROR:// δ֪�쳣
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

	// ��¼
	private Integer login() {
		String username = loginConfig.getUsername();
		//String password = loginConfig.getPassword();
		try {
			XmppConnectionManager manager = XmppConnectionManager.getInstance(activity);
					//.getConnection();
			
			// ��¼�ɹ��󣬳�ʼ����ϵ���б�
			ContacterManager.init(manager, activity);

			/*
			if (mUser != null) {
				manager.connect();
			}
			else {
				manager.login(username, password); // ��¼
			}
			*/
			manager.reconnectForcely(loginConfig);
			
			// ����ǵ�һ��ע�ᣬ��¼ʱ�����û�ע����Ϣ
			if (mUser != null) {
				
				/* FIXME:
				// ��ʼ����Կ
				AUKeyManager keyMan = AUKeyManager.getInstance();
				keyMan.initKey();
				
				mUser.setPrivateKey(keyMan.encryptPrivateKey(password));
				mUser.setPublicKey(keyMan.encodePublicKey());
				*/
				
				VCard r = ContacterManager.saveUserVCard(manager.getConnection(), mUser);
				Log.d(TAG, "register new user succ: " + r);
			} else {
				// ������¼���ӷ�������ȡ��¼�û�����Ϣ
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
					// not authorized: �ʺ��������
					return Constant.LOGIN_ERROR_ACCOUNT_PASS;
				}else if (errorCode == 403) {
					// forbidden: �ѵ�¼���������ظ���¼
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
