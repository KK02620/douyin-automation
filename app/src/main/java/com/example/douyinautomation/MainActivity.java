package com.example.douyinautomation;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "DouyinAutomationPrefs";
    private static final String KEY_LIKE = "auto_like";
    private static final String KEY_FOLLOW = "auto_follow";
    private static final String KEY_COMMENT = "auto_comment";
    private static final String KEY_SCROLL = "auto_scroll";
    private static final String KEY_COMMENTS = "comments_list";

    private TextView txtStatus;
    private Button btnAccessibility;
    private CheckBox chkLike, chkFollow, chkComment, chkScroll;
    private TextInputEditText edtComments;
    private Button btnSave;

    private SharedPreferences preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        initViews();
        loadSettings();
        setupListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateServiceStatus();
    }

    private void initViews() {
        txtStatus = findViewById(R.id.txtStatus);
        btnAccessibility = findViewById(R.id.btnAccessibility);
        chkLike = findViewById(R.id.chkLike);
        chkFollow = findViewById(R.id.chkFollow);
        chkComment = findViewById(R.id.chkComment);
        chkScroll = findViewById(R.id.chkScroll);
        edtComments = findViewById(R.id.edtComments);
        btnSave = findViewById(R.id.btnSave);
    }

    private void loadSettings() {
        chkLike.setChecked(preferences.getBoolean(KEY_LIKE, true));
        chkFollow.setChecked(preferences.getBoolean(KEY_FOLLOW, false));
        chkComment.setChecked(preferences.getBoolean(KEY_COMMENT, false));
        chkScroll.setChecked(preferences.getBoolean(KEY_SCROLL, true));
        edtComments.setText(preferences.getString(KEY_COMMENTS, "很棒！\n真不错\n支持一下\n点赞了"));
    }

    private void setupListeners() {
        btnAccessibility.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
            }
        });

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveSettings();
                Toast.makeText(MainActivity.this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveSettings() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(KEY_LIKE, chkLike.isChecked());
        editor.putBoolean(KEY_FOLLOW, chkFollow.isChecked());
        editor.putBoolean(KEY_COMMENT, chkComment.isChecked());
        editor.putBoolean(KEY_SCROLL, chkScroll.isChecked());
        editor.putString(KEY_COMMENTS, edtComments.getText().toString());
        editor.apply();
    }

    private void updateServiceStatus() {
        boolean isServiceRunning = isAccessibilityServiceEnabled();
        String statusText = getString(R.string.service_status, 
                isServiceRunning ? getString(R.string.service_running) : getString(R.string.service_stopped));
        txtStatus.setText(statusText);
    }

    private boolean isAccessibilityServiceEnabled() {
        AccessibilityManager accessibilityManager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> enabledServices = accessibilityManager.getEnabledAccessibilityServiceList(
                AccessibilityServiceInfo.FEEDBACK_ALL_MASK);

        for (AccessibilityServiceInfo service : enabledServices) {
            if (service.getResolveInfo().serviceInfo.packageName.equals(getPackageName())) {
                return true;
            }
        }
        return false;
    }
}