/*
 * 新規スケジュール追加画面.

 * 新規スケジュールをデータベースへ追加するダイアログ
 *
 * o-ta
 *
 */

package com.tumiki0ituki.visitcare;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;

import android.R;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.Spinner;
import android.widget.TimePicker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * 新規スケジュール作成ダイアログ.
 */
public class AddSchedleDialog extends AlertDialog {

	private static boolean testflg;
	
	/** WebAPIリクエストタイプ. */
	private static final String REQUEST_TYPE = "新規";

	/** ログタグ. */
	private static final String TAG = AddSchedleDialog.class.getName();

	/** タイムアウト値. */
	private static final int TIMEOUT = 10000;

	/** ストリーミングバッファーサイズ. */
	private static final int STREAMBUFFSIZE = 4096;

	/** 日付フォーマット. */
	private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

	/** 訪問先選択スピナー. */
	private Spinner mSpinner;

	/** 日付選択ピッカー. */
	private DatePicker datePicker;

	/** 時間選択ピッカー. */
	private TimePicker timePicker;

	/** 送信ボタン. */
	private List<User> userList;

	/** スピナーアイテム. */
	private ArrayList<String> mSpinnerItems = new ArrayList<String>();

	/** スピナーアイテム. */
	private ArrayList<Map<String, String>> mSpinnerItems2 = new ArrayList<Map<String, String>>();

	/** 選択訪問先. */
	private String mSelectedUserID;

	/** 呼び出し元コンテキスト. */
	private Context mContext;

	/** 訪問日時. */
	private String mVisitDate;

	/** スタッフID. */
	private String mStaffID;
	private Thread getUserListThread;

	/**
	 * スケジュールダイアログコンストラクタ.
	 *
	 * @param context 呼び出し元コンストラクタ
	 */
	public AddSchedleDialog(Context cnt) {
		super(cnt);
		Log.d(TAG, "Constructor");
		/* コンテキスト保持 */
		mContext = cnt;
	}

