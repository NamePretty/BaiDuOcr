package com.example.baiduocr;

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
import com.baidu.ocr.sdk.model.IDCardParams;
import com.baidu.ocr.sdk.model.IDCardResult;
import com.baidu.ocr.ui.camera.CameraActivity;
import com.example.baiduocr.permission.PermissionTool;
import com.example.baiduocr.permission.RuntimeRationale;
import com.example.baiduocr.utils.FileUtil;
import com.example.baiduocr.utils.Global;
import com.example.baiduocr.utils.IdcardValidator;
import com.example.baiduocr.utils.ImageLoader;
import com.example.baiduocr.utils.LogUtil;
import com.example.baiduocr.utils.NetUtils;
import com.example.baiduocr.utils.OtherUtils;
import com.example.baiduocr.utils.ToastUtil;
import com.example.baiduocr.utils.Utils;
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
 * description:  实名认证 身份证正面
 */
public class RealNameAuthActivity extends BaseActivity {
    private static final int REQUEST_CODE_CAMERA = 101;
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
    @BindView(R.id.et_real_name)
    EditText etRealName;
    @BindView(R.id.et_idcard)
    EditText etIdcard;
    @BindView(R.id.remind_tv)
    TextView remindTv;
    @BindView(R.id.sure_btn)
    TextView sureBtn;
    private ProgressDialog dialog;
    private int sex;//1：男 2：女
    private int type;
    private boolean hasGotToken = false;
    private String idCardPath;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_real_name_auth);
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
        tvTitle.setText("实名认证");
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
                checkParams();
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
                        idCardCamera();
                    }
                })
                .onDenied(new Action<List<String>>() {
                    @Override
                    public void onAction(@NonNull List<String> permissions) {
                        ToastUtil.show(RealNameAuthActivity.this, "拍照权限被拒绝，请手动开启！");
                        if (AndPermission.hasAlwaysDeniedPermission(RealNameAuthActivity.this, permissions)) {
                            PermissionTool.showSettingDialog(RealNameAuthActivity.this, permissions);
                        }
                    }
                })
                .start();
    }

    private void idCardCamera() {
        idCardPath = FileUtil.getCameraPath(this) + File.separator + System.currentTimeMillis() + ".jpg";
        Intent intent = new Intent(RealNameAuthActivity.this, CameraActivity.class);
        intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
                idCardPath);
        intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_ID_CARD_FRONT);
        startActivityForResult(intent, REQUEST_CODE_CAMERA);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_CAMERA:
                    if (data != null) {
                        String contentType = data.getStringExtra(CameraActivity.KEY_CONTENT_TYPE);
                        String filePath = idCardPath;
                        if (!TextUtils.isEmpty(contentType)) {
                            if (CameraActivity.CONTENT_TYPE_ID_CARD_FRONT.equals(contentType)) {
                                if (!TextUtils.isEmpty(filePath)) {
                                    LogUtil.e(filePath);
                                    recIDCard(IDCardParams.ID_CARD_SIDE_FRONT, filePath);
                                }
                            }
                        }
                    }
                    break;
            }
        }
    }

    /**
     * 解析身份证图片
     *
     * @param idCardSide 身份证正反面
     * @param filePath   图片路径
     */
    private void recIDCard(String idCardSide, String filePath) {
        if (!hasGotToken) {
            etRealName.setText("");
            etIdcard.setText("");
            tipDialog(RealNameAuthActivity.this, "识别失败，请检查图片质量！", "我知道了", R.drawable.dialog_error_icon_red);
            return;
        }
        final File file = new File(filePath);
        if (file.exists()) {
            if (!isFinishing()) {
                dialog = new ProgressDialog(this);
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                dialog.setCanceledOnTouchOutside(false);
                dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                dialog.setMessage("正在识别");
                dialog.show();
            }
            IDCardParams param = new IDCardParams();
            param.setImageFile(file);
            // 设置身份证正反面
            param.setIdCardSide(idCardSide);
            // 设置方向检测
            param.setDetectDirection(true);
            // 设置图像参数压缩质量0-100, 越大图像质量越好但是请求时间越长。 不设置则默认值为20
            param.setImageQuality(40);
            OCR.getInstance(this).recognizeIDCard(param, new OnResultListener<IDCardResult>() {
                @Override
                public void onResult(IDCardResult result) {
                    if ((dialog != null) && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    LogUtil.e("tag",result.toString());
                    if (result != null) {
                        if (result.getName() != null && !TextUtils.isEmpty(result.getName().toString())) {
                            etRealName.setText(result.getName().toString());
                        }
                        if (result.getIdNumber() != null && !TextUtils.isEmpty(result.getIdNumber().toString())) {
                            etIdcard.setText(result.getIdNumber().toString());
                        }
                        if (result.getGender() != null && !TextUtils.isEmpty(result.getGender().toString())) {
                            if ("男".equals(result.getGender().toString())) {
                                sex = 1;
                            } else if ("女".equals(result.getGender().toString())) {
                                sex = 2;
                            }
                        }
                        ivSfzPhoto.setVisibility(View.VISIBLE);
                        layoutCamera.setVisibility(View.GONE);
                        ImageLoader.getInstance().loadImage(RealNameAuthActivity.this, file, ivSfzPhoto);
                    } else {
                        tipDialog(RealNameAuthActivity.this, "识别失败，请重新尝试", "我知道了", R.drawable.dialog_error_icon_red);
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
                        tipDialog(RealNameAuthActivity.this, "识别次数限制，请手动录入", "我知道了", R.drawable.dialog_error_icon_red);
                    } else {
                        tipDialog(RealNameAuthActivity.this, "识别失败，请重新尝试", "我知道了", R.drawable.dialog_error_icon_red);
                    }
                }
            });
        }
    }

    private void checkParams() {
        if (TextUtils.isEmpty(etRealName.getText().toString())) {
            ToastUtil.show(this, "请输入姓名");
            return;
        }
        if (TextUtils.isEmpty(etIdcard.getText().toString())) {
            ToastUtil.show(this, "请输入身份证号码");
            return;
        }
        if (Utils.hasDigit(etRealName.getText().toString())) {
            ToastUtil.show(this, "您输入的姓名格式错误，请重新输入");
            return;
        }
        if (!Utils.isChinese(etRealName.getText().toString())) {
            ToastUtil.show(this, "您输入的姓名有特殊字符，请重新输入");
            return;
        }
        if (!IdcardValidator.isValidatedAllIdcard(etIdcard.getText().toString().trim())) {
            ToastUtil.show(this, "您输入的身份证号码格式不正确，请重新输入");
            return;
        }
        ToastUtil.show(this,"姓名："+etRealName.getText().toString()+"\n"+"身份证号码："+etIdcard.getText().toString());
    }


    public static void startActivity(Context context) {
        Intent intent = new Intent(context, RealNameAuthActivity.class);
        context.startActivity(intent);
    }


}
