package com.latuhov.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.widget.ImageView;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;

import java.util.ArrayList;

/**
 * Created by Latuhov on 11/8/16.
 */

public class ImageUtilsLoader {

    public static void loadImage(Context context, String url, ImageView imageView) {
        loadImage(context, url, imageView, false);
    }

    private static final int MAX_PIC_SIZE = 1024;
    private static PulseLruCache memCache;

    public static PulseLruCache getCache() {
        if (memCache == null) {
            int size = (int) (Runtime.getRuntime().maxMemory() / (1024 * 4));
            memCache = new PulseLruCache(size);
        }
        return memCache;
    }


    public static void preloadImages(final Context context, final ArrayList<String> loadUrls) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                for (String url : loadUrls) {
                    preloadImage(context, url, new LoadingListener() {
                        @Override
                        public void onSuccess() {
                        }

                        @Override
                        public void onFail() {
                        }
                    });
                }
            }
        }, 2000);
    }

    public static void loadImage(Context context, String url, ImageView imageView, boolean fitAndCrop) {
        DrawableTypeRequest drawableTypeRequest = Glide.with(context).load(url);
        if (fitAndCrop)
            drawableTypeRequest.fitCenter().centerCrop().into(imageView);
        else
            drawableTypeRequest.into(imageView);
    }

    public static void loadImage(Context context, String url, final ImageView imageView, Drawable errorDrawable, Drawable holderDrawable) {
        if (getCache().loadedFromCache(url, imageView)) {
            return;
        }

        BitmapRequestBuilder<String, Bitmap> builder = Glide.with(context).load(url).asBitmap()
                .fitCenter()
                .error(errorDrawable);
        if (holderDrawable != null)
            builder.placeholder(holderDrawable).into(imageView);
        else
            builder.into(imageView);
    }

    public static void loadImage(Context context, String url, ImageView imageView, Drawable errorDrawable) {
        loadImage(context, url, imageView, errorDrawable, null);
    }

    public static void preloadImage(Context context, final String url, final LoadingListener requestListener) {
        loadToBitmap(context, url, new LoadingBitmapListener() {
            @Override
            public void onSuccess(Bitmap bitmap) {
                getCache().put(url, bitmap);
                requestListener.onSuccess();
            }

            @Override
            public void onFail() {
                requestListener.onFail();
            }
        });
    }

    public static void loadImage(Context context, String url, ImageView imageView, final LoadingListener requestListener) {
        DrawableRequestBuilder builder = getRequestBuilder(context, url, requestListener);
        if (imageView != null) builder.into(imageView);
    }

    public static void loadImageResized(Context context, String url, ImageView imageView, final LoadingListener requestListener) {
        DrawableRequestBuilder builder = getRequestBuilder(context, url, requestListener);
        builder.override(MAX_PIC_SIZE, MAX_PIC_SIZE).into(imageView);
    }


    private static DrawableRequestBuilder getRequestBuilder(Context context, String url, final LoadingListener requestListener) {
        return Glide.with(context).load(url).listener(new RequestListener<String, GlideDrawable>() {
            @Override
            public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                if (requestListener != null) requestListener.onFail();
                return false;
            }

            @Override
            public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                if (requestListener != null) requestListener.onSuccess();
                return false;
            }
        });
    }

    public static void loadImage(Context context, String url, ImageView imageView, final LoadingListener requestListener, Drawable errorDrawable, Drawable holderDrawable) {
        if (getCache().loadedFromCache(url, imageView)) {
            requestListener.onSuccess();
            return;
        }

        DrawableRequestBuilder builder = getRequestBuilder(context, url, requestListener);
        builder.placeholder(holderDrawable).error(errorDrawable).into(imageView);
    }

    public static SimpleTarget<Bitmap> loadToBitmap(Context context, String url, final LoadingBitmapListener requestListener) {
        SimpleTarget<Bitmap> simpleTarget = new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap bitmap, GlideAnimation<? super Bitmap> glideAnimation) {
                requestListener.onSuccess(bitmap);
            }

            @Override
            public void onLoadFailed(Exception e, Drawable errorDrawable) {
                super.onLoadFailed(e, errorDrawable);
                requestListener.onFail();
            }
        };
        Glide.with(context).load(url).asBitmap().into(simpleTarget);
        return simpleTarget;
    }

    public interface LoadingListener {
        void onSuccess();

        void onFail();
    }

    public interface LoadingBitmapListener {
        void onSuccess(Bitmap bitmap);

        void onFail();
    }
}
