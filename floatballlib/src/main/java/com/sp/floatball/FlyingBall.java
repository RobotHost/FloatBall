package com.sp.floatball;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.annotation.Nullable;

/**
 * Created by songyuan on 2016/11/1.
 */

public class FlyingBall {
    private static FlyingBall flyingBall = null;
    private FlyingBallService flyingBallService = null;
    private Context mContext = null;
    private Intent intent = null;
    private FlyingBallImp.FlyingBallCallback mFlyingBallCallback = null;
    private int mLogoIconRId;
    private int[] mMenuIconRIds = null;
    private Float mSmallScale = null;

    private FlyingBall() {

    }

    public static FlyingBall getInstance() {
        if (flyingBall == null) {
            synchronized (FlyingBall.class) {
                flyingBall = new FlyingBall();
            }
        }

        return flyingBall;
    }

    /**
     * 初始化，在app onCreate时
     *
     * @param context
     * @param flyingBallCallback
     * @param logoIconRId        浮标icon资源 id
     * @param menuIconRIds       展开目录中的icon资源id。null指无可展开菜单项。
     * @param smallScale         浮标缩小时时相对于完整状态下宽度的比例（0.1-1）。建议0.3-0.5。null时，默认为0.33。
     */
    public void init(Context context, int logoIconRId, @Nullable int[] menuIconRIds, @Nullable Float smallScale, FlyingBallImp.FlyingBallCallback flyingBallCallback) {
        mContext = context;
        mLogoIconRId = logoIconRId;
        mMenuIconRIds = menuIconRIds;
        mSmallScale = smallScale;
        mFlyingBallCallback = flyingBallCallback;
        if (flyingBallService == null) {
            synchronized (FlyingBall.class) {
                intent = new Intent(mContext, FlyingBallService.class);
                mContext.startService(intent);
                mContext.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            }
        }
    }

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            flyingBallService = ((FlyingBallService.FlyingBallServiceBinder) service).getService();
            flyingBallService.init(mLogoIconRId, mMenuIconRIds, mSmallScale, mFlyingBallCallback);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            flyingBallService = null;
        }
    };

    /**
     * @param logoIconRId 浮标icon资源 id
     */
    public FlyingBall setLogoIconRId(int logoIconRId) {
        if (flyingBallService != null) {
            flyingBallService.setLogoIconRId(logoIconRId);
        }
        return flyingBall;
    }

    /**
     * @param menuIconRIds 展开目录中的icon资源id
     */
    public FlyingBall setMenuIconRIds(int[] menuIconRIds) {
        if (flyingBallService != null) {
            flyingBallService.setMenuIconRIds(menuIconRIds);
        }
        return flyingBall;
    }

    /**
     * @param smallScale 浮标缩小时时相对于完整状态下宽度的比例（0.1-1）。建议0.3-0.5。
     */
    public FlyingBall setSmallScale(float smallScale) {
        if (flyingBallService != null) {
            flyingBallService.setSmallScale(smallScale);
        }
        return flyingBall;
    }

    /**
     * 完全显示
     */
    public void displayFull() {
        if (flyingBallService != null) {
            flyingBallService.displayFull();
        }
    }

    /**
     * 缩小显示
     */
    public void displaySmall() {
        if (flyingBallService != null) {
            flyingBallService.displaySmall();
        }
    }

    /**
     * 消失完全隐藏
     */
    public void disappear() {
        if (flyingBallService != null) {
            flyingBallService.disappear();
        }

    }

    /**
     * 销毁整个浮标，当app onDestroy时调用
     */
    public void destroy() {
        if (flyingBallService != null) {
            flyingBallService.destroy();
            mContext.unbindService(serviceConnection);
            mContext.stopService(intent);
            flyingBall = null;
        }
    }

    /**
     * 是否已消失完全隐藏
     *
     * @return true or false
     */
    public boolean isDisappear() {
        if (flyingBallService != null) {
            return flyingBallService.isDisappear();
        }
        return true;
    }


}
