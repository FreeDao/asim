package com.view.asim.view;

import java.util.List;

import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.CallLogs;
import com.view.asim.model.ChatHisBean;
import com.view.asim.model.GroupUser;
import com.view.asim.model.IMMessage;
import com.view.asim.model.User;
import com.view.asim.util.DateUtil;
import com.view.asim.util.FaceConversionUtil;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.SpannableString;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.view.asim.R;

public class CallLogsAdapter extends BaseAdapter {
	private static final String TAG = "CallLogsAdapter";
	private LayoutInflater mInflater;
	private List<CallLogs> calllogsList;
	private Context context;
	private OnClickListener calllogsOnClick = null;

	public CallLogsAdapter(Context context, List<CallLogs> calllogsList) {
		this.context = context;
		mInflater = LayoutInflater.from(context);
		this.calllogsList = calllogsList;
	}

	public void refreshList(List<CallLogs> calllogsList) {
		this.calllogsList = calllogsList;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return calllogsList.size();
	}

	@Override
	public Object getItem(int position) {
		return calllogsList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		CallLogs logs = calllogsList.get(position);
		ViewHolderx holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.recent_calllog_item, null);
			holder = new ViewHolderx();
			holder.nameTxt = (TextView) convertView.findViewById(R.id.contact_name_txt);
			holder.avatarImg = (ImageView) convertView.findViewById(R.id.contact_avatar_img);
			holder.lastCallInfoTxt = (TextView) convertView.findViewById(R.id.last_call_txt);
			holder.lastCallTimeTxt = (TextView) convertView.findViewById(R.id.last_call_time_txt);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolderx) convertView.getTag();
		}

		String jid = logs.getWith();
		Log.d(TAG, "calllogs view pos " + position + ", jid " + jid);
		
		User u = ContacterManager.contacters.get(jid);
		if (u != null) {
			String name = u.getNickName();
			if (logs.getTotalCount() > 1) {
				name += " (" + logs.getTotalCount() + ")";
			}
			holder.nameTxt.setText(name);
			
			if (u.getHeadImg() != null) {
				holder.avatarImg.setImageBitmap(u.getHeadImg());
			} else {
				if (u.getGender() != null) {
					holder.avatarImg.setImageResource(u.getGender().equals(User.MALE) ? 
							R.drawable.default_avatar_male : R.drawable.default_avatar_female);
				} else {
					holder.avatarImg.setImageResource(R.drawable.default_avatar_male);
				}
			}
		}
		else {
			Log.d(TAG, "cannot found roster user by jid");
		}
		
		String dispTime = DateUtil.getMDHM(logs.getTime());
		holder.lastCallTimeTxt.setText(dispTime);
		
		Drawable leftIcon = null;
		Drawable rightIcon = null;
		
		switch(logs.getType()) {
		// 来电
		case 1:
			holder.lastCallInfoTxt.setText("来电");
			leftIcon = context.getResources().getDrawable(R.drawable.incoming_call);
			leftIcon.setBounds(0, 0, leftIcon.getMinimumWidth(), leftIcon.getMinimumHeight());
			//holder.lastCallInfoTxt.setCompoundDrawables(incomingCallIcon, null, null, null);
			break;
		
		// 去电
		case 2:
			holder.lastCallInfoTxt.setText("去电");
			leftIcon = context.getResources().getDrawable(R.drawable.outgoing_call);
			leftIcon.setBounds(0, 0, leftIcon.getMinimumWidth(), leftIcon.getMinimumHeight());
			//holder.lastCallInfoTxt.setCompoundDrawables(outgoingCallIcon, null, null, null);
			break;
			
		// 未接来电
		case 3:
			holder.lastCallInfoTxt.setText("未接来电");
			leftIcon = context.getResources().getDrawable(R.drawable.missed_call);
			leftIcon.setBounds(0, 0, leftIcon.getMinimumWidth(), leftIcon.getMinimumHeight());
			//holder.lastCallInfoTxt.setCompoundDrawables(missedCallIcon, null, null, null);
			break;
		
		}
		if (logs.getSecurity().equals(IMMessage.ENCRYPTION)) {
			rightIcon = context.getResources().getDrawable(R.drawable.notificationbar_icon_logo_intercept_call);
			rightIcon.setBounds(0, 0, rightIcon.getMinimumWidth(), rightIcon.getMinimumHeight());
		}
		
		holder.lastCallInfoTxt.setCompoundDrawables(leftIcon, null, rightIcon, null);

		holder.nameTxt.setTag(logs);
		
		if (calllogsOnClick != null) {
			convertView.setOnClickListener(calllogsOnClick);
		}
		return convertView;
	}

	public class ViewHolderx {
		public ImageView avatarImg;
		public TextView nameTxt;
		public TextView lastCallInfoTxt;
		public TextView lastCallTimeTxt;
	}


	public void setOnClickListener(OnClickListener calllogsOnClick) {
		this.calllogsOnClick = calllogsOnClick;
	}
}