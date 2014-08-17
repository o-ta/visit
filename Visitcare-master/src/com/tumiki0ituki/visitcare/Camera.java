/*
 * カメラ撮影.
 * カメラ撮影処理
 *
 * o-ta
 *
 */

package com.tumiki0ituki.visitcare;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class Camera extends Activity {

	//ログ用
	private static final String TAG = Camera.class.getName();
	private static final String methodname = "メソッド名：";

	//業務IDを受け取る変数
	private int mWorkId;

	//被介護者名を受け取る変数
	private String mUserName;

	//画像保存用の専用フォルダの指定
	private final String mDir = "/sdcard/houmonimg/";

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//業務IDと被介護者名を受け取る
		Intent intent = getIntent();
		mWorkId = intent.getIntExtra("WORK_ID", 0);
		mUserName= intent.getStringExtra("USER_NAME");

		//フォルダを作るメソッドを呼び出し
		mkdir(mDir);

		//CameraViewをセット
		setContentView(new CameraView(this,mWorkId,mUserName,mDir));

	}

	//画像保存用にSDカードに専用のフォルダを作成
	public boolean mkdir(String path){
		Log.d(TAG, methodname+"mkdir");
		File file = new File(path);
		Log.d("フォルダ作成結果", Boolean.toString(file.mkdir()));
		if(!file.exists()){
			// フォルダが存在しない場合新規作成
			return 	file.mkdir ();
		}
		return true;
	}

	//画像確認画面から戻ってきたときの処理
	public void onActivityResult(int rqcode,int result,Intent data){
		Log.d(TAG, methodname+"onActivityResult");
		//撮影が完了して戻ってきたときのみ前の画面にRESULT_OKで戻る
		//撮影が完了していなければ（撮り直しならば）撮影画面のまま

		//カメラ
		android.hardware.Camera camera = CameraView.getmCamera();

		if(camera!=null){

			//コールバックを指定
			camera.setPreviewCallback(null);

			//カメラのプレビューを停止
			camera.stopPreview();

			//カメラのリソースを開放
			camera.release();

			//カメラオブジェクトの開放
			camera = null;
		}
		if(result==RESULT_OK){
			Intent intent = new Intent();
			setResult(RESULT_OK,intent);
			finish();
		}
	}
}