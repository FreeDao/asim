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
import com.view.asim.util.DateUtil;

import android.util.Log;

/**
 * 邀请加入群聊组的 handler
 * @author xuweinan
 *
 */
public class InviteGroupChatHandler implements BaseHandler {
	private final static String TAG = "InviteGroupChatHandler";
    
    private final GroupUser mGroup;
    private final Map<String, Chat> mChats;

                                                                                                                                                                                                                                                                          
    //通过构造函数传递所需的参数
    public InviteGroupChatHandler(GroupUser grp){
    	mGroup = grp;
        mChats = new HashMap<String, Chat>();
        
		for (String member : mGroup.getGroupUsers()) {
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
		String groupJson = mGroup.dumps();
		
        try {        
    		for (String member: mGroup.getGroupUsers()) {

    			Message message = new Message();
    			message.setProperty(IMMessage.PROP_TYPE, IMMessage.PROP_TYPE_CTRL);
				message.setProperty(IMMessage.PROP_TIME, time);
				message.setProperty(IMMessage.PROP_WITH, mGroup.getName());
    			message.setProperty(CtrlMessage.PROP_CTRL_MSGTYPE, CtrlMessage.CTRL_GROUP_INVITE);
				message.setBody(groupJson);

    			mChats.get(member).sendMessage(message);
    		}
        }
        catch (Exception e) {         
            e.printStackTrace(); 
            return;
        }
    }
}
