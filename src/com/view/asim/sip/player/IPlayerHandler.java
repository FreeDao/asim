
package com.view.asim.sip.player;

import com.view.asim.sip.service.SipService.SameThreadException;

public interface IPlayerHandler {
    
    public void startPlaying() throws SameThreadException;

    public void stopPlaying() throws SameThreadException;
    
    
}
