package net.iptux.systemsettingseditor.support;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import net.iptux.systemsettingseditor.R;
import net.iptux.systemsettingseditor.model.SettingItem;
import net.iptux.systemsettingseditor.provider.BlackListProvider;


public final class BlackListUtility {
	public static
	Uri addToBlackList(Context context, String name, String reason) {
		if (TextUtils.isEmpty(reason)) {
			reason = "";
		}
		ContentValues values = new ContentValues();
		values.put(BlackListProvider.BlackList.NAME, name);
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

	public static AlertDialog getSimpleAddDialog(final Context context, CharSequence title, String message) {
		View view = LayoutInflater.from(context).inflate(R.layout.settings_edit_dialog, null);
		TextView textView = (TextView) view.findViewById(android.R.id.title);
		textView.setText(Html.fromHtml(message));
		final EditText editText = (EditText) view.findViewById(android.R.id.text1);
		return Utility.getSimpleEditTextDialog(context, title, view,
			new AlertDialog.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					addToBlackList(context, editText.getText().toString(), null);
				}
			}
		);
	}
}
