/*
 * 現在位置情報送信のサービス
 * 設定された間隔ごとにサーバーへ自分の現在位置を送信する
 *
 * o-ta
 *
 */

package com.tumiki0ituki.visitcare;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import android.R;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

public class SendLocationServise extends Service implements LocationListener {
	/** クラス名 */
	private static final String TAG = SendLocationServise.class.getName();

	/** ロケーションマネージャ */
	private LocationManager mLocationManager;

	/** double型:緯度 */
	private double mLatitude;

	/** double型：軽度 */
	private double mLongitude;

	/** int型：更新間隔 */
	private int mRelordtime;

	/** int型:スタッフＩＤ */
	private int mStaffId = 2;

	/** ノティフィケーションマネージャ */
	private NotificationManager mNotificationManager;

	/** ノティフィケーション */
	private Notification mNotification;

	private static final String URI = "http://japadroid.appspot.com//api/v1/regist/location.jsp";

	/** デフォルト値：更新時間 */
	public static final String DEFAULT_TIME = "30";

	/** キー：更新時間 */
	public static final String KEY_RELORDTIME = "relordtime";

	/** タイムアウト値. */
	private static final int TIMEOUT = 10000;



	@Override
	public IBinder onBind(Intent intent) {
		// TODO 自動生成されたメソッド・スタブ
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate");
	}

