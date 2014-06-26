package com.view.asim.worker;

import com.view.asim.model.UpgradeRule;

public interface OTACheckResultListener {
    public void onCheckResult(boolean needUpgrade, UpgradeRule rule);
}
  
