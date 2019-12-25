package com.example.baiduocr.utils;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Priority;
import com.bumptech.glide.request.RequestOptions;
import com.example.baiduocr.R;

import java.io.File;

/**
 * Description:
 * User: chenzheng
 * Date: 2016/8/31 0031
 * Time: 15:43
 */
public class ImageLoader {

    private static ImageLoader sInstance;

    private static RequestOptions simpleOptions;

    private static RequestOptions roundOptions;

    private static RequestOptions circleOptions;

    /**
     * 单例 获取ImageLoader的实例
     * @return
     */
    public synchronized static ImageLoader getInstance() {
        if(null == sInstance) {
            sInstance = new ImageLoader();
        }
        return sInstance;
    }

    private ImageLoader() {
        simpleOptions = new RequestOptions()
                .centerCrop()
                .encodeQuality(70)
                .placeholder(R.drawable.empty_photo)
                .error(R.drawable.empty_photo)
                .priority(Priority.NORMAL);

        roundOptions = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.empty_photo)
                .error(R.drawable.empty_photo)
                .priority(Priority.NORMAL)
                .transform(new GlideRoundTransform(5));

        circleOptions = new RequestOptions()
                .centerCrop()
                .placeholder(R.drawable.empty_photo)
                .error(R.drawable.empty_photo)
                .priority(Priority.NORMAL)
                .transform(new GlideCircleTransform());
    }

    public void loadImage(Context context, String url, ImageView iv, int emptyImg, int erroImg) {
        if(context!=null) {
            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .placeholder(emptyImg)
                    .error(erroImg)
                    .priority(Priority.HIGH);
            Glide.with(context).load(url).apply(options).into(iv);
        }
    }

    public void loadImage(Context context, String url, ImageView iv) {
        if(context!=null && simpleOptions!=null) {
            Glide.with(context).load(url).apply(simpleOptions).into(iv);
        }
    }

    public void loadCircleImage(Context context, String url, ImageView iv) {
        if(context!=null && circleOptions!=null) {
            Glide.with(context).load(url).apply(circleOptions).into(iv);
        }
    }

    public void loadCircleImage(Context context, String url, ImageView iv, int emptyImg, int erroImg) {
        if(context!=null) {
            RequestOptions options = new RequestOptions()
                    .centerCrop()
                    .placeholder(emptyImg)
                    .error(erroImg)
                    .priority(Priority.HIGH)
                    .transform(new GlideCircleTransform());
            Glide.with(context).load(url).apply(options).into(iv);
        }
    }

    public void loadCircleImage(Context context, int resourceId, ImageView iv) {
        if(context!=null && circleOptions!=null) {
            Glide.with(context).load(resourceId).apply(circleOptions).into(iv);
        }
    }

    public void loadRoundCornerImage(Context context, String url, ImageView iv) {
        if(context!=null && roundOptions!=null) {
            Glide.with(context).load(url).apply(roundOptions).into(iv);
        }
    }

    public void loadImage(Context context, final File file, final ImageView imageView) {
        if(context!=null) {
            Glide.with(context)
                    .load(file)
                    .into(imageView);
        }
    }

    public void loadImage(Context context, final int resourceId, final ImageView imageView) {
        if(context!=null) {
            Glide.with(context)
                    .load(resourceId)
                    .into(imageView);
        }
    }
}