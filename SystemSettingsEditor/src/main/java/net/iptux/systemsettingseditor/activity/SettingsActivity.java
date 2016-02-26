package net.iptux.systemsettingseditor.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;

import net.iptux.systemsettingseditor.R;
import net.iptux.systemsettingseditor.support.Utility;


public class SettingsActivity extends PreferenceActivity {

	boolean mMonitor;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);

		mMonitor = Utility.prefMonitor(this);
	}

	@Override
	protected void onPause() {
		super.onPause();

		boolean newValue = Utility.prefMonitor(this);
		if (mMonitor != newValue) {
			Utility.setMonitorRunning(this, newValue);
			mMonitor = newValue;
		}
	}
}
