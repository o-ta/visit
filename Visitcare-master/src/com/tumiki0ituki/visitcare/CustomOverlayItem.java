/***
 * Copyright (c) 2011 readyState Software Ltd
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */

package com.tumiki0ituki.visitcare;

import android.R;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class CustomOverlayItem extends OverlayItem {

	protected String mImageURL;
	private static final String TAG = CustomOverlayItem.class.getName();
	private int test[] = { R.drawable.number_1, R.drawable.number_2,
			R.drawable.kawagoe4, R.drawable.number_4, R.drawable.ono,
			R.drawable.oota, R.drawable.hagio, R.drawable.number_8,
			R.drawable.tagomori };

	private int mId;

	public CustomOverlayItem(GeoPoint point, String title, String snippet,
			int id) {
		super(point, title, snippet);
		Log.d(TAG, "CustomOverlayItem");
		mId = id - 2;
	}

	public CustomOverlayItem(GeoPoint point, String title, String snippet) {
		super(point, title, snippet);
		Log.d(TAG, "CustomOverlayItem");
	}

	/**
	 * バルーンに表示されるスタッフの画像を取得します
	 */
	public int getImageid() {
		Log.d(TAG, "getImageid");
		return test[mId];
	}

	/**
	 * マップに表示されるスタッフのindexを取得します
	 */
	public int getPositionid() {
		Log.d(TAG, "getImageid");
		return mId;
	}

}
