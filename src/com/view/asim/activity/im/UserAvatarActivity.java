package com.view.asim.activity.im;

import java.io.InputStream;

import org.jivesoftware.smack.XMPPException;
import com.view.asim.activity.ActivitySupport;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.MessageManager;
import com.view.asim.manager.NoticeManager;
import com.view.asim.manager.UserManager;
import com.view.asim.model.LoginConfig;
import com.view.asim.model.User;
import com.view.asim.util.StringUtil;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.view.asim.R;

/**
 * 
 * 用户资料查看.
 * 
 * @author xuweinan
 */
public class UserAvatarActivity extends ActivitySupport {
	public static final String TAG = "UserAvatarActivity";
		
	private int mShowType = 0;
	
	private User mUser = null;
	private ImageView mAvatarImg = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.user_avatar);
		init();
	}

	private void init() {
		getEimApplication().addActivity(this);

		mUser = (User)getIntent().getParcelableExtra(User.userKey);
		mAvatarImg = (ImageView) findViewById(R.id.avatar_img);
		if (mUser.getHeadImg() != null) {
			mAvatarImg.setImageBitmap(mUser.getHeadImg());
		} else {
			if (mUser.getGender() == null) {
				mAvatarImg.setImageResource(R.drawable.default_avatar_male);
				
			} else {
				if (mUser.getGender().equals(User.MALE)) {
					mAvatarImg.setImageResource(R.drawable.default_avatar_male);
				}
				else {
					mAvatarImg.setImageResource(R.drawable.default_avatar_female);
				}
			}
		}	
	}
	
}
