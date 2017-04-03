package com.latuhov.helpers.photo;

import android.annotation.TargetApi;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.webkit.MimeTypeMap;

import com.latuhov.helpers.AppLog;
import com.latuhov.helpers.basic.BasicPrefHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Andrey Derkach on 11/13/15.
 */
class CameraUtils {

    private static Uri mFileUri;

    public static Intent getPhotoIntent(Context context) throws IOException {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String fileName = "IMG_" + timeStamp + ".jpg";
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, fileName);
            mFileUri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            mFileUri = Uri.fromFile(new File(fileName));
        }
        //TODO Find out why create temp file doesn't work
//        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
//        String imageFileName = "JPEG_" + timeStamp;
//        File storageDir = Environment.getExternalStoragePublicDirectory(
//                Environment.DIRECTORY_PICTURES);
//        File image = File.createTempFile(
//                imageFileName,  /* prefix */
//                ".jpg",         /* suffix */
//                storageDir      /* directory */
//        );
//        mFileUri = Uri.fromFile(image);
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mFileUri);
        cameraIntent.putExtra("return-data", false);
        BasicPrefHelper.setPhotoPath(mFileUri.toString());
        return cameraIntent;
    }

    static Uri getFileUri() {
        if (mFileUri == null) {
            AppLog.d(BasicPhotoFragment.SAVE_INSTANCE, "getBitmapUriFromCamera fileUri == null");
            mFileUri = Uri.parse(BasicPrefHelper.getPhotoPath());
        }
        AppLog.d(BasicPhotoFragment.SAVE_INSTANCE, "getFileUri" + mFileUri);
        return mFileUri;
    }

    static void setFileUri(Uri uri) {
        mFileUri = uri;
    }

    static Uri getBitmapUriFromCamera(Context context, Intent data, Uri fileUri) {
        AppLog.d(BasicPhotoFragment.SAVE_INSTANCE, "getBitmapUriFromCamera");
        Uri result;
        try {
            if (data != null && data.getData() != null) {
                Uri d = data.getData();
                String mimeType = MimeTypeMap.getSingleton()
                        .getMimeTypeFromExtension(".jpg");
                if ("image/jpeg".equals(mimeType)
                        || "image/png".equals(mimeType)) {
                    result = d;
                    File f = new File(result.getPath());
                    if (!f.exists()) {
                        return null;
                    }
                } else {

                    if (mimeType == null) {
                        if ("content".equals(d.getScheme())) {
                            Uri imageUri = data.getData();
                            // some devices (OS versions return an URI of com.android instead of com.google.android
                            if (imageUri.toString().startsWith("content://com.android.gallery3d.provider")) {
                                // use the com.google provider, not the com.android provider.
                                imageUri = Uri.parse(imageUri.toString().replace("com.android.gallery3d", "com.google.android.gallery3d"));
                            }
                            if (imageUri.toString().startsWith("content://com.google.android.gallery3d")) {
                                return imageUri;
                            }
                            result = Uri.parse(getPath(context, data.getData()));
                            //result = /data.getData();
                            File f = new File(result.getPath());
                            if (!f.exists()) {
                                return result;
                            }
                        } else {
                            return null;
                        }
                    } else {
                        return null;
                    }
                }
            } else {
                AppLog.d(BasicPhotoFragment.SAVE_INSTANCE, "getBitmapUriFromCamera else {");
                File f = new File(getPath(context, fileUri));
                result = Uri.fromFile(f);
                AppLog.d(BasicPhotoFragment.SAVE_INSTANCE, "getBitmapUriFromCamera else  f = " + f + "   result = " + result);
                if (!f.exists()) {
                    return null;
                }
            }
        } catch (Exception e) {
            AppLog.d(BasicPhotoFragment.SAVE_INSTANCE, "getBitmapUriFromCamera catch (Exception e)");
            e.printStackTrace();
            return null;
        }
        return result;
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };
                return getDataColumn(context, contentUri, selection, selectionArgs);
            } else {
//                Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
//                if (cursor != null && cursor.moveToFirst()) {
//                    Log.d("CameraUtils", "getPhotoPath cursor count: "+cursor.getCount());
//                    Log.d("CameraUtils", "getPhotoPath cursor column count: "+cursor.getColumnCount());
//                    cursor.close();
//                }
                return null;
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {

            // Return the remote address
            if (isGooglePhotosUri(uri))
                return uri.getLastPathSegment();

            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is Google Photos.
     */
    private static boolean isGooglePhotosUri(Uri uri) {
        return "com.google.android.apps.photos.content".equals(uri.getAuthority());
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    private static String getDataColumn(Context context, Uri uri, String selection,
                                        String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    private static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    private static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    private static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }


    static int needToRotateBitmap(Bitmap bitmap, String path) {
        int exifOrientation = 0;
        try {
            ExifInterface mExif = new ExifInterface(path);
            int orientation = mExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);

            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_270:
                    exifOrientation = 270;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    exifOrientation = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_90:
                    exifOrientation = 90;
                    break;
                case ExifInterface.ORIENTATION_NORMAL:
                    exifOrientation = 0;
                    break;
            }
            if (bitmap.getWidth() > bitmap.getHeight() &&
                    (orientation == ExifInterface.ORIENTATION_ROTATE_90 ||
                            orientation == ExifInterface.ORIENTATION_ROTATE_270)) {
                return exifOrientation;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return exifOrientation;
//        return 0;
    }

    static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        try {
            Matrix matrix = new Matrix();
            matrix.postRotate(orientation);
            AppLog.d("CameraUtils", "Rotating bitmap for degrees: " + orientation);
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    static FileInputStream rotateFileIfNeeded(String path) throws IOException {
        int targetW = 1200;
        int targetH = 1200;

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        ExifInterface mExif = new ExifInterface(path);
        int orientation = mExif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);

        int exifOrientation = 0;
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_270:
                exifOrientation = 270;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                exifOrientation = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                exifOrientation = 90;
                break;
            case ExifInterface.ORIENTATION_NORMAL:
                exifOrientation = 0;
                break;
        }

        if (exifOrientation != 0 || (photoW > targetW || photoH > targetH)) {
            int scaleFactor = Math.max(photoW / targetW, photoH / targetH);
            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;

            Bitmap mBitmap = BitmapFactory.decodeFile(path, bmOptions);
            if (exifOrientation != 0)
                mBitmap = rotate(mBitmap, exifOrientation);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 90, new FileOutputStream(path));
            return new FileInputStream(path);
        }
        return null;
    }

    private static Bitmap rotate(Bitmap b, int degrees) {
        return rotateAndMirror(b, degrees, false);
    }

    private static Bitmap rotateAndMirror(Bitmap b, int degrees, boolean mirror) {
        if ((degrees != 0 || mirror) && b != null) {
            Matrix m = new Matrix();
            // Mirror first.
            // horizontal flip + rotation = -rotation + horizontal flip
            if (mirror) {
                m.postScale(-1, 1);
                degrees = (degrees + 360) % 360;
                if (degrees == 0 || degrees == 180) {
                    m.postTranslate(b.getWidth(), 0);
                } else if (degrees == 90 || degrees == 270) {
                    m.postTranslate(b.getHeight(), 0);
                } else {
                    throw new IllegalArgumentException("Invalid degrees="
                            + degrees);
                }
            }
            if (degrees != 0) {
                // clockwise
                m.postRotate(degrees, (float) b.getWidth() / 2,
                        (float) b.getHeight() / 2);
            }
            try {
                Bitmap b2 = Bitmap.createBitmap(b, 0, 0, b.getWidth(),
                        b.getHeight(), m, true);
                if (b != b2) {
                    b.recycle();
                    b = b2;
                }
            } catch (OutOfMemoryError ex) {
                // We have no memory to rotate. Return the original bitmap.
            }
        }
        return b;
    }

}
