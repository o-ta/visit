/*
 * 日付変更画面.
 * 表示する訪問リストの日付を変更
 *
 * o-ta
 *
 */

package com.tumiki0ituki.visitcare;

import java.util.Calendar;

import android.R;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;

public class DaySelect extends Activity implements OnClickListener,OnDateChangedListener {
	private static final String TAG = DaySelect.class.getName();
	private static final String methodname = "メソッド名：";

	//デフォルトで表示する今日の日付を取得するためのカレンダークラス生成	
	Calendar calendar = Calendar.getInstance();

	//年月日
	int year = calendar.get(Calendar.YEAR);
	int month = calendar.get(Calendar.MONTH)+1;
	int day = calendar.get(Calendar.DAY_OF_MONTH);

	//ピッカー
	DatePicker picker;

	//送信ボタン
	Button select;

	//キャンセルボタン
	Button back;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_dayselect);

		//ID取得、リスナー登録
		select = (Button)findViewById(R.id.select_button);
		back = (Button)findViewById(R.id.back_button);
		select.setOnClickListener(this);
		back.setOnClickListener(this);

		//ピッカーのデフォルトに今日の日付をセット、レイアウトを年/月/日にセット
		picker=(DatePicker)findViewById(R.id.datePicker1);
		picker.init(year, month-1, day, this);
		picker.layout(year, month, day, 0);

	}

	//ピッカーの値が変更されたときに呼ばれる
	@Override
	public void onDateChanged(DatePicker view, int selectyear, int selectmonth,
			int selectday) {
		Log.d(TAG, methodname+"onDateChanged");
		//選択された日付を変数に代入
			year = selectyear;
			month = selectmonth+1;
			day = selectday;
	}

	//送信またはキャンセルボタンが押されたときに呼ばれる
	@Override
	public void onClick(View v) {
		Log.d(TAG, methodname+"onClick");
		switch (v.getId()){
		//日付選択ボタンがクリックされたときに年月日をメイン画面へ渡す
		case R.id.select_button:
			Intent intent = new Intent();
			intent.putExtra("YEAR", year);
			intent.putExtra("MONTH", month);
			intent.putExtra("DAY", day);
			setResult(RESULT_OK,intent);
			finish();
			break;
		//戻るボタンが押されたときに値を何も渡さずにCANCELEDを返す
		case R.id.back_button:
			Intent intent1 = new Intent();
			setResult(RESULT_CANCELED,intent1);
			finish();
			break;
		default:
		}
	}

	//物理キーのバックボタンが押された時に呼ばれる（バックボタンで戻ったとき落ちるのを防ぐ）
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		Log.d(TAG, methodname+"dispatchKeyEvent");
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
				//端末のバックボタンが押されたら、キャンセルボタンが押されたときと同じ処理をする
				Intent intent1 = new Intent();
				setResult(RESULT_CANCELED,intent1);
				finish();
				return false;
			}
		}
		return super.dispatchKeyEvent(event);
	}

}
