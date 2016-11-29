package com.sp.floatball;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by songyuan on 2016/11/1.
 */
public class FlyingBallView extends FrameLayout implements OnTouchListener {
    private Context mContext;
    private int mLogoPngRId;
    private int[] mMenuIconRIds = null;
    private Float mSmallScale = null;
    private FlyingBallImp.FlyingBallCallback mFlyingBallCallback = null;
    private Bitmap fullBitmap;
    private Bitmap rightBitmap;
    private Bitmap leftBitmap;
    private WindowManager.LayoutParams wlp;
    private WindowManager wm;
    private int screenWidth;
    private int screenHeight;
    private ImageView ballIv;
    private LinearLayout menuLl;
    private RootLinearLayout rootView;
    private boolean isRightSide = false;
    private boolean isLastSideRight = false;
    private boolean isOnlyFullBall = false;
    private boolean isOnlySmall = false;
    private boolean isMoved = false;
    private boolean isHasMenu = false;
    private boolean isMenuShowed = false;
    private float touchStartX;
    private float touchStartY;
    private Timer timer;
    private TimerTask timerTask;
    private final static int TO_SMALL_FLYING_BALL = 1;
    private static int BALL_FULL_WIDTH;// px
    private static int BALL_FULL_HEIGHT;// px
    private static int BALL_HALF_WIDTH;// px
    private static int ROOT_VIEW_ADAPTIVE_DIP;// px
    private static int MENU_BTN_WIDTH_HEIGHT;// px
    private static int MENU_BTN_MARGIN;// px
    private final static float FULL_ALPHA = 1f;
    private final static float HALF_ALPHA = 0.7f;
    private long clickBallLastTime = 0l;
    private final static long CLICK_BALL_TIME_INTERVAL = 1000l;
    //如果为true，当浮标在右边时，主浮标图标和菜单栏的位置将左右调换，使得主浮标靠边框。
    // （但有一些视觉的bug：展开和缩回不美观，bug原因可能是WindowManager的gravity或width造成的，目前没有找到合理解决方案）
    private boolean isCanResetRootView = false;

    @SuppressLint("NewApi")
    public FlyingBallView(Context context, int logoIconRId, @Nullable int[] menuIconRIds, @Nullable Float smallScale, FlyingBallImp.FlyingBallCallback flyingBallCallback) {
        super(context);
        this.mContext = context;
        this.mLogoPngRId = logoIconRId;
        this.mMenuIconRIds = menuIconRIds;
        if (smallScale == null) {
            this.mSmallScale = 0.33f;
        } else {
            this.mSmallScale = smallScale;
        }
        mFlyingBallCallback = flyingBallCallback;
        BALL_FULL_WIDTH = dip2Px(context, 48);
        BALL_FULL_HEIGHT = dip2Px(context, 48);
        BALL_HALF_WIDTH = (int) (BALL_FULL_WIDTH * smallScale);
        ROOT_VIEW_ADAPTIVE_DIP = dip2Px(context, 2);
        MENU_BTN_WIDTH_HEIGHT = dip2Px(context, 36);
        MENU_BTN_MARGIN = dip2Px(context, 4);
        resToBitmap();
        createWM();
        createViews();
        this.addView(rootView);
        wm.addView(this, wlp);

        timer = new Timer();
    }

    private void createWM() {
        wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        this.wlp = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            wlp.type = WindowManager.LayoutParams.TYPE_TOAST;
        } else {
            wlp.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        wlp.format = PixelFormat.RGBA_8888;
        wlp.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        wlp.gravity = Gravity.LEFT | Gravity.TOP;
        wlp.x = 0;
        wlp.y = screenHeight / 2;
        wlp.width = WindowManager.LayoutParams.WRAP_CONTENT;
        wlp.height = WindowManager.LayoutParams.WRAP_CONTENT;
    }


