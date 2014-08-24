package com.view.asim.worker;

import java.util.HashMap;
import java.util.Map;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.packet.Message;

import com.view.asim.manager.AUKeyManager;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.MessageManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.GroupUser;
import com.view.asim.model.CtrlMessage;
import com.view.asim.model.ChatMessage;
import com.view.asim.model.IMMessage;
import com.view.asim.model.User;
import com.view.asim.utils.DateUtil;

import android.util.Log;

/**
 * @author xuweinan
 *
 */
public class MessageDestroyReceiptsHandler implements BaseHandler {
	private final static String TAG = "MessageDestroyReceiptsHandler";
    
	private final ChatMessage mSendMsg; 
    private final Chat mChat;
                                                                                                                                                                                                                                                                          
    //通过构造函数传递所需的参数
    public MessageDestroyReceiptsHandler(ChatMessage msg){
    	mSendMsg = msg;
        mChat = XmppConnectionManager.getInstance().getConnection()
				.getChatManager().createChat(mSendMsg.getWith(), null);
    }
                                                                                                                                                                                                                                                                          
    @Override
    public void execute() {
    	Log.d(TAG, "Handler execute");
    	
		String time = DateUtil.getCurDateStr();
		
        try {        
			Message message = new Message();
			message.setProperty(IMMessage.PROP_ID, mSendMsg.getUniqueId());
			message.setProperty(IMMessage.PROP_TYPE, IMMessage.PROP_TYPE_CTRL);
			message.setProperty(IMMessage.PROP_TIME, time);
			message.setProperty(IMMessage.PROP_WITH, mSendMsg.getWith());
			message.setProperty(CtrlMessage.PROP_CTRL_MSGTYPE, CtrlMessage.DESTROY_RECEIPTS);

			message.setBody(mSendMsg.getUniqueId());

			mChat.sendMessage(message);
        }
        catch (Exception e) {         
            e.printStackTrace(); 
            return;
        }
    }
}
