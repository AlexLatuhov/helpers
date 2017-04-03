package com.latuhov.helpers;

import android.app.DialogFragment;
import android.view.MotionEvent;
import android.widget.EditText;

import com.latuhov.helpers.basic.BasicActivity;

import java.util.ArrayList;

/**
 * Created by Latuhov on 3/3/17.
 */

public class ActivityWithHideKeyboardLogic extends BasicActivity {
    public ArrayList<EditText> editTextToManage = new ArrayList<>();
    protected DialogFragment progress;


    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            EditText viewToHide = null;
            boolean hide = true;
            for (EditText editText : editTextToManage) {
                if (editText != null) {
                    boolean isInTouchArea = ViewUtils.isPointInsideView(ev.getRawX(), ev.getRawY(), editText);
                    if (!isInTouchArea && editText.hasFocus()) {
                        viewToHide = editText;
                        break;
                    } else if (isInTouchArea && !editText.hasFocus()) {
                        hide = false;
                        break;
                    }
                }
            }
            if (hide && viewToHide != null) {
                viewToHide.clearFocus();
                hideActiveKeyboard();
            }
        }
        return super.dispatchTouchEvent(ev);
    }

}
