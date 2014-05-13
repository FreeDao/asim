package com.view.asim.worker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.jivesoftware.smack.packet.Message;

import com.view.asim.comm.Constant;
import com.view.asim.manager.AUKeyManager;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.MessageManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.Attachment;
import com.view.asim.model.ChatMessage;
import com.view.asim.model.CtrlMessage;
import com.view.asim.model.IMMessage;
import com.view.asim.model.User;
import com.view.asim.util.DateUtil;
import com.view.asim.util.ImageUtil;
import com.view.asim.util.StringUtil;

import android.content.Context;
import android.graphics.Bitmap;
import android.provider.SyncStateContract.Constants;
import android.util.Log;

/**
 * �����ļ���Ϣ�� handler
 * @author xuweinan
 *
 */
public class RecvFileMsgHandler implements BaseHandler {
	private final static String TAG = "RecvFileMsgHandler";
    
    private final Message mRawMsg;
    private final MessageRecvResultListener mListener;
    private final Context mCntx;
    private String mNickName = "";

                                                                                                                                                                                                                                                                          
    public RecvFileMsgHandler(Context cntx, Message rawMsg, MessageRecvResultListener listener){
    	mRawMsg = rawMsg;
        mListener  = listener;
        mCntx = cntx;
    }
                                                                                                                                                                                                                                                                          
    @Override
    public void execute() {
    	Log.d(TAG, "Handler execute");
		String from = mRawMsg.getFrom().split("/")[0];
		String time = (String) mRawMsg.getProperty(IMMessage.PROP_TIME);
		String with = (String) mRawMsg.getProperty(IMMessage.PROP_WITH);
		String destroy = (String) mRawMsg.getProperty(IMMessage.PROP_DESTROY);
		String security = (String) mRawMsg.getProperty(IMMessage.PROP_SECURITY);
		String chatType = (String) mRawMsg.getProperty(IMMessage.PROP_CHATTYPE);
		String recvType = (String) mRawMsg.getProperty(CtrlMessage.PROP_CTRL_MSGTYPE);
		
		String content = null;
		
		if (security.equals(IMMessage.ENCRYPTION)) {
			content = AUKeyManager.getInstance().decryptData(mRawMsg.getBody());
		} else {
			content = mRawMsg.getBody();
		}
		
		if(chatType.equals(IMMessage.GROUP)) {
			if(!ContacterManager.groupUsers.containsKey(with)) {
				Log.e(TAG, "recv unknown group text msg(" + with + ")");
				mListener.onRecvResult(null);
				return;
			}
			//nickName = 
		}
		else {
			with = from;
			mNickName = ContacterManager.contacters.get(from).getNickName();
		}
		
		ChatMessage newMessage = new ChatMessage();
		newMessage.setDir(IMMessage.RECV);
		newMessage.setFrom(from);
		newMessage.setWith(with);
		newMessage.setDestroy(destroy);
		newMessage.setChatType(chatType);
		//newMessage.setContent(mNickName + "������һ��ͼƬ");
		newMessage.setSecurity(security);
		if(time == null || time.length() == 0) {
			time = DateUtil.getCurDateStr();
		}
		newMessage.setTime(time);
		newMessage.setType(recvType);

		Log.d(TAG, "recv file handler content " + content);
		Attachment att = Attachment.loads(content);
		
		try {
			if (recvType.equals(CtrlMessage.CTRL_SEND_IMG)) {
				downloadImage(att, newMessage);
			} else if  (recvType.equals(CtrlMessage.CTRL_SEND_VIDEO)) {
				downloadVideo(att, newMessage);
	
			} else {
				downloadFile(recvType, att, newMessage);
			}
		} catch (Exception e) {
			e.printStackTrace();
			newMessage.setStatus(IMMessage.ERROR);
		}
        
		mListener.onRecvResult(newMessage);
    }
    
