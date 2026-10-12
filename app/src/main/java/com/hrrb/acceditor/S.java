package com.hrrb.acceditor;

import android.content.Context;
import android.content.SharedPreferences;

/** Shared settings and tiny two-language helper. */
final class S {
    static final String PREFS = "settings";
    static final String KEY_LANG = "lang";

    private S() {
    }

    static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    static boolean fa(Context c) {
        return "fa".equals(prefs(c).getString(KEY_LANG, "fa"));
    }

    /** Returns the Persian text when the app language is Persian, otherwise the English text. */
    static String t(Context c, String fa, String en) {
        return fa(c) ? fa : en;
    }
}
