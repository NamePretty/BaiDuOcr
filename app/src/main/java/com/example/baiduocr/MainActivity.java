package com.example.baiduocr;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;

import com.example.baiduocr.utils.ToastUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.layout_sjrz)
    LinearLayout layoutSjrz;
    @BindView(R.id.layout_smrz)
    LinearLayout layoutSmrz;
    @BindView(R.id.layout_yhk)
    LinearLayout layoutYhk;
    @BindView(R.id.layout_rlrz)
    LinearLayout layoutRlrz;
    @BindView(R.id.layout_qyrz)
    LinearLayout layoutQyrz;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.layout_sjrz, R.id.layout_smrz, R.id.layout_yhk, R.id.layout_rlrz, R.id.layout_qyrz})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.layout_sjrz:
                MobileAuthActivity.startActivity(this);
                break;
            case R.id.layout_smrz:
                RealNameAuthActivity.startActivity(this);
                break;
            case R.id.layout_yhk:
                BankCardAuthActivity.startActivity(this);
                break;
            case R.id.layout_rlrz:
                ToastUtil.show(this,"努力开发中。。。");
                break;
            case R.id.layout_qyrz:
                EnterpriseAuthActivity.startActivity(this);
                break;
        }
    }
}
