package by.andreidanilevich.bpst;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class Boot extends BroadcastReceiver {

	SharedPreferences mSettings;
	public static final String APP_PREFERENCES_COUNTER1 = "auto_load"; // авто_проверка
	public static final String APP_PREFERENCES = "mysettings";
	Integer auto_load = 1;

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			mSettings = context.getSharedPreferences(APP_PREFERENCES,
					Context.MODE_PRIVATE);
			if (mSettings.contains(APP_PREFERENCES_COUNTER1)) {
				auto_load = mSettings.getInt(APP_PREFERENCES_COUNTER1, 1);
			}
		}
		if (auto_load == 1) {// запустим сервис и все
			Intent serv = new Intent(context, Servis.class);
			context.startService(serv);
		}
	}
}
