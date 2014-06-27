package com.view.asim.view;

import java.util.List;

import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.ChatHisBean;
import com.view.asim.model.GroupUser;
import com.view.asim.model.IMMessage;
import com.view.asim.model.User;
import com.view.asim.util.DateUtil;
import com.view.asim.util.FaceConversionUtil;

import android.content.Context;
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

public class RecentChatAdapter extends BaseAdapter {
	private static final String TAG = "RecentChatAdapter";
	private LayoutInflater mInflater;
	private List<ChatHisBean> inviteUsers;
	private Context context;
	private OnClickListener theadOnClick = null;

	public RecentChatAdapter(Context context, List<ChatHisBean> inviteUsers) {
		this.context = context;
		mInflater = LayoutInflater.from(context);
		this.inviteUsers = inviteUsers;
	}

	public void refreshList(List<ChatHisBean> inviteUsers) {
		this.inviteUsers = inviteUsers;
		notifyDataSetChanged();
	}

	@Override
	public int getCount() {
		return inviteUsers.size();
	}

	@Override
	public Object getItem(int position) {
		return inviteUsers.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		SpannableString spannableString;
		
		ChatHisBean notice = inviteUsers.get(position);
		int ppCount = notice.getUnreadCount();
		ViewHolderx holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.recent_chat_item, null);
			holder = new ViewHolderx();
			holder.nameTxt = (TextView) convertView.findViewById(R.id.contact_name_txt);
			holder.avatarImg = (ImageView) convertView.findViewById(R.id.contact_avatar_img);
			holder.lastMsgTxt = (TextView) convertView.findViewById(R.id.last_msg_txt);
			holder.lastMsgTimeTxt = (TextView) convertView.findViewById(R.id.last_msg_time_txt);
			holder.newMsgNumTxt = (TextView) convertView.findViewById(R.id.new_msg_num_txt);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolderx) convertView.getTag();
		}

		String jid = notice.getWith();
		Log.d(TAG, "view pos " + position + ", jid " + jid);
		
		if (notice.getChatType().equals(IMMessage.SINGLE)) {
			User u = ContacterManager.contacters.get(jid);
			holder.nameTxt.setText(u.getNickName());
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
			GroupUser g = ContacterManager.groupUsers.get(jid);
			holder.nameTxt.setText(g.getNickName());
			holder.avatarImg.setImageResource(R.drawable.ic_contact_list_picture_group);

		}		
		holder.nameTxt.setTag(notice);
		
		spannableString = FaceConversionUtil.getInstace().getExpressionString(context, notice.getContent(), 40);
		
		holder.lastMsgTxt.setText(spannableString);
		
		String dispTime = DateUtil.getMDHM(DateUtil.str2Calendar(notice.getTime()).getTimeInMillis());
		holder.lastMsgTimeTxt.setText(dispTime);

		if (ppCount > 0) {
			holder.newMsgNumTxt.setText(ppCount + "");
			holder.newMsgNumTxt.setVisibility(View.VISIBLE);

		} else {
			holder.newMsgNumTxt.setVisibility(View.GONE);
		}
		
		if (theadOnClick != null) {
			convertView.setOnClickListener(theadOnClick);
		}
		return convertView;
	}

	public class ViewHolderx {
		public ImageView avatarImg;
		public TextView nameTxt;
		public TextView lastMsgTxt;
		public TextView lastMsgTimeTxt;
		public TextView newMsgNumTxt;

	}


	public void setOnClickListener(OnClickListener contacterOnClick) {

		this.theadOnClick = contacterOnClick;
	}
}