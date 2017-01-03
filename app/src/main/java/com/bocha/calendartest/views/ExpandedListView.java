package com.bocha.calendartest.views;

/**
 * Created by bob on 03.01.17.
 *
 * A ListView which expands every listItem at any time
 *
 */
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ListView;

public class ExpandedListView extends ListView {

    private android.view.ViewGroup.LayoutParams params;
    private int old_count = 0;

    public ExpandedListView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (getCount() != old_count) {
            old_count = getCount();
            params = getLayoutParams();
            if(getChildCount() != 0){
                params.height = getCount() * (old_count > 0 ? getChildAt(0).getHeight() : 0);
            }
            setLayoutParams(params);
        }

        super.onDraw(canvas);
    }

}
