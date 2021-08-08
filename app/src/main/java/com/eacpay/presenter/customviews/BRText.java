package com.eacpay.presenter.customviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

import com.eacpay.R;
import com.eacpay.tools.manager.FontManager;
import com.eacpay.tools.util.Utils;

@SuppressLint("AppCompatCustomView") // we don't need to support older versions
public class BRText extends TextView {
    private static final String TAG = BRText.class.getName();

    public BRText(Context context) {
        super(context);
    }

    public BRText(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public BRText(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public BRText(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context ctx, AttributeSet attrs) {
        TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.BRText);
        String customFont = a.getString(R.styleable.BRText_customTFont);
        FontManager.setCustomFont(ctx, this, Utils.isNullOrEmpty(customFont) ? "BarlowSemiCondensed-Medium.ttf" : customFont);
        a.recycle();
        setLineSpacing(0, 1.3f);
    }

}
