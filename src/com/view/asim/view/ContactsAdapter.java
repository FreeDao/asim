package com.view.asim.view;

import java.util.ArrayList;
import java.util.List;

import com.view.asim.manager.AUKeyManager;
import com.view.asim.model.IMMessage;
import com.view.asim.model.User;

import com.view.asim.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

public class ContactsAdapter extends BaseAdapter {
	private static final String TAG = "ContactsAdapter";
	
	public static final int SHOW_MODE = 1;
	public static final int SELECT_MODE = 2;

	private List<User> list;
	private LayoutInflater layoutinfater;
	private int flag;
	private SharedPreferences private_sign_shp;
	private Context context;
	private boolean[] is_select;
	private OnClickListener contacterOnClick =  null;
	

	public ContactsAdapter(Context context, List<User> list, int flag) {
		super();
		this.list = list;
		this.layoutinfater = LayoutInflater.from(context);
		this.flag = flag;
		this.context = context;
		if(flag == SELECT_MODE){
			this.is_select = new boolean[list.size()];
			setFalse();
		}
	}
	public void setFalse(){
		for(int i = 0; i < is_select.length ; i++)
			is_select[i] = false;
	}
	
	public void updateSelection(int p, boolean selected) {
		is_select[p] = selected;
	}
	
	public void updateSelection(User u, boolean selected) {
		if (list.contains(u)) {
			int p = list.indexOf(u);
			is_select[p] = selected;
		}
	}

	@Override
	public int getCount() {
		return list.size();
	}

	public void refreshList(List<User> list) {
		this.list = list;
		notifyDataSetChanged();
	}

	@Override
	public User getItem(int p) {
		return list.get(p);
	}

	@Override
	public long getItemId(int p) {
		return p;
	}

