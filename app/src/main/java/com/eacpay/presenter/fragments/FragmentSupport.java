package com.eacpay.presenter.fragments;

import android.app.Fragment;
import android.content.pm.ApplicationInfo;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.eacpay.R;
import com.eacpay.tools.animation.BRAnimator;
import com.eacpay.tools.animation.SlideDetector;
import com.eacpay.tools.util.Utils;
import com.platform.HTTPServer;

import timber.log.Timber;

import static com.platform.HTTPServer.URL_SUPPORT;
public class FragmentSupport extends Fragment {
    public LinearLayout backgroundLayout;
    public CardView signalLayout;
    WebView webView;
    public static boolean appVisible = false;
    private String onCloseUrl;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_support, container, false);
        backgroundLayout = rootView.findViewById(R.id.background_layout);
        signalLayout = rootView.findViewById(R.id.signal_layout);

        signalLayout.setOnTouchListener(new SlideDetector(getContext(), signalLayout));

        signalLayout.setLayoutTransition(BRAnimator.getDefaultTransition());

        webView = rootView.findViewById(R.id.web_view);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                Timber.d("shouldOverrideUrlLoading: %s %s", request.getUrl(), request.getMethod());
                if (onCloseUrl != null && request.getUrl().toString().equalsIgnoreCase(onCloseUrl)) {
                    getActivity().onBackPressed();
                    onCloseUrl = null;
                } else if (request.getUrl().toString().contains("_close")) {
                    getActivity().onBackPressed();
                } else {
                    view.loadUrl(request.getUrl().toString());
                }

                return true;
            }
        });

        HTTPServer.mode = HTTPServer.ServerMode.SUPPORT;
        String articleId = getArguments() == null ? null : getArguments().getString("articleId");

        WebSettings webSettings = webView.getSettings();

        if (0 != (getActivity().getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE)) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        webSettings.setDomStorageEnabled(true);
        webSettings.setJavaScriptEnabled(true);

        String theUrl = URL_SUPPORT;
        if (articleId != null && !articleId.isEmpty()) {
            theUrl += "/article?slug=" + articleId;
        }

        webView.loadUrl(theUrl);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ViewTreeObserver observer = signalLayout.getViewTreeObserver();
        observer.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (observer.isAlive()) {
                    observer.removeOnGlobalLayoutListener(this);
                }
                BRAnimator.animateBackgroundDim(backgroundLayout, false);
                BRAnimator.animateSignalSlide(signalLayout, false, null);
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        BRAnimator.animateBackgroundDim(backgroundLayout, true);
        BRAnimator.animateSignalSlide(signalLayout, true, new BRAnimator.OnSlideAnimationEnd() {
            @Override
            public void onAnimationEnd() {
                if (getActivity() != null) {
                    try {
                        getActivity().getFragmentManager().popBackStack();
                    } catch (Exception ignored) {
                        Timber.e(ignored);
                    }
                }
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        Utils.hideKeyboard(getActivity());
        BRAnimator.supportIsShowing = false;
    }
}