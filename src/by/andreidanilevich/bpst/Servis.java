package by.andreidanilevich.bpst;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;

@SuppressLint("SimpleDateFormat")
@SuppressWarnings("deprecation")
public class Servis extends Service {

	public static final String APP_PREFERENCES_COUNTER4 = "server_time"; // �����_��_����������
	public static final String APP_PREFERENCES_COUNTER3 = "time_temp"; // �����_����
	public static final String APP_PREFERENCES_COUNTER2 = "auto_load_sound"; // ����_����
	public static final String APP_PREFERENCES = "mysettings";
	SharedPreferences mSettings;
	Handler handler;
	Long time_temp, time_current;
	String tmp;
	Integer auto_load_sound = 1, server_time = 5000;
	Boolean update_mark = false;
	PowerManager.WakeLock wl;
	DB_READ_service db_read_service;
	SimpleDateFormat t_current = new SimpleDateFormat("HH:mm dd.MM.yyyy");
	private SQLiteDatabase DB_track;
	Intent intent;
	PendingIntent pendIntent;
	NotificationCompat.Builder builder;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public void onStart(Intent intent, int startId) {

		intent = new Intent(getApplicationContext(), MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_ACTIVITY_SINGLE_TOP);
		pendIntent = PendingIntent.getActivity(getApplicationContext(), 0,
				intent, PendingIntent.FLAG_CANCEL_CURRENT);
		builder = new NotificationCompat.Builder(getApplicationContext());

		DB_track = openOrCreateDatabase("DB_track.db", Context.MODE_PRIVATE,
				null);
		mSettings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

		tmp = t_current.format(new Date()).toString();

		try {
			time_current = new SimpleDateFormat("HH:mm dd.MM.yyyy").parse(tmp)
					.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		} // �������� ������� �����
			// ������� ����� �� ������ ������
		Editor editor = mSettings.edit();
		editor.putString(APP_PREFERENCES_COUNTER3, time_current.toString());
		editor.commit();

		// ����� �������

		if (mSettings.contains(APP_PREFERENCES_COUNTER4)) {
			server_time = mSettings.getInt(APP_PREFERENCES_COUNTER4, 5000);
		} // ����� �������

		PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
		wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "-");

