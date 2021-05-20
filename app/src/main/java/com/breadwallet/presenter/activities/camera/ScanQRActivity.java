package com.breadwallet.presenter.activities.camera;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.legacy.app.ActivityCompat;

import com.breadwallet.R;
import com.breadwallet.presenter.activities.util.BRActivity;
import com.breadwallet.tools.animation.SpringAnimator;
import com.breadwallet.tools.qrcode.QRCodeReaderView;
import com.breadwallet.tools.security.BitcoinUrlHandler;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.DecodeHintType;
import com.google.zxing.FormatException;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;
import com.platform.tools.BRBitId;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Hashtable;

import timber.log.Timber;


/**
 * BreadWallet
 * <p/>
 * Created by Mihail Gutan on <mihail@breadwallet.com> 3/29/17.
 * Copyright (c) 2017 breadwallet LLC
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
public class ScanQRActivity extends BRActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback,
        QRCodeReaderView.OnQRCodeReadListener {
    private ImageView cameraGuide;
    private TextView descriptionText;
    private long lastUpdated;
    private UIUpdateTask task;
    private boolean handlingCode;
    public static boolean appVisible = false;
    private static ScanQRActivity app;
    private static final int MY_PERMISSION_REQUEST_CAMERA = 56432;
    private static final int REQUEST_CODE_SCAN_GALLERY = 223344;
    private ViewGroup mainLayout;

    private QRCodeReaderView qrCodeReaderView;

    public static ScanQRActivity getApp() {
        return app;
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr);
        findViewById(R.id.btn_pick_photo).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(intent, REQUEST_CODE_SCAN_GALLERY);
            }
        });
        cameraGuide = (ImageView) findViewById(R.id.scan_guide);
        descriptionText = (TextView) findViewById(R.id.description_text);

        task = new UIUpdateTask();
        task.start();

        cameraGuide.setImageResource(R.drawable.cameraguide);
        cameraGuide.setVisibility(View.GONE);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            initQRCodeReaderView();
        } else {
//            requestCameraPermission();
            Timber.d("onCreate: Permissions needed? HUH?");
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                cameraGuide.setVisibility(View.VISIBLE);
                SpringAnimator.showExpandCameraGuide(cameraGuide);
            }
        }, 400);
    }


    @Override
    protected void onResume() {
        super.onResume();
        appVisible = true;
        app = this;
        if (qrCodeReaderView != null) {
            qrCodeReaderView.startCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        appVisible = false;
        if (qrCodeReaderView != null) {
            qrCodeReaderView.stopCamera();
        }
        task.stopTask();
    }

    @Override
    public void onBackPressed() {
        overridePendingTransition(R.anim.fade_down, 0);
        super.onBackPressed();
    }

    private class UIUpdateTask extends Thread {
        public boolean running = true;

        @Override
        public void run() {
            super.run();
            while (running) {
                if (System.currentTimeMillis() - lastUpdated > 300) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            cameraGuide.setImageResource(R.drawable.cameraguide);
                            descriptionText.setText("");
                        }
                    });
                }
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    Timber.e(e);
                }
            }
        }

        public void stopTask() {
            running = false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode != MY_PERMISSION_REQUEST_CAMERA) {
            return;
        }

        if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//            Snackbar.make(mainLayout, "Camera permission was granted.", Snackbar.LENGTH_SHORT).show();
            initQRCodeReaderView();
        } else {
//            Snackbar.make(mainLayout, "Camera permission request was denied.", Snackbar.LENGTH_SHORT)
//                    .show();
        }
    }

    @Override
    public void onQRCodeRead(final String text, PointF[] points) {

        if (handlingCode) return;
        if (BitcoinUrlHandler.isBitcoinUrl(text) || BRBitId.isBitId(text)) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lastUpdated = System.currentTimeMillis();
                    cameraGuide.setImageResource(R.drawable.cameraguide);
                    descriptionText.setText("");
                    handlingCode = true;
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("result", text);
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();

                }
            });
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    lastUpdated = System.currentTimeMillis();
                    cameraGuide.setImageResource(R.drawable.cameraguide_red);
                    descriptionText.setText(getString(R.string.Send_invalidAddressTitle));
                }
            });
        }
    }

    private void initQRCodeReaderView() {
        qrCodeReaderView = (QRCodeReaderView) findViewById(R.id.qrdecoderview);
        qrCodeReaderView.setAutofocusInterval(500L);
        qrCodeReaderView.setOnQRCodeReadListener(this);
        qrCodeReaderView.setBackCamera();
        qrCodeReaderView.startCamera();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    }

    @Override
    protected void onActivityResult(final int requestCode,
                                    int resultCode,
                                    Intent data) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_SCAN_GALLERY:
                    handleAlbumPic(data);
                    break;
                default:
                    super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    /**
     * 处理选择的图片
     *
     * @param data
     */
    private void handleAlbumPic(Intent data) {
        //获取选中图片的路径
        final Uri uri = data.getData();
;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Result result = scanningImage(uri);
                if (result != null) {
                    System.out.println("qr" + result.getText());
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("result", result.getText());
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                } else {
                    Toast.makeText(ScanQRActivity.this,
                            getString(R.string.txt_unknown_qrcode),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public Result scanningImage(Uri uri) {
        if (uri == null) {
            return null;
        }
        Hashtable<DecodeHintType, String> hints = new Hashtable<>();
        hints.put(DecodeHintType.CHARACTER_SET, "UTF8"); //设置二维码内容的编码
        Bitmap scanBitmap = decodeUri(this, uri, 2040, 2040);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        scanBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        int[] data = new int[scanBitmap.getWidth() * scanBitmap.getHeight()];
        scanBitmap.getPixels(data, 0, scanBitmap.getWidth(), 0, 0,
                scanBitmap.getWidth(), scanBitmap.getHeight());
        RGBLuminanceSource source = new RGBLuminanceSource(scanBitmap.getWidth(),
                scanBitmap.getWidth(), data);
        BinaryBitmap bitmap1 = new BinaryBitmap(new HybridBinarizer(source));
        QRCodeReader reader = new QRCodeReader();
        try {
            return reader.decode(bitmap1, null);
        } catch (NotFoundException e) {
            e.printStackTrace();
        } catch (ChecksumException e) {
            e.printStackTrace();
        } catch (FormatException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Bitmap decodeUri(Context context, Uri uri, int maxWidth, int maxHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true; //只读取图片尺寸
        readBitmapScale(context, uri, options);

        //计算实际缩放比例
        int scale = 1;
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            if ((options.outWidth / scale > maxWidth &&
                    options.outWidth / scale > maxWidth * 1.4) ||
                    (options.outHeight / scale > maxHeight &&
                            options.outHeight / scale > maxHeight * 1.4)) {
                scale++;
            } else {
                break;
            }
        }

        options.inSampleSize = scale;
        options.inJustDecodeBounds = false;//读取图片内容
        options.inPreferredConfig = Bitmap.Config.RGB_565; //根据情况进行修改
        Bitmap bitmap = null;
        try {
            bitmap = readBitmapData(context, uri, options);
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    private static void readBitmapScale(Context context, Uri uri, BitmapFactory.Options options) {
        if (uri == null) {
            return;
        }
        String scheme = uri.getScheme();
        if (ContentResolver.SCHEME_CONTENT.equals(scheme) ||
                ContentResolver.SCHEME_FILE.equals(scheme)) {
            InputStream stream = null;
            try {
                stream = context.getContentResolver().openInputStream(uri);
                BitmapFactory.decodeStream(stream, null, options);
            } catch (Exception e) {
                Timber.tag("readBitmapScale").w(e, "Unable to open content: " + uri);
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        Timber.e(e, "Unable to close content: " + uri);
                    }
                }
            }
        } else if (ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme)) {
            Timber.e("Unable to close content: " + uri);
        } else {
            Timber.e("Unable to close content: " + uri);
        }
    }

    private static Bitmap readBitmapData(Context context, Uri uri,
                                         BitmapFactory.Options options) {
        if (uri == null) {
            return null;
        }
        Bitmap bitmap = null;
        String scheme = uri.getScheme();
        if (ContentResolver.SCHEME_CONTENT.equals(scheme) ||
                ContentResolver.SCHEME_FILE.equals(scheme)) {
            InputStream stream = null;
            try {
                stream = context.getContentResolver().openInputStream(uri);
                bitmap = BitmapFactory.decodeStream(stream, null, options);
            } catch (Exception e) {
                Timber.e(e, "Unable to open content: " + uri);
            } finally {
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e) {
                        Timber.e(e, "Unable to close content: " + uri);
                    }
                }
            }
        } else if (ContentResolver.SCHEME_ANDROID_RESOURCE.equals(scheme)) {
            Timber.e("Unable to close content: " + uri);
        } else {
            Timber.e("Unable to close content: " + uri);
        }
        return bitmap;
    }
}