package com.hrrb.acceditor;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

public class SettingsActivity extends Activity {

    private View root;
    private TextView titleView;
    private TextView sectionLang;
    private Spinner langSpinner;
    private Button helpButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        root = findViewById(R.id.root);
        titleView = findViewById(R.id.titleView);
        sectionLang = findViewById(R.id.sectionLang);
        langSpinner = findViewById(R.id.langSpinner);
        helpButton = findViewById(R.id.helpButton);

        if (Build.VERSION.SDK_INT >= 28) {
            titleView.setAccessibilityHeading(true);
            sectionLang.setAccessibilityHeading(true);
        }

        String[] names = new String[]{"فارسی", "English"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, names);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        langSpinner.setAdapter(adapter);
        langSpinner.setSelection(S.fa(this) ? 0 : 1, false);

        langSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String lang = position == 1 ? "en" : "fa";
                if (!lang.equals(S.prefs(SettingsActivity.this).getString(S.KEY_LANG, "fa"))) {
                    S.prefs(SettingsActivity.this).edit().putString(S.KEY_LANG, lang).apply();
                    applyLanguage();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
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
        sectionLang.setText(S.t(this, "زبان", "Language"));
        helpButton.setText(S.t(this, "راهنما", "Help"));
    }

    private String loadHelp() {
        String file = S.fa(this) ? "help_fa.txt" : "help_en.txt";
        try (java.io.BufferedReader r = new java.io.BufferedReader(
                new java.io.InputStreamReader(getAssets().open(file), "UTF-8"))) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = r.readLine()) != null) {
                sb.append(line).append('\n');
            }
            return sb.toString();
        } catch (java.io.IOException e) {
            return S.t(this, "خواندن راهنما ممکن نشد.", "The help text could not be loaded.");
        }
    }

    private void showHelp() {
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(S.t(this, "راهنما", "Help"))
                .setMessage(loadHelp())
                .setPositiveButton(S.t(this, "بستن", "Close"), null)
                .create();
        dialog.show();
        dialog.getWindow().getDecorView().setLayoutDirection(
                S.fa(this) ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);
    }
}
