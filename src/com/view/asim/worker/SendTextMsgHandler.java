package com.view.asim.worker;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;

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
 * 发送一对一文本消息的 handler
 * @author xuweinan
 *
 */
public class SendTextMsgHandler implements BaseHandler {
	private final static String TAG = "SendTextMsgHandler";
    
	private final ChatMessage mSendMsg; 
    private final MessageSentResultListener mListener;
    private final Chat mChat;
                                                                                                                                                                                                                                                                          
    //通过构造函数传递所需的参数
    public SendTextMsgHandler(ChatMessage msg, MessageSentResultListener listener){
    	mSendMsg = msg;
        mListener  = listener;
        
        mChat = XmppConnectionManager.getInstance().getConnection()
				.getChatManager().createChat(mSendMsg.getWith(), null);
    }
                                                                                                                                                                                                                                                                          
    @Override
    public void execute() {
    	Log.d(TAG, "Handler execute");
    	
		/*
		ChatMessage newMessage = new ChatMessage();
		newMessage.setDir(IMMessage.SEND);
		newMessage.setFrom(ContacterManager.userMe.getJID());
		newMessage.setTo(mToUser.getJID());
		newMessage.setType(ChatMessage.CHAT_TEXT);
		newMessage.setDestroy(mDestroy);
		newMessage.setChatType(IMMessage.SINGLE);
		newMessage.setContent(mMsgContent);
		newMessage.setTime(time);
		*/
		
        try {        

    		boolean needEncr = AUKeyManager.getInstance().getAUKeyStatus().equals(AUKeyManager.ATTACHED);
    		
			Message message = new Message();
			message.setProperty(IMMessage.PROP_ID, mSendMsg.getUniqueId());
			message.setProperty(IMMessage.PROP_TYPE, IMMessage.PROP_TYPE_CHAT);
			message.setProperty(IMMessage.PROP_TIME, mSendMsg.getTime());
			message.setProperty(IMMessage.PROP_WITH, mSendMsg.getWith());
			message.setProperty(IMMessage.PROP_DESTROY, mSendMsg.getDestroy());
			message.setProperty(IMMessage.PROP_CHATTYPE, IMMessage.SINGLE);
			
			if (needEncr) {
				User u = ContacterManager.contacters.get(mSendMsg.getWith());
				message.setProperty(IMMessage.PROP_SECURITY, IMMessage.ENCRYPTION);
				String encrContent = AUKeyManager.getInstance().encryptData(u.getPublicKey(), mSendMsg.getContent());
				message.setBody(encrContent);
			}
			else {
				message.setProperty(IMMessage.PROP_SECURITY, IMMessage.PLAIN);
				message.setBody(mSendMsg.getContent());
			}
			
			//newMessage.setSecurity(needEncr ? IMMessage.ENCRYPTION: IMMessage.PLAIN);
			message.setProperty(ChatMessage.PROP_IM_MSGTYPE, ChatMessage.CHAT_TEXT);

			DeliveryReceiptManager.addDeliveryReceiptRequest(message);
			mChat.sendMessage(message);
        }
        catch (Exception e) {         
            e.printStackTrace(); 
            mSendMsg.setStatus(IMMessage.ERROR);
    		mListener.onSentResult(mSendMsg);
    		return;
    		//MessageManager.getInstance().saveIMMessage(newMessage);
        }
        
		//MessageManager.getInstance().saveIMMessage(newMessage);
        mSendMsg.setStatus(IMMessage.SUCCESS);
		mListener.onSentResult(mSendMsg);
    }
}
