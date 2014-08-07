package com.view.asim.task;


import java.util.Collection;

import org.jivesoftware.smack.PacketCollector;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.AndFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketIDFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.IQ;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Registration;
import org.jivesoftware.smack.packet.XMPPError;

import com.view.asim.activity.ActivitySupport;
import com.view.asim.activity.IActivitySupport;
import com.view.asim.activity.MainActivity;
import com.view.asim.activity.SignUpActivity;
import com.view.asim.activity.SplashActivity;
import com.view.asim.comm.Constant;
import com.view.asim.manager.AUKeyManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.LoginConfig;
import com.view.asim.model.User;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.view.asim.R;

/**
 * 
 * ע���첽����.
 * 
 * @author xuweinan
 */
public class SignUpTask extends AsyncTask<String, Integer, Integer> {
	private final static String TAG = "SignUpTask";
	private ProgressDialog pd;
	private ActivitySupport activity;
	private LoginConfig loginConfig;
	private User mUser = new User();

	public SignUpTask(ActivitySupport as, LoginConfig loginConfig) {
		this.loginConfig = loginConfig;
		this.pd = as.getProgressDialog();
		this.activity = as;
	}

	public void initUser(User u) {
		this.mUser = u;
	}
	
	@Override
	protected void onPreExecute() {
		pd.setTitle("���Ե�");
		pd.setMessage("����ע��...");
		pd.show();
		super.onPreExecute();
	}

	@Override
	protected Integer doInBackground(String... params) {
		return signUp();
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
	}

	@Override
	protected void onPostExecute(Integer result) {
		pd.dismiss();
		
		SignUpActivity signup;
		switch (result) {
		case Constant.SERVER_SUCCESS: // ע��ɹ�
			activity.saveLoginConfig(loginConfig);// �����û�������Ϣ

			signup = (SignUpActivity)activity;
			signup.notifySignUpSucc();
			break;
		case Constant.SIGNUP_ERROR_ACCOUNT_PASS:// �˻������������
			/*
			Toast.makeText(
					activity,
					activity.getResources().getString(
							R.string.message_existed_username),
					Toast.LENGTH_LONG).show();
					*/
			signup = (SignUpActivity)activity;
			signup.notifyUserIsExist();
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

	// ע��
	private Integer signUp() {
		String username = loginConfig.getUsername();
		String password = loginConfig.getPassword();
		
		try {
			// ע��
			XmppConnectionManager manager = XmppConnectionManager.getInstance(activity);
			manager.connectOnly(loginConfig);
			
			//XMPPConnection connection = XmppConnectionManager.getInstance()
					//.getConnection();
			//connection.connect();
			loginConfig.setOnline(true);
			
	        Registration reg = new Registration();  
	        reg.setType(IQ.Type.SET);  
	        reg.setTo(manager.getConnection().getServiceName());  
	        reg.setUsername(username);  
	        reg.setPassword(password);  
	        reg.addAttribute("name", mUser.getNickName());

	        PacketFilter filter = new AndFilter(new PacketIDFilter(  
	                reg.getPacketID()), new PacketTypeFilter(IQ.class));  
	        PacketCollector collector = manager.getConnection().createPacketCollector(filter);  
	        manager.getConnection().sendPacket(reg);  
	        IQ result = (IQ) collector.nextResult(SmackConfiguration.getPacketReplyTimeout());  
	        // Stop queuing resultsֹͣ����results���Ƿ�ɹ��Ľ����  
	        collector.cancel();  
	        
	        if (result == null) {  
	        	Log.d(TAG, "No response from server.");  
	            return Constant.SERVER_UNAVAILABLE; 
	        } else if (result.getType() == IQ.Type.ERROR) {
	            if (result.getError().toString().equalsIgnoreCase("conflict(409)")) {  
	            	Log.d(TAG, "IQ.Type.ERROR: "  
	                        + result.getError().toString());  
	                return Constant.SIGNUP_ERROR_ACCOUNT_PASS;  
	            } else {  
	            	Log.d(TAG, "IQ.Type.ERROR: "  
	                        + result.getError().toString());  
	                return Constant.UNKNOWN_ERROR;
	            }  
	        } else if (result.getType() == IQ.Type.RESULT) {  
	        	Log.d(TAG, "register success.");
	        }
	        
	        return Constant.SERVER_SUCCESS;

		} catch (Exception xee) {
			if (xee instanceof XMPPException) {
				XMPPException xe = (XMPPException) xee;
				final XMPPError error = xe.getXMPPError();
				int errorCode = 0;
				if (error != null) {
					errorCode = error.getCode();
				}
				if (errorCode == 401) {
					return Constant.SIGNUP_ERROR_ACCOUNT_PASS;
				}else if (errorCode == 403) {
					return Constant.SIGNUP_ERROR_ACCOUNT_PASS;
				} else {
					return Constant.SERVER_UNAVAILABLE;
				}
			} else {
				return Constant.UNKNOWN_ERROR;
			}
		}
	}
}
