package com.view.asim.view;

import java.util.List;

import com.view.asim.model.Notice;
import com.view.asim.model.User;
import com.view.asim.util.StringUtil;

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

public class ContactUsersAdapter extends BaseAdapter {
	private LayoutInflater mInflater;
	private List<User> mContactUsers;
	private Context context;
	private OnClickListener mListener = null;

	public ContactUsersAdapter(Context context, List<User> contactUsers, OnClickListener addClickListener) {
		this.context = context;
		mInflater = LayoutInflater.from(context);
		this.mContactUsers = contactUsers;
		this.mListener = addClickListener;
	}

	public void setUserList(List<User> users) {
		this.mContactUsers = users;
	}

	@Override
	public int getCount() {
		return mContactUsers == null ? 0 : mContactUsers.size();
	}

	@Override
	public Object getItem(int position) {
		return mContactUsers.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		User u = mContactUsers.get(position);
		ViewHolder holder = null;
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.add_contact_friends_item, null);
			holder = new ViewHolder();
			holder.tvName = (TextView) convertView.findViewById(R.id.new_user_name_txt);
			holder.tvNumber = (TextView) convertView.findViewById(R.id.new_user_phone_txt);
			holder.btnAdd = (Button) convertView.findViewById(R.id.request_btn);
			
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.tvName.setText(u.getContactName());
		holder.tvNumber.setText(StringUtil.getCellphoneByName(u.getName()));
		holder.btnAdd.setTag(u);
		holder.btnAdd.setOnClickListener(mListener);

		return convertView;
	}

	private class ViewHolder {
		public TextView tvName;
		public TextView tvNumber;
		public Button btnAdd;
	}
}