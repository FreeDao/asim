package com.view.asim.sip.stack;

import java.io.File;
import java.lang.reflect.Field;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

import com.view.asim.utils.Compatibility;
import com.view.asim.utils.Log;
import com.view.asim.utils.PreferencesProviderWrapper;

public class NativeLibManager {
	private static final String THIS_FILE = "NativeLibMgr";
    public static final String STD_LIB_NAME = "stlport_shared";
	public static final String STACK_NAME = "pjsipjni";
	
	public static File getBundledStackLibFile(Context ctx, String libName) {
		PackageInfo packageInfo = PreferencesProviderWrapper.getCurrentPackageInfos(ctx);
		if(packageInfo != null) {
			ApplicationInfo appInfo = packageInfo.applicationInfo;
			File f = getLibFileFromPackage(appInfo, libName, true);
			return f;
		}
		
		// This is the very last fallback method
		return new File(ctx.getFilesDir().getParent(), "lib" + File.separator + libName);
	}
	
	public static File getLibFileFromPackage(ApplicationInfo appInfo, String libName, boolean allowFallback) {
		Log.v(THIS_FILE, "Dir "+appInfo.dataDir);
		if(Compatibility.isCompatible(9)) {
			try {
				Field f = ApplicationInfo.class.getField("nativeLibraryDir");
				File nativeFile = new File((String) f.get(appInfo), libName);
				if(nativeFile.exists()) {
					Log.v(THIS_FILE, "Found native lib using clean way");
					return nativeFile;
				}
			} catch (Exception e) {
				Log.e(THIS_FILE, "Cant get field for native lib dir", e);
			}
		}
		if(allowFallback) {
			return new File(appInfo.dataDir, "lib" + File.separator + libName);
		}else {
			return null;
		}
	}

	
	public static boolean isDebuggableApp(Context ctx) {
		try {
			PackageInfo pinfo = ctx.getPackageManager().getPackageInfo(ctx.getPackageName(), 0);
			return ( (pinfo.applicationInfo.flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0);
		} catch (NameNotFoundException e) {
			// Should not happen....or something is wrong with android...
			Log.e(THIS_FILE, "Not possible to find self name", e);
		}
		return false;
	}
	

	/**
	 * Return the complete path to stack lib if detectable (2.3 and upper)
	 * Return the short name of the library else (2.2 and lower)
	 * @param ctx Context 
	 * @return String library name to load through System.load();
	 */
	/*
	public static String getStackLib(Context ctx) {
		
		File f = NativeLibManager.getStackLibFile(ctx);
		if(f != null) {
			return f.getAbsolutePath();
		}
		
		return STACK_FILE_NAME;
	}
	 */
}
