package com.eacpay.presenter.activities.settings;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.eacpay.R;
import com.eacpay.presenter.activities.util.BRActivity;
import com.eacpay.tools.manager.BRSharedPrefs;
import com.eacpay.tools.manager.FontManager;
import com.eacpay.tools.security.AuthManager;
import com.eacpay.tools.security.BRKeyStore;
import com.eacpay.tools.util.BRConstants;
import com.eacpay.tools.util.BRExchange;
import com.eacpay.wallet.BRWalletManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


import timber.log.Timber;


public class SpendLimitActivity extends BRActivity {
    public static boolean appVisible = false;
    private static SpendLimitActivity app;
    private ListView listView;
    private LimitAdaptor adapter;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
    }

    public static SpendLimitActivity getApp() {
        return app;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_spend_limit);

        //TODO: all views are using the layout of this button. Views should be refactored without it
        // Hiding until layouts are built.
        ImageButton faq = findViewById(R.id.faq_button);

        listView = findViewById(R.id.limit_list);
        listView.setFooterDividersEnabled(true);
        adapter = new LimitAdaptor(this);
        List<Long> items = new ArrayList<>();
        items.add(getAmountByStep(0).longValue());
        items.add(getAmountByStep(1).longValue());
        items.add(getAmountByStep(2).longValue());
        items.add(getAmountByStep(3).longValue());
        items.add(getAmountByStep(4).longValue());

        adapter.addAll(items);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Timber.d("onItemClick: %s", position);
                long limit = adapter.getItem(position);
                BRKeyStore.putSpendLimit(limit, app);

                AuthManager.getInstance().setTotalLimit(app, BRWalletManager.getInstance().getTotalSent()
                        + BRKeyStore.getSpendLimit(app));
                adapter.notifyDataSetChanged();
            }
        });
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    //satoshis - SANDO note: as ONE_BITCOIN is declared as 32-bit int, you need retype it to long (64-bit integer), otherwise the multiplication will overflow
    private BigDecimal getAmountByStep(int step) {
        BigDecimal result;
        switch (step) {
            case 0:
                result = new BigDecimal(0);// 0 always require
                break;
            case 1:
                result = new BigDecimal((long) BRConstants.ONE_BITCOIN * 1000);//   0.01 BTC
                break;
            case 2:
                result = new BigDecimal((long) BRConstants.ONE_BITCOIN * 10000);//   0.1 BTC
                break;
            case 3:
                result = new BigDecimal((long) BRConstants.ONE_BITCOIN * 100000);//   1 BTC
                break;
            case 4:
                result = new BigDecimal((long) BRConstants.ONE_BITCOIN * 1000000);//   10 BTC
                break;

            default:
                result = new BigDecimal((long) BRConstants.ONE_BITCOIN * 100000);//   1 BTC Default
                break;
        }
        return result;
    }

    private int getStepFromLimit(long limit) {
        int swLimit = (int)(limit/ BRConstants.ONE_BITCOIN);    // SANDO note: switch command does not accept long type as the control variable, we need transaform lond to a reduced int
        switch (swLimit) {
            case 0:
                return 0;
            case 1000:
                return 1;
            case 10000:
                return 2;
            case 100000:
                return 3;
            case 1000000:
                return 4;
            default:
                return 2; //1 BTC Default
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        appVisible = true;
        app = this;
    }

    @Override
    protected void onPause() {
        super.onPause();
        appVisible = false;
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        overridePendingTransition(R.anim.enter_from_left, R.anim.exit_to_right);
    }

    public class LimitAdaptor extends ArrayAdapter<Long> {

        private final Context mContext;
        private final int layoutResourceId;
        private TextView textViewItem;

        public LimitAdaptor(Context mContext) {
            super(mContext, R.layout.currency_list_item);
            this.layoutResourceId = R.layout.currency_list_item;
            this.mContext = mContext;
        }

        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            final long limit = BRKeyStore.getSpendLimit(app);
            if (convertView == null) {
                // inflate the layout
                LayoutInflater inflater = ((Activity) mContext).getLayoutInflater();
                convertView = inflater.inflate(layoutResourceId, parent, false);
            }
            // get the TextView and then set the text (item name) and tag (item ID) values
            textViewItem = convertView.findViewById(R.id.currency_item_text);
            FontManager.overrideFonts(textViewItem);
            Long item = getItem(position);
            BigDecimal curAmount = BRExchange.getAmountFromSatoshis(app, BRSharedPrefs.getIso(app), new BigDecimal(item));
            BigDecimal btcAmount = BRExchange.getBitcoinForSatoshis(app, new BigDecimal(item));
            String text = String.format(item == 0 ? app.getString(R.string.TouchIdSpendingLimit) : "%s (%s)", curAmount, btcAmount);
            textViewItem.setText(text);
            ImageView checkMark = convertView.findViewById(R.id.currency_checkmark);

            if (position == getStepFromLimit(limit)) {
                checkMark.setVisibility(View.VISIBLE);
            } else {
                checkMark.setVisibility(View.GONE);
            }
            return convertView;
        }

        @Override
        public int getItemViewType(int position) {
            return IGNORE_ITEM_VIEW_TYPE;
        }
    }
}
