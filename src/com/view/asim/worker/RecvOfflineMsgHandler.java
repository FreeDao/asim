package com.view.asim.worker;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smackx.OfflineMessageManager;

import com.view.asim.manager.AUKeyManager;
import com.view.asim.manager.ContacterManager;
import com.view.asim.manager.MessageManager;
import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.model.ChatMessage;
import com.view.asim.model.IMMessage;
import com.view.asim.model.User;
import com.view.asim.utils.DateUtil;

import android.util.Log;

/**
 * 接收文本消息的 handler
 * @author xuweinan
 *
 */
public class RecvOfflineMsgHandler implements BaseHandler {
	private final static String TAG = "RecvOfflineMsgHandler";
    
    private final OfflineMessageRecvResultListener mListener;
                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               
    public RecvOfflineMsgHandler(OfflineMessageRecvResultListener listener){
        mListener  = listener;
    }
                                                                                                                                                                                                                                                                          
    @Override
    public void execute() {
    	Log.d(TAG, "Handler execute");
    	
    	OfflineMessageManager offlineManager = new OfflineMessageManager(
				XmppConnectionManager.getInstance().getConnection());
    	
    	Iterator<Message> it = null;
    	List<Message> msgs = new ArrayList<Message>();
		try {
			int count = offlineManager.getMessageCount();
			Log.i(TAG, "offline message num: " + count);

			if(count > 0) {
				it = offlineManager.getMessages();
				
				while (it.hasNext()) {
					msgs.add(it.next());
					Log.i(TAG, "offline messages: " + it.next());

				}
				offlineManager.deleteMessages();
			}

		} catch (XMPPException e) {
			e.printStackTrace();
		}
		    	
		mListener.onRecvResult(msgs);
    }
}
