package com.view.asim.view;

import java.util.List;

import com.view.asim.model.Notice;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.view.asim.R;

public class NoticeAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private List<Notice> inviteNotices;
	private Context context;
	private OnClickListener mListener = null;

	public NoticeAdapter(Context context, List<Notice> inviteUsers, OnClickListener acceptClickListener) {
		this.context = context;
		mInflater = LayoutInflater.from(context);
		this.inviteNotices = inviteUsers;
		this.mListener = acceptClickListener;
	}

	public void setNoticeList(List<Notice> inviteUsers) {
		this.inviteNotices = inviteUsers;
	}

	@Override
	public int getCount() {
		return inviteNotices == null ? 0 : inviteNotices.size();
	}

	@Override
	public Object getItem(int position) {
		return inviteNotices.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Notice notice = inviteNotices.get(position);
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.my_notice_item, null);
			holder = new ViewHolder();
			holder.ivAvatar = (ImageView) convertView.findViewById(R.id.new_user_head_img);
			holder.tvName = (TextView) convertView.findViewById(R.id.new_user_name_txt);
			holder.btnAccept = (Button) convertView.findViewById(R.id.accept_btn);
			holder.tvAdded = (TextView) convertView.findViewById(R.id.added_txt);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		holder.tvName.setText(notice.getContent());

		if (notice.getAvatar() != null) {
			holder.ivAvatar.setImageBitmap(notice.getAvatar());
		} else
			holder.ivAvatar.setImageResource(R.drawable.default_avatar_male);
		holder.tvName.setTag(notice);
		holder.ivAvatar.setTag(notice);
		holder.ivAvatar.setOnClickListener(mListener);
		holder.tvName.setOnClickListener(mListener);

		if (notice.getStatus().equals(Notice.STATUS_ADD_REQUEST)) {
			holder.btnAccept.setVisibility(View.VISIBLE);
			holder.tvAdded.setVisibility(View.GONE);
			holder.btnAccept.setTag(notice);
			holder.btnAccept.setOnClickListener(mListener);
		} else if (notice.getStatus().equals(Notice.STATUS_WAIT_FOR_ACCEPT)) {
			holder.btnAccept.setVisibility(View.GONE);
			holder.tvAdded.setText(context.getResources().getString(R.string.wait_verification));
			holder.tvAdded.setVisibility(View.VISIBLE);
		} else {
			holder.btnAccept.setVisibility(View.GONE);
			holder.tvAdded.setText(context.getResources().getString(R.string.add_success));
			holder.tvAdded.setVisibility(View.VISIBLE);
		}
		
		return convertView;
	}

	private class ViewHolder {
		public ImageView ivAvatar;
		public TextView tvName;
		public Button btnAccept;
		public TextView tvAdded;
	}
}
