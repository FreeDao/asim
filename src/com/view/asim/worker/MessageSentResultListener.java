package com.view.asim.worker;

import com.view.asim.model.ChatMessage;

//回调函数，通知消息发送结果
public interface MessageSentResultListener {
    public void onSentResult(ChatMessage msgSent);

}
