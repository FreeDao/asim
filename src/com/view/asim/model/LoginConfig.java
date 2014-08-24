package com.view.asim.model;

/**
 * 
 * ��¼����.
 * 
 * @author xuweinan
 */
public class LoginConfig {

	private Server server;
	private String uuid;
	private String username;  // �û���
	private String password;  // ����
	private String rootPath; // ��Ŀ¼
	private long loginTime;
	private boolean isOnline;   // �û����ӳɹ�connection

	public long getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(long loginTime) {
		this.loginTime = loginTime;
	}

	
	public LoginConfig() {
		server = new Server();
	}

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

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
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

	
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}
	
	@Override
	public String toString() {
		return "LoginConfig: server = [" + server + 
				"], username = " + username +
				", password = " + password + 
				", rootPath = " + rootPath + 
				", uuid = " + uuid +
				", isOnline = " + isOnline;
	}

}
