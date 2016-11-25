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
 * Desction:悬浮窗 Author:pengjianbo Date:15/10/26 下午8:39
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
    private boolean isRightSide;
    private boolean isLastSideRight;
    private boolean isOnlyFullBall;
    private boolean isOnlySmall;
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
        BALL_FULL_WIDTH = dip2Px(context, 48);
        BALL_FULL_HEIGHT = dip2Px(context, 48);
        BALL_HALF_WIDTH = (int) (BALL_FULL_WIDTH * smallScale);
        ROOT_VIEW_ADAPTIVE_DIP = dip2Px(context, 2);
        MENU_BTN_WIDTH_HEIGHT = dip2Px(context, 36);
        MENU_BTN_MARGIN = dip2Px(context, 4);
        resToBitmap();
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
        screenHeight = wm.getDefaultDisplay().getHeight();
        wlp.x = 0;
        wlp.y = screenHeight / 2;
        wlp.width = LayoutParams.WRAP_CONTENT;
        wlp.height = LayoutParams.WRAP_CONTENT;
        createViews();
        setVisibility(View.GONE);
        timer = new Timer();

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
            isHasMenu = true;
        }

        if (isHasMenu) {
            rootView.addView(ballIv);
            rootView.addView(menuLl);
            menuLl.setVisibility(View.GONE);
        } else {
            rootView.addView(ballIv);
        }
        this.addView(rootView);
        wm.addView(this, wlp);
    }


    private void refreshViewOnHasMenu() {
        //包含可展开的menu
        if (isHasMenu) {
            if (isRightSide) {
                rootView.removeView(ballIv);
            } else {
                rootView.removeView(menuLl);
            }
        }
    }


    public void setLogoIconRId(int logoIconRId) {
        mLogoPngRId = logoIconRId;
        resToBitmap();
        displayFull();
    }

    public void setMenuIconRIds(int[] menuIconRIds) {
        mMenuIconRIds = menuIconRIds;
    }

    public void setSmallScale(float smallScale) {
        mSmallScale = smallScale;
        resToBitmap();
        displaySmall();
    }


    public void displayFull() {
        setVisibility(View.VISIBLE);
        isOnlySmall = false;
        isOnlyFullBall = true;
        isMenuShowed = false;
        refreshBallSize();
        ballIv.setImageBitmap(fullBitmap);
        wlp.alpha = 1f;
        wm.updateViewLayout(this, wlp);
        startTimerTask();
    }

    public void displaySmall() {
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
        removeFloatView();
        stopTimerTask();
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }


    private void startTimerTask() {
        isOnlyFullBall = true;
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

    private final Handler mTimerHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == TO_SMALL_FLYING_BALL) {
                if (isOnlyFullBall) {
                    isOnlyFullBall = false;
                    isOnlySmall = true;
                    isMenuShowed = false;
                    refreshBallSize();
                    if (isRightSide) {
                        ballIv.setImageBitmap(rightBitmap);
                    } else {
                        ballIv.setImageBitmap(leftBitmap);
                    }

                    wlp.alpha = 0.7f;
                    wm.updateViewLayout(FlyingBallView.this, wlp);
                    refreshRootViewGravity();
                }
            }
            super.handleMessage(msg);
        }
    };

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

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        stopTimerTask();
        int x = (int) event.getRawX();
        int y = (int) event.getRawY();
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                touchStartX = event.getX();
                touchStartY = event.getY();
                refreshBallSize();
                ballIv.setImageBitmap(fullBitmap);
                wlp.alpha = 1f;
                wm.updateViewLayout(this, wlp);
                isMoved = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (!isMenuShowed) {
                    float mMoveStartX = event.getX();
                    float mMoveStartY = event.getY();
                    if (Math.abs(touchStartX - mMoveStartX) > 2 && Math.abs(touchStartY - mMoveStartY) > 2) {
                        isMoved = true;
                        wlp.x = (int) (x - touchStartX);
                        wlp.y = (int) (y - touchStartY);
                        wm.updateViewLayout(this, wlp);
                        return false;
                    }
                }
                break;
            case MotionEvent.ACTION_UP:
                if (wlp.x >= screenWidth / 2) {
                    wlp.x = screenWidth;
                    isRightSide = true;
                } else if (wlp.x < screenWidth / 2) {
                    isRightSide = false;
                    wlp.x = 0;
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
                            isMenuShowed = false;
                            isOnlyFullBall = true;
                            refreshRootViewSize();
                            menuLl.setVisibility(View.GONE);
                            startTimerTask();
                        } else {
                            isMenuShowed = true;
                            isOnlyFullBall = false;
                            refreshRootViewSize();
                            menuLl.setVisibility(View.VISIBLE);
                            stopTimerTask();
                        }
                    }
                }

                if (isRightSide != isLastSideRight) {
                    refreshRootViewGravity();
                    if (isOnlyFullBall && isMoved) {
                        refreshViewOnHasMenu();
                    }
                    isLastSideRight = isRightSide;
                }
                if (isOnlySmall || isMoved) {
                    isOnlySmall = false;
                    startTimerTask();
                }
                refreshBallSize();
                ballIv.setImageBitmap(fullBitmap);

                // 重置
                touchStartX = touchStartY = 0;
                break;
        }
        return false;
    }

    private void stopTimerTask() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
    }

    private void removeFloatView() {
        try {
            wm.removeView(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void refreshBallSize() {
        ViewGroup.LayoutParams ballIvLayoutParams = ballIv.getLayoutParams();
        if (isOnlySmall) {
            ballIvLayoutParams.width = BALL_HALF_WIDTH;
            ballIvLayoutParams.height = BALL_FULL_HEIGHT;
        } else {
            ballIvLayoutParams.width = BALL_FULL_WIDTH;
            ballIvLayoutParams.height = BALL_FULL_HEIGHT;
        }

        ballIv.setLayoutParams(ballIvLayoutParams);
    }

    private void refreshRootViewSize() {
        FrameLayout.LayoutParams paramsFlFloat = (FrameLayout.LayoutParams) rootView.getLayoutParams();
        if (isMenuShowed) {
            paramsFlFloat.width = BALL_FULL_WIDTH + (MENU_BTN_WIDTH_HEIGHT + MENU_BTN_MARGIN) * mMenuIconRIds.length + ROOT_VIEW_ADAPTIVE_DIP;
        } else {
            paramsFlFloat.width = BALL_FULL_WIDTH + ROOT_VIEW_ADAPTIVE_DIP;
        }
        rootView.setLayoutParams(paramsFlFloat);
    }


    private void refreshRootViewGravity() {
        FrameLayout.LayoutParams rootViewLP = (FrameLayout.LayoutParams) rootView.getLayoutParams();
        if (isRightSide) {
            rootViewLP.gravity = Gravity.RIGHT;
            rootView.setGravity(Gravity.RIGHT);
        } else {
            rootViewLP.gravity = Gravity.LEFT;
            rootView.setGravity(Gravity.LEFT);
        }
        rootView.setLayoutParams(rootViewLP);
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
