package com.view.asim.view;

import java.util.List;

import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.SingleCallLog;
import com.view.asim.model.CallLogs;
import com.view.asim.model.ChatHisBean;
import com.view.asim.model.GroupUser;
import com.view.asim.model.IMMessage;
import com.view.asim.model.User;
import com.view.asim.util.CallUtil;
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

public class CallLogAdapter extends BaseAdapter {
	private static final String TAG = "CallLogAdapter";
	private LayoutInflater mInflater;
	private List<SingleCallLog> calllogs;
	private Context context;
	private OnClickListener calllogOnClick = null;

	public CallLogAdapter(Context context, List<SingleCallLog> calllogs) {
		this.context = context;
		mInflater = LayoutInflater.from(context);
		this.calllogs = calllogs;
	}

	public void refreshList(List<SingleCallLog> calllogs) {
		this.calllogs = calllogs;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return calllogs.size();
	}

	@Override
	public Object getItem(int position) {
		return calllogs.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		SingleCallLog logs = calllogs.get(position);
		ViewHolderx holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.user_calllog_item, null);
			holder = new ViewHolderx();
			holder.callInfoTxt = (TextView) convertView.findViewById(R.id.call_info);
			holder.callTimeTxt = (TextView) convertView.findViewById(R.id.call_time);
			//holder.secretCallImg = (ImageView) convertView.findViewById(R.id.secret_call_img);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolderx) convertView.getTag();
		}

		String jid = logs.getWith();
		Log.d(TAG, "single calllog view pos " + position + ", jid " + jid + ", type " + logs.getType() + ", security " + logs.getSecurity());
		
		String dispTime = DateUtil.getMDHM(logs.getTime());
		holder.callTimeTxt.setText(dispTime);
		
		String callInfo = "";
		Drawable leftIcon = null;
		Drawable rightIcon = null;
		
		switch(logs.getType()) {
		// 来电
		case 1:
			callInfo += "来电" + getStringInfoByStatusCode(logs);
			leftIcon = context.getResources().getDrawable(R.drawable.incoming_call);
			leftIcon.setBounds(0, 0, leftIcon.getMinimumWidth(), leftIcon.getMinimumHeight());
			break;
		
		// 去电
		case 2:
			callInfo += "去电" + getStringInfoByStatusCode(logs);
			leftIcon = context.getResources().getDrawable(R.drawable.outgoing_call);
			leftIcon.setBounds(0, 0, leftIcon.getMinimumWidth(), leftIcon.getMinimumHeight());
			//holder.callInfoTxt.setCompoundDrawables(outgoingCallIcon, null, null, null);
			break;
			
		// 未接来电
		case 3:
			callInfo = "未接来电";
			leftIcon = context.getResources().getDrawable(R.drawable.missed_call);
			leftIcon.setBounds(0, 0, leftIcon.getMinimumWidth(), leftIcon.getMinimumHeight());
			//holder.callInfoTxt.setCompoundDrawables(missedCallIcon, null, null, null);
			break;
		
		}
		
		if (logs.getSecurity().equals(IMMessage.ENCRYPTION)) {
			rightIcon = context.getResources().getDrawable(R.drawable.notificationbar_icon_logo_intercept_call);
			rightIcon.setBounds(0, 0, rightIcon.getMinimumWidth(), rightIcon.getMinimumHeight());
		}
		
		holder.callInfoTxt.setCompoundDrawables(leftIcon, null, rightIcon, null);

		holder.callInfoTxt.setText(callInfo);
		holder.callInfoTxt.setTag(logs);
		
		if (calllogOnClick != null) {
			convertView.setOnClickListener(calllogOnClick);
		}
		return convertView;
	}

	public class ViewHolderx {
		public TextView callInfoTxt;
		public TextView callTimeTxt;
	}

	private String getStringInfoByStatusCode(SingleCallLog log) {
		if (log.getStatusCode() == 200) {
			return "（通话 " + log.getDuration()  + " 秒）";
		}
		else {
			return "（" + CallUtil.getStringByStatusCode(log.getStatusCode()) + "）";
		}
	}

	public void setOnClickListener(OnClickListener calllogOnClick) {
		this.calllogOnClick = calllogOnClick;
	}
}