
package com.view.asim.sip.player;

import com.view.asim.sip.api.SipCallSession;
import com.view.asim.sip.api.SipManager;
import com.view.asim.sip.service.SipService.SameThreadException;

import org.pjsip.pjsua.pjsua;
import org.pjsip.pjsua.pjsuaConstants;

import java.io.IOException;

public class SimpleWavPlayerHandler implements IPlayerHandler {

    private final SipCallSession callInfo;
    private final int way;
    private final int playerId;

    public SimpleWavPlayerHandler(SipCallSession callInfo, String filePath, int way) throws SameThreadException, IOException {
        this.callInfo = callInfo;
        this.way = way;
        
        int[] plId = new int[1];
        int status = pjsua.player_create(pjsua.pj_str_copy(filePath), 1 /* PJMEDIA_FILE_NO_LOOP */,
                plId);

        if (status == pjsuaConstants.PJ_SUCCESS) {
            // Save player
            playerId = plId[0];
        } else {
            throw new IOException("Cannot create player " + status);
        }
    }

    @Override
    public void startPlaying() throws SameThreadException {

        // Connect player to requested ports
        int wavPort = pjsua.player_get_conf_port(playerId);
        if ((way & SipManager.BITMASK_OUT) == SipManager.BITMASK_OUT) {
            int wavConfPort = callInfo.getConfPort();
            pjsua.conf_connect(wavPort, wavConfPort);
        }
        if ((way & SipManager.BITMASK_IN) == SipManager.BITMASK_IN) {
            pjsua.conf_connect(wavPort, 0);
        }
        // Once connected, start to play
        pjsua.player_set_pos(playerId, 0);
    }

    @Override
    public void stopPlaying() throws SameThreadException {
        pjsua.player_destroy(playerId);
    }

}
