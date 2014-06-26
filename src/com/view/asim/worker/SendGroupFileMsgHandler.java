package com.view.asim.worker;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.json.JSONException;
import org.json.JSONObject;

import com.view.asim.qiniu.auth.JSONObjectRet;
import com.view.asim.qiniu.io.IO;
import com.view.asim.qiniu.io.PutExtra;
import com.view.asim.comm.Constant;
import com.view.asim.manager.AUKeyManager;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.MessageManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.Attachment;
import com.view.asim.model.ChatMessage;
import com.view.asim.model.CtrlMessage;
import com.view.asim.model.GroupUser;
import com.view.asim.model.IMMessage;
import com.view.asim.model.User;
import com.view.asim.util.DateUtil;
import com.view.asim.util.StringUtil;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

/**
 * 发送一对一图片消息的 handler
 * @author xuweinan
 *
 */
public class SendGroupFileMsgHandler implements BaseHandler {
	private final static String TAG = "SendGroupFileMsgHandler";
    
	private final ChatMessage mSendMsg;
    private final GroupUser mGroupUser;
    private final FileSentResultListener mListener;
    private final Map<String, Chat> mChats;
    private final Context mCntx;
    
	private String uptoken = null;
	private String accessKey = "2h4PWQ6V8_OJcMvejQvaa-tFm1-oRcCTNMZ5q_KG";
	private String secretKey = "hxUxU1W37BkNqF--CU48EZwg32ep2Oy9fIAdsIQQ";
                                                                                                                                                                                                                                                                              
    //通过构造函数传递所需的参数
    public SendGroupFileMsgHandler(Context cntx, ChatMessage msg, FileSentResultListener listener){
    	mSendMsg = msg;
        mListener  = listener;
        mCntx = cntx;
    	mGroupUser = ContacterManager.groupUsers.get(msg.getWith());
        
        mChats = new HashMap<String, Chat>();
        
		for (String member : mGroupUser.getGroupUsers()) {
			User u = ContacterManager.contacters.get(member);
			Chat chat = XmppConnectionManager.getInstance().getConnection()
					.getChatManager().createChat(u.getJID(), null);
			mChats.put(member, chat);
		} 
    }
                                                                                                                                                                                                                                                                          
    @Override
    public void execute() {
    	Log.d(TAG, "Handler execute");
    	
    	try {
			initToken();
		} catch (JSONException e) {
			e.printStackTrace();
			return;
		}
    	doUpload(Uri.parse(mSendMsg.getAttachment().getSrcUri()));
 
    }
    
	private void initToken() throws JSONException {
		long deadline = (long) Calendar.getInstance().getTimeInMillis() / 1000 + 3600;
		JSONObject policy = new JSONObject();
		policy.put("scope", "com-viewiot-mobile-asim");
		policy.put("deadline", deadline);
		policy.put("returnBody", 
				"{\"key\":$(key),\"hash\":$(etag),\"size\":$(fsize),\"mimeType\":$(mimeType),\"width\":$(imageInfo.width),\"height\":$(imageInfo.height)}");
		policy.put("detectMime", 0);

		String policyJson = policy.toString().trim();
		
		byte[] policyJsonBase64 = Base64.encode(policyJson.getBytes(), Base64.URL_SAFE);
		String p = new String(policyJsonBase64).trim();

		String sign = null;
		try {
			sign = hmacSha1(p, secretKey);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}		
		uptoken = accessKey + ":" + sign + ":" + p;
	}
	
