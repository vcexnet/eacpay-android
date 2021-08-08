package com.eacpay.tools.manager;

import android.content.Context;
import android.graphics.Typeface;
import android.widget.TextView;

import java.util.Hashtable;

import timber.log.Timber;

public class FontManager {

    private static final Hashtable<String, Typeface> cache = new Hashtable<>();

    public static void overrideFonts(TextView... v) {
        if (v == null) return;
        Typeface FONT_REGULAR = Typeface.create("sans-serif-light", Typeface.NORMAL);
        for (TextView view : v) {
            try {
                if (view != null) {
                    view.setTypeface(FONT_REGULAR);
                }
            } catch (Exception e) {
                Timber.e(e);
            }
        }
    }

    public static Typeface get(Context c, String name) {
        synchronized (cache) {
            if (!cache.containsKey(name)) {
                Typeface t = Typeface.createFromAsset(
                        c.getAssets(),
                        String.format("fonts/%s", name)
                );
                cache.put(name, t);
            }
            return cache.get(name);
        }
    }

    public static boolean setCustomFont(Context ctx, TextView v, String asset) {
        Typeface tf = FontManager.get(ctx,  asset);
        v.setTypeface(tf);
        return true;
    }

}
