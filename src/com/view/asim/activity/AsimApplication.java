package com.view.asim.activity;

import java.util.LinkedList;
import java.util.List;

import com.view.asim.manager.XmppConnectionManager;
import com.view.asim.util.CrashHandler;

import android.app.Activity;
import android.app.Application;
import android.app.Service;

/**
 * 
 * �������˳�Ӧ��.
 * 
 * @author xuweinan
 */
public class AsimApplication extends Application {
	private List<Activity> activityList = new LinkedList<Activity>();
	private List<Service> serviceList = new LinkedList<Service>();

	@Override  
    public void onCreate() {  
        super.onCreate();
        
        CrashHandler crashHandler = CrashHandler.getInstance();  
        crashHandler.init(this);  
    }  
	
	// ���Activity��������
	public void addActivity(Activity activity) {
		activityList.add(activity);
	}
	
	// ���Service��������
	public void addService(Service service) {
		serviceList.add(service);
	}

	// ��ȫ���׵��˳����� App
	public void exit() {
		XmppConnectionManager.getInstance().disconnect();
		
		for (Activity activity : activityList) {
			activity.finish();
		}
		
		for (Service service : serviceList) {
			service.stopSelf();
		}
	}
}
