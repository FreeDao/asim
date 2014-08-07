package com.view.asim.activity.im;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jivesoftware.smack.packet.Presence;

import com.view.asim.activity.ActivitySupport;
import com.view.asim.activity.LoginActivity;
import com.view.asim.activity.MainActivity;
import com.view.asim.comm.Constant;
import com.view.asim.manager.AUKeyManager;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.NoticeManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.Notice;
import com.view.asim.model.User;
import com.view.asim.utils.PinyinComparator;
import com.view.asim.utils.StringUtil;
import com.view.asim.view.ContactsAdapter;
import com.view.asim.view.HorizontalListAdapter;
import com.view.asim.view.HorizontalListView;
import com.view.asim.view.NoticeAdapter;
import com.view.asim.view.ScrollLayout;
import com.view.asim.view.SideBar;
import com.view.asim.view.SideBar.OnTouchingLetterChangedListener;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.view.asim.R;

/**
 * 
 * 群聊的多选好友界面
 * 
 * @author  xuweinan
 */
public class GroupAddUserActivity extends ActivitySupport {	
	private final static String TAG = "GroupAddUserActivity";

	private LayoutInflater inflater;

	private TextView mBackTxt = null;
	private TextView mAlphabeticText = null;
	private Button mCfmBtn = null;
	private ListView mContacters = null;
	private ContactsAdapter mContactListAdapter = null;
	private HorizontalListView mSelectedUserList = null;
	private HorizontalListAdapter mSelectedUserAdapter = null;
	private ArrayList<User> mSelectedUsers = new ArrayList<User>();
	private List<User> mAllContacters = new ArrayList<User>();

	private PinyinComparator mPinyinComparator;
	private SideBar mSideBar = null;



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.add_group_contacters);
		init();
	}
	
	private void init() {
		getEimApplication().addActivity(this);
		inflater = LayoutInflater.from(context);

		mBackTxt = (TextView) findViewById(R.id.title_back_btn);
		mBackTxt.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		mCfmBtn = (Button) findViewById(R.id.confirm_btn);
		mCfmBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra(User.userListKey, mSelectedUsers);
                setResult(RESULT_OK, intent);
                finish();
			}
		});

		mContacters = (ListView) findViewById(R.id.contacts_list);
		mContacters.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int p,
					long arg3) {
				User u = (User) (v.findViewById(R.id.contact_name_txt).getTag());

				if (mSelectedUsers.contains(u)) {
					// unselect
					mSelectedUsers.remove(u);
					mContactListAdapter.updateSelection(p, false);
				} else {
					// select
					mSelectedUsers.add(u);
					mContactListAdapter.updateSelection(p, true);
				}
				mCfmBtn.setText("确认" + (mSelectedUsers.size() > 0 ? "(" + mSelectedUsers.size() + ")" : ""));
				mContactListAdapter.notifyDataSetChanged();
				mSelectedUserAdapter.notifyDataSetChanged();
			}
		});
		
		mSelectedUserList = (HorizontalListView) findViewById(R.id.selected_user_list);
		mSelectedUserAdapter = new HorizontalListAdapter(this, mSelectedUsers);
		mSelectedUserList.setAdapter(mSelectedUserAdapter);
		mSelectedUserList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View v, int p,
					long arg3) {
				User u = (User) (v.findViewById(R.id.avatar_img).getTag());

				if (mSelectedUsers.contains(u)) {
					// unselect
					mSelectedUsers.remove(u);
					mContactListAdapter.updateSelection(u, false);
				} else {
					// select
					mSelectedUsers.add(u);
					mContactListAdapter.updateSelection(u, true);
				}
				mCfmBtn.setText("确认" + (mSelectedUsers.size() > 0 ? "(" + mSelectedUsers.size() + ")" : ""));

				mContactListAdapter.notifyDataSetChanged();
				mSelectedUserAdapter.notifyDataSetChanged();
			}
		});

		// 联系人子界面的联系人列表
		mPinyinComparator = new PinyinComparator();
		
		mAllContacters = ContacterManager.getContacterList();
		Collections.sort(mAllContacters, mPinyinComparator);
		mContactListAdapter = new ContactsAdapter(this, mAllContacters, ContactsAdapter.SELECT_MODE);
		mContacters.setAdapter(mContactListAdapter);
		

		// 联系人子界面右侧的字母表
		mSideBar = (SideBar) findViewById(R.id.sidebar_view);
		mAlphabeticText = (TextView) findViewById(R.id.alphabetic_txt);
		mSideBar.setTextView(mAlphabeticText);
		mSideBar.setOnTouchingLetterChangedListener(new OnTouchingLetterChangedListener() {

			@Override
			public void onTouchingLetterChanged(String s) {
				int position = mContactListAdapter.getPositionForSection(s.charAt(0));
				if (position != -1) {
					mContacters.setSelection(position);
				}

			}
		});
	}

}
