package com.breadwallet.presenter.activities.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.breadwallet.R;
import com.breadwallet.presenter.activities.util.BRActivity;

import java.util.Locale;

public class SupportActivity extends BRActivity implements View.OnClickListener {
    private static final String TAG = SupportActivity.class.getName();
    private static SupportActivity app;

    public static SupportActivity getApp() {
        return app;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //注意！！！在setContentView之前！！！
        setContentView(R.layout.activity_support);
        findViewById(R.id.tv1).setOnClickListener(this);
        findViewById(R.id.tv2).setOnClickListener(this);
        findViewById(R.id.tv3).setOnClickListener(this);
        findViewById(R.id.tv4).setOnClickListener(this);
        findViewById(R.id.tv5).setOnClickListener(this);
        findViewById(R.id.tv6).setOnClickListener(this);
        findViewById(R.id.tv7).setOnClickListener(this);
        findViewById(R.id.tv8).setOnClickListener(this);
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.fade_up, R.anim.exit_to_bottom);
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    @Override
    public void onClick(View v) {
//        1、EAC介绍及使用教程，自动播放视频，视频文件https://eacpay.com/sc/course.mp4（中文）；https://vcexnet.github.io/img/course.mp4（english）
//        2、如何获得EAC？点击后，弹窗或者打开新页面。具体内容：“获取EAC的渠道有：1、挖矿(参见挖矿教程)；2、购买(参见购买EAC教程)”
//        3、购买EAC教程，自动播放视频，视频文件https://eacpay.com/sc/buy.mp4（中文）；https://vcexnet.github.io/img/buy.mp4（english）
//        4、提币到eacpay教程，自动播放视频，视频文件https://eacpay.com/sc/withdraw.mp4（中文）；https://vcexnet.github.io/img/withdraw.mp4（english）
//        5、挖EAC教程，自动播放视频，视频文件https://eacpay.com/sc/mining.mp4(中文)；https://vcexnet.github.io/img/mining.mp4(english)
//        6、支持交易所，直接获取内容页面嵌套（https://eacpay.com/sc/exchange.html；https://vcexnet.github.io/img/exchange.html）
//        7、支持矿池，直接获取内容页面嵌套（https://eacpay.com/sc/pool.html；https://vcexnet.github.io/img/pool.html）
//        8、支持eacpay的网站和应用，直接获取内容页面嵌套（https://eacpay.com/sc/support.html；https://vcexnet.github.io/img/support.html）
//        9、支持：dev@eacpay.com。仅仅显示即可。
        boolean isZh = isZh();
        Intent intent = null;
        switch (v.getId()) {
            case R.id.tv1:
                intent = new Intent(this,
                        VideoActivity.class);
                intent.putExtra("videoChineseUrl", "https://eacpay.com/sc/course.mp4");
                intent.putExtra("videoEnglishUrl", "https://vcexnet.github.io/img/course.mp4");
                intent.putExtra("title", getString(R.string.supportCenter_eac));
                intent.putExtra("type", "introduction_and_use_of_eac");
                break;
            case R.id.tv2:
                showListDialog();
                break;
            case R.id.tv3:
                intent = new Intent(this,
                        VideoActivity.class);
                intent.putExtra("videoChineseUrl", "https://eacpay.com/sc/buy.mp4");
                intent.putExtra("videoEnglishUrl", "https://vcexnet.github.io/img/buy.mp4");
                intent.putExtra("title", getString(R.string.supportCenter_buyEac));
                intent.putExtra("type", "buy");
                break;
            case R.id.tv4:
                intent = new Intent(this,
                        VideoActivity.class);
                intent.putExtra("videoChineseUrl", "https://eacpay.com/sc/withdraw.mp4");
                intent.putExtra("videoEnglishUrl", "https://vcexnet.github.io/img/withdraw.mp4");
                intent.putExtra("title", getString(R.string.supportCenter_transEac));
                intent.putExtra("type", "exchange");
                break;
            case R.id.tv5:
                intent = new Intent(this,
                        VideoActivity.class);
                intent.putExtra("videoChineseUrl", "https://eacpay.com/sc/mining.mp4");
                intent.putExtra("videoEnglishUrl", "https://vcexnet.github.io/img/mining.mp4");
                intent.putExtra("title", getString(R.string.supportCenter_drawEac));
                intent.putExtra("type", "mining_eac_course");
                break;
            case R.id.tv6:
                //support exchanges
                intent = new Intent(this,
                        CusWebActivity.class);
                intent.putExtra("url", isZh ?
                        "https://eacpay.com/sc/exchange.html"
                        : "https://vcexnet.github.io/img/exchange.html"
                );
                intent.putExtra("title", getString(R.string.supportCenter_supportS));
                break;
            case R.id.tv7:
                //support pool
                intent = new Intent(this,
                        CusWebActivity.class);
                intent.putExtra("url", isZh ?
                        "https://eacpay.com/sc/pool.html"
                        : "https://vcexnet.github.io/img/pool.html"
                );
                intent.putExtra("title", getString(R.string.supportCenter_supportA));
                break;
            case R.id.tv8:
                //support website and app
                intent = new Intent(this,
                        CusWebActivity.class);
                intent.putExtra("url", isZh ?
                        "https://eacpay.com/sc/support.html"
                        : "https://vcexnet.github.io/img/support.html"
                );
                intent.putExtra("title", getString(R.string.supportCenter_supportP));
                break;
            default:
                break;
        }
        if (null == intent) {
            return;
        }
        startActivity(intent);
        overridePendingTransition(R.anim.enter_from_bottom,
                R.anim.fade_down);
    }

    private void showListDialog() {
        final String[] items = {getString(R.string.supportCenter_drawEac), getString(R.string.supportCenter_buyEac)};
        AlertDialog.Builder listDialog =
                new AlertDialog.Builder(this);
        listDialog.setTitle(R.string.txt_you_can_view);
        listDialog.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = null;
                if (which == 0) {
                    intent = new Intent(SupportActivity.this,
                            VideoActivity.class);
                    intent.putExtra("videoChineseUrl", "https://eacpay.com/sc/mining.mp4");
                    intent.putExtra("videoEnglishUrl", "https://vcexnet.github.io/img/mining.mp4");
                    intent.putExtra("title", getString(R.string.supportCenter_drawEac));
                    intent.putExtra("type", "mining_eac_course");
                }
                if (which == 1) {
                    intent = new Intent(SupportActivity.this,
                            VideoActivity.class);
                    intent.putExtra("videoChineseUrl", "https://eacpay.com/sc/buy.mp4");
                    intent.putExtra("videoEnglishUrl", "https://vcexnet.github.io/img/buy.mp4");
                    intent.putExtra("title", getString(R.string.supportCenter_buyEac));
                    intent.putExtra("type", "buy");
                }
                if (null != intent) {
                    SupportActivity.this.startActivity(intent);
                    SupportActivity.this.overridePendingTransition(R.anim.enter_from_bottom,
                            R.anim.fade_down);
                }
            }
        });
        listDialog.setPositiveButton(R.string.txt_i_see,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        listDialog.show();
    }

    private boolean isZh() {
        Locale locale = this.getResources().getConfiguration().locale;
        String language = locale.getLanguage();
        return language.endsWith("zh");
    }
}
