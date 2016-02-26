package net.iptux.systemsettingseditor.support;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.widget.Toast;

import net.iptux.systemsettingseditor.BuildConfig;
import net.iptux.systemsettingseditor.service.SettingsMonitorService;

public final class Utility {
	public static
	void showToast(Context context, CharSequence text) {
		Toast.makeText(context, text, Toast.LENGTH_SHORT).show();
	}

	public static
	void toastFormat(Context context, String format, Object... args) {
		String text = String.format(format, args);
		showToast(context, text);
	}

	public static
	void toastFormat(Context context, int resId, Object... args) {
		showToast(context, stringFormat(context, resId, args));
	}

	public static
	String stringFormat(Context context, int resId, Object... args) {
		String format = context.getString(resId);
		return String.format(format, args);
	}

	public static
	String getVersionName() {
		return BuildConfig.VERSION_NAME;
	}

	public static
	SharedPreferences getDefaultPreferences(Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}

	public static
	boolean getBooleanPref(Context context, String key, boolean defaultValue) {
		return getDefaultPreferences(context).getBoolean(key, defaultValue);
	}

	public static
	boolean prefMonitor(Context context) {
		return getBooleanPref(context, Constants.PREF_MONITOR, true);
	}

	public static
	boolean prefShowToast(Context context) {
		return getBooleanPref(context, Constants.PREF_TOAST, true);
	}

	public static
	boolean prefShowNotification(Context context) {
		return getBooleanPref(context, Constants.PREF_NOTIFY, true);
	}

	public static
	boolean prefHighPriorityNotification(Context context) {
		return getBooleanPref(context, Constants.PREF_NOTIFY_HEADS_UP, true);
	}

	public static
	void setMonitorRunning(Context context, boolean running) {
		if (running) {
			startMonitor(context);
		} else {
			stopMonitor(context);
		}
	}

	public static
	void startMonitor(Context context) {
		context.startService(new Intent(context, SettingsMonitorService.class));
	}

	public static
	void stopMonitor(Context context) {
		context.stopService(new Intent(context, SettingsMonitorService.class));
	}
}