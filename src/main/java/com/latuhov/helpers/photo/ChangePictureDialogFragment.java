package com.latuhov.helpers.photo;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.Toast;

import com.latuhov.helpers.AppLog;
import com.basic.helpers.R;
import com.latuhov.helpers.ToastHelper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Andrey Derkach on 11/13/15.
 */
public class ChangePictureDialogFragment extends DialogFragment {

    private static final String TAG = "ChangePicture";
    private static final int REQUEST_CODE_SOME_FEATURES_PERMISSIONS = 101;


    public static ChangePictureDialogFragment newInstance() {
        return new ChangePictureDialogFragment();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String items[] = getResources().getStringArray(R.array.take_photo_array);
        if (getArguments() != null && !getArguments().getBoolean(TAG)) {
            items = Arrays.copyOf(items, items.length - 1);
        }

        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                if (which == 0) {
                    if (needToCheckPermission()) requestPermissions();
                    else dispatchTakePicture();
                } else if (which == 1) {
                    dispatchTakeFromGallery();
                }
            }
        });

        builder.setNegativeButton(getString(android.R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        return builder.create();
    }


    private boolean needToCheckPermission() {
        return android.os.Build.VERSION.SDK_INT >= 23;
    }

    private void requestPermissions() {
        if (needToCheckPermission()) {
            int hasLocationPermission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int cameraPermission = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA);
            List<String> permissions = new ArrayList<>();
            if (hasLocationPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }

            if (cameraPermission != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.CAMERA);
            }

            if (!permissions.isEmpty()) {
                requestPermissions(permissions.toArray(new String[permissions.size()]), REQUEST_CODE_SOME_FEATURES_PERMISSIONS);
            } else {
                dispatchTakePicture();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_SOME_FEATURES_PERMISSIONS: {
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                        dispatchTakePicture();
                    } else if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        this.dismiss();
                        ToastHelper.sendMessage(getString(R.string.permission_denied));
                    }
                }
            }
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void dispatchTakeFromGallery() {
        Intent intent;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        else {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        }
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        getActivity().startActivityForResult(Intent.createChooser(intent, "Select Picture"), BasicPhotoFragment.REQUEST_GALLERY);
    }

    private void dispatchTakePicture() {
        Intent intent;
        try {
            intent = CameraUtils.getPhotoIntent(getActivity());
            if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                AppLog.d(TAG, "Starting camera app");
                getActivity().startActivityForResult(intent, BasicPhotoFragment.REQUEST_TAKE_PHOTO);
            } else {
                Toast.makeText(getActivity(), "You don't have camera app!", Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }
}
