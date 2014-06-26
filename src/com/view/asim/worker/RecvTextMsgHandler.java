package com.view.asim.worker;

import org.jivesoftware.smack.packet.Message;

import com.view.asim.manager.AUKeyManager;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.MessageManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.ChatMessage;
import com.view.asim.model.IMMessage;
import com.view.asim.model.User;
import com.view.asim.util.DateUtil;

import android.util.Log;

/**
 * 接收文本消息的 handler
 * @author xuweinan
 *
 */
public class RecvTextMsgHandler implements BaseHandler {
	private final static String TAG = "RecvTextMsgHandler";
    
    private final Message mRawMsg;
    private final MessageRecvResultListener mListener;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
    public RecvTextMsgHandler(Message rawMsg, MessageRecvResultListener listener){
    	mRawMsg = rawMsg;
        mListener  = listener;
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
		}
		else {
			with = from;
		}
		
		ChatMessage newMessage = new ChatMessage();
		newMessage.setUniqueId(uniqueId);
		newMessage.setDir(IMMessage.RECV);
		newMessage.setFrom(from);
		newMessage.setWith(with);
		newMessage.setType(ChatMessage.CHAT_TEXT);
		newMessage.setDestroy(destroy);
		newMessage.setChatType(chatType);
		newMessage.setContent(content);
		newMessage.setSecurity(security);
		newMessage.setStatus(IMMessage.SUCCESS);
		
		if(time == null || time.length() == 0) {
			time = DateUtil.getCurDateStr();
		}
		newMessage.setTime(time);

        
		mListener.onRecvResult(newMessage);
    }
}
