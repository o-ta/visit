/*
 * メイン画面
 * 訪問リストの一覧をリスト表示する
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
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnManagerPNames;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;

import android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.SimpleAdapter.ViewBinder;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class MainActivity extends Activity implements OnClickListener, OnItemClickListener,
		OnItemSelectedListener {

	View back;

	// ログ用タグ
	private static final String TAG = MainActivity.class.getName();
	private static final String methodname = "メソッド名：";

	/**
	 * メソッド名を自動で取りたいときの記述 （負荷が高いので、基本的には使わないこと）
	 */
	// String methodname = new Throwable().getStackTrace()[0].getMethodName();
	// Log.d(TAG, methodname+"");

	// スケジュール取得用API
	private String URL_SCHEDULE;

	// スタッフ一覧取得用API
	private String URL_STAFF;

	// サーバーから取得した業務IDをリストのイメージに埋め込むために保持するリスト
	private ArrayList<Integer> mWorkIdList;

	// 業務IDに対応したユーザー名を保持するリスト
	private ArrayList<String> mUsername;

	// 業務IDに対応したステータスを保持するリスト
	private ArrayList<Integer> mStatus;

	// サーバーから取得したスタッフ名に対応したスタッフIDを取得するために保持するリスト
	private ArrayList<Integer> mStaffIdList;

	// スタッフID変更時に表示するスピナー
	private Spinner mStaffSpinner;

	// スタッフID一覧をスピナーに表示させるためのアダプター
	private ArrayAdapter<String> mStaffChangeAdp;

	// gsonから渡された訪問先情報を保持するリスト
	private ArrayList<Map<String, Object>> mList;

	// 訪問リストおよびスタッフ名取得中に表示するプログレスダイアログ
	private ProgressDialog mDia;
	private ProgressDialog mDia2;
	private ProgressDialog mDia3;

	// プログレスダイアログ表示およびスレッドからのリストビュー操作に使うハンドラ
	private Handler mHandler;

	// 訪問リストを受け取るScheduleクラス
	public Schedule mSchedule;

	// 訪問リストを最新の状態にするボタン
	private Button mUpdateButton;

	// 訪問リストのマップを表示するボタン
	private Button mMapButton;

	// スタッフIDおよびスタッフ名を表示するテキストビュー
	private TextView mStaffNameText;

	// 訪問リストの日付を表示するテキストビュー
	private TextView mDateText;

	// 訪問リストが0件のときに「該当なし」を表示するテキストビュー
	private TextView mNoDataText;

	// デフォルトで表示する日付（今日）を取得するためインスタンスを生成
	private Calendar mCalender = Calendar.getInstance();

	// 今年を取得
	private int mYear = mCalender.get(Calendar.YEAR);

	// 今月を取得
	private int mMonth = mCalender.get(Calendar.MONTH) + 1;

	// 今日を取得
	private int mDay = mCalender.get(Calendar.DAY_OF_MONTH);

	// スタッフIDを保持する変数
	private String mStaffId = "";

	// スタッフIDからスタッフ名を取得して表示するための変数
	private String mStaffName = "";

	// imagefixの状態を保持する変数（撮影画面からキャンセルで戻ったときに状態をもとに戻すために保持）
	private String mImageFixFlag = "";

	// HTTP通信でデータを取得する際のURLを格納する変数
	private HttpGet mObjGet;

	// httpレスポンス
	HttpResponse objResponse;

	// intentのリクエストコードの定数化
	private final int ADD_SCHEDULE = 0;
	private final int STAFF_MAP = 1;
	private final int DAY_SELECT = 2;
	private final int SCHEDULE_MAP = 3;
	private final int SET_GPS = 4;
	private final int REGIST_REPORT = 5;
	private final int CAMERA = 6;

	// サーバー通信時の引数の定数化
	private final int GET_SCHEDULE = 0;
	private final int GET_STAFF_LIST = 1;
	private final int GET_STAFF_NAME = 2;

	private static boolean mLifeline = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Log.d(TAG, "oncreate");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_main);

		// スケジュール取得用API
		URL_SCHEDULE = getString(R.string.schedule_get_url);

		// スタッフ一覧取得用API
		URL_STAFF = getString(R.string.staff_get_url);

		// スタッフID変更ダイアログでスタッフ一覧を取得するスピナーを生成
		mStaffSpinner = new Spinner(this);

		// プログレスダイアログ用ハンドラを生成
		mHandler = new Handler();

		// 訪問リストに情報を埋め込むマップリストを生成
		mList = new ArrayList<Map<String, Object>>();

		// 取得した業務IDを埋め込むためのリストを生成
		mWorkIdList = new ArrayList<Integer>();

		// 取得したユーザー名を埋め込むためのリストを生成
		mUsername = new ArrayList<String>();

		// 取得したステータスを埋め込むためのリストを生成
		mStatus = new ArrayList<Integer>();

		// 取得したスタッフIDを取得するためのリストを生成
		mStaffIdList = new ArrayList<Integer>();

		// スタッフ名を表示するテキストビュー
		mStaffNameText = (TextView) findViewById(R.id.staffname);

		// 訪問リストの日付を表示するテキストビュー
		mDateText = (TextView) findViewById(R.id.date);

		mNoDataText = (TextView) findViewById(R.id.nodata);

		// 訪問リストのマップ表示へ移動するボタン
		mMapButton = (Button) findViewById(R.id.map_button);
		mMapButton.setOnClickListener(this);

		// 訪問リストを最新の状態にするボタン
		mUpdateButton = (Button) findViewById(R.id.update_button);
		mUpdateButton.setOnClickListener(this);

		// プリファレンスからスタッフIDと位置情報の更新間隔を取得
		SharedPreferences pref = getSharedPreferences("houmon", MODE_PRIVATE);
		mStaffId = pref.getString("STAFFID", mStaffId);

		if (mStaffId == "") {
			// スタッフIDが空だったら、スタッフID入力ダイアログを表示する
			InputStaff_id();

		} else {
			if (!mLifeline) {
				// スタッフIDをもとにスタッフ名をサーバーから取得
				updateListView(GET_STAFF_NAME);

				// サーバーから該当スタッフIDの本日の訪問リストを取得する
				updateListView(GET_SCHEDULE);

				// 位置情報サービススタート
				Intent intent = new Intent(MainActivity.this, SendLocationServise.class);
				intent.setAction("start");
				intent.putExtra("STAFF_ID", Integer.parseInt(mStaffId));
				startService(intent);
			} else {
				Log.d(TAG, "マジありえねぇわろすｗｗｗｗｗｗｗｗ");
			}
		}

	}

	// アプリの終了（バックボタン押下）時の処理
	@Override
	public void finish() {
		Log.d(TAG, methodname + "finish" + "　スタッフID：" + mStaffId);
		new AlertDialog.Builder(this).setTitle("終了しますか？")
				.setPositiveButton("はい", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// プリファレンスにスタッフIDと位置情報更新間隔を保存してアプリを終了する
						SharedPreferences pref = getSharedPreferences("houmon", MODE_PRIVATE);
						Editor e = pref.edit();
						e.putString("STAFFID", mStaffId);
						e.commit();
						MainActivity.super.finish();
					}
				})
				// いいえボタンが押されたら何もしない（アプリを終了しない）
				.setNegativeButton("いいえ", null).show();
	}

	@Override
	protected void onDestroy() {
		mLifeline = false;
		super.onDestroy();
	}
	
	@Override
	protected void onPause() {
		Log.d(TAG, "onPause");
		if (mDia.isShowing() == true) {
			Log.d(TAG, "mDia is dismiss");
			mDia.dismiss();
		}
		if (mDia2.isShowing() == true) {
			Log.d(TAG, "mDia2 is dismiss");
			Log.d(TAG, "onPause");
			mDia2.dismiss();
		}
		super.onPause();
	}
	// 上部のボタンが押されたときの処理
	@Override
	public void onClick(View v) {
		Log.d(TAG, methodname + "onClick" + "　スタッフID：" + mStaffId);
		switch (v.getId()) {
		// 訪問マップボタン
		case R.id.map_button:
			// 訪問先マップ表示へ
			Intent intent = new Intent(this, RouteMapActivity.class);
			// 現在選択されているスタッフIDと訪問リストの日付をput
			intent.putExtra("STAFF_ID", Integer.parseInt(mStaffId));
			intent.putExtra("DATE", Integer.toString(mYear) + "-" + Integer.toString(mMonth) + "-"
					+ Integer.toString(mDay));
			mLifeline = true;
			startActivityForResult(intent, 3);
			break;
		// 最新に更新ボタン
		case R.id.update_button:
			// スタッフ名をサーバーから取得
			updateListView(GET_STAFF_NAME);

			// サーバーから該当スタッフIDの当日の訪問リストを再取得する
			updateListView(GET_SCHEDULE);
			break;
		default:

		}
	}

	// メニューボタンの項目とアイコンを定義
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.d(TAG, methodname + "onCreateOptionsMenu" + "　スタッフID：" + mStaffId);
		super.onCreateOptionsMenu(menu);
		menu.add(0, 0, Menu.NONE, "新規スケジュール").setIcon(android.R.drawable.ic_menu_add);
		menu.add(0, 1, Menu.NONE, "他のスタッフの状況").setIcon(android.R.drawable.ic_menu_myplaces);
		menu.add(0, 2, Menu.NONE, "位置情報送信設定").setIcon(android.R.drawable.ic_menu_mylocation);
		menu.add(0, 3, Menu.NONE, "スタッフ変更").setIcon(android.R.drawable.ic_menu_manage);
		menu.add(0, 4, Menu.NONE, "訪問リスト日付変更").setIcon(android.R.drawable.ic_menu_day);

		return true;
	}

	// メニューボタンの項目が押されたときの処理
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.d(TAG, methodname + "onOptionsItemSelected" + "　スタッフID：" + mStaffId);
		switch (item.getItemId()) {
		case 0:
			// 新規スケジュールへ
			AddSchedleDialog asd = new AddSchedleDialog(this);
			asd.setmStuffID(mStaffId);
			asd.show();
			break;
		case 1:
			// 他スタッフの状況へ
			Intent intent1 = new Intent(this, ViewLocationActivity.class);
			intent1.putExtra("STAFF_ID", Integer.parseInt(mStaffId));
			Log.d(TAG, mStaffId);
			mLifeline = true;
			startActivityForResult(intent1, STAFF_MAP);
			break;
		case 2:
			// 位置情報送信設定へ
			Intent intent3 = new Intent(this, SendLocationActivity.class);
			intent3.putExtra("STAFF_ID", Integer.parseInt(mStaffId));
			Log.d(TAG, mStaffId);
			mLifeline = true;
			startActivityForResult(intent3, SET_GPS);
			break;
		case 3:
			// スタッフID変更へ
			InputStaff_id();
			break;
		case 4:
			// リストの日付選択へ
			Intent intent2 = new Intent(this, DaySelect.class);
			mLifeline = true;
			startActivityForResult(intent2, DAY_SELECT);
			break;

		}
		return true;

	}

	// スタッフID登録ダイアログ
	public void InputStaff_id() {
		Log.d(TAG, methodname + "InputStaff_id" + "　スタッフID：" + mStaffId);

		// 前回セットしたスピナーを一度親から外す
		View parent = (View) mStaffSpinner.getParent();
		if (parent != null) {
			((ViewGroup) parent).removeView(mStaffSpinner);
		}

		// 通信中のプログレスダイアログ
		mDia3 = new ProgressDialog(this);
		mDia3.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mDia3.setMessage("スタッフ一覧取得中...");
		mDia3.setCancelable(true);
		mDia3.show();

		// スタッフIDとスタッフ名の一覧をサーバーから取得
		updateListView(GET_STAFF_LIST);

	}

	// 他のアクティビティから戻ってきたときの処理
	public void onActivityResult(int rqcode, int rcode, Intent data) {
		Log.d(TAG, methodname + "onActivityResult" + "　スタッフID：" + mStaffId);
		Log.d(TAG, "onActivityResult START" + " rqcode:" + Integer.toString(rqcode) + "rcode:"
				+ Integer.toString(rcode));
		Log.d(TAG, "mLifeline" + Boolean.toString(mLifeline));
		switch (rqcode) {
		case ADD_SCHEDULE:
			// 新規追加から戻ってきたときの処理
			// アクティビティでなくダイアログにしたので、不要になりました
			break;
		case STAFF_MAP:
			// 他のスタッフの状況から戻ってきたときの処理
			// なにもしない
			break;
		case DAY_SELECT:
			// 訪問先リストの日付選択画面から戻ってきたときの処理
			if (rcode == RESULT_CANCELED) {
				// 日付変更せずに戻ったから何もしない
			} else {
				// 日付が変更されたから変更した年月日を取得
				mYear = data.getIntExtra("YEAR", mYear);
				mMonth = data.getIntExtra("MONTH", mMonth);
				mDay = data.getIntExtra("DAY", mDay);

				// サーバーから指定日のリストを取得して再表示
				updateListView(GET_SCHEDULE);
			}
			break;
		case SCHEDULE_MAP:
			// 訪問先のマップ表示から戻ってきたときの処理
			// なにもしない
			break;
		case SET_GPS:
			// 位置情報設定から戻ってきたときの処理
			// なにもしない
			break;
		case REGIST_REPORT:
			// 日報入力から戻ってきたときの処理
			if (rcode == RESULT_CANCELED) {
				// 入力せずに戻ったから何もしない
			} else {
				// サーバーから本日の最新リストを取得して再表示（入力済をリストに反映）
				updateListView(GET_SCHEDULE);
			}
			break;
		case CAMERA:
			// 写真撮影・送信から戻ってきたときの処理
			if (rcode == RESULT_CANCELED) {
				// 撮影せずに戻ったから何もしない
			} else {
				// サーバーからリストを取得して再表示し（画像の有無をリストに反映）
				updateListView(GET_SCHEDULE);
			}
			break;
		default:
		}
	}

	// 担当者変更のスピナーの選択時の処理
	@Override
	public void onItemSelected(AdapterView<?> parent, View v, int pos, long id) {
		Log.d(TAG, methodname + "onItemSelected" + "　スタッフID：" + mStaffId);
		// 選択された時点では何もしない（ダイアログの送信ボタンが押された時点でスタッフIDをgetする）
		Log.d("選択されたスタッフID：", Integer.toString(mStaffIdList.get(pos)));

	}

	// 訪問リストの訪問先が選択されたときの処理（日報入力画面へ）
	@Override
	public void onItemClick(AdapterView<?> parent, View v, final int pos, long id) {
		Log.d(TAG, methodname + "onItemClick" + "　スタッフID：" + mStaffId);

		Log.d("チェック取れたかな？", mList.get(pos).get("CHECK").toString());
		Log.d("ステータスは？", mStatus.get(pos).toString());
		Log.d("ポジションは？", Integer.toString(pos));

		// ////////////////////////■■■■■■■■■■■■■■■■■/////////////////////////
		if (mStatus.get(pos) == 1) {

			Builder builder = new AlertDialog.Builder(MainActivity.this);
			builder.setTitle("日報上書き確認");
			builder.setMessage("すでに日報を入力済みです。このまま続行して入力すると、前のデータに上書きされます。\nよろしいですか？");
			builder.setPositiveButton("続行", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// 日報入力画面に移動する処理
					goReport(pos);
				}
			});
			builder.setNegativeButton("キャンセル", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {

					return;
				}
			});
			builder.create();
			builder.show();
			// ////////////////////////■■■■■■■■■■■■■■■■■/////////////////////////
		} else {
			// 日報入力画面に移動する処理
			goReport(pos);
		}
	}

	// 日報入力画面に移動する処理
	public void goReport(int pos) {
		// 日報入力画面に渡す日付をyyyy/mm/ddのstring形式にするため、月が一桁のときは”0”を付ける
		String putMonth = "";
		if (mMonth < 10) {
			putMonth = "0" + Integer.toString(mMonth);
		} else {
			putMonth = Integer.toString(mMonth);
		}

		// 日報入力画面に渡す日付をyyyy/mm/ddのstring形式にするため、日が一桁のときは”0”を付ける
		String putDay = "";
		if (mDay < 10) {
			putDay = "0" + Integer.toString(mDay);
		} else {
			putDay = Integer.toString(mDay);
		}

		Intent intent = new Intent(MainActivity.this, ReportActivity.class);
		// 業務ID、被介護者名、日付をput
		intent.putExtra("WORK_ID", mWorkIdList.get(pos));
		intent.putExtra("USER_NAME", mUsername.get(pos));
		intent.putExtra("DATE", Integer.toString(mYear) + "/" + putMonth + "/" + putDay);

		/* Add 2012/2/23 ==START== */
		String schedle_time = ((HashMap<String, Object>) mList.get(pos)).get("TIME").toString();
		Log.d("Intent to ReportActivity - Schedle_time:", schedle_time);
		intent.putExtra("SchedleTime", schedle_time);
		/* Add 2012/2/23 ==END== */

		Log.d(TAG, mWorkIdList.get(pos) + mUsername.get(pos) + Integer.toString(mYear) + "/"
				+ putMonth + "/" + putDay);
		mLifeline = true;
		startActivityForResult(intent, REGIST_REPORT);
	}

	// 撮影画面からキャンセルで戻ったときに、カメラ画像の状態を元に戻す
	@Override
	protected void onResume() {
		super.onResume();
		if (back != null) {
			if (mImageFixFlag.equals("1")) {
				// imagefixが1だったら濃いカメラ画像
				((ImageView) back).setImageResource(R.drawable.camera);
			} else {
				// imagefixが0だったら薄いカメラ画像
				((ImageView) back).setImageResource(R.drawable.nocamera);
			}
		}

	}

	// リスト内のカメラ画像をクリックしたときの独自のクリックリスナーを定義
	OnClickListener myonclicklistener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			// カメラ画像の状態を保持
			mImageFixFlag = ((TextView) ((LinearLayout) v.getParent()).getChildAt(6)).getText()
					.toString();

			// クリックされたことがわかるように黄色いカメラ画像を表示
			((ImageView) v).setImageResource(R.drawable.click_camera);

			back = v;

			Log.d(TAG, methodname + "myonclicklistenerのonClick" + "　スタッフID：" + mStaffId);

			/**
			 * クリックされたカメライメージが所属している親view（つまり、リストの一行を構成しているLinearLayout）を取得し、
			 * その子viewの４番目にセットされているテキストを取得している。 ４番目の子view（TextView）
			 * には、アダプターで業務IDがsettextされているから、対応した業務IDが取り出せる。 ４番目の子view（TextView）
			 * は、height0、width0、invisibleとしているので、画面上には表示されていない。
			 * ５番目の子view（TextView） には、同様に被介護者名がsettextされている。以下４番目と同じ。
			 */
			Log.d("業務ID", ((TextView) ((LinearLayout) v.getParent()).getChildAt(4)).getText()
					.toString());
			Log.d("被介護者名", ((TextView) ((LinearLayout) v.getParent()).getChildAt(5)).getText()
					.toString());

			Intent intent6 = new Intent(v.getContext(), Satsuei.class);
			// 業務IDをput
			intent6.putExtra("WORK_ID", Integer.parseInt(((TextView) ((LinearLayout) v.getParent())
					.getChildAt(4)).getText().toString()));
			intent6.putExtra("USER_NAME",
					((TextView) ((LinearLayout) v.getParent()).getChildAt(5)).getText());
			Log.d(TAG,
					((TextView) ((LinearLayout) v.getParent()).getChildAt(4)).getText().toString()
							+ ((TextView) ((LinearLayout) v.getParent()).getChildAt(5)).getText());
			mLifeline = true;
			startActivityForResult(intent6, CAMERA);
		}
	};

	/**
	 * サーバーから訪問先リストを取得してリストビューを更新するメソッド 引数は通信の種類　GET_SCHEDULE = 0、GET_STAFF_LIST
	 * = 1、GET_STAFF_NAME = 2;
	 */
	private void updateListView(final int request) {
		Log.d(TAG,
				methodname + "updateListView" + "　スタッフID：" + mStaffId + "request:"
						+ Integer.toString(request));

		// if(opening==true){
		//
		// }else{
		if (request == GET_STAFF_LIST) {
			Thread thread = new Thread(null, new Runnable() {

				public void run() {
					try {
						// WebAPIの実行（HTTP通信）
						HttpResponse objResponse = getHttpSV(request);

						// リストの設定
						setBookList(objResponse, request);

					} catch (ClientProtocolException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}

					handler.sendEmptyMessage(0);
				}
			});
			thread.start();
		} else {
			if (request == GET_STAFF_NAME) {
				if (mDia2 == null) {
					Log.d(TAG, "mDia2 is null");
					mDia2 = new ProgressDialog(this);					
				}else if (mDia2.isShowing()) {
					return;
				}
				mDia2.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				mDia2.setMessage("スタッフ名取得中...");
				mDia2.setCancelable(true);
				mDia2.show();
			}
			if (request == GET_SCHEDULE) {
				if (mDia == null) {
					Log.d(TAG, "mDia is null");
					mDia = new ProgressDialog(this);					
				}else if (mDia.isShowing()) {
					return;
				}
				mDia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				mDia.setMessage("訪問リスト取得中...");
				mDia.setCancelable(true);
				mDia.show();
			}
			// }

			Thread updateThread = new Thread(null, new Runnable() {

				public void run() {
					try {
						// WebAPIの実行（HTTP通信）
						objResponse = getHttpSV(request);

						// リストの設定
						setBookList(objResponse, request);

					} catch (ClientProtocolException e) {
						e.printStackTrace();
					} catch (SocketTimeoutException e) {
						e.printStackTrace();
					} catch (Exception e) {
						e.printStackTrace();
					} finally {
						Log.d("getHttpSV", "Exception Finally START");
						if (mDia.isShowing() == true) {
							mDia.dismiss();
						}
						if (mDia2.isShowing() == true) {
							mDia2.dismiss();
						}

						// 通信エラーダイアログ
						mHandler.post(new Runnable() {
							public void run() {

								if (objResponse == null) {
									Log.d("通信エラー", "リクエスト番号：" + Integer.toString(request));
									new AlertDialog.Builder(MainActivity.this).setTitle("通信エラー")
											.setMessage("最新に更新ボタンで再度通信してください。")
											.setPositiveButton("OK", null).show();
								}

							}
						});
						Log.d("getHttpSV", "Exception Finally END");
					}
				}
			});
			updateThread.start();

		}

	}

	/**
	 * WebAPIの実行（HTTP通信） 引数は通信の種類　GET_SCHEDULE = 0、GET_STAFF_LIST =
	 * 1、GET_STAFF_NAME = 2;
	 */
	private synchronized HttpResponse getHttpSV(int request) throws ClientProtocolException,
			IOException {

		Log.d("通信開始したよ", Integer.toString(request));
		// 戻り値の初期化
		HttpResponse res = null;
		// HTTP通信実行
		// アパッチが提供するクラス
		HttpClient objCli = new DefaultHttpClient();

		String apiUrl = "";
		if (request == GET_SCHEDULE) {
			// 訪問リスト取得の場合
			apiUrl = URL_SCHEDULE + "staff_id=" + mStaffId + "&date_of=" + Integer.toString(mYear)
					+ "-" + Integer.toString(mMonth) + "-" + Integer.toString(mDay);
		} else if (request == GET_STAFF_LIST) {
			// スタッフ一覧取得の場合
			apiUrl = URL_STAFF + "staff_id=-1&location=false";
		} else if (request == GET_STAFF_NAME) {
			// スタッフ名取得の場合
			apiUrl = URL_STAFF + "staff_id=" + mStaffId + "&location=false";
		}
		mObjGet = new HttpGet(apiUrl);
		Log.d("getHttpSV", apiUrl);
		// パラメータの生成
		HttpParams para = mObjGet.getParams();
		para.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 10000);
		para.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 10000);
		para.setLongParameter(ConnManagerPNames.TIMEOUT, 10000);

		// 通信を実行（ここまではお決まりの記述）
		res = objCli.execute(mObjGet);

		// resに結果を取り込む
		if (res.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK) {
			Log.d("通信エラー発生", res.toString());
		}
