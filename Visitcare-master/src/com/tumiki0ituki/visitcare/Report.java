/*
 * 裏方.
 * 各入力情報のgetter,setterクラス
 *
 * o-ta
 *
 */

package com.tumiki0ituki.visitcare;

import java.io.Serializable;
import java.util.Date;

/**
 * 各入力情報のgetter,setterクラス.
 * このクラスごとIntentで渡します。
 */
@SuppressWarnings("serial")
public class Report implements Serializable{

	private Date mDate;
	private Date mStartDate;
	private Date mEndDate;
	private String mAim;

	private String mNote;

	private int mWorkId;

	private String mWorkDay;
	private String mCareName;


	final int IDX_BATH = 0;
	final int IDX_CLEAN = 1;
	final int IDX_WASH = 2;
	final int IDX_SHOPPING = 3;
	final int IDX_COOK = 4;
	final int IDX_WEAR = 5;

	final int IDX_WALK = 0;
	final int IDX_MOVE = 1;
	final int IDX_TALK = 2;
	final int IDX_EAT = 3;
	final int IDX_SLEEP = 4;

	public int getmWorkId() {
		return mWorkId;
	}

	public void setmWorkId(int mWorkId) {
		this.mWorkId = mWorkId;
	}

	public String getmWorkDay() {
		return mWorkDay;
	}

	public void setmWorkDay(String mWorkDay) {
		this.mWorkDay = mWorkDay;
	}

	public String getmCareName() {
		return mCareName;
	}

	public void setmCareName(String mCareName) {
		this.mCareName = mCareName;
	}


	//提供サービスのところを配列で
	boolean mServices[] = {false, false, false, false, false, false};

	public boolean ismServices(int idx) {
		return mServices[idx];
	}

	public void setmServices(boolean val, int idx) {
		mServices[idx] = val;
	}

	//状態チェックのところを配列で
	int mStateCheck[] = {-1, -1, -1, -1, -1};

	public int getmStateCheck(int idx) {
		return mStateCheck[idx];
	}

	public void setmStateCheck(int idx, int select) {
		mStateCheck[idx] = select;
	}

	//必須項目
	public Date getmDate() {
		return mDate;
	}

	public void setmDate(Date mDate) {
		this.mDate = mDate;
	}

	public Date getmStartDate() {
		return mStartDate;
	}

	public void setmStartDate(Date mStartDate) {
		this.mStartDate = mStartDate;
	}

	public Date getmEndDate() {
		return mEndDate;
	}

	public void setmEndDate(Date mEndDate) {
		this.mEndDate = mEndDate;
	}

	public String getmAim() {
		return mAim;
	}

	public void setmAim(String mAim) {
		this.mAim = mAim;
	}

	//備考欄表示
	public String getmNote() {
		return mNote;
	}

	public void setmNote(String mNote) {
		this.mNote = mNote;
	}

	/**
	 * 時間指定の誤りを判定するメソッド
	 * 開始時間　＞　終了時間の場合true
	 * @return
	 */
	public boolean checkDateTime() {

		if(mStartDate != null && mEndDate != null){
			if(mStartDate.getTime() > mEndDate.getTime() || mStartDate.getTime() == mEndDate.getTime()){
				return true;
			}
		}
		return false;
	}

	/**
	 * 必須項目の漏れを判定するメソッド
	 * 空があるとtrue
	 * @return
	 */
	public boolean checkEmpty() {
		if (mDate == null){
			return true;
		}
		if (mStartDate == null){
			return true;
		}
		if (mEndDate == null){
			return true;
		}
		if (mAim == null){
			return true;
		}
		return false;
	}

}
