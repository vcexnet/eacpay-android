package com.eacpay.tools.util;

import android.content.Context;

import com.eacpay.presenter.entities.CurrencyEntity;
import com.eacpay.tools.manager.BRSharedPrefs;
import com.eacpay.tools.sqlite.CurrencyDataSource;
import com.eacpay.wallet.BRWalletManager;

import java.math.BigDecimal;

public class BRExchange {

    private static final String TAG = BRExchange.class.getName();

    public static BigDecimal getMaxAmount(Context context, String iso) {
        final long MAX_BTC = 13500000000l;
        if (iso.equalsIgnoreCase("EAC"))
            return getBitcoinForSatoshis(context, new BigDecimal(MAX_BTC * 100000000));
        CurrencyEntity ent = CurrencyDataSource.getInstance(context).getCurrencyByIso(iso);
        if (ent == null) return new BigDecimal(Integer.MAX_VALUE);
        return new BigDecimal(ent.rate * MAX_BTC);
    }

    // amount in satoshis
    public static BigDecimal getBitcoinForSatoshis(Context app, BigDecimal amount) {
        BigDecimal result = new BigDecimal(0);
        int unit = BRSharedPrefs.getCurrencyUnit(app);
        switch (unit) {
            case BRConstants.CURRENT_UNIT_PHOTONS:
                result = new BigDecimal(String.valueOf(amount)).divide(new BigDecimal("100"), 2, BRConstants.ROUNDING_MODE);
                break;
            case BRConstants.CURRENT_UNIT_LITES:
                result = new BigDecimal(String.valueOf(amount)).divide(new BigDecimal("100000"), 5, BRConstants.ROUNDING_MODE);
                break;
            case BRConstants.CURRENT_UNIT_LITECOINS:
                result = new BigDecimal(String.valueOf(amount)).divide(new BigDecimal("100000000"), 8, BRConstants.ROUNDING_MODE);
                break;
        }
        return result;
    }

    public static BigDecimal getSatoshisForBitcoin(Context app, BigDecimal amount) {
        BigDecimal result = new BigDecimal(0);
        int unit = BRSharedPrefs.getCurrencyUnit(app);
        switch (unit) {
            case BRConstants.CURRENT_UNIT_PHOTONS:
                result = new BigDecimal(String.valueOf(amount)).multiply(new BigDecimal("100"));
                break;
            case BRConstants.CURRENT_UNIT_LITES:
                result = new BigDecimal(String.valueOf(amount)).multiply(new BigDecimal("100000"));
                break;
            case BRConstants.CURRENT_UNIT_LITECOINS:
                result = new BigDecimal(String.valueOf(amount)).multiply(new BigDecimal("100000000"));
                break;
        }
        return result;
    }

    public static String getBitcoinSymbol(Context app) {
        String currencySymbolString = BRConstants.bitcoinLowercase;
        if (app != null) {
            int unit = BRSharedPrefs.getCurrencyUnit(app);
            switch (unit) {
                case BRConstants.CURRENT_UNIT_PHOTONS:
                    currencySymbolString = "m" + BRConstants.bitcoinLowercase;
//                        decimalPoints = 2;
//                    if (getNumberOfDecimalPlaces(result.toPlainString()) == 1)
//                        currencyFormat.setMinimumFractionDigits(1);
                    break;
                case BRConstants.CURRENT_UNIT_LITES:
                    currencySymbolString = BRConstants.bitcoinLowercase;
                    break;
                case BRConstants.CURRENT_UNIT_LITECOINS:
                    currencySymbolString = BRConstants.bitcoinUppercase;
                    break;
            }
        }
        return currencySymbolString;
    }

    //get an iso amount from  satoshis
    public static BigDecimal getAmountFromSatoshis(Context app, String iso, BigDecimal amount) {
        BigDecimal result;
        if (iso.equalsIgnoreCase("EAC")) {
            result = getBitcoinForSatoshis(app, amount);
        } else {
            //multiply by 100 because core function localAmount accepts the smallest amount e.g. cents
            CurrencyEntity ent = CurrencyDataSource.getInstance(app).getCurrencyByIso(iso);
            if (ent == null) return new BigDecimal(0);
            BigDecimal rate = new BigDecimal(ent.rate).multiply(new BigDecimal(100));
            result = new BigDecimal(BRWalletManager.getInstance().localAmount(amount.longValue(), rate.doubleValue()))
                    .divide(new BigDecimal(100), 2, BRConstants.ROUNDING_MODE);
        }
        return result;
    }


    //get satoshis from an iso amount
    public static BigDecimal getSatoshisFromAmount(Context app, String iso, BigDecimal amount) {
        BigDecimal result;
        if (iso.equalsIgnoreCase("EAC")) {
            result = BRExchange.getSatoshisForBitcoin(app, amount);
        } else {
            //multiply by 100 because core function localAmount accepts the smallest amount e.g. cents
            CurrencyEntity ent = CurrencyDataSource.getInstance(app).getCurrencyByIso(iso);
            if (ent == null) return new BigDecimal(0);
            BigDecimal rate = new BigDecimal(ent.rate).multiply(new BigDecimal(100));
            result = new BigDecimal(BRWalletManager.getInstance().bitcoinAmount(amount.multiply(new BigDecimal(100)).longValue(), rate.doubleValue()));
        }
        return result;
    }
}
