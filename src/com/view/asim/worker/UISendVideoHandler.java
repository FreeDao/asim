package com.view.asim.worker;

import java.io.File;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;

import com.view.asim.comm.Constant;
import com.view.asim.manager.AUKeyManager;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.MessageManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.Attachment;
import com.view.asim.model.ChatMessage;
import com.view.asim.model.IMMessage;
import com.view.asim.model.User;
import com.view.asim.util.DateUtil;
import com.view.asim.util.FileUtil;
import com.view.asim.util.ImageUtil;
import com.view.asim.util.StringUtil;

import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore.Images;
import android.util.Log;

/**
 * UI 界面发送视频文件的 handler
 * @author xuweinan
 *
 */
public class UISendVideoHandler implements BaseHandler {
	private final static String TAG = "UISendVideoHandler";
    
	private final String with; 
	private final String videoUri; 
	private final String destroy; 
	private final String chatType; 

	private final MessageSentResultListener mListener;
                                                                                                                                                                                                                                                                          
    public UISendVideoHandler(String with, String videoUri, String destroy, String chatType, MessageSentResultListener listener){
    	this.with = with;
    	this.videoUri = videoUri;
    	this.destroy = destroy;
    	this.chatType = chatType;
    	mListener  = listener;
    }
                                                                                                                                                                                                                                                                          
    @Override
    public void execute() {
    	Log.d(TAG, "Handler execute");
    	
    	boolean needEncr = AUKeyManager.getInstance().getAUKeyStatus().equals(AUKeyManager.ATTACHED);

		String time = DateUtil.getCurDateStr();
		
		ChatMessage newMessage = new ChatMessage();
		newMessage.setDir(IMMessage.SEND);
		newMessage.setFrom(ContacterManager.userMe.getJID());
		newMessage.setType(ChatMessage.CHAT_VIDEO);
		newMessage.setDestroy(destroy);
		newMessage.setContent("你发了一段视频");
		newMessage.setTime(time);
		newMessage.setSecurity(needEncr ? IMMessage.ENCRYPTION: IMMessage.PLAIN);
		newMessage.setChatType(chatType);
		newMessage.setWith(with);
		
		Attachment att = new Attachment();
		att.setSrcUri(videoUri);
		
		Bitmap bm = null;
		String videoPath = FileUtil.getVideoPathByWith(with);
		FileUtil.createDir(new File(videoPath));
		String thumbFile = videoPath + FileUtil.genCaptureVideoThumbName(with);
		try {
			bm = ImageUtil.getVideoThumbnail(videoUri, Images.Thumbnails.MINI_KIND);
			ImageUtil.saveImage(bm, thumbFile);
			att.setThumbUri(thumbFile);
		}
		catch (Exception e) {
			e.printStackTrace();
			newMessage.setStatus(IMMessage.ERROR);
		}
		
		newMessage.setAttachment(att);
		newMessage.setUniqueId(String.valueOf(newMessage.hashCode()));

		mListener.onSentResult(newMessage);
    }
}
