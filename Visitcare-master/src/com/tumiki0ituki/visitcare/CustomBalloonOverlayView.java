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
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.maps.OverlayItem;

public class CustomBalloonOverlayView<Item extends OverlayItem> extends
		BalloonOverlayView<CustomOverlayItem> {

	private TextView title;
	private TextView snippet;
	private ImageView image;
	private static final String TAG = CustomBalloonOverlayView.class.getName();


	public CustomBalloonOverlayView(Context context, int balloonBottomOffset,
			int index) {
		super(context, balloonBottomOffset);
		Log.d("TAG", "CustomBalloonOverlayView");
	}

	@Override
	protected void setupView(Context context, final ViewGroup parent) {
		Log.d(TAG, "setupView");

		// inflate our custom layout into parent
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.custom_balloon_overlay, parent);

		// setup our fields
		title = (TextView) v.findViewById(R.id.balloon_item_title);
		snippet = (TextView) v.findViewById(R.id.balloon_item_snippet);
		image = (ImageView) v.findViewById(R.id.balloon_item_image);

		// implement balloon close
		ImageView close = (ImageView) v.findViewById(R.id.balloon_close);
		close.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				parent.setVisibility(GONE);
			}
		});

	}

	@Override
	protected void setBalloonData(CustomOverlayItem item, ViewGroup parent) {
		Log.d(TAG, "setBalloonData");
		// map our custom item data to fields
		title.setText(item.getTitle());
		snippet.setText(item.getSnippet());

		image.setImageResource(item.getImageid());

	}

}
