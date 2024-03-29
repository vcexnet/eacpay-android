package com.eacpay.tools.security;
public class BRErrorPipe {

    private static final String TAG = BRErrorPipe.class.getName();

    private static android.app.AlertDialog dialog;

//    public static void parseKeyStoreError(final Context context, Exception e, String alias, boolean report) {
//        if (e instanceof KeyPermanentlyInvalidatedException) {
//            BRErrorPipe.showKeyStoreDialog(context, context.getString(R.string.Alert_keystore_title_android) + ": " + alias, context.getString(R.string.Alert_keystore_invalidated_android), context.getString(R.string.Button_ok), null,
//                    new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.cancel();
//                        }
//                    }, null, new DialogInterface.OnDismissListener() {
//                        @Override
//                        public void onDismiss(DialogInterface dialogInterface) {
//                            if (context instanceof Activity) {
//                                if (!BRAnimator.isClickAllowed()) return;
//                                Intent intent = new Intent(context, RecoverActivity.class);
//                                context.startActivity(intent);
//                                ((Activity) context).overridePendingTransition(R.anim.enter_from_right, R.anim.exit_to_left);
//                                BRSharedPrefs.putGreetingsShown(context, true);
//
//                            }
//                        }
//                    });
//            report = false;
//        } else if (e instanceof InvalidKeyException) {
//            showKeyStoreDialog(context, context.getString(R.string.Alert_keystore_title_android), "Failed to load KeyStore(" + alias + "). Please try again later or enter your phrase to recover your Bread now.", context.getString(R.string.AccessibilityLabels_close), null,
//                    null, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            ((Activity) context).finish();
//                        }
//                    },
//                    null);
//        } else {
//            showKeyStoreDialog(context,
//                    context.getString(R.string.Alert_keystore_title_android),
//                    "Failed to load KeyStore:\n" + e.getClass().getSimpleName() + "\n" + e.getMessage(),
//                    context.getString(R.string.AccessibilityLabels_close), null,
//                    new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                            ((Activity) context).finish();
//                        }
//                    },
//                    null,
//                    new DialogInterface.OnDismissListener() {
//                        @Override
//                        public void onDismiss(DialogInterface dialog) {
//                            ((Activity) context).finish();
//                        }
//                    });
//        }
//        if (report) {
//            BRReportsManager.reportBug(e);
//        }
//    }
//
//    public static void parseError(final Context context, String message, Exception ex, final boolean critical) {
//        BRReportsManager.reportBug(ex);
//        showKeyStoreDialog(context,
//                "Internal error:",
//                message,
//                context.getString(R.string.AccessibilityLabels_close), null,
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                        if (critical)
//                            ((Activity) context).finish();
//                    }
//                },
//                null,
//                new DialogInterface.OnDismissListener() {
//                    @Override
//                    public void onDismiss(DialogInterface dialog) {
//                        if (critical)
//                            ((Activity) context).finish();
//                    }
//                });
//    }
//
//    public static void showKeyStoreDialog(Context context, final String title, final String message, final String posButton, final String negButton,
//                                          final DialogInterface.OnClickListener posButtonListener,
//                                          final DialogInterface.OnClickListener negButtonListener,
//                                          final DialogInterface.OnDismissListener dismissListener) {
//        Activity app = (Activity) context;
//        if (app == null) app = (Activity) EacApp.getBreadContext();
//        if (app == null) {
//            Log.e(TAG, "showKeyStoreDialog: app is null");
//            return;
//        }
//        final Activity finalApp = app;
//        if (finalApp != null)
//            finalApp.runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//                    if (finalApp != null) {
//                        if (dialog != null && dialog.isShowing()) {
//                            if (dialog.getOwnerActivity() != null && !dialog.getOwnerActivity().isDestroyed())
//                                dialog.dismiss();
//                            else
//                                return;
//                        }
//                        if (!finalApp.isDestroyed())
//                            dialog = new android.app.AlertDialog.Builder(finalApp).
//                                    setTitle(title)
//                                    .setMessage(message)
//                                    .setPositiveButton(posButton, posButtonListener)
//                                    .setNegativeButton(negButton, negButtonListener)
//                                    .setOnDismissListener(dismissListener)
//                                    .setIcon(android.R.drawable.ic_dialog_alert)
//                                    .show();
//                    }
//                }
//            });
//    }
}
