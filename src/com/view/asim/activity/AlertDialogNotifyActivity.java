package com.view.asim.activity;

/**
 * 
 *@author xb
 * 
 */

import java.io.File;
import java.util.Calendar;

import com.view.asim.R;
import com.view.asim.comm.Constant;
import com.view.asim.model.UpgradeRule;
import com.view.asim.service.OTAService;
import com.view.asim.utils.FileUtil;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.DownloadManager.Request;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class AlertDialogNotifyActivity extends ActivitySupport {

	private Context context;
	private UpgradeRule upgradeRule = new UpgradeRule();
	private DownloadManager downloadManager;
	private static final int UPGRADE = 0;
	private static final int INSTALL = 1;
	private int mNotifyFlag = -1;
	private String mime;
	private Uri u;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTheme(R.style.Transparent);
		context = this;
		downloadManager = (DownloadManager) context
				.getSystemService(context.DOWNLOAD_SERVICE);

	}

	AlertDialog malert;

	private void dissMissDialog() {
		// TODO Auto-generated method stub
		malert.dismiss();
		malert = null;
		finish();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		mNotifyFlag = getIntent().getIntExtra("mNotifyFlag", -1);
		if (mNotifyFlag == UPGRADE) {
			upgradeRule = getIntent().getExtras().getParcelable("upgradeRule");
		} else if (mNotifyFlag == INSTALL) {
			upgradeRule = getIntent().getExtras().getParcelable("upgradeRule");
			mime = getIntent().getStringExtra("mime");
			u = getIntent().getExtras().getParcelable("u");
		}
		if (malert != null) {
			if (malert.isShowing()) {
				return;
			} else {
				malert = null;
			}
		}
		if (mNotifyFlag == UPGRADE) {
			AlertDialog mDialog = null;
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setIcon(android.R.drawable.ic_dialog_info);
			builder.setTitle(getResources().getString(R.string.version_update));
			builder.setMessage(getResources().getString(R.string.mixin_new_version) + " " + upgradeRule.getTgtVer()
					+ " " + getResources().getString(R.string.had_publish_whether_download));

			builder.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {

							Uri uri = Uri.parse("http://"
									+ Constant.OTA_STORAGE_HOST
									+ File.separator + upgradeRule.getName());
							DownloadManager.Request request = new Request(uri);
							request.setAllowedNetworkTypes(Request.NETWORK_WIFI);
							request.setTitle(getResources().getString(R.string.mixin_new_version) + " "
									+ upgradeRule.getTgtVer() + " " + getResources().getString(R.string.downloading));

							String filename = upgradeRule.getName().replace(
									".apk", "")
									+ "_"
									+ Calendar.getInstance().getTimeInMillis()
									+ ".apk";
							Uri localUri = Uri.parse("file://"
									+ FileUtil.getGlobalCachePath() + filename);
							request.setDestinationUri(localUri);
							long downloadId = downloadManager.enqueue(request);
							String status = "downloading";

							Intent downloading = new Intent();
							downloading.setClass(context, OTAService.class);
							downloading.putExtra("state", "downloading");
							downloading.putExtra("downloadId", downloadId);
							downloading.putExtra("filename", filename);

							context.startService(downloading);

							finish();
						}
					});
			builder.setNegativeButton(android.R.string.cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dissMissDialog();
							cancle();
						}
					});
			mDialog = builder.create();
			mDialog.show();
			mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					finish();
				}
			});
			malert = mDialog;

		} else if (mNotifyFlag == INSTALL) {

			AlertDialog mDialog = null;
			AlertDialog.Builder builder = new AlertDialog.Builder(context);
			builder.setIcon(android.R.drawable.ic_dialog_info);
			builder.setTitle(getResources().getString(R.string.version_install));
			builder.setMessage(getResources().getString(R.string.mixin_new_version) + " " + upgradeRule.getTgtVer()
					+ " " + getResources().getString(R.string.download_finish_whether_update_right_now));
			builder.setPositiveButton(android.R.string.ok,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {

							Intent intent = new Intent(Intent.ACTION_VIEW);
							intent.setDataAndType(u, mime);
							intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
							startActivity(intent);
							finish();
						}
					});
			builder.setNegativeButton(android.R.string.cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							dissMissDialog();
						}
					});
			mDialog = builder.create();
			mDialog.show();
			mDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {

				@Override
				public void onDismiss(DialogInterface dialog) {
					finish();
				}
			});
			malert = mDialog;
		}
	}

	private void cancle() {
		Intent cancledownload = new Intent();
		cancledownload.setClass(context, OTAService.class);
		cancledownload.putExtra("state", "cancledownload");
		context.startService(cancledownload);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		if (null != malert) {
			malert = null;
		}
	}
}
