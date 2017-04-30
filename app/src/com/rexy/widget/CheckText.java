package com.rexy.widget;

import android.content.Context;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.TextView;


public class CheckText extends TextView implements Checkable {
    private static int FLAG_CHECK_ABLE = 1, FLAG_CLICK_CHECK = 2;
    protected boolean mChecked = false;
    private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};
    protected boolean mCheckEnable=true;
    protected boolean mClickCheckEnable =false;
    CharSequence mTextOn, mTextOff;


    public CheckText(Context context) {
        super(context);
    }

    public CheckText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CheckText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setTextState(CharSequence textOn, CharSequence textOff) {
        if (!TextUtils.isEmpty(textOn) && !TextUtils.isEmpty(textOff)) {
            mTextOn = textOn;
            mTextOff = textOff;
            setText(mChecked ? mTextOn : mTextOff);
        }
    }

    public void setCheckAble(boolean checkable) {
        mCheckEnable=checkable;
    }

    public boolean isCheckAble() {
        return mCheckEnable;
    }

    public void setClickCheckAble(boolean checkable) {
        mClickCheckEnable =checkable;
    }

    public boolean isClickCheckAble() {
        return mClickCheckEnable;
    }

    @Override
    public boolean isChecked() {
        return mChecked;
    }


    public void setChecked(boolean checked) {
        if (mCheckEnable) {
            if (mChecked != checked) {
                mChecked = checked;
                if (!TextUtils.isEmpty(mTextOn)) {
                    setText(checked ? mTextOn : mTextOff);
                }
                refreshDrawableState();
            }
        }
    }

    @Override
    public boolean performClick() {
        /*
         * XXX: These are tiny, need some surrounding 'expanded touch area',
		 * which will need to be implemented in Button if we only override
		 * performClick()
		 */
        /* When clicked, toggle the state */
        if (mClickCheckEnable) {
            toggle();
        }
        return super.performClick();
    }

    @Override
    public void toggle() {
        setChecked(!mChecked);
    }

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
        if (isChecked()) {
            mergeDrawableStates(drawableState, CHECKED_STATE_SET);
        }
        return drawableState;
    }
}
