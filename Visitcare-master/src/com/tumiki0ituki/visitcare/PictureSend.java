/*
 * 画像送信画面.
 * 撮った写真を送るための画面
 *
 * o-ta
 *
 */

package com.tumiki0ituki.visitcare;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

public class PictureSend extends Activity implements OnClickListener{
	private static final String TAG = PictureSend.class.getName();
	private static final String methodname = "メソッド名：";

	//業務IDや被介護者名を表示するテキストビュー
	TextView mTxWorkId;

	//ファイル名を表示するテキストビュー
	TextView mTxFileName;

	//撮った画像のプレビューを表示するイメージビュー
	private ImageView mIvImage;

	//送信ボタン
	Button mBtSend;

	//撮り直しボタン
	Button mBtRe;

	//画像登録用のURLを保持する変数
	private String URL = "";

	//画像登録用URLを保持する変数
	private String mRegistUrl;

	//画像保存用の専用フォルダ（camera.javaで作成されたもの）
	private String mDir;

	//ファイル名を保持する変数
	private String mFileName;

	//業務IDを保持する変数
	private int mWorkId;

	//被介護者名を保持する変数
	private String mUserName;

	//イメージビューにプレビューを表示するときにビットマップデータを保持する変数
	private Bitmap bitmap;

	//画像を送信するときにビットマップデータを保持する変数
	private Bitmap bm;

	//HTTP通信でデータを取得する際のURLを格納する変数
	private HttpGet mObjGet ;

	//http通信のレスポンスを受け取る変数
	HttpResponse response;

	//画像送信中に表示するプログレスダイアログ
	private ProgressDialog mDia;

	//通信エラーのダイアログを表示する際に使うハンドラ
	private Handler mHandler;

