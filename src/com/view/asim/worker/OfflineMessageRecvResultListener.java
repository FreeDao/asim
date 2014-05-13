package com.view.asim.worker;

import java.util.Iterator;
import java.util.List;

import com.view.asim.model.ChatMessage;

public interface OfflineMessageRecvResultListener {
    public void onRecvResult(List<org.jivesoftware.smack.packet.Message> msgs);
}
  
