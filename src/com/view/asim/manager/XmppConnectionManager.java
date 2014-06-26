package com.view.asim.manager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.jivesoftware.smack.AndroidConnectionConfiguration;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.ConnectionListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.filter.MessageTypeFilter;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.GroupChatInvitation;
import org.jivesoftware.smackx.PrivateDataManager;
import org.jivesoftware.smackx.packet.ChatStateExtension;
import org.jivesoftware.smackx.packet.LastActivity;
import org.jivesoftware.smackx.packet.OfflineMessageInfo;
import org.jivesoftware.smackx.packet.OfflineMessageRequest;
import org.jivesoftware.smackx.packet.SharedGroupsInfo;
import org.jivesoftware.smackx.ping.PingFailedListener;
import org.jivesoftware.smackx.ping.PingManager;
import org.jivesoftware.smackx.provider.DataFormProvider;
import org.jivesoftware.smackx.provider.DelayInformationProvider;
import org.jivesoftware.smackx.provider.DiscoverInfoProvider;
import org.jivesoftware.smackx.provider.DiscoverItemsProvider;
import org.jivesoftware.smackx.provider.MUCAdminProvider;
import org.jivesoftware.smackx.provider.MUCOwnerProvider;
import org.jivesoftware.smackx.provider.MUCUserProvider;
import org.jivesoftware.smackx.provider.MessageEventProvider;
import org.jivesoftware.smackx.provider.MultipleAddressesProvider;
import org.jivesoftware.smackx.provider.RosterExchangeProvider;
import org.jivesoftware.smackx.provider.StreamInitiationProvider;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.provider.XHTMLExtensionProvider;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;
import org.jivesoftware.smackx.search.UserSearch;

import com.view.asim.comm.Constant;
import com.view.asim.model.LoginConfig;

import android.content.Context;
import android.content.Intent;
import android.util.Log;


/**
 * 
 * XMPP服务器连接工具类.
 * 
 * @author xuweinan
 */
public class XmppConnectionManager {
	private final static String TAG = "XmppConnectionManager";
	
	public final static String CONNECTED = "xmpp.connected";
	public final static String WAIT_FOR_NETWORK = "xmpp.wait_for_network";
	public final static String CONNECTING = "xmpp.connecting";
	public final static String DISCONNECTED = "xmpp.disconnected";
    private static ReentrantLock lock = new ReentrantLock();

	/*
	static {     
        try{    
           Class.forName("org.jivesoftware.smack.ReconnectionManager");    
        }catch(Exception e){    
            e.printStackTrace();    
        }    
    }  
	*/
	
	private XMPPConnection connection;
	private static ConnectionConfiguration connectionConfig;
	private static XmppConnectionManager xmppConnectionManager;
    private final List<XmppConnectionChangeListener> mConnectionChangeListeners;
    private ConnectionListener mConnectionListener = null;    
    private Context context;
    private String status = DISCONNECTED;

	private XmppConnectionManager(Context cntx) {
        mConnectionChangeListeners = new ArrayList<XmppConnectionChangeListener>();
        context = cntx;
	}

	public static XmppConnectionManager getInstance(Context cntx) {
		if (xmppConnectionManager == null) {
			try {
                lock.lock();   //先上锁，来保证下面这个代码不会同时被执行
                if (xmppConnectionManager == null) {
					Log.i(TAG, "XmppConnectionManager instanced by context " + cntx);
					xmppConnectionManager = new XmppConnectionManager(cntx);
                }
			} finally {
				lock.unlock();
			}
		}
		return xmppConnectionManager;
	}

	public static XmppConnectionManager getInstance() {
		return xmppConnectionManager;
	}
	
	public void destroy() {
		xmppConnectionManager = null;
	}
	
    public void registerConnectionChangeListener(XmppConnectionChangeListener listener) {
        mConnectionChangeListeners.add(listener);
    }
    
	private XMPPConnection init(LoginConfig loginConfig) {
      
		try {
			Connection.DEBUG_ENABLED = false;
			ProviderManager pm = ProviderManager.getInstance();
			configure(pm);
			
			SmackConfiguration.setPacketReplyTimeout(2 * 60 * 1000);
	
			connectionConfig = new AndroidConnectionConfiguration(
					loginConfig.getXmppHost(), loginConfig.getXmppPort(),
					loginConfig.getXmppServiceName());
			connectionConfig.setSASLAuthenticationEnabled(false);
			connectionConfig.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);
			
			connectionConfig.setReconnectionAllowed(false);
			connectionConfig.setSocketFactory(XmppSocketFactory.getInstance());
			
			// 允许登陆成功后更新在线状态
			connectionConfig.setSendPresence(false);
			// 收到好友邀请后manual表示需要经过同意,accept_all表示不经同意自动为好友
			Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.manual);
			
