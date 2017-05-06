package com.rexy.widget;

import android.view.View;

/**
 * Created by rexy on 17/4/12.
 */

public class TestPageTransformer implements PageScrollView.PageTransformer {

    private static final float MIN_SCALE = 0.7f;
    private static final float MIN_ALPHA = 0.3f;
    private boolean mAdjustTranslate = false;

    public void transformPage(View view, float position, boolean horizontal) {
        int pageSize = horizontal ? view.getWidth() : view.getHeight();
        if (position < -1) { // [-Infinity,-1)way off-screen to the left
            view.setAlpha(MIN_ALPHA);
            view.setScaleX(MIN_SCALE);
            view.setScaleY(MIN_SCALE);
        } else if (position <= 1) { // [-1,1]
            float percent = 1 - Math.abs(position);
            float scale = MIN_SCALE + (1 - MIN_SCALE) * percent;
            if (mAdjustTranslate) {
                float horizontalMargin = pageSize * (1 - scale) / 2;
                if (position > 0) {
                    if (horizontal) {
                        view.setTranslationX(horizontalMargin);
                    } else {
                        view.setTranslationY(horizontalMargin);
                    }
                } else {
                    if (horizontal) {
                        view.setTranslationX(-horizontalMargin);
                    } else {
                        view.setTranslationY(-horizontalMargin);
                    }
                }
            }
            view.setScaleX(scale);
            view.setScaleY(scale);
            view.setAlpha(MIN_ALPHA + (1 - MIN_ALPHA) * percent);
        } else { // (1,+Infinity]page is way off-screen to the right.
            view.setAlpha(MIN_ALPHA);
            view.setScaleX(MIN_SCALE);
            view.setScaleY(MIN_SCALE);
        }
    }

    @Override
    public void recoverTransformPage(View view, boolean horizontal) {
        view.setAlpha(1);
        view.setScaleX(1);
        view.setScaleY(1);
        if (mAdjustTranslate) {
            if (horizontal) {
                view.setTranslationX(0);
            } else {
                view.setTranslationY(0);
            }
        }
    }
}
