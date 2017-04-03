package com.latuhov.helpers;

import android.content.Context;

import com.trello.rxlifecycle.components.support.RxFragment;

/**
 * Created by Latuhov on 11/25/16.
 */
//public class HintsFragment extends BaseGameLogicFragment<GameLogicHost>
public abstract class BaseFragment<T> extends RxFragment {
    protected T host;

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        try {
            host = (T) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement HostInterface " + getClass());
        }
    }

    @Override
    public void onDetach() {
        host = null;
        super.onDetach();
    }

    public void dismissProgressDialog() {
        ((BasicActivity) getActivity()).dismissProgressDialog();
    }

    public void showProgressDialog() {
        ((BasicActivity) getActivity()).showProgressDialog();
    }

    public void showErrorDialog(String error) {
        ((BasicActivity) getActivity()).showErrorDialog(null, error, false);
    }

    public void hideActiveKeyboard() {
        ((BasicActivity) getActivity()).hideActiveKeyboard();
    }


}
