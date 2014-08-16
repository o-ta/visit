/*
 * SendLocationDialog
 * EditTextPreferenceのカスタマイズ
 *
 * o-ta
 *
 */

package com.tumiki0ituki.visitcare;

import android.R;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class SendLocationDialog extends DialogPreference {

	/** エディットテキスト */
	private EditText mEditText;

	/** テキスト */
	private String mText;

	/** タグ：クラス名 */
	private static final String TAG = SendLocationDialog.class.getName();

	public SendLocationDialog(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mEditText = new EditText(context, attrs);

		// Give it an ID so it can be saved/restored
		mEditText.setId(R.id.sendloc_editText);

		mEditText.setEnabled(true);
	}

	public SendLocationDialog(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SendLocationDialog(Context context) {
		this(context, null);
	}

	/**
	 * EditTextに入力された文字をプリファレンスに保存します
	 * 
	 * @param text
	 *            保存するためのテキスト
	 */
	public void setText(String text) {
		Log.d(TAG, "setText　Start!");
		final boolean wasBlocking = shouldDisableDependents();

		mText = text;

		persistString(text);

		final boolean isBlocking = shouldDisableDependents();
		if (isBlocking != wasBlocking) {
			notifyDependencyChange(isBlocking);
		}
		Log.d(TAG, "setText　End");
	}

	/**
	 * ダイアログが展開される時に呼び出されます
	 */
	@Override
	protected View onCreateDialogView() {
		Log.d(TAG, "onCreateDialogView　Start!");

		// 自前のレイアウトを読み込む
		LayoutInflater inflater = (LayoutInflater) getContext()
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = inflater.inflate(R.layout.sendloc_dialog, null);
		mEditText = (EditText) view.findViewById(R.id.sendloc_editText);

		Log.d(TAG, "onCreateDialogView　End");
		return view;
	}

	/**
	 * プリファレンスからデータを取り出すときに呼び出されます
	 * 
	 * @return The current preference value.
	 */
	public String getText() {
		Log.d(TAG, "getText　Start!");

		Log.d(TAG, "getText　End!");
		return mText;
	}

	/**
	 * ダイアログが閉じられるときに呼び出されます
	 * 
	 * @return The current preference value.
	 */
	@Override
	protected void onDialogClosed(boolean positiveResult) {
		Log.d(TAG, "onDialogClosed　Start!");

		super.onDialogClosed(positiveResult);

		// エディットテキストに入力された文字を取得
		String value = mEditText.getText().toString();
		if (positiveResult) {
			Log.d(TAG, "onDialogClosed----positiveResult true-----");
			if (value.equals("")) {
				// EditTextに何も入力されていない時の処理
				new AlertDialog.Builder(getContext())
						.setTitle(R.string.sl_dialog_title)
						.setMessage(R.string.sl_dialog_message)
						.setPositiveButton(R.string.sl_dialog_button,
								new DialogInterface.OnClickListener() {

									// 「はい」ボタンが押下された時の処理
									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO 自動生成されたメソッド・スタブ
										Log.d(TAG,
												"■■■■■onClick(Dialogのボタン)■■■■■■■■■");
									}
								}).show();
				Log.d(TAG, "onDialogClosed　End---エラー時--");

				return;// 何もしない
			}

			// サービスをスタートさせる
			Intent intent = new Intent(getContext(), SendLocationServise.class);
			intent.setAction("start");
			intent.putExtra(getContext().getString(R.string.sl_Reloadtime_key), Integer.parseInt(value));
			getContext().startService(intent);
			setText(value);
			Log.d(TAG, "更新時間(relordtime--->" + Integer.parseInt(value));
		}
		Log.d(TAG, "onDialogClosed　End");
	}

	// }

	@Override
	protected Object onGetDefaultValue(TypedArray a, int index) {
		Log.d(TAG, "onGetDefaultValue　Start!");
		Log.d(TAG, "onGetDefaultValue　End(戻り値　index--->" + index);
		return a.getString(index);
	}

	@Override
	protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
		Log.d(TAG, "onSetInitialValue　Start!");
		setText(restoreValue ? getPersistedString(mText)
				: (String) defaultValue);

		Log.d(TAG, "onSetInitialValue　End");
	}

	@Override
	public boolean shouldDisableDependents() {
		Log.d(TAG, "shouldDisableDependents　Start!");
		Log.d(TAG, "shouldDisableDependents　End");
		return TextUtils.isEmpty(mText) || super.shouldDisableDependents();
	}

	/**
	 * Returns the {@link EditText} widget that will be shown in the dialog.
	 * 
	 * @return The {@link EditText} widget that will be shown in the dialog.
	 */
	public EditText getEditText() {
		Log.d(TAG, "getEditText　Start!");
		Log.d(TAG, "getEditText　End(戻り値　mEditText--->" + mEditText);
		return mEditText;
	}

	/** @hide */
	protected boolean needInputMethod() {
		Log.d(TAG, "needInputMethod　Start!");
		Log.d(TAG, "needInputMethod　End");
		return true;
	}

	@Override
	protected Parcelable onSaveInstanceState() {
		Log.d(TAG, "onSaveInstanceState　Start!");
		final Parcelable superState = super.onSaveInstanceState();
		if (isPersistent()) {
			return superState;
		}

		final SavedState myState = new SavedState(superState);
		myState.text = getText();
		
		Log.d(TAG, "onSaveInstanceState　End");
		return myState;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		Log.d(TAG, "onRestoreInstanceState　Start!");
		
		if (state == null || !state.getClass().equals(SavedState.class)) {
			// Didn't save state for us in onSaveInstanceState
			super.onRestoreInstanceState(state);
			return;
		}

		SavedState myState = (SavedState) state;
		super.onRestoreInstanceState(myState.getSuperState());
		setText(myState.text);
		
		Log.d(TAG, "onRestoreInstanceState　End");
	}

	private static class SavedState extends BaseSavedState {
		String text;

		public SavedState(Parcel source) {
			super(source);
			Log.d(TAG, "SavedState　Start!");
			text = source.readString();
			Log.d(TAG, "SavedState　End");
		}

		@Override
		public void writeToParcel(Parcel dest, int flags) {
			super.writeToParcel(dest, flags);
			Log.d(TAG, "writeToParcel　Start!");
			dest.writeString(text);
			Log.d(TAG, "writeToParcel　End");
		}

		public SavedState(Parcelable superState) {
			super(superState);
			Log.d(TAG, "SavedState　Start!");
			Log.d(TAG, "SavedState　End");
		}

		public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.Creator<SavedState>() {
			public SavedState createFromParcel(Parcel in) {
				Log.d(TAG, "createFromParcel　Start!");
				Log.d(TAG, "createFromParcel　End");
				return new SavedState(in);
			}

			public SavedState[] newArray(int size) {
				Log.d(TAG, "newArray　Start!");
				Log.d(TAG, "newArray　End");
				return new SavedState[size];
			}
		};
	}

}
