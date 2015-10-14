package by.andreidanilevich.bpst;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;

public class LogoActivity extends Activity {

	public static final String APP_PREFERENCES_COUNTER0 = "first_start"; // первый_старт
	public static final String APP_PREFERENCES_COUNTER1 = "auto_load"; // авто_проверка
	public static final String APP_PREFERENCES_COUNTER2 = "auto_load_sound"; // авто_звук
	public static final String APP_PREFERENCES_COUNTER4 = "server_time"; // время_на_соединение
	public static final String APP_PREFERENCES_COUNTER5 = "power"; // экран

	public static final String APP_PREFERENCES = "mysettings";
	SharedPreferences mSettings;
	Integer first_start = 0;

	Boolean go_back = false;
	Handler handler;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.logo);

		// остановим наш сервис
		Intent serv = new Intent(getApplicationContext(), Servis.class);
		getApplicationContext().stopService(serv);

		mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
		if (mSettings.contains(APP_PREFERENCES_COUNTER0)) { // проверка_на_1й_старт
			first_start = mSettings.getInt(APP_PREFERENCES_COUNTER0, 0);
		}

		if (first_start == 0) { // если это первый запуск - записываем
			// стандартные настройки
			first_start = 1;

			Editor editor = mSettings.edit();
			editor.putInt(APP_PREFERENCES_COUNTER0, first_start);
			editor.putInt(APP_PREFERENCES_COUNTER1, 1);
			editor.putInt(APP_PREFERENCES_COUNTER2, 1);
			editor.putInt(APP_PREFERENCES_COUNTER4, 5000);
			editor.putInt(APP_PREFERENCES_COUNTER5, 1);

			editor.commit();
		}

		go_back = true;
		handler = new Handler();
		handler.postDelayed(new Runnable() {
			public void run() {
				finish();
			}
		}, 3000);
	}

	public void goBack(View v) {
		if (go_back) {
			finish();
		}
	}

	public void goSite(View v) {
		Intent openlink = new Intent(Intent.ACTION_VIEW,
				Uri.parse("http://andreidanilevich.blogspot.com/"));
		startActivity(openlink);
	}

	public void onBackPressed() {
		if (go_back) {
			finish();
		}
	}
}
