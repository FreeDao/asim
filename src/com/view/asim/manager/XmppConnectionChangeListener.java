package com.view.asim.manager;

import org.jivesoftware.smack.XMPPConnection;

public abstract class XmppConnectionChangeListener {
    
    public abstract void newConnection(XMPPConnection connection);

}