		show_mes("start_servise");
		// �������� ����������� ������
	}

	public void show_mes(String mes) {

		if (mes == "start_servise") {

			builder.setContentIntent(pendIntent)
					.setSmallIcon(R.drawable.icon_mes_1)
					.setLargeIcon(
							BitmapFactory.decodeResource(
									getApplicationContext().getResources(),
									R.drawable.icon))
					.setAutoCancel(true)
					.setContentTitle(
							getResources().getString(R.string.app_name))
					.setContentText("�������� ���� ���...");

			Notification notification = builder.getNotification();
			startForeground(101, notification);
		}

		if (mes == "update") {

			Cursor cursor_temp = DB_track.rawQuery(
					"SELECT * FROM actual_track WHERE upd_mark = 1", null);

			if (cursor_temp.moveToFirst()) { // ---------- ���� ����������

				if (mSettings.contains(APP_PREFERENCES_COUNTER2)) {
					auto_load_sound = mSettings.getInt(
							APP_PREFERENCES_COUNTER2, 0);
				}

				if (!update_mark) { // ���� ��� �� �������� ����������

					builder.setContentIntent(pendIntent)
							.setLargeIcon(
									BitmapFactory.decodeResource(
											getApplicationContext()
													.getResources(),
											R.drawable.icon))
							.setAutoCancel(true)
							.setContentText("���� ��������!!!")
							.setTicker("���� ��������!!!")
							.setSmallIcon(R.drawable.icon_mes)
							.setContentTitle(
									getResources().getString(R.string.app_name));

					Notification notification = builder.getNotification();
					if (auto_load_sound == 1) {
						notification.sound = RingtoneManager
								.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
						// �������� ������ ��� �������
					}

					NotificationManager notificationManager = (NotificationManager) getApplicationContext()
							.getSystemService(Context.NOTIFICATION_SERVICE);
					notificationManager.notify(101, notification);

					update_mark = true; // ������ �� ����������

				}

				cursor_temp.close();
			} else { // ----------------------------------- ��� ����������

				builder.setContentIntent(pendIntent)
						.setLargeIcon(
								BitmapFactory.decodeResource(
										getApplicationContext().getResources(),
										R.drawable.icon))
						.setAutoCancel(true)
						.setContentText("�������� ���� ���...")
						.setSmallIcon(R.drawable.icon_mes_1)
						.setContentTitle(
								getResources().getString(R.string.app_name));

				Notification notification = builder.getNotification();

				NotificationManager notificationManager = (NotificationManager) getApplicationContext()
						.getSystemService(Context.NOTIFICATION_SERVICE);
				notificationManager.notify(101, notification);

				cursor_temp.close();
			}
		}

		timer();
	}

	@SuppressLint("SimpleDateFormat")
	public void timer() { // �������� ������

		db_read_service = new DB_READ_service();

		// ------------------------ �������� ������� � ���������� �����
		if (mSettings.contains(APP_PREFERENCES_COUNTER3)) {
			tmp = mSettings.getString(APP_PREFERENCES_COUNTER3, null);
		}
		time_temp = Long.valueOf(tmp);
		// �������� ������ �� ��������

		SimpleDateFormat t_current = new SimpleDateFormat("HH:mm dd.MM.yyyy");
		tmp = t_current.format(new Date()).toString();

		try {
			time_current = new SimpleDateFormat("HH:mm dd.MM.yyyy").parse(tmp)
					.getTime();
		} catch (ParseException e) {
			e.printStackTrace();
		}
		// ------------------------ �������� ---------------------
		if (handler != null) {
			handler.removeCallbacksAndMessages(null); // �������� �������
		}

		if (time_current > time_temp + 1800000) { // ����_�����_���������

			// 1800000 = 30�����
			// 120000 = 2 ������

			// ������� �����
			Editor editor = mSettings.edit();
			editor.putString(APP_PREFERENCES_COUNTER3, time_current.toString());
			editor.commit();

			db_read_service.execute(); // �������� �����

		} else {

			handler = new Handler();
			handler.postDelayed(new Runnable() {

				public void run() {
					timer();
					// ��������������
				}
			}, 60000);
		}

	}

	@SuppressLint("SimpleDateFormat")
	class DB_READ_service extends AsyncTask<Integer, Void, Integer> {

		String temp1 = null, temp2 = null;
		ContentValues values = new ContentValues();

		@Override
		protected Integer doInBackground(Integer... params) {

			wl.acquire(); // �������� ��������

			Cursor cursor = DB_track.rawQuery("SELECT * FROM actual_track",
					null);

			if (cursor.moveToFirst()) {
				if (isCancelled())
					return null;

				do {// !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! � � � � �

					HttpParams httpParameters = new BasicHttpParams();
					HttpConnectionParams.setConnectionTimeout(httpParameters,
							server_time); // ����������_��_��������_5���_��
											// ���������
					HttpClient httpclient = new DefaultHttpClient(
							httpParameters);
					HttpPost httppost = new HttpPost(
							"http://search.belpost.by/ajax/search");
					// ������ � ��������� 1
					List<NameValuePair> first = new ArrayList<NameValuePair>(2);
					first.add(new BasicNameValuePair("item", cursor
							.getString(cursor.getColumnIndex("track"))));
					first.add(new BasicNameValuePair("internal", "1"));
					// ������ � ��������� 2
					List<NameValuePair> second = new ArrayList<NameValuePair>(2);
					second.add(new BasicNameValuePair("item", cursor
							.getString(cursor.getColumnIndex("track"))));
					second.add(new BasicNameValuePair("internal", "2"));

					if (isCancelled())
						return null;

					try {
						// �������� ������ 1
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
					} catch (Exception e) {
						temp1 = null;
					}

					if (isCancelled())
						return null;

					try {
						// �������� ������ 2
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
					} catch (Exception e) {
						temp2 = null;
					}

					if (isCancelled())
						return null;

					try { // ������ � �� � ����� �������������� ����������
						if (temp1 == null || temp2 == null) {
							// ���������� ��� = 0
						} else {

							if (isCancelled())
								return null;

							if (!temp1.equals(cursor.getString(cursor
									.getColumnIndex("content1")))) {
								values.put("content1", temp1);
								values.put("upd_mark", 1);
							}
							if (!temp2.equals(cursor.getString(cursor
									.getColumnIndex("content2")))) {
								values.put("content2", temp2);
								values.put("upd_mark", 1);
							}
						}

						if (isCancelled())
							return null;

						values.put("last_update", t_current.format(new Date())
								.toString());
						DB_track.update("actual_track", values, "_id = "
								+ cursor.getInt(cursor.getColumnIndex("_id")),
								null);

					} finally {
						values.clear();
						temp1 = null;
						temp2 = null;

					}

				} while (cursor.moveToNext());
				cursor.close();
			}
			return null;
		}

		protected void onCancelled() {
			temp1 = null;
			temp2 = null;
		}

		protected void onPostExecute(Integer result) {

			wl.release(); // �������� ��������
			show_mes("update");
		}
	}

	@Override
	public void onDestroy() { // ���� ������ ���� �������
		super.onDestroy();

		if (db_read_service.getStatus() == Status.RUNNING) {
			db_read_service.cancel(true); // ��������� ��������
		}

		if (handler != null) {
			handler.removeCallbacksAndMessages(null); // �������� �������
		}
		wl.acquire();
		wl.release(); // �������� ��������
	}

	public Boolean valid_me(String valid) { // ��������� ������

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
}