    private void downloadVideo(Attachment att, ChatMessage msg) {
		String downloadUrl = "http://" + Constant.FILE_STORAGE_HOST + "/" + att.getKey();
		//String queryInfoUrl = downloadUrl + "?avinfo";
		
		//String vInfo = new String(ImageUtil.getBytesFromUrl(queryInfoUrl, Constant.FILE_DOWNLOAD_TIMEOUT));

		msg.setContent(mNickName + "������һ����Ƶ");

		String thumbUrl = downloadUrl + "?vframe/jpg/offset/1/w/200/h/200";
		
		String fileSavePath = Constant.SDCARD_ROOT_PATH + Constant.VIDEO_PATH + "/" + StringUtil.getUserNameByJid(msg.getWith());
		String fileSaveName = fileSavePath + "/" + att.getKey() 
				+ Constant.FILE_SUFFIX;
		String thumbSaveName = fileSavePath+ "/" + att.getKey() 
				+ Constant.THUMB_SUFFIX;
		
		File dir = new File(fileSavePath);
		if(!dir.exists()) {
			dir.mkdir();
		}
		
		// ���ز�����ԭ��Ƶ
		byte[] fileBytes = ImageUtil.getBytesFromUrl(downloadUrl, Constant.FILE_DOWNLOAD_TIMEOUT);
		if (fileBytes == null) {
	        throw new RuntimeException("download file failed");
		}
		
		try {
			ImageUtil.saveFile(mCntx, fileBytes, fileSaveName);
		} catch (IOException e) {
			e.printStackTrace();
			msg.setStatus(IMMessage.ERROR);
			return;
		}
		
				
		// ���ز���������ͼ
		Bitmap thumbBm = ImageUtil.getBitmapFromUrl(thumbUrl, Constant.FILE_DOWNLOAD_TIMEOUT);
		if (thumbBm == null) {
	        throw new RuntimeException("download image thumb file failed");
		}
		
		ImageUtil.saveImage(thumbBm, thumbSaveName);
		
		att.setSrcUri(fileSaveName);
		att.setThumbUri(thumbSaveName);
		
		msg.setAttachment(att);
    }
    
    private void downloadImage(Attachment att, ChatMessage msg) {
		String downloadUrl = "http://" + Constant.FILE_STORAGE_HOST + "/" + att.getKey();
		
		msg.setContent(mNickName + "������һ��ͼƬ");

		// ����ͼ�޶��������200px
		String thumbUrl = downloadUrl + "?imageView2/0/w/200";
		
		String fileSavePath = Constant.SDCARD_ROOT_PATH + Constant.IMAGE_PATH + "/" + 
				StringUtil.getUserNameByJid(msg.getWith());
		String fileSaveName = fileSavePath + "/" + att.getKey()
				+ Constant.FILE_SUFFIX;
		String thumbSaveName = fileSavePath + "/" + att.getKey()
				+ Constant.THUMB_SUFFIX;
		
		File dir = new File(fileSavePath);
		if(!dir.exists()) {
			dir.mkdir();
		}
		
		// ���ز�����ԭͼ
		Bitmap srcBm = ImageUtil.getBitmapFromUrl(downloadUrl, Constant.FILE_DOWNLOAD_TIMEOUT);
		if (srcBm == null) {
	        throw new RuntimeException("download image file failed");
		}
		ImageUtil.saveImage(srcBm, fileSaveName);
		
		// ���ز���������ͼ
		Bitmap thumbBm = ImageUtil.getBitmapFromUrl(thumbUrl, Constant.FILE_DOWNLOAD_TIMEOUT);
		if (thumbBm == null) {
	        throw new RuntimeException("download image thumb file failed");
		}
		ImageUtil.saveImage(thumbBm, thumbSaveName);
		
		att.setSrcUri(fileSaveName);
		att.setThumbUri(thumbSaveName);
		
		msg.setAttachment(att);

    }
    
    private void downloadFile(String type, Attachment att, ChatMessage msg) {
		String downloadUrl = "http://" + Constant.FILE_STORAGE_HOST + "/" + att.getKey();
		String fileSavePath = null;
		
		if (type.equals(ChatMessage.CHAT_AUDIO)) {
			msg.setContent(mNickName + "������һ������");
			fileSavePath = Constant.SDCARD_ROOT_PATH + Constant.AUDIO_PATH + "/" + StringUtil.getUserNameByJid(msg.getWith());

		}
		else {
			msg.setContent(mNickName + "������һ���ļ�");
			fileSavePath = Constant.SDCARD_ROOT_PATH + Constant.FILE_PATH + "/" + StringUtil.getUserNameByJid(msg.getWith());

		}

		String fileSaveName = fileSavePath + "/" + att.getKey();
		File dir = new File(fileSavePath);
		if(!dir.exists()) {
			dir.mkdir();
		}
		
		byte[] fileBytes = ImageUtil.getBytesFromUrl(downloadUrl, Constant.FILE_DOWNLOAD_TIMEOUT);
		if (fileBytes == null) {
	        throw new RuntimeException("download file failed");
		}
		
		try {
			ImageUtil.saveFile(mCntx, fileBytes, fileSaveName);
		} catch (IOException e) {
			e.printStackTrace();
			msg.setStatus(IMMessage.ERROR);
			return;
		}
		
		att.setSrcUri(fileSaveName);

		msg.setAttachment(att);
    }
}
