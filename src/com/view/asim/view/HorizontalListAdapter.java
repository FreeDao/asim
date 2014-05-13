package com.view.asim.view;

import java.util.List;

import com.view.asim.model.User;

import com.view.asim.R;
import android.content.Context;  
import android.view.LayoutInflater;  
import android.view.View;  
import android.view.ViewGroup;  
import android.widget.BaseAdapter;  
import android.widget.ImageView;  
import android.widget.TextView;  
  
public class HorizontalListAdapter extends BaseAdapter{  
    private Context mContext;  
    private LayoutInflater mInflater;  
    private List<User> mUsers = null;
  
    public HorizontalListAdapter(Context context, List<User> users){  
        this.mContext = context;  
        this.mUsers = users;  
        mInflater = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }  
    @Override  
    public int getCount() {  
        return mUsers.size();  
    }  
    @Override  
    public Object getItem(int position) {  
        return mUsers.get(position);  
    }  
  
    @Override  
    public long getItemId(int position) {  
        return position;  
    }  
  
    @Override  
    public View getView(int position, View convertView, ViewGroup parent) {  
  
    	User u = mUsers.get(position);
    	
        ViewHolder holder;  
        if(convertView == null){  
            holder = new ViewHolder();  
            convertView = mInflater.inflate(R.layout.selected_user_item, null);  
            holder.mImage = (ImageView) convertView.findViewById(R.id.avatar_img);  
            convertView.setTag(holder);  
        }else{  
            holder = (ViewHolder) convertView.getTag();  
        }
          
        holder.mImage.setTag(u);
        
        if (u.getHeadImg() != null) {
        	holder.mImage.setImageBitmap(u.getHeadImg());
		} else {
			if (u.getGender() == null) {
				holder.mImage.setImageResource(R.drawable.default_avatar_male);
				
			} else {
				if (u.getGender().equals(User.MALE)) {
					holder.mImage.setImageResource(R.drawable.default_avatar_male);
				}
				else {
					holder.mImage.setImageResource(R.drawable.default_avatar_female);
				}
			}
		
		}
        return convertView;  
    }  
  
    private static class ViewHolder {  
        private ImageView mImage;  
    }  

}