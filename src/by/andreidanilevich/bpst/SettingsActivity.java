package by.andreidanilevich.bpst;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.Toast;

public class SettingsActivity extends Activity {

	SharedPreferences mSettings;
	public static final String APP_PREFERENCES = "mysettings";
	public static final String APP_PREFERENCES_COUNTER1 = "auto_load"; // ����_��������
	public static final String APP_PREFERENCES_COUNTER2 = "auto_load_sound"; // ����_����
	public static final String APP_PREFERENCES_COUNTER4 = "server_time"; // �����_��_����������
	public static final String APP_PREFERENCES_COUNTER5 = "power"; // �����
	Integer auto_load = 1, auto_load_sound = 1, server_time = 5000, power = 1;

	CheckBox set_auto_load_chb, set_auto_load_led, set_chb_power;
	Spinner spinner;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.settings);

		set_chb_power = (CheckBox) findViewById(R.id.set_chb_power);
		set_auto_load_chb = (CheckBox) findViewById(R.id.set_auto_load_chb);
		set_auto_load_led = (CheckBox) findViewById(R.id.set_auto_load_led);
		spinner = (Spinner) findViewById(R.id.server_timer);

		mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

		// --------------------------------------------------------

		if (mSettings.contains(APP_PREFERENCES_COUNTER1)) {
			auto_load = mSettings.getInt(APP_PREFERENCES_COUNTER1, 1);
		}

		if (auto_load == 1) {
			set_auto_load_chb.setChecked(true);
		} else {
			set_auto_load_chb.setChecked(false);
		}

		// -------------- ����� ��������1

		if (mSettings.contains(APP_PREFERENCES_COUNTER2)) {
			auto_load_sound = mSettings.getInt(APP_PREFERENCES_COUNTER2, 1);
		}

		if (auto_load_sound == 1) {
			set_auto_load_led.setChecked(true);
		} else {
			set_auto_load_led.setChecked(false);
		}

		// -------------- ����� ������ power

		if (mSettings.contains(APP_PREFERENCES_COUNTER5)) {
			power = mSettings.getInt(APP_PREFERENCES_COUNTER5, 1);
		}

		if (power == 1) {
			set_chb_power.setChecked(true);
			getWindow()
					.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		} else {
			set_chb_power.setChecked(false);
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		// -------------- ����� ��������2

		if (mSettings.contains(APP_PREFERENCES_COUNTER4)) {
			server_time = mSettings.getInt(APP_PREFERENCES_COUNTER4, 5000);
		}

		if (server_time == 5000) {
			spinner.setSelection(0);
		}
		if (server_time == 10000) {
			spinner.setSelection(1);
		}
		if (server_time == 15000) {
			spinner.setSelection(2);
		}
		if (server_time == 20000) {
			spinner.setSelection(3);
		}
		// ��������� ������
		spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent,
					View itemSelected, int selectedItemPosition, long selectedId) {
				// ��������� ��������� �����
				server_time = Integer.valueOf(spinner.getSelectedItem()
						.toString());
			}

			public void onNothingSelected(AdapterView<?> parent) {
			}
		});

		// -------------- ����� ��������4

	}

	public void save_me(View v) {

		if (set_auto_load_chb.isChecked()) {
			auto_load = 1;
		} else {
			auto_load = 0;
		}

		if (set_auto_load_led.isChecked()) {
			auto_load_sound = 1;
		} else {
			auto_load_sound = 0;
		}

		if (set_chb_power.isChecked()) {
			power = 1;
		} else {
			power = 0;
		}

		Editor editor = mSettings.edit();
		editor.putInt(APP_PREFERENCES_COUNTER1, auto_load);
		editor.putInt(APP_PREFERENCES_COUNTER2, auto_load_sound);
		editor.putInt(APP_PREFERENCES_COUNTER4, server_time);
		editor.putInt(APP_PREFERENCES_COUNTER5, power);

		editor.commit();
		// �������� ���������

		Toast toast = Toast.makeText(getApplicationContext(),
				"��������� ���������.", Toast.LENGTH_LONG);
		toast.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL,
				0, 0);
		toast.show();

		onBackPressed();

	}

	public void onBackPressed() {
		finish();
		overridePendingTransition(R.anim.flipin_r, R.anim.flipout_r);
	}

}
