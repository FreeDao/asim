package com.view.asim.worker;

import com.view.asim.model.ChatMessage;

//�ص�������֪ͨ��Ϣ���ͽ��
public interface MessageSentResultListener {
    public void onSentResult(ChatMessage msgSent);

}
