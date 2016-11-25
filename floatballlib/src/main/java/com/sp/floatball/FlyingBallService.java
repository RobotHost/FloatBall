package com.sp.floatball;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by songyuan on 2016/11/1.
 */

public class FlyingBallService extends Service {
    private FlyingBallView flyingBallView = null;

    @Override
    public void onCreate() {
        super.onCreate();

    }

    @Override
    public IBinder onBind(Intent intent) {
        return new FlyingBallServiceBinder();
    }

    public class FlyingBallServiceBinder extends Binder {
        FlyingBallService getService() {
            return FlyingBallService.this;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        destroy();
    }


    public void init(int logoIconRId, @Nullable int[] menuIconRIds, @Nullable Float smallScale, FlyingBallImp.FlyingBallCallback flyingBallCallback){
        if (flyingBallView == null) {
            flyingBallView = new FlyingBallView(this,logoIconRId, menuIconRIds, smallScale, flyingBallCallback);
        }
    }


    /**
     * @param logoIconRId 浮标icon资源 id
     */
    public void setLogoIconRId(int logoIconRId) {
        if (flyingBallView != null) {

        }
    }

    /**
     * @param menuIconRIds 展开目录中的icon资源id
     */
    public void setMenuIconRIds(int[] menuIconRIds) {
        if (flyingBallView != null) {

        }
    }

    /**
     * @param smallScale 浮标缩小时时相对于完整状态下宽度的比例（0.1-1）。建议0.3-0.5。默认0.33。
     */
    public void setSmallScale(float smallScale) {
        if (flyingBallView != null) {

        }
    }

    /**
     * 完全显示
     */
    public void displayFull() {
        if (flyingBallView != null) {
            flyingBallView.displayFull();
        }
    }

    /**
     * 缩小显示
     */
    public void displaySmall() {
        if (flyingBallView != null) {
            flyingBallView.displaySmall();
        }
    }

    /**
     * 消失完全隐藏
     */
    public void disappear() {
        if (flyingBallView != null) {
            flyingBallView.disappear();
        }

    }

    /**
     * 销毁整个浮标，当app onDestroy时调用
     */
    public void destroy() {
        if (flyingBallView != null) {
            flyingBallView.destroy();
            flyingBallView = null;
        }
    }

    /**
     * 是否已消失完全隐藏
     *
     * @return true or false
     */
    public boolean isDisappear() {
        if (flyingBallView != null) {
            return flyingBallView.isDisappear();
        }
        return true;
    }
}