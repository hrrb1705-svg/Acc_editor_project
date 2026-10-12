package com.hrrb.acceditor;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

/** EditText that reports every cursor or selection change. */
public class CursorEditText extends EditText {

    public interface SelectionListener {
        void onSelectionChanged(int start, int end);
    }

    private SelectionListener listener;

    public CursorEditText(Context context) {
        super(context);
    }

    public CursorEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CursorEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setSelectionListener(SelectionListener l) {
        this.listener = l;
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        if (listener != null) {
            listener.onSelectionChanged(selStart, selEnd);
        }
    }
}
