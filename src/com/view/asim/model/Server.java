package com.view.asim.model;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

/**
 * 
 * 服务器配置
 * 
 * @author xuweinan
 */
public class Server {

	private String xmppHost;
	private Integer xmppPort;
	private String xmppServiceName;
	
	private String sipHost; 
	private Integer sipPort; 
	private String stunHost; 
	
	public Server() {
		xmppHost = null;
		xmppPort = -1;
		xmppServiceName = null;
		sipHost = null;
		sipPort = -1;
		stunHost = null;
	}

	public String getXmppHost() {
		return xmppHost;
	}

	public void setXmppHost(String xmppHost) {
		this.xmppHost = xmppHost;
	}

	public Integer getXmppPort() {
		return xmppPort;
	}

	public void setXmppPort(Integer xmppPort) {
		this.xmppPort = xmppPort;
	}

	public String getXmppServiceName() {
		return xmppServiceName;
	}

	public void setXmppServiceName(String xmppServiceName) {
		this.xmppServiceName = xmppServiceName;
	}
	
	public String getSipHost() {
		return sipHost;
	}

	public void setSipHost(String sipHost) {
		this.sipHost = sipHost;
	}

	public Integer getSipPort() {
		return sipPort;
	}

	public void setSipPort(Integer sipPort) {
		this.sipPort = sipPort;
	}

	public String getStunHost() {
		return stunHost;
	}

	public void setStunHost(String stunHost) {
		this.stunHost = stunHost;
	}
	
	@Override
	public String toString() {
		return "Server: xmppHost = " + xmppHost + 
				", xmppPort = " + xmppPort +
				", xmppServiceName = " + xmppServiceName + 
				", sipHost = " + sipHost + 
				", sipPort = " + sipPort + 
				", stunHost = " + stunHost;
	}
	
	public static Server loads(String json) {
		Server srv = new Server();
		
		if (json == null || json.length() == 0) {
			return null;
		}
		
		try {  
		    JSONTokener jsonParser = new JSONTokener(json);  
		    JSONObject resp = (JSONObject) jsonParser.nextValue();
		    
		    JSONObject sip = resp.getJSONObject("sip");
		    JSONObject xmpp = resp.getJSONObject("xmpp");
		    
		    srv.setXmppHost(xmpp.getString("ip"));
		    srv.setXmppServiceName(xmpp.getString("ip"));
		    srv.setXmppPort(xmpp.getInt("port"));
		    
		    srv.setSipHost(sip.getString("ip"));
		    srv.setSipPort(sip.getInt("port"));
		    srv.setStunHost(sip.getString("ip"));

		} catch (JSONException e) {  
			e.printStackTrace();
			return null;
		}

		return srv;	
	}

}