//		mObjGet.abort();
//		objCli.getConnectionManager().shutdown();
		Log.d("通信成功", res.toString());
		Log.d("通信終了したよ", Integer.toString(request));
		return res;

	}

	// 訪問リストに表示する内容を設定
	private synchronized void setBookList(HttpResponse httpRes, int request) throws IOException {

		Log.d(TAG, methodname + "setBookList" + "　スタッフID：" + mStaffId);
		// 引数チェック
		if (httpRes == null) {
			return;
		}
		// ストリーム->文字列（JSON）変換
		InputStream stream = httpRes.getEntity().getContent();
		String jsonString = convertStreamToString(stream);
		// JSON->GSON変換
		GsonBuilder gsonBuilder = new GsonBuilder();
		Gson gson = gsonBuilder.create();

		if (request == GET_SCHEDULE) {

			// 訪問リストの取得だったときの処理
			List<Schedule> scheduleList = gson.fromJson(jsonString,
					new TypeToken<List<Schedule>>() {
					}.getType());

			// 受け取った情報を埋め込むハッシュマップ
			HashMap<String, Object> data;

			// 前の情報の入ったリストを一度クリア
			mList.clear();

			// 前の業務IDの入ったリストを一度クリア
			mWorkIdList.clear();

			// 前の被介護者名の入ったリストを一度クリア
			mUsername.clear();

			// 前のステータスの入ったリストを一度クリア
			mStatus.clear();

			// スケジュールクラスのインスタンスの数だけゲッターで値を取り出し、それぞれに入れる
			for (Schedule schedule : scheduleList) {

				// 業務IDをArraylistに入れる
				mWorkIdList.add(schedule.getwork_id());

				// 被介護者名をArraylistに入れる
				mUsername.add(schedule.getuser_name());

				// 日報入力済みのステータスをArraylistに入れる
				mStatus.add(schedule.getstatus());

				// jsonがScheduleクラスのメンバ変数に格納してくれたデータをハッシュマップに入れる
				data = new HashMap<String, Object>();

				data.put("NAME", schedule.getuser_name());
				data.put("ADDRESS", schedule.getaddress());
				data.put("TIME", schedule.getschedule_time());
				// 画像がDBに登録されていたらcamera、登録されてなかったらnocameraをリソースに設定
				// Log.d("画像登録済みフラグ", Integer.toString(schedule.getImagefix()));
				switch (schedule.getImagefix()) {
				case 0:
					data.put("CAMERA", R.drawable.nocamera);
					break;
				case 1:
					data.put("CAMERA", R.drawable.camera);
					break;
				default:
					data.put("CAMERA", R.drawable.nocamera);
					break;
				}
				// 日報がDBに登録されたいたらon、登録されてなかったらoffをリソースに設定
				Log.d("日報入力済みフラグ", Integer.toString(schedule.getstatus()));
				switch (schedule.getstatus()) {
				case 0:
					data.put("CHECK", android.R.drawable.checkbox_off_background);
					break;
				case 1:
					data.put("CHECK", android.R.drawable.checkbox_on_background);
					break;
				default:
					data.put("CHECK", android.R.drawable.checkbox_off_background);
					break;
				}
				// 業務IDを埋め込み（画面上は非表示）
				// カメラ画像がクリックされたときにここから業務IDを取得する）
				data.put("WORK", schedule.getwork_id());

				// 被介護者名を埋め込み（画面上は非表示）
				// カメラ画像がクリックされたときにここから被介護者名を取得する）
				data.put("USER", schedule.getuser_name());

				// 訪問順を表示するためにput
				data.put("TURN", schedule.getturn());

				// 画像登録有無の状態を埋め込み（画面上は非表示）
				// カメラ画像がクリックされたときにここからimagefixの値を取得する）
				data.put("IMAGEFIX", schedule.getImagefix());

				// 一行分の情報をリストにaddする
				mList.add(data);
			}

			// １行分に乗っかっているviewのIDを順番に定義
			int[] to = { R.id.name, R.id.adress, R.id.time, R.id.camera, R.id.check, R.id.listwork,
					R.id.listuser, R.id.turn, R.id.imagefix };

			// それぞれのviewに対応する情報のマップのキーを設定
			String[] from = { "NAME", "ADDRESS", "TIME", "CAMERA", "CHECK", "WORK", "USER", "TURN",
					"IMAGEFIX" };

			// シンプルアダプターでそれぞれのviewに情報を埋め込む
			// ImageViewにはリソースが、TextViewにはテキストが自動でセットされる
			final SimpleAdapter adapter = new SimpleAdapter(this, mList, R.layout.main_list_item,
					from, to);

			// カメラのイメージビューを独自リスナーに登録する
			// （アダプターにViewBinderとしてセットする方法で登録）
			adapter.setViewBinder(new ViewBinder() {
				@Override
				public boolean setViewValue(View view, Object data, String textRepresentation) {
					if (view instanceof ImageView) {
						// ビューの種類がImageViewだったら、カメラのみをリスナーに登録する
						switch (((ImageView) view).getId()) {
						case R.id.camera:
							// カスタムのクリックリスナーに登録
							((ImageView) view).setOnClickListener(myonclicklistener);
							break;
						case R.id.check:
							// チェックボックスだったら何もしない
							break;
						}
					}
					return false;
				}
			});

			// アダプターセット
			mHandler.post(new Runnable() {
				public void run() {
					// メインスレッド以外からウィジェットを操作できないので、ハンドラーからアダプターにセットさせる
					((ListView) findViewById(R.id.listView1)).setAdapter(adapter);
					mDateText.setText(mYear + "年" + mMonth + "月" + mDay + "日"
							+ Integer.toString(mWorkIdList.size()) + "件");
					if (mWorkIdList.size() == 0) {
						mNoDataText.setText("該当データがありません。");
					} else if (mWorkIdList.size() > 0) {
						mNoDataText.setText("");
					}

				}
			});

			// リストビューをリスナーに登録
			((ListView) findViewById(R.id.listView1)).setOnItemClickListener(this);

		} else if (request == GET_STAFF_NAME) {

			// スタッフ名の取得だったときの処理
			final List<Staff> staffList = gson.fromJson(jsonString, new TypeToken<List<Staff>>() {
			}.getType());

			// アダプターセット
			mHandler.post(new Runnable() {
				public void run() {
					// 該当のスタッフID１人のみが帰ってくるので、要素0番のスタッフ名をgetしてスタッフ名に代入
					mStaffName = (staffList.get(0)).getStaff_name();
					// 取得したスタッフ名と、表示しているリストの日付を表示
					mStaffNameText.setText("担当：" + mStaffName);
				}
			});

		} else if (request == GET_STAFF_LIST) {

			// スタッフ全員のリスト取得だったときの処理
			List<Staff> staffList = gson.fromJson(jsonString, new TypeToken<List<Staff>>() {
			}.getType());

			// ダイアログのスピナーにスタッフ名を表示するためのアダプター
			mStaffChangeAdp = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
			mStaffChangeAdp.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

			// 前回の情報をクリア
			mStaffChangeAdp.clear();
			mStaffIdList.clear();

			// サーバーからスタッフIDとスタッフ名の一覧を取得
			for (Staff staff : staffList) {

				// スタッフIDのリストにIDを全員分セット
				mStaffIdList.add(staff.getStaff_id());

				// アダプターにスタッフ名を全員分セット
				mStaffChangeAdp.add(staff.getStaff_name());
			}

			// アダプターに情報のセットが終わったことを知らせる
			mStaffChangeAdp.notifyDataSetChanged();

			// スピナーにアダプターの内容（スタッフ名）をセット
			mHandler.post(new Runnable() {
				public void run() {

					// メインスレッド以外からウィジェットを操作できないので、ハンドラーからセットさせる
					mStaffSpinner.setAdapter(mStaffChangeAdp);
				}
			});

			// スピナーをリスナーに登録
			mStaffSpinner.setOnItemSelectedListener(this);

		}

		// if(opening==true){
		//
		// }else{
		// プログレスダイアログを消去
		// if(mDia.isShowing()==true){
		// mDia.dismiss();
		// }
		// if(mDia2.isShowing()==true){
		// mDia2.dismiss();
		// }
		// }
	}

	// ストリーム->文字列変換
	private String convertStreamToString(InputStream is) throws IOException {
		Log.d(TAG, methodname + "convertStreamToString" + "　スタッフID：" + mStaffId);
		if (is == null) {
			return "";
		}
		int n;
		char[] buffer = new char[4096];
		Writer writer = new StringWriter();
		try {
			// ストリームを文字列に変換して書き込む（isはinputstreamを略した変数名）
			Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			while ((n = reader.read(buffer)) != -1) {
				writer.write(buffer, 0, n);
			}
		} finally {
			// ファイルを開いて書き込んだ後閉じる感じ
			is.close();
		}
		// writerに入った文字列を返す
		return writer.toString();
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {

			new AlertDialog.Builder(MainActivity.this)
					// ダイアログにスタッフID一覧のスピナーをセット
					.setTitle("スタッフ名を選択")
					.setView(mStaffSpinner)
					.setPositiveButton(MainActivity.this.getString(R.string.submit),
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog, int whichButton) {
									/* OKボタンをクリックした時の処理 */
									// スピナーで選択されているスタッフ名に対応するスタッフIDを変数に代入
									mStaffId = Integer.toString(mStaffIdList.get(mStaffSpinner
											.getSelectedItemPosition()));

									// 変更されたスタッフIDをセットして通信を実行
									updateListView(GET_SCHEDULE);

									// スタッフ名をサーバーから取得して画面に表示
									updateListView(GET_STAFF_NAME);

									// 位置情報サービススタート
									Intent intent = new Intent(MainActivity.this,
											SendLocationServise.class);
									intent.setAction("start");
									intent.putExtra("STAFF_ID", Integer.parseInt(mStaffId));
									startService(intent);

									// プリファレンスにスタッフIDを保存する
									SharedPreferences pref = getSharedPreferences("houmon",
											MODE_PRIVATE);
									Editor e = pref.edit();
									e.putString("STAFFID", mStaffId);
									e.commit();
								}
							}).show();
			// プログレスダイアログ終了
			if (mDia3.isShowing() == true) {
				mDia3.dismiss();
			}
		}
	};

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {

	}
}
