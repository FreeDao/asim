package com.view.asim.task;

import java.util.Collection;
import java.util.List;

import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.XMPPError;

import com.view.asim.comm.Constant;
import com.view.asim.activity.ActivitySupport;
import com.view.asim.activity.IActivitySupport;
import com.view.asim.activity.MainActivity;
import com.view.asim.activity.im.UserInfoActivity;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.LoginConfig;
import com.view.asim.model.User;
import com.view.asim.util.StringUtil;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;
import com.view.asim.R;

/**
 * 
 * �����û��첽����.
 * 
 * @author  xuweinan
 */
public class SearchUserTask extends AsyncTask<String, Integer, Integer> {
	private final static String TAG = "SearchUserTask";
	
	private ProgressDialog pd;
	private ActivitySupport activity;
	private LoginConfig loginConfig;
	private String name = null;
	private User user = null;

	public SearchUserTask(ActivitySupport as, String name) {
		this.activity = as;
		this.pd = as.getProgressDialog();
		this.name = name;
	}

	@Override
	protected void onPreExecute() {
		// �����ע�����Զ���¼��������ʾ���ȶԻ���
		pd.setTitle("���Ե�");
		pd.setMessage("��������...");
		pd.show();
		super.onPreExecute();
	}

	@Override
	protected Integer doInBackground(String... params) {
		return search();
	}

	@Override
	protected void onProgressUpdate(Integer... values) {
	}

	@Override
	protected void onPostExecute(Integer result) {
		pd.dismiss();
		switch (result) {
		case Constant.SERVER_SUCCESS: // �����ɹ�
			Intent intent = new Intent();
			
			if(ContacterManager.contacters.containsKey(user.getJID())) {
				intent.putExtra(User.userKey, ContacterManager.contacters.get(user.getJID()));
				intent.putExtra(UserInfoActivity.modeKey, UserInfoActivity.FRIEND_MODE);
			} else {
				intent.putExtra(User.userKey, user);
				intent.putExtra(UserInfoActivity.modeKey, UserInfoActivity.STRANDER_MODE);
			}
			intent.setClass(activity, UserInfoActivity.class);
			activity.startActivity(intent);
			activity.finish();
			
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
		case Constant.NONE_RESULTS:// ������
			new AlertDialog.Builder(activity)
			.setMessage("���������û����Ƿ���ȷ")
			.setTitle("�����Ѳ�����")
			.setNeutralButton("ȷ��", 
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

	// ��¼
	private Integer search() {
		try {
			List<User> resultsByCellphone = null;
			List<User> resultsByName = null;

			if(StringUtil.isNumeric(name)) {
				resultsByCellphone = ContacterManager.searchUserByCellphone(name);
				if (resultsByCellphone.size() > 0) {
					user = resultsByCellphone.get(0);
					return Constant.SERVER_SUCCESS;
				}
			}
			
			resultsByName = ContacterManager.searchUserByName(name);
			if (resultsByName.size() > 0) {
				user = resultsByName.get(0);
				return Constant.SERVER_SUCCESS;
			}
			else {
				return Constant.NONE_RESULTS;
			}
			
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
					Log.d(TAG, "exception code " + errorCode);
					return Constant.SERVER_UNAVAILABLE;
				}
			} else {

				return Constant.UNKNOWN_ERROR;
			}
		}
	}
}
