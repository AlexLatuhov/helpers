package com.latuhov.helpers.basic;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.basic.helpers.BuildConfig;
import com.basic.helpers.R;
import com.latuhov.helpers.AppLog;
import com.latuhov.helpers.DrawableBackgroundSelector;
import com.latuhov.helpers.photo.OnActivityResultHandler;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by Latuhov on 12/1/16.
 */

public class BasicActivity extends RxAppCompatActivity {
    protected DialogFragment progress;

    private View decorView;
    private View contentView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //http://stackoverflow.com/questions/8398102/androidwindowsoftinputmode-adjustresize-doesnt-make-any-difference
        if (/*Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT &&*/
                getWindow().getAttributes().softInputMode == WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE) {

            decorView = getWindow().getDecorView();
            contentView = findViewById(android.R.id.content);
            decorView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    Rect r = new Rect();
                    //r will be populated with the coordinates of your view that area still visible.
                    decorView.getWindowVisibleDisplayFrame(r);

                    //get screen height and calculate the difference with the useable area from the r
                    int height = decorView.getContext().getResources().getDisplayMetrics().heightPixels;
                    int diff = height - r.bottom;

                    //if it could be a keyboard add the padding to the view
                    if (diff != 0) {
                        // if the use-able screen height differs from the total screen height we assume that it shows a keyboard now
                        //check if the padding is 0 (if yes set the padding for the keyboard)
                        if (contentView.getPaddingBottom() != diff) {
                            //set the padding of the contentView for the keyboard
                            contentView.setPadding(0, 0, 0, diff);
                        }
                    } else {
                        //check if the padding is != 0 (if yes reset the padding)
                        if (contentView.getPaddingBottom() != 0) {
                            //reset the padding of the contentView
                            contentView.setPadding(0, 0, 0, 0);
                        }
                    }
                }
            });
        }
    }

    protected boolean debug() {
        return BuildConfig.DEBUG;
    }

    protected int getFragmentContainerId() {
        return 0;
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    public void showMessage(String message) {
        showMessage(message, null);
    }

    public void showMessage(String message, String title) {
        if (isFinishing())
            return;

        createDialog(title, message);
    }

    public void showMessage(int messageId, int titleId) {
        if (isFinishing())
            return;

        createDialog(getString(titleId), getString(messageId));
    }

    public void showProgressDialog() {
        try {
            if (getFragmentManager().findFragmentByTag("progress") != null) {
                return;
            }
            if (progress == null) {
                progress = ProgressDialogFragment.newInstance();
                progress.setCancelable(false);
            } else {
                getFragmentManager().beginTransaction().remove(progress).commitAllowingStateLoss();
            }
            if (progress.isAdded()) return;

            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.add(progress, "progress");
            transaction.commitAllowingStateLoss();
        } catch (Exception e) {
            AppLog.d("showProgressDialog Exception!!!!!");
        }
    }

    public void dismissProgressDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (progress != null && progress.isAdded() && !progress.isDetached()) {
                    AppLog.d("dismissProgressDialog");
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.remove(progress);
                    ft.commitAllowingStateLoss();
                    getFragmentManager().executePendingTransactions();
                }
            }
        });
    }

    public void showErrorDialog(String error) {
        showErrorDialog(null, error, false);
    }

    public void showErrorDialog(String title, String error) {
        showErrorDialog(title, error, false);
    }

    public void showErrorDialog(@StringRes int error, boolean finishActivity) {
        showErrorDialog(null, getString(error), finishActivity);
    }

    public void showErrorDialog(String title, String error, boolean finishActivity) {
        if (isFinishing())
            return;

        createDialog(title == null ? getString(R.string.error) : title, error);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(getFragmentContainerId());
        if (fragment != null && (fragment instanceof OnActivityResultHandler)) {
            fragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    public static class ProgressDialogFragment extends DialogFragment {

        public static DialogFragment newInstance() {
            return new ProgressDialogFragment();
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getActivity().getString(R.string.please_wait));
            progressDialog.setCancelable(false);
            progressDialog.setCanceledOnTouchOutside(false);
            return progressDialog;
        }

    }

    protected void createDialog(String title, String message) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).show();
    }

    public void hideActiveKeyboard() {
        if (getCurrentFocus() instanceof EditText) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }
    }

    protected void setSelector(int viewId) {
        setSelector(findViewById(viewId));
    }

    protected void setSelector(View view) {
        if (view == null) return;
        view.setOnTouchListener(DrawableBackgroundSelector.create());
    }

    public void createRetryDialogFinish(String message, DialogInterface.OnClickListener retryListener) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.error)
                .setMessage(message)
                .setPositiveButton(R.string.retry, retryListener)
                .setNegativeButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                }).setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialogInterface) {
                finish();
            }
        }).show();
    }

    public void createRetryDialog(String message, DialogInterface.OnClickListener retryListener) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.error)
                .setMessage(message)
                .setPositiveButton(R.string.retry, retryListener)
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }
}