    private void createViews() {
        if (rootView == null) {
            rootView = new RootLinearLayout(mContext);
            rootView.setOrientation(LinearLayout.HORIZONTAL);
            rootView.setLayoutParams(new ViewGroup.LayoutParams(BALL_FULL_WIDTH + ROOT_VIEW_ADAPTIVE_DIP,
                    BALL_FULL_HEIGHT + ROOT_VIEW_ADAPTIVE_DIP));
        }

        if (ballIv == null) {
            ballIv = new ImageView(mContext);
            ballIv.setLayoutParams(new ViewGroup.LayoutParams(BALL_FULL_WIDTH, BALL_FULL_HEIGHT));
            ballIv.setScaleType(ImageView.ScaleType.FIT_XY);//必须ScaleType.FIT_XY
            ballIv.setClickable(true);
            ballIv.setOnTouchListener(this);
        }

        if (mMenuIconRIds != null && mMenuIconRIds.length > 0) {
            isHasMenu = true;
            if (menuLl == null) {
                menuLl = new LinearLayout(mContext);
                LinearLayout.LayoutParams menuLlLp = new LinearLayout.LayoutParams((MENU_BTN_WIDTH_HEIGHT + MENU_BTN_MARGIN)
                        * mMenuIconRIds.length,
                        BALL_FULL_HEIGHT);
                menuLl.setLayoutParams(menuLlLp);
                menuLl.setGravity(Gravity.CENTER_VERTICAL);
                menuLl.setOrientation(LinearLayout.HORIZONTAL);
                for (int i = 0; i < mMenuIconRIds.length; i++) {
                    ImageView menuItemIv = new ImageView(mContext);
                    LinearLayout.LayoutParams menuItemIvLp = new LinearLayout.LayoutParams(MENU_BTN_WIDTH_HEIGHT, MENU_BTN_WIDTH_HEIGHT);
                    menuItemIvLp.leftMargin = MENU_BTN_MARGIN / 2;
                    menuItemIvLp.rightMargin = MENU_BTN_MARGIN / 2;
                    menuItemIv.setLayoutParams(menuItemIvLp);
                    menuItemIv.setScaleType(ImageView.ScaleType.FIT_XY);//必须ScaleType.FIT_XY
                    menuItemIv.setClickable(true);
                    menuItemIv.setTag(i + 1);
                    menuItemIv.setImageResource(mMenuIconRIds[i]);
                    menuItemIv.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mFlyingBallCallback.onBtnClick((int) v.getTag(), v);
                        }
                    });
                    menuLl.addView(menuItemIv);
                }
            }
        }

        refreshRootViewGravity();

        if (isHasMenu) {
            rootView.addView(ballIv);
            rootView.addView(menuLl);
            menuLl.setVisibility(View.GONE);
        } else {
            rootView.addView(ballIv);
        }
        setVisibility(View.GONE);
    }


    public void setLogoIconRId(int logoIconRId) {
        mLogoPngRId = logoIconRId;
        resToBitmap();
        displayFull();
    }

    public void setMenuIconRIds(int[] menuIconRIds) {
        if (mMenuIconRIds != null && menuIconRIds.length == mMenuIconRIds.length) {
            mMenuIconRIds = menuIconRIds;
            for (int i = 0; i < menuLl.getChildCount(); i++) {
                ImageView iv = (ImageView) menuLl.getChildAt(i);
                iv.setImageResource(mMenuIconRIds[i]);
            }

        }
    }

    public void setSmallScale(float smallScale) {
        mSmallScale = smallScale;
        resToBitmap();
        displaySmall();
    }


    public void displayFull() {
        setVisibility(View.VISIBLE);
        resetRootViewStatus(false, true, false);
        refreshBall();
        startTimerTask();
    }

    public void displaySmall() {
        resetRootViewStatus(true, false, false);
        Message message = mTimerHandler.obtainMessage();
        message.what = TO_SMALL_FLYING_BALL;
        mTimerHandler.sendMessage(message);
        stopTimerTask();
    }

    public void disappear() {
        setVisibility(View.GONE);
        stopTimerTask();
    }

    public boolean isDisappear() {
        if (this.getVisibility() == View.GONE) {
            return true;
        }
        return false;
    }

    public void destroy() {
        removeRootView();
        stopTimerTask();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }


    private void startTimerTask() {
        if (timerTask != null) {
            try {
                timerTask.cancel();
                timerTask = null;
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        timerTask = new TimerTask() {
            @Override
            public void run() {
                Message message = mTimerHandler.obtainMessage();
                message.what = TO_SMALL_FLYING_BALL;
                mTimerHandler.sendMessage(message);
            }
        };
        if (isOnlyFullBall) {
            timer.schedule(timerTask, 5000, 3000);
        }
    }

    private void stopTimerTask() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    private void removeRootView() {
        try {
            wm.removeView(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private final Handler mTimerHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == TO_SMALL_FLYING_BALL) {
                if (isOnlyFullBall) {
                    resetRootViewStatus(true, false, false);
                    refreshBall();
                }
            }
            super.handleMessage(msg);
        }
    };


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        stopTimerTask();
        int xNow = (int) event.getRawX();
        int yNow = (int) event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStartX = event.getX();
                touchStartY = event.getY();
                isMoved = false;

                break;
            case MotionEvent.ACTION_MOVE:
                if (isOnlyFullBall) {
                    float mMoveStartX = event.getX();
                    float mMoveStartY = event.getY();
                    if (Math.abs(touchStartX - mMoveStartX) > 2 && Math.abs(touchStartY - mMoveStartY) > 2) {
                        isMoved = true;
                        wlp.x = (int) (xNow - touchStartX);
                        wlp.y = (int) (yNow - touchStartY);
                        wm.updateViewLayout(this, wlp);
                        return false;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!isMoved) {
                    if (isOnlySmall) {
                        resetRootViewStatus(false, true, false);
                    } else if (isOnlyFullBall) {
                        resetRootViewStatus(false, false, true);
                    } else if (isMenuShowed) {
                        resetRootViewStatus(false, true, false);
                    }
                }

                if (wlp.x >= screenWidth / 2) {
                    wlp.x = screenWidth;
                    isRightSide = true;
                } else if (wlp.x < screenWidth / 2) {
                    wlp.x = 0;
                    isRightSide = false;
                }
                wm.updateViewLayout(this, wlp);

                if ((isOnlyFullBall || isMenuShowed) && !isMoved) {
                    if (!isHasMenu) {
                        if (System.currentTimeMillis() - clickBallLastTime > CLICK_BALL_TIME_INTERVAL) {
                            mFlyingBallCallback.onBtnClick(FlyingBallImp.MAIN_BALL_CLICK, v);
                            clickBallLastTime = System.currentTimeMillis();
                        }
                    } else {
                        if (isMenuShowed) {
                            menuLl.setVisibility(View.VISIBLE);
                            refreshRootViewSize();
                        } else {
                            menuLl.setVisibility(View.GONE);
                            refreshRootViewSize();
                        }

                    }
                }

                if (isOnlyFullBall && isMoved) {
                    //当切换左右位置时
                    if (isRightSide != isLastSideRight) {
                        if (isCanResetRootView) {
                            refreshRootViewChildOnHasMenu();
                        }
                        refreshRootViewGravity();
                        isLastSideRight = isRightSide;
                    }
                }
                refreshBall();
                if (isOnlyFullBall) {
                    startTimerTask();
                }

                // 重置
                touchStartX = touchStartY = 0;
                break;
        }
        return false;
    }

    private void resetRootViewStatus(boolean isOnlySmall, boolean isOnlyFullBall, boolean isMenuShowed) {
        this.isOnlySmall = isOnlySmall;
        this.isOnlyFullBall = isOnlyFullBall;
        this.isMenuShowed = isMenuShowed;
    }

    private void refreshRootViewChildOnHasMenu() {
        if (isHasMenu) {
            if (isRightSide) {
                rootView.removeView(ballIv);
            } else {
                rootView.removeView(menuLl);
            }
        }
    }

    private void refreshBall() {
        ViewGroup.LayoutParams ballIvLayoutParams = ballIv.getLayoutParams();
        if (isOnlySmall) {
            if (isRightSide) {
                ballIv.setImageBitmap(rightBitmap);
            } else {
                ballIv.setImageBitmap(leftBitmap);
            }
            ballIvLayoutParams.width = BALL_HALF_WIDTH;
            ballIvLayoutParams.height = BALL_FULL_HEIGHT;
            ballIv.setLayoutParams(ballIvLayoutParams);
            wlp.alpha = HALF_ALPHA;
            wm.updateViewLayout(FlyingBallView.this, wlp);
        } else if (isOnlyFullBall || isMenuShowed) {
            ballIvLayoutParams.width = BALL_FULL_WIDTH;
            ballIvLayoutParams.height = BALL_FULL_HEIGHT;
            ballIv.setLayoutParams(ballIvLayoutParams);
            ballIv.setImageBitmap(fullBitmap);
            wlp.alpha = FULL_ALPHA;
            wm.updateViewLayout(this, wlp);
        }
    }

    private void refreshRootViewSize() {
        if (isHasMenu) {
            FrameLayout.LayoutParams paramsFlFloat = (FrameLayout.LayoutParams) rootView.getLayoutParams();
            if (isMenuShowed) {
                paramsFlFloat.width = BALL_FULL_WIDTH + (MENU_BTN_WIDTH_HEIGHT + MENU_BTN_MARGIN) * mMenuIconRIds.length + ROOT_VIEW_ADAPTIVE_DIP;
            } else {
                paramsFlFloat.width = BALL_FULL_WIDTH + ROOT_VIEW_ADAPTIVE_DIP;
            }
            rootView.setLayoutParams(paramsFlFloat);
        }
    }


    private void refreshRootViewGravity() {
        if (isRightSide) {
            rootView.setGravity(Gravity.RIGHT);
        } else {
            rootView.setGravity(Gravity.LEFT);
        }
    }

    @SuppressLint("NewApi")
    @Override
    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        screenWidth = dm.widthPixels;
        screenHeight = dm.heightPixels;
        int oldX = wlp.x;
        int oldY = wlp.y;
        switch (newConfig.orientation) {
            case Configuration.ORIENTATION_LANDSCAPE:// 横屏
                if (isRightSide) {
                    wlp.x = screenWidth;
                    wlp.y = oldY;
                } else {
                    wlp.x = oldX;
                    wlp.y = oldY;
                }
                break;
            case Configuration.ORIENTATION_PORTRAIT:// 竖屏
                if (isRightSide) {
                    wlp.x = screenWidth;
                    wlp.y = oldY;
                } else {
                    wlp.x = oldX;
                    wlp.y = oldY;
                }
                break;
        }
        wm.updateViewLayout(this, wlp);
    }

    /**
     * 按比例生成新的full small icon drawable
     */
    private void resToBitmap() {
        fullBitmap = BitmapFactory.decodeResource(mContext.getResources(), mLogoPngRId);
        int fullWidthPx = fullBitmap.getWidth();
        int fullHeightPx = fullBitmap.getHeight();
        int halfWidthPx = (int) (fullWidthPx * mSmallScale);
        rightBitmap = Bitmap.createBitmap(fullBitmap, 0, 0, halfWidthPx, fullHeightPx);
        leftBitmap = Bitmap.createBitmap(fullBitmap, fullWidthPx - halfWidthPx, 0, halfWidthPx, fullHeightPx);
    }

    private int dip2Px(Context context, float dip) {
        float density = context.getResources().getDisplayMetrics().density;
        return (int) (dip * density + 0.5f);
    }

    class RootLinearLayout extends LinearLayout {

        public RootLinearLayout(Context context) {
            super(context);
        }

        @Override
        public void onViewRemoved(View child) {
            super.onViewRemoved(child);
            if (child == ballIv) {
                rootView.addView(ballIv);
            } else if (child == menuLl) {
                rootView.addView(menuLl);
            }

            menuLl.setVisibility(View.GONE);
            isMenuShowed = false;
        }
    }

}
