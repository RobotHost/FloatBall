package com.sp.floatball.simple;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.sp.floatball.FlyingBall;
import com.sp.floatball.FlyingBallImp;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FlyingBall.getInstance().init(MainActivity.this,
                R.mipmap.full,
                new int[]{R.mipmap.menu_01, R.mipmap.menu_02, R.mipmap.menu_03},
                0.33f,
                new FlyingBallImp.FlyingBallCallback() {
                    @Override
                    public void onBtnClick(int btnCode, View v) {
                        Toast.makeText(MainActivity.this, "btnCode=" + btnCode, Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onError(String msg) {

                    }
                });

        ((Button) findViewById(R.id.test_btn)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FlyingBall.getInstance().displayFull();
            }
        });
    }
}
