package com.eacpay.presenter.activities.util;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;

import com.eacpay.EacApp;
import com.eacpay.presenter.activities.DisabledActivity;
import com.eacpay.presenter.activities.intro.IntroActivity;
import com.eacpay.presenter.activities.intro.RecoverActivity;
import com.eacpay.presenter.activities.intro.WriteDownActivity;
import com.eacpay.tools.animation.BRAnimator;
import com.eacpay.tools.manager.BRApiManager;
import com.eacpay.tools.manager.InternetManager;
import com.eacpay.tools.security.AuthManager;
import com.eacpay.tools.security.BRKeyStore;
import com.eacpay.tools.security.BitcoinUrlHandler;
import com.eacpay.tools.security.PostAuth;
import com.eacpay.tools.threads.BRExecutor;
import com.eacpay.tools.util.BRConstants;
import com.eacpay.wallet.BRWalletManager;
import com.platform.HTTPServer;
import com.platform.tools.BRBitId;

import timber.log.Timber;
public class BRActivity extends Activity {

    static {
        System.loadLibrary(BRConstants.NATIVE_LIB_NAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        EacApp.activityCounter.decrementAndGet();
        EacApp.onStop(this);
        EacApp.backgroundedTime = System.currentTimeMillis();
    }

    @Override
    protected void onResume() {
        init(this);
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        // 123 is the qrCode result
        switch (requestCode) {
            case BRConstants.PAY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    BRExecutor.getInstance().forLightWeightBackgroundTasks().execute(new Runnable() {
                        @Override
                        public void run() {
                            PostAuth.getInstance().onPublishTxAuth(BRActivity.this, true);
                        }
                    });
                }
                break;
            case BRConstants.REQUEST_PHRASE_BITID:
                if (resultCode == RESULT_OK) {
                    PostAuth.getInstance().onBitIDAuth(BRActivity.this, true);

                }
                break;

            case BRConstants.PAYMENT_PROTOCOL_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    PostAuth.getInstance().onPaymentProtocolRequest(this, true);
                }
                break;

            case BRConstants.CANARY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    PostAuth.getInstance().onCanaryCheck(this, true);
                } else {
                    finish();
                }
                break;

            case BRConstants.SHOW_PHRASE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    PostAuth.getInstance().onPhraseCheckAuth(this, true);
                }
                break;
            case BRConstants.PROVE_PHRASE_REQUEST:
                if (resultCode == RESULT_OK) {
                    PostAuth.getInstance().onPhraseProveAuth(this, true);
                }
                break;
            case BRConstants.PUT_PHRASE_RECOVERY_WALLET_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    PostAuth.getInstance().onRecoverWalletAuth(this, true);
                } else {
                    finish();
                }
                break;

            case BRConstants.SCANNER_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            String result = data.getStringExtra("result");
                            if (BitcoinUrlHandler.isBitcoinUrl(result))
                                BitcoinUrlHandler.processRequest(BRActivity.this, result);
                            else if (BRBitId.isBitId(result))
                                BRBitId.signBitID(BRActivity.this, result, null);
                            else
                                Timber.i("onActivityResult: not earthcoin address NOR bitID");
                        }
                    }, 500);

                }
                break;
            case BRConstants.SCANNER_BCH_REQUEST:
                if (resultCode == Activity.RESULT_OK) {
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            String result = data.getStringExtra("result");
                            PostAuth.getInstance().onSendBch(BRActivity.this, true, result);
                        }
                    }, 500);

                }
                break;

            case BRConstants.PUT_PHRASE_NEW_WALLET_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    PostAuth.getInstance().onCreateWalletAuth(this, true);
                } else {
                    Timber.d("WARNING: resultCode != RESULT_OK");
                    BRWalletManager m = BRWalletManager.getInstance();
                    m.wipeWalletButKeystore(this);
                    finish();
                }
                break;
        }
    }

    public static void init(Activity app) {
        InternetManager.getInstance();
        if (!(app instanceof IntroActivity || app instanceof RecoverActivity || app instanceof WriteDownActivity))
            BRApiManager.getInstance().startTimer(app);
        //show wallet locked if it is
        if (!ActivityUTILS.isAppSafe(app))
            if (AuthManager.getInstance().isWalletDisabled(app))
                AuthManager.getInstance().setWalletDisabled(app);

        EacApp.activityCounter.incrementAndGet();
        EacApp.setBreadContext(app);
        //lock wallet if 3 minutes passed
        if (EacApp.backgroundedTime != 0 && (System.currentTimeMillis() - EacApp.backgroundedTime >= 180 * 1000) && !(app instanceof DisabledActivity)) {
            if (!BRKeyStore.getPinCode(app).isEmpty()) {
                BRAnimator.startBreadActivity(app, true);
            }
        }
        BRExecutor.getInstance().forBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                HTTPServer.startServer();
            }
        });
        EacApp.backgroundedTime = System.currentTimeMillis();
    }
}
