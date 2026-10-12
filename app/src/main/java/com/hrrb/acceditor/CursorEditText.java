package com.hrrb.acceditor;

import android.content.Context;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityEvent;
import android.widget.EditText;

/**
 * EditText that reports every cursor or selection change and can hide its own
 * selection events from the screen reader, so that only the app's own speech is heard.
 */
public class CursorEditText extends EditText {

    public interface SelectionListener {
        void onSelectionChanged(int start, int end);
    }

    private SelectionListener listener;
    private boolean suppressSelectionEvents = false;

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

    public void setSuppressSelectionEvents(boolean suppress) {
        this.suppressSelectionEvents = suppress;
    }

    @Override
    protected void onSelectionChanged(int selStart, int selEnd) {
        super.onSelectionChanged(selStart, selEnd);
        if (listener != null) {
            listener.onSelectionChanged(selStart, selEnd);
        }
    }

    @Override
    public void sendAccessibilityEvent(int eventType) {
        if (suppressSelectionEvents && eventType == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
            return;
        }
        super.sendAccessibilityEvent(eventType);
    }

    @Override
    public void sendAccessibilityEventUnchecked(AccessibilityEvent event) {
        if (suppressSelectionEvents && event.getEventType() == AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED) {
            return;
        }
        super.sendAccessibilityEventUnchecked(event);
    }
}
