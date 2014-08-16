/*
 * 他従業員の位置情報
 * 自分を含めた全スタッフの位置をマップ上に表示する
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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.R;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class ViewLocationActivity extends MapActivity implements
		OnItemSelectedListener {

	/** マップビュー */
	private MapView mMapView;

	/** Overlay型のリスト */
	private List<Overlay> mMapOverlays;

	/** 赤のマーカー */
	private Drawable mDrawable_red;

	/** 緑のマーカー */
	private Drawable mDrawable_green;

	/** 赤のマーカーを乗せたItemizedOverlay */
	private CustomItemizedOverlay<CustomOverlayItem> mItemizedOverlay_red;

	/** 緑のマーカーを乗せたItemizedOverlay */
	private CustomItemizedOverlay<CustomOverlayItem> mItemizedOverlay2_green;

	/** 位置情報を格納したList */
	private ArrayList<GeoPoint> mGeolist;

	/** バルーンに表示する情報を格納したList */
	private ArrayList<CustomOverlayItem> mOverlayList;

	/** 全スタッフの情報を取得するURI */
	private static final String URI_STAFF = "http://japadroid.appspot.com/api/v1/request/staff.jsp?staff_id=-1&location=true";

	/** タグ：名前 */
	private static final String TAG = ViewLocationActivity.class.getName();

	/** スタッフの名前を選択するスピナー */
	private Spinner mSelectSpinner;

	/** タイムアウト値. */
	private static final int TIMEOUT = 10000;

	/** HttpGetのobject */
	private HttpGet mObjGet;

	/** 前回選択された（赤マーカーを置いた）スタッフのposition */
	private int mLastposition = 0;

	/** スピナーにアイテムをセットするアダプター */
	private ArrayAdapter<CharSequence> mAdapter;

	/** マップコントローラー */
	private MapController mMc;

	/** スタッフID */
	private int mStaffid = 2;
	
	/** カレンダークラス*/
	private Calendar timeCal;
	
	/** HttpResponseのobject */
	private HttpResponse objResponse;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "onCreate　Start!");

		super.onCreate(savedInstanceState);

		// タイトルバーの非表示
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// フルスクリーン
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

		setContentView(R.layout.viewlocation);


		// 現在の時刻を取得
		timeCal = Calendar.getInstance();

		/* R.id.mapをMapViewに紐付けて生成 */
		mMapView = (MapView) findViewById(R.id.mapview);
		// /* クリックを有効化 */
		// mMapView.setClickable(true);
		// /* ズームを有効化 */
		// mMapView.setBuiltInZoomControls(true);

		findViewById(R.id.mapbutton).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mMapView.isSatellite()) {
					mMapView.setSatellite(false);
				} else {
					mMapView.setSatellite(true);
				}

			}
		});

		// スタッフIDの受け取り
		Intent intent = getIntent();
		mStaffid = intent.getIntExtra("STAFF_ID", mStaffid);
		Log.d(TAG, "受け取ったSTAFF_ID--->" + mStaffid);

		// マップコントローラーの生成と倍率の初期設定
		mMapView = (MapView) findViewById(R.id.mapview);
		mMc = mMapView.getController();
		mMc.setZoom(18); // 倍率の初期設定値

		// ズームコントローラーを配置する
		// mMapView.setBuiltInZoomControls(true);
		// mMapView.invalidate();

		mMapOverlays = mMapView.getOverlays();

		// マーカーの生成
		mDrawable_red = getResources().getDrawable(R.drawable.marker); // 赤いマーカー
		mDrawable_green = getResources().getDrawable(R.drawable.marker2); // 緑のマーカー

		mItemizedOverlay_red = new CustomItemizedOverlay<CustomOverlayItem>(
				mDrawable_red, mMapView);
		mItemizedOverlay2_green = new CustomItemizedOverlay<CustomOverlayItem>(
				mDrawable_green, mMapView);

		mGeolist = new ArrayList<GeoPoint>();
		
		final ProgressDialog pDialog;
		pDialog = new ProgressDialog(this);
		pDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		pDialog.setMessage("処理を実行中です...");
		pDialog.setCancelable(true);
		pDialog.show();
	
		
		new Thread(new Runnable() {
			Handler hnd = new Handler();

			@Override
			public void run() {
				boolean ret;
				/* 訪問先一覧取得 - 通信 */
				ret = updateMap(); // 通信・及びマップ情報セットを実行
				Log.d(TAG, "updateMap Done");
				Log.d(TAG, "Postrun START");
				if (ret) {
					hnd.post(new Runnable() {
						@Override
						public void run() {
							try {
								setMapinfo(objResponse);
							} catch (IOException e) {
								// TODO 自動生成された catch ブロック
								e.printStackTrace();
							}
							pDialog.dismiss();
						}
					});
				} else {
					hnd.post(new Runnable() {
						@Override
						public void run() {
							new AlertDialog.Builder(ViewLocationActivity.this).setTitle("通信エラー")
									.setMessage("位置情報の取得に失敗しました。").setPositiveButton("OK", null)
									.show();
							pDialog.dismiss();
						}
					});
				}
				Log.d(TAG, "Postrun END");
			}
		}).start();

		Log.d(TAG, "onCreate　End");
	}

	/**
	 * サーバーからスタッフ情報を取得し、マップ情報を更新します
	 */
	private boolean updateMap() {
		Log.d(TAG, "updateMap Start!");
		boolean ret = false;
		try {
			// WebAPIの実行（HTTP通信）
			 objResponse = getHttpSV();
			if (objResponse != null
					&& objResponse.getStatusLine().getStatusCode() == HttpURLConnection.HTTP_OK) {
				// マップ情報の設定
//				setMapinfo(objResponse);
				ret = true;
			} else {
				Log.d(TAG, "=====httpRes is NG=====");
			}

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
		} finally {
			Log.d(TAG, "updateMap Exception");
		}
		Log.d(TAG, "updateMap End");
		return ret;
	}

	/**
	 * スピナー・マーカー等のマップ情報を取得します
	 */
	private void setMapinfo(HttpResponse httpRes) throws IOException {
		Log.d(TAG, "setMapinfo Start!");
		// 引数チェック
		if (httpRes == null) {
			return;
		}
		// ストリーム->文字列（JSON）変換
		InputStream stream = httpRes.getEntity().getContent();
		String jsonString = convertStreamToString(stream);
		Log.d(TAG, jsonString);

		// JSON->GSON変換
		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.create();

		// 　アダプターの生成
		mAdapter = new ArrayAdapter<CharSequence>(this,
				android.R.layout.simple_spinner_item);
		mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		mSelectSpinner = (Spinner) findViewById(R.id.Spinner02); // スピナーの取得

		// オーバーレイアイテム（マーカー）の生成
		mItemizedOverlay2_green = new CustomItemizedOverlay<CustomOverlayItem>(
				mDrawable_green, mMapView);

		mOverlayList = new ArrayList<CustomOverlayItem>();
		List<Staff> staffList = gson.fromJson(jsonString,
				new TypeToken<List<Staff>>() {
				}.getType());

		int cnt = 0; // 　添字
		// サーバーからスタッフID一覧を取得
		for (Staff staff : staffList) {
			Log.d(TAG, "スタッフ名：" + staff.getStaff_name());

			// アダプターにスタッフ全員の名前をセットする
			mAdapter.add(staff.getStaff_name());
			Log.d(TAG, "緯度：" + staff.getLatitude());
			Log.d(TAG, "経度：" + staff.getLongitude());

			// 各スタッフの位置情報（緯度・経度）をmGeolistに格納
			mGeolist.add(new GeoPoint((int) (staff.getLatitude() * 1E6),
					(int) (staff.getLongitude() * 1E6)));

			// 各ｽﾀｯﾌの最終更新時刻を取得し、現在時刻との差分を求める
			timeCal = toCalendar(staff.getLast_update_time());
			toDiffDate();

			// バルーンに表示される情報（名前・スタッフＩＤ・最終更新時刻）をセット
			mOverlayList.add(new CustomOverlayItem(mGeolist.get(cnt), staff
					.getStaff_name(), "スタッフコード:" + staff.getStaff_id() + "\n"
					+ toDiffDate() + "前", staff.getStaff_id()));

			// マーカーにバルーンをセット
			mItemizedOverlay2_green.addOverlay(mOverlayList.get(cnt));

			cnt++; // インクリメンツ！
			Log.d(TAG, "スタッフ名：" + staff.getStaff_name());
		}
		mMapOverlays.add(mItemizedOverlay2_green); // マップにオアーバーレイをセット
		mSelectSpinner.setAdapter(mAdapter); // スピナーにアダプターをセット
		mSelectSpinner.setOnItemSelectedListener(this); // スピナーのリスナー設定
		mSelectSpinner.setSelection((mStaffid) - 2); // スピナーの初期位置設定（IDとスピナーの場所番号との整合性を取るため-2)
		Log.d(TAG, "setMapinfo End");
	}

	/**
	 * ルート情報を表示するかどうか
	 * 
	 * @param なし
	 * 
	 * @return 表示させる場合はtrue させない場合：false
	 */
	@Override
	protected boolean isRouteDisplayed() {
		// Log.d(TAG, "isRouteDisplayed　Start!");
		// Log.d(TAG, "isRouteDisplayed　End");
		return false;
	}

	/**
	 * スピナーの項目が選択されたときの処理
	 */
	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position,
			long id) {
		Log.d(TAG, "onItemSelected Start!");
		// 　初期化
		if (mMapOverlays.size() > 1) {
			mMapOverlays.remove(1);
		}
		Log.d(TAG, "リストのサイズ（追加前)" + mMapOverlays.size());
		mItemizedOverlay_red.delOverlay(0); // 赤いマーカーを一旦削除
		mItemizedOverlay_red.addOverlay(mOverlayList.get(position)); // 　新たに選択された場所に赤いマーカーを配置

		mMapOverlays.add(mItemizedOverlay_red);
		Log.d(TAG, "リストのサイズ(追加後）" + mMapOverlays.size());
		Log.d(TAG, "現在のポジション：" + position + "前回のポジション：" + mLastposition);
		mMc.animateTo(mGeolist.get(position)); // 選択されたスタッフのマーカーに移動する
		mLastposition = position; // 前回のポジションを再設定

		Log.d(TAG, "onItemSelected　End");

	}

	/**
	 * 何も選択されなかった時に呼び出されます。（ここでは何もしない）
	 */
	@Override
	public void onNothingSelected(AdapterView<?> parent) {
		Log.d(TAG, "onNothingSelected Start!");
		Log.d(TAG, "onNothingSelected End");

	}

	/**
	 * WebAPIの実行（HTTP通信）
	 */
	private HttpResponse getHttpSV() throws Exception {

		Log.d(TAG, "getHttpSV Start!");
		// 戻り値の初期化
		HttpResponse res = null;

		// パラメータの生成
		HttpParams para = new BasicHttpParams();

		// 接続のタイムアウトの設定(アパッチが提供する通信するときのパラメータを設定するメソッド)
		HttpConnectionParams.setConnectionTimeout(para, TIMEOUT);

		// データ取得のタイムアウトの設定
		HttpConnectionParams.setSoTimeout(para, TIMEOUT);

		// HTTP通信実行
		// アパッチが提供するクラス
		HttpClient objCli = new DefaultHttpClient(para);

		// 全スタッフ情報を取得
		mObjGet = new HttpGet(URI_STAFF);

		// 通信を実行
		res = objCli.execute(mObjGet);
		Log.d(TAG, "■■■■■■生存確認■■■■■■");
		Log.d("通信成功", res.toString());

		// resに結果を取り込む
		if (res.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK) {
			// 通信は成功したけど取れない場合
			Log.d(TAG, "■■■■■■もしかして取れてない？■■■■■■");
			// TODO 通信が失敗した時or取得に失敗した時、何らかのメッセージを表示する
			return null;
		}

		Log.d(TAG, "getHttpSV End");
		return res;
	}

	/**
	 * ストリームを文字列に変換します
	 */
	private String convertStreamToString(InputStream is) throws IOException {
		Log.d(TAG, "convertStreamToString Start!");
		if (is == null) {
			return "";
		}
		int n;
		char[] buffer = new char[4096];
		Writer writer = new StringWriter();
		try {
			// ストリームを文字列に変換
			Reader reader = new BufferedReader(new InputStreamReader(is,
					"UTF-8"));
			while ((n = reader.read(buffer)) != -1) {
				writer.write(buffer, 0, n);
			}
		} finally {
			// ファイルクローズ
			is.close();
		}
		Log.d(TAG, "convertStreamToString End");
		return writer.toString();
	}

	/**
	 * 現在の日時と比較して、時間の差分を求めます
	 */
	private String toDiffDate() {
		// 現在の日時
		Calendar nowcal = Calendar.getInstance();

		// long型の差分（ミリ秒）
		long diffTime = nowcal.getTimeInMillis() - timeCal.getTimeInMillis();

		// 秒
		long second = diffTime / 1000;
		if (second < 60) {
			return second + "秒";
		}

		// 分
		long minute = second / 60;
		if (minute < 60) {
			return minute + "分";
		}

		// 時
		long hour = minute / 60;
		if (hour < 24) {
			return hour + "時間";
		}

		// 日
		long day = hour / 24;
		if (day <= 28) {
			return day + "日";
		}

		// 30日以上の場合
		// 月＋1
		timeCal.add(Calendar.MONDAY, 1);
		if (timeCal.after(nowcal)) {
			return day + "日"; // 一ヶ月以内
		}

		return "1年"; // 1年前
	}

	/**
	 * 指定された日付・時刻文字列を、可能であれば Calendarクラスに変換します。 以下の形式の日付文字列を変換できます。
	 * 
	 * ●変換可能な形式は以下となります。 yyyy/MM/dd yy/MM/dd yyyy-MM-dd yy-MM-dd yyyyMMdd
	 * 
	 * 上記に以下の時間フィールドが組み合わされた状態 でも有効です。 HH:mm HH:mm:ss HH:mm:ss.SSS
	 * 
	 * @param strDate
	 *            日付・時刻文字列。
	 * @return 変換後のCalendarクラス。
	 * @throws IllegalArgumentException
	 *             日付文字列が変換不可能な場合 または、矛盾している場合（例：2000/99/99）。
	 */
	public static Calendar toCalendar(String strDate) {
		strDate = format(strDate);
		Calendar cal = Calendar.getInstance();
		cal.setLenient(false);

		int yyyy = Integer.parseInt(strDate.substring(0, 4));
		int MM = Integer.parseInt(strDate.substring(5, 7));
		int dd = Integer.parseInt(strDate.substring(8, 10));
		int HH = cal.get(Calendar.HOUR_OF_DAY);
		int mm = cal.get(Calendar.MINUTE);
		int ss = cal.get(Calendar.SECOND);
		int SSS = cal.get(Calendar.MILLISECOND);
		cal.clear();
		cal.set(yyyy, MM - 1, dd);
		int len = strDate.length();
		switch (len) {
		case 10:
			break;
		case 16: // yyyy/MM/dd HH:mm
			HH = Integer.parseInt(strDate.substring(11, 13));
			mm = Integer.parseInt(strDate.substring(14, 16));
			cal.set(Calendar.HOUR_OF_DAY, HH);
			cal.set(Calendar.MINUTE, mm);
			break;
		case 19: // yyyy/MM/dd HH:mm:ss
			HH = Integer.parseInt(strDate.substring(11, 13));
			mm = Integer.parseInt(strDate.substring(14, 16));
			ss = Integer.parseInt(strDate.substring(17, 19));
			cal.set(Calendar.HOUR_OF_DAY, HH);
			cal.set(Calendar.MINUTE, mm);
			cal.set(Calendar.SECOND, ss);
			break;
		case 23: // yyyy/MM/dd HH:mm:ss.SSS
			HH = Integer.parseInt(strDate.substring(11, 13));
			mm = Integer.parseInt(strDate.substring(14, 16));
			ss = Integer.parseInt(strDate.substring(17, 19));
			SSS = Integer.parseInt(strDate.substring(20, 23));
			cal.set(Calendar.HOUR_OF_DAY, HH);
			cal.set(Calendar.MINUTE, mm);
			cal.set(Calendar.SECOND, ss);
			cal.set(Calendar.MILLISECOND, SSS);
			break;
		default:
			throw new IllegalArgumentException("引数の文字列[" + strDate
					+ "]は日付文字列に変換できません");
		}
		return cal;
	}

	/**
	 * 様々な日付、時刻文字列をデフォルトの日付・時刻フォーマット へ変換します。
	 * 
	 * ●デフォルトの日付フォーマットは以下になります。 日付だけの場合：yyyy/MM/dd 日付+時刻の場合：yyyy/MM/dd
	 * HH:mm:ss.SSS
	 * 
	 * @param str
	 *            変換対象の文字列
	 * @return デフォルトの日付・時刻フォーマット
	 * @throws IllegalArgumentException
	 *             日付文字列が変換不可能な場合
	 */
	private static String format(String str) {
		if (str == null || str.trim().length() < 8) {
			throw new IllegalArgumentException("引数の文字列[" + str
					+ "]は日付文字列に変換できません");
		}
		str = str.trim();
		String yyyy = null;
		String MM = null;
		String dd = null;
		String HH = null;
		String mm = null;
		String ss = null;
		String SSS = null;
		// "-" or "/" が無い場合
		if (str.indexOf("/") == -1 && str.indexOf("-") == -1) {
			if (str.length() == 8) {
				yyyy = str.substring(0, 4);
				MM = str.substring(4, 6);
				dd = str.substring(6, 8);
				return yyyy + "/" + MM + "/" + dd;
			}
			yyyy = str.substring(0, 4);
			MM = str.substring(4, 6);
			dd = str.substring(6, 8);
			HH = str.substring(9, 11);
			mm = str.substring(12, 14);
			ss = str.substring(15, 17);
			return yyyy + "/" + MM + "/" + dd + " " + HH + ":" + mm + ":" + ss;
		}
		StringTokenizer token = new StringTokenizer(str, "_/-:. ");
		StringBuffer result = new StringBuffer();
		for (int i = 0; token.hasMoreTokens(); i++) {
			String temp = token.nextToken();
			switch (i) {
			case 0:// 年の部分
				yyyy = fillString(str, temp, "L", "20", 4);
				result.append(yyyy);
				break;
			case 1:// 月の部分
				MM = fillString(str, temp, "L", "0", 2);
				result.append("/" + MM);
				break;
			case 2:// 日の部分
				dd = fillString(str, temp, "L", "0", 2);
				result.append("/" + dd);
				break;
			case 3:// 時間の部分
				HH = fillString(str, temp, "L", "0", 2);
				result.append(" " + HH);
				break;
			case 4:// 分の部分
				mm = fillString(str, temp, "L", "0", 2);
				result.append(":" + mm);
				break;
			case 5:// 秒の部分
				ss = fillString(str, temp, "L", "0", 2);
				result.append(":" + ss);
				break;
			case 6:// ミリ秒の部分
				SSS = fillString(str, temp, "R", "0", 3);
				result.append("." + SSS);
				break;
			}
		}
		return result.toString();
	}

	private static String fillString(String strDate, String str,
			String position, String addStr, int len) {
		if (str.length() > len) {
			throw new IllegalArgumentException("引数の文字列[" + strDate
					+ "]は日付文字列に変換できません");
		}
		return fillString(str, position, len, addStr);
	}

	/**
	 * 文字列[str]に対して、補充する文字列[addStr]を [position]の位置に[len]に満たすまで挿入します。
	 * 
	 * ※[str]がnullや空リテラルの場合でも[addStr]を [len]に満たすまで挿入した結果を返します。
	 * 
	 * @param str
	 *            対象文字列
	 * @param position
	 *            前に挿入 ⇒ L or l 後に挿入 ⇒ R or r
	 * @param len
	 *            補充するまでの桁数
	 * @param addStr
	 *            挿入する文字列
	 * @return 変換後の文字列。
	 */
	private static String fillString(String str, String position, int len,
			String addStr) {
		if (addStr == null || addStr.length() == 0) {
			throw new IllegalArgumentException("挿入する文字列の値が不正です。addStr="
					+ addStr);
		}
		if (str == null) {
			str = "";
		}
		StringBuffer buffer = new StringBuffer(str);
		while (len > buffer.length()) {
			if (position.equalsIgnoreCase("l")) {
				int sum = buffer.length() + addStr.length();
				if (sum > len) {
					addStr = addStr.substring(0, addStr.length() - (sum - len));
					buffer.insert(0, addStr);
				} else {
					buffer.insert(0, addStr);
				}
			} else {
				buffer.append(addStr);
			}
		}
		if (buffer.length() == len) {
			return buffer.toString();
		}
		return buffer.toString().substring(0, len);
	}

}
