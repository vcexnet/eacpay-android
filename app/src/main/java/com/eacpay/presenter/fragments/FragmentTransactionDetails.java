package com.eacpay.presenter.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.eacpay.R;
import com.eacpay.presenter.entities.TxItem;
import com.eacpay.tools.adapter.TransactionPagerAdapter;
import com.eacpay.tools.animation.BRAnimator;

import java.util.List;

import timber.log.Timber;
public class FragmentTransactionDetails extends Fragment {

    public TextView mTitle;
    public LinearLayout backgroundLayout;
    private ViewPager txViewPager;
    private TransactionPagerAdapter txPagerAdapter;
    private List<TxItem> items;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // The last two arguments ensure LayoutParams are inflated
        // properly.
        View rootView = inflater.inflate(R.layout.fragment_transaction_details, container, false);
        mTitle = (TextView) rootView.findViewById(R.id.title);
        backgroundLayout = (LinearLayout) rootView.findViewById(R.id.background_layout);
        txViewPager = (ViewPager) rootView.findViewById(R.id.tx_list_pager);
        txViewPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            public void onPageScrollStateChanged(int state) {
            }

            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            public void onPageSelected(int position) {
                // Check if this is the page you want.
            }
        });
        txPagerAdapter = new TransactionPagerAdapter(getChildFragmentManager(), items);
        txViewPager.setAdapter(txPagerAdapter);
        txViewPager.setOffscreenPageLimit(5);
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16 * 2, getResources().getDisplayMetrics());
        txViewPager.setPageMargin(-margin);
        int pos = getArguments().getInt("pos");
        txViewPager.setCurrentItem(pos, false);

        return rootView;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ViewTreeObserver observer = txViewPager.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if(observer.isAlive()) {
                    observer.removeOnGlobalLayoutListener(this);
                }
                BRAnimator.animateBackgroundDim(backgroundLayout, false);
                BRAnimator.animateSignalSlide(txViewPager, false, null);
            }
        });

    }

    @Override
    public void onStop() {
        super.onStop();
        final Activity app = getActivity();
        BRAnimator.animateBackgroundDim(backgroundLayout, true);
        BRAnimator.animateSignalSlide(txViewPager, true, new BRAnimator.OnSlideAnimationEnd() {
            @Override
            public void onAnimationEnd() {
                if (app != null && !app.isDestroyed())
                    app.getFragmentManager().popBackStack();
                else
                    Timber.d("onAnimationEnd: app is null");
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
    }

    public void setItems(List<TxItem> items) {
        this.items = items;
    }
}