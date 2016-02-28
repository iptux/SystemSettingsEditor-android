package net.iptux.systemsettingseditor.support;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import net.iptux.systemsettingseditor.model.SettingItem;
import net.iptux.systemsettingseditor.provider.BlackListProvider;


public final class BlackListUtility {
	public static
	Uri addToBlackList(Context context, SettingItem item, String reason) {
		if (TextUtils.isEmpty(reason)) {
			reason = "";
		}
		ContentValues values = new ContentValues();
		values.put(BlackListProvider.BlackList.NAME, item.name);
		values.put(BlackListProvider.BlackList.VALUE, reason);
		return context.getContentResolver().insert(BlackListProvider.BlackList.CONTENT_URI, values);
	}

	public static
	boolean removeFromBlackList(Context context, SettingItem item) {
		Uri uri = Uri.withAppendedPath(BlackListProvider.BlackList.CONTENT_URI, item.name);
		return context.getContentResolver().delete(uri, null, null) > 0;
	}

	public static
	boolean updateBlockReason(Context context, SettingItem item, String reason) {
		if (TextUtils.isEmpty(reason)) {
			reason = "";
		}
		Uri uri = Uri.withAppendedPath(BlackListProvider.BlackList.CONTENT_URI, item.name);
		ContentValues values = new ContentValues();
		values.put(BlackListProvider.BlackList.VALUE, reason);
		return context.getContentResolver().update(uri, values, null, null) > 0;
	}

	public static
	boolean isInBlackList(Context context, SettingItem item) {
		String selection = "? LIKE " + BlackListProvider.BlackList.NAME;
		String selectionArgs[] = {item.name};
		Cursor c = context.getContentResolver().query(BlackListProvider.BlackList.CONTENT_URI, null, selection, selectionArgs, null);
		return null != c && c.getCount() > 0;
	}
}
