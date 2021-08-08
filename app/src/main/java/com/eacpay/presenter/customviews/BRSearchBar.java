package com.eacpay.presenter.customviews;

import android.content.Context;
import android.os.Handler;
import androidx.annotation.Nullable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.eacpay.R;
import com.eacpay.presenter.activities.BreadActivity;
import com.eacpay.tools.manager.TxManager;
import com.eacpay.tools.threads.BRExecutor;

public class BRSearchBar extends androidx.appcompat.widget.Toolbar {

    private static final String TAG = BRSearchBar.class.getName();

    private EditText searchEdit;
    //    private LinearLayout filterButtonsLayout;
    private BRButton sentFilter;
    private BRButton receivedFilter;
    private BRButton pendingFilter;
    private BRButton completedFilter;
    private BRButton cancelButton;
    private BreadActivity breadActivity;

    public boolean[] filterSwitches = new boolean[4];

    public BRSearchBar(Context context) {
        super(context);
        init();
    }

    public BRSearchBar(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public BRSearchBar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.search_bar, this);
        breadActivity = (BreadActivity) getContext();
        searchEdit = (EditText) findViewById(R.id.search_edit);
        sentFilter = (BRButton) findViewById(R.id.sent_filter);
        receivedFilter = (BRButton) findViewById(R.id.received_filter);
        pendingFilter = (BRButton) findViewById(R.id.pending_filter);
        completedFilter = (BRButton) findViewById(R.id.complete_filter);
        cancelButton = (BRButton) findViewById(R.id.cancel_button);

        clearSwitches();

        setListeners();

        searchEdit.requestFocus();
        searchEdit.postDelayed(new Runnable() {

            @Override
            public void run() {
                InputMethodManager keyboard = (InputMethodManager)
                        getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(searchEdit, 0);
            }
        }, 200); //use 300 to make it run when coming back from lock screen

        BRExecutor.getInstance().forBackgroundTasks().execute(new Runnable() {
            @Override
            public void run() {
                TxManager.getInstance().updateTxList(breadActivity);
            }
        });

    }

    private void updateFilterButtonsUI(boolean[] switches) {
        sentFilter.setType(switches[0] ? 3 : 2);
        receivedFilter.setType(switches[1] ? 3 : 2);
        pendingFilter.setType(switches[2] ? 3 : 2);
        completedFilter.setType(switches[3] ? 3 : 2);
        if (TxManager.getInstance().adapter != null)
            TxManager.getInstance().adapter.filterBy(searchEdit.getText().toString(), filterSwitches);
    }

    private void setListeners() {
        searchEdit.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (!hasFocus) {
                    if (breadActivity.barFlipper != null) {
                        breadActivity.barFlipper.setDisplayedChild(0);
                        clearSwitches();
                    }
                }
            }
        });

        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                searchEdit.setText("");
                breadActivity.barFlipper.setDisplayedChild(0);
                clearSwitches();
                onShow(false);
            }
        });

        searchEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TxManager.getInstance().adapter != null)
                    TxManager.getInstance().adapter.filterBy(s.toString(), filterSwitches);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        searchEdit.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if ((event.getAction() == KeyEvent.ACTION_DOWN) && (keyCode == KeyEvent.KEYCODE_ENTER)) {
                    onShow(false);
                    return true;
                }
                return false;
            }
        });

        sentFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterSwitches[0] = !filterSwitches[0];
                filterSwitches[1] = false;
                updateFilterButtonsUI(filterSwitches);

            }
        });

        receivedFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterSwitches[1] = !filterSwitches[1];
                filterSwitches[0] = false;
                updateFilterButtonsUI(filterSwitches);
            }
        });

        pendingFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterSwitches[2] = !filterSwitches[2];
                filterSwitches[3] = false;
                updateFilterButtonsUI(filterSwitches);
            }
        });

        completedFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterSwitches[3] = !filterSwitches[3];
                filterSwitches[2] = false;
                updateFilterButtonsUI(filterSwitches);
            }
        });
    }

    public void clearSwitches() {
        filterSwitches[0] = false;
        filterSwitches[1] = false;
        filterSwitches[2] = false;
        filterSwitches[3] = false;
    }

    public void onShow(boolean b) {

        final InputMethodManager keyboard = (InputMethodManager)
                getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (b) {
            clearSwitches();
            updateFilterButtonsUI(filterSwitches);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    searchEdit.requestFocus();
                    keyboard.showSoftInput(searchEdit, 0);
                }
            }, 400);
            if (TxManager.getInstance().adapter != null)
                TxManager.getInstance().adapter.updateData();

        } else {
            keyboard.hideSoftInputFromWindow(searchEdit.getWindowToken(), 0);
            clearSwitches();
            updateFilterButtonsUI(filterSwitches);
            if (TxManager.getInstance().adapter != null) {
                TxManager.getInstance().adapter.resetFilter();
            }
        }
    }


}