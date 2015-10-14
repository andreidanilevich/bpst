package by.andreidanilevich.bpst;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class PlusActivity extends Activity {

	SharedPreferences mSettings;
	public static final String APP_PREFERENCES = "mysettings";
	public static final String APP_PREFERENCES_COUNTER5 = "power"; // экран
	Integer power = 1;

	Integer _id;
	private SQLiteDatabase DB_track;
	Button btn_OK;
	EditText et_track, et_name;
	TextView et_data;

	@SuppressLint("SimpleDateFormat")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.plus_track);

		mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
		if (mSettings.contains(APP_PREFERENCES_COUNTER5)) {
			power = mSettings.getInt(APP_PREFERENCES_COUNTER5, 1);
		}

		if (power == 1) {
			getWindow()
					.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		} else {
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		DB_track = openOrCreateDatabase("DB_track.db", Context.MODE_PRIVATE,
				null);

		Intent intent = getIntent(); // получим передачку интента
		_id = intent.getIntExtra("_id", 0);

		btn_OK = (Button) findViewById(R.id.btn_OK);
		et_name = (EditText) findViewById(R.id.et_name);
		et_track = (EditText) findViewById(R.id.et_track);
		et_data = (TextView) findViewById(R.id.et_data);

		if (_id > 0) {

			Cursor cursor = DB_track.rawQuery(
					"SELECT * FROM actual_track WHERE _id = " + _id, null);
			cursor.moveToFirst();

			et_name.setText(cursor.getString(cursor.getColumnIndex("name")));
			et_track.setText(cursor.getString(cursor.getColumnIndex("track")));
			et_data.setText(cursor.getString(cursor.getColumnIndex("data")));
			btn_OK.setText("Изменить");

		} else {
			SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy");
			et_data.setText(dateFormat.format(new Date()).toString());
			btn_OK.setText("Добавить");
		}

		et_name.addTextChangedListener(new TextWatcher() {// следим_за_вводом
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (et_name.getText().toString().equals(" ")
						|| et_name.getText().toString().equals("'")
						|| et_name.getText().toString().equals("%")) {
					et_name.setText("");
				}
			}
		});

		et_track.addTextChangedListener(new TextWatcher() {// следим_за_вводом
			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
			}

			@Override
			public void afterTextChanged(Editable s) {
				if (et_track.getText().toString().equals(" ")
						|| et_track.getText().toString().equals("'")
						|| et_track.getText().toString().equals("%")) {
					et_track.setText("");
				}
			}
		});
		et_name.requestFocus();
	}

	public void plus_track(View v) {

		ContentValues values = new ContentValues();

		values.put("name", et_name.getText().toString());
		values.put("name_l",
				et_name.getText().toString().toLowerCase(Locale.getDefault()));
		values.put("data", et_data.getText().toString());
		values.put("track", et_track.getText().toString());

		if (_id > 0) { // если обновляем

			if (et_name.getText().toString().equals("")
					|| et_track.getText().toString().equals("")
					|| et_name.getText().length() < 1
					|| et_track.getText().length() < 5) {

				Toast toast = Toast.makeText(getApplicationContext(),
						"Неверные данные.\nИзменения не сохранены!",
						Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER_HORIZONTAL
						| Gravity.CENTER_VERTICAL, 0, 0);
				toast.show();

			} else {

				DB_track.update("actual_track", values, "_id = " + _id, null);
				Toast toast = Toast.makeText(getApplicationContext(),
						"Изменения сохранены", Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER_HORIZONTAL
						| Gravity.CENTER_VERTICAL, 0, 0);
				toast.show();

				values.clear();
				onBackPressed();
			}

		} else { // если вводим новое

			if (et_name.getText().toString().equals("")
					|| et_track.getText().toString().equals("")
					|| et_name.getText().length() < 1
					|| et_track.getText().length() < 5) {

				Toast toast = Toast.makeText(getApplicationContext(),
						"Неверные данные.\nИзменения не сохранены!",
						Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER_HORIZONTAL
						| Gravity.CENTER_VERTICAL, 0, 0);
				toast.show();

			} else {

				values.put("last_update", "--:-- / --.--.----");
				values.put("content1", "По вашему запросу ничего не найдено");
				values.put("content2", "По вашему запросу ничего не найдено");
				DB_track.insert("actual_track", null, values);
				Toast toast = Toast.makeText(getApplicationContext(),
						"Трэк-код добавлен.", Toast.LENGTH_LONG);
				toast.setGravity(Gravity.CENTER_HORIZONTAL
						| Gravity.CENTER_VERTICAL, 0, 0);
				toast.show();

				values.clear();
				onBackPressed();
			}
		}
	}

	public void onBackPressed() {
		DB_track.close();
		finish();
		overridePendingTransition(R.anim.flipin_r, R.anim.flipout_r);
	}
}
