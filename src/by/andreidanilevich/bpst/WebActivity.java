package by.andreidanilevich.bpst;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.TextView;

public class WebActivity extends Activity {

	SharedPreferences mSettings;
	public static final String APP_PREFERENCES = "mysettings";
	public static final String APP_PREFERENCES_COUNTER5 = "power"; // �����
	Integer power = 1;

	Integer _id;
	String track_marker, contentString;
	String temp_string_1, temp_string_2;
	private SQLiteDatabase DB_track;
	WebView tv;
	TextView tv_info, web_actual_history;
	Cursor cursor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.web);

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

		tv = (WebView) findViewById(R.id.tv);
		tv.getSettings();
		tv.setBackgroundColor(0x00000000);

		tv_info = (TextView) findViewById(R.id.tv_info);
		web_actual_history = (TextView) findViewById(R.id.web_actual_history);

		Intent intent = getIntent(); // ������� ��������� �������
		_id = intent.getIntExtra("_id", 1);
		track_marker = intent.getStringExtra("track_marker");

		DB_track = openOrCreateDatabase("DB_track.db", Context.MODE_PRIVATE,
				null);

		cursor = DB_track.rawQuery("SELECT * FROM '" + track_marker
				+ "' WHERE _id = " + _id, null);

		if (cursor.moveToFirst()
				&& cursor.getString(cursor.getColumnIndex("content1")) != null
				&& cursor.getString(cursor.getColumnIndex("content2")) != null) {

			create_html_content();
			// ������ ������ �������
			ContentValues values = new ContentValues();
			values.put("upd_mark", 0);
			DB_track.update(track_marker, values,
					"_id = " + cursor.getInt(cursor.getColumnIndex("_id")),
					null);
			values.clear();

		} else {

			AlertDialog.Builder quitDialog = new AlertDialog.Builder(
					WebActivity.this);
			quitDialog
					.setTitle("��������!")
					.setMessage(
							"���� ����-��� �� �������� ������� ���������� �� ��������.\n��������� ����� � �������� ������!")
					.setIcon(R.drawable.icon).setCancelable(false);

			quitDialog.setPositiveButton("OK", new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {

					onBackPressed();
				}
			});
			quitDialog.show();

		}

		tv_info.setText("��������: "
				+ cursor.getString(cursor.getColumnIndex("name"))
				+ "\n����-���: "
				+ cursor.getString(cursor.getColumnIndex("track")));

		web_actual_history.setText("��������� ����������: "
				+ cursor.getString(cursor.getColumnIndex("last_update")));

	}

	public void onBackPressed() {
		cursor.close();
		finish();
		overridePendingTransition(R.anim.flipin_r, R.anim.flipout_r);
	}

	public void create_html_content() {

		// ������ �������� � ���������� ��� JSOUP
		// � ������� �������� html
		// ***************************************************////////////////////

		String temp_content_1 = null, temp_content_2 = null;

		// ��� ���������� ����������� html � string �����

		Document doc1 = Jsoup.parse(cursor.getString(
				cursor.getColumnIndex("content1")).toString());
		Document doc2 = Jsoup.parse(cursor.getString(
				cursor.getColumnIndex("content2")).toString());

		if (cursor.getString(cursor.getColumnIndex("content1")).toString()
				.equals("�� ������ ������� ������ �� �������")) {
			temp_content_1 = "<table style=\"width: 100%;\" cellpadding=\"5\"><tbody><tr>"
					+ "<td style=\"vertical-align: center; background-color: rgb(72, 114, 150); text-align: left;\">"
					+ "<big><span style=\"color: white; font-weight: bold;\">�� ��:</span></big><br><br>"
					+ "<span style=\"color: red;\">�� ������� ����-���� ���������� ���!</span>";

			temp_string_1 = "�� ��: �� ������� ����-���� ���������� ���!";

		} else {

			try { // ������� �������� ��������� ���� ����� ������� � �����

				// ���� ���� ������� �������� �� 1 �����
				// ������� �������� html
				temp_content_1 = "<table style=\"width: 100%;\" cellpadding=\"5\"><tbody><tr>"
						+ "<td style=\"vertical-align: center; background-color: rgb(72, 114, 150); text-align: left;\">"
						+ "<big><span style=\"color: white; font-weight: bold;\">�� ��:</span></big><br><br>";

				temp_string_1 = "�� ��:\n\n";

				Elements tr = doc1.getElementsByTag("tr");
				Element td;
				Integer i = 0;

				do {
					i = i + 1;
					td = tr.get(i);
					Elements td_text = td.getElementsByTag("td");
					temp_content_1 = temp_content_1
							+ "<table style=\"width: 100%;\" cellpadding=\"5\"><tbody><tr>"
							+ "<td style=\"vertical-align: center; background-color: rgb(34, 70, 101); text-align: left;\">"
							+ "<small><span style=\"color: white;\">����:&nbsp&nbsp</span><span style=\"color: yellow;\">"
							+ td_text.get(0).text()
							+ "</span></small>"
							+ "<td style=\"vertical-align: top; text-align: right;\">"
							+ "<span style=\"color: white;\"><b>#"
							+ i
							+ "</b></span>"
							+ "<tr><td style=\"vertical-align: top; background-color: white;\" colspan=\"2\">"
							+ "<b>�������: </b>" + td_text.get(1).text()
							+ "</td></tr></td></tr></tbody></table>";

					temp_string_1 = temp_string_1 + "����:\n"
							+ td_text.get(0).text() + "\n�������:\n"
							+ td_text.get(1).text() + "\n-----\n";

				} while (tr.get(i) != tr.last());

			} catch (Exception e) {
				// ���� ������ ������ ������� �������� ������
				error_NoValidHtml();

			}

		}
		if (cursor.getString(cursor.getColumnIndex("content2")).toString()
				.equals("�� ������ ������� ������ �� �������")) {
			temp_content_2 = "<table style=\"width: 100%;\" cellpadding=\"5\"><tbody><tr>"
					+ "<td style=\"vertical-align: center; background-color: rgb(72, 114, 150); text-align: left;\">"
					+ "<big><span style=\"color: white; font-weight: bold;\">������������� � EMS:</span></big><br><br>"
					+ "<span style=\"color: red;\">�� ������� ����-���� ���������� ���!</span>";

			temp_string_2 = "������������� � EMS: �� ������� ����-���� ���������� ���!";

		} else {

			try { // ������� �������� ��������� ���� ����� ������� � �����

				// ���� ���� ������� �������� �� 2 �����
				// ������� �������� html
				temp_content_2 = "<table style=\"width: 100%;\" cellpadding=\"5\"><tbody><tr>"
						+ "<td style=\"vertical-align: center; background-color: rgb(72, 114, 150); text-align: left;\">"
						+ "<big><span style=\"color: white; font-weight: bold;\">������������� � EMS:</span></big><br><br>";

				temp_string_2 = "������������� � EMS:\n\n";

				Element table = doc2.getElementsByTag("table").first();
				// ������ ������ �������
				Elements tr = table.getElementsByTag("tr");
				// �� ��� ������ ��� ������
				Element td;
				Integer i = 0;

				do {
					i = i + 1;
					td = tr.get(i);
					// ����� ���������� ������
					Elements td_text = td.getElementsByTag("td");
					String td_text3;
					try {
						td_text3 = td_text.get(2).text();
					} catch (Exception e) {
						td_text3 = " ��� ������.";
					}
					temp_content_2 = temp_content_2
							+ "<table style=\"width: 100%;\" cellpadding=\"5\"><tbody><tr>"
							+ "<td style=\"vertical-align: center; background-color: rgb(34, 70, 101); text-align: left;\">"
							+ "<small><span style=\"color: white;\">����:&nbsp&nbsp</span><span style=\"color: yellow;\">"
							+ td_text.get(0).text()
							+ "</span></small>"
							+ "<td style=\"vertical-align: top; text-align: right;\">"
							+ "<span style=\"color: white;\"><b>#"
							+ i
							+ "</b></span>"
							+ "<tr><td style=\"vertical-align: top; background-color: white;\" colspan=\"2\">"
							+ "<b>�������: </b>" + td_text.get(1).text()
							+ "<br><b>���������: </b>" + td_text3
							+ "</td></tr></td></tr></tbody></table>";

					temp_string_2 = temp_string_2 + "����:\n"
							+ td_text.get(0).text() + "\n�������:\n"
							+ td_text.get(1).text() + "\n���������:\n"
							+ td_text3 + "\n-----\n";

				} while (tr.get(i) != tr.last());

			} catch (Exception e) {
				// ���� ������ ������ ������� �������� ������
				error_NoValidHtml();

			}
		}

		contentString = "��������: "
				+ cursor.getString(cursor.getColumnIndex("name"))
				+ "\n����-���: "
				+ cursor.getString(cursor.getColumnIndex("track"))
				+ "\n��������� ����������: "
				+ cursor.getString(cursor.getColumnIndex("last_update"))
				+ "\n----------------\n" + temp_string_1
				+ "\n----------------\n" + temp_string_2;

		String contentHtml = "<html><body>" + temp_content_1
				+ "</td></tr></tbody></table>" + temp_content_2
				+ "</td></tr></tbody></table></body></html>";

		tv.getSettings().setUseWideViewPort(false);

		tv.loadDataWithBaseURL("file:///android_asset/img", contentHtml,
				"text/html", "utf-8", null);

	}

	public void error_NoValidHtml() {

		AlertDialog.Builder quitDialog = new AlertDialog.Builder(
				WebActivity.this);
		quitDialog
				.setTitle("������!")
				.setMessage(
						"����� ������� �������� �������� ��������� ������.\n�������� �� ����������, ��� �� ��� ���������� ���. ������.\n���������� �������� ���������� �����.")
				.setIcon(R.drawable.icon).setCancelable(false);

		quitDialog.setPositiveButton("OK", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				onBackPressed();
			}
		});
		quitDialog.show();

	}

	public void btn_share(View v) {

		Intent emailIntent = new Intent(Intent.ACTION_SEND);
		emailIntent.setType("text/plain");
		emailIntent.putExtra(Intent.EXTRA_TEXT, contentString);
		emailIntent.putExtra(Intent.EXTRA_SUBJECT,
				"��������� �������� ����-����.");
		startActivity(Intent.createChooser(emailIntent, "������ ����-����.."));
	}

}
