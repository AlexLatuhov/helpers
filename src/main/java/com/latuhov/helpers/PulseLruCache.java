package com.latuhov.helpers;

import android.graphics.Bitmap;
import android.support.v4.util.LruCache;
import android.util.Log;
import android.widget.ImageView;

import java.util.Map;

/**
 * Created by Latuhov on 11/22/16.
 */

public class PulseLruCache extends LruCache<String, Bitmap> {
    public PulseLruCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected int sizeOf(String key, Bitmap image) {
        return image.getByteCount() / 1024;
    }

    @Override
    protected void entryRemoved(boolean evicted, String key, Bitmap oldBitmap, Bitmap newBitmap) {
        super.entryRemoved(evicted, key, oldBitmap, newBitmap);
        if (oldBitmap != null && !oldBitmap.equals(newBitmap) && !oldBitmap.isRecycled()) {
            oldBitmap.recycle();
        }
    }

    public void deleteAllResults() {
        Map<String, Bitmap> map = this.snapshot();
        if (map != null && !map.isEmpty()) {
            for (String key : map.keySet()) {
                this.remove(key);
            }
        }
    }

    public boolean loadedFromCache(String url, final ImageView imageView) {
        Bitmap image = get(url);
        Log.d("loadedFromCache", "url " + url);
        if (image != null) {
            Log.d("loadedFromCache", "loadedFromCache!!!");
            imageView.setImageBitmap(image);
            return true;
        }
        Log.d("loadedFromCache", "loadedFromCache FALSE!!!");
        return false;
    }

}