	@Override
	public void onStart(Intent intent, int startId) {
		Log.d(TAG, "startId" + Integer.toString(startId));
		Log.d(TAG, "■■■■■■■onStart■■■■■■■");
		super.onStart(intent, startId);

		String intentAction = intent.getAction(); // 現在の状態を取得

		mStaffId = intent.getIntExtra("STAFF_ID", mStaffId); //スタッフＩＤをインテントから取得
		Log.d(TAG, "STAFF_ID: " + mStaffId);

		// プリファレンスから更新間隔を取得
		mRelordtime = getReloadtime(this);
		Log.d(TAG, "更新間隔--->" + mRelordtime);

		// アラームマネージャの実装
		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

		intent.setAction("interval");

		 // 自分自身を呼び出す
		PendingIntent pi = PendingIntent.getService(this, 0, new Intent("interval", null, this, SendLocationServise.class), 0);

		// ノーティフィケーションマネージャの実装
		mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		// ロケーションマネージャの実装
		mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

		// サービスの状態がインターバル（生きている間）の処理
		if (intentAction.equals("start") || intentAction.equals("interval")) {
			Log.d(TAG, "■■■■■■■Start or interval■■■■■■■");
//			 this.mRelordtime = intent.getIntExtra(getString(R.string.sl_Reloadtime_key), mRelordtime);
			Log.d(TAG, "更新間隔:" + mRelordtime);

			// 更新間隔（分）をミリ秒に変換
			 int minTime = (mRelordtime * 60 * 1000); 
			 Log.d(TAG, "更新間隔（ミリ秒）：" + minTime);

			 boolean gps_para = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			 Log.d(TAG,"プロバイダーの状態--->"+Boolean.toString(gps_para));

			// GPS機能がオフの時
			if (!mLocationManager
					.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				Toast.makeText(this, getString(R.string.sl_GPS_error),
						Toast.LENGTH_LONG).show();
				return;
			}

			// onLocationChangedを呼び出す
			mLocationManager.requestLocationUpdates(
					LocationManager.GPS_PROVIDER, 0, 0, this);

			// ノーティフィケーションの作成
			mNotification = new Notification();

			// 表示するアイコンの指定
			mNotification.icon = R.drawable.ic_launcher;

			// 表示する際のティッカーテキストを指定
			mNotification.tickerText = getString(R.string.sl_Notification_ticker_text);

			// ノーティフィケーション一覧画面での表示内容、動作を設定
			mNotification.setLatestEventInfo(getApplicationContext(),
					getString(R.string.app_name), getString(R.string.sl_Notification_text), pi);

			// ノーティフィケーションを発行して表示する
			mNotificationManager.notify(1, mNotification);

			// 一定時間後に再度自身を呼び出す
			alarmManager.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
					SystemClock.elapsedRealtime() + minTime, pi);
		} else if (intentAction.equals("stop")) {
			Log.d(TAG, "■■■■■■■■■■■■STOP■■■■■■■■■■■■■");
			alarmManager.cancel(pi); // アラームマネージジャをデストローイ！
			mNotificationManager.cancelAll(); // ノティフィケーションをデストローイ！
			stopSelf(); // 　サービスもデストローイ！
		}
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "■■■■■■■onDestroy■■■■■■■■");
		mLocationManager.removeUpdates(this); // ロケーションリスナーを抹殺
		super.onDestroy();

	}

	/**
	 * GPS情報が変化した場合に呼び出されるコールバック関数
	 */
	@Override
	public void onLocationChanged(Location location) {
		// TODO 自動生成されたメソッド・スタブ
		Log.d(TAG, "■■■■■■■■■onLocationChanged■■■■■■■■■■");

		mLatitude = location.getLatitude();// 　緯度を取得
		Log.d("Latitude（緯度）", String.valueOf(mLatitude));
		mLongitude = location.getLongitude();// 経度を取得
		Log.d("Longitude(経度)", String.valueOf(mLongitude));
		try {
			postHttpSV(mLatitude, mLongitude, mStaffId);
		} catch (ClientProtocolException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} catch (IOException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		}
		mLocationManager.removeUpdates(this);

		Log.d(TAG, "■■■■■■■■■onLocationChanged　　END■■■■■■■■■■");
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO 自動生成されたメソッド・スタブ
		Log.d(TAG, "■■■■■■■■■■onStatusChanged■■■■■■■■■■■");

	}

	@Override
	public void onProviderEnabled(String provider) {
		// TODO 自動生成されたメソッド・スタブ
		Log.d(TAG, "■■■■■■■■■■■onProviderEnabled■■■■■■■■■■■");

	}

	@Override
	public void onProviderDisabled(String provider) {
		// TODO 自動生成されたメソッド・スタブ
		Log.d(TAG, "■■■■■■■■■■■■■■onProviderDisabled■■■■■■■■■■■■■■■");

	}

	/**
	 * HTTP通信を実行します
	 */
	private HttpResponse postHttpSV(double latitude, double longitude,
			int staffid) throws  IOException {
		Log.d(TAG, "■■■■■■postHttpSV■■■■■");
		// 戻り値の初期化
		HttpResponse res = null;
		// パラメータの生成
		HttpParams para = new BasicHttpParams();
		// 接続のタイムアウトの設定
		HttpConnectionParams.setConnectionTimeout(para, TIMEOUT);
		// データ取得のタイムアウトの設定
		HttpConnectionParams.setSoTimeout(para, TIMEOUT);

		HttpClient httpclient = new DefaultHttpClient(para);
		HttpPost httppost = new HttpPost(URI);

		// 送信するパラメータを設定
		List<NameValuePair> locationValuePairs = new ArrayList<NameValuePair>();
		locationValuePairs.add(new BasicNameValuePair("latitude", String
				.valueOf(mLatitude)));
		locationValuePairs.add(new BasicNameValuePair("longitude", String
				.valueOf(mLongitude)));
		locationValuePairs.add(new BasicNameValuePair("staff_id", Integer
				.toString(mStaffId)));

		httppost.setEntity(new UrlEncodedFormEntity(locationValuePairs,
				HTTP.UTF_8));

		// HTTP通信実行
		res = httpclient.execute(httppost);
		res.getEntity();
		if (res.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK) {

			Log.d(TAG, "HttpConnectionError");
			Log.d(TAG, res.getEntity().toString());
		} else {
			Log.d(TAG, "■■■■■■■■■■■■HttpConectionSuccessful!!■■■■■■■■■■■■");
			Log.d(TAG, res.getEntity().toString());
		}
		Log.d(TAG, "■■■■■■postHttpSV  END■■■■■");
		Log.d(TAG, "■■■■■■戻り値　res --->" + res.toString() + "■■■■■");

		return res;
	}

	/**
	 * 更新時間を取得します｡
	 *
	 * @param context
	 *            アプリケーション情報
	 * @return 設定されている送信間隔（未設定の場合は{@link #DEFAULT_TIME}）
	 */
	public static int getReloadtime(Context context) {
		Log.d(TAG, "getReloadtime  Start!");

		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(context);
		Log.d(TAG, "プリファレンス --->" + sharedPreferences);

		// プリファレンスに保存されている文字を数字にしてセット
		int reloadtime = Integer.parseInt(sharedPreferences.getString(
				KEY_RELORDTIME, DEFAULT_TIME));

		Log.d(TAG, "getReloadtime  END");
		Log.d(TAG, "戻り値：name --->" + reloadtime);
		return reloadtime;
	}

}
