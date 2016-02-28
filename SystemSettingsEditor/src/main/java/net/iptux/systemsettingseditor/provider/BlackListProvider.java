package net.iptux.systemsettingseditor.provider;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

import java.util.HashMap;

public class BlackListProvider extends ContentProvider {

	public static final String AUTHORITY = "net.iptux.systemsettingseditor.provider.BlackListProvider";

	public static final class BlackList implements BaseColumns {
		private BlackList() {}
		public static final String TABLE_NAME = "blacklist";
		public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME);
		public static final Uri CONTENT_ID_URI_BASE = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME + "/");
		public static final String NAME = "name";
		public static final String VALUE = "value";

		public static final String DEFAULT_SORT_ORDER = _ID + " ASC";
	}

	class BlackListDatabaseHelper extends SQLiteOpenHelper {
		static final String DB_NAME = "blacklist.db";
		static final int DB_VERSION = 1;

		public BlackListDatabaseHelper(Context context) {
			super(context, DB_NAME, null, DB_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("CREATE TABLE " + BlackList.TABLE_NAME + " ("
				+ BlackList._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ BlackList.NAME + " TEXT UNIQUE ON CONFLICT REPLACE,"
				+ BlackList.VALUE + " TEXT"
				+ ");");
			db.execSQL("CREATE INDEX " + BlackList.TABLE_NAME + "Index1 ON "
				+ BlackList.TABLE_NAME + " (" + BlackList.NAME + ");");
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}
	}

	private final HashMap<String, String> mBlackListProjectionMap;
	private final UriMatcher mUriMatcher;
	private final int BLACK_LIST = 1;
	private final int BLACK_LIST_ID = 2;
	private final int BLACK_LIST_NAME = 3;

	private BlackListDatabaseHelper mDBHelper;

	public BlackListProvider() {
		mUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		mUriMatcher.addURI(AUTHORITY, BlackList.TABLE_NAME, BLACK_LIST);
		mUriMatcher.addURI(AUTHORITY, BlackList.TABLE_NAME + "/#", BLACK_LIST_ID);
		mUriMatcher.addURI(AUTHORITY, BlackList.TABLE_NAME + "/*", BLACK_LIST_NAME);

		mBlackListProjectionMap = new HashMap<>();
		mBlackListProjectionMap.put(BlackList._ID, BlackList._ID);
		mBlackListProjectionMap.put(BlackList.NAME, BlackList.NAME);
		mBlackListProjectionMap.put(BlackList.VALUE, BlackList.VALUE);
	}

	@Override
	public boolean onCreate() {
		mDBHelper = new BlackListDatabaseHelper(getContext());
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
						String[] selectionArgs, String sortOrder) {
		SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
		qb.setTables(BlackList.TABLE_NAME);

		switch (mUriMatcher.match(uri)) {
		case BLACK_LIST:
			qb.setProjectionMap(mBlackListProjectionMap);
			break;
		case BLACK_LIST_ID:
			qb.setProjectionMap(mBlackListProjectionMap);
			qb.appendWhere(BlackList._ID + "=?");
			selectionArgs = DatabaseUtils.appendSelectionArgs(selectionArgs, new String[] {uri.getLastPathSegment()});
			break;
		case BLACK_LIST_NAME:
			qb.setProjectionMap(mBlackListProjectionMap);
			qb.appendWhere(BlackList.NAME + "=?");
			selectionArgs = DatabaseUtils.appendSelectionArgs(selectionArgs, new String[] {uri.getLastPathSegment()});
			break;
		default:
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		if (TextUtils.isEmpty(sortOrder)) {
			sortOrder = BlackList.DEFAULT_SORT_ORDER;
		}
		SQLiteDatabase db = mDBHelper.getReadableDatabase();
		Cursor c = qb.query(db, projection, selection, selectionArgs, null, null, sortOrder);
		c.setNotificationUri(getContext().getContentResolver(), uri);
		return c;
	}

	@Override
	public String getType(Uri uri) {
		return null;
	}

	@Override
	public Uri insert(Uri uri, ContentValues initialValues) {
		if (BLACK_LIST != mUriMatcher.match(uri)) {
			throw new IllegalArgumentException("Unknown URI: " + uri);
		}

		ContentValues values;
		if (null != initialValues) {
			values = initialValues;
		} else {
			values = new ContentValues();
		}
		if (!values.containsKey(BlackList.NAME)) {
			throw new IllegalArgumentException("NAME is missing");
		}
		if (!values.containsKey(BlackList.VALUE)) {
			values.put(BlackList.VALUE, "");
		}

		SQLiteDatabase db = mDBHelper.getWritableDatabase();
		long rowId = db.insert(BlackList.TABLE_NAME, null, values);

		if (rowId > 0) {
			Uri blackListUri = ContentUris.withAppendedId(BlackList.CONTENT_ID_URI_BASE, rowId);
			getContext().getContentResolver().notifyChange(blackListUri, null);
			return blackListUri;
		}

		throw new SQLException("Failed to insert row into " + uri);
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SQLiteDatabase db = mDBHelper.getWritableDatabase();


		switch (mUriMatcher.match(uri)) {
		case BLACK_LIST:
			break;
		case BLACK_LIST_ID:
			selection = DatabaseUtils.concatenateWhere(BlackList._ID + " = " + ContentUris.parseId(uri), selection);
			break;
		case BLACK_LIST_NAME:
			selection = DatabaseUtils.concatenateWhere(BlackList.NAME + "=?", selection);
			selectionArgs = DatabaseUtils.appendSelectionArgs(selectionArgs, new String[] {uri.getLastPathSegment()});
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		int count = db.delete(BlackList.TABLE_NAME, selection, selectionArgs);;
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
					  String[] selectionArgs) {
		SQLiteDatabase db = mDBHelper.getWritableDatabase();

		switch (mUriMatcher.match(uri)) {
		case BLACK_LIST:
			break;
		case BLACK_LIST_ID:
			selection = DatabaseUtils.concatenateWhere(BlackList._ID + " = " + ContentUris.parseId(uri), selection);
			break;
		case BLACK_LIST_NAME:
			selection = DatabaseUtils.concatenateWhere(BlackList.NAME + "=?", selection);
			selectionArgs = DatabaseUtils.appendSelectionArgs(selectionArgs, new String[] {uri.getLastPathSegment()});
			break;
		default:
			throw new IllegalArgumentException("Unknown URI " + uri);
		}

		int count = db.update(BlackList.TABLE_NAME, values, selection, selectionArgs);
		getContext().getContentResolver().notifyChange(uri, null);
		return count;
	}
}
