/*
 * SendLocation
 * 自分の現在位置を送信する機能のON/OFF切り替え
 *
 * o-ta
 *
 */
package com.tumiki0ituki.visitcare;

import java.util.List;

import android.R;
import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.util.Log;

public class SendLocationActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {

	/** キー：更新時間 */
	public static final String KEY_RELORDTIME = "relordtime";

	/** キー：自動送信 */
	public static final String KEY_AUTO_SEND = "auto_send";

	/** デフォルト値：更新時間 */
	public static final String DEFAULT_TIME = "30";

	/** タグ：クラス名 */
	private static final String TAG = SendLocationActivity.class.getName();

	/** サービス名：SendLocationServise */
	private static final String mServiceName = SendLocationServise.class
			.getCanonicalName();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		// PreferenceActivityに自前の設定xmlを設定
		addPreferencesFromResource(R.xml.sendloc_setting);

		// チェックボックスのON / OFF状態のセット
		initChecked(isServiceRunning());

		// チェックボックスのサマリーの設定
		initSummary(KEY_AUTO_SEND);

		// エディットテキストのサマリーの設定
		initSummary(KEY_RELORDTIME);
	}

	/**
	 * チェックボックスの状態の初期設定をします
	 *
	 * @param boolean型
	 *            : サービスが起動しているかどうか
	 */
	public void initChecked(boolean isStart_service) {

		// チェックボックスプリファレンスの取得
		CheckBoxPreference checkbox_preference = (CheckBoxPreference) getPreferenceScreen()
				.findPreference(KEY_AUTO_SEND);

		// 既にサービスが起動していればチェックボックスをONに、起動していなければOFFの状態にする
		if (isStart_service) {
			checkbox_preference.setChecked(true);
			Log.d(TAG, "サービスが現在起動している状態かどうか　---> true");
		} else {
			checkbox_preference.setChecked(false);
			Log.d(TAG, "サービスが現在起動している状態かどうか　---> false");
		}
	}

	/**
	 * サマリーを初期化します
	 *
	 * @param key
	 *            プリファレンスに登録するためのキーで判別
	 */
	public void initSummary(String key) {
		Log.d(TAG, "initSummary  Start!");

		if (key.equals(KEY_AUTO_SEND)) {

			Log.d(TAG, "initSummary(チェックボックス側　Start!)");
			CheckBoxPreference checkbox_preference = (CheckBoxPreference) getPreferenceScreen()
					.findPreference(KEY_AUTO_SEND);

			// チェックボックスがONの時とOFFの時のサマリーの設定
			checkbox_preference.setSummaryOn(R.string.sl_checkon_summary);
			checkbox_preference.setSummaryOff(R.string.sl_checkoff_summary);

			Log.d(TAG, "initSummary(チェックボックス側　End)");

		} else if (key.equals(KEY_RELORDTIME)) {
			Log.d(TAG, "initSummary(エディットテキスト側　Start!)");

			// エディットテキストプリファレンスの取得
			SendLocationDialog edittext_preference = (SendLocationDialog) getPreferenceScreen()
					.findPreference(KEY_RELORDTIME);

			// エディットテキストのサマリー設定
			edittext_preference.setSummary("現在"
					+ getReloadtime(SendLocationActivity.this) + "分に設定されています");
			Log.d(TAG, "initSummary(エディットテキスト側　End)");
		}
		Log.d(TAG, "initSummary  End");
	}

	/**
	 * 現在起動中のサービスがあるかどうかを調べます
	 *
	 * @param なし
	 *
	 * @return 起動しているサービスがある：true 　　起動しているサービスがない：false
	 */
	private boolean isServiceRunning() {
		Log.d(TAG, "isServiceRunning  Start!");

		// アクティビティマネージャの取得
		ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		List<RunningServiceInfo> services = activityManager
				.getRunningServices(Integer.MAX_VALUE);

		// 起動しているサービスがあるかどうかチェックする
		for (RunningServiceInfo info : services) {
			if (mServiceName.equals(info.service.getClassName())) {
				Log.d(TAG, "起動しているサービス--->" + info.service.getClassName());
				return true;
			}
		}
		Log.d(TAG, "isServiceRunning  End");
		return false;
	}

	/**
	 * プリファレンスに新しい値が設定された時に呼び出されます.
	 */
	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// TODO 自動生成されたメソッド・スタブ
		if (key.equals(KEY_RELORDTIME) == true) {
			Log.d(TAG, "onSharedPreferenceChanged--- EditText編----  Start!");

			// エディットテキスｓトのサマリーとチェックボックスの状態の再設定
			initSummary(KEY_RELORDTIME);
			initChecked(isServiceRunning());
		}

		if (key.equals(KEY_AUTO_SEND) == true) {
			Log.d(TAG, "onSharedPreferenceChanged--- CheckBox編----  Start!");

			CheckBoxPreference checkbox_preference = (CheckBoxPreference) getPreferenceScreen()
					.findPreference(KEY_AUTO_SEND);

			// チェックボックスのサマリーの再設定
			initSummary(KEY_AUTO_SEND);

			// OFF -> OＮになった時の処置
			if (checkbox_preference.isChecked()) {
				// サービスをスタートさせる
				Intent intent = new Intent(SendLocationActivity.this,
						SendLocationServise.class);
				intent.setAction("start");
				intent.putExtra(getString(R.string.sl_Reloadtime_key),
						getReloadtime(SendLocationActivity.this));
				Log.d(TAG, "更新間隔(Reloadtime)--->"
						+ getReloadtime(SendLocationActivity.this));
				startService(intent);

				// ON -> OFFになった時の処理
			} else if (!(checkbox_preference.isChecked())) {
				// サービスを停止させる
				Intent intent = new Intent(SendLocationActivity.this,
						SendLocationServise.class);
				intent.setAction("stop");

				startService(intent);
			}
		}
		Log.d(TAG, "onSharedPreferenceChanged  End");
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

	@Override
	protected void onResume() {
		super.onResume();
		// OnSharedPreferenceChangeListenerをレジスト（決まり文句）
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		// OnSharedPreferenceChangeListenerをアンレジスト（決まり文句）
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}
}
