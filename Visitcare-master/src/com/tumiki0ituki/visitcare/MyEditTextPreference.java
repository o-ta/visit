/*
 * MyEditTextPreference.
 * EditTextPreferenceのカスタマイズクラス
 *
 * o-ta
 *
 */
package com.tumiki0ituki.visitcare;

import android.R;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.preference.EditTextPreference;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class MyEditTextPreference extends EditTextPreference {
	/** Sring:保存値*/
	private String preferenceValue = "";

	/** エディットテキスト */
	private EditText mEditText;

	/** タグ：クラス名 */
	private static final String TAG = MyEditTextPreference.class.getName();

	public MyEditTextPreference(Context context, AttributeSet attrs) {

		super(context, attrs);
		Log.d(TAG, "MyEditTextPreference  Start!");

	}

	public MyEditTextPreference(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
		Log.d(TAG, "MyEditTextPreference  Start!");

	}

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		Log.d(TAG, "onGetDefaultValue  Start!");
		return a.getString(index);
	}

	@Override
	protected void onSetInitialValue(boolean restorePersistedValue,
			Object defaultValue) {
		Log.d(TAG, "onSetInitialValue  Start!");

		if (restorePersistedValue) {
			preferenceValue = getPersistedString(preferenceValue);
		} else {
			preferenceValue = (String) defaultValue;
			persistString(preferenceValue);
		}
		Log.d(TAG, "onSetInitialValue  END");
	}

	/**
	 * ダイアログが展開される時に呼び出されます.
	 */
	@Override
	protected View onCreateDialogView() {
		Log.d(TAG, "onCreateDialogView  Start!");

		// 自前のレイアウトを読み込む
		LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.custom_edittext_preference, null);
		mEditText = (EditText) view.findViewById(R.id.editText1);

		Log.d(TAG, "onCreateDialogView  END");
		return view;
	}

	/**
	 * ダイアログが閉じる時に呼び出されます.
	 */
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		Log.d(TAG, "onDialogClosed  Start!");

		if (positiveResult) {
			String edittime = mEditText.getText().toString(); // エディットテキストの文字を取得
			int relordtime = Integer.parseInt(edittime);	// intに変換

			preferenceValue = edittime;
			boolean ret = persistString(preferenceValue);// プリファレンスに保存する
			Log.d(TAG, "保存結果--->" + Boolean.toString(ret));

			// 入力された更新間隔でサービスを再起動させる
			Intent intent = new Intent(getContext(),
					SendLocationServise.class);
			intent.setAction("start");
			 intent.putExtra("更新間隔", relordtime);
			getContext().startService(intent);
		}

		super.onDialogClosed(positiveResult);
		Log.d(TAG, "onDialogClosed  END");
	}
}
