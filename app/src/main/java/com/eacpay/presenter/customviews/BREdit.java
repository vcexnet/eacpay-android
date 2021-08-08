package com.eacpay.presenter.customviews;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import androidx.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.EditText;

import com.eacpay.R;
import com.eacpay.tools.manager.FontManager;
import com.eacpay.tools.util.Utils;

@SuppressLint("AppCompatCustomView") // we don't need to support older versions
public class BREdit extends EditText {
    private static final String TAG = BREdit.class.getName();
    private final int ANIMATION_DURATION = 200;
    private int currentX = 0;
    private int currentY = 0;
    private boolean isBreadButton; //meaning is has the special animation and shadow

    public BREdit(Context context) {
        super(context);
    }

    public BREdit(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public BREdit(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public BREdit(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void init(Context ctx, AttributeSet attrs) {
        TypedArray a = ctx.obtainStyledAttributes(attrs, R.styleable.BREdit);
        String customFont = a.getString(R.styleable.BREdit_customEFont);
        FontManager.setCustomFont(ctx, this, Utils.isNullOrEmpty(customFont) ? "BarlowSemiCondensed-Medium.ttf" : customFont);
        a.recycle();
    }

}
