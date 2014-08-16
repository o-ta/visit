/*
 * 写真撮影画面
 * 写真撮影に入る前の確認画面
 *
 * o-ta
 *
 */

package com.tumiki0ituki.visitcare;

import android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class Satsuei extends Activity implements OnClickListener{
	private static final String TAG = Satsuei.class.getName();
	private static final String methodname = "メソッド名：";

	//業務IDや被介護者名を表示するテキストビュー
	private TextView mTxWorkId;

	//撮影ボタン
	private Button mBtCamera;

	//キャンセルボタン
	private Button mBtBack;

	//業務IDを入れる変数
	private int mWorkId;

	//被介護者名を入れる変数
	private String mUserName;
	
	//バックキー押下時用ダイアログ
	Builder mDia;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.camera_main);

		//メイン画面から業務IDと被介護者名を受け取る
		Intent intent = getIntent();
		mWorkId = intent.getIntExtra("WORK_ID", 0);	
		mUserName= intent.getStringExtra("USER_NAME");	

		//viewのID取得とリスナー登録
		mTxWorkId = (TextView)findViewById(R.id.workid);
		mBtCamera = (Button)findViewById(R.id.button1);
		mBtBack = (Button)findViewById(R.id.button2);
		mBtCamera.setOnClickListener(this);
		mBtBack.setOnClickListener(this);

		//テキストビューに業務IDや被介護者名を表示（確認のため）
		mTxWorkId.setText("\n\n業務ID："+Integer.toString(mWorkId)+"\n\n被介護者名："+mUserName+"さん\n\nの撮影を開始します。");

	}

	//撮影またはキャンセルボタンが押されたときの処理
	@Override
	public void onClick(View v) {
		Log.d(TAG, methodname+"onClick");
		switch (v.getId()){
		//撮影ボタンが押されたとき
		case R.id.button1:
			//業務IDと被介護者名を撮影画面に渡す
			Intent intent = new Intent(this,Camera.class);
			intent.putExtra("WORK_ID", mWorkId);
			intent.putExtra("USER_NAME", mUserName);
			startActivityForResult(intent,0);
			break;
			//キャンセルボタンが押されたとき
		case R.id.button2:
			//中断確認ダイアログを表示する
			breakDialog();	
			break;
		default:
		}

	}

	//撮影画面から戻ってきたときの処理
	//撮影が成功したときのみ、メイン画面にRESULT_OKを渡す
	//撮影せずに戻ってきたときは、この画面で止まったまま何もしない
	public void onActivityResult(int rqcode,int result,Intent data){
		Log.d(TAG, methodname+"onActivityResult");
		if(result==RESULT_OK){
			Intent intent = new Intent();
			setResult(RESULT_OK,intent);
			finish();
		}
	}

	//端末の戻るボタンの処理
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {

		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
				breakDialog();				
			}
		}
		return super.dispatchKeyEvent(event);
	}
	
	public void breakDialog (){
		Log.d(TAG, "breakDialog: " + "中断用のダイアログが出るよ");
		mDia = new AlertDialog.Builder(Satsuei.this);
		mDia.setTitle("撮影を中断しますか？");
		mDia.setMessage("中断すると、写真を登録せずにメイン画面に戻ります。");
		mDia.setPositiveButton("中断",
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,
					int which) {
				Log.d(TAG, "breakDialog: " + "中断用ボタンが押されたよ");
				finish();
			}
		});
		mDia.setNegativeButton("続行",
				new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog,
					int which) {
				Log.d(TAG, "breakDialog: " + "続行ボタンが押されたよ");
				return;
			}
		});
		mDia.create();
		mDia.show();
	}
	
}