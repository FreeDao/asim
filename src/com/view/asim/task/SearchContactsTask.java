package com.view.asim.task;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.XMPPError;

import com.view.asim.activity.ActivitySupport;
import com.view.asim.activity.IActivitySupport;
import com.view.asim.activity.MainActivity;
import com.view.asim.activity.im.AddUserContactsActivity;
import com.view.asim.activity.im.UserInfoActivity;
import com.view.asim.comm.ApplicationContext;
import com.view.asim.comm.Constant;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.LoginConfig;
import com.view.asim.model.User;
import com.view.asim.utils.StringUtil;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.view.asim.R;

/**
 * 
 * 搜索通讯录密友异步任务.
 * 
 * @author  xuweinan
 */
public class SearchContactsTask extends AsyncTask<String, Integer, Integer> {
	private final static String TAG = "SearchContactsTask";
    
	private ProgressDialog pd;
	private ActivitySupport activity;
	private String name = null;
	private User user = null;
	private List<User> contactUsers = new ArrayList<User>();
	
	public SearchContactsTask(ActivitySupport as) {
		this.activity = as;
		this.pd = as.getProgressDialog();
	}
	
	public void stopProgress() {
		pd.dismiss();
	}

	@Override
	protected void onPreExecute() {
		// 如果是注册后的自动登录，不再显示进度对话框
		pd.setTitle(ApplicationContext.get().getString(R.string.please_wait));
		pd.setMessage(ApplicationContext.get().getString(R.string.searching_contacts));
		pd.setCancelable(true);
		pd.setCanceledOnTouchOutside(false);
		pd.show();
		super.onPreExecute();
	}

	@Override
	protected Integer doInBackground(String... params) {
		ContacterManager.initPhoneContacts();
		return search();
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
		pd.setMessage(ApplicationContext.get().getString(R.string.searching_contacts)+" (" + values[0] + "%)");
	}

	@Override
	protected void onPostExecute(Integer result) {
		pd.dismiss();
		switch (result) {
		case Constant.SERVER_SUCCESS: // 搜索成功
			((AddUserContactsActivity) activity).setContactUsers(contactUsers);
			((AddUserContactsActivity) activity).refresh();
			
			break;
		case Constant.SERVER_UNAVAILABLE:// 服务器连接失败
			Toast.makeText(
					activity,
					activity.getResources().getString(
							R.string.message_server_unavailable),
					Toast.LENGTH_SHORT).show();
			break;
		case Constant.UNKNOWN_ERROR:// 未知异常
			Toast.makeText(
					activity,
					activity.getResources().getString(
							R.string.unrecoverable_error), Toast.LENGTH_SHORT)
					.show();
			break;
		case Constant.NONE_RESULTS:// 不存在
			new AlertDialog.Builder(activity)
			.setMessage(ApplicationContext.get().getString(R.string.searched_nothing))
			.setTitle(ApplicationContext.get().getString(R.string.search_result))
			.setNeutralButton(ApplicationContext.get().getString(R.string.ok), 
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog,
								int which) {
							dialog.dismiss();

						}
					})
					.show();
			break;
		}
		super.onPostExecute(result);
	}

	// 登录
	private Integer search() {
		List<User> resultsByCellphone = null;
		int total = ContacterManager.phoneContacters.size();
		int cur = 0;

		try {

			for (String key : ContacterManager.phoneContacters.keySet()) {  
					
				resultsByCellphone = ContacterManager.searchUserByCellphone(key);
				if (resultsByCellphone.size() > 0) {
					user = resultsByCellphone.get(0);
			        Log.d(TAG, "found user: " + user.getName());
			        
			        if(ContacterManager.contacters.containsKey(user.getJID()) == false && 
			          !ContacterManager.userMe.getName().equals(user.getName())) {
				        user.setContactName(ContacterManager.phoneContacters.get(key));
				        contactUsers.add(user);
			        }
			        else {
				        Log.d(TAG, "friend yet");
			        }
				}
				else {
			        Log.d(TAG, "no match user");
				}
				cur += 1;
				publishProgress((int) ((cur / (float) total) * 100));
	        }  
		  
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
				if (errorCode == 401 || errorCode == 403) {
					return Constant.LOGIN_ERROR_ACCOUNT_PASS;
				} else {
					xe.printStackTrace();
					Log.d(TAG, "exception code " + errorCode);
					return Constant.SERVER_UNAVAILABLE;
				}
			} else {
				return Constant.UNKNOWN_ERROR;
			}
		}
	}

}