	@Override
	public void show() {
		Log.d(TAG, "show START");
		testflg = false;
		View v = this.getLayoutInflater().inflate(R.layout.addschedule, null);

		/* ダイアログタイトル */
		setTitle(mContext.getString(R.string.dialog_title));

		/* ウィジェットリソース取得 */
		mSpinner = (Spinner) v.findViewById(R.id.userListSpinner);
		mSpinner.setVisibility(View.INVISIBLE);
		datePicker = (DatePicker) v.findViewById(R.id.datePiker);
		timePicker = (TimePicker) v.findViewById(R.id.timePiker);
		timePicker.setIs24HourView(true);
		timePicker.setCurrentMinute(0);
		setView(v);

		/* ボタン設定 */
		setButton(BUTTON_POSITIVE, mContext.getString(R.string.submit), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.d(TAG, "BUTTON_POSITIVE onClick");
				new Thread(new Runnable() {
					Handler hnd = new Handler();

					@Override
					public void run() {
						hnd.post(new Runnable() {
							@Override
							public void run() {
								Log.d(TAG, "PostThread START");
								getDate();
								if (postNewSchedle()) {
									new AlertDialog.Builder(mContext).setTitle("送信結果")
											.setMessage("データは正常に送信されました。")
											.setPositiveButton("OK", null).show();
								} else {
									new AlertDialog.Builder(mContext).setTitle("送信エラー")
											.setMessage("データの送信に失敗しました。")
											.setPositiveButton("OK", null).show();
								}
								Log.d(TAG, "PostThread END");
							}
						});
					}
				}).start();
			}
		});
		setButton(BUTTON_NEGATIVE, mContext.getString(R.string.cancel), new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Log.d(TAG, "BUTTON_NEGATIVE onClick");
			}
		});
		super.show();

		final ProgressDialog pDialog;
		pDialog = new ProgressDialog(this.getContext());
		pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pDialog.setMessage("処理を実行中です...");
		pDialog.setCancelable(true);
		pDialog.show();
		pDialog.setOnCancelListener(new OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				Log.d(TAG, "ProgressDialog onCancel");
				testflg = true;
				dismiss();
			}
		});

		getUserListThread = new Thread(new Runnable() {
			Handler hnd = new Handler();

			@Override
			public void run() {
				boolean ret;

				/* 訪問先一覧取得 - 通信 */
				ret = getUserList();
				Log.d(TAG, "Back from getUserList" + "flg" + Boolean.toString(testflg));
				if (!testflg) {
					if (ret) {
						hnd.post(new Runnable() {
							@Override
							public void run() {
								initSpinner();
								mSpinner.setVisibility(View.VISIBLE);
								pDialog.dismiss();
							}
						});
					} else {
						hnd.post(new Runnable() {
							@Override
							public void run() {
								new AlertDialog.Builder(mContext).setTitle("通信エラー")
										.setMessage("訪問先リストの取得に失敗しました。").setPositiveButton("OK", null)
										.show();
								pDialog.dismiss();
								dismiss();
							}
						});
					}
				}
				Log.d(TAG, "Postrun END");
			}
		});
		getUserListThread.start();

		Log.d(TAG, "show END");
	}

	/**
	 * スピナー生成処理.
	 */
	private void initSpinner() {
		Log.d(TAG, "initSpinner-START");

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(mContext,
				R.layout.addschedule_spinner_items);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		// アイテムを追加します
		for (String buff : mSpinnerItems) {
			adapter.add(buff);
		}
		// アダプターを設定します
		mSpinner.setAdapter(adapter);

		// スピナーのアイテムが選択された時に呼び出されるコールバックリスナーを登録します
		mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				// 選択アイテム取得
				String item = (String) parent.getSelectedItem();
				Log.d(TAG, "Selection:" + item);
				Map<String, String> map = mSpinnerItems2.get(position);
				mSelectedUserID = map.get("user_id");
				Log.d(TAG, "SelectedUserID:" + mSelectedUserID);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				Log.d(TAG, "onNothingSelected");
			}
		});
	}

	/**
	 * ユーザーリスト取得処理.
	 *
	 * @return ret 通信結果　True:成功　False:失敗
	 */
	private boolean getUserList() {
		Log.d(TAG, "getUserList START");
		boolean ret = false;
		// WebAPIの実行（HTTP通信）
		HttpResponse objResponse = getHttpSV(mContext.getString(R.string.userurl));
		if (objResponse != null
				&& objResponse.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK) {
			// リストの設定
			makeAdapterItems(objResponse);
			ret = true;
		} else {
			Log.d(TAG, "===== httpRes is NG =====");
		}
		Log.d(TAG, "getUserList END");
		return ret;
	}

	/**
	 * 新規スケジュール送信.
	 *
	 */
	private boolean postNewSchedle() {
		Log.d(TAG, "postNewSchedle START");
		boolean ret = false;
		/* レスポンス */
		try {
			HttpResponse res = postHttpSV(mContext.getString(R.string.scheduleurl));
			if (res != null && res.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK) {
				Log.d(TAG, "HttpConectionSuccessful!!");
				ret = true;
			} else {
				Log.d(TAG, "HttpConnectionError");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		Log.d(TAG, "postNewSchedle END");
		return ret;
	}

	/**
	 * HTTP通信接続処理-ユーザーリスト取得.
	 *
	 * @param purl 接続先URL
	 * @return レスポンス
	 * @throws IOException 入出力例外
	 */
	private HttpResponse getHttpSV(String purl) {
		Log.d(TAG, "getHttpSV START");
		HttpResponse res = null;
		// クライアントインスタンス化
		HttpClient objCli = new DefaultHttpClient();
		// 通信処理実行
		HttpGet objGet = new HttpGet(purl);

		// パラメータの生成
		HttpParams para = objGet.getParams();
		HttpParams clipara = objCli.getParams();

		// 接続のタイムアウトの設定
		HttpConnectionParams.setConnectionTimeout(clipara, TIMEOUT);
		// データ取得のタイムアウトの設定
		HttpConnectionParams.setSoTimeout(clipara, TIMEOUT);

		para.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT);
		para.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, TIMEOUT);
		para.setLongParameter(ConnManagerPNames.TIMEOUT, TIMEOUT);

		try {
			res = objCli.execute(objGet);
		} catch (Exception e) {
			e.printStackTrace();
			res = null;
		} finally {
			objGet.abort();
			objCli.getConnectionManager().shutdown();
			Log.d(TAG, "HtttpGet abort & Client shutdown");
		}
		Log.d(TAG, "getHttpSV END");
		return res;
	}

	/**
	 * HTTP通信接続処理-新規スケジュール送信.
	 *
	 * @param purl 接続先URL
	 * @return レスポンス
	 * @throws IOException 入出力例外
	 */
	private HttpResponse postHttpSV(String purl) throws Exception {
		Log.d(TAG, "postHttpSV-START");
		HttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost(purl);

		// パラメータの生成
		HttpParams para = httppost.getParams();

		para.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, TIMEOUT);
		para.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, TIMEOUT);
		para.setLongParameter(ConnManagerPNames.TIMEOUT, TIMEOUT);

		/* 追加Post処理 */
		Log.d(TAG, "URL:" + purl);

		List<NameValuePair> nvp = new ArrayList<NameValuePair>();
		nvp.add(new BasicNameValuePair("staff_id", mStaffID));
		nvp.add(new BasicNameValuePair("type", REQUEST_TYPE));
		nvp.add(new BasicNameValuePair("user_id", mSelectedUserID));
		nvp.add(new BasicNameValuePair("schedule_time", mVisitDate));

		httppost.setEntity(new UrlEncodedFormEntity(nvp, HTTP.UTF_8));

		// HTTP通信実行
		HttpResponse res = httpclient.execute(httppost);
		return res;
	}

	/**
	 * スピナーアイテム追加処理.
	 *
	 * @param httpRes 通信レスポンス-　JSONString
	 */
	private void makeAdapterItems(HttpResponse httpRes) {
		Log.d(TAG, "getDate-START");
		String jsonString = null;
		try {
			// ストリーム->文字列（JSON）変換
			InputStream stream = httpRes.getEntity().getContent();
			jsonString = convertStreamToString(stream);

		} catch (Exception e) {
			Log.d(TAG, "Json変換に失敗");
			e.printStackTrace();
			return;
		}
		Log.d(TAG, jsonString);
		// JSON->GSON変換
		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.create();
		userList = gson.fromJson(jsonString, new TypeToken<List<User>>() {
		}.getType());
		Map<String, String> data;
		// アダプター更新
		for (User user : userList) {
			/* ＩＤ付データ */
			data = new HashMap<String, String>();
			data.put("user_id", user.getUser_id());
			data.put("name", user.getUser_name());
			mSpinnerItems2.add(data);
			mSpinnerItems.add(user.getUser_name());
		}
		Log.d(TAG, "getDate-END");
	}

	/**
	 * 日付取得処理.
	 *
	 */
	private void getDate() {
		Log.d(TAG, "getDate-START");

		Calendar cal = Calendar.getInstance();
		cal.set(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth(),
				timePicker.getCurrentHour(), timePicker.getCurrentMinute(), 0);
		MessageFormat mf = new MessageFormat("{0,date," + DATE_PATTERN + "}");
		Object[] objs = { cal.getTime() };
		mVisitDate = mf.format(objs);
		Log.d(TAG, "日付:" + mVisitDate);
		Log.d(TAG, "getDate-END");
	}

	/**
	 * ストリーム⇒String　変換処理.
	 *
	 * @param is インプットストリーム
	 * @return string　変換後String
	 * @throws IOException 入出力例外
	 */
	private String convertStreamToString(InputStream is) throws IOException {
		Log.d(TAG, "convertStreamToString-START");
		if (is == null) {
			Log.d(TAG, "InputStream is null");
			return "";
		}
		int n;
		char[] buffer = new char[STREAMBUFFSIZE];
		Writer writer = new StringWriter();
		try {
			Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			while ((n = reader.read(buffer)) != -1) {
				// Log.d(TAG, "Buffer：" + n);
				writer.write(buffer, 0, n);
			}
		} finally {
			is.close();
		}
		Log.d(TAG, "convertStreamToString-END");
		return writer.toString();
	}

	/**
	 * スタッフＩＤ登録.
	 *
	 * @return スタッフID
	 */
	public String getmStaffID() {
		Log.d(TAG, "getmStaffID");
		return mStaffID;
	}

	/**
	 * スタッフＩＤ登録.
	 *
	 * @param pStuffID スタッフＩＤ
	 */
	public void setmStuffID(String pStuffID) {
		Log.d(TAG, "setmStaffID");
		this.mStaffID = pStuffID;
	}

	/*
	 * (非 Javadoc)
	 * @see android.app.Dialog#onStop()
	 */
	@Override
	protected void onStop() {
		Log.d(TAG, "onStop");
		super.onStop();
	}

}
