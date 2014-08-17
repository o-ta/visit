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

import java.util.ArrayList;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.util.Log;

import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class CustomItemizedOverlay<Item extends OverlayItem> extends
		BalloonItemizedOverlay<CustomOverlayItem> {

	private ArrayList<CustomOverlayItem> m_overlays = new ArrayList<CustomOverlayItem>();
	private Context c;
	private static final String TAG = CustomItemizedOverlay.class.getName();
	private int mIndex = 0;;
	private boolean mImg_flg = false;

	public CustomItemizedOverlay(Drawable defaultMarker, MapView mapView, boolean pimg_flg) {
		super(boundCenter(defaultMarker), mapView);
		mImg_flg = pimg_flg;
		Log.d("CustomItemizedOverlay", Boolean.toString(mImg_flg));
		Log.d(TAG, "CustomItemizedOverlay");
		c = mapView.getContext();

	}

	public CustomItemizedOverlay(Drawable defaultMarker, MapView mapView) {
		super(boundCenter(defaultMarker), mapView);
		Log.d(TAG, "CustomItemizedOverlay");
		c = mapView.getContext();
	}

	public void addOverlay(CustomOverlayItem overlay) {
		Log.d(TAG, "addOverlay");
		m_overlays.add(overlay);
		populate();
	}

	public void delOverlay(int pindex) {
		Log.d(TAG, "delOverlay");

		if (m_overlays.size() > 0) {
			m_overlays.remove(pindex);
		}

	}

	@Override
	protected CustomOverlayItem createItem(int i) {
		Log.d(TAG, "createItem");
		mIndex = i;
		return m_overlays.get(i);
	}

	@Override
	public int size() {
		// Log.d("TAG","size");
		return m_overlays.size();
	}

	@Override
	protected boolean onBalloonTap(int index, CustomOverlayItem item) {
		Log.d(TAG, "onBalloonTap");
		if (!mImg_flg) {
			Log.d("CustomItemizedOverlay", "index ----->" + index);

			// バルーンをタップしたとき、何か処理をさせたい場合はここに追記
			Log.d(TAG, "item's title:" + item.getTitle());
			int id = item.getPositionid();
			Log.d(TAG, "id ---->" + id);

			ContentResolver resolver = c.getContentResolver();

			Cursor cursor = resolver.query(Data.CONTENT_URI, new String[] { Data.CONTACT_ID },
					Data.DISPLAY_NAME + " = ?", new String[] { item.getTitle() }, null);

			cursor.moveToFirst();
			int cId = cursor.getInt(0);
			Log.i("HOGE", "CONTACT_ID = " + cId);

			Uri contactUri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI,
					(long) cId);

			Log.d(TAG, "真・でんわID --->" + contactUri);
			// 電話帳にジャンプ！

			Intent cti = new Intent(Intent.ACTION_VIEW, contactUri);
			c.startActivity(cti);
		}
		return true;
	}

	@Override
	protected BalloonOverlayView<CustomOverlayItem> createBalloonOverlayView() {
		Log.d(TAG, "createBalloonOverlayView");
		Log.d("createBalloonOverlayView", Boolean.toString(mImg_flg));
		if (mImg_flg) {
			// use our custom balloon view with our custom overlay item type:
			return new BalloonOverlayView<CustomOverlayItem>(getMapView().getContext(),
					getBalloonBottomOffset());
		} else {
			// use our custom balloon view with our custom overlay item type:
			return new CustomBalloonOverlayView<CustomOverlayItem>(getMapView().getContext(),
					getBalloonBottomOffset(), mIndex);
		}
	}
}
