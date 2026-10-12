package com.hrrb.acceditor;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class SettingsActivity extends Activity {

    private View root;
    private TextView titleView;
    private TextView sectionRead;
    private TextView sectionLang;
    private RadioButton radioSilent;
    private RadioButton radioChar;
    private RadioButton radioWord;
    private RadioButton radioFa;
    private RadioButton radioEn;
    private Button helpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        root = findViewById(R.id.root);
        titleView = findViewById(R.id.titleView);
        sectionRead = findViewById(R.id.sectionRead);
        sectionLang = findViewById(R.id.sectionLang);
        radioSilent = findViewById(R.id.radioSilent);
        radioChar = findViewById(R.id.radioChar);
        radioWord = findViewById(R.id.radioWord);
        radioFa = findViewById(R.id.radioFa);
        radioEn = findViewById(R.id.radioEn);
        helpButton = findViewById(R.id.helpButton);
        RadioGroup modeGroup = findViewById(R.id.modeGroup);
        RadioGroup langGroup = findViewById(R.id.langGroup);

        if (Build.VERSION.SDK_INT >= 28) {
            titleView.setAccessibilityHeading(true);
            sectionRead.setAccessibilityHeading(true);
            sectionLang.setAccessibilityHeading(true);
        }

        int mode = S.mode(this);
        if (mode == S.MODE_CHAR) {
            modeGroup.check(R.id.radioChar);
        } else if (mode == S.MODE_WORD) {
            modeGroup.check(R.id.radioWord);
        } else {
            modeGroup.check(R.id.radioSilent);
        }
        langGroup.check(S.fa(this) ? R.id.radioFa : R.id.radioEn);

        modeGroup.setOnCheckedChangeListener((group, checkedId) -> {
            int m = S.MODE_SILENT;
            if (checkedId == R.id.radioChar) {
                m = S.MODE_CHAR;
            } else if (checkedId == R.id.radioWord) {
                m = S.MODE_WORD;
            }
            S.prefs(this).edit().putInt(S.KEY_MODE, m).apply();
        });

        langGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String lang = checkedId == R.id.radioEn ? "en" : "fa";
            S.prefs(this).edit().putString(S.KEY_LANG, lang).apply();
            applyLanguage();
        });

        helpButton.setOnClickListener(v -> showHelp());

        applyLanguage();
    }

    private void applyLanguage() {
        boolean fa = S.fa(this);
        root.setLayoutDirection(fa ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);

        String title = S.t(this, "تنظیمات", "Settings");
        setTitle(title);
        titleView.setText(title);
        sectionRead.setText(S.t(this, "پس از جابجایی مکان نما", "After the cursor moves"));
        radioSilent.setText(S.t(this, "سکوت", "Silent"));
        radioChar.setText(S.t(this, "خواندن حرف بعدی", "Read next character"));
        radioWord.setText(S.t(this, "خواندن کلمه بعدی", "Read next word"));
        sectionLang.setText(S.t(this, "زبان", "Language"));
        radioFa.setText("فارسی");
        radioEn.setText("English");
        helpButton.setText(S.t(this, "راهنما", "Help"));
    }

    private void showHelp() {
        new AlertDialog.Builder(this)
                .setTitle(S.t(this, "راهنما", "Help"))
                .setMessage(S.t(this,
                        "راهنمای برنامه به‌زودی کامل می‌شود.",
                        "The help guide will be completed later."))
                .setPositiveButton(S.t(this, "بستن", "Close"), null)
                .show();
    }
}
