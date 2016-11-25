package com.sp.floatball;

import android.view.View;

/**
 * Created by songyuan on 2016/11/2.
 */

public class FlyingBallImp {
	public static final int MAIN_BALL_CLICK = 1;// 当前点击的是ball

	public interface FlyingBallCallback {
		void onBtnClick(int btnCode, View v);

		void onError(String msg);
	}
}
