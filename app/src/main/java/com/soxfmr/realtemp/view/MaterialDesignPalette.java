package com.soxfmr.realtemp.view;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

import com.soxfmr.realtemp.R;

/**
 * Created by Soxfmr@gmail.com on 2016/4/10.
 */
public class MaterialDesignPalette extends RelativeLayout {

    public MaterialDesignPalette(Context context) {
        super(context);
        init();
    }

    public MaterialDesignPalette(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MaterialDesignPalette(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        ViewCompat.setElevation(this, getResources().getDimension(R.dimen.spacing_normal));
    }
}
