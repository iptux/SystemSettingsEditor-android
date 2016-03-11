package net.iptux.systemsettingseditor.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import net.iptux.systemsettingseditor.support.Utility;

public class BootReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
			if (Utility.prefMonitorOnBoot(context)) {
				Utility.startMonitor(context);
			}
		}
	}
}
