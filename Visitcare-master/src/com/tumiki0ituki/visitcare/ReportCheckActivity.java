/*
 * 日報、２画面目
 * 状態チェック入力画面
 *
 * o-ta
 *
 */
package com.tumiki0ituki.visitcare;

import android.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

/**
 * 状態チェック画面
 */
public class ReportCheckActivity extends Activity {
	/** Called when the activity is first created. */

	/**
	 * 各入力情報のgetter,setterクラス。このクラスごとIntentで渡す。
	 */
	private Report mReport;

	int mBtns[] = {R.id.walk_button, R.id.move_button, R.id.talk_button, R.id.eat_button, R.id.sleep_button};
	int mTvs[] = {R.id.walk_text, R.id.move_text, R.id.talk_text, R.id.eat_text, R.id.sleep_text};
	
	ArrayAdapter<CharSequence> adapter5;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		//Activity起動時のソフトキーボード制御
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		setContentView(R.layout.report_check);
				
		Bundle extras = getIntent().getExtras();
		mReport = (Report) extras.getSerializable("report");
		
		//タイトルバーに該当業務の日付と被介護者名を表示
		setTitle(mReport.getmWorkDay() + " " + mReport.getmCareName());
		
		
		adapter5 = ArrayAdapter.createFromResource(
				this, R.array.check, android.R.layout.simple_dropdown_item_1line);

	
		//既に入力情報がある場合は表示
		for(int i = 0; i < mTvs.length; i++){
			if (mReport.getmStateCheck(i) != -1){
				TextView tv = (TextView)findViewById(mTvs[i]);
				tv.setText((String) adapter5.getItem(mReport.getmStateCheck(i)));
			}
		}
		
				
		//備考欄
		if (mReport.getmNote() != null){
			EditText editText = (EditText)findViewById(R.id.editText1);
			editText.setText(mReport.getmNote());
		}
		
				
		//項目ボタン
		for (int i = 0; i < mBtns.length; i++){
			Button btn = (Button)findViewById(mBtns[i]);
			btn.setOnClickListener(new OnClickListener() {				
				@Override
				public void onClick(View v) {
					makeDialog(v);
				}
			});
		}
		

		//確認画面へボタン
		Button btn1 = (Button)findViewById(R.id.go_sendingActivity_button);
		btn1.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {				
				//備考欄EditText
				EditText editText;
				editText = (EditText)findViewById(R.id.editText1);
				String note = editText.getText().toString();
				mReport.setmNote(note);
				Intent intent = new Intent(ReportCheckActivity.this,
						ReportSendingActivity.class);
				intent.putExtra("report", mReport);
				startActivityForResult(intent, 0);
			}
		});		

		
		//戻るボタン　前の画面に戻るのみ
		Button btn2 = (Button)findViewById(R.id.go_reportActivity_button);
		btn2.setText("戻る");
		btn2.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				EditText editText;
				editText = (EditText)findViewById(R.id.editText1);
				String note = editText.getText().toString();
				mReport.setmNote(note);
				Intent intent = new Intent(ReportCheckActivity.this,
						ReportActivity.class);
				intent.putExtra("report", mReport);
				setResult(RESULT_CANCELED, intent);
				finish();				
			}
		});				
	}
	
	
	//サーバー送信後のダイアログボタンから戻ってきた時の処理
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if(resultCode == RESULT_OK){
			setResult(RESULT_OK);
			this.finish();
		}else  {
			Bundle extras = data.getExtras();
			mReport = (Report) extras.getSerializable("report");
		}	
	}

	
	//状態チェックの項目ボタンが押された時のダイアログ表示処理
	public void makeDialog(final View v){
		// リスト表示する文字列
		AlertDialog.Builder checkDlg = new AlertDialog.Builder(ReportCheckActivity.this);
		checkDlg.setTitle("いずれか選択してください。");
		checkDlg.setAdapter(adapter5, new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				TextView textView;
				//ボタンのIDを取ってくる
				int id = v.getId();
				for (int i = 0; i < mBtns.length; i++) {
					if (id == mBtns[i]){						
						textView = (TextView)findViewById(mTvs[i]);
						textView.setText((String) adapter5.getItem(which));						
						mReport.setmStateCheck(i, which);
					}
				}
			}
		});
		checkDlg.create();
		checkDlg.show();
	}
	

	//端末の戻るボタンの処理
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {

		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
				Intent intent = new Intent(ReportCheckActivity.this,
						ReportActivity.class);
				intent.putExtra("report", mReport);
				setResult(RESULT_CANCELED, intent);
				finish();				
			}
		}
		return super.dispatchKeyEvent(event);
	}
}