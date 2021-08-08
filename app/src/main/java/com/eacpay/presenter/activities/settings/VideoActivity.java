package com.eacpay.presenter.activities.settings;

import android.content.pm.ActivityInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.TextView;

import com.eacpay.R;
import com.eacpay.presenter.activities.util.BRActivity;
import com.eacpay.presenter.customviews.BRText;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;

import java.io.IOException;
import java.io.InputStream;

public class VideoActivity extends BRActivity {
    private static final String TAG = SupportActivity.class.getName();
    private static VideoActivity app;

    public static VideoActivity getApp() {
        return app;
    }

    private PlayerView videoView;
    private MediaController mediaController;
    private TextView changeChinese;
    private TextView changeEnglish;
    private int selectedBackgroundColor
            = Color.parseColor("#41c7db");
    private int normalTextColor = Color.parseColor("#303030");

    private String videoChineseUrl;
    private String videoEnglishUrl;
    private boolean isChinese = true;
    private LinearLayout videoDesc;
    private TextView videoDescText;
    private ImageView videoDescImage;
    private String type;
    private SimpleExoPlayer player;
    private ImageButton imageButtonFullScreen;
    private Boolean isFullScreen = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //注意！！！在setContentView之前！！！
//        requestWindowFeature(Window.FEATURE_NO_TITLE); // 隐藏标题 //这行代码一定要在setContentView之前，不然会闪退
        setContentView(R.layout.activity_video);
        imageButtonFullScreen = findViewById(R.id.exo_fullscreen_button);
        imageButtonFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int flagsFullScreen = WindowManager.LayoutParams.FLAG_FULLSCREEN;
                if (!isFullScreen) {
                    getWindow().addFlags(flagsFullScreen); // 设置全屏
                    LinearLayout.LayoutParams params =
                            (LinearLayout.LayoutParams) videoView.getLayoutParams();
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    params.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    videoView.setLayoutParams(params);
                    findViewById(R.id.video_toolbar).setVisibility(View.GONE);
                    findViewById(R.id.video_desc).setVisibility(View.GONE);
                    findViewById(R.id.lang_change).setVisibility(View.GONE);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                    isFullScreen = true;
                } else { //退出全屏
                    WindowManager.LayoutParams attrs = getWindow().getAttributes();
                    attrs.flags &= (~flagsFullScreen);
                    getWindow().setAttributes(attrs);
                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
                    LinearLayout.LayoutParams params =
                            (LinearLayout.LayoutParams) videoView.getLayoutParams();
                    params.width = ViewGroup.LayoutParams.MATCH_PARENT;
                    params.height = ViewGroup.LayoutParams.WRAP_CONTENT;
                    findViewById(R.id.video_toolbar).setVisibility(View.VISIBLE);
                    findViewById(R.id.video_desc).setVisibility(View.VISIBLE);
                    findViewById(R.id.lang_change).setVisibility(View.VISIBLE);
                    videoView.setLayoutParams(params);
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    isFullScreen = false;
                }
            }
        });
        changeChinese = findViewById(R.id.change_chinese);
        changeChinese.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, " changeChinese onClick: " + isChinese);
                if (isChinese) {
                    return;
                }
                isChinese = true;
                changeChinese.setBackgroundColor(selectedBackgroundColor);
                changeChinese.setTextColor(Color.WHITE);
                changeEnglish.setBackgroundColor(Color.WHITE);
                changeEnglish.setTextColor(normalTextColor);
                changeImage(true);
                try {
                    player.clearMediaItems();
                    player.setMediaItem(MediaItem.fromUri(videoChineseUrl));
                    player.prepare();
                    player.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        changeEnglish = findViewById(R.id.change_english);
        changeEnglish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG, "changeEnglish onClick: " + isChinese);
                if (!isChinese) {
                    return;
                }
                isChinese = false;
                changeEnglish.setBackgroundColor(selectedBackgroundColor);
                changeEnglish.setTextColor(Color.WHITE);
                changeChinese.setBackgroundColor(Color.WHITE);
                changeChinese.setTextColor(normalTextColor);
                changeImage(false);
                try {
                    player.clearMediaItems();
                    player.setMediaItem( MediaItem.fromUri(videoEnglishUrl));
                    player.prepare();
                    player.play();
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        videoChineseUrl = getIntent().getExtras().getString("videoChineseUrl");
        videoEnglishUrl = getIntent().getExtras().getString("videoEnglishUrl");
        String title = getIntent().getExtras().getString("title");
        type = getIntent().getExtras().getString("type");
        ((BRText) findViewById(R.id.video_toolbar_txt)).setText(title);
        findViewById(R.id.bak_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
        videoDesc = findViewById(R.id.video_desc);
        videoDescText = findViewById(R.id.video_desc_text);
        videoDescImage = findViewById(R.id.video_desc_image);
        initVideoDesc(type);
        playVideo();

    }

    private void changeImage(Boolean chinese) {
        if ("buy".equals(type)) {
            videoDescImage.setImageBitmap(getBitmapFromAssets(
                    chinese ? "buy_chinese.png"
                            : "buy_english.png"));
            return;
        }
        if ("exchange".equals(type)) {
            videoDescImage.setImageBitmap(getBitmapFromAssets(
                    chinese ? "exchange_chinese.png"
                            : "exchange_english.png"
            ));
        }
    }

    private void initVideoDesc(String type) {
        if (null == type) {
            return;
        }
        if ("introduction_and_use_of_eac".equals(type)) {
            videoDesc.setVisibility(View.VISIBLE);
            videoDescText.setVisibility(View.VISIBLE);
            videoDescImage.setVisibility(View.GONE);
            videoDescText.setText(getText(R.string.txt_introduction_and_use_of_eac));
            return;
        }
        if ("mining_eac_course".equals(type)) {
            videoDesc.setVisibility(View.VISIBLE);
            videoDescText.setVisibility(View.VISIBLE);
            videoDescImage.setVisibility(View.GONE);
            videoDescText.setText(getText(R.string.txt_mining_eac_course));
            return;
        }

        if ("buy".equals(type)) {
            videoDesc.setVisibility(View.VISIBLE);
            videoDescText.setVisibility(View.GONE);
            videoDescImage.setVisibility(View.VISIBLE);
            videoDescImage.setImageBitmap(getBitmapFromAssets("buy_chinese.png"));
            return;
        }
        if ("exchange".equals(type)) {
            videoDesc.setVisibility(View.VISIBLE);
            videoDescText.setVisibility(View.GONE);
            videoDescImage.setVisibility(View.VISIBLE);
            videoDescImage.setImageBitmap(getBitmapFromAssets("exchange_chinese.png"));
        }
    }

    private void playVideo() {
        player = new SimpleExoPlayer.Builder(VideoActivity.this)
                .build();
        videoView = findViewById(R.id.cus_video);
        videoView.setPlayer(player);
        player.setMediaItem(MediaItem.fromUri(videoChineseUrl));
        player.prepare();
        player.play();

    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (null != videoView) {
//            videoView.onResume();
//        }
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
//        if (null != videoView) {
//            videoView.onPause();
//        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        if (null != videoView) {
////            videoView.re();
//            videoView = null;
//        }
    }

    public Bitmap getBitmapFromAssets(String fileName) {
        AssetManager assetManager = getAssets();
        try {
            InputStream istr = assetManager.open("images/" + fileName);
            Bitmap bitmap = BitmapFactory.decodeStream(istr);
            istr.close();
            return bitmap;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }
}