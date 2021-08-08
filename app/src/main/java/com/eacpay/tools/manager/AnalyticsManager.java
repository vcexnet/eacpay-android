package com.eacpay.tools.manager;

import android.content.Context;
import android.os.Bundle;

import com.eacpay.tools.util.BRConstants;
import com.google.firebase.analytics.FirebaseAnalytics;

public final class AnalyticsManager {

    private static FirebaseAnalytics instance;

    private AnalyticsManager() {
        // NO-OP
    }

    public static void init(Context context) {
        instance = FirebaseAnalytics.getInstance(context);
    }

    public static void logCustomEvent(@BRConstants.Event String customEvent) {
        instance.logEvent(customEvent, null);
    }

    public static void logCustomEventWithParams(@BRConstants.Event String customEvent, Bundle params) {
        instance.logEvent(customEvent, params);
    }

    public static void logEvent(String eventString) {
        instance.logEvent(eventString, null);
    }
}




