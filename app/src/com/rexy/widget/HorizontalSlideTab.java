package com.rexy.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.Checkable;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.rexy.pagescrollview.R;

import java.util.Locale;


public class HorizontalSlideTab extends HorizontalScrollView {

    public interface ITabProvider {
        CharSequence getPageTitle(int position);

        Object getItem(int pos);

        int getCount();
    }

    public interface ViewTabProvider extends ITabProvider {
        int getItemViewType(int pos);

        View getView(Context cx, LayoutInflater lf, int pos);
    }

    public interface ITabClickEvent {
        boolean onTabClicked(HorizontalSlideTab parent, View cur, int curPos, View pre, int prePos);
    }


    private static int TAB_INDEX = R.id.key_special_0;
    // @formatter:off
    private static final int[] ATTRS = new int[]{android.R.attr.textSize,
            android.R.attr.textColor};
    // @formatter:on


    private int mTabItemCount; //tab item 数量。
    private LinearLayout mTabContainer; //tab item 的直接容器。

    /**
     * 以下和
     */
    private int mCurrentPosition = 0; //当前选中的tab 索引。
    private float mCurrentPositionOffset = 0f;//选中tab 的偏移量子。
    private int mLastScrollX = 0;
    private int scrollOffset = 52;


    /**
     * 以下是设置tab item 的 LayoutParams 布局。
     */
    private boolean mShouldItemExpand = true;
    private LinearLayout.LayoutParams mDefaultItemLayoutParams;
    private LinearLayout.LayoutParams mExpandedItemLayoutParams;

    /**
     * 以下是设置tab item 的最小padding 值。
     */
    private int mItemMinPaddingHorizonal = 10;
    private int mItemMinPaddingTop = 0;
    private int mItemMinPaddingBottom = 0;

    /**
     * tab item 的背景
     */
    private int mItemBackgroundFirst = 0, mItemBackground = 0, mItemBackgroundLast = 0, mItemBackgroundFull = 0;

    /**
     * 如果item 是 TextView 会应用以下属性。
     */

    private boolean mTextAllCaps = false;
    private Typeface mTextTypeFace = null;
    private int mTextTypefaceStyle = Typeface.NORMAL;
    private int mTextSize = 14;
    private int mTextColor = 0xFF666666;
    private int mTextColorResId = 0;

    private Paint rectPaint;
    private Paint dividerPaint;


    /**
     * item 之间垂直分割线。
     */
    private int mDividerWidth = 1;
    private int mDividerPadding = 6;
    private int mDividerColor = 0x1A000000;


    /**
     * 选中item 底部指示线。
     */
    private int mIndicatorHeight = 2;
    private int mIndicatorColor = 0xffff9500;

    /**
     * 底部水平分界线
     */
    private int mBottomLineHeight = 0;
    private int mBottomLineColor = 0x1A000000;

    /**
     * 顶部水平分界线。
     */
    private int mTopLineHeight = 0;
    private int mTopLineColor = 0xffd8e2e9;

    private Locale mLocalInfo;
    private boolean mAutoCheckState = true;
    private View mPreCheckView = null;


    //c
    private ViewPager mViewPager = null;
    private final PageListener mViewPageListener = new PageListener();
    public OnPageChangeListener mDelegatePageListener;

    protected ITabProvider mITabProvider = null;
    protected ITabClickEvent mTabClick = null;
    private OnClickListener mTabItemClick = new OnClickListener() {
        @Override
        public void onClick(View view) {
            Object tag = view.getTag(TAB_INDEX);
            int cur = (tag instanceof Integer) ? (Integer) tag : mCurrentPosition;
            int pre = mCurrentPosition;
            boolean handled = mTabClick == null ? false : mTabClick.onTabClicked(HorizontalSlideTab.this, view, cur, mPreCheckView, pre);
            handTabClick(cur, pre, handled);
        }
    };

    protected void handTabClick(int cur, int pre, boolean handled) {
        if (!handled) {
            if (cur != pre) {
                if (mViewPager != null) {
                    mViewPager.setCurrentItem(cur);
                } else {
                    setSelectedTab(cur, false, false);
                }
            }
        } else {
            mCurrentPosition = cur;
        }
    }

    public HorizontalSlideTab(Context context) {
        this(context, null);
    }

