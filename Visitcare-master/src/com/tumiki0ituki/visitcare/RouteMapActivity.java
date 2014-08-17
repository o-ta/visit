/*
 * 訪問ルートマップ.
 * 遷移元から指定された日の訪問ルートをMapViewへ表示するアクティビティー
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
import java.util.Iterator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.R;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

/**
 * 訪問ルートMapActivity.
 */
public class RouteMapActivity extends MapActivity {

	/** TIMEOUT値　Milliseconds. */
	private static final int TIMEOUTPARAM = 30000;

	/** ログタグ. */
	private static final String TAG = RouteMapActivity.class.getName();

	/** マップコントローラー. */
	private static MapController mapCtrl;

	/** マップビュー. */
	private static MapView map;

	private String requeststaff;

	private Context mContext;

	HttpResponse hres = null;

	/** バルーン付ピンオーバーレイ */
	private CustomItemizedOverlay<CustomOverlayItem> mPinOverlay;

	/*
	 * (非 Javadoc)
	 * @see com.google.android.maps.MapActivity#onCreate(android.os.Bundle)
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d(TAG, "onCreate START");
		/* タイトルバー非表示 */
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.route_map);

		/* スタッフＩＤ・訪問日の取得 */
		Intent intent = getIntent();
		final int staff_id = intent.getIntExtra("STAFF_ID", 0);
		final String date = intent.getStringExtra("DATE");

		((TextView)findViewById(R.id.route_map_date)).setText(date + "の予定");

		Log.d("intent STAFF_IDの中身", Integer.toString(staff_id));
		Log.d("intent DATEの中身", date.toString());

		/* WebAPI引数の生成 */
		requeststaff = "staff_id=" + staff_id + "&date_of=" + date;

		/* R.id.mapをMapViewに紐付けて生成 */
		map = (MapView) findViewById(R.id.route_map_view);
		/* クリックを有効化 */
		map.setClickable(true);
		/* ズームを有効化 */
		map.setBuiltInZoomControls(false);

		findViewById(R.id.view_change).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (map.isSatellite()) {
					map.setSatellite(false);
				} else {
					map.setSatellite(true);
				}

			}
		});

		/* マップの詳細設定をするためのコントローラを取得 */
		mapCtrl = map.getController();

		mContext = this;

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

				/* サーバーからスタッフの指定日の訪問スケジュールを取得しマップ表示する */
				try {
					hres = doDrawScheduleAction(requeststaff);
					if (hres != null) {
						hnd.post(new Runnable() {
							@Override
							public void run() {
								Log.d(TAG, "PostThread START");
								try {
									setVisitGeoPoint(hres);
									pDialog.dismiss();
								} catch (IOException e) {
									e.printStackTrace();
									new AlertDialog.Builder(mContext).setTitle("通信エラー")
									.setMessage("訪問先リストの取得に失敗しました。").setPositiveButton("OK", null).show();
								}
								Log.d(TAG, "PostThread END");
							}
						});
					}
				} catch (IOException e) {
					Log.d("IOException発生", e.toString());
					hnd.post(new Runnable() {
						@Override
						public void run() {
							pDialog.dismiss();
							new AlertDialog.Builder(mContext).setTitle("通信エラー")
							.setMessage("訪問先リストの取得に失敗しました。").setPositiveButton("OK", null).show();
//							finish();
						}
					});
					e.printStackTrace();
				}
			}
		}).start();

		Log.d(TAG, "onCreate END");
	}

	/**
	 * 訪問先リスト取得.
	 *
	 * @param staff_id_data APIリクエスト引数
	 * @return レスポンス
	 * @throws IOException 入出力例外.
	 */
	private HttpResponse doDrawScheduleAction(final String staff_id_data) throws IOException {
		Log.d(TAG, "doDrawScheduleAction START");
		// 戻り値の初期化
		HttpResponse res = null;
		// パラメータの生成
		HttpParams para = new BasicHttpParams();

		// 接続のタイムアウトの設定（30秒）
		HttpConnectionParams.setConnectionTimeout(para, TIMEOUTPARAM);
		// データ取得のタイムアウトの設定（30秒）
		HttpConnectionParams.setSoTimeout(para, TIMEOUTPARAM);

		// HTTP通信実行
		HttpClient objCli = new DefaultHttpClient(para);

		// 本番用
		HttpGet httpGet = new HttpGet(getString(R.string.reqscheduleurl) + "?" + staff_id_data);

		res = objCli.execute(httpGet);
		if (res.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK) {
			Log.d("通信失敗", "ResponseCode:" + Integer.toString(res.getStatusLine().getStatusCode()));
			Log.d(TAG, "doDrawScheduleAction END");
			return null;
		}
		Log.d("通信成功", "ResponseCode:" + Integer.toString(res.getStatusLine().getStatusCode()));
		Log.d(TAG, "doDrawScheduleAction END");
		return res;
	}

	/**
	 * オーバーレイ描画.
	 *
	 * @param  httpRes WebAPIレスポンス
	 * @throws IOException 入出力例外.
	 */
	private void setVisitGeoPoint(HttpResponse httpRes) throws IOException {
		Log.d(TAG, "setVisitGeoPoint START");
		// ストリーム->文字列（JSON）変換
		InputStream stream = httpRes.getEntity().getContent();
		String jsonString = convertStreamToString(stream);
		// JSON->GSON変換
		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.create();
		Log.d(TAG, "JsonString = " + jsonString);

		/* GsonからList生成 */
		List<Schedule> scheduleList = gson.fromJson(jsonString, new TypeToken<List<Schedule>>() {
		}.getType());

		/* Listからラインオーバーレイ生成 */
		RouteLineOverlay lineOverlay = new RouteLineOverlay(scheduleList);
		map.getOverlays().add(lineOverlay);

		/* Listからピンオーバーレイ生成 */
		int loopcount = 0;
		int turn;
		for (Iterator<Schedule> it = scheduleList.iterator(); it.hasNext();) {
			loopcount++;
			Schedule route = it.next();
			GeoPoint loc = new GeoPoint(new Double(route.getlatitude() * 1E6).intValue(),
					new Double(route.getlongitude() * 1E6).intValue());
			if (loopcount == 1) {
				// 地図の中心座標設定
				mapCtrl.animateTo(loc);
				// 地図の縮尺設定
				mapCtrl.setZoom(16);
			}
			int status = route.getstatus();

			Log.d(TAG, "PinOberlayLoopCount=" + Integer.toString(loopcount));
			Log.d(TAG, "被介護者：" + route.getuser_name());
			Log.d(TAG, "住所：" + route.getaddress());
			Log.d(TAG, "座標：" + loc.toString());
			Log.d(TAG, "ステータス:" + status);

			Drawable pin;
			switch (status) {
			// 訪問済み、未訪問で画像を切り替え設定
			case 1:
				pin = getResources().getDrawable(R.drawable.symbol_multiply);
				break;
			default:
				turn = route.getturn();
				if (turn > 20) {
					turn = 0;
				}
				pin = getResources().getDrawable(
						getResources()
								.getIdentifier("number_" + turn, "drawable", getPackageName()));
				break;
			}

			/* バルーンピンオーバーレイ生成 */
			mPinOverlay = new CustomItemizedOverlay<CustomOverlayItem>(
					pin, map,true);

			// マーカーにバルーンをセット
			mPinOverlay.addOverlay(new CustomOverlayItem(loc, route.getuser_name(),  route.getaddress()));

			map.getOverlays().add(mPinOverlay);

			/* ピンオーバーレイ生成 */
//			RoutePinItemizedOverlay pinOverlay = new RoutePinItemizedOverlay(pin);
//			pinOverlay.addPoint(loc);
//			map.getOverlays().add(pinOverlay);
		}
		Log.d(TAG, "setVisitGeoPoint END");
	}

	/**
	 * ストリーム->String変換処理.
	 *
	 * @param is InputStream
	 * @return 変換後String
	 * @throws IOException 入出力例外.
	 */
	private String convertStreamToString(InputStream is) throws IOException {
		Log.d(TAG, "convertStreamToString START");
		if (is == null) {
			return "";
		}
		int n;
		char[] buffer = new char[4096];
		Writer writer = new StringWriter();
		try {
			Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			while ((n = reader.read(buffer)) != -1) {
				writer.write(buffer, 0, n);
			}
		} finally {
			// ファイルを開いて書き込んだ後閉じる感じ
			is.close();
		}
		Log.d(TAG, "convertStreamToString END");
		// writerに入った文字列を返す
		return writer.toString();
	}

	/*
	 * (非 Javadoc)
	 * @see com.google.android.maps.MapActivity#isRouteDisplayed()
	 */
	@Override
	protected boolean isRouteDisplayed() {
		Log.d(TAG, "isRouteDisplayed");
		/* ルート表示OFF */
		return false;
	}

}
