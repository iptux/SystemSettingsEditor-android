package net.iptux.systemsettingseditor.support;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.provider.Settings;
import android.text.TextUtils;

import net.iptux.systemsettingseditor.model.SettingItem;


public final class SettingItemUtility {
	public static
	String extractSettingName(Uri uri) {
		return uri.getLastPathSegment();
	}

	public static
	boolean isSystemSettings(SettingItem item) {
		boolean b = Settings.AUTHORITY.equals(item.uri.getAuthority());
		if (b) {
			b = "system".equals(item.uri.getPathSegments().get(0));
		}
		return b;
	}

	public static
	boolean isEditable(SettingItem item) {
		return isSystemSettings(item);
	}

	public static
	boolean update(Context context, SettingItem item, String newValue) {
		if (TextUtils.isEmpty(newValue) || item.value.equals(newValue) || !isEditable(item)) {
			return false;
		}
		ContentResolver cr = context.getContentResolver();
		ContentValues values = new ContentValues(1);
		values.put(Settings.NameValueTable.VALUE, newValue);
		return cr.update(item.uri, values, null, null) > 0;
	}

	public static
	boolean delete(Context context, SettingItem item) {
		if (!isEditable(item)) {
			return false;
		}
		ContentResolver cr = context.getContentResolver();
		return cr.delete(item.uri, null, null) > 0;
	}

	public static
	void showAsToast(Context context, SettingItem item) {
		Utility.toastFormat(context, "uri=%s, id=%d, name=%s, value=%s", item.uri, item.id, item.name, item.value);
	}
}
