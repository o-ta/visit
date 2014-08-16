/*
 * カメラ撮影
 * カメラ撮影処理
 *
 * o-ta
 * 
 */

package com.tumiki0ituki.visitcare;

import java.io.FileOutputStream;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Camera;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraView extends SurfaceView 
implements SurfaceHolder.Callback, Camera.PictureCallback {
	private static final String TAG = CameraView.class.getName();
	private static final String methodname = "メソッド名：";
	
	//業務IDを入れる変数
	private int mWorkId;
	
	//被介護者名を入れる変数
	private String mUserName;
	
	//ホルダー
	private SurfaceHolder mHolder;
	
	//カメラ
	private static Camera mCamera;
	
	//コンテキスト
	private Context mContext;
	
	//画像保存用の専用フォルダ（camera.javaで作成されたもの）
	private String mDir;
	
	//撮影したときに自動でふられるファイル名を入れる変数
	private String mFileName;

	//コンストラクタ
	public CameraView(Context context,int workId,String userName,String dir) {
		
		//スーパークラスのコンストラクタ実行
		super(context);
		
		Log.d(TAG, methodname+"CameraView");
		
		//サーフェスホルダーの取得
		mHolder = getHolder();
		
		//サーフェスホルダーの通知先を指定
		mHolder.addCallback(this);
		
		//サーフェスホルダーの種別を指定
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		
		//業務IDをセット
		this.mWorkId=workId;
		
		//被介護者名をセット
		this.mUserName=userName;
		
		//画像保存用フォルダをセット
		this.mDir = dir;
		
		//コンテキストをセット
		mContext = context;
	}

	//サーフェス生成時に実行される
	public void surfaceCreated(SurfaceHolder holder) {
		Log.d(TAG, methodname+"surfaceCreated");
		//カメラオブジェクトの生成
		setmCamera(Camera.open());
		
		//プレビューの表示先を指定
		try {
			getmCamera().setPreviewDisplay(holder);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//サーフェス変更時に呼ばれる
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
		Log.d(TAG, methodname+"surfaceChanged");
		//カメラのプレビューを開始する
		getmCamera().startPreview();
	}

	//サーフェスが破棄されたときに呼ばれる
	public void surfaceDestroyed(SurfaceHolder holder) {
		Log.d(TAG, methodname+"surfaceDestroyed");
		//コールバックを指定
		getmCamera().setPreviewCallback(null);
		
		//カメラのプレビューを停止
		getmCamera().stopPreview();
		
		//カメラのリソースを開放
		getmCamera().release();
		
		//カメラオブジェクトの開放
		setmCamera(null);
	}

	//画面をタッチしたときに呼ばれる
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		Log.d(TAG, methodname+"onTouchEvent");
		if (event.getAction() == MotionEvent.ACTION_DOWN) {
						
			//写真を撮影
			getmCamera().takePicture(null, null, this); 
		}
		return true;
	}

	//写真撮影後に呼ばれる
	public void onPictureTaken(byte[] data, Camera camera) {
		Log.d(TAG, methodname+"onPictureTaken");
		//画像のファイル名を”業務ID.jpg”に指定
		mFileName = mWorkId+".jpg";
		
		//ファイル書き出しのストリームを定義
		FileOutputStream fos = null;
		
		try {
			//ファイルの保存先を指定
			fos = new FileOutputStream(mDir+mFileName);
			
			//ファイルを書き込み
			fos.write(data);
			
			//ストリームを閉じる
			fos.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		//ファイルを書き込んだ後、ファイル名と業務IDと被介護者名を確認画面に渡す
		Intent intent = new Intent(mContext,PictureSend.class);
		intent.putExtra("FILE_NAME", mFileName);
		intent.putExtra("DIR_NAME", mDir);
		intent.putExtra("WORK_ID", mWorkId);
		intent.putExtra("USER_NAME", mUserName);
		((Activity)mContext).startActivityForResult(intent,0);
	}

	public static Camera getmCamera() {
		return mCamera;
	}

	public void setmCamera(Camera mCamera) {
		CameraView.mCamera = mCamera;
	}

}