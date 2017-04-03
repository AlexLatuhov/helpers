package com.latuhov.helpers.photo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.view.View;
import android.widget.Toast;

import com.latuhov.helpers.AppLog;
import com.latuhov.helpers.basic.BaseFragment;
import com.latuhov.helpers.ToastHelper;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by Andrey Derkach on 11/24/15.
 */
public abstract class BasicPhotoFragment<T> extends BaseFragment<T> implements OnActivityResultHandler {

    public static final int REQUEST_GALLERY = 4;
    public static final int REQUEST_TAKE_PHOTO = 5;
    public static final String PHOTO_URI = "photo_uri", FRAGMENT_TYPE = "FRAGMENT_TYPE";
    private static final String TAG = "BasicPhotoFragment";
    protected static final String SAVE_INSTANCE = "SAVE_INSTANCE";
    public int curType = 0;
    //    protected ImageView previewImage;
    protected String mCurrentPhotoPath;
    protected Uri mCurrentPhotoUri;
    protected int rotation;
    protected Bitmap previewBitmap;
    protected View progressWheel;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            String photoUriString = savedInstanceState.getString(PHOTO_URI);
            if (photoUriString != null) {
                AppLog.d(SAVE_INSTANCE, "onCreate. Restored uri string: " + photoUriString);
                mCurrentPhotoUri = Uri.parse(photoUriString);
                CameraUtils.setFileUri(mCurrentPhotoUri);
            }
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        AppLog.d(SAVE_INSTANCE, "onActivityResult. BasicPhotoFragment");
        AppLog.d(SAVE_INSTANCE, "onActivityResult. resultCode: " + resultCode + " requestCode: " + requestCode +
                " data is null: " + (data == null));
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_GALLERY:
                    if (data != null) {
                        mCurrentPhotoUri = data.getData();
                    }
                    if (mCurrentPhotoUri == null) {
                        Toast.makeText(getActivity(), "Gallery error", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    CameraUtils.setFileUri(null);
                    initPickedPhoto();
                    break;
                case REQUEST_TAKE_PHOTO:
                    mCurrentPhotoUri = CameraUtils.getBitmapUriFromCamera(getActivity(), data, CameraUtils.getFileUri());
                    AppLog.d(SAVE_INSTANCE, "onActivityResult. BasicPhotoFragment mCurrentPhotoUri = " + mCurrentPhotoUri);
                    if (mCurrentPhotoUri == null) {
                        Toast.makeText(getActivity(), "Camera error", Toast.LENGTH_SHORT).show();
                        break;
                    }
                    initPickedPhoto();
                    break;
            }
        }
    }


    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        AppLog.d(SAVE_INSTANCE, "onSaveInstanceState. BasicPhotoFragment");
        AppLog.d(SAVE_INSTANCE, "onSaveInstanceState. mCurrentPhotoPath != null: " +
                (mCurrentPhotoPath != null) + " CameraUri != null: " +
                (CameraUtils.getFileUri() != null));
        if (getUriStringToSave() != null) {
            savedInstanceState.putString(PHOTO_URI, getUriStringToSave());
            savedInstanceState.putInt(FRAGMENT_TYPE, curType);
        }
    }

    public String getUriStringToSave() {
        String uriString = null;
        if (mCurrentPhotoUri != null || CameraUtils.getFileUri() != null) {
            if (CameraUtils.getFileUri() != null) {
                uriString = CameraUtils.getFileUri().toString();
            } else if (mCurrentPhotoUri != null) {
                uriString = mCurrentPhotoUri.toString();
            }
            if (uriString != null)
                AppLog.d(SAVE_INSTANCE, "onSaveInstanceState. Saved uri: " + uriString);
        }
        return uriString;
    }

    protected void handleSelectedImage() {
        if (mCurrentPhotoPath != null) {
            new RotateFileTask().execute();
        } else {
            try {
                InputStream inputStream = getActivity().getContentResolver().openInputStream(mCurrentPhotoUri);
                onImageStreamReady(inputStream);
            } catch (FileNotFoundException e) {
                dismissProgressDialog();
                e.printStackTrace();
            }
        }
    }

    protected void onImageStreamReady(InputStream inputStream) {

    }

    protected int getPreviewTargetWidth() {
        return 512;
    }

    protected void initPickedPhoto() {
        FileDescriptor fileDescriptor = null;
        try {
            ParcelFileDescriptor parcelFileDescriptor = getActivity().getContentResolver().openFileDescriptor(mCurrentPhotoUri, "r");
            if (parcelFileDescriptor != null) {
                fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            }
            if (fileDescriptor == null) {
                AppLog.e(TAG, "File descriptor is null");
                mCurrentPhotoUri = null;
                return;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            mCurrentPhotoUri = null;
            return;
        }
        // Get the dimensions of the View
        int targetW = getPreviewTargetWidth();

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;

        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;


        AppLog.d(TAG, "Original Photo W: " + photoW + " photo H: " + photoH + " targetW: " + targetW);
        if (photoH < 1 || photoW < 1) return;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetW);
        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;

        previewBitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, bmOptions);
        mCurrentPhotoPath = CameraUtils.getPath(getActivity(), mCurrentPhotoUri);

        if (mCurrentPhotoPath != null) {
            rotation = CameraUtils.needToRotateBitmap(previewBitmap, mCurrentPhotoPath);
            if (rotation != 0) {
                previewBitmap = CameraUtils.rotateBitmap(previewBitmap, rotation);
            }
        }

        AppLog.d(TAG, "Preview W: " + previewBitmap.getWidth() + " photo H: " + previewBitmap.getHeight());
        onBitmapReady();
        if (progressWheel != null) progressWheel.setVisibility(View.GONE);
    }

    protected void onBitmapReady() {

    }

    protected class RotateFileTask extends AsyncTask<Void, Void, InputStream> {

        private boolean wasError = false;

        public RotateFileTask() {
        }

        @Override
        protected InputStream doInBackground(Void... params) {
            try {
                return CameraUtils.rotateFileIfNeeded(mCurrentPhotoPath);
            } catch (OutOfMemoryError | IOException e) {
                e.printStackTrace();
                wasError = true;
                return null;
            }
        }

        @Override
        protected void onPostExecute(InputStream result) {
            super.onPostExecute(result);
            if (wasError) {
                dismissProgressDialog();
                ToastHelper.sendMessage("Error while rotating bitmap, memory is not sufficient");
                return;
            }
            if (result == null) {
                try {
                    result = getActivity().getContentResolver().openInputStream(mCurrentPhotoUri);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            onImageStreamReady(result);
        }
    }


}
