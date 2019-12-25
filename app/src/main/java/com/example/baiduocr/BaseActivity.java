package com.example.baiduocr;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.example.baiduocr.utils.ActivityCollector;
import com.example.baiduocr.utils.LogUtil;
import com.gyf.immersionbar.BarHide;
import com.gyf.immersionbar.ImmersionBar;

/**
 * Created by xxb on 2016/11/27.
 * <p>
 * baseActivity
 */

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initImmersionBar();
        LogUtil.e(this.getClass().getName());
        // 添加Activity到堆
        ActivityCollector.addActivity(this);
    }

    /**
     * 初始化沉浸式
     * Init immersion bar.
     */
    protected void initImmersionBar() {
        //设置共同沉浸式样式
        ImmersionBar immersionBar = ImmersionBar.with(this)
                .navigationBarColor(R.color.white)
                .navigationBarDarkIcon(true);

        if (translucentStatusBar()) {
            immersionBar.fitsSystemWindows(false);
        } else {
            if (isFullScreen()) {
                immersionBar
                        .hideBar(BarHide.FLAG_HIDE_STATUS_BAR)
                        .keyboardEnable(true);
                if (!ImmersionBar.hasNavigationBar(this)) {
                    immersionBar.fullScreen(true);
                }
            } else {
                immersionBar
                        .statusBarColor(setStatusBarColor())
                        .fitsSystemWindows(true);
                if (setStatusBarColor() == R.color.white) {
                    immersionBar.statusBarDarkFont(true);
                } else {
                    immersionBar.statusBarDarkFont(false);
                }
            }
        }
        immersionBar.init();
    }

    protected boolean isFullScreen() {
        return false;
    }

    /**
     * 子类可以重写改变状态栏颜色
     */
    protected int setStatusBarColor() {
        return R.color.white;
    }

    protected boolean translucentStatusBar() {
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ImmersionBar.with(this).reset();
        ActivityCollector.removeActivity(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideKeyboard(v, ev)) {
                hideKeyboard(v.getWindowToken());
            }
        }
        try{
            return super.dispatchTouchEvent(ev);
        }catch (Exception e){
            return true;
        }finally {
            return true;
        }
    }

    /**
     * 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘，因为当用户点击EditText时则不能隐藏
     *
     * @param v
     * @param event
     * @return
     */
    private boolean isShouldHideKeyboard(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击EditText的事件，忽略它。
                return false;
            } else {
                return true;
            }
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditText上，和用户用轨迹球选择其他的焦点
        return false;
    }

    /**
     * 获取InputMethodManager，隐藏软键盘
     *
     * @param token
     */
    private void hideKeyboard(IBinder token) {
        if (token != null) {
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    public void tipDialog(Context context, String messageStr, String buttonStr, int title_icon) {
        if (context != null) {
            if (!((Activity) context).isFinishing()) {
                new MaterialDialog.Builder(context)
                        .title("提示")
                        .content(messageStr)
                        .iconRes(title_icon)
                        .positiveText(buttonStr)
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                dialog.dismiss();
                            }
                        })
                        .show();
            }
        }
    }

}

