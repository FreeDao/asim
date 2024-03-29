
package com.view.asim.sip.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.view.asim.sip.api.SipConfigManager;
import com.view.asim.sip.api.SipManager;
import com.view.asim.sip.api.SipProfile;
import com.view.asim.sip.service.SipService;
import com.view.asim.sip.service.SipService.SameThreadException;
import com.view.asim.sip.service.SipService.SipRunnable;
import com.view.asim.utils.Log;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class DynamicReceiver4 extends BroadcastReceiver {

    private static final String THIS_FILE = "DynamicReceiver";
    

    // Comes from android.net.vpn.VpnManager.java
    // Action for broadcasting a connectivity state.
    public static final String ACTION_VPN_CONNECTIVITY = "vpn.connectivity";
    /** Key to the connectivity state of a connectivity broadcast event. */
    public static final String BROADCAST_CONNECTION_STATE = "connection_state";
    
    private SipService service;
    
    
    // Store current state
    private String mNetworkType;
    private boolean mConnected = false;
    private String mRoutes = "";
    
    private boolean hasStartedWifi = false;


    private Timer pollingTimer;

    
    /**
     * Check if the intent received is a sticky broadcast one 
     * A compat way
     * @param it intent received
     * @return true if it's an initial sticky broadcast
     */
    public boolean compatIsInitialStickyBroadcast(Intent it) {
        if(ConnectivityManager.CONNECTIVITY_ACTION.equals(it.getAction())) {
            if(!hasStartedWifi) {
                hasStartedWifi = true;
                return true;
            }
        }
        return false;
    }
    
    public DynamicReceiver4(SipService aService) {
        service = aService;
    }

    @Override
    public void onReceive(final Context context, final Intent intent) {
        // Run the handler in SipServiceExecutor to be protected by wake lock
        service.getExecutor().execute(new SipRunnable()  {
            public void doRun() throws SameThreadException {
                onReceiveInternal(context, intent, compatIsInitialStickyBroadcast(intent));
            }
        });
    }
    
    

    /**
     * Internal receiver that will run on sip executor thread
     * @param context Application context
     * @param intent Intent received
     * @throws SameThreadException
     */
    private void onReceiveInternal(Context context, Intent intent, boolean isSticky) throws SameThreadException {
        String action = intent.getAction();
        Log.d(THIS_FILE, "Internal receive " + action);
        if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
            ConnectivityManager cm =
                    (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
            onConnectivityChanged(activeNetwork, isSticky);
        } else if (action.equals(SipManager.ACTION_SIP_ACCOUNT_CHANGED)) {
            final long accountId = intent.getLongExtra(SipProfile.FIELD_ID, SipProfile.INVALID_ID);
            // Should that be threaded?
            if (accountId != SipProfile.INVALID_ID) {
                final SipProfile account = service.getAccount(accountId);
                if (account != null) {
                    Log.d(THIS_FILE, "Enqueue set account registration");
                    service.setAccountRegistration(account, account.active ? 1 : 0, true);
                }
            }
        } else if (action.equals(SipManager.ACTION_SIP_ACCOUNT_DELETED)){
            final long accountId = intent.getLongExtra(SipProfile.FIELD_ID, SipProfile.INVALID_ID);
            if(accountId != SipProfile.INVALID_ID) {
                final SipProfile fakeProfile = new SipProfile();
                fakeProfile.id = accountId;
                service.setAccountRegistration(fakeProfile, 0, true);
            }
        } else if (action.equals(SipManager.ACTION_SIP_CAN_BE_STOPPED)) {
            service.cleanStop();
        } else if (action.equals(SipManager.ACTION_SIP_REQUEST_RESTART)){
            service.restartSipStack();
        } else if(action.equals(ACTION_VPN_CONNECTIVITY)) {
            onConnectivityChanged(null, isSticky);
        }
    }
    

    private static final String PROC_NET_ROUTE = "/proc/net/route";
    private String dumpRoutes() {
        String routes = "";
        FileReader fr = null;
        try {
            fr = new FileReader(PROC_NET_ROUTE);
            if(fr != null) {
                StringBuffer contentBuf = new StringBuffer();
                BufferedReader buf = new BufferedReader(fr);
                String line;
                while ((line = buf.readLine()) != null) {
                    contentBuf.append(line+"\n");
                }
                routes = contentBuf.toString();
                buf.close();
            }
        } catch (FileNotFoundException e) {
            Log.e(THIS_FILE, "No route file found routes", e);
        } catch (IOException e) {
            Log.e(THIS_FILE, "Unable to read route file", e);
        }finally {
            try {
                fr.close();
            } catch (IOException e) {
                Log.e(THIS_FILE, "Unable to close route file", e);
            }
        }
        
        // Clean routes that point unique host 
        // this aims to workaround the fact android 4.x wakeup 3G layer when position is retrieve to resolve over 3g position
        String finalRoutes = routes;
        if(!TextUtils.isEmpty(routes)) {
            String[] items = routes.split("\n");
            List<String> finalItems = new ArrayList<String>();
            int line = 0;
            for(String item : items) {
                boolean addItem = true;
                if(line > 0){
                    String[] ent = item.split("\t");
                    if(ent.length > 8) {
                        String maskStr = ent[7];
                        if(maskStr.matches("^[0-9A-F]{8}$")) {
                            int lastMaskPart = Integer.parseInt(maskStr.substring(0, 2), 16);
                            if(lastMaskPart > 192) {
                                // if more than 255.255.255.192 : ignore this line
                                addItem = false;
                            }
                        }else {
                            Log.w(THIS_FILE, "The route mask does not looks like a mask" + maskStr);
                        }
                    }
                }
                
                if(addItem) {
                    finalItems.add(item);
                }
                line ++;
            }
            finalRoutes = TextUtils.join("\n", finalItems); 
        }
        
        return finalRoutes;
    }

    
    /**
     * Treat the fact that the connectivity has changed
     * @param info Network info
     * @param incomingOnly start only if for outgoing 
     * @throws SameThreadException
     */
    private void onConnectivityChanged(NetworkInfo info, boolean isSticky) throws SameThreadException {
        // We only care about the default network, and getActiveNetworkInfo()
        // is the only way to distinguish them. However, as broadcasts are
        // delivered asynchronously, we might miss DISCONNECTED events from
        // getActiveNetworkInfo(), which is critical to our SIP stack. To
        // solve this, if it is a DISCONNECTED event to our current network,
        // respect it. Otherwise get a new one from getActiveNetworkInfo().
        if (info == null || info.isConnected() ||
                !info.getTypeName().equals(mNetworkType)) {
            ConnectivityManager cm = (ConnectivityManager) service.getSystemService(Context.CONNECTIVITY_SERVICE);
            info = cm.getActiveNetworkInfo();
        }

        boolean connected = (info != null && info.isConnected() && service.isConnectivityValid());
        String networkType = connected ? info.getTypeName() : "null";
        String currentRoutes = dumpRoutes();
        String oldRoutes;
        synchronized (mRoutes) {
            oldRoutes = mRoutes;
        }
        
        // Ignore the event if the current active network is not changed.
        if (connected == mConnected && networkType.equals(mNetworkType) && currentRoutes.equals(oldRoutes)) {
            return;
        }
        if(Log.getLogLevel() >= 4) {
            if(!networkType.equals(mNetworkType)) {
                Log.d(THIS_FILE, "onConnectivityChanged(): " + mNetworkType +
                            " -> " + networkType);
            }else {
                Log.d(THIS_FILE, "Route changed : "+ mRoutes+" -> "+currentRoutes);
            }
        }
        // Now process the event
        synchronized (mRoutes) {
            mRoutes = currentRoutes;
        }
        mConnected = connected;
        mNetworkType = networkType;

        if(!isSticky) {
            if (connected) {
                service.restartSipStack();
            } else {
                Log.d(THIS_FILE, "We are not connected, stop");
                if(service.stopSipStack()) {
                    service.stopSelf();
                }
            }
        }
    }
    
    
    
    public void startMonitoring() {
        int pollingIntervalMin = service.getPrefs().getPreferenceIntegerValue(SipConfigManager.NETWORK_ROUTES_POLLING);

        Log.d(THIS_FILE, "Start monitoring of route file ? " + pollingIntervalMin);
        if(pollingIntervalMin > 0) {
            pollingTimer = new Timer("RouteChangeMonitor", true);
            pollingTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    String currentRoutes = dumpRoutes();
                    String oldRoutes;
                    synchronized (mRoutes) {
                        oldRoutes = mRoutes;
                    }
                    if(!currentRoutes.equalsIgnoreCase(oldRoutes)) {
                        Log.d(THIS_FILE, "Route changed");
                        // Run the handler in SipServiceExecutor to be protected by wake lock
                        service.getExecutor().execute(new SipRunnable()  {
                            public void doRun() throws SameThreadException {
                                onConnectivityChanged(null, false);
                            }
                        });
                    }
                }
            }, new Date(), pollingIntervalMin * 60 * 1000);
        }
    }
    
    public void stopMonitoring() {
        if(pollingTimer != null) {
            pollingTimer.cancel();
            pollingTimer.purge();
            pollingTimer = null;
        }
    }
}