			connection = new XMPPConnection(connectionConfig);
			if (connection == null) {
				Log.e(TAG, "init xmpp connection failed");
			}
			return connection;
		} catch (Exception xe) {
			xe.printStackTrace();
			return null;
		}
	}

	/**
	 * 
	 * 返回一个有效的xmpp连接,如果无效则返回空.
	 * 
	 * @return
	 * @author xuweinan
	 * @update 2012-7-4 下午6:54:31
	 */
	public XMPPConnection getConnection() {
		return connection;
	}

	public Roster getRoster() {
		if (connection != null) {
			return connection.getRoster();
		}
		else {
			return null;
		}
	}
	
	public void connect(LoginConfig loginCfg) {
		XMPPConnection conn;

        status = CONNECTING;

        if (connection == null || connection.isConnected() ) {
            try {
            	conn = init(loginCfg);
            } catch (Exception e) {
                Log.e(TAG, "Exception creating new XMPP Connection", e);
                maybeStartReconnect();
                return;
            }
            if (!connectAndAuth(conn, loginCfg)) {
                return;
            }                  
        } else {
        	conn = connection;
            if (!connectAndAuth(conn, loginCfg)) {
                return;
            }
        }
        onConnectionEstablished(conn);
	}
	
	
	public void connectOnly(LoginConfig loginCfg) {
		XMPPConnection conn;
        status = CONNECTING;

        if (connection == null || connection.isConnected() ) {
            try {
            	conn = init(loginCfg);
            } catch (Exception e) {
                Log.e(TAG, "Exception creating new XMPP Connection", e);
                maybeStartReconnect();
                return;
            }
            if (!connectAndAuth(conn, null)) {
                return;
            }                  
        } else {
        	conn = connection;
            if (!connectAndAuth(conn, null)) {
                return;
            }
        }
        status = CONNECTED;

	}
	
	private void maybeStartReconnect() {
        sendConnStatusBroadcast(CONNECTING);
	}
	
	private boolean connectAndAuth(XMPPConnection connection, LoginConfig cfg) {
        try {
            connection.connect();
        } catch (Exception e) {
            Log.w(TAG, "XMPP connection failed", e);
            if (e instanceof XMPPException) {
                Log.w(TAG, "XMPP connection failed because of stream error: " + e.getMessage());
            }
            status = DISCONNECTED;

            maybeStartReconnect();
            return false;
        }
        
        if(cfg != null) {
	        try {
	            connection.login(cfg.getUsername(), cfg.getPassword());
	        } catch (Exception e) {
	            Log.e(TAG, "Xmpp login failed", e);
	            if (e.getMessage().contains("Already")) {
	            	return true;
	            }
	            else {
		            maybeStartReconnect();
		            status = DISCONNECTED;
	            }

	            return false;
	        }
        }
        return true;
    }
	
	/**
	 * 
	 * 销毁xmpp连接.
	 * 
	 * @author xuweinan
	 */
	public void disconnect() {
		if (connection != null) {
            if (mConnectionListener != null) {
            	connection.removeConnectionListener(mConnectionListener);
            }
            if (connection.isConnected()) {
            	connection.disconnect();
                connection = null;
            }
        }
        mConnectionListener = null;
        status = DISCONNECTED;
        sendConnStatusBroadcast(DISCONNECTED);
	}

	public void reconnect(LoginConfig loginCfg) {
		Log.i(TAG, "reconnect xmpp: conn status " + status + ", connection isConnected " + (connection == null ? null : connection.isConnected()));
		
		if (status.equals(CONNECTING) || (connection != null && connection.isConnected())) {
			Log.w(TAG, "xmpp manager is connecting or connected");
			return;
		}
		disconnect();
		connect(loginCfg);
	}
	
	public void reconnectForcely(LoginConfig loginCfg) {
		Log.i(TAG, "reconnectForcely xmpp: conn status " + status + ", connection isConnected " + (connection == null ? null : connection.isConnected()));
		
		disconnect();
		connect(loginCfg);
	}


	
	private void onConnectionEstablished(XMPPConnection conn) {
		connection = conn;
        mConnectionListener = new ConnectionListener() {
            @Override
            public void connectionClosed() {
                Log.w(TAG, "ConnectionListener: connectionClosed() called - connection was shutdown by foreign host or by us");
                maybeStartReconnect();
            }

            @Override
            public void connectionClosedOnError(Exception e) {
                Log.w(TAG, "xmpp disconnected due to error: ", e);
                maybeStartReconnect();
            }

            @Override
            public void reconnectingIn(int arg0) {
                throw new IllegalStateException("Reconnection Manager is running");
            }

            @Override
            public void reconnectionFailed(Exception arg0) {
                throw new IllegalStateException("Reconnection Manager is running");
            }

            @Override
            public void reconnectionSuccessful() {
                throw new IllegalStateException("Reconnection Manager is running");
            }
        };
        connection.addConnectionListener(mConnectionListener);
        
        final DeliveryReceiptManager drm = DeliveryReceiptManager.getInstanceFor(connection);
        drm.enableAutoReceipts();
        
        Log.i(TAG, "connection established with parameters: con=" + connection.isConnected() + 
                " auth=" + connection.isAuthenticated() + 
                " enc=" + connection.isSecureConnection() +
                " comp=" + connection.isUsingCompression());
        
        status = CONNECTED;

        sendConnStatusBroadcast(CONNECTED);
    }
    
	
	private void sendConnStatusBroadcast(String status) {
		Log.i(TAG, "send connect status " +  status + " broadcast to all");
		Intent intent = new Intent(Constant.ACTION_RECONNECT_STATE);
		intent.putExtra(Constant.RECONNECT_STATE, status);
		context.sendBroadcast(intent);
	}
	
	private void configure(ProviderManager pm) {

		// Private Data Storage
		pm.addIQProvider("query", "jabber:iq:private",
				new PrivateDataManager.PrivateDataIQProvider());

		// Time
		try {
			pm.addIQProvider("query", "jabber:iq:time",
					Class.forName("org.jivesoftware.smackx.packet.Time"));
		} catch (ClassNotFoundException e) {
		}

		// XHTML
		pm.addExtensionProvider("html", "http://jabber.org/protocol/xhtml-im",
				new XHTMLExtensionProvider());

		// Roster Exchange
		pm.addExtensionProvider("x", "jabber:x:roster",
				new RosterExchangeProvider());
		// Message Events
		pm.addExtensionProvider("x", "jabber:x:event",
				new MessageEventProvider());
		// Chat State
		pm.addExtensionProvider("active",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("composing",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("paused",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("inactive",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());
		pm.addExtensionProvider("gone",
				"http://jabber.org/protocol/chatstates",
				new ChatStateExtension.Provider());

		// FileTransfer
		pm.addIQProvider("si", "http://jabber.org/protocol/si",
				new StreamInitiationProvider());

		// Group Chat Invitations
		pm.addExtensionProvider("x", "jabber:x:conference",
				new GroupChatInvitation.Provider());
		// Service Discovery # Items
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#items",
				new DiscoverItemsProvider());
		// Service Discovery # Info
		pm.addIQProvider("query", "http://jabber.org/protocol/disco#info",
				new DiscoverInfoProvider());
		// Data Forms
		pm.addExtensionProvider("x", "jabber:x:data", new DataFormProvider());
		// MUC User
		pm.addExtensionProvider("x", "http://jabber.org/protocol/muc#user",
				new MUCUserProvider());
		// MUC Admin
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#admin",
				new MUCAdminProvider());
		// MUC Owner
		pm.addIQProvider("query", "http://jabber.org/protocol/muc#owner",
				new MUCOwnerProvider());
		// Delayed Delivery
		pm.addExtensionProvider("x", "jabber:x:delay",
				new DelayInformationProvider());
		// Version
		try {
			pm.addIQProvider("query", "jabber:iq:version",
					Class.forName("org.jivesoftware.smackx.packet.Version"));
		} catch (ClassNotFoundException e) {
		}
		// VCard
		pm.addIQProvider("vCard", "vcard-temp", new VCardProvider());

		// Offline Message Requests
		pm.addIQProvider("offline", "http://jabber.org/protocol/offline",
				new OfflineMessageRequest.Provider());
		// Offline Message Indicator
		pm.addExtensionProvider("offline",
				"http://jabber.org/protocol/offline",
				new OfflineMessageInfo.Provider());
		// Last Activity
		pm.addIQProvider("query", "jabber:iq:last", new LastActivity.Provider());
		// User Search
		pm.addIQProvider("query", "jabber:iq:search", new UserSearch.Provider());
		// SharedGroupsInfo
		pm.addIQProvider("sharedgroup",
				"http://www.jivesoftware.org/protocol/sharedgroup",
				new SharedGroupsInfo.Provider());
		// JEP-33: Extended Stanza Addressing
		pm.addExtensionProvider("addresses",
				"http://jabber.org/protocol/address",
				new MultipleAddressesProvider());
		
		// JEP-0184: Delivery Receipts
		pm.addExtensionProvider(DeliveryReceipt.ELEMENT, DeliveryReceipt.NAMESPACE,  
		        new DeliveryReceipt.Provider());  
		pm.addExtensionProvider(DeliveryReceiptRequest.ELEMENT, DeliveryReceipt.NAMESPACE,  
		        new DeliveryReceiptRequest.Provider());  
	}
}
