package com.eacpay.tools.security;

import android.app.Activity;

import com.eacpay.R;
import com.eacpay.presenter.customviews.BRDialogView;
import com.eacpay.presenter.entities.PaymentRequestWrapper;
import com.eacpay.presenter.entities.RequestObject;
import com.eacpay.tools.animation.BRAnimator;
import com.eacpay.tools.animation.BRDialog;
import com.eacpay.tools.manager.BREventManager;
import com.eacpay.tools.threads.PaymentProtocolTask;
import com.eacpay.wallet.BRWalletManager;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

public class BitcoinUrlHandler {
    private static final Object lockObject = new Object();

    public static synchronized boolean processRequest(Activity app, String url) {
        if (url == null) {
            Timber.d("processRequest: url is null");
            return false;
        }

        Map<String, String> attr = new HashMap<>();
        URI uri = null;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            Timber.e(e);
        }
        attr.put("scheme", uri == null ? "null" : uri.getScheme());
        attr.put("host", uri == null ? "null" : uri.getHost());
        attr.put("path", uri == null ? "null" : uri.getPath());
        BREventManager.getInstance().pushEvent("send.handleURL", attr);

        RequestObject requestObject = getRequestFromString(url);
        if (BRWalletManager.getInstance().confirmSweep(app, url)) {
            return true;
        }
        if (requestObject == null) {
            if (app != null) {
                BRDialog.showCustomDialog(app, app.getString(R.string.JailbreakWarnings_title),
                        app.getString(R.string.Send_invalidAddressTitle), app.getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
                            @Override
                            public void onClick(BRDialogView brDialogView) {
                                brDialogView.dismissWithAnimation();
                            }
                        }, null, null, 0);
            }
            return false;
        }
        if (requestObject.r != null) {
            return tryPaymentRequest(requestObject);
        } else if (requestObject.address != null) {
            return tryBitcoinURL(url, app);
        } else {
            if (app != null) {
                BRDialog.showCustomDialog(app, app.getString(R.string.JailbreakWarnings_title),
                        app.getString(R.string.Send_remoteRequestError), app.getString(R.string.Button_ok), null, new BRDialogView.BROnClickListener() {
                            @Override
                            public void onClick(BRDialogView brDialogView) {
                                brDialogView.dismissWithAnimation();
                            }
                        }, null, null, 0);
            }
            return false;
        }
    }

    public static boolean isBitcoinUrl(String url) {
        RequestObject requestObject = getRequestFromString(url);
        // return true if the request is valid url and has param: r or param: address
        // return true if it is a valid bitcoinPrivKey
        return (requestObject != null && (requestObject.r != null || requestObject.address != null)
                || BRWalletManager.getInstance().isValidBitcoinBIP38Key(url)
                || BRWalletManager.getInstance().isValidBitcoinPrivateKey(url));
    }


    public static RequestObject getRequestFromString(String str) {
        if (str == null || str.isEmpty()) return null;
        RequestObject obj = new RequestObject();

        String tmp = str.trim().replaceAll("\n", "").replaceAll(" ", "%20");

        if (!tmp.startsWith("earthcoin://")) {
            if (!tmp.startsWith("earthcoin:"))
                tmp = "earthcoin://".concat(tmp);
            else
                tmp = tmp.replace("earthcoin:", "earthcoin://");
        }
        URI uri;
        try {
            uri = URI.create(tmp);
        } catch (IllegalArgumentException ex) {
            Timber.e(ex, "getRequestFromString: ");
            return null;
        }

        String host = uri.getHost();
        if (host != null) {
            String addrs = host.trim();
            if (BRWalletManager.validateAddress(addrs)) {
                obj.address = addrs;
            }
        }
        String query = uri.getQuery();
        if (query == null) return obj;
        String[] params = query.split("&");
        for (String s : params) {
            String[] keyValue = s.split("=", 2);
            if (keyValue.length != 2)
                continue;
            if (keyValue[0].trim().equals("amount")) {
                try {
                    BigDecimal bigDecimal = new BigDecimal(keyValue[1].trim());
                    //obj.amount = bigDecimal.multiply(new BigDecimal("100000000")).toString();
                    obj.amount = bigDecimal.toString();
                } catch (NumberFormatException e) {
                    Timber.e(e);
                }
            } else if (keyValue[0].trim().equals("label")) {
                obj.label = keyValue[1].trim();
            } else if (keyValue[0].trim().equals("message")) {
                obj.message = keyValue[1].trim();
            } else if (keyValue[0].trim().startsWith("req")) {
                obj.req = keyValue[1].trim();
            } else if (keyValue[0].trim().startsWith("r")) {
                obj.r = keyValue[1].trim();
            }
        }
        return obj;
    }

    private static boolean tryPaymentRequest(RequestObject requestObject) {
        String theURL;
        String url = requestObject.r;
        synchronized (lockObject) {
            try {
                theURL = URLDecoder.decode(url, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                Timber.e(e);
                return false;
            }
            new PaymentProtocolTask().execute(theURL, requestObject.label);
        }
        return true;
    }

    private static boolean tryBitcoinURL(final String url, final Activity app) {
        RequestObject requestObject = getRequestFromString(url);
        if (requestObject == null || requestObject.address == null || requestObject.address.isEmpty())
            return false;
        final String[] addresses = new String[1];
        addresses[0] = requestObject.address;

        String amount = requestObject.amount;

        if (amount == null || amount.isEmpty() || new BigDecimal(amount).doubleValue() == 0) {
            app.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    BRAnimator.showSendFragment(app, url);
                }
            });
        } else {
            app.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    BRAnimator.showSendFragment(app, url);
                }
            });
            /*if (app != null) {
                BRAnimator.killAllFragments(app);
                BRSender.getInstance().sendTransaction(app, new PaymentItem(addresses, null, new BigDecimal(amount).longValue(), null, true));
            } else {
                Timber.e(new NullPointerException("tryearthcoinURL, app is null!"));
            }*/
        }

        return true;
    }

    public static native PaymentRequestWrapper parsePaymentRequest(byte[] req);

    public static native String parsePaymentACK(byte[] req);

    public static native byte[] getCertificatesFromPaymentRequest(byte[] req, int index);

}
