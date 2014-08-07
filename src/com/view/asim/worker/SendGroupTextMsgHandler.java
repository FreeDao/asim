package com.view.asim.worker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;

import com.view.asim.manager.AUKeyManager;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.MessageManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.GroupUser;
import com.view.asim.model.ChatMessage;
import com.view.asim.model.IMMessage;
import com.view.asim.model.User;
import com.view.asim.utils.DateUtil;

import android.util.Log;

/**
 * 发送群聊文本消息的 handler
 * @author xuweinan
 *
 */
public class SendGroupTextMsgHandler implements BaseHandler {
	private final static String TAG = "SendGroupTextMsgHandler";
    
	private final ChatMessage mSendMsg;
    private final GroupUser mGroupUser;
    private final MessageSentResultListener mListener;
    private final Map<String, Chat> mChats;
                                                                                                                                                                                                                                                                          
    //通过构造函数传递所需的参数
    public SendGroupTextMsgHandler(ChatMessage msg, MessageSentResultListener listener){
    	mSendMsg = msg;
    	mGroupUser = ContacterManager.groupUsers.get(msg.getWith());
        mListener  = listener;
        
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
    	
		String time = DateUtil.getCurDateStr();
        
		/*
		ChatMessage newMessage = new ChatMessage();
		newMessage.setDir(IMMessage.SEND);
		newMessage.setFrom(ContacterManager.userMe.getJID());
		newMessage.setTo(mGroupUser.getName());
		newMessage.setType(ChatMessage.CHAT_TEXT);
		newMessage.setDestroy(mDestroy);
		newMessage.setChatType(IMMessage.GROUP);
		newMessage.setContent(mMsgContent);
		newMessage.setTime(time);
		*/
		
        try {        

    		boolean needEncr = AUKeyManager.getInstance().getAUKeyStatus().equals(AUKeyManager.ATTACHED);
    		
    		for (String member: mGroupUser.getGroupUsers()) {

    			Message message = new Message();
    			message.setProperty(IMMessage.PROP_ID, mSendMsg.getUniqueId());
    			message.setProperty(IMMessage.PROP_TYPE, IMMessage.PROP_TYPE_CHAT);
    			message.setProperty(IMMessage.PROP_TIME, time);
    			message.setProperty(IMMessage.PROP_WITH, mGroupUser.getName());
    			message.setProperty(IMMessage.PROP_DESTROY, mSendMsg.getDestroy());
    			message.setProperty(IMMessage.PROP_CHATTYPE, IMMessage.GROUP);
    			
    			if (needEncr) {
    				message.setProperty(IMMessage.PROP_SECURITY, IMMessage.ENCRYPTION);
    				User u = ContacterManager.contacters.get(member);
    				if (u == null) {
    					Log.d(TAG, "send message to unknown user " + member);
    					continue;
    				}
    				String encrContent = AUKeyManager.getInstance().encryptData(u.getPublicKey(), mSendMsg.getContent());
    				message.setBody(encrContent);
    			}
    			else {
    				message.setProperty(IMMessage.PROP_SECURITY, IMMessage.PLAIN);
    				message.setBody(mSendMsg.getContent());
    			}
    			mChats.get(member).sendMessage(message);
    		}
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
