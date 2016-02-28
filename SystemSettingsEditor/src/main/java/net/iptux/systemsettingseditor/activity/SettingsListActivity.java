package net.iptux.systemsettingseditor.activity;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.app.ActionBar;
import android.app.Activity;
import android.provider.Settings;
import android.text.Html;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import net.iptux.systemsettingseditor.model.SettingItem;
import net.iptux.systemsettingseditor.R;
import net.iptux.systemsettingseditor.provider.BlackListProvider;
import net.iptux.systemsettingseditor.service.SettingsMonitorService;
import net.iptux.systemsettingseditor.support.BlackListUtility;
import net.iptux.systemsettingseditor.support.Constants;
import net.iptux.systemsettingseditor.support.SettingItemUtility;
import net.iptux.systemsettingseditor.support.Utility;


public class SettingsListActivity extends Activity
		implements
		LoaderManager.LoaderCallbacks<Cursor>,
		AdapterView.OnItemClickListener,
		AdapterView.OnItemLongClickListener,
		ActionMode.Callback,
		ActionBar.OnNavigationListener {

	private static final String ARG_CONTENT_URI = "uri";
	private static final String ARG_SORT_ORDER = "order";

	private static final int ORDER_ID_DESC = 0;
	private static final int ORDER_NAME_ASC = 1;

	private static final String[] ORDER_STRINGS_ARRAY = {
		Settings.NameValueTable._ID + " DESC",
		Settings.NameValueTable.NAME + " ASC",
	};

	private static final int SYSTEM_SETTINGS_URI_INDEX = 0;
	private static final int GLOBAL_SETTINGS_URI_INDEX = 2;
	private static final int BLACK_LIST_URI_INDEX = 3;
	private static final Uri[] SETTINGS_URI_ARRAY = {
		Settings.System.CONTENT_URI,
		Settings.Secure.CONTENT_URI,
		Settings.Global.CONTENT_URI,
		BlackListProvider.BlackList.CONTENT_URI,
	};

	ListView mListView;
	SimpleCursorAdapter mAdapter;
	SpinnerAdapter mSpinnerAdapter;
	ActionMode mActionMode = null;
	SettingItem mCurrentItem = null;
	int mSettingsUriIndex = SYSTEM_SETTINGS_URI_INDEX;
	int mSortOrder = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings_list);

		final ActionBar actionBar = getActionBar();
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);

		mSpinnerAdapter = ArrayAdapter.createFromResource(this,
			R.array.settings_type_array, android.R.layout.simple_spinner_dropdown_item);
		actionBar.setListNavigationCallbacks(mSpinnerAdapter, this);

		mListView = (ListView) findViewById(android.R.id.list);
		mListView.setOnItemClickListener(this);
		mListView.setOnItemLongClickListener(this);

		String[] fromColumns = {Settings.NameValueTable.NAME, Settings.NameValueTable.VALUE};
		int[] toViews = {android.R.id.title, android.R.id.text1};
		mAdapter = new SimpleCursorAdapter(this, R.layout.setting_item, null, fromColumns, toViews, 0);
		mListView.setAdapter(mAdapter);

		getLoaderManager().initLoader(android.R.id.list, null, this);
	}

	@Override
	protected void onStart() {
		super.onStart();
		startService(new Intent(this, SettingsMonitorService.class));
	}

	@Override
	protected void onResume() {
		super.onResume();
		handleIntent(getIntent());
	}

	void handleIntent(Intent intent) {
		if (null == intent)
			return;

		int operation = intent.getIntExtra(Constants.EXTRA_OPERATION, 0);
		SettingItem item = intent.getParcelableExtra(Constants.EXTRA_SETTING_ITEM);
		switch (operation) {
		case Constants.OPERATION_DELETE:
			getDeleteConfirmDialog(this, item).show();
			break;
		case Constants.OPERATION_BLOCK:
			getBlockConfirmDialog(this, item).show();
			break;
		default:
			break;
		}
	}

	private Bundle buildLoaderArgs(Uri uri, int order) {
		Bundle args = new Bundle();
		args.putParcelable(ARG_CONTENT_URI, uri);
		args.putInt(ARG_SORT_ORDER, order);
		return args;
	}

	private void reloadLoader() {
		Bundle args = buildLoaderArgs(SETTINGS_URI_ARRAY[mSettingsUriIndex], mSortOrder);
		getLoaderManager().restartLoader(android.R.id.list, args, this);
	}

	// LoaderManager.LoaderCallbacks<D>.onCreateLoader
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		Uri uri = Settings.System.CONTENT_URI;
		int order = ORDER_ID_DESC;
		if (null != args) {
			uri = args.getParcelable(ARG_CONTENT_URI);
			order = args.getInt(ARG_SORT_ORDER);
		}
		return new CursorLoader(this, uri, null, null, null, ORDER_STRINGS_ARRAY[order]);
	}

	// LoaderManager.LoaderCallbacks<D>.onLoadFinished
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		mAdapter.swapCursor(data);
	}

	// LoaderManager.LoaderCallbacks<D>.onLoaderReset
	public void onLoaderReset(Loader<Cursor> loader) {
		mAdapter.swapCursor(null);
	}

	// ActionBar.OnNavigationListener.onNavigationItemSelected
	public boolean onNavigationItemSelected(int itemPosition, long itemId) {
		if (GLOBAL_SETTINGS_URI_INDEX == itemPosition
				&& Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
			Utility.toastFormat(this, R.string.no_global_settings_message);
			return false;
		}
		mSettingsUriIndex = itemPosition;
		reloadLoader();
		return true;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_sort_order:
			switch (mSortOrder) {
			case ORDER_ID_DESC:
				mSortOrder = ORDER_NAME_ASC;
				item.setIcon(android.R.drawable.ic_menu_sort_by_size);
				break;
			case ORDER_NAME_ASC:
				mSortOrder = ORDER_ID_DESC;
				item.setIcon(android.R.drawable.ic_menu_sort_alphabetically);
				break;
			}
			reloadLoader();
			break;
		case R.id.menu_preferences:
			startActivity(new Intent(this, SettingsActivity.class));
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	// ActionMode.Callback.onCreateActionMode
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		int menuResid = R.menu.settingslist_context_menu;
		if (mSettingsUriIndex == BLACK_LIST_URI_INDEX) {
			menuResid = R.menu.blocklist_context_menu;
		}
		mode.getMenuInflater().inflate(menuResid, menu);
		return true;
	}

	// ActionMode.Callback.onPrepareActionMode
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		return false;
	}

	// ActionMode.Callback.onActionItemClicked
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_add:
			break;
		case R.id.menu_block:
			getBlockConfirmDialog(this, mCurrentItem).show();
			break;
		case R.id.menu_edit:
			getEditDialog(this, mCurrentItem).show();
			break;
		case R.id.menu_delete:
			getDeleteConfirmDialog(this, mCurrentItem).show();
			break;
		default:
			return false;
		}
		mode.finish();
		return true;
	}

	// ActionMode.Callback.onDestroyActionMode
	public void onDestroyActionMode(ActionMode mode) {
		mListView.setItemChecked(mListView.getCheckedItemPosition(), false);
		mActionMode = null;
		mCurrentItem = null;
	}

	// AdapterView.OnItemLongClickListener.onItemLongClick
	public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
		if (mSettingsUriIndex != SYSTEM_SETTINGS_URI_INDEX
			&& mSettingsUriIndex != BLACK_LIST_URI_INDEX
			) {
			return false;
		}
		if (null != mActionMode) {
			return false;
		}

		mActionMode = startActionMode(this);
		mCurrentItem = getItemFromView(view, id);
		mListView.setItemChecked(position, true);
		SettingItemUtility.showAsToast(this, mCurrentItem);
		return true;
	}

	// AdapterView.OnItemClickListener.onItemClick
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		SettingItem item = getItemFromView(view, id);
		if (SYSTEM_SETTINGS_URI_INDEX == mSettingsUriIndex) {
			AlertDialog dialog = getEditDialog(this, item);
			dialog.show();
		}
		else {
			SettingItemUtility.showAsToast(this, item);
		}
	}

	SettingItem getItemFromView(View view, long id) {
		TextView textView = (TextView) view.findViewById(android.R.id.title);
		String name = textView.getText().toString();
		textView = (TextView) view.findViewById(android.R.id.text1);
		CharSequence value = textView.getText();
		Uri uri = Uri.withAppendedPath(SETTINGS_URI_ARRAY[mSettingsUriIndex], name);
		return new SettingItem(uri, id, name, value.toString());
	}

	AlertDialog getEditDialog(final Context context, final SettingItem item) {
		View view = LayoutInflater.from(context).inflate(R.layout.settings_edit_dialog, null);
		TextView textView = (TextView) view.findViewById(android.R.id.title);
		textView.setText(Html.fromHtml(context.getString(R.string.dialog_edit_settings_warning)));
		final EditText editText = (EditText) view.findViewById(android.R.id.text1);
		editText.setText(item.value);
		AlertDialog.Builder builder = new AlertDialog.Builder(context)
			.setTitle(item.name)
			.setView(view)
			.setNegativeButton(android.R.string.cancel, null)
			.setPositiveButton(android.R.string.ok, new AlertDialog.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					SettingItemUtility.update(context, item, editText.getText().toString());
				}
			})
			.setNeutralButton(R.string.delete, new AlertDialog.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					SettingItemUtility.delete(context, item);
				}
			});
		return builder.create();
	}

	AlertDialog getDeleteConfirmDialog(final Context context, final SettingItem item) {
		return Utility.getConfirmDialog(context,
			getString(R.string.dialog_confirm_title),
			Utility.stringFormat(context, R.string.dialog_confirm_message, item.name),
			new AlertDialog.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					SettingItemUtility.delete(context, item);
				}
			}
		);
	}

	AlertDialog getBlockConfirmDialog(final Context context, final SettingItem item) {
		return Utility.getConfirmDialog(context,
			getString(R.string.dialog_block_confirm_title),
			Utility.stringFormat(context, R.string.dialog_block_confirm_message, item.name),
			new AlertDialog.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					SettingItemUtility.delete(context, item);
					BlackListUtility.addToBlackList(context, item, null);
				}
			}
		);
	}
}
