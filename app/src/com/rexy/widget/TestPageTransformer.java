package com.rexy.widget;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by rexy on 17/4/12.
 */

public class TestPageTransformer implements ViewPager.PageTransformer {

    private static final float MIN_SCALE = 0.75f;
    private static final float MIN_ALPHA = 0.35f;
    private boolean isVertical = false, mAdjustTranslate = false;
    public TestPageTransformer(boolean isVertical){
        this.isVertical=isVertical;
    }

    public void setOrientation(boolean isVertical) {
        this.isVertical = isVertical;
    }
    public void transformPage(View view, float position) {
        int pageSize = isVertical?view.getHeight():view.getWidth();
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
                    if (isVertical) {
                        view.setTranslationY(horizontalMargin);
                    } else {
                        view.setTranslationX(horizontalMargin);
                    }
                } else {
                    if (isVertical) {
                        view.setTranslationY(-horizontalMargin);
                    } else {
                        view.setTranslationX(-horizontalMargin);
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
}
