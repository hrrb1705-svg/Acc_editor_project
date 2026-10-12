package com.hrrb.acceditor;

import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.Layout;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {

    private View root;
    private TextView titleView;
    private TextView cursorLabel;
    private CursorEditText editor;
    private Button btnSettings;
    private Button btnCut;
    private Button btnCopy;
    private Button btnPaste;
    private Button btnSelectAll;
    private Button btnHome;
    private Button btnUp;
    private Button btnDown;
    private Button btnEnd;
    private Button btnSelToggle;
    private Button btnPrevWord;
    private Button btnPrevChar;
    private Button btnNextChar;
    private Button btnNextWord;

    private boolean selecting = false;
    private int anchor = 0;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private Runnable pendingAnnounce;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        root = findViewById(R.id.root);
        titleView = findViewById(R.id.titleView);
        cursorLabel = findViewById(R.id.cursorLabel);
        editor = findViewById(R.id.editor);
        btnSettings = findViewById(R.id.btnSettings);
        btnCut = findViewById(R.id.btnCut);
        btnCopy = findViewById(R.id.btnCopy);
        btnPaste = findViewById(R.id.btnPaste);
        btnSelectAll = findViewById(R.id.btnSelectAll);
        btnHome = findViewById(R.id.btnHome);
        btnUp = findViewById(R.id.btnUp);
        btnDown = findViewById(R.id.btnDown);
        btnEnd = findViewById(R.id.btnEnd);
        btnSelToggle = findViewById(R.id.btnSelToggle);
        btnPrevWord = findViewById(R.id.btnPrevWord);
        btnPrevChar = findViewById(R.id.btnPrevChar);
        btnNextChar = findViewById(R.id.btnNextChar);
        btnNextWord = findViewById(R.id.btnNextWord);

        if (Build.VERSION.SDK_INT >= 28) {
            titleView.setAccessibilityHeading(true);
        }

        editor.setSelectionListener((start, end) -> updateLabel());

        btnSettings.setOnClickListener(v -> startActivity(new Intent(this, SettingsActivity.class)));

        btnCut.setOnClickListener(v -> doCut());
        btnCopy.setOnClickListener(v -> doCopy());
        btnPaste.setOnClickListener(v -> doPaste());
        btnSelectAll.setOnClickListener(v -> doSelectAll());

        btnHome.setOnClickListener(v -> moveTo(0, false));
        btnEnd.setOnClickListener(v -> moveTo(editor.length(), true));
        btnUp.setOnClickListener(v -> moveLine(-1));
        btnDown.setOnClickListener(v -> moveLine(1));

        btnSelToggle.setOnClickListener(v -> toggleSelection());
        btnPrevWord.setOnClickListener(v -> moveTo(prevWordPos(cursor()), false));
        btnPrevChar.setOnClickListener(v -> moveTo(prevCharPos(cursor()), false));
        btnNextChar.setOnClickListener(v -> moveTo(nextCharPos(cursor()), true));
        btnNextWord.setOnClickListener(v -> moveTo(nextWordPos(cursor()), true));

        applyLanguage();
        updateLabel();
    }

    @Override
    protected void onResume() {
        super.onResume();
        applyLanguage();
        updateLabel();
    }

    // ---------------------------------------------------------------- language

    private void applyLanguage() {
        boolean fa = S.fa(this);
        root.setLayoutDirection(fa ? View.LAYOUT_DIRECTION_RTL : View.LAYOUT_DIRECTION_LTR);

        String name = S.t(this, "ویرایشگر دسترس‌پذیر", "Accessible Editor");
        setTitle(name);
        titleView.setText(name);
        btnSettings.setText(S.t(this, "تنظیمات", "Settings"));
        editor.setHint(S.t(this, "متن را اینجا بنویسید", "Type your text here"));

        btnCut.setText(S.t(this, "برش", "Cut"));
        btnCopy.setText(S.t(this, "کپی", "Copy"));
        btnPaste.setText(S.t(this, "الصاق", "Paste"));
        btnSelectAll.setText(S.t(this, "انتخاب همه", "Select all"));

        btnHome.setText(S.t(this, "ابتدای متن", "Start of text"));
        btnUp.setText(S.t(this, "یک خط بالا", "Line up"));
        btnDown.setText(S.t(this, "یک خط پایین", "Line down"));
        btnEnd.setText(S.t(this, "انتهای متن", "End of text"));

        btnPrevWord.setText(S.t(this, "کلمه قبلی", "Previous word"));
        btnPrevChar.setText(S.t(this, "حرف قبلی", "Previous character"));
        btnNextChar.setText(S.t(this, "حرف بعدی", "Next character"));
        btnNextWord.setText(S.t(this, "کلمه بعدی", "Next word"));
        updateToggleLabel();
    }

    private void updateToggleLabel() {
        if (selecting) {
            btnSelToggle.setText(S.t(this, "پایان انتخاب", "End selection"));
        } else {
            btnSelToggle.setText(S.t(this, "شروع انتخاب", "Start selection"));
        }
    }

    private void updateLabel() {
        cursorLabel.setText(S.t(this, "مکان نما: ", "Cursor position: ") + cursor());
    }

    // ---------------------------------------------------------------- speech

    private void announce(final String text) {
        if (pendingAnnounce != null) {
            handler.removeCallbacks(pendingAnnounce);
        }
        pendingAnnounce = () -> cursorLabel.announceForAccessibility(text);
        handler.postDelayed(pendingAnnounce, 150);
    }

    private String startText() {
        return S.t(this, "ابتدای متن", "Start of text");
    }

    private String endText() {
        return S.t(this, "انتهای متن", "End of text");
    }

    private String charText(int pos) {
        CharSequence t = editor.getText();
        if (pos >= t.length()) {
            return endText();
        }
        int cp = Character.codePointAt(t, pos);
        if (cp == ' ' || cp == 0x00A0) {
            return S.t(this, "فاصله", "Space");
        }
        if (cp == '\n') {
            return S.t(this, "خط جدید", "New line");
        }
        if (cp == '\t') {
            return S.t(this, "تب", "Tab");
        }
        if (cp == 0x200C) {
            return S.t(this, "نیم‌فاصله", "Zero width non joiner");
        }
        return new String(Character.toChars(cp));
    }

    private String wordText(int pos) {
        CharSequence t = editor.getText();
        int n = t.length();
        if (pos >= n) {
            return endText();
        }
        int start;
        if (isWs(t.charAt(pos))) {
            int i = pos;
            while (i < n && isWs(t.charAt(i))) {
                i++;
            }
            if (i >= n) {
                return endText();
            }
            start = i;
        } else {
            start = pos;
            while (start > 0 && !isWs(t.charAt(start - 1))) {
                start--;
            }
        }
        int end = start;
        while (end < n && !isWs(t.charAt(end))) {
            end++;
        }
        return t.subSequence(start, end).toString();
    }

    // ---------------------------------------------------------------- movement

    private int cursor() {
        return Math.max(0, editor.getSelectionEnd());
    }

    private static boolean isWs(char c) {
        return Character.isWhitespace(c) || c == ' ';
    }

    private int prevCharPos(int p) {
        if (p <= 0) {
            return 0;
        }
        CharSequence t = editor.getText();
        if (p >= 2 && Character.isLowSurrogate(t.charAt(p - 1)) && Character.isHighSurrogate(t.charAt(p - 2))) {
            return p - 2;
        }
        return p - 1;
    }

    private int nextCharPos(int p) {
        CharSequence t = editor.getText();
        if (p >= t.length()) {
            return t.length();
        }
        return p + Character.charCount(Character.codePointAt(t, p));
    }

    private int prevWordPos(int p) {
        CharSequence t = editor.getText();
        int i = Math.min(p, t.length());
        while (i > 0 && isWs(t.charAt(i - 1))) {
            i--;
        }
        while (i > 0 && !isWs(t.charAt(i - 1))) {
            i--;
        }
        return i;
    }

    private int nextWordPos(int p) {
        CharSequence t = editor.getText();
        int n = t.length();
        int i = p;
        while (i < n && !isWs(t.charAt(i))) {
            i++;
        }
        while (i < n && isWs(t.charAt(i))) {
            i++;
        }
        return i;
    }

    private void moveLine(int delta) {
        Layout layout = editor.getLayout();
        if (layout == null) {
            return;
        }
        int p = cursor();
        int target = layout.getLineForOffset(p) + delta;
        int newPos;
        if (target < 0) {
            newPos = 0;
        } else if (target >= layout.getLineCount()) {
            newPos = editor.length();
        } else {
            float x = layout.getPrimaryHorizontal(p);
            newPos = layout.getOffsetForHorizontal(target, x);
        }
        moveTo(newPos, delta > 0);
    }

    private void moveTo(int target, boolean forward) {
        int len = editor.length();
        int old = cursor();
        int pos = Math.max(0, Math.min(target, len));
        if (selecting) {
            anchor = Math.max(0, Math.min(anchor, len));
            editor.setSelection(anchor, pos);
        } else {
            editor.setSelection(pos);
        }
        updateLabel();

        int mode = S.mode(this);
        if (mode == S.MODE_SILENT) {
            return;
        }
        if (pos == old) {
            announce(forward ? endText() : startText());
            return;
        }
        announce(mode == S.MODE_CHAR ? charText(pos) : wordText(pos));
    }

    // ---------------------------------------------------------------- selection

    private void toggleSelection() {
        selecting = !selecting;
        if (selecting) {
            anchor = cursor();
            editor.setSelection(anchor);
            announce(S.t(this, "شروع انتخاب", "Selection started"));
        } else {
            int count = Math.abs(editor.getSelectionEnd() - editor.getSelectionStart());
            announce(S.t(this, "پایان انتخاب، " + count + " حرف انتخاب شد",
                    "Selection ended, " + count + " characters selected"));
        }
        updateToggleLabel();
    }

    private void endSelectionMode() {
        selecting = false;
        updateToggleLabel();
    }

    private int selStart() {
        return Math.max(0, Math.min(editor.getSelectionStart(), editor.getSelectionEnd()));
    }

    private int selEnd() {
        return Math.max(0, Math.max(editor.getSelectionStart(), editor.getSelectionEnd()));
    }

    // ---------------------------------------------------------------- clipboard

    private ClipboardManager clipboard() {
        return (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
    }

    private void doCopy() {
        int s = selStart();
        int e = selEnd();
        if (s == e) {
            announce(S.t(this, "متنی انتخاب نشده است", "Nothing is selected"));
            return;
        }
        String text = editor.getText().subSequence(s, e).toString();
        clipboard().setPrimaryClip(ClipData.newPlainText("text", text));
        announce(S.t(this, "کپی شد", "Copied"));
    }

    private void doCut() {
        int s = selStart();
        int e = selEnd();
        if (s == e) {
            announce(S.t(this, "متنی انتخاب نشده است", "Nothing is selected"));
            return;
        }
        Editable ed = editor.getText();
        String text = ed.subSequence(s, e).toString();
        clipboard().setPrimaryClip(ClipData.newPlainText("text", text));
        ed.delete(s, e);
        editor.setSelection(Math.min(s, editor.length()));
        endSelectionMode();
        updateLabel();
        announce(S.t(this, "برش انجام شد", "Cut"));
    }

    private void doPaste() {
        ClipData clip = clipboard().getPrimaryClip();
        if (clip == null || clip.getItemCount() == 0) {
            announce(S.t(this, "حافظه موقت خالی است", "Clipboard is empty"));
            return;
        }
        CharSequence text = clip.getItemAt(0).coerceToText(this);
        if (text == null || text.length() == 0) {
            announce(S.t(this, "حافظه موقت خالی است", "Clipboard is empty"));
            return;
        }
        int s = selStart();
        int e = selEnd();
        editor.getText().replace(s, e, text);
        editor.setSelection(Math.min(s + text.length(), editor.length()));
        endSelectionMode();
        updateLabel();
        announce(S.t(this, "الصاق شد", "Pasted"));
    }

    private void doSelectAll() {
        int len = editor.length();
        if (len == 0) {
            announce(S.t(this, "متنی وجود ندارد", "There is no text"));
            return;
        }
        anchor = 0;
        editor.setSelection(0, len);
        endSelectionMode();
        updateLabel();
        announce(S.t(this, "همه متن انتخاب شد، " + len + " حرف",
                "All text selected, " + len + " characters"));
    }
}
