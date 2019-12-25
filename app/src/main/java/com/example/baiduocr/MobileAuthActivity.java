package com.example.baiduocr;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.baiduocr.utils.ToastUtil;
import com.example.baiduocr.utils.Utils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * author: zdj
 * created on: 2019/11/12
 * description:  手机号认证
 */
public class MobileAuthActivity extends BaseActivity {

    @BindView(R.id.iv_back)
    ImageView ivBack;
    @BindView(R.id.ll_back)
    LinearLayout llBack;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.iv_right)
    ImageView ivRight;
    @BindView(R.id.tv_right)
    TextView tvRight;
    @BindView(R.id.ll_right)
    LinearLayout llRight;
    @BindView(R.id.et_ec_phone)
    EditText etEcPhone;
    @BindView(R.id.sure_btn)
    TextView sureBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_mobile_auth);
        ButterKnife.bind(this);
        initView();
    }

    @Override
    protected int setStatusBarColor() {
        return R.color.white;
    }

    private void initView() {
        tvTitle.setText("手机号认证");
    }

    @OnClick({R.id.ll_back, R.id.sure_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_back:
                finish();
                break;
            case R.id.sure_btn:
                checkValue();
                break;
        }
    }

    private void checkValue(){
        if(TextUtils.isEmpty(etEcPhone.getText().toString())){
            ToastUtil.show(this,"请输入手机号");
            return;
        }
        if(!Utils.isChinaPhoneLegal(etEcPhone.getText().toString())){
            ToastUtil.show(this,"请输入正确的手机号");
            return;
        }
        ToastUtil.show(this,"手机号："+etEcPhone.getText().toString());
    }

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, MobileAuthActivity.class);
        context.startActivity(intent);
    }


}
