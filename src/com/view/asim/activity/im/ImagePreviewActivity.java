package com.view.asim.activity.im;

import java.io.InputStream;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.packet.VCard;

import com.view.asim.activity.ActivitySupport;
import com.view.asim.comm.Constant;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.MessageManager;
import com.view.asim.manager.NoticeManager;
import com.view.asim.manager.UserManager;
import com.view.asim.model.ChatMessage;
import com.view.asim.model.LoginConfig;
import com.view.asim.model.User;
import com.view.asim.util.ImageUtil;
import com.view.asim.util.StringUtil;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
public class ImagePreviewActivity extends ActivitySupport {
	public static final String TAG = "ImagePreviewActivity";
			
	private ChatMessage message = null;
	private ImageView mImg = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		setContentView(R.layout.user_avatar);
		init();
	}

	private void init() {
		getEimApplication().addActivity(this);

		message = (ChatMessage) getIntent().getParcelableExtra(ChatMessage.IMMESSAGE_KEY);
		
		mImg = (ImageView) findViewById(R.id.avatar_img);
		
		Bitmap bitmap = BitmapFactory.decodeFile(message.getAttachment().getSrcUri());
		mImg.setImageBitmap(bitmap);
		
		
	}
	
}
