/*
 * ラインオーバーレイ.
 * MapViewにラインを表示するオーバーレイクラス
 *
 * o-ta
 *
 */
package com.tumiki0ituki.visitcare;

import java.util.List;
import java.util.ListIterator;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.Log;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

/**
 * ラインオーバーレイクラス.
 */
public class RouteLineOverlay extends Overlay {

	/** ログタグ. */
	private static final String TAG = RouteLineOverlay.class.getName();

	/** 訪問先リスト. */
	private List<Schedule> mScheduleList;

	/**
	 * コンストラクタ.
	 *
	 * @param pScheduleList 描画対象の訪問先リスト
	 */
	public RouteLineOverlay(List<Schedule> pScheduleList) {
		Log.d(TAG, "Constructor");
		this.mScheduleList = pScheduleList;
	}

	/*
	 * (非 Javadoc)
	 * @see com.google.android.maps.Overlay#draw(android.graphics.Canvas,
	 * com.google.android.maps.MapView, boolean)
	 */
	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, shadow);
		Log.d(TAG, "draw START shadow=" + Boolean.toString(shadow));
		if (!shadow) {
			/*
			 * Paint設定
			 * .setAntialias	アンチエリアス
			 * .setStrokeWidth	太さ 			7px
			 * .setStrokeCap	線の角 		ROUND=丸
			 * .setStrokeJoin	線のつなぎ目 	ROUND=丸
			 * .setColor		線の色
			 */
			Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
			paint.setAntiAlias(true);
			paint.setStrokeWidth(7);
			paint.setStrokeCap(Paint.Cap.ROUND);
			paint.setStrokeJoin(Paint.Join.ROUND);
			paint.setColor(Color.argb(150, 0, 120, 80));

			/* GeoPoint to Pixels 変換クラス */
			Projection projection = mapView.getProjection();

			/* [デバッグ]ループカウンター */
			int loopcount = 0;
			/* 描画始点,描画終点 */
			GeoPoint geoStart = null;
			GeoPoint geoEnd = null;
			for (ListIterator<Schedule> it = (ListIterator<Schedule>) mScheduleList
					.iterator(); it.hasNext();) {
				loopcount++;
				Log.d(TAG, "RouteListCount=" + Integer.toString(loopcount));
				Schedule route = it.next();
				geoStart = new GeoPoint(
						new Double(route.getlatitude() * 1E6).intValue(),
						new Double(route.getlongitude() * 1E6).intValue());
				Log.d(TAG, "geoStart:" + geoStart.toString());
				if (geoEnd != null) {
					Log.d(TAG, "geoEnd:" + geoEnd.toString());
					/* Map座標をCanvas用ピクセル座標へ変換 */
					Point pxStart = projection.toPixels(geoStart, null);
					Point pxEnd = projection.toPixels(geoEnd, null);
					/* ライン描画 */
					canvas.drawLine(pxStart.x, pxStart.y, pxEnd.x, pxEnd.y,
							paint);
				}
				if (it.hasNext()) {
					geoEnd = geoStart;
				}
			}
		}
		Log.d(TAG, "draw END");
	}

}
