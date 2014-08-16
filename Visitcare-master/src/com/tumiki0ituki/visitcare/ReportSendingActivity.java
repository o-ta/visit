/*
 * 日報、３画面目（送信画面）
 * 入力情報を一覧表示し、送信ボタンでサーバーへ送信
 *
 * o-ta
 *
 */

package com.tumiki0ituki.visitcare;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

/**
 * 確認画面、サーバーに送信
 * 
 */
public class ReportSendingActivity extends Activity {
	private static final String URI = "http://japadroid.appspot.com/api/v1/regist/schedule.jsp";
	/** Called when the activity is first created. */

	/**
	 * 各入力情報のgetter,setterクラス。 このクラスごとIntentで渡す。
	 */
	private Report mReport;

	SimpleDateFormat mDispDate = new SimpleDateFormat("yyyy年MM月dd日");
	SimpleDateFormat mDispTime = new SimpleDateFormat("HH時mm分");
	// SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	SimpleDateFormat mDf1 = new SimpleDateFormat("yyyy-MM-dd");
	SimpleDateFormat mDf2 = new SimpleDateFormat("HH:mm:ss");

	String mKeys[] = { "日付", "開始時間", "終了時間", "区分" };
	String mKeys2[] = { "歩行", "移動", "会話", "食事", "睡眠", "内容" };

	/**
	 * テキストビューのID
	 */
	int mTvs[] = { R.id.walk_text, R.id.move_text, R.id.talk_text, R.id.eat_text, R.id.sleep_text };

