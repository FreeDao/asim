package com.view.asim.worker;

public interface FileRecvResultListener {
    public void onRecvResult(boolean result);
    public void onRecvProgress(long cur, long total);
}
