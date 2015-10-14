package by.andreidanilevich.bpst;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class MainActivity extends Activity {

	SharedPreferences mSettings;
	public static final String APP_PREFERENCES_COUNTER1 = "auto_load"; // ����_��������
	public static final String APP_PREFERENCES_COUNTER4 = "server_time"; // �����_��_����������
	public static final String APP_PREFERENCES_COUNTER5 = "power"; // �����
	public static final String APP_PREFERENCES = "mysettings";
	Integer auto_load = 1, server_time = 5000, power = 1;

	TextView progress, load_tv, tv_actual_history;
	RelativeLayout load_RL;
	LinearLayout layoutMenu;
	EditText et;
	Integer _id;
	Boolean load_open = false; // ������ �������� ��������

	ListView lv, listMenu;
	String track_marker = "actual_track";
	private SQLiteDatabase DB_track;
	DB_READ db_read;

	private static final int CM_chg = 101;
	private static final int CM_del = 102;
	private static final int CM_ret = 103;
	private static final int CM_deldel = 104;

	// ��� ������� ������ �������
	ArrayList<HashMap<String, Object>> mList;
	ArrayList<Integer> izbrannoe_id;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.menu);

		mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

		if (mSettings.contains(APP_PREFERENCES_COUNTER4)) {
			server_time = mSettings.getInt(APP_PREFERENCES_COUNTER4, 5000);
		} // ����� �������

		if (mSettings.contains(APP_PREFERENCES_COUNTER5)) {
			power = mSettings.getInt(APP_PREFERENCES_COUNTER5, 1);
		}

		if (power == 1) {
			getWindow()
					.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		} else {
			getWindow().clearFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		} // ���������� ������

		et = (EditText) findViewById(R.id.et);
		layoutMenu = (LinearLayout) findViewById(R.id.layoutMenu);
		load_RL = (RelativeLayout) findViewById(R.id.load_RL);
		load_tv = (TextView) findViewById(R.id.load_tv);
		tv_actual_history = (TextView) findViewById(R.id.tv_actual_history);
		lv = (ListView) findViewById(R.id.lv);
		registerForContextMenu(lv);

		listMenu = (ListView) findViewById(R.id.listMenu);
		String[] nameMenu = new String[] { "�������� ���!",
				"�������� ����-���", "���������� �����", "��������� �����",
				"���������", "�������", "�����" };
		// ���������� ������� ������
		ArrayAdapter<String> adapterMenu = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, nameMenu);
		listMenu.setAdapter(adapterMenu);

		// �������� ����
		Intent intent = new Intent(this, LogoActivity.class);
		startActivity(intent);

		// �������� ��� ��������� ��������� ���� � ������
		// ����������!!!!!!!!!!!!!!!!!!!!!!!!
		DB_track = openOrCreateDatabase("DB_track.db", Context.MODE_PRIVATE,
				null);
		// ������� ��� ��������� 2 ������� � ����� ����
		DB_track.execSQL("CREATE TABLE IF NOT EXISTS actual_track (_id integer primary key autoincrement, name, name_l, data, track, last_update, content1, content2, upd_mark)");
		DB_track.execSQL("CREATE TABLE IF NOT EXISTS history_track (_id integer primary key autoincrement, name,name_l, data, track, last_update, content1, content2, upd_mark)");

	}

	public void onResume() {
		super.onResume();

		actual_track("actual_track", "");
		et.setText("");

		// ��������� ������
		// *******************************************************************
		// ��������_���������_��_������_����
		et.setOnKeyListener(new OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if ((event.getAction() == KeyEvent.ACTION_DOWN)
						&& (keyCode == KeyEvent.KEYCODE_ENTER)) {
					// ������� ����������
					InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(et.getWindowToken(), 0);
					return true;
				}
				return false;
			}
		});

		// *******************************************************************

		et.addTextChangedListener(new TextWatcher() {// ������_��_������
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
				if (et.getText().toString().equals(" ")
						|| et.getText().toString().equals("'")
						|| et.getText().toString().equals("%")) {
					et.setText("");
				}
				// �������� � ������ ������������ ***********************
				actual_track(track_marker, et.getText().toString());

			}
		});
	}

	// ���������� ������ �������
	public void actual_track(String track_table, String like_mark) {

		et.setEnabled(true); // ������� ���� �����

		Cursor cursor = DB_track.rawQuery("SELECT * FROM " + track_table
				+ " WHERE name_l LIKE '%" + like_mark + "%' ORDER BY _id DESC",
				null);

		// ������ �������� ����
		mList = new ArrayList<HashMap<String, Object>>();
		HashMap<String, Object> hm;

		// ������ id
		izbrannoe_id = new ArrayList<Integer>();

		if (cursor.moveToFirst()) {

			do {
				// �������� hashmap ��������� ������ ��� ������ �������
				hm = new HashMap<>();

				hm.put("list_text1",
						cursor.getString(cursor.getColumnIndex("data")));
				hm.put("list_text2",
						cursor.getString(cursor.getColumnIndex("name")));
				hm.put("list_text3",
						cursor.getString(cursor.getColumnIndex("track")));
				hm.put("list_text4",
						cursor.getString(cursor.getColumnIndex("last_update")));

				if (cursor.getInt(cursor.getColumnIndex("upd_mark")) == 1) {
					hm.put("list_text5", "������ ���������!");
				} else {
					hm.put("list_text5", "");
				}

				if (track_table == "actual_track") {
					hm.put("list_im", R.drawable.im_active);
				} else {
					hm.put("list_im", R.drawable.im_delete);
				}

				mList.add(hm);

				// �� ������ ������ ��������� ��
				izbrannoe_id.add(cursor.getInt(cursor.getColumnIndex("_id")));

			} while (cursor.moveToNext());

			SimpleAdapter adapter = new SimpleAdapter(getApplicationContext(),
					mList, R.layout.list, new String[] { "list_text1",
							"list_text2", "list_text3", "list_text4",
							"list_text5", "list_im" }, new int[] {
							R.id.list_text1, R.id.list_text2, R.id.list_text3,
							R.id.list_text4, R.id.list_text5, R.id.img_list });

			lv.setAdapter(adapter);
			lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent,
						View itemClicked, int position, long id) {

					if (!load_open) {

						Intent intent = new Intent(MainActivity.this,
								WebActivity.class);
						intent.putExtra("_id", izbrannoe_id.get(position));
						intent.putExtra("track_marker", track_marker);

						// �������� ��������� ��
						startActivity(intent);
						overridePendingTransition(R.anim.flipin_l,
								R.anim.flipout_l);
					}

				}
			});

			lv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
				@Override
				public boolean onItemLongClick(AdapterView<?> parent,
						View view, int position, long id) {
					_id = izbrannoe_id.get(position);
					return false;
				}
			});

		} else {
			lv.setAdapter(null);
		}
		// ������� ��������� � ������� ���������
		if (track_table == "actual_track") {
			tv_actual_history.setText("���������� ����-���� ("
					+ cursor.getCount() + "):");
		} else {
			tv_actual_history.setText("��������� ����-���� ("
					+ cursor.getCount() + "):");
		}
		cursor.close();
	}

	// ����������� ���� - ��������
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);

		Cursor cursor = DB_track.rawQuery("SELECT * FROM '" + track_marker
				+ "' WHERE _id = " + _id, null);
		cursor.moveToFirst();

		menu.setHeaderTitle("����-���: "
				+ cursor.getString(cursor.getColumnIndex("name")));

		if (track_marker == "actual_track") {
			menu.add(Menu.NONE, CM_chg, Menu.NONE, "�������� ����-���");
			menu.add(Menu.NONE, CM_del, Menu.NONE, "��������� � ���������");
			cursor.close();
		} else {
			menu.add(Menu.NONE, CM_ret, Menu.NONE, "������� � ����������");
			menu.add(Menu.NONE, CM_deldel, Menu.NONE, "������� ���������");
			cursor.close();
		}
	}

	// ����������� ���� - ��������� �������
	@Override
	public boolean onContextItemSelected(MenuItem item) {

		Cursor cursor = DB_track.rawQuery("SELECT * FROM '" + track_marker
				+ "' WHERE _id = " + _id, null);
		cursor.moveToFirst();

		switch (item.getItemId()) {
		case CM_chg:

			Intent intent = new Intent(MainActivity.this, PlusActivity.class);
			intent.putExtra("_id", _id);
			startActivity(intent);
			overridePendingTransition(R.anim.flipin_l, R.anim.flipout_l);

			break;
		case CM_del:

			ContentValues values = new ContentValues();

			values.put("name", cursor.getString(cursor.getColumnIndex("name")));
			values.put("name_l",
					cursor.getString(cursor.getColumnIndex("name_l")));
			values.put("data", cursor.getString(cursor.getColumnIndex("data")));
			values.put("track",
					cursor.getString(cursor.getColumnIndex("track")));
			values.put("last_update",
					cursor.getString(cursor.getColumnIndex("last_update")));
			values.put("content1",
					cursor.getString(cursor.getColumnIndex("content1")));
			values.put("content2",
					cursor.getString(cursor.getColumnIndex("content2")));

			DB_track.insert("history_track", null, values);
			values.clear();

			DB_track.delete("actual_track", "_id = " + _id, null);

			Toast toast = Toast.makeText(getApplicationContext(), "����-���:\n"
					+ cursor.getString(cursor.getColumnIndex("name"))
					+ "\n��������� � ���������", Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER_HORIZONTAL
					| Gravity.CENTER_VERTICAL, 0, 0);
			toast.show();
			cursor.close();
			actual_track(track_marker, "");

			break;
		case CM_ret:

			ContentValues values2 = new ContentValues();

			values2.put("name", cursor.getString(cursor.getColumnIndex("name")));
			values2.put("name_l",
					cursor.getString(cursor.getColumnIndex("name_l")));
			values2.put("data", cursor.getString(cursor.getColumnIndex("data")));
			values2.put("track",
					cursor.getString(cursor.getColumnIndex("track")));
			values2.put("last_update",
					cursor.getString(cursor.getColumnIndex("last_update")));
			values2.put("content1",
					cursor.getString(cursor.getColumnIndex("content1")));
			values2.put("content2",
					cursor.getString(cursor.getColumnIndex("content2")));

			DB_track.insert("actual_track", null, values2);
			values2.clear();

			DB_track.delete("history_track", "_id = " + _id, null);

			Toast toast2 = Toast.makeText(
					getApplicationContext(),
					"����-���:\n"
							+ cursor.getString(cursor.getColumnIndex("name"))
							+ "\n��������� � ����������", Toast.LENGTH_LONG);
			toast2.setGravity(Gravity.CENTER_HORIZONTAL
					| Gravity.CENTER_VERTICAL, 0, 0);
			toast2.show();
			cursor.close();
			actual_track(track_marker, "");

			break;
		case CM_deldel:

			AlertDialog.Builder quitDialog = new AlertDialog.Builder(
					MainActivity.this);
			quitDialog
					.setTitle("�������?")
					.setMessage(
							"��������: "
									+ cursor.getString(cursor
											.getColumnIndex("name"))
									+ "\n����-���: "
									+ cursor.getString(cursor
											.getColumnIndex("track")))
					.setIcon(R.drawable.icon).setCancelable(true);

			quitDialog.setPositiveButton("��", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {

					Toast toast3 = Toast.makeText(getApplicationContext(),
							"����-��� ������!", Toast.LENGTH_LONG);
					toast3.setGravity(Gravity.CENTER_HORIZONTAL
							| Gravity.CENTER_VERTICAL, 0, 0);

					DB_track.delete("history_track", "_id = " + _id, null);
					actual_track(track_marker, "");
					toast3.show();
				}
			});
			quitDialog.show();
			break;
		default:
			return super.onContextItemSelected(item);
		}
		return true;
	}

	// �������� ���� - ��������� �������
	public void btn_menu(View v) { // ��������� ���� � ������������ �������

		if (!load_open) { // ���� ��������� ��������
			// ������ ����������
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(et.getWindowToken(), 0);

			layoutMenu.setVisibility(View.VISIBLE);
			listMenu.setOnItemClickListener(new AdapterView.OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> parent,
						View itemClicked, int position, long id) {

					layoutMenu.setVisibility(View.INVISIBLE);

					if (position == 0) {
						update_all(); // �������� ���
					}
					if (position == 1) { // �������� �����
						Intent intent = new Intent(MainActivity.this,
								PlusActivity.class);
						startActivity(intent);
						overridePendingTransition(R.anim.flipin_l,
								R.anim.flipout_l);
					}
					if (position == 2) {// ����������
						track_marker = "actual_track";
						actual_track(track_marker, "");
					}
					if (position == 3) {// ���������
						track_marker = "history_track";
						actual_track(track_marker, "");
					}
					if (position == 4) {// ���������
						Intent intent2 = new Intent(MainActivity.this,
								SettingsActivity.class);
						startActivity(intent2);
						overridePendingTransition(R.anim.flipin_l,
								R.anim.flipout_l);
					}
					if (position == 5) { // �������
						Intent ocenka = new Intent(Intent.ACTION_VIEW);
						ocenka.setData(Uri
								.parse("market://details?id=by.andreidanilevich.bpst"));
						startActivity(ocenka);
						overridePendingTransition(R.anim.flipin_l,
								R.anim.flipout_l);
					}
					if (position == 6) { // �����
						go_exit();
					}
				}
			});
		}
	}

	public void exitMenu(View v) { // ���� ��� ���� ��������� ���
		layoutMenu.setVisibility(View.INVISIBLE);
	}

	// �������� ���������� ���� �������� ����-�����
	public void update_all() {

		// ������� ��������
		track_marker = "actual_track";
		actual_track(track_marker, "");
		// ���� ���� ���� ���� ������
		if (lv.getCount() > 0) {

			load_open = true; // ������� ����������
			load_RL.setVisibility(View.VISIBLE);
			et.setEnabled(false);

			db_read = new DB_READ();
			db_read.execute(); // �������� �����

			load_tv.setText("����������� � �������...");
		} else {
			Toast toast = Toast.makeText(getApplicationContext(),
					"��� �� ������ �����������\n����-���� ��� ��������!",
					Toast.LENGTH_SHORT);
			toast.setGravity(Gravity.CENTER_HORIZONTAL
					| Gravity.CENTER_VERTICAL, 0, 0);
			toast.show();
		}
	}

	@SuppressLint("SimpleDateFormat")
	class DB_READ extends AsyncTask<Integer, Integer, String> {

		String temp1 = null, temp2 = null, result;
		ProgressBar load_pb = (ProgressBar) findViewById(R.id.load_pb);
		ContentValues values = new ContentValues();
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm / dd.MM.yyyy");

		@Override
		protected String doInBackground(Integer... params) {

			Cursor cursor2 = DB_track.rawQuery("SELECT * FROM actual_track",
					null);
			cursor2.moveToFirst();
			if (isCancelled())
				return null;

			do {

				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters,
						server_time);
				// ����������_��_��������_5���_��
				// ���������
				HttpClient httpclient = new DefaultHttpClient(httpParameters);
				HttpPost httppost = new HttpPost(
						"http://search.belpost.by/ajax/search");

				// ��������� ������ � ��������� 1
				List<NameValuePair> first = new ArrayList<NameValuePair>(2);
				first.add(new BasicNameValuePair("item", cursor2
						.getString(cursor2.getColumnIndex("track"))));
				first.add(new BasicNameValuePair("internal", "1"));

				// ��������� ������ � ��������� 2
				List<NameValuePair> second = new ArrayList<NameValuePair>(2);
				second.add(new BasicNameValuePair("item", cursor2
						.getString(cursor2.getColumnIndex("track"))));
				second.add(new BasicNameValuePair("internal", "2"));

				if (isCancelled())
					return null;

				try {
					// ������� �������� 1
					httppost.setEntity(new UrlEncodedFormEntity(first));
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}

				if (isCancelled())
					return null;

				try { // ������� 1 �����
					HttpResponse response = httpclient.execute(httppost);
					InputStream is = response.getEntity().getContent();
					InputStreamReader isr = new InputStreamReader(is);
					BufferedReader br = new BufferedReader(isr);
					StringBuilder sb = new StringBuilder();
					String bufferedStrChunk = null;
					while ((bufferedStrChunk = br.readLine()) != null) {
						sb.append(bufferedStrChunk);
					}
					temp1 = sb.toString();
					if (!valid_me(temp1)
							&& !temp1
									.equals("�� ������ ������� ������ �� �������")) {
						temp1 = null;
					}
					is.close();
					isr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				if (isCancelled())
					return null;

				try {
					// ������� �������� 2
					httppost.setEntity(new UrlEncodedFormEntity(second));
				} catch (UnsupportedEncodingException e1) {
					e1.printStackTrace();
				}

				if (isCancelled())
					return null;

				try { // ������� 2 �����
					HttpResponse response = httpclient.execute(httppost);
					InputStream is = response.getEntity().getContent();
					InputStreamReader isr = new InputStreamReader(is);
					BufferedReader br = new BufferedReader(isr);
					StringBuilder sb = new StringBuilder();
					String bufferedStrChunk = null;
					while ((bufferedStrChunk = br.readLine()) != null) {
						sb.append(bufferedStrChunk);
					}
					temp2 = sb.toString();
					if (!valid_me(temp2)
							&& !temp2
									.equals("�� ������ ������� ������ �� �������")) {
						temp2 = null;
					}
					is.close();
					isr.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				if (isCancelled())
					return null;

				// ---------------------������ � ������� ��������
				try { // ������ � �� � ����� �������������� ����������
					if (temp1 == null || temp2 == null) {
						// ���������� ��� = 0
					} else {

						if (isCancelled())
							return null;

						if (!temp1.equals(cursor2.getString(cursor2
								.getColumnIndex("content1")))) {
							values.put("content1", temp1);
							values.put("upd_mark", 1);
						}
						if (!temp2.equals(cursor2.getString(cursor2
								.getColumnIndex("content2")))) {
							values.put("content2", temp2);
							values.put("upd_mark", 1);
						}

					}

					if (isCancelled())
						return null;

					values.put("last_update", dateFormat.format(new Date())
							.toString());
					DB_track.update(
							"actual_track",
							values,
							"_id = "
									+ cursor2.getInt(cursor2
											.getColumnIndex("_id")), null);

					result = "���������.";
					values.clear();
					temp1 = null;
					temp2 = null;

				} finally {
					publishProgress(cursor2.getPosition() + 1,
							cursor2.getCount());
				}

			} while (cursor2.moveToNext());
			cursor2.close();
			return result;
		}

		protected void onCancelled() {
			temp1 = null;
			temp2 = null;

			load_tv.setText(load_tv.getText() + "\n���������� ��������!");

			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				public void run() {
					load_open = false;
					load_tv.setText("");
					et.setText("");
					load_RL.setVisibility(View.INVISIBLE);
					et.setEnabled(true);
					load_pb.setProgress(0);// ������� ��������

					actual_track("actual_track", "");
				}
			}, 3000);

		}

		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);

			Cursor tmp_name = DB_track.rawQuery("SELECT * FROM actual_track",
					null);
			tmp_name.moveToPosition(values[0] - 1);
			load_pb.setMax(values[1]);
			load_pb.setProgress(values[0]);
			load_tv.setText("����������� � �������...\n����� ���������: "
					+ values[0] + " �� " + values[1] + ".\n( "
					+ tmp_name.getString(tmp_name.getColumnIndex("name"))
					+ " )");

			tmp_name.close();
		}

		protected void onPostExecute(String result) {
			load_tv.setText("����������� � �������...\n" + result);

			Handler handler = new Handler();
			handler.postDelayed(new Runnable() {
				public void run() {
					load_open = false;
					load_tv.setText("");
					et.setText("");
					load_RL.setVisibility(View.INVISIBLE);
					et.setEnabled(true);
					load_pb.setProgress(0);// ������� ��������

					actual_track("actual_track", "");

				}
			}, 2000);
		}
	}

	public Boolean valid_me(String valid) { // �������� ����� �� ����������
											// ����� ��������
		Boolean ansver = true;

		Document doc = Jsoup.parse(valid);
		try {
			Elements tr = doc.getElementsByTag("tr");
			Element td;
			Integer i = 0;

			do {
				i = i + 1;
				td = tr.get(i);
				Elements td_text = td.getElementsByTag("td");
				td_text.get(0).text();
				td_text.get(1).text();

			} while (tr.get(i) != tr.last());
		} catch (Exception e) {
			// ���� ������ ������ ������� �������� ������
			ansver = false;
		}

		return ansver;
	}

	// ��������� ������ ����� *******************
	public void btn_search(View v) {
		if (!load_open) { // ���� ��������� ��������
			// �������� ���� ������
			InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
		}
	}

	// ��������� ������ ����� *******************
	public void onBackPressed() {
		if (!load_open) { // ���� ��������� ��������
			et.setText("");
		} else {

			AlertDialog.Builder quitDialog = new AlertDialog.Builder(
					MainActivity.this);
			quitDialog.setTitle("��������!")
					.setMessage("�� ������ �������� ��������?")
					.setIcon(R.drawable.icon).setCancelable(true);
			quitDialog.setPositiveButton("��", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					load_tv.setText(load_tv.getText() + "\n����������...");

					db_read.cancel(true);
				}
			});
			quitDialog.show();

		}
		layoutMenu.setVisibility(View.INVISIBLE);
	}

	// ��������� ������ ����� *******************
	public void go_exit() {
		if (!load_open) { // ���� ��������� �������� ����� �������

			if (mSettings.contains(APP_PREFERENCES_COUNTER1)) {
				auto_load = mSettings.getInt(APP_PREFERENCES_COUNTER1, 1);
			}
		}

		if (lv.getCount() > 0) {
			// ���� � ���������� ����� ������ �����������
			if (auto_load == 1) {// �������� ������ � ���
				// �������� ������ � ����
				Intent serv = new Intent(getApplicationContext(), Servis.class);
				getApplicationContext().startService(serv);
			}
		} else { // ����_������������_��������_��_���������_������

			Editor editor = mSettings.edit();
			editor.putInt(APP_PREFERENCES_COUNTER1, 0);

			editor.commit();
			// �������� ���������

			Toast toast = Toast
					.makeText(
							getApplicationContext(),
							"������������ ���� ��������� �.�. ��� �� ������ ����������� ����-����.",
							Toast.LENGTH_LONG);
			toast.setGravity(Gravity.CENTER_HORIZONTAL
					| Gravity.CENTER_VERTICAL, 0, 0);
			toast.show();

		}

		// ��� ������� � �����
		DB_track.close();
		finish();
		overridePendingTransition(R.anim.flipin_r, R.anim.flipout_r);

	}
}
