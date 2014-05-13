package com.view.asim.model;

/**
 * 
 * 登录配置.
 * 
 * @author xuweinan
 */
public class LoginConfig {

	private String xmppHost;// 地址
	private Integer xmppPort;// 端口
	private String xmppServiceName;// 服务器名称
	
	private String username;  // 用户名
	private String password;  // 密码
	private String sessionId; // 会话id
	private String rootPath; // 根目录
	private boolean isOnline;   // 用户连接成功connection

	public String getRootPath() {
		return rootPath;
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}


	public boolean isOnline() {
		return isOnline;
	}

	public void setOnline(boolean isOnline) {
		this.isOnline = isOnline;
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

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

}
