package net.iptux.systemsettingseditor.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;

import net.iptux.systemsettingseditor.model.SettingItem;
import net.iptux.systemsettingseditor.R;
import net.iptux.systemsettingseditor.activity.SettingsListActivity;
import net.iptux.systemsettingseditor.support.Constants;
import net.iptux.systemsettingseditor.support.SettingItemUtility;
import net.iptux.systemsettingseditor.support.Utility;

import java.util.ArrayList;
import java.util.List;


public class SettingsMonitorService extends Service {
	NotificationManager mNM;
	ContentResolver mContentResolver;
	SettingsObserver mSettingsObserver;
	Handler mHandler;
	ArrayList<SettingItem> mModifiedItems;
	int mLastModifiedSize = 0;

	@Override
	public void onCreate() {
		mHandler = new Handler();
		mSettingsObserver = new SettingsObserver(mHandler);
		mContentResolver = getContentResolver();
		mContentResolver.registerContentObserver(Settings.System.CONTENT_URI, true, mSettingsObserver);
		mNM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		mModifiedItems = new ArrayList<>(10);
	}

	@Override
	public void onDestroy() {
		mContentResolver.unregisterContentObserver(mSettingsObserver);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (null != intent) {
			int operation = intent.getIntExtra(Constants.EXTRA_OPERATION, 0);
			switch (operation) {
			case Constants.OPERATION_CLEARED:
				mModifiedItems.clear();
				updateNotification();
				break;
			default:
				break;
			}
		}
		return Service.START_NOT_STICKY;
	}

	class SettingsObserver extends ContentObserver {
		SettingsObserver(Handler handler) {
			super(handler);
		}

		@Override
		public void onChange(boolean selfChange) {
			onChange(selfChange, null);
		}

		@Override
		public void onChange(boolean selfChange, Uri uri) {
			SettingItem item = SettingItem.fromUri(mContentResolver, uri);
			if (null != item) {
				// ignore known system settings
				if (SettingItemUtility.isSystemSettings(item)) {
					return;
				}
				showChangedInfo(item);
			}
			else {
				String name = SettingItemUtility.extractSettingName(uri);
				removeItemByName(name);
				Utility.toastFormat(SettingsMonitorService.this, R.string.setting_deleted, name);
			}
			updateNotification();
		}
	}

	void showChangedInfo(SettingItem item) {
		if (Utility.prefShowToast(this)) {
			SettingItemUtility.showAsToast(this, item);
		}
		if (Utility.prefShowNotification(this)) {
			addSettingItem(item);
		}
	}

	void removeItemByName(String name) {
		for (int i = mModifiedItems.size() - 1; i >= 0; i--) {
			// start from last, so it's safe to remove
			if (mModifiedItems.get(i).name.equals(name)) {
				mModifiedItems.remove(i);
				break;
			}
		}
	}

	void addSettingItem(SettingItem item) {
		removeItemByName(item.name);
		mModifiedItems.add(item);
	}

	void updateNotification() {
		if (mModifiedItems.size() > 0) {
			mNM.notify(Constants.NOTIFICATION_MODIFIED,
				getItemNotification(this, mModifiedItems));
		} else {
			mNM.cancel(Constants.NOTIFICATION_MODIFIED);
		}
		mLastModifiedSize = mModifiedItems.size();
	}

	Notification getItemNotification(Context context, List<SettingItem> items) {
		String title = context.getString(R.string.notify_settings_modified_title);
		PendingIntent contentIntent = PendingIntent.getActivity(
			context, 0, new Intent(context, SettingsListActivity.class), PendingIntent.FLAG_UPDATE_CURRENT);
		SettingItem last = items.get(items.size() - 1);
		String text = last.toString();
		Notification.Builder builder = new Notification.Builder(context)
			.setSmallIcon(android.R.drawable.stat_sys_warning)
			.setContentTitle(title)
			.setContentText(text)
			.setContentIntent(contentIntent)
			.setNumber(items.size())
			.setDeleteIntent(getClearIntent(context))
			.addAction(android.R.drawable.ic_menu_delete,
				context.getString(R.string.delete),
				getDeleteItemIntent(context, last)
			)
			.addAction(android.R.drawable.ic_menu_close_clear_cancel,
				context.getString(R.string.text_block),
				getBlockItemIntent(context, last)
			)
			;
		if (items.size() > 1) {
			Notification.InboxStyle style = new Notification.InboxStyle(builder)
				.setBigContentTitle(title)
				.setSummaryText(Utility.stringFormat(context, R.string.notify_settings_modified_summary, items.size()));
			for (int i = items.size() - 1; i >= 0; i--) {
				style.addLine(items.get(i).toString());
			}
			builder.setStyle(style);
		} else {
			Notification.BigTextStyle style = new Notification.BigTextStyle(builder)
				.bigText(text);
			builder.setStyle(style);
		}
		if (items.size() > mLastModifiedSize && Utility.prefHighPriorityNotification(context)) {
			builder.setFullScreenIntent(contentIntent, true);
		}
		return builder.build();
	}

	PendingIntent getDeleteItemIntent(Context context, SettingItem item) {
		Intent intent = new Intent(context, SettingsListActivity.class);
		intent.putExtra(Constants.EXTRA_OPERATION, Constants.OPERATION_DELETE);
		intent.putExtra(Constants.EXTRA_SETTING_ITEM, item);
		return PendingIntent.getActivity(context, Constants.OPERATION_DELETE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	PendingIntent getBlockItemIntent(Context context, SettingItem item) {
		Intent intent = new Intent(context, SettingsListActivity.class);
		intent.putExtra(Constants.EXTRA_OPERATION, Constants.OPERATION_BLOCK);
		intent.putExtra(Constants.EXTRA_SETTING_ITEM, item);
		return PendingIntent.getActivity(context, Constants.OPERATION_BLOCK, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}

	PendingIntent getClearIntent(Context context) {
		Intent intent = new Intent(context, SettingsMonitorService.class);
		intent.putExtra(Constants.EXTRA_OPERATION, Constants.OPERATION_CLEARED);
		return PendingIntent.getService(context, Constants.OPERATION_CLEARED, intent, PendingIntent.FLAG_UPDATE_CURRENT);
	}
}
