package net.iptux.systemsettingseditor.model;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;

public class SettingItem implements Parcelable {
	public final Uri uri;
	public final long id;
	public final String name;
	public final String value;

	public SettingItem(Uri uri, long id, String name, String value) {
		this.uri = uri;
		this.id = id;
		this.name = name;
		this.value = value;
	}

	protected SettingItem(Parcel in) {
		uri = in.readParcelable(Uri.class.getClassLoader());
		id = in.readLong();
		name = in.readString();
		value = in.readString();
	}

	public static
	SettingItem fromUri(ContentResolver cr, Uri uri) {
		if (null == cr || null == uri) {
			return null;
		}
		if (!Settings.AUTHORITY.equals(uri.getAuthority())) {
			return null;
		}
		Cursor cursor = cr.query(uri, null, null, null, null);
		if (null == cursor || cursor.getCount() <= 0) {
			// deleted item
			return null;
		}
		cursor.moveToNext();
		long id = cursor.getLong(cursor.getColumnIndex(Settings.NameValueTable._ID));
		String name = cursor.getString(cursor.getColumnIndex(Settings.NameValueTable.NAME));
		String value = cursor.getString(cursor.getColumnIndex(Settings.NameValueTable.VALUE));
		cursor.close();
		return new SettingItem(uri, id, name, value);
	}

	public static final Creator<SettingItem> CREATOR = new Creator<SettingItem>() {
		public SettingItem createFromParcel(Parcel in) {
			return new SettingItem(in);
		}

		public SettingItem[] newArray(int size) {
			return new SettingItem[size];
		}
	};

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeParcelable(uri, flags);
		dest.writeLong(id);
		dest.writeString(name);
		dest.writeString(value);
	}

	public String toString() {
		return String.format("%s=%s", name, value);
	}

	@Override
	public boolean equals(Object o) {
		return o instanceof SettingItem && uri.equals(((SettingItem) o).uri);
	}
}