	@Override
	public View getView(final int p, View convertView, ViewGroup parent) {
		Drawable rightIcon = null;

		if (flag == SHOW_MODE) {
			View v = null;
			ViewHolderShow holder = null;
			if (convertView == null) {
				v = layoutinfater.inflate(R.layout.contact_item, null);
				holder = new ViewHolderShow();
				holder.text = (TextView) v.findViewById(R.id.contact_name_txt);
				holder.tvLetter = (TextView) v.findViewById(R.id.alphabetic_txt);
				holder.tvTopBlank = (TextView) v.findViewById(R.id.top_blank_txt);
				holder.ivDivider = (ImageView) v.findViewById(R.id.alphabetic_divide_img);
				holder.tvRemark = (TextView) v.findViewById(R.id.contact_remark_txt);
				holder.headimage = (ImageView) v.findViewById(R.id.contact_head_img);
				holder.securityImage = (ImageView) v.findViewById(R.id.security_img);
				v.setTag(holder);

			} else {
				v = convertView;
				holder = (ViewHolderShow) v.getTag();
			}
			
			User u = getItem(p);
			holder.text.setTag(u);

			if (u.getNickName() != null) {
				holder.text.setText(u.getNickName());
			} else {
				holder.text.setText(u.getName());
			}
			
			if (u.getSecurity() != null && u.getSecurity().equals(AUKeyManager.ATTACHED)) {
				holder.securityImage.setImageResource(R.drawable.notificationbar_icon_logo_normal);
				holder.securityImage.setVisibility(View.VISIBLE);
				//rightIcon = context.getResources().getDrawable(R.drawable.notificationbar_icon_logo_normal);
				//rightIcon.setBounds(0, 0, rightIcon.getMinimumWidth(), rightIcon.getMinimumHeight());
			}
			
			//holder.text.setCompoundDrawables(null, null, rightIcon, null);
			
			Log.d(TAG, "list view item " + p + ", text:" + holder.text.getText() + ", tag user: " + ((User)(holder.text.getTag())).getName() + ", security: " + ((User)(holder.text.getTag())).getSecurity());

			if (u.getHeadImg() != null) {
				holder.headimage.setImageBitmap(u.getHeadImg());
			} else {
				if (u.getGender() == null) {
					holder.headimage.setImageResource(R.drawable.default_avatar_male);
					
				} else {
					if (u.getGender().equals(User.MALE)) {
						holder.headimage.setImageResource(R.drawable.default_avatar_male);
					}
					else {
						holder.headimage.setImageResource(R.drawable.default_avatar_female);
					}
				}
			
			}
			
			if (u.getRemark() == null || u.getRemark().trim().length() == 0) {
				holder.tvRemark.setVisibility(View.GONE);
				holder.tvTopBlank.setVisibility(View.VISIBLE);
				
			}
			else {
				holder.tvRemark.setText(u.getRemark());
				holder.tvRemark.setVisibility(View.VISIBLE);
				holder.tvTopBlank.setVisibility(View.GONE);
			}
			// 根据position获取分类的首字母的Char asc值
			int section = getSectionForPosition(p);

			// 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
			if (p == getPositionForSection(section)) {
				holder.tvLetter.setVisibility(View.VISIBLE);
				holder.tvLetter.setText(u.getSortLetters());
				holder.ivDivider.setVisibility(View.VISIBLE);
			} else {
				holder.tvLetter.setVisibility(View.GONE);
				holder.ivDivider.setVisibility(View.GONE);
			}
			
			if (contacterOnClick != null ) 
				v.setOnClickListener(contacterOnClick);

			return v;
		} else if( flag == SELECT_MODE ) {
			View v = null;
			ViewHolderSelect holder = null;
			if (convertView == null) {
				v = layoutinfater.inflate(R.layout.contact_item, null);
				holder = new ViewHolderSelect();
				holder.text = (TextView) v.findViewById(R.id.contact_name_txt);
				holder.tvLetter = (TextView) v.findViewById(R.id.alphabetic_txt);
				holder.ivDivider = (ImageView) v.findViewById(R.id.alphabetic_divide_img);
				holder.tvTopBlank = (TextView) v.findViewById(R.id.top_blank_txt);
				holder.tvRemark = (TextView) v.findViewById(R.id.contact_remark_txt);
				holder.headimage = (ImageView) v.findViewById(R.id.contact_head_img);
				holder.securityImage = (ImageView) v.findViewById(R.id.security_img);
				holder.is_select = (ImageView) v.findViewById(R.id.is_select_img);
				v.setTag(holder);

			} else {
				v = convertView;
				holder = (ViewHolderSelect) v.getTag();

			}
			User u = getItem(p);
			holder.text.setTag(u);

			if (u.getNickName() != null) {
				holder.text.setText(u.getNickName());
			} else {
				holder.text.setText(u.getName());
			}
			
			if (u.getSecurity() != null && u.getSecurity().equals(AUKeyManager.ATTACHED)) {
				holder.securityImage.setImageResource(R.drawable.notificationbar_icon_logo_normal);
				holder.securityImage.setVisibility(View.VISIBLE);
				//rightIcon = context.getResources().getDrawable(R.drawable.notificationbar_icon_logo_normal);
				//rightIcon.setBounds(0, 0, rightIcon.getMinimumWidth(), rightIcon.getMinimumHeight());
			}
			
			if (u.getHeadImg() != null) {
				holder.headimage.setImageBitmap(u.getHeadImg());
			} else {
				if (u.getGender() == null) {
					holder.headimage.setImageResource(R.drawable.default_avatar_male);
					
				} else {
					if (u.getGender().equals(User.MALE)) {
						holder.headimage.setImageResource(R.drawable.default_avatar_male);
					}
					else {
						holder.headimage.setImageResource(R.drawable.default_avatar_female);
					}
				}
			
			}
			
			if (u.getRemark() == null || u.getRemark().trim().length() == 0) {
				holder.tvRemark.setVisibility(View.GONE);
				holder.tvTopBlank.setVisibility(View.VISIBLE);
				
			}
			else {
				holder.tvRemark.setText(u.getRemark());
				holder.tvRemark.setVisibility(View.VISIBLE);
				holder.tvTopBlank.setVisibility(View.GONE);
			}
			
			holder.is_select.setVisibility(View.VISIBLE);
			
			if(is_select[p])
				holder.is_select.setImageResource(R.drawable.g_checkbox_checked_green);
			else
				holder.is_select.setImageResource(R.drawable.g_checkbox_unchecked);

			// 根据position获取分类的首字母的Char asc值
			int section = getSectionForPosition(p);

			// 如果当前位置等于该分类首字母的Char的位置 ，则认为是第一次出现
			if (p == getPositionForSection(section)) {
				holder.tvLetter.setVisibility(View.VISIBLE);
				holder.tvLetter.setText(u.getSortLetters());
				holder.ivDivider.setVisibility(View.VISIBLE);
				
			} else {
				holder.tvLetter.setVisibility(View.GONE);
				holder.ivDivider.setVisibility(View.GONE);

			}
			
			if (contacterOnClick != null ) 
				v.setOnClickListener(contacterOnClick);

			return v;
		}

		return null;
	}

	class ViewHolderShow {

		TextView text;
		TextView tvLetter;
		TextView tvTopBlank;
		ImageView ivDivider;
		TextView tvRemark;
		ImageView headimage;
		ImageView securityImage;
	}

	class ViewHolderSelect{

		TextView text;
		TextView tvLetter;
		TextView tvTopBlank;
		ImageView ivDivider;
		TextView tvRemark;
		ImageView headimage;
		ImageView securityImage;
		ImageView is_select;
	}
	
	public int getSectionForPosition(int p) {
		// TODO Auto-generated method stub
		return list.get(p).getSortLetters().charAt(0);
	}

	public int getPositionForSection(int section) {
		for (int i = 0; i < getCount(); i++) {
			String sortStr = list.get(i).getSortLetters();
			char firstChar = sortStr.toUpperCase().charAt(0);
			if (firstChar == section) {
				return i;
			}
		}

		return -1;
	}
	
	public void setOnClickListener(OnClickListener contacterOnClick) {

		this.contacterOnClick = contacterOnClick;
	}
}