	private String hmacSha1(String value, String key)
	        throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
	    String type = "HmacSHA1";
	    SecretKeySpec secret = new SecretKeySpec(key.getBytes(), type);
	    Mac mac = Mac.getInstance(type);
	    mac.init(secret);
	    byte[] bytes = mac.doFinal(value.getBytes());
	    byte[] result = Base64.encode(bytes, Base64.URL_SAFE);     
	    return new String(result).trim();
	}

	private void doUpload(Uri uri) {
		
		final boolean needEncr = AUKeyManager.getInstance().getAUKeyStatus().equals(AUKeyManager.ATTACHED);
		final Uri localUri = uri;

		/*
		newMessage.setDir(IMMessage.SEND);
		newMessage.setTo(mGroup.getName());
		newMessage.setType(mSendType);
		newMessage.setTime(time);
		newMessage.setChatType(IMMessage.GROUP);
		newMessage.setDestroy(mDestroy);
		newMessage.setSecurity(needEncr ? IMMessage.ENCRYPTION: IMMessage.PLAIN);
		*/
		
		/*
		String key = null;
		ContentResolver cr = mCntx.getContentResolver();
		Cursor cursor = cr.query(uri, null, null, null, null);

		if (cursor != null) {
			cursor.moveToFirst();
			// Column 2 = _display_name
			key = cursor.getString(2);
		}
		else {
			key = uri.getPath().substring(uri.getPath().lastIndexOf("/") + 1, uri.getPath().length());
		}
		*/
		
		String key = uri.getPath().substring(uri.getPath().lastIndexOf("/") + 1, uri.getPath().length());
		
		key = StringUtil.getUserNameByJid(ContacterManager.userMe.getJID()) + "_" + key;
		
		PutExtra extra = new PutExtra();
		extra.params = new HashMap<String, String>();
		
		Log.d(TAG, "upload file = " + uri.toString());
		IO.putFile(mCntx, uptoken, key, uri, extra, new JSONObjectRet() {
			@Override
			public void onProcess(long current, long total) {
				mListener.onSentProgress(current, total);
			}

			@Override
			public void onSuccess(JSONObject resp) {

				Attachment attach = Attachment.loads(resp);
				
				mSendMsg.getAttachment().setHash(attach.getHash());
				mSendMsg.getAttachment().setKey(attach.getKey());
				mSendMsg.getAttachment().setMimeType(attach.getMimeType());
				mSendMsg.getAttachment().setWidth(attach.getWidth());
				mSendMsg.getAttachment().setHeight(attach.getHeight());
				mSendMsg.getAttachment().setSize(attach.getSize());

				try {
		    		for (String member: mGroupUser.getGroupUsers()) {
						Message message = new Message();
						message.setProperty(IMMessage.PROP_ID, mSendMsg.getUniqueId());
						message.setProperty(IMMessage.PROP_TYPE, IMMessage.PROP_TYPE_CTRL);
						message.setProperty(IMMessage.PROP_TIME, mSendMsg.getTime());
						message.setProperty(IMMessage.PROP_WITH, mSendMsg.getWith());
						message.setProperty(IMMessage.PROP_DESTROY, mSendMsg.getDestroy());
		    			message.setProperty(IMMessage.PROP_CHATTYPE, IMMessage.SINGLE);
		    			
		    			if (needEncr) {
		    				message.setProperty(IMMessage.PROP_SECURITY, IMMessage.ENCRYPTION);
		    				User u = ContacterManager.contacters.get(member);
		    				if (u == null) {
		    					Log.d(TAG, "send message to unknown user " + member);
		    					continue;
		    				}
		    				String encrContent = AUKeyManager.getInstance().encryptData(u.getPublicKey(), resp.toString());
		    				message.setBody(encrContent);
		    			}
		    			else {
		    				message.setProperty(IMMessage.PROP_SECURITY, IMMessage.PLAIN);
		    				message.setBody(resp.toString());
		    			}
		    			
		    			message.setProperty(CtrlMessage.PROP_CTRL_MSGTYPE, mSendMsg.getType());
		    			mChats.get(member).sendMessage(message);
		    		}
					
				} catch (XMPPException e) {
					mSendMsg.setStatus(IMMessage.ERROR);
					mListener.onSentResult(mSendMsg);
					return;
				}

				mSendMsg.setStatus(IMMessage.SUCCESS);
				mListener.onSentResult(mSendMsg);
			}

			@Override
			public void onFailure(Exception ex) {
				mSendMsg.setStatus(IMMessage.ERROR);
				mListener.onSentResult(mSendMsg);
			}
		});
	}
}
