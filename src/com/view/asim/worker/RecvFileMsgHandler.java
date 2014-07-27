package com.view.asim.worker;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import org.jivesoftware.smack.packet.Message;

import com.nostra13.universalimageloader.core.assist.ImageSize;
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
import com.view.asim.util.FileUtil;
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
		String uniqueId = (String) mRawMsg.getProperty(IMMessage.PROP_ID);

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
		newMessage.setUniqueId(uniqueId);
		newMessage.setDir(IMMessage.RECV);
		newMessage.setFrom(from);
		newMessage.setWith(with);
		newMessage.setDestroy(destroy);
		newMessage.setChatType(chatType);
		newMessage.setSecurity(security);
//		if(time == null || time.length() == 0) {
//			time = DateUtil.getCurDateStr();
//		}
		newMessage.setTime(DateUtil.getCurDateStr());
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
			mListener.onRecvResult(newMessage);
			return;
		}
        
		newMessage.setStatus(IMMessage.SUCCESS);
		mListener.onRecvResult(newMessage);
    }
    
    private void downloadVideo(Attachment att, ChatMessage msg) {
		String downloadUrl = "http://" + Constant.FILE_STORAGE_HOST + "/" + att.getKey();
		String downloadThumbUrl = "http://" + Constant.FILE_STORAGE_HOST + "/" + att.getThumbKey();

		msg.setContent(mNickName + "������һ����Ƶ");
		
//		// ͨ����ţ API ������Ƶ����ʾ�ߴ�
//		String avInfoUrl = downloadUrl + "?avinfo";
//		byte[] fileBytes = ImageUtil.getBytesFromUrl(avInfoUrl, Constant.FILE_DOWNLOAD_TIMEOUT);
//		if (fileBytes == null) {
//	        throw new RuntimeException("get video info failed");
//		}
//		String videoInfo = new String(fileBytes);
//		ImageSize is = ImageUtil.getVideoDisplaySizeByAvInfo(videoInfo);
//		if (is == null) {
//	        throw new RuntimeException("parse video info for display size failed: " + videoInfo);
//		}
//		
//		// ������ʾ�ߴ�ȥ��ȡ��Ƶ��ͼ
//		String thumbUrl = downloadUrl + "?vframe/jpg/offset/0/w/" + is.getWidth() + "/h/" + is.getHeight();
		
		String fileSavePath = FileUtil.getVideoPathByWith(msg.getWith());
		File dir = new File(fileSavePath);
		FileUtil.createDir(dir);
		
		String thumbSaveName = fileSavePath + att.getThumbKey();//FileUtil.genRecvVideoThumbName(msg.getWith());
		
		/*
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
		*/
				
		// Ĭ��ֻ���ز���������ͼ��ԭ��Ƶ�����Ԥ��ʱ�����ز�����
		try {
			Bitmap thumbBm = ImageUtil.getBitmapFromUrl(downloadThumbUrl, Constant.FILE_DOWNLOAD_TIMEOUT);
			if (thumbBm == null) {
		        throw new RuntimeException("download video thumb file failed");
			}
			
			ImageUtil.saveImage(thumbBm, thumbSaveName);
		} catch (Exception e) {
			e.printStackTrace();
			msg.setStatus(IMMessage.ERROR);
		}
		
		// srcUri �ﱣ����Ƿ��ͷ��ı���·�������ڽ��շ������壬�����ÿգ�������������Ƶԭ�ļ������غ����
		att.setSrcUri("");
		att.setThumbUri(thumbSaveName);
		
		msg.setAttachment(att);
    }
    
    private void downloadImage(Attachment att, ChatMessage msg) {
		String downloadUrl = "http://" + Constant.FILE_STORAGE_HOST + "/" + att.getKey();
		
		msg.setContent(mNickName + "������һ��ͼƬ");

		// ����ͼ�޶��������200px
		String thumbUrl = downloadUrl + "?imageView2/0/h/200";
		
		String fileSavePath = FileUtil.getImagePathByWith(msg.getWith());
		File dir = new File(fileSavePath);
		FileUtil.createDir(dir);

		//String fileSaveName = fileSavePath + FileUtil.genRecvImageName(msg.getWith());
		String thumbSaveName = fileSavePath + FileUtil.genRecvImageThumbName(msg.getWith());
		
		/*
		// ���ز�����ԭͼ
		Bitmap srcBm = ImageUtil.getBitmapFromUrl(downloadUrl, Constant.FILE_DOWNLOAD_TIMEOUT);
		if (srcBm == null) {
	        throw new RuntimeException("download image file failed");
		}
		ImageUtil.saveImage(srcBm, fileSaveName);
		*/
		
		// Ĭ��ֻ���ز���������ͼ��ԭͼ�����Ԥ��ʱ�����ز�����
		Bitmap thumbBm = ImageUtil.getBitmapFromUrl(thumbUrl, Constant.FILE_DOWNLOAD_TIMEOUT);
		if (thumbBm == null) {
	        throw new RuntimeException("download image thumb file failed");
		}
		ImageUtil.saveImage(thumbBm, thumbSaveName);
		
		// srcUri �ﱣ����Ƿ��ͷ��ı���·�������ڽ��շ������壬�����ÿգ�����������ԭͼ�����غ����
		att.setSrcUri("");
		att.setThumbUri(thumbSaveName);
		
		msg.setAttachment(att);

    }
    
    private void downloadFile(String type, Attachment att, ChatMessage msg) {
		String downloadUrl = "http://" + Constant.FILE_STORAGE_HOST + "/" + att.getKey();
		String fileSavePath = null;
		
		if (type.equals(ChatMessage.CHAT_AUDIO)) {
			msg.setContent(mNickName + "������һ������");
			fileSavePath = FileUtil.getAudioPathByWith(msg.getWith());

		}
		else {
			msg.setContent(mNickName + "������һ���ļ�");
			fileSavePath = FileUtil.getFilePathByWith(msg.getWith());

		}
		File dir = new File(fileSavePath);
		FileUtil.createDir(dir);
		String fileSaveName = fileSavePath + FileUtil.genRecvAudioName(msg.getWith());
		
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
