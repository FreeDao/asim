package com.view.asim.worker;


import java.util.List;
import android.content.Context;
import android.util.Log;

import com.view.asim.comm.Constant;
import com.view.asim.model.UpgradeRule;
import com.view.asim.utils.ImageUtil;

/**
 * 检查 OTA 状态 handler
 * @author xuweinan
 *
 */
public class CheckOTAStatusHandler implements BaseHandler {
	private final static String TAG = "CheckOTAStatusHandler";
    
    private final String mVersion;
    private final OTACheckResultListener mListener;
    private final Context mCntx;

                                                                                                                                                                                                                                                                          
    public CheckOTAStatusHandler(Context cntx, String version, OTACheckResultListener listener){
    	mVersion = version;
        mListener  = listener;
        mCntx = cntx;
    }
                                                                                                                                                                                                                                                                          
    @Override
    public void execute() {
    	Log.d(TAG, "Handler execute");
		
		String verCheckUrl = "http://" + Constant.OTA_STORAGE_HOST + Constant.OTA_CHECK_STATUS_PATH;
		String verCheckJson = null;
		List<UpgradeRule> upgradeRules = null;
		
		byte[] fileBytes = ImageUtil.getBytesFromUrl(verCheckUrl, Constant.FILE_DOWNLOAD_TIMEOUT);
		if (fileBytes == null) {
			Log.e(TAG, "get ota check json failed.");
			mListener.onCheckResult(false, null);
			return;
		}
		
		verCheckJson = new String(fileBytes);
		upgradeRules = UpgradeRule.loads(verCheckJson);
		
		if (upgradeRules == null) {
			Log.e(TAG, "parse ota check json failed.");
			mListener.onCheckResult(false, null);
			return;
		}
		
		for (UpgradeRule rule: upgradeRules) {
			if (rule.getSrcVer().equals(mVersion)) {
				mListener.onCheckResult(true, rule);
				return;
			}
		}
    	
    	// 走到这里，说明没有找到匹配的升级规则
		mListener.onCheckResult(false, null);
    }
   
}
