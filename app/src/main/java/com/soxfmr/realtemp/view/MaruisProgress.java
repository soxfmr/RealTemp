package com.soxfmr.realtemp.view;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.util.AttributeSet;

import com.github.lzyzsd.circleprogress.ArcProgress;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by Soxfmr@gmail.com on 2016/4/10.
 */
public class MaruisProgress extends ArcProgress {

    private HashMap<Integer, Integer> mColorMap;
    private Integer[] mColorRange;

    private int rangeIndex = -1;
    private final ArgbEvaluator mArgbEvaluator = new ArgbEvaluator();

    private long mDuration = 800;

    public MaruisProgress(Context context) {
        super(context);
        init();
    }

    public MaruisProgress(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MaruisProgress(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mColorMap = new HashMap<>();
    }

    public void clear() {
        mColorMap.clear();
        mColorRange = null;
    }

    public void addRangeColor(int progress, int color) {
        mColorMap.put(progress, color);

        int i = 0;
        mColorRange = new Integer[mColorMap.size()];
        for (Integer integer : mColorMap.keySet()) {
            mColorRange[i++] = integer;
        }
        Arrays.sort(mColorRange);
    }

    public long getDuration() {
        return mDuration;
    }

    public void setDuration(long duration) {
        this.mDuration = duration;
    }

    @Override
    public void setProgress(int progress) {
        if (progress > getMax()) {
            progress %= getMax();
        }

        if (mColorRange != null) {
            for (int i = 0, len = mColorRange.length; i < len; i++) {
                if (progress <= mColorRange[i] && rangeIndex != i) {
                    int color = mColorMap.get(mColorRange[i]);
                    changeColor(color);

                    rangeIndex = i;
                    break;
                }
            }
        }

        super.setProgress(progress);
    }

    private void changeColor(int color) {
        int currentColor = getFinishedStrokeColor();
        ObjectAnimator animator = ObjectAnimator.ofInt(this, "finishedStrokeColor", currentColor, color);
        animator.setEvaluator(mArgbEvaluator);
        animator.setDuration(mDuration);
        animator.start();
    }
}