	// private static String TAG;
	private static String TAG = ReportSendingActivity.class.getName();
	private ProgressDialog progressDialog;
	HttpResponse mResponse;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.report_sending);

		// TAG = ReportSendingActivity.class.getName();

		Bundle extras = getIntent().getExtras();
		mReport = (Report) extras.getSerializable("report");

		// タイトルバーに該当業務の日付と被介護者名を表示
		setTitle(mReport.getmWorkDay() + " " + mReport.getmCareName());

		// 送信ボタン(ここでHTTP通信を行うようにします)
		Button btn1 = (Button) findViewById(R.id.send_button);
		btn1.setText("送信");
		btn1.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// 通信中ダイアログを表示させる。
				progressDialog = new ProgressDialog(ReportSendingActivity.this);
				progressDialog.setTitle("通信中");
				progressDialog.setMessage("データ送信中・・・");
				progressDialog.setIndeterminate(false);
				progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "キャンセル",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								// ProgressDialog をキャンセル
								dialog.cancel();
							}
						});
				// ProgressDialog のキャンセルされた時に呼び出されるコールバックを登録
				progressDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						// Thread を停止
						onStop();
					}
				});
				progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				progressDialog.show();

				Thread thread = new Thread(null, new Runnable() {
					// Handler testhand = new Handler();
					public void run() {
						Log.d(TAG, "run now...：" + "run");
						if (sending()) {
							handler.sendEmptyMessage(0);
						} else {
							handler.sendEmptyMessage(1);
						}
					}
				});
				thread.start();
			}
		});

		// 戻るボタン 前の画面に戻るのみ
		Button btn2 = (Button) findViewById(R.id.back_button);
		btn2.setText("戻る");
		btn2.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ReportSendingActivity.this, ReportCheckActivity.class);
				intent.putExtra("report", mReport);
				setResult(RESULT_CANCELED, intent);
				finish();
			}
		});

		TextView textView;

		// 日付表示
		Date date = (Date) mReport.getmDate();
		TextView text = (TextView) findViewById(R.id.date_text);
		text.setText(mDispDate.format(date));

		// 開始時間表示
		Date startDate = (Date) mReport.getmStartDate();
		textView = (TextView) findViewById(R.id.start_text);
		textView.setText(mDispTime.format(startDate));

		// 終了時間表示
		Date endDate = (Date) mReport.getmEndDate();
		textView = (TextView) findViewById(R.id.end_text);
		textView.setText(mDispTime.format(endDate));

		// 区分表示
		textView = (TextView) findViewById(R.id.aim_text);
		textView.setText(mReport.getmAim());

		// 提供サービス表示
		String service_name[] = { "入浴", "掃除", "洗濯", "買い物", "一般調理", "衣服整理" };
		String tx = "";
		textView = (TextView) findViewById(R.id.service_text);
		for (int i = 0; i < service_name.length; i++) {
			if (mReport.ismServices(i) == true) {
				tx = tx + service_name[i] + "\n";
				;
			}
		}
		textView.setText(tx);

		// 状態チェック表示
		ArrayAdapter<CharSequence> adapter5 = ArrayAdapter.createFromResource(
				ReportSendingActivity.this, R.array.check,
				android.R.layout.simple_dropdown_item_1line);

		for (int i = 0; i < mTvs.length; i++) {
			if (mReport.getmStateCheck(i) != -1) {
				textView = (TextView) findViewById(mTvs[i]);
				textView.setText((String) adapter5.getItem(mReport.getmStateCheck(i)));
			}
		}

		// 備考表示
		if (mReport.getmNote() != null) {
			textView = (TextView) findViewById(R.id.note_text);
			textView.setText(mReport.getmNote());
		}
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			Log.d("MainHandler", "msg:" + Integer.toString(msg.what));
			if (msg.what == 0) {
				// 処理終了時の動作をここに記述。
				AlertDialog.Builder dlg = new AlertDialog.Builder(ReportSendingActivity.this);
				dlg.setTitle("送信完了");
				dlg.setMessage("日報画面を終了します。");
				dlg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						setResult(RESULT_OK);
						finish();
					}
				});
				dlg.create();
				dlg.show();
			} else {
				AlertDialog.Builder dlg = new AlertDialog.Builder(ReportSendingActivity.this);
				dlg.setTitle("送信失敗");
				dlg.setMessage("サーバーからの応答がありません。");
				dlg.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						Log.d("erDialog", "onclick");
					}
				}).show();
			}
			// プログレスダイアログ終了
			progressDialog.dismiss();
		}
	};

	/**
	 * サーバーに送信する処理
	 * 
	 */
	public boolean sending() {
		Log.d(TAG, "sending:" + "sending");
		boolean ret = false;

		// 戻り値の初期化
		@SuppressWarnings("unused")
		HttpResponse res = null;

		HttpClient objCli = new DefaultHttpClient();
		// パラメータの生成
		HttpParams para = objCli.getParams();

		// 接続のタイムアウトの設定
		HttpConnectionParams.setConnectionTimeout(para, 3000);
		// データ取得のタイムアウトの設定
		HttpConnectionParams.setSoTimeout(para, 3000);

		HttpPost httppost = new HttpPost(URI);

		// 送信するパラメータを設定
		List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

		nameValuePairs.add(new BasicNameValuePair("type", "日報"));

		nameValuePairs.add(new BasicNameValuePair("work_id", String.valueOf(mReport.getmWorkId())));
		Log.d(TAG,
				"workis String.valueOf(mReport.getmWorkId())) = "
						+ String.valueOf(mReport.getmWorkId()));

		nameValuePairs.add(new BasicNameValuePair("start_time", mDf1.format(mReport.getmDate())
				+ " " + mDf2.format(mReport.getmStartDate())));
		nameValuePairs.add(new BasicNameValuePair("end_time", mDf1.format(mReport.getmDate()) + " "
				+ mDf2.format(mReport.getmEndDate())));
		nameValuePairs.add(new BasicNameValuePair("aim", mReport.getmAim()));

		nameValuePairs.add(new BasicNameValuePair("bath", String.valueOf(mReport
				.ismServices(mReport.IDX_BATH))));
		nameValuePairs.add(new BasicNameValuePair("clean", String.valueOf(mReport
				.ismServices(mReport.IDX_CLEAN))));
		nameValuePairs.add(new BasicNameValuePair("wash", String.valueOf(mReport
				.ismServices(mReport.IDX_WASH))));
		nameValuePairs.add(new BasicNameValuePair("shopping", String.valueOf(mReport
				.ismServices(mReport.IDX_SHOPPING))));
		nameValuePairs.add(new BasicNameValuePair("cook", String.valueOf(mReport
				.ismServices(mReport.IDX_COOK))));
		nameValuePairs.add(new BasicNameValuePair("wear", String.valueOf(mReport
				.ismServices(mReport.IDX_WEAR))));

		nameValuePairs
				.add(new BasicNameValuePair("walk", String.valueOf(mReport.getmStateCheck(0))));
		nameValuePairs
				.add(new BasicNameValuePair("move", String.valueOf(mReport.getmStateCheck(1))));
		nameValuePairs
				.add(new BasicNameValuePair("talk", String.valueOf(mReport.getmStateCheck(2))));
		nameValuePairs
				.add(new BasicNameValuePair("eat", String.valueOf(mReport.getmStateCheck(3))));
		nameValuePairs.add(new BasicNameValuePair("sleep",
				String.valueOf(mReport.getmStateCheck(4))));

		nameValuePairs.add(new BasicNameValuePair("note", mReport.getmNote()));
		Log.d(TAG, "sending　now...：" + "通信");

		try {
			httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
			mResponse = objCli.execute(httppost);

			Log.d(TAG, "送った日報内容：" + nameValuePairs);
			Log.d(TAG, "送れた");

			ret = true;

		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			Log.d(TAG, "エラーをキャッチUnsupportedEncodingException");
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			Log.d(TAG, "エラーをキャッチClientProtocolException");
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
			Log.d(TAG, "エラーをキャッチSocketTimeoutException");
		} catch (IOException e) {
			e.printStackTrace();
			Log.d(TAG, "エラーをキャッチIOException");

		}
		// Log.d(TAG, "response：" + mResponse.getStatusLine().toString() +
		// mResponse.getAllHeaders().toString());
		// Log.d(TAG,
		// "HttpConnectionError ResponseCode:"
		// + Integer.toString(response.getStatusLine().getStatusCode()));
		return ret;
	}

	// 端末の戻るボタンの処理
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {

		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
				Intent intent = new Intent(ReportSendingActivity.this, ReportCheckActivity.class);
				intent.putExtra("report", mReport);
				setResult(RESULT_CANCELED, intent);
				finish();
			}
		}
		return super.dispatchKeyEvent(event);
	}
}
