package net.iptux.systemsettingseditor.support;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.provider.Settings;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import net.iptux.systemsettingseditor.R;
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

	public static
	AlertDialog getSimpleEditDialog(final Context context, final SettingItem item, CharSequence title, String message) {
		View view = LayoutInflater.from(context).inflate(R.layout.settings_edit_dialog, null);
		TextView textView = (TextView) view.findViewById(android.R.id.title);
		textView.setText(Html.fromHtml(message));
		final EditText editText = (EditText) view.findViewById(android.R.id.text1);
		editText.setText(item.value);
		return Utility.getSimpleEditTextDialog(context, title, view,
			new AlertDialog.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					update(context, item, editText.getText().toString());
				}
			},
			new AlertDialog.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					delete(context, item);
				}
			});
	}
}
