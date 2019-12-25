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
import com.baidu.ocr.sdk.model.OcrRequestParams;
import com.baidu.ocr.sdk.model.OcrResponseResult;
import com.baidu.ocr.sdk.model.ResponseResult;
import com.baidu.ocr.ui.camera.CameraActivity;
import com.example.baiduocr.model.EnterpriseCode;
import com.example.baiduocr.permission.PermissionTool;
import com.example.baiduocr.permission.RuntimeRationale;
import com.example.baiduocr.utils.FastJsonUtils;
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
 * description:  企业认证
 */
public class EnterpriseAuthActivity extends BaseActivity {
    private static final int REQUEST_CODE = 103;
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
    @BindView(R.id.et_ec_name)
    EditText etEcName;
    @BindView(R.id.et_ec_card)
    EditText etEcCard;
    @BindView(R.id.sure_btn)
    TextView sureBtn;
    private ProgressDialog dialog;
    private boolean hasGotToken = false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_enterprise_auth);
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
        tvTitle.setText("企业认证");
    }

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
                checkValue();
                break;
        }
    }

    private boolean checkTokenStatus() {
        if (!hasGotToken) {
            ToastUtil.show(this, "token还未成功获取");
        }
        return hasGotToken;
    }

    private void checkCameraPermission() {
        AndPermission.with(this)
                .runtime()
                .permission(Permission.Group.STORAGE, Permission.Group.CAMERA)
                .rationale(new RuntimeRationale())
                .onGranted(new Action<List<String>>() {
                    @Override
                    public void onAction(List<String> permissions) {
                        enterpriseCamera();
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(@NonNull List<String> permissions) {
                        ToastUtil.show(EnterpriseAuthActivity.this, "拍照权限被拒绝，请手动开启！");
                        if (AndPermission.hasAlwaysDeniedPermission(EnterpriseAuthActivity.this, permissions)) {
                            PermissionTool.showSettingDialog(EnterpriseAuthActivity.this, permissions);
                        }
                    }
                })
                .start();
    }

    private void enterpriseCamera() {
        Intent intent = new Intent(EnterpriseAuthActivity.this, CameraActivity.class);
        intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                FileUtil.getSaveFile(getApplication()).getAbsolutePath());
        intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_GENERAL);
        startActivityForResult(intent, REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                String contentType = data.getStringExtra(CameraActivity.KEY_CONTENT_TYPE);
                String filePath = FileUtil.getSaveFile(getApplicationContext()).getAbsolutePath();
                layoutCamera.setVisibility(View.GONE);
                ivSfzPhoto.setVisibility(View.VISIBLE);
                ImageLoader.getInstance().loadRoundCornerImage(EnterpriseAuthActivity.this, filePath, ivSfzPhoto);
                if (!TextUtils.isEmpty(contentType)) {
                    if (CameraActivity.CONTENT_TYPE_GENERAL.equals(contentType)) {
                        if (!TextUtils.isEmpty(filePath)) {
                            LogUtil.e("营业执照图片路径"+filePath);
                            recQyCard(filePath);
                        }
                    }
                }
            }
        }
    }

    /**
     * 解析营业执照图片
     *
     * @param filePath 图片路径
     */

    private void recQyCard(String filePath) {
        if (!hasGotToken) {
            etEcCard.setText("无");
            etEcName.setText("无");
            return;
        }
        File file = new File(filePath);
        if (file.exists()) {
            if (!isFinishing()) {
                dialog = new ProgressDialog(this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setMessage("正在识别");
                dialog.show();
            }
            // 营业执照识别参数设置
            OcrRequestParams param = new OcrRequestParams();
            // 设置image参数
            param.setImageFile(new File(filePath));
            // 设置其他参数
            param.putParam("detect_direction", true);
            // 调用营业执照识别服务
            OCR.getInstance(EnterpriseAuthActivity.this).recognizeBusinessLicense(param, new OnResultListener<OcrResponseResult>() {
                @Override
                public void onResult(OcrResponseResult result) {
                    // 调用成功，返回OcrResponseResult对象
                    if ((dialog != null) && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    if (result != null) {
                        setResult(result);
                    } else {
                        tipDialog(EnterpriseAuthActivity.this, "识别失败，请重新尝试", "我知道了", R.drawable.dialog_error_icon_red);
                    }
                }

                @Override
                public void onError(OCRError error) {
                    // 调用失败，返回OCRError对象
                    if ((dialog != null) && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    LogUtil.e("onError: " + error.getErrorCode());
                    LogUtil.e("onError: " + error.getMessage());
                    if (error.getErrorCode() == 429 || error.getErrorCode() == 510) {
                        tipDialog(EnterpriseAuthActivity.this, "识别次数限制，请手动录入", "我知道了", R.drawable.dialog_error_icon_red);
                    } else {
                        tipDialog(EnterpriseAuthActivity.this, "识别失败，请重新尝试", "我知道了", R.drawable.dialog_error_icon_red);
                    }
                }
            });

        }
    }

    /**
     * 从返回内容中提取识别出的信息
     *
     * @param result
     * @return
     */
    public void setResult(ResponseResult result) {
        String sb = result.getJsonRes();
        EnterpriseCode code = FastJsonUtils.parseObject(sb, EnterpriseCode.class);
        LogUtil.e("TAG", code.getWords_result().get社会信用代码().getWords());
        LogUtil.e("TAG", code.getWords_result().get单位名称().getWords());
        etEcCard.setText(code.getWords_result().get社会信用代码().getWords());
        etEcName.setText(code.getWords_result().get单位名称().getWords());
    }

    private void checkValue() {
        if (TextUtils.isEmpty(etEcName.getText().toString())) {
            ToastUtil.show(EnterpriseAuthActivity.this, "请输入企业名称");
            return;
        }
        if (TextUtils.isEmpty(etEcCard.getText().toString())) {
            ToastUtil.show(EnterpriseAuthActivity.this, "请输入统一社会信用代码");
            return;
        }
        ToastUtil.show(EnterpriseAuthActivity.this,"企业名称："+etEcName.getText().toString()+"\n"+"统一社会信用代码："+etEcCard.getText().toString());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            // 释放内存资源
            OCR ocrInstance = OCR.getInstance(this);
            if (ocrInstance != null) {
                ocrInstance.release();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void startActivity(Context context) {
        Intent intent = new Intent(context, EnterpriseAuthActivity.class);
        context.startActivity(intent);
    }

}