    public HorizontalSlideTab(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalSlideTab(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setFillViewport(true);
        setWillNotDraw(false);

        mTabContainer = new LinearLayout(context);
        mTabContainer.setOrientation(LinearLayout.HORIZONTAL);
        mTabContainer.setGravity(Gravity.CENTER_VERTICAL);
        addView(mTabContainer);

        DisplayMetrics dm = getResources().getDisplayMetrics();

        scrollOffset = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, scrollOffset,
                dm);
        mIndicatorHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                mIndicatorHeight, dm);
        mTopLineHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                mTopLineHeight, dm);
        mBottomLineHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                mBottomLineHeight, dm);
        mDividerPadding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                mDividerPadding, dm);
        mItemMinPaddingHorizonal = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mItemMinPaddingHorizonal, dm);
        mItemMinPaddingTop = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mItemMinPaddingTop, dm);
        mItemMinPaddingBottom = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mItemMinPaddingBottom, dm);
        mDividerWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mDividerWidth,
                dm);
        mTextSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, mTextSize, dm);
        // get system attrs (android:textSize and android:textColor)
        TypedArray a = context.obtainStyledAttributes(attrs, ATTRS);

        mTextSize = a.getDimensionPixelSize(0, mTextSize);
        mTextColor = a.getColor(1, mTextColor);

        a.recycle();

        // get custom attrs

        a = context.obtainStyledAttributes(attrs, R.styleable.HorizontalSlideTab);
        mItemBackground = a.getResourceId(R.styleable.HorizontalSlideTab_tabItemBackground,
                mItemBackground);
        mItemBackgroundFirst = a.getResourceId(R.styleable.HorizontalSlideTab_tabItemBackgroundFirst,
                mItemBackgroundFirst);
        mItemBackgroundLast = a.getResourceId(R.styleable.HorizontalSlideTab_tabItemBackgroundLast,
                mItemBackgroundLast);
        mItemBackgroundFull = a.getResourceId(R.styleable.HorizontalSlideTab_tabItemBackgroundFull,
                mItemBackgroundFull);


        mIndicatorColor = a.getColor(R.styleable.HorizontalSlideTab_tabIndicatorColor,
                mIndicatorColor);
        mIndicatorHeight = a.getDimensionPixelSize(
                R.styleable.HorizontalSlideTab_tabIndicatorHeight, mIndicatorHeight);


        mTopLineColor = a.getColor(R.styleable.HorizontalSlideTab_tabTopLineColor,
                mTopLineColor);
        mTopLineHeight = a.getDimensionPixelSize(
                R.styleable.HorizontalSlideTab_tabTopLineHeight, mTopLineHeight);

        mBottomLineColor = a.getColor(R.styleable.HorizontalSlideTab_tabBottomLineColor,
                mBottomLineColor);
        mBottomLineHeight = a.getDimensionPixelSize(
                R.styleable.HorizontalSlideTab_tabBottomLineHeight, mBottomLineHeight);


        mDividerColor = a.getColor(R.styleable.HorizontalSlideTab_tabItemDividerColor, mDividerColor);
        mDividerWidth = a.getDimensionPixelSize(R.styleable.HorizontalSlideTab_tabItemDividerWidth, mDividerWidth);
        mDividerPadding = a.getDimensionPixelSize(
                R.styleable.HorizontalSlideTab_tabItemDividerPadding, mDividerPadding);


        mItemMinPaddingHorizonal = a.getDimensionPixelSize(
                R.styleable.HorizontalSlideTab_tabItemMinPaddingHorizonal, mItemMinPaddingHorizonal);
        mItemMinPaddingTop = a.getDimensionPixelSize(
                R.styleable.HorizontalSlideTab_tabItemMinPaddingTop, mItemMinPaddingHorizonal);
        mItemMinPaddingBottom = a.getDimensionPixelSize(
                R.styleable.HorizontalSlideTab_tabItemMinPaddingBottom, mItemMinPaddingHorizonal);

        mTextAllCaps = a.getBoolean(R.styleable.HorizontalSlideTab_tabItemTextCaps, mTextAllCaps);
        mTextColorResId = a.getResourceId(R.styleable.HorizontalSlideTab_tabItemTextColor,
                mTextColorResId);
        mShouldItemExpand = a
                .getBoolean(R.styleable.HorizontalSlideTab_tabItemShouldExpand, mShouldItemExpand);
        scrollOffset = a.getDimensionPixelSize(R.styleable.HorizontalSlideTab_tabScrollOffset,
                scrollOffset);

        a.recycle();

        rectPaint = new Paint();
        rectPaint.setAntiAlias(true);
        rectPaint.setStyle(Style.FILL);

        dividerPaint = new Paint();
        dividerPaint.setAntiAlias(true);
        dividerPaint.setStrokeWidth(mDividerWidth);

        mDefaultItemLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.MATCH_PARENT);
        mExpandedItemLayoutParams = new LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1.0f);

        if (mLocalInfo == null) {
            mLocalInfo = getResources().getConfiguration().locale;
        }
    }

    public int getTabItemCount() {
        if (mITabProvider != null) {
            return mITabProvider.getCount();
        }
        if (mViewPager != null && mViewPager.getAdapter() != null) {
            return mViewPager.getAdapter().getCount();
        }
        return 0;
    }

    public LinearLayout getTabContainer() {
        return mTabContainer;
    }

    public ITabProvider getTabProvider() {
        if (mITabProvider != null) {
            return mITabProvider;
        }
        if (mViewPager != null && mViewPager.getAdapter() instanceof ITabProvider) {
            return (ITabProvider) mViewPager.getAdapter();
        }
        return null;
    }

    public void setViewPager(ViewPager pager) {
        mViewPager = pager;
        PagerAdapter adp = pager == null ? null : pager.getAdapter();
        if (adp != null) {
            if (adp instanceof ITabProvider) {
                mITabProvider = (ITabProvider) adp;
            }
            pager.setOnPageChangeListener(mViewPageListener);
        }
        notifyDataSetChanged();
    }

    public void setTabProvider(ITabProvider provider, int currentPosition) {
        mITabProvider = provider;
        this.mCurrentPosition = currentPosition;
        notifyDataSetChanged();
    }

    public void setTabClickListener(ITabClickEvent l) {
        mTabClick = l;
    }

    public void setOnPageChangeListener(OnPageChangeListener listener) {
        this.mDelegatePageListener = listener;
    }


    public void notifyDataSetChanged() {
        mTabContainer.removeAllViews();
        mTabItemCount = getTabItemCount();
        boolean accessToTabProvider = mITabProvider != null;
        boolean accessToViewPage = mViewPager != null;
        boolean isViewTab = false;
        if (!accessToTabProvider && !accessToViewPage) {
            return;
        } else {
            isViewTab = (accessToTabProvider && mITabProvider instanceof ViewTabProvider);
        }
        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (int i = 0; i < mTabItemCount; i++) {
            if (isViewTab) {
                addTab(i, ((ViewTabProvider) mITabProvider).getView(getContext(), inflater, i));
            } else {
                CharSequence tabLable = accessToTabProvider ? (mITabProvider.getPageTitle(i)) : (mViewPager.getAdapter().getPageTitle(i));
                addTextTab(i, tabLable);
            }
        }
        updateTabStyles();
        getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
            @SuppressWarnings("deprecation")
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                } else {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                }
                if (mViewPager != null) {
                    mCurrentPosition = mViewPager.getCurrentItem();
                }
                int n = mTabContainer.getChildCount();
                if (mCurrentPosition >= 0 && mCurrentPosition < n) {
                    mPreCheckView = mTabContainer.getChildAt(mCurrentPosition);
                    if (mPreCheckView instanceof Checkable && mPreCheckView.isEnabled()) {
                        ((Checkable) mPreCheckView).setChecked(true);
                    }
                    scrollToChild(mCurrentPosition, 0, false);
                } else {
                    scrollToChild(0, 0, false);
                }
            }
        });
    }

    private void addTextTab(final int position, CharSequence title) {
        CheckText tab = new CheckText(getContext());
        tab.setEnabled(true);
        tab.setText(title);
        tab.setGravity(Gravity.CENTER);
        tab.setSingleLine();
        tab.setIncludeFontPadding(false);
        addTab(position, tab);
    }

    private void addTab(final int position, View tab) {
        tab.setFocusable(true);
        tab.setTag(TAB_INDEX, position);
        tab.setOnClickListener(mTabItemClick);
        int left = Math.max(mItemMinPaddingHorizonal, tab.getPaddingLeft());
        int top = Math.max(mItemMinPaddingTop, tab.getPaddingTop());
        int right = Math.max(mItemMinPaddingHorizonal, tab.getPaddingRight());
        int bottom = Math.max(mItemMinPaddingBottom, tab.getPaddingBottom());
        tab.setPadding(left, top, right, bottom);
        mTabContainer.addView(tab, position, mShouldItemExpand ? mExpandedItemLayoutParams
                : mDefaultItemLayoutParams);
    }

    public void addTabItem(CharSequence title, boolean updateStyle) {
        addTextTab(mTabItemCount, title);
        mTabItemCount++;
        if (updateStyle) {
            updateTabStyles();
        }
    }

    private void updateTabStyles() {
        boolean hasMutiBackground = mItemBackgroundFirst != 0 && mItemBackgroundLast != 0;
        for (int i = 0; i < mTabItemCount; i++) {
            int backgroundRes = mItemBackground;
            View v = mTabContainer.getChildAt(i);
            if (hasMutiBackground) {
                if (i == 0) {
                    if (mTabItemCount == 1) {
                        if (mItemBackgroundFull != 0) {
                            backgroundRes = mItemBackgroundFull;
                        }
                    } else {
                        backgroundRes = mItemBackgroundFirst;
                    }
                } else if (i == mTabItemCount - 1) {
                    backgroundRes = mItemBackgroundLast;
                }
            }
            if (backgroundRes != 0) {
                v.setBackgroundResource(backgroundRes);
            }
            if (v instanceof TextView) {
                TextView tab = (TextView) v;
                tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, mTextSize);
                tab.setTypeface(mTextTypeFace, mTextTypefaceStyle);
                if (mTextColorResId != 0) {
                    tab.setTextColor(getContext().getResources().getColorStateList(mTextColorResId));
                } else {
                    tab.setTextColor(mTextColor);
                }
                // setAllCaps() is only available from API 14, so the upper case
                // is made manually if we are on a
                // pre-ICS-build
                if (mTextAllCaps) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                        tab.setAllCaps(true);
                    } else {
                        tab.setText(tab.getText().toString().toUpperCase(mLocalInfo));
                    }
                }
            }
        }

    }

    private void scrollToChild(int position, int offset, boolean anim) {

        if (mTabItemCount == 0) {
            return;
        }

        int newScrollX = mTabContainer.getChildAt(position).getLeft() + offset;

        if (position > 0 || offset > 0) {
            newScrollX -= scrollOffset;
        }

        if (newScrollX != mLastScrollX) {
            mLastScrollX = newScrollX;
            if (anim) {
                smoothScrollTo(newScrollX, 0);
            } else {
                scrollTo(newScrollX, 0);
            }
        }

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isInEditMode() || mTabItemCount == 0) {
            return;
        }
        final int height = getHeight();
        // draw indicator line
        if (mIndicatorHeight > 0) {
            rectPaint.setColor(mIndicatorColor);

            // default: line below current tab
            View currentTab = mTabContainer.getChildAt(mCurrentPosition);
            float lineLeft = currentTab.getLeft();
            float lineRight = currentTab.getRight();

            // if there is an offset, start interpolating left and right coordinates
            // between current and next tab
            if (mCurrentPositionOffset > 0f && mCurrentPosition < mTabItemCount - 1) {

                View nextTab = mTabContainer.getChildAt(mCurrentPosition + 1);
                final float nextTabLeft = nextTab.getLeft();
                final float nextTabRight = nextTab.getRight();

                lineLeft = (mCurrentPositionOffset * nextTabLeft + (1f - mCurrentPositionOffset)
                        * lineLeft);
                lineRight = (mCurrentPositionOffset * nextTabRight + (1f - mCurrentPositionOffset)
                        * lineRight);
            }

            canvas.drawRect(lineLeft, height - mIndicatorHeight, lineRight, height, rectPaint);

            // draw underline
        }

        if (mBottomLineHeight > 0) {
            rectPaint.setColor(mBottomLineColor);
            canvas.drawRect(0, height - mBottomLineHeight, mTabContainer.getWidth(), height, rectPaint);
        }
        if (mTopLineHeight > 0) {
            rectPaint.setColor(mTopLineColor);
            canvas.drawRect(0, 0, mTabContainer.getWidth(), mTopLineHeight, rectPaint);

        }
        // draw divider

        if (mDividerWidth > 0) {
            dividerPaint.setColor(mDividerColor);
            float dividerWidth = dividerPaint.getStrokeWidth();
            for (int i = 0; i < mTabItemCount - 1; i++) {
                View tab = mTabContainer.getChildAt(i);
                float linex = tab.getRight() + dividerWidth;
                canvas.drawLine(linex, mDividerPadding, linex,
                        height - mDividerPadding, dividerPaint);
            }
        }
    }

    private class PageListener implements OnPageChangeListener {

        @Override
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            callPageScrolled(position, positionOffset);
            if (mDelegatePageListener != null) {
                mDelegatePageListener.onPageScrolled(position, positionOffset, positionOffsetPixels);
            }
        }

        @Override
        public void onPageScrollStateChanged(int state) {
            callPageScrollStateChanged(state, mViewPager.getCurrentItem());
            if (mDelegatePageListener != null) {
                mDelegatePageListener.onPageScrollStateChanged(state);
            }
        }

        @Override
        public void onPageSelected(int position) {
            callPageSelected(position);
            if (mDelegatePageListener != null) {
                mDelegatePageListener.onPageSelected(position);
            }
        }
    }


    public void callPageScrolled(int position, float positionOffset) {
        mCurrentPosition = position;
        mCurrentPositionOffset = positionOffset;
        scrollToChild(position, (int) (positionOffset * mTabContainer.getChildAt(position)
                .getWidth()), false);
        invalidate();
    }

    public void callPageSelected(int position) {
        setSelectedTab(position, true);
    }

    public void callPageScrollStateChanged(int state, int viewPageItem) {
        if (state == ViewPager.SCROLL_STATE_IDLE) {
            if (viewPageItem != mCurrentPosition) {
                mCurrentPosition = viewPageItem;
                mCurrentPositionOffset = 0;
            }
            scrollToChild(viewPageItem, 0, false);
        }
    }

    public void setSelectedTab(int position, boolean fromViewPageListener, boolean animToCur) {
        setSelectedTab(position, fromViewPageListener);
        scrollToChild(mCurrentPosition, 0, animToCur);
    }

    protected void setSelectedTab(int position, boolean fromViewPageListener) {
        if (!fromViewPageListener) {
            mCurrentPosition = position;
            mCurrentPositionOffset = 0;
        }
        View v = mTabContainer.getChildAt(position);
        if (mPreCheckView == null || mPreCheckView != v) {
            if (mAutoCheckState) {
                if (mPreCheckView instanceof Checkable) {
                    ((Checkable) mPreCheckView).setChecked(false);
                }
            }
            mPreCheckView = v;
            if (v instanceof Checkable) {
                ((Checkable) v).setChecked(true);
            }
        }
        invalidate();
    }

    public boolean setCheckedAtPosition(int pos, boolean checked) {
        if (pos < 0) {
            pos = mCurrentPosition;
        }
        if (pos >= 0 && pos < getTabItemCount()) {
            View v = mTabContainer.getChildAt(pos);
            if (v instanceof Checkable) {
                Checkable cv = (Checkable) v;
                if (cv.isChecked() != checked) {
                    cv.setChecked(checked);
                    return true;
                }
            }
        }
        return false;
    }

    public int getSelectedPosition() {
        return mCurrentPosition;
    }

    public View getSelectedView() {
        if (mCurrentPosition >= 0 && mCurrentPosition < getTabItemCount()) {
            return mTabContainer.getChildAt(mCurrentPosition);
        }
        return null;
    }

    public void setIndicatorHeight(int indicatorLineHeightPx) {
        this.mIndicatorHeight = indicatorLineHeightPx;
        invalidate();
    }

    public void setIndicatorColor(int indicatorColor) {
        this.mIndicatorColor = indicatorColor;
        invalidate();
    }

    public void setIndicatorColorId(int resId) {
        this.mIndicatorColor = getResources().getColor(resId);
        invalidate();
    }

    public void setDividerWidth(int dividerWidth) {
        this.mDividerWidth = dividerWidth;
        invalidate();
    }

    public void setDividerPadding(int dividerPaddingPx) {
        this.mDividerPadding = dividerPaddingPx;
        invalidate();
    }

    public void setDividerColor(int dividerColor) {
        this.mDividerColor = dividerColor;
        invalidate();
    }

    public void setDividerColorId(int resId) {
        this.mDividerColor = getResources().getColor(resId);
        invalidate();
    }

    public void setTopLineHeight(int topLineHeightPx) {
        this.mTopLineHeight = topLineHeightPx;
        invalidate();
    }

    public void setTopLineColor(int color) {
        this.mTopLineColor = color;
        invalidate();
    }

    public void setTopLineColorId(int resId) {
        this.mTopLineColor = getResources().getColor(resId);
        invalidate();
    }

    public void setBottomLineHeight(int underlineHeightPx) {
        this.mBottomLineHeight = underlineHeightPx;
        invalidate();
    }

    public void setBottomLineColor(int bottomLineColor) {
        this.mBottomLineColor = bottomLineColor;
        invalidate();
    }

    public void setBottomLineColorId(int resId) {
        this.mBottomLineColor = getResources().getColor(resId);
        invalidate();
    }

    public void setScrollOffset(int scrollOffsetPx) {
        this.scrollOffset = scrollOffsetPx;
        invalidate();
    }

    public void setShouldItemExpand(boolean shouldItemExpand) {
        this.mShouldItemExpand = shouldItemExpand;
        requestLayout();
    }

    public void setAutoCheckState(boolean autoCheckState) {
        this.mAutoCheckState = autoCheckState;
    }

    public boolean isAutoCheckState() {
        return mAutoCheckState;
    }

    public void setTextAllCaps(boolean textAllCaps) {
        this.mTextAllCaps = textAllCaps;
    }

    public void setTextSize(int textSizePx) {
        this.mTextSize = textSizePx;
        updateTabStyles();
    }

    public void setTextColor(int textColor) {
        this.mTextColor = textColor;
        updateTabStyles();
    }

    public void setTextColorId(int resId) {
        this.mTextColorResId = resId;
        updateTabStyles();
    }

    public void setTextTypeface(Typeface typeface, int style) {
        this.mTextTypeFace = typeface;
        this.mTextTypefaceStyle = style;
        updateTabStyles();
    }

    /**
     * {first middle last full} or {normal}
     *
     * @param resIds
     */
    public void setItemBackground(int... resIds) {
        int size = resIds == null ? 0 : resIds.length;
        if (size == 1) {
            this.mItemBackground = resIds[0];
        } else {
            if (size > 0) {
                this.mItemBackgroundFirst = resIds[0];
            }
            if (size > 1) {
                this.mItemBackground = resIds[1];
            }
            if (size > 2) {
                this.mItemBackgroundLast = resIds[2];
            }
            if (size > 3) {
                this.mItemBackgroundFull = resIds[4];
            }
        }
    }

    public void setItemPaddingHorizonal(int paddingHorizonalPixel) {
        this.mItemMinPaddingHorizonal = paddingHorizonalPixel;
    }

    public void setItemPaddingTop(int paddingTopPixel) {
        this.mItemMinPaddingTop = paddingTopPixel;
    }

    public void setItemPaddingBottom(int paddingBottomPixel) {
        this.mItemMinPaddingBottom = paddingBottomPixel;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        SavedState savedState = (SavedState) state;
        super.onRestoreInstanceState(savedState.getSuperState());
        mCurrentPosition = savedState.currentPosition;
        requestLayout();
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Parcelable superState = super.onSaveInstanceState();
        SavedState savedState = new SavedState(superState);
        savedState.currentPosition = mCurrentPosition;
        return savedState;
    }

    static class SavedState extends BaseSavedState {
        int currentPosition;

        public SavedState(Parcelable superState) {
            super(superState);
        }

        private SavedState(Parcel in) {
            super(in);
            currentPosition = in.readInt();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(currentPosition);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }


    public void autoScroll(int from, int to, Animation.AnimationListener l) {
        if (from >= 0 && to >= 0 && (from < mTabContainer.getChildCount() && to < mTabContainer.getChildCount())) {
            if (getAnimation() != null) {
                getAnimation().cancel();
                clearAnimation();
            }
            int maxX = mTabContainer.getMeasuredWidth() - getMeasuredWidth();
            int xfrom = Math.min(maxX, mTabContainer.getChildAt(from).getLeft());
            int xto = Math.min(maxX, mTabContainer.getChildAt(to).getLeft());
            int absDx = Math.abs(xfrom - xto);
            CustScrollAnima anim = new CustScrollAnima(xfrom, xto);
            int measureWidth = getMeasuredWidth();
            //modified by renzheng .may be measure width is zero.
            if (measureWidth == 0) {
                measureWidth = Math.max(getSuggestedMinimumWidth(), 1);
            }
            anim.setDuration(Math.min(4000, absDx * 1800 / measureWidth));
            anim.setInterpolator(new LinearInterpolator());
            anim.setAnimationListener(l);
            startAnimation(anim);
        }
    }

    class CustScrollAnima extends Animation {
        private int mScrollFrom, mScrollTo;

        public CustScrollAnima(int from, int to) {
            mScrollFrom = from;
            mScrollTo = to;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            int current = (int) (mScrollFrom + (mScrollTo - mScrollFrom) * interpolatedTime);
            scrollTo(current, 0);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (isEnabled()) {
            return super.dispatchTouchEvent(ev);
        } else {
            return true;
        }
    }
}
