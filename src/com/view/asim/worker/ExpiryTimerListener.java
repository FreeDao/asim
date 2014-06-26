package com.view.asim.worker;

public interface ExpiryTimerListener {
    public void onTick(int sec);
    public void onEnd();
}
