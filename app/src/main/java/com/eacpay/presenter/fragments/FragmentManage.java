package com.eacpay.presenter.fragments;

import android.app.Fragment;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.eacpay.R;
import com.eacpay.tools.animation.BRAnimator;
import com.eacpay.tools.manager.BRSharedPrefs;
import com.eacpay.tools.security.BRKeyStore;
import com.eacpay.tools.util.Utils;

public class FragmentManage extends Fragment {
    private static final String TAG = FragmentManage.class.getName();

    public TextView mTitle;
    public RelativeLayout layout;
    public LinearLayout signalLayout;
    public EditText walletNameText;
    public TextView creationTimeText;
    private OnNameChanged onNameChanged;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.

        View rootView = inflater.inflate(R.layout.fragment_manage, container, false);
        layout = (RelativeLayout) rootView.findViewById(R.id.layout);
        signalLayout = (LinearLayout) rootView.findViewById(R.id.signal_layout);
        walletNameText = (EditText) rootView.findViewById(R.id.wallet_name_label);
        creationTimeText = (TextView) rootView.findViewById(R.id.wallet_creation_label);

        layout.clearFocus();

        walletNameText.setText(BRSharedPrefs.getWalletName(getContext()));

        walletNameText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (onNameChanged != null) onNameChanged.onNameChanged(s.toString());
                BRSharedPrefs.putWalletName(getActivity(), s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        long time = (long) BRKeyStore.getWalletCreationTime(getContext()) * 1000;
        // multiply by 1000, make it millis, since the Wallet creation time is seconds.
        String creationDate = Utils.formatTimeStamp(time, "MMM. dd, yyyy  ha");

        creationTimeText.setText(String.format(getString(R.string.ManageWallet_creationDatePrefix) + " %s", creationDate));

        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!BRAnimator.isClickAllowed()) return;
                getActivity().onBackPressed();
            }
        });

        mTitle = (TextView) rootView.findViewById(R.id.title);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ViewTreeObserver observer = signalLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(observer.isAlive()) {
                    observer.removeOnGlobalLayoutListener(this);
                }
                BRAnimator.animateBackgroundDim(layout, false);
                BRAnimator.animateSignalSlide(signalLayout, false, null);
            }
        });
    }


    @Override
    public void onStop() {
        super.onStop();
        BRAnimator.animateBackgroundDim(layout, true);
        BRAnimator.animateSignalSlide(signalLayout, true, new BRAnimator.OnSlideAnimationEnd() {
            @Override
            public void onAnimationEnd() {
                if (getActivity() != null)
                    getActivity().getFragmentManager().popBackStack();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        BRSharedPrefs.putWalletName(getActivity(), walletNameText.getText().toString());
        Utils.hideKeyboard(getActivity());

    }

    public interface OnNameChanged {

        void onNameChanged(String name);
    }

    public void setOnNameChanged(OnNameChanged onNameChanged) {
        this.onNameChanged = onNameChanged;
    }
}