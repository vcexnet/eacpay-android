package com.eacpay.presenter.customviews;

import android.content.Context;
import android.content.res.TypedArray;

import androidx.annotation.Nullable;

import android.util.AttributeSet;
import android.view.View;

import com.eacpay.R;
import com.eacpay.presenter.activities.BreadActivity;

public class BRNotificationBar extends androidx.appcompat.widget.Toolbar {

    private static final String TAG = BRNotificationBar.class.getName();

    private BreadActivity breadActivity = (BreadActivity) getContext();
    private BRText description;
    private BRButton close;

    public boolean[] filterSwitches = new boolean[4];

    public BRNotificationBar(Context context) {
        super(context);
        init(null);
    }

    public BRNotificationBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public BRNotificationBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs);
    }

    private void init(AttributeSet attrs) {
        inflate(getContext(), R.layout.notification_bar, this);
        breadActivity = (BreadActivity) getContext();
        description = (BRText) findViewById(R.id.description);
        close = (BRButton) findViewById(R.id.cancel_button);

        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.BRNotificationBar);
        final int N = a.getIndexCount();
        for (int i = 0; i < N; ++i) {
            int attr = a.getIndex(i);
            switch (attr) {
                case R.styleable.BRNotificationBar_breadText:
                    String text = a.getString(0);
                    description.setText(text);
                    break;
            }
        }
        a.recycle();

        close.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                breadActivity.barFlipper.setDisplayedChild(0);
            }
        });

    }

}