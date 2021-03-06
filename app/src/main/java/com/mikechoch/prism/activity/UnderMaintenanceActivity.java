package com.mikechoch.prism.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.mikechoch.prism.R;
import com.mikechoch.prism.constant.Default;
import com.mikechoch.prism.constant.Key;
import com.mikechoch.prism.constant.Message;
import com.mikechoch.prism.helper.IntentHelper;


public class UnderMaintenanceActivity extends AppCompatActivity {

    private TextView alertMessageTextView;
    private LinearLayout refreshLinearLayout;
    private TextView refreshTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.under_maintenance_activity_layout);

        alertMessageTextView = findViewById(R.id.under_maintenance_activity_text_view);
        refreshLinearLayout = findViewById(R.id.under_maintenance_activity_refresh_button);
        refreshTextView = findViewById(R.id.under_maintenance_activity_refresh_text_view);

        setupInterfaceElements();
    }

    /**
     * Get the alert message String from Bundle of Intent from SplashScreenActivity
     * @return alert - alert message String
     */
    private String getAlertMessageString() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            return bundle.getString(Key.STATUS_MESSAGE, Message.DEFAULT_UNDER_MAINTENANCE_MESSAGE);
        }
        return Message.DEFAULT_UNDER_MAINTENANCE_MESSAGE;
    }

    /**
     * Setup the alert message text view text
     */
    private void setupAlertMessageView() {
        String alertMessage = getAlertMessageString();
        alertMessageTextView.setText(alertMessage);
    }

    /**
     *
     */
    private void setupRefreshButton() {
        refreshLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentHelper.resetApplication(UnderMaintenanceActivity.this);
            }
        });
    }

    /**
     * Setup elements in current activity
     */
    private void setupInterfaceElements() {
        alertMessageTextView.setTypeface(Default.sourceSansProBold);
        refreshTextView.setTypeface(Default.sourceSansProBold);

        setupAlertMessageView();
        setupRefreshButton();
    }
}