	//通信時のレスポンス
	HttpResponse res;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_send);

		//通信エラーダイアログ用ハンドラを生成
		mHandler = new Handler();

		//撮影画面からファイル名、業務ID、被介護者名を受け取る
		Intent intent = getIntent();
		mFileName = intent.getStringExtra("FILE_NAME");
		mDir = intent.getStringExtra("DIR_NAME");
		mWorkId = intent.getIntExtra("WORK_ID",0);
		mUserName= intent.getStringExtra("USER_NAME");

		//IDの取得とリスナー登録
		mTxWorkId = (TextView)findViewById(R.id.workid);
		mTxFileName = (TextView)findViewById(R.id.filename);
		mIvImage = (ImageView)findViewById(R.id.img);
		mBtSend = (Button)findViewById(R.id.button1);
		mBtRe = (Button)findViewById(R.id.button2);
		mBtSend.setOnClickListener(this);
		mBtRe.setOnClickListener(this);

		//画像登録用のURLを取得するためのAPIを指定
		URL = getString(R.string.image_send_url);

		//業務IDなどの情報を確認用に表示
		mTxWorkId.setText("\n業務ID："+Integer.toString(mWorkId)+"\n被介護者名："+mUserName+"　さん");
		mTxFileName.setText("ファイル名「"+mFileName+"」で送信します。");


		//画像のプレビューをイメージビューに表示する処理
		URL url = null;
		try {

			//画像ファイルを指定
			url = new URL("file://"+mDir + mFileName);

			//ビットマップを一度nullにする（しないと撮り直し何度目かでメモリオーバーで落ちるので）
			if (bitmap != null) {
				bitmap.recycle();
				bitmap = null;
			}

			//ビットマップストリームにデコード
			bitmap = BitmapFactory.decodeStream(url.openStream());


		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		//イメージビューにビットマップを表示
		mIvImage.setImageBitmap(bitmap);
	}

	//撮影または撮り直しボタンが押されたときの処理
	@Override
	public void onClick(View v) {
		Log.d(TAG, methodname+"onClick");
		switch (v.getId()){
		//送信ボタンが押されたとき
		case R.id.button1:
			try {
				//読み込み用のオプションオブジェクトを生成
				BitmapFactory.Options options = new BitmapFactory.Options();
				//画像の縦横サイズを4/1に設定
				options.inSampleSize = 4;

				/*
				 * サイズを絶対値で指定したい場合---ここから
				 * */


				//画像を読み込まずに画像の縦横サイズだけを取り出す
				//				options.inJustDecodeBounds = true;
				//				BitmapFactory.decodeFile("/sdcard/houmonimg/"+mFileName, options);

				//数値に絶対値を入れれば、それ以下のサイズにリサイズできる
				//				int scaleW = options.outWidth / 380 + 1;
				//				int scaleH = options.outHeight / 420 + 1;
				//何分の1にする場合の指定方法
				//				int scaleW = 3;
				//				int scaleH = 3;

				//実際に設定
				//				int scale = Math.max(scaleW, scaleH);

				//サイズだけでなく実際に画像を読み込む
				//				options.inJustDecodeBounds = false;

				//上で指定したサイズを画像に設定
				//				options.inSampleSize = scale;

				//指定したサイズで画像を読み込む
				//				bm = BitmapFactory.decodeFile(path, options);


				/*
				 * サイズを絶対値で指定したい場合---ここまで
				 * */

				//ビットマップをデコード
				bm = BitmapFactory.decodeFile(mDir+mFileName, options);

				if (!isFinishing()) {
				// プログレスダイアログ表示
				mDia = new ProgressDialog(PictureSend.this);
				mDia.setTitle("画像送信中");
				mDia.setMessage("しばらくお待ちください。。。");
				mDia.setIndeterminate(false);
				mDia.setButton(
						DialogInterface.BUTTON_NEGATIVE,
						"キャンセル",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int which) {
								// mDia をキャンセル
								dialog.cancel();
							}
						});
				// mDia のキャンセルされた時に呼び出されるコールバックを登録
				mDia.setOnCancelListener(new DialogInterface.OnCancelListener() {
					public void onCancel(DialogInterface dialog) {
						// Thread を停止
						onStop();
					}
				});
				mDia.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				mDia.show();
				}
				Thread thread = new Thread(null, new Runnable() {
					public void run() {
						//APIから画像送信用URLを取得する処理
						getSendImageUrl();

						//送信用URLへ画像を送信する処理
						try {
							executeMultipartPost();
						} catch (Exception e) {

							e.printStackTrace();
						}

						handler.sendEmptyMessage(0);
					}
				});
				thread.start();

			} catch (Exception e) {
				e.printStackTrace();
			}

			break;
		case R.id.button2:
			//撮り直しボタンが押されたときプレビュー画面に戻る
			finish();
			break;
		default:
		}
	}

	//アクティビティ終了時の処理
	@Override
	public void finish() {
		Log.d(TAG, methodname+"finish");
		//画像を削除する
		deleteFile();

		//業務IDを渡して再び撮影画面に（撮り直し処理）
		Intent intent = new Intent();
		setResult(RESULT_CANCELED,intent);

		super.finish();
	}

	public void deleteFile(){
		//画像ファイルを削除
		File file = new File(mDir + mFileName);
		file.delete();

		//イメージビューからビットマップをはがす
		mIvImage.setImageBitmap(null);
	}

	//画像送信用URLへ画像を送信する処理
	public void executeMultipartPost() throws Exception {
		Log.d(TAG, methodname+"executeMultipartPost");

		//バイトアレーストリーム生成
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		//ビットマップの変換を品質75のjpegに設定
		bm.compress(CompressFormat.JPEG, 75, bos);

		//バイトアレイを配列dataに格納
		byte[] data = bos.toByteArray();

		//HttpClient生成
		final HttpClient httpClient = new DefaultHttpClient();

		//HttpPost生成（APIから取得した画像送信用URLを引数に設定）
		final HttpPost postRequest = new HttpPost(mRegistUrl);

		//ByteArrayBodyの引数３つのコンストラクタ（ビットマップデータ、コンテントタイプ、ファイル名）
		ByteArrayBody bab = new ByteArrayBody(data,"image/jpeg", mFileName);

		//MultipartEntity生成
		MultipartEntity reqEntity = new MultipartEntity(
				HttpMultipartMode.BROWSER_COMPATIBLE);

		//imgの名前でEntityにバイトアレイボティをadd
		reqEntity.addPart("img", bab);

		//HttpPostにEntityをセット
		postRequest.setEntity(reqEntity);

		//通信実行
		response = httpClient.execute(postRequest);
		Log.d("response","■■■■■■■■■■■■■");

	}

	// サーバーから画像送信用URLを取得するメソッド
	private void getSendImageUrl() {
		Log.d(TAG, methodname+"getSendImageUrl");
		try {
			// WebAPIの実行（HTTP通信）
			HttpResponse objResponse = getHttpSV();
			// 取得した送信用URLをセットする
			setUrl(objResponse);

		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch(SocketTimeoutException e){
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally{


			//通信エラーダイアログ
			mHandler.post(new Runnable() {
				public void run() {

					if(res==null){
						Log.d("通信エラー", "");
						// プログレスダイアログ終了
						if(mDia.isShowing()==true){
							mDia.dismiss();
							}
						//通信エラーダイアログ表示
						if (!isFinishing()) {
						new AlertDialog.Builder(PictureSend.this)
						.setTitle("通信エラー")
						.setMessage("通信エラーが発生しました。\nもう一度送信し直してください")
						.setPositiveButton("OK", null)
						.show();
						}
					}

				}
			});
		}
	}

	// WebAPIの実行（HTTP通信）
	private HttpResponse getHttpSV() throws ClientProtocolException, IOException {
		Log.d(TAG, methodname+"getHttpSV");
		// 戻り値の初期化
		res = null;
		// パラメータの生成
		HttpParams para = new BasicHttpParams();
		// 接続のタイムアウトの設定(アパッチが提供する通信するときのパラメータを設定するメソッド)
		HttpConnectionParams.setConnectionTimeout(para, 3000);
		// データ取得のタイムアウトの設定
		HttpConnectionParams.setSoTimeout(para, 3000);

		// HTTP通信実行
		//アパッチが提供するクラス
		HttpClient objCli = new DefaultHttpClient(para);
		mObjGet = new HttpGet(URL);

		//通信を実行（ここまではお決まりの記述）
		res = objCli.execute(mObjGet);

		//resに結果を取り込む
		if (res.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK) {
			//通信は成功したけど取れない場合

			return null;
		}
		Log.d("通信成功", res.toString());
		return res;
	}


	// リストの設定
	private void setUrl(HttpResponse httpRes) throws IOException {
		Log.d(TAG, methodname+"setUrl");
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

		//json取得用のクラス（ImgUrl）にjsonからデータを入れてもらうリストを生成（１つだけなんだけど・・）
		List<ImgUrl> urlList = gson.fromJson(
				jsonString, 
				new TypeToken<List<ImgUrl>>(){}.getType());

		//データ（画像送信用URLは１つだけなので、要素0番のURLを取得してmRegistUrlに格納する）
		mRegistUrl = (urlList.get(0)).getUrl();
		Log.d("画像アップURLが入った", mRegistUrl);
	}

	// ストリーム->文字列変換
	private String convertStreamToString(InputStream is) throws IOException {
		Log.d(TAG, methodname+"convertStreamToString");
		if (is == null) {
			return "";
		}
		int n;
		char[] buffer = new char[4096];
		Writer writer = new StringWriter();
		try {
			//ストリームを文字列に変換して書き込む（isはinputstreamを略した変数名）
			Reader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
			while ((n = reader.read(buffer)) != -1) {
				writer.write(buffer, 0, n);
			}
		} finally {
			//ファイルを開いて書き込んだ後閉じる感じ
			is.close();
		}
		//writerに入った文字列を返す
		return writer.toString();
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			// プログレスダイアログ終了
			if(mDia.isShowing()==true){
				mDia.dismiss();
				}
			//通信結果ダイアログ
			if (response == null){
				return;
			}

			if (response.getStatusLine().getStatusCode() != HttpURLConnection.HTTP_OK) {

				//通信が失敗した場合
				Log.d("画像アップURL", mRegistUrl);
				if (!isFinishing()) {
				new AlertDialog.Builder(PictureSend.this)
				.setTitle("通信エラー")
				.setMessage("通信エラーが発生しました。\nデータは正常に送信されませんでした。")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
					public void onClick(DialogInterface dialog, int whichButton) { 
						/* OKボタンをクリックした時の処理 */
						//画像を削除する
						deleteFile(); 

						//RESULT_OKでアクティビティを終了
						Intent intent = new Intent();
						setResult(RESULT_OK,intent);

						PictureSend.super.finish();
						return;
					}
				})
				.show();
				}
			}else{
				if (!isFinishing()) {
				//通信が成功した場合
				new AlertDialog.Builder(PictureSend.this)
				.setTitle("送信完了")
				.setMessage("データは正常に送信されました。")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() { 
					public void onClick(DialogInterface dialog, int whichButton) { 
						/* OKボタンをクリックした時の処理 */
						//画像を削除する
						deleteFile();

						//RESULT_OKでアクティビティを終了
						Intent intent = new Intent();
						setResult(RESULT_OK,intent);

						PictureSend.super.finish();
						//    					return;
					}
				})
				.show();
				}
			}

		}
	};

}

