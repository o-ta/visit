/*
 * 日報メイン画面
 * 日報の日時・区分・提供サービスを入力してもらう画面
 *
 * o-ta
 * 
 */

package com.tumiki0ituki.visitcare;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

/**
 * 日報の最初の画面
 * 
 */
public class ReportActivity extends Activity implements OnClickListener {
	/** Called when the activity is first created. */

	/**
	 * 各入力情報のgetter,setterクラス。このクラスごとIntentで渡す。
	 */
	private Report mReport;

	Calendar mCalendar = Calendar.getInstance();
	AlertDialog.Builder mDialog;
	TextView mTextView;

	/**
	 * 提供サービスチェックボックスのID
	 */
	int mCbId[] = { R.id.bath_box, R.id.clean_box, R.id.wash_box, R.id.shopping_box, R.id.cook_box,
			R.id.wear_box };

	private static String TAG;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.report_main);

		// Reportクラスのインスタンス
		mReport = new Report();

		// 業務IDを取得
		Intent intent = getIntent();
		int work_id = intent.getIntExtra("WORK_ID", 0);
		Log.d(TAG, "ワークID：　" + work_id);

		// 日付
		String work_day = intent.getStringExtra("DATE");
		Log.d(TAG, "予定の日付：　" + work_day);

		/* Add 2012/2/23 ==START== */
		// 予定時刻
		String schedle_time = intent.getStringExtra("SchedleTime");
		Log.d(TAG, "予定の時刻：　" + work_day + schedle_time);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		try {
			Date deftime = sdf.parse(work_day + " " + schedle_time);

			mCalendar.setTime(deftime);
		} catch (ParseException e) {
			e.printStackTrace();
		}

		// 被介護者名前
		String care_name = intent.getStringExtra("USER_NAME");
		Log.d(TAG, "被介護者名：　" + care_name);

		mReport.setmWorkId(work_id);
		mReport.setmWorkDay(work_day);
		mReport.setmCareName(care_name);

		// タイトルバーに該当業務の日付と被介護者名を表示
		setTitle(work_day + " " + care_name);

		// 日付ボタン
		Button date_btn = (Button) findViewById(R.id.date_button);
		date_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				new DatePickerDialog(ReportActivity.this, new DatePickerDialog.OnDateSetListener() {
					@Override
					public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
						// 日付が設定されたときの処理
						Date d = new Date();
						d.setYear(year - 1900);
						d.setMonth(monthOfYear);
						d.setDate(dayOfMonth);
						mReport.setmDate(d);
						mTextView = (TextView) findViewById(R.id.date_text);
						mTextView.setText(year + "年" + (monthOfYear + 1) + "月" + dayOfMonth + "日");
					}
				}, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar
						.get(Calendar.DAY_OF_MONTH)).show();
			}
		});

		// 開始時間ボタン
		Button start_btn = (Button) findViewById(R.id.start_time);
		start_btn.setOnClickListener(new View.OnClickListener() {
			/* Replace 2012/2/27 ==START== */
			@Override
			public void onClick(View v) {
				new TimePickerDialog(ReportActivity.this, new TimePickerDialog.OnTimeSetListener() {
					@Override
					public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
						// 時間が設定されたときの処理
						Date st = new Date();
						st.setTime(0);
						st.setHours(hourOfDay);
						st.setMinutes(minute);
						mReport.setmStartDate(st);
						mTextView = (TextView) findViewById(R.id.start_text);
						mTextView.setText(hourOfDay + "時" + minute + "分");
					}
				}, mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE), true)
						.show();
			}
			/* Replace 2012/2/27 ==END== */
		});

		// 終了時間ボタン
		Button end_btn = (Button) findViewById(R.id.end_time);
		end_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {

				TimePickerDialog end_picker = new TimePickerDialog(ReportActivity.this,
						new TimePickerDialog.OnTimeSetListener() {
							@Override
							public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
								// 時間が設定されたときの処理
								Date et = new Date();
								et.setTime(0);
								et.setHours(hourOfDay);
								et.setMinutes(minute);
								mReport.setmEndDate(et);
								mTextView = (TextView) findViewById(R.id.end_text);
								mTextView.setText(hourOfDay + "時" + minute + "分");
							}
						}, mCalendar.get(Calendar.HOUR_OF_DAY) + 1, mCalendar.get(Calendar.MINUTE),
						true);
				if (mReport.getmStartDate() != null) {
					end_picker.updateTime(mReport.getmStartDate().getHours() + 1,
							mCalendar.get(Calendar.MINUTE));
				}
				end_picker.show();
			}
		});

		// 区分ボタン
		Button aim_btn = (Button) findViewById(R.id.aim_button);
		final ArrayAdapter<CharSequence> adapter5 = ArrayAdapter.createFromResource(this,
				R.array.aim, android.R.layout.simple_dropdown_item_1line);

		aim_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// リスト表示する文字列
				AlertDialog.Builder kubun = new AlertDialog.Builder(ReportActivity.this);
				kubun.setTitle("区分を選択してください。");
				kubun.setAdapter(adapter5, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						mReport.setmAim((String) adapter5.getItem(which));
						mTextView = (TextView) findViewById(R.id.aim_text);
						mTextView.setText(mReport.getmAim());
					}
				});
				kubun.create();
				kubun.show();
			}
		});

		// 提供サービス　チェックボックス　
		for (int i = 0; i < mCbId.length; i++) {
			CheckBox checkBox = (CheckBox) findViewById(mCbId[i]);
			checkBox.setOnClickListener(this);
		}

		// 次へボタン
		Button btn1 = (Button) findViewById(R.id.go_checkActivity_button);
		btn1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				String errMsg = "";
				// チェック（時間）
				if (mReport.checkDateTime() == true) {
					errMsg = "時間の指定に誤りがあります。";
					Log.d(TAG, "次へボタンのonClick: " + errMsg);
				}
				// チェック（未入力）
				else if (mReport.checkEmpty() == true) {
					errMsg = "必須項目を入力してください。";
					Log.d(TAG, "次へボタンのonClick: " + errMsg);
				}

				if (errMsg.length() > 0) {
					mDialog = new AlertDialog.Builder(ReportActivity.this);
					mDialog.setTitle("入力エラー");
					mDialog.setMessage(errMsg);
					mDialog.setPositiveButton("入力画面に戻る", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							return;
						}
					});
					mDialog.create();
					mDialog.show();
				} else {
					Intent intent = new Intent(ReportActivity.this, ReportCheckActivity.class);
					intent.putExtra("report", mReport);
					startActivityForResult(intent, 0);
				}
			}
		});

		// 中断ボタン
		Button btn2 = (Button) findViewById(R.id.back_button);
		btn2.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				breakDialog();
			}
		});
	}

	/**
	 * 中断用ダイアログ
	 */
	public void breakDialog() {
		Log.d(TAG, "breakDialog: " + "中断用のダイアログが出るよ");
		mDialog = new AlertDialog.Builder(ReportActivity.this);
		mDialog.setTitle("入力を中断しますか？");
		mDialog.setMessage("保存されていない情報は消去されます。");
		mDialog.setPositiveButton("中断", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.d(TAG, "breakDialog: " + "中断用ボタンが押されたよ");
				finish();
			}
		});
		mDialog.setNegativeButton("続行", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.d(TAG, "breakDialog: " + "続行ボタンが押されたよ");
				return;
			}
		});
		mDialog.create();
		mDialog.show();
	}

	// サーバー送信後のダイアログボタンから戻ってきた時の処理
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK) {
			setResult(RESULT_OK);
			this.finish();
		} else {
			Bundle extras = data.getExtras();
			mReport = (Report) extras.getSerializable("report");
		}
	}

	// 提供サービスで呼ばれる
	@Override
	public void onClick(View v) {
		int id = v.getId();
		int i;
		for (i = 0; i < mCbId.length; i++) {
			if (id == mCbId[i]) {
				break;
			}
		}
		mReport.setmServices(((CheckBox) v).isChecked(), i);
	}

	// 端末の戻るボタンの処理
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {

		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
				breakDialog();
			}
		}
		return super.dispatchKeyEvent(event);
	}
}
