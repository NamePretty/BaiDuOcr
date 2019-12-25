package com.example.baiduocr;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.ocr.sdk.OCR;
import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.AccessToken;
import com.baidu.ocr.sdk.model.BankCardParams;
import com.baidu.ocr.sdk.model.BankCardResult;
import com.baidu.ocr.ui.camera.CameraActivity;
import com.example.baiduocr.permission.PermissionTool;
import com.example.baiduocr.permission.RuntimeRationale;
import com.example.baiduocr.utils.FileUtil;
import com.example.baiduocr.utils.Global;
import com.example.baiduocr.utils.ImageLoader;
import com.example.baiduocr.utils.LogUtil;
import com.example.baiduocr.utils.NetUtils;
import com.example.baiduocr.utils.OtherUtils;
import com.example.baiduocr.utils.ToastUtil;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.Permission;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * author: zdj
 * created on: 2019/11/12
 * description:  银行卡认证
 */
public class BankCardAuthActivity extends BaseActivity {
    private static final int REQUEST_CODE_CAMERA = 102;
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
    @BindView(R.id.iv_sfz_photo)
    ImageView ivSfzPhoto;
    @BindView(R.id.layout_camera)
    LinearLayout layoutCamera;
    @BindView(R.id.et_bankno)
    EditText etBankno;
    @BindView(R.id.sure_btn)
    TextView sureBtn;
    private ProgressDialog dialog;
    private boolean hasGotToken = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_bank_card_auth);
        ButterKnife.bind(this);
        if (NetUtils.isHasNet(this)) {
            Global.IS_NETWORK_CONTECT = true;
        } else {
            ToastUtil.show(this, "当前网络不可用，请检查网络设置");
            Global.IS_NETWORK_CONTECT = false;
        }
        initView();
        initAccessTokenWithAkSk();
    }

    @Override
    protected int setStatusBarColor() {
        return R.color.white;
    }

    private void initView() {
        tvTitle.setText("银行卡认证");
    }

    /**
     * 用明文ak，sk初始化
     */
    private void initAccessTokenWithAkSk() {
        OCR.getInstance(this).initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(AccessToken result) {
                String token = result.getAccessToken();
                LogUtil.e("token：" + token);
                hasGotToken = true;
            }

            @Override
            public void onError(OCRError error) {
                error.printStackTrace();
            }
        }, getApplicationContext(), OtherUtils.getBaiduAiApiKey(), OtherUtils.getBaiduAiSecretKey());
    }

    private boolean checkTokenStatus() {
        if (!hasGotToken) {
            ToastUtil.show(this, "token还未成功获取");
        }
        return hasGotToken;
    }


    @OnClick({R.id.ll_back, R.id.layout_camera, R.id.sure_btn})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.ll_back:
                finish();
                break;
            case R.id.layout_camera:
                if (!checkTokenStatus()) {
                    return;
                }
                checkCameraPermission();
                break;
            case R.id.sure_btn:
                checkParams();
                break;
        }
    }

    private void checkCameraPermission() {
        AndPermission.with(this)
                .runtime()
                .permission(Permission.Group.STORAGE, Permission.Group.CAMERA)
                .rationale(new RuntimeRationale())
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {
                        bankCamera();
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(@NonNull List<String> permissions) {
                        ToastUtil.show(BankCardAuthActivity.this, "拍照权限被拒绝，请手动开启！");
                        if (AndPermission.hasAlwaysDeniedPermission(BankCardAuthActivity.this, permissions)) {
                            PermissionTool.showSettingDialog(BankCardAuthActivity.this, permissions);
                        }
                    }
                })
                .start();
    }

    private void bankCamera() {
        Intent intent = new Intent(BankCardAuthActivity.this, CameraActivity.class);
        intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                FileUtil.getSaveFile(getApplication()).getAbsolutePath());
        intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_BANK_CARD);
        startActivityForResult(intent, REQUEST_CODE_CAMERA);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_CODE_CAMERA && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                String contentType = data.getStringExtra(CameraActivity.KEY_CONTENT_TYPE);
                String filePath = FileUtil.getSaveFile(getApplicationContext()).getAbsolutePath();
                layoutCamera.setVisibility(View.GONE);
                ivSfzPhoto.setVisibility(View.VISIBLE);
                ImageLoader.getInstance().loadRoundCornerImage(BankCardAuthActivity.this, filePath, ivSfzPhoto);
                if (!TextUtils.isEmpty(contentType)) {
                    if (CameraActivity.CONTENT_TYPE_BANK_CARD.equals(contentType)) {
                        if (!TextUtils.isEmpty(filePath)) {
                            LogUtil.e(filePath);
                            recCreditCard(filePath);
                        }
                    }
                }
            }
        }
    }

    /**
     * 解析银行卡
     *
     * @param filePath 图片路径
     */
    private void recCreditCard(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            // 银行卡识别参数设置
            BankCardParams param = new BankCardParams();
            param.setImageFile(new File(filePath));

            if (!isFinishing()) {
                dialog = new ProgressDialog(this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setMessage("正在识别");
                dialog.show();
            }

            // 调用银行卡识别服务
            OCR.getInstance(this).recognizeBankCard(param, new OnResultListener<BankCardResult>() {
                @Override
                public void onResult(BankCardResult result) {
                    if ((dialog != null) && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    if (result != null) {
                        if (!TextUtils.isEmpty(result.getBankCardNumber())) {
                            etBankno.setText(result.getBankCardNumber());
                        }
                    }
                }

                @Override
                public void onError(OCRError error) {
                    if ((dialog != null) && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    LogUtil.e("onError: " + error.getErrorCode());
                    LogUtil.e("onError: " + error.getMessage());
                    if (error.getErrorCode() == 429 || error.getErrorCode() == 510) {
                        tipDialog(BankCardAuthActivity.this, "识别次数限制，请手动录入", "我知道了", R.drawable.dialog_error_icon_red);
                    } else {
                        tipDialog(BankCardAuthActivity.this, "识别失败，请重新尝试", "我知道了", R.drawable.dialog_error_icon_red);
                    }
                }
            });
        }
    }


    private void checkParams() {
        if (TextUtils.isEmpty(etBankno.getText().toString())) {
            ToastUtil.show(this, "输入本人银行卡号");
            return;
        }
        ToastUtil.show(this, etBankno.getText().toString());
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放内存资源
        OCR ocrInstance = OCR.getInstance(this);
        if (ocrInstance != null) {
            ocrInstance.release();
        }
    }

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, BankCardAuthActivity.class);
        context.startActivity(intent);
    }

}
