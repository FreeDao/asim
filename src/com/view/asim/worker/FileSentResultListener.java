package com.view.asim.worker;

import com.view.asim.model.ChatMessage;

public interface FileSentResultListener {
    public void onSentResult(ChatMessage msgSent);
    public void onSentProgress(long cur, long total);
}
