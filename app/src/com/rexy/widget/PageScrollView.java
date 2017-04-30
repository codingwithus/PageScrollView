package com.rexy.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewConfigurationCompat;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.Interpolator;
import android.widget.OverScroller;

import com.rexy.pagescrollview.R;

/**
 * TODO:功能说明
 *
 * @author: renzheng
 * @date: 2017-04-25 09:32
 */
public class PageScrollView extends ViewGroup {
    public static final int HORIZONTAL = 0;
    public static final int VERTICAL = 1;
    private static final int MAX_DURATION = 600;
    private static final int FLOAT_VIEW_SCROLL = 1;

    protected int mOrientation;
    protected int mGravity;
    protected int mMaxWidth = -1;
    protected int mMaxHeight = -1;
    protected int mMiddleMargin = 0;
    protected float mSizeFixedPercent = 0;
    protected boolean isPageViewStyle = false;


    protected int mFloatViewStart = -1;
    protected int mFloatViewEnd = -1;
    protected boolean isChildCenter = false;
    protected boolean mAttachLayouted = false;

    protected int mSwapFloatViewIndex = -1;
    protected int mFloatViewStartIndex = -1;
    protected int mFloatViewEndIndex = -1;

    protected int mFloatViewStartMode = 0;
    protected int mFloatViewEndMode = 0;

    //目前只保证 pageHeader pageFooter 在item View 添加完后再设置。
    protected View mPageHeaderView;
    protected View mPageFooterView;

    private int mContentWidth;
    private int mContentHeight;


    int mTouchSlop;
    int mMinDistance;
    int mMinimumVelocity;
    int mMaximumVelocity;
    private int mOverFlingDistance;

    int mCurrItem = 0;
    int mPrevItem = -1;
    int mVirtualCount = 0;
    int mScrollState = ViewPager.SCROLL_STATE_IDLE;
    boolean mIsBeingDraged = false;
    boolean mScrollerUsed = false;
    boolean mNeedResolveFloatOffset = false;

    PointF mPointDown = new PointF();
    PointF mPointLast = new PointF();
    //index,offset,duration,center
    Rect mScrollInfo = new Rect(-1, -1, -1, -1);
    VelocityTracker mVelocityTracker = null;
    OverScroller mScrollerScrollView = null;
    OverScroller mScrollerPageView = null;
    private ViewPager.PageTransformer mPageTransformer;
    private ViewPager.OnPageChangeListener mPageListener = null;

    public PageScrollView(Context context) {
        super(context);
        init(context, null);
    }

    public PageScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PageScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    public PageScrollView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    private void print(CharSequence msg) {
        Log.d("rexy_pagescroll", String.valueOf(msg));
    }

    private void init(Context context, AttributeSet attributeSet) {
        final ViewConfiguration configuration = ViewConfiguration.get(context);
        final float density = context.getResources().getDisplayMetrics().density;
        mTouchSlop = ViewConfigurationCompat.getScaledPagingTouchSlop(configuration);
        mMinimumVelocity = (int) (400 * density);
        mMinDistance = (int) (25 * density);
        mMaximumVelocity = configuration.getScaledMaximumFlingVelocity();
        mOverFlingDistance = configuration.getScaledOverflingDistance() * 2;

        TypedArray attr = attributeSet == null ? null : context.obtainStyledAttributes(attributeSet, R.styleable.PageScrollView);
        if (attr != null) {
            mGravity = attr.getInt(R.styleable.PageScrollView_android_gravity, mGravity);
            mMaxWidth = attr.getDimensionPixelSize(R.styleable.PageScrollView_android_maxWidth, mMaxWidth);
            mMaxHeight = attr.getDimensionPixelSize(R.styleable.PageScrollView_android_maxHeight, mMaxHeight);
            mOrientation = attr.getInt(R.styleable.PageScrollView_android_orientation, mOrientation);
            mMiddleMargin = attr.getDimensionPixelSize(R.styleable.PageScrollView_middleMargin, mMiddleMargin);
            mSizeFixedPercent = attr.getFloat(R.styleable.PageScrollView_sizeFixedPercent, mSizeFixedPercent);
            isPageViewStyle = attr.getBoolean(R.styleable.PageScrollView_pageViewStyle, isPageViewStyle);
            mFloatViewStart = attr.getInt(R.styleable.PageScrollView_floatViewStart, mFloatViewStart);
            mFloatViewEnd = attr.getInt(R.styleable.PageScrollView_floatViewEnd, mFloatViewEnd);
            isChildCenter = attr.getBoolean(R.styleable.PageScrollView_childCenter, isChildCenter);
            mOverFlingDistance = attr.getDimensionPixelSize(R.styleable.PageScrollView_overFlingDistance, mOverFlingDistance);
        }
    }

    public int getOrientation() {
        return mOrientation;
    }

    public void setOrientation(int orientation) {
        if (mOrientation != orientation && (orientation == HORIZONTAL || orientation == VERTICAL)) {
            mOrientation = orientation;
            if (!isPageViewStyle) {
                boolean oldHorizontal = mOrientation == VERTICAL;
                int scrollLength = oldHorizontal ? getScrollX() : getScrollY();
                mCurrItem = resolveScrollViewFirstViewIndex(scrollLength, oldHorizontal);
                resetFloatViewOffset(mFloatViewStartIndex, oldHorizontal);
                resetFloatViewOffset(mFloatViewEndIndex, oldHorizontal);
                mFloatViewStartIndex = -1;
                mSwapFloatViewIndex = -1;
                mFloatViewStartMode = 0;
                mFloatViewEndIndex = -1;
                mFloatViewEndMode = 0;
            }
            mScrollInfo.set(mCurrItem, 0, 0, isPageViewStyle ? 1 : 0);
            scrollTo(0, 0);
            mAttachLayouted = false;
            requestLayout();
        }
    }

    public int getGravity() {
        return mGravity;
    }

    public void setGravity(int gravity) {
        if (mGravity != gravity) {
            mGravity = gravity;
            requestLayout();
        }
    }

    public int getMaxWidth() {
        return mMaxWidth;
    }

    public void setMaxWidth(int maxWidth) {
        if (mMaxWidth != maxWidth) {
            mMaxWidth = maxWidth;
            requestLayout();
        }
    }

    public int getMaxHeight() {
        return mMaxHeight;
    }

    public void setMaxHeight(int maxHeight) {
        if (mMaxHeight != maxHeight) {
            mMaxHeight = maxHeight;
            requestLayout();
        }
    }

    public int getMiddleMargin() {
        return mMiddleMargin;
    }

    public void setMiddleMargin(int middleMargin) {
        if (mMiddleMargin != middleMargin) {
            mMiddleMargin = middleMargin;
            requestLayout();
        }
    }

    public int getFloatViewStart() {
        return mFloatViewStart;
    }

    public void setFloatViewStart(int floatStartIndex) {
        if (mFloatViewStart != floatStartIndex) {
            resetFloatViewOffset(mFloatViewStartIndex, mOrientation == HORIZONTAL);
            mFloatViewStart = floatStartIndex;
            if (mFloatViewStart >= 0) {
                mNeedResolveFloatOffset = true;
            }
            mSwapFloatViewIndex = -1;
            mFloatViewStartIndex = -1;
            mFloatViewStartMode = 0;
            requestLayout();
        }
    }

    public int getFloatViewEnd() {
        return mFloatViewEnd;
    }

    public void setFloatViewEnd(int floatEndIndex) {
        if (mFloatViewEnd != floatEndIndex) {
            resetFloatViewOffset(mFloatViewEndIndex, mOrientation == HORIZONTAL);
            mFloatViewEnd = floatEndIndex;
            if (mFloatViewEnd >= 0) {
                mNeedResolveFloatOffset = true;
            }
            mFloatViewEndIndex = -1;
            mFloatViewEndMode = 0;
            requestLayout();
        }
    }

    public float getSizeFixedPercent() {
        return mSizeFixedPercent;
    }

    public void setSizeFixedPercent(float percent) {
        if (mSizeFixedPercent != percent && percent >= 0 && percent <= 0) {
            mSizeFixedPercent = percent;
            requestLayout();
        }
    }

    public void setOverflingDistance(int overflingDistance) {
        mOverFlingDistance = overflingDistance;
    }

    public int getOverflingDistance() {
        return mOverFlingDistance;
    }

    public boolean isPageViewStyle() {
        return isPageViewStyle;
    }

    public void setPageViewStyle(boolean pageViewStyle) {
        if (isPageViewStyle != pageViewStyle) {
            isPageViewStyle = pageViewStyle;
        }
    }

    public boolean isChildCenter() {
        return isChildCenter;
    }

    public void setChildCenter(boolean centerChild) {
        if (this.isChildCenter != centerChild) {
            this.isChildCenter = centerChild;
            if (mAttachLayouted) {
                requestLayout();
            }
        }
    }

    public View getPageHeaderView() {
        return mPageHeaderView;
    }

    public void setPageHeaderView(View headView) {
        if (mPageHeaderView != headView) {
            if (mPageHeaderView != null) {
                removeViewInLayout(mPageHeaderView);
            }
            mPageHeaderView = headView;
            if (mPageHeaderView != null) {
                addView(mPageHeaderView);
                mNeedResolveFloatOffset = true;
            }
            requestLayout();
        }
    }

    public View getPageFooterView() {
        return mPageFooterView;
    }

    public void setPageFooterView(View pageFooterView) {
        if (mPageFooterView != pageFooterView) {
            if (mPageFooterView != null) {
                removeViewInLayout(mPageFooterView);
            }
            mPageFooterView = pageFooterView;
            if (mPageFooterView != null) {
                addView(mPageFooterView);
                mNeedResolveFloatOffset = true;
            }
            requestLayout();
        }
    }

    public ViewPager.PageTransformer getPageTransformer() {
        return mPageTransformer;
    }

    public void setPageTransformer(ViewPager.PageTransformer transformer) {
        if (mPageTransformer != transformer) {
            boolean needResetTransformer = mPageTransformer != null;
            mPageTransformer = transformer;
            if (mAttachLayouted) {
                if (needResetTransformer && mPageTransformer == null) {
                    requestLayout();
                }
                if (mPageTransformer != null) {
                    resolvePageOffset(mOrientation == HORIZONTAL ? getScrollX() : getScrollY(), getPageItemCount());
                }
            }
        }
    }

    public ViewPager.OnPageChangeListener getPageListener() {
        return mPageListener;
    }

    public void setPageListener(ViewPager.OnPageChangeListener listener) {
        mPageListener = listener;
    }

    public boolean hasPageHeaderView() {
        return isChildNotGone(mPageHeaderView);
    }

    public boolean hasPageFooterView() {
        return isChildNotGone(mPageFooterView);
    }

    public int getCurrentItem() {
        return mCurrItem;
    }

    public int getPrevItem() {
        return mPrevItem;
    }

    public int getScrollState() {
        return mScrollState;
    }

    protected OverScroller getScroller() {
        if (isPageViewStyle) {
            if (mScrollerPageView == null) {
                mScrollerPageView = new OverScroller(getContext(), new Interpolator() {
                    @Override
                    public float getInterpolation(float t) {
                        t -= 1.0f;
                        return t * t * t * t * t + 1.0f;
                    }
                });
            }
            return mScrollerPageView;
        } else {
            if (mScrollerScrollView == null) {
                mScrollerScrollView = new OverScroller(getContext());
            }
            return mScrollerScrollView;
        }
    }

    protected boolean isChildNotGone(View child) {
        return child != null && child.getVisibility() != View.GONE && child.getParent() == PageScrollView.this;
    }

    protected View getVirtualChildAt(int index, boolean withoutGone) {
        int virtualCount = 0;
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if ((withoutGone && child.getVisibility() == View.GONE) || (child == mPageHeaderView || child == mPageFooterView))
                continue;
            if (virtualCount == index) {
                return child;
            }
            virtualCount++;
        }
        return null;
    }

    protected int getVirtualChildCount(boolean withoutGone) {
        int virtualCount = 0;
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if ((withoutGone && child.getVisibility() == View.GONE) || (child == mPageHeaderView || child == mPageFooterView))
                continue;
            virtualCount++;
        }
        return virtualCount;
    }

    public int getPageItemCount() {
        int pageCount = mVirtualCount;
        if (pageCount == 0) {
            pageCount = mVirtualCount = getVirtualChildCount(true);
        }
        return pageCount;
    }

    public View getPageItemView(int index) {
        View result = null;
        int pageCount = getPageItemCount();
        if (index >= 0 && index < pageCount) {
            result = getVirtualChildAt(index, true);
        }
        return result;
    }

    public int indexOfPageItemView(View view) {
        if (view != null) {
            int virtualCount = 0;
            final int count = getChildCount();
            for (int i = 0; i < count; i++) {
                final View child = getChildAt(i);
                if ((child.getVisibility() == View.GONE) || (child == mPageHeaderView || child == mPageFooterView))
                    continue;
                if (view == child) {
                    return virtualCount;
                }
                virtualCount++;
            }
        }
        return -1;
    }

    protected int translateMeasure(int spec, int padding, boolean limitedSize) {
        int specMode = MeasureSpec.getMode(spec);
        int specSize = MeasureSpec.getSize(spec);
        int size = limitedSize ? Math.max(0, specSize - padding) : Integer.MAX_VALUE;
        return MeasureSpec.makeMeasureSpec(size, specMode);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int childState = 0, headerExtraWidth = 0, headerExtraHeight = 0;
        final int paddingHorizontal = getPaddingLeft() + getPaddingRight();
        final int paddingVertical = getPaddingTop() + getPaddingBottom();
        boolean horizontal = mOrientation == HORIZONTAL;
        mContentWidth = 0;
        mContentHeight = 0;
        mVirtualCount = getVirtualChildCount(true);
        if (mPageHeaderView != null || mPageFooterView != null) {
            final int measureSpecWidth = translateMeasure(widthMeasureSpec, paddingHorizontal, true);
            final int measureSpecHeight = translateMeasure(heightMeasureSpec, paddingVertical, true);
            childState = measurePageExtraView(mPageHeaderView, measureSpecWidth, measureSpecHeight, horizontal) | childState;
            childState = measurePageExtraView(mPageFooterView, measureSpecWidth, measureSpecHeight, horizontal) | childState;
            if (mContentWidth > 0 || mContentHeight > 0) {
                if (horizontal) {
                    headerExtraHeight = mContentHeight;
                } else {
                    headerExtraWidth = mContentWidth;
                }
            }
        }
        if (mVirtualCount > 0) {
            final int measureSpecWidth = translateMeasure(widthMeasureSpec, paddingHorizontal + headerExtraWidth, !horizontal);
            final int measureSpecHeight = translateMeasure(heightMeasureSpec, paddingVertical + headerExtraHeight, horizontal);
            int fixedSize = 0;
            if (horizontal) {
                if (mSizeFixedPercent > 0 && mSizeFixedPercent <= 1) {
                    fixedSize = (int) (MeasureSpec.getSize(widthMeasureSpec) * mSizeFixedPercent);
                }
                childState = measureMiddleViewHorizontal(measureSpecWidth, measureSpecHeight, fixedSize) | childState;
            } else {
                if (mSizeFixedPercent > 0 && mSizeFixedPercent <= 1) {
                    fixedSize = (int) (MeasureSpec.getSize(heightMeasureSpec) * mSizeFixedPercent);
                }
                childState = measureMiddleViewVertical(measureSpecWidth, measureSpecHeight, fixedSize) | childState;
            }
        }
        int maxWidth = Math.max(mContentWidth + paddingHorizontal, getSuggestedMinimumWidth());
        int maxHeight = Math.max(mContentHeight + paddingVertical, getSuggestedMinimumWidth());
        if (mMaxWidth > 0 && maxWidth > mMaxWidth) {
            maxWidth = mMaxWidth;
        }
        if (mMaxHeight > 0 && maxHeight > mMaxHeight) {
            maxHeight = mMaxHeight;
        }
        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
                resolveSizeAndState(maxHeight, heightMeasureSpec, childState << MEASURED_HEIGHT_STATE_SHIFT));
        measureFloatViewStart(mFloatViewStart, mVirtualCount);
        measureFloatViewEnd(mFloatViewEnd, mVirtualCount);
    }

    protected void measureFloatViewStart(int itemIndex, int virtualCount) {
        mFloatViewStartIndex = -1;
        boolean measureNeeded = itemIndex >= 0 && itemIndex < virtualCount && virtualCount >= 2 && (mPageHeaderView == null && mPageFooterView == null);
        if (measureNeeded) {
            View view = getVirtualChildAt(itemIndex, true);
            measureNeeded = (getVirtualChildAt(itemIndex, false) == view);
            if (measureNeeded && floatViewScrollNeeded(view, mOrientation == HORIZONTAL)) {
                mFloatViewStartIndex = indexOfChild(view);
                mFloatViewStartMode = FLOAT_VIEW_SCROLL;
            }
        }
    }

    protected void measureFloatViewEnd(int itemIndex, int virtualCount) {
        mFloatViewEndIndex = -1;
        boolean measureNeeded = itemIndex >= 0 && itemIndex < virtualCount && virtualCount >= 2 && (mPageHeaderView == null && mPageFooterView == null);
        if (measureNeeded) {
            View view = getVirtualChildAt(itemIndex, true);
            measureNeeded = (getVirtualChildAt(itemIndex, false) == view);
            if (measureNeeded && floatViewScrollNeeded(view, mOrientation == HORIZONTAL)) {
                mFloatViewEndIndex = indexOfChild(view);
                mFloatViewEndMode = FLOAT_VIEW_SCROLL;
            }
        }
    }

    protected boolean floatViewScrollNeeded(View view, boolean horizontal) {
        boolean scrollOk = view != null;
        if (scrollOk) {
            int scrollRange = 0, viewSize;
            if (horizontal) {
                if (mContentWidth > 0) {
                    scrollRange = Math.max(0, mContentWidth - (getMeasuredWidth() - getPaddingLeft() - getPaddingRight()));
                }
                viewSize = view.getMeasuredWidth() + ((PageScrollView.LayoutParams) view.getLayoutParams()).getMarginHorizontal();
            } else {
                if (mContentHeight > 0) {
                    scrollRange = Math.max(0, mContentHeight - (getMeasuredHeight() - getPaddingTop() - getPaddingBottom()));
                }
                viewSize = view.getMeasuredHeight() + ((PageScrollView.LayoutParams) view.getLayoutParams()).getMarginVertical();
            }
            scrollOk = scrollRange >= viewSize;
        }
        return scrollOk;
    }

    protected int measureMiddleViewHorizontal(int widthMeasureSpec, int heightMeasureSpec, int fixedSize) {
        final int childCount = getChildCount();
        int childFixedWidthSpec = fixedSize <= 0 ? 0 : MeasureSpec.makeMeasureSpec(fixedSize, MeasureSpec.EXACTLY);
        int contentWidth = 0;
        int contentHeight = 0;
        int measuredCount = 0;
        int childState = 0;
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == View.GONE || (child == mPageHeaderView || child == mPageFooterView))
                continue;
            PageScrollView.LayoutParams params = (PageScrollView.LayoutParams) child.getLayoutParams();
            int childMarginHorizontal = params.getMarginHorizontal();
            int childMarginVertical = params.getMarginVertical();
            int childWidthSpec = childFixedWidthSpec == 0 ? getChildMeasureSpec(widthMeasureSpec, childMarginHorizontal, params.width) : childFixedWidthSpec;
            int childHeightSpec = getChildMeasureSpec(heightMeasureSpec, childMarginVertical, params.height);
            child.measure(childWidthSpec, childHeightSpec);
            if (mMiddleMargin > 0 && measuredCount > 0) {
                contentWidth += mMiddleMargin;
            }
            contentWidth += (child.getMeasuredWidth() + childMarginHorizontal);
            int itemHeight = child.getMeasuredHeight() + childMarginVertical;
            if (contentHeight < itemHeight) {
                contentHeight = itemHeight;
            }
            childState |= child.getMeasuredState();
            measuredCount++;
        }
        mContentWidth = Math.max(mContentWidth, contentWidth);
        mContentHeight += contentHeight;
        return childState;
    }

    protected int measureMiddleViewVertical(int widthMeasureSpec, int heightMeasureSpec, final int fixedSize) {
        final int childCount = getChildCount();
        int childFixedHeightSpec = fixedSize <= 0 ? 0 : MeasureSpec.makeMeasureSpec(fixedSize, MeasureSpec.EXACTLY);
        int contentWidth = 0;
        int contentHeight = 0;
        int measuredCount = 0;
        int childState = 0;
        for (int i = 0; i < childCount; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == View.GONE || (child == mPageHeaderView || child == mPageFooterView))
                continue;
            PageScrollView.LayoutParams params = (PageScrollView.LayoutParams) child.getLayoutParams();
            int childMarginHorizontal = params.getMarginHorizontal();
            int childMarginVertical = params.getMarginVertical();
            int childWidthSpec = getChildMeasureSpec(widthMeasureSpec, childMarginHorizontal, params.width);
            int childHeightSpec = childFixedHeightSpec == 0 ? getChildMeasureSpec(heightMeasureSpec, childMarginVertical, params.height) : childFixedHeightSpec;
            child.measure(childWidthSpec, childHeightSpec);
            if (mMiddleMargin > 0 && measuredCount > 0) {
                contentHeight += mMiddleMargin;
            }
            contentHeight += (child.getMeasuredHeight() + childMarginVertical);
            int itemWidth = child.getMeasuredWidth() + childMarginHorizontal;
            if (contentWidth < itemWidth) {
                contentWidth = itemWidth;
            }
            childState |= child.getMeasuredState();
            measuredCount++;
        }
        mContentWidth += contentWidth;
        mContentHeight = Math.max(mContentHeight, contentHeight);
        return childState;
    }

    protected int measurePageExtraView(View view, int widthMeasureSpec, int heightMeasureSpec, boolean horizontal) {
        int childState = 0;
        if (isChildNotGone(view)) {
            PageScrollView.LayoutParams params = (PageScrollView.LayoutParams) view.getLayoutParams();
            int childMarginHorizontal = params.getMarginHorizontal();
            int childMarginVertical = params.getMarginVertical();
            int extraMeasureWidthSpec = getChildMeasureSpec(widthMeasureSpec, childMarginHorizontal, params.width);
            int extraMeasureHeightSpec = getChildMeasureSpec(heightMeasureSpec, childMarginVertical, params.height);
            if (horizontal) {
                if (params.width == -1) {
                    extraMeasureWidthSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(extraMeasureWidthSpec), MeasureSpec.EXACTLY);
                }
                view.measure(extraMeasureWidthSpec, extraMeasureHeightSpec);
                int contentHeight = view.getMeasuredHeight() + childMarginVertical;
                mContentWidth = Math.max(mContentWidth, view.getMeasuredWidth() + childMarginHorizontal);
                if (contentHeight > 0) {
                    mContentHeight += contentHeight;
                }
            } else {
                if (params.height == -1) {
                    extraMeasureHeightSpec = MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(extraMeasureHeightSpec), MeasureSpec.EXACTLY);
                }
                view.measure(extraMeasureWidthSpec, extraMeasureHeightSpec);
                int contentWidth = view.getMeasuredWidth() + childMarginHorizontal;
                if (contentWidth > 0) {
                    mContentWidth += contentWidth;
                }
                mContentHeight = Math.max(mContentHeight, view.getMeasuredHeight() + childMarginVertical);
            }
            childState = view.getMeasuredState();
        }
        return childState;
    }

    protected int getContentStart(int containerStart, int containerEnd, int contentWillSize, int contentGravity, boolean horizontalDirection) {
        int start = containerStart;
        if (contentGravity != -1) {
            final int mask = horizontalDirection ? Gravity.HORIZONTAL_GRAVITY_MASK : Gravity.VERTICAL_GRAVITY_MASK;
            final int maskCenter = horizontalDirection ? Gravity.CENTER_HORIZONTAL : Gravity.CENTER_VERTICAL;
            final int maskEnd = horizontalDirection ? Gravity.RIGHT : Gravity.BOTTOM;
            final int okGravity = contentGravity & mask;
            if (maskCenter == okGravity) {
                start = containerStart + (containerEnd - containerStart - contentWillSize) / 2;
            } else if (maskEnd == okGravity) {
                start = containerEnd - contentWillSize;
            }
        }
        return start;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int left = getPaddingLeft();
        int right = getPaddingRight();
        int top = getPaddingTop();
        int bottom = getPaddingBottom();
        int width = r - l, height = b - t;
        int baseLeft = getContentStart(left, width - right, mContentWidth, mGravity, true);
        int baseTop = getContentStart(top, height - bottom, mContentHeight, mGravity, false);
        if (mOrientation == HORIZONTAL) {
            if (baseLeft < left) {
                baseLeft = left;
            }
            onLayoutHorizontal(baseLeft, baseTop, width - left - right);
        } else {
            if (baseTop < top) {
                baseTop = top;
            }
            onLayoutVertical(baseLeft, baseTop, height - top - bottom);
        }
        if (mAttachLayouted == false) {
            mAttachLayouted = true;
            if (mScrollInfo.left >= 0 || mPrevItem == -1) {
                scrollAfterLayout();
            }
            invalidate();
        } else {
            if (mNeedResolveFloatOffset) {
                mNeedResolveFloatOffset = false;
                boolean horizontal = mOrientation == HORIZONTAL;
                int scrollLength = horizontal ? getScrollX() : getScrollY();
                if (mFloatViewStartIndex >= 0 && mSwapFloatViewIndex < 0) {
                    mSwapFloatViewIndex = calculateSwapFirstFloatViewIndex(scrollLength, horizontal);
                }
                if (mFloatViewStartMode == FLOAT_VIEW_SCROLL || mFloatViewEndMode == FLOAT_VIEW_SCROLL) {
                    resolveFloatViewOffset(scrollLength, horizontal);
                }
                if (mPageHeaderView != null || mPageFooterView != null) {
                    resolvePageHeaderFooterOffset(scrollLength, horizontal);
                }
            }
        }
    }

    protected void onLayoutVertical(int baseLeft, int baseTop, int accessHeight) {
        int childLeft, childTop, childRight, childBottom;
        int middleWidth = mContentWidth;
        if (isChildNotGone(mPageHeaderView)) {
            PageScrollView.LayoutParams params = (PageScrollView.LayoutParams) mPageHeaderView.getLayoutParams();
            childTop = getPaddingTop() + Math.max(params.topMargin, (accessHeight - (mPageHeaderView.getMeasuredHeight() + params.getMarginVertical())) / 2);
            childBottom = childTop + mPageHeaderView.getMeasuredHeight();
            childLeft = baseLeft + params.leftMargin;
            childRight = childLeft + mPageHeaderView.getMeasuredWidth();
            mPageHeaderView.layout(childLeft, childTop, childRight, childBottom);
            baseLeft = childRight + params.rightMargin;
            middleWidth -= (mPageHeaderView.getMeasuredWidth() + params.getMarginHorizontal());
        }
        if (isChildNotGone(mPageFooterView)) {
            PageScrollView.LayoutParams params = (PageScrollView.LayoutParams) mPageFooterView.getLayoutParams();
            childTop = getPaddingTop() + Math.max(params.topMargin, (accessHeight - (mPageFooterView.getMeasuredHeight() + params.getMarginVertical())) / 2);
            childBottom = childTop + mPageFooterView.getMeasuredHeight();
            childRight = getWidth() - getPaddingRight() - params.rightMargin;
            childLeft = childRight - mPageFooterView.getMeasuredWidth();
            mPageFooterView.layout(childLeft, childTop, childRight, childBottom);
            middleWidth -= (mPageFooterView.getMeasuredWidth() + params.getMarginHorizontal());
        }

        final int count = getChildCount();
        final int baseRight = baseLeft + middleWidth;
        childTop = baseTop;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == View.GONE || (child == mPageHeaderView || child == mPageFooterView))
                continue;
            PageScrollView.LayoutParams params = (PageScrollView.LayoutParams) child.getLayoutParams();
            childTop += params.topMargin;
            childBottom = childTop + child.getMeasuredHeight();
            childLeft = getContentStart(baseLeft, baseRight, child.getMeasuredWidth() + params.getMarginHorizontal(), isChildCenter ? Gravity.CENTER : params.gravity, true);
            childRight = childLeft + child.getMeasuredWidth();
            child.layout(childLeft, childTop, childRight, childBottom);
            childTop = childBottom + params.bottomMargin;
            if (mMiddleMargin > 0) {
                childTop += mMiddleMargin;
            }
        }
    }

    protected void onLayoutHorizontal(int baseLeft, int baseTop, int accessWidth) {
        int childLeft, childTop, childRight, childBottom;
        int middleHeigh = mContentHeight;
        if (isChildNotGone(mPageHeaderView)) {
            PageScrollView.LayoutParams params = (PageScrollView.LayoutParams) mPageHeaderView.getLayoutParams();
            childLeft = getPaddingLeft() + Math.max(params.leftMargin, (accessWidth - (mPageHeaderView.getMeasuredWidth() + params.getMarginHorizontal())) / 2);
            childRight = childLeft + mPageHeaderView.getMeasuredWidth();
            childTop = baseTop + params.topMargin;
            childBottom = childTop + mPageHeaderView.getMeasuredHeight();
            mPageHeaderView.layout(childLeft, childTop, childRight, childBottom);
            baseTop = childBottom + params.bottomMargin;
            middleHeigh -= (mPageHeaderView.getMeasuredHeight() + params.getMarginVertical());
        }
        if (isChildNotGone(mPageFooterView)) {
            PageScrollView.LayoutParams params = (PageScrollView.LayoutParams) mPageFooterView.getLayoutParams();
            childLeft = getPaddingLeft() + Math.max(params.leftMargin, (accessWidth - (mPageFooterView.getMeasuredWidth() + params.getMarginHorizontal())) / 2);
            childRight = childLeft + mPageFooterView.getMeasuredWidth();
            childBottom = getHeight() - getPaddingBottom() - params.bottomMargin;
            childTop = childBottom - mPageFooterView.getMeasuredHeight();
            mPageFooterView.layout(childLeft, childTop, childRight, childBottom);
            middleHeigh -= (mPageFooterView.getMeasuredHeight() + params.getMarginVertical());
        }

        final int count = getChildCount();
        final int baseBottom = baseTop + middleHeigh;
        childLeft = baseLeft;
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            if (child.getVisibility() == View.GONE || (child == mPageHeaderView || child == mPageFooterView))
                continue;
            PageScrollView.LayoutParams params = (PageScrollView.LayoutParams) child.getLayoutParams();
            childLeft += params.leftMargin;
            childRight = childLeft + child.getMeasuredWidth();
            childTop = getContentStart(baseTop, baseBottom, child.getMeasuredHeight() + params.getMarginVertical(), isChildCenter ? Gravity.CENTER : params.gravity, false);
            childBottom = childTop + child.getMeasuredHeight();
            child.layout(childLeft, childTop, childRight, childBottom);
            childLeft = childRight + params.rightMargin;
            if (mMiddleMargin > 0) {
                childLeft += mMiddleMargin;
            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mAttachLayouted = false;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mAttachLayouted = false;
    }

    protected int calculateSwapFirstFloatViewIndex(int scrolled, boolean horizontal) {
        if (mFloatViewStartIndex >= 0) {
            int count = getChildCount(), baseLine;
            View view = getChildAt(mFloatViewStartIndex);
            baseLine = (horizontal ? view.getRight() : view.getBottom()) + scrolled;
            for (int i = mFloatViewStartIndex + 1; i < count; i++) {
                final View child = getChildAt(i);
                if (child.getVisibility() == View.GONE || (child == mPageHeaderView || child == mPageFooterView))
                    continue;
                if (horizontal) {
                    if (child.getRight() >= baseLine) {
                        return i;
                    }
                } else {
                    if (child.getBottom() >= baseLine) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }

    protected void resetFloatViewOffset(int realChildIndex, boolean horizontal) {
        View child = realChildIndex >= 0 ? getChildAt(realChildIndex) : null;
        if (child != null) {
            child.setTranslationX(0);
            child.setTranslationY(0);
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        boolean swapIndexEnable = mFloatViewStartIndex >= 0 && mSwapFloatViewIndex >= 0;
        if (swapIndexEnable && isChildrenDrawingOrderEnabled() == false) {
            setChildrenDrawingOrderEnabled(true);
        } else {
            if (swapIndexEnable == false) {
                setChildrenDrawingOrderEnabled(false);
            }
        }
        super.dispatchDraw(canvas);
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        int order = i;
        if (mFloatViewStartIndex >= 0 && mSwapFloatViewIndex >= 0) {
            if (mFloatViewStartIndex == i) {
                return mSwapFloatViewIndex;
            }
            if (i == mSwapFloatViewIndex) {
                return mFloatViewStartIndex;
            }
        }
        return order;
    }

    @Override
    protected int computeHorizontalScrollOffset() {
        return Math.max(0, super.computeHorizontalScrollOffset());
    }

    @Override
    protected int computeHorizontalScrollRange() {
        final int count = getChildCount();
        final int paddingLeft = getPaddingLeft();
        final int contentWidth = getWidth() - paddingLeft - getPaddingRight();
        if (count == 0) {
            return contentWidth;
        }
        int scrollRange = paddingLeft + mContentWidth;
        final int scrollX = getScrollX();
        final int overscrollRight = Math.max(0, scrollRange - contentWidth);
        if (scrollX < 0) {
            scrollRange -= scrollX;
        } else if (scrollX > overscrollRight) {
            scrollRange += scrollX - overscrollRight;
        }
        return scrollRange;
    }

    @Override
    protected int computeVerticalScrollOffset() {
        return Math.max(0, super.computeVerticalScrollOffset());
    }

    @Override
    protected int computeVerticalScrollRange() {
        final int count = getChildCount();
        final int paddingTop = getPaddingTop();
        final int contentHeight = getHeight() - paddingTop - getPaddingBottom();
        if (count == 0) {
            return contentHeight;
        }
        int scrollRange = paddingTop + mContentHeight;
        final int scrollY = getScrollY();
        final int overscrollBottom = Math.max(0, scrollRange - contentHeight);
        if (scrollY < 0) {
            scrollRange -= scrollY;
        } else if (scrollY > overscrollBottom) {
            scrollRange += scrollY - overscrollBottom;
        }
        return scrollRange;
    }

    public int getScrollRangeHorizontal() {
        int scrollRange = 0;
        if (mContentWidth > 0) {
            scrollRange = Math.max(0, mContentWidth - (getWidth() - getPaddingLeft() - getPaddingRight()));
        }
        return scrollRange;
    }

    protected int getScrollRangeVertical() {
        int scrollRange = 0;
        if (mContentHeight > 0) {
            scrollRange = Math.max(0, mContentHeight - (getHeight() - getPaddingTop() - getPaddingBottom()));
        }
        return scrollRange;
    }

    protected int computeCenterScrollOffset(int pageItemIndex, int pageItemCount) {
        if (pageItemIndex >= 0 && pageItemIndex < pageItemCount) {
            View view = getVirtualChildAt(pageItemIndex, true);
            int viewCenter, contentCenter, range;
            if (mOrientation == HORIZONTAL) {
                int paddingLeft = getPaddingLeft();
                contentCenter = paddingLeft + (getWidth() - (paddingLeft + getPaddingRight())) / 2;
                viewCenter = view.getLeft() + view.getWidth() / 2;
                range = getScrollRangeHorizontal();
            } else {
                int paddingTop = getPaddingTop();
                contentCenter = paddingTop + (getHeight() - (paddingTop + getPaddingBottom())) / 2;
                viewCenter = view.getTop() + view.getHeight() / 2;
                range = getScrollRangeVertical();
            }
            int result = viewCenter - contentCenter;
            return Math.max(0, Math.min(range, result));
        }
        return 0;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (getChildCount() == 0 || !isEnabled()) {
            return super.onTouchEvent(event);
        }
        if (event.getAction() == MotionEvent.ACTION_DOWN && event.getEdgeFlags() != 0) {
            return false;
        }
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);
        final int action = event.getAction() & MotionEventCompat.ACTION_MASK;
        if (action == MotionEvent.ACTION_MOVE) {
            handleTouchActionMove(event);
        } else {
            if (action == MotionEvent.ACTION_DOWN) {
                handleTouchActionDown(event);
            }
            if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
                handleTouchActionUp(event);
            }
        }
        return true;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (getChildCount() == 0 || !isEnabled()) {
            mIsBeingDraged = false;
        } else {
            final int action = ev.getAction() & MotionEventCompat.ACTION_MASK;
            if (action == MotionEvent.ACTION_MOVE) {
                handleTouchActionMove(ev);
            } else {
                if (action == MotionEvent.ACTION_DOWN) {
                    handleTouchActionDown(ev);
                }
                if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
                    handleTouchActionUp(ev);
                }
            }
        }
        return mIsBeingDraged;
    }

    private void handleTouchActionMove(MotionEvent ev) {
        float x = ev.getX(), y = ev.getY();
        if (mIsBeingDraged) {
            scrollDxDy((int) (mPointLast.x - x), (int) (mPointLast.y - y));
            mPointLast.set(x, y);
        } else {
            int dx = (int) (mPointDown.x - x), dy = (int) (mPointDown.y - y);
            int dxAbs = Math.abs(dx), dyAbs = Math.abs(dy);
            boolean dragged;
            if (mOrientation == HORIZONTAL) {
                dragged = dxAbs > mTouchSlop && (dxAbs * 0.6f) > dyAbs;
                dx = (dx > 0 ? mTouchSlop : -mTouchSlop) >> 2;
                dy = 0;
            } else {
                dragged = dyAbs > mTouchSlop && (dyAbs * 0.6f) > dxAbs;
                dy = (dy > 0 ? mTouchSlop : -mTouchSlop) >> 2;
                dx = 0;
            }
            if (dragged) {
                markAsWillDragged();
                scrollDxDy(dx, dy);
                mPointLast.set(x, y);
            }
        }
    }

    private void handleTouchActionUp(MotionEvent ev) {
        if (mIsBeingDraged) {
            mIsBeingDraged = false;
            mPointLast.x = ev.getX();
            mPointLast.y = ev.getY();
            int velocityX = 0, velocityY = 0;
            final VelocityTracker velocityTracker = mVelocityTracker;
            if (velocityTracker != null) {
                velocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
                velocityX = (int) velocityTracker.getXVelocity();
                velocityY = (int) velocityTracker.getYVelocity();
            }
            if (!flingToWhere((int) (mPointLast.x - mPointDown.x), (int) (mPointLast.y - mPointDown.y), -velocityX, -velocityY)) {
                markAsWillIdle();
            }
        }
        if (mVelocityTracker != null) {
            mVelocityTracker.recycle();
            mVelocityTracker = null;
        }
    }

    private void handleTouchActionDown(MotionEvent ev) {
        mPointDown.set(ev.getX(), ev.getY());
        mPointLast.set(mPointDown);
        if (mScrollerUsed) {
            OverScroller scroller = getScroller();
            scroller.computeScrollOffset();
            if (!scroller.isFinished()) {
                scroller.abortAnimation();
                markAsWillDragged();
            }
        }
    }

    private void markAsWillDragged() {
        mIsBeingDraged = true;
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(true);
        }
        setScrollState(ViewPager.SCROLL_STATE_DRAGGING);
    }

    private void markAsWillScroll() {
        mScrollerUsed = true;
        setScrollState(ViewPager.SCROLL_STATE_SETTLING);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    private void markAsWillIdle() {
        mScrollerUsed = false;
        setScrollState(ViewPager.SCROLL_STATE_IDLE);
    }

    public boolean isAttachLayouted() {
        return mAttachLayouted;
    }

    private boolean isFlingAllowed(int scroll, int scrollRange, int velocity) {
        return !(velocity == 0 || (velocity < 0 && scroll <= 0) || (velocity > 0 && scroll >= scrollRange));
    }

    private boolean flingToWhere(int movedX, int movedY, int velocityX, int velocityY) {
        int scroll, scrollRange, velocity, moved;
        boolean horizontal = mOrientation == HORIZONTAL, willScroll;
        if (horizontal) {
            scroll = getScrollX();
            scrollRange = getScrollRangeHorizontal();
            velocity = velocityX;
            moved = movedX;
        } else {
            scroll = getScrollY();
            scrollRange = getScrollRangeVertical();
            velocity = velocityY;
            moved = movedY;
        }
        if (willScroll = isFlingAllowed(scroll, scrollRange, velocity)) {
            if (isPageViewStyle) {
                int targetIndex = mCurrItem;
                int itemSize = horizontal ? getChildAt(mCurrItem).getWidth() : getChildAt(mCurrItem).getHeight();
                int containerSize = horizontal ? (getWidth() - getPaddingLeft() - getPaddingRight()) : (getHeight() - getPaddingTop() - getPaddingBottom());
                int absVelocity = velocity > 0 ? velocity : -velocity;
                int pageItemCount = getPageItemCount();
                if (Math.abs(moved) > mMinDistance) {
                    int halfItemSize = itemSize / 2;
                    if (absVelocity > mMinimumVelocity) {
                        if (velocity > 0 && mCurrItem < pageItemCount - 1 && (velocity / 10 - moved) > halfItemSize) {
                            targetIndex++;
                        }
                        if (velocity < 0 && mCurrItem > 0 && (moved - velocity / 10) > halfItemSize) {
                            targetIndex--;
                        }
                    } else {
                        if (moved > halfItemSize && mCurrItem > 0) {
                            targetIndex--;
                        }
                        if (moved < -halfItemSize && mCurrItem < pageItemCount - 1) {
                            targetIndex++;
                        }
                    }
                }
                int targetScroll = computeCenterScrollOffset(targetIndex, pageItemCount);
                if (willScroll = (targetScroll != scroll)) {
                    setCurrentItem(targetIndex);
                    int dScroll = targetScroll - scroll;
                    int duration = computeScrollDurationForItem(dScroll, absVelocity, itemSize, containerSize);
                    if (horizontal) {
                        getScroller().startScroll(scroll, getScrollY(), dScroll, 0, duration);
                    } else {
                        getScroller().startScroll(getScrollX(), scroll, 0, dScroll, duration);
                    }
                    markAsWillScroll();
                }
            } else {
                if (horizontal) {
                    getScroller().fling(scroll, getScrollY(), velocity, 0, 0, scrollRange, 0, 0, mOverFlingDistance, 0);
                } else {
                    getScroller().fling(getScrollX(), scroll, 0, velocity, 0, 0, 0, scrollRange, 0, mOverFlingDistance);
                }
                markAsWillScroll();
            }
        }
        return willScroll;
    }

    public void scrollTo(int index, int offset, int duration) {
        if (mAttachLayouted) {
            scrollTo(getPageItemView(index), offset, duration, false);
        } else {
            if (index >= 0) {
                mScrollInfo.set(index, offset, duration, 0);
            }
        }
    }

    public void scrollToCenter(int index, int offset, int duration) {
        if (mAttachLayouted) {
            scrollTo(getPageItemView(index), offset, duration, true);
        } else {
            if (index >= 0) {
                mScrollInfo.set(index, offset, duration, 1);
            }
        }
    }

    public void scrollTo(View child, int offset, int duration, boolean childCenter) {
        int pageIndex = indexOfPageItemView(child);
        if (pageIndex == -1) return;
        if (mAttachLayouted) {
            if (mScrollInfo.left >= 0) {
                mScrollInfo.set(-1, -1, -1, -1);
            }
            boolean horizontal = mOrientation == HORIZONTAL;
            int paddingStart, containerSize, childStart, childSize;
            int scroll, scrollRange, targetScroll;
            if (horizontal) {
                paddingStart = getPaddingLeft();
                containerSize = getWidth() - paddingStart - getPaddingRight();
                childStart = child.getLeft();
                childSize = child.getWidth();
                scroll = getScrollX();
                scrollRange = getScrollRangeHorizontal();
            } else {
                paddingStart = getPaddingTop();
                containerSize = getHeight() - paddingStart - getPaddingBottom();
                childStart = child.getTop();
                childSize = child.getHeight();
                scroll = getScrollY();
                scrollRange = getScrollRangeVertical();
            }
            targetScroll = childStart - paddingStart + offset + (childCenter ? (childSize - containerSize) / 2 : 0);
            targetScroll = Math.max(0, Math.min(scrollRange, targetScroll));
            if (targetScroll != scroll) {
                setCurrentItem(pageIndex);
                int dScroll = targetScroll - scroll;
                if (duration < 0) {
                    duration = computeScrollDuration(Math.abs(dScroll), 0, containerSize, MAX_DURATION);
                }
                if (duration == 0) {
                    if (horizontal) {
                        scrollTo(targetScroll, getScrollY());
                    } else {
                        scrollTo(getScrollX(), targetScroll);
                    }
                } else {
                    if (horizontal) {
                        getScroller().startScroll(scroll, getScrollY(), dScroll, 0, duration);
                    } else {
                        getScroller().startScroll(getScrollX(), scroll, 0, dScroll, duration);
                    }
                    markAsWillScroll();
                }
            }
        } else {
            mScrollInfo.set(pageIndex, offset, duration, childCenter ? 1 : 0);
        }
    }

    private void scrollAfterLayout() {
        boolean needAdjustPageTransform = mPrevItem == -1;
        if (mScrollInfo.left >= 0) {
            View pageView = getPageItemView(mScrollInfo.left);
            if (pageView != null) {
                scrollTo(pageView, mScrollInfo.top, mScrollInfo.right, 1 == mScrollInfo.bottom);
            }
            mScrollInfo.set(-1, -1, -1, -1);
        } else {
            if (mPrevItem == -1 && mVirtualCount > 0) {
                setCurrentItem(mCurrItem);
            }
        }
        if (needAdjustPageTransform) {
            mNeedResolveFloatOffset = false;
            boolean horizontal = mOrientation == HORIZONTAL;
            int scrollLength = horizontal ? getScrollX() : getScrollY();
            if (mFloatViewStartMode == FLOAT_VIEW_SCROLL || mFloatViewEndMode == FLOAT_VIEW_SCROLL) {
                resolveFloatViewOffset(scrollLength, horizontal);
            }
            if (mPageHeaderView != null || mPageFooterView != null) {
                resolvePageHeaderFooterOffset(scrollLength, horizontal);
            }
            if ((mPageListener != null || mPageTransformer != null)) {
                resolvePageOffset(scrollLength, getPageItemCount());
            }
        }
    }

    private void scrollDxDy(int scrollDx, int scrollDy) {
        if (mOrientation == HORIZONTAL) {
            int scrollWant = getScrollX() + scrollDx;
            int scrollRange = getScrollRangeHorizontal();
            if (scrollWant < 0) scrollWant = 0;
            if (scrollWant > scrollRange) scrollWant = scrollRange;
            scrollTo(scrollWant, getScrollY());
        } else {
            int scrollWant = getScrollY() + scrollDy;
            int scrollRange = getScrollRangeVertical();
            if (scrollWant < 0) scrollWant = 0;
            if (scrollWant > scrollRange) scrollWant = scrollRange;
            scrollTo(getScrollX(), scrollWant);
        }
    }

    protected int computeScrollDurationForItem(int willMoved, int absVelocity, int itemSized, int containerSize) {
        if (itemSized <= 0) {
            return computeScrollDuration(Math.abs(willMoved), absVelocity, containerSize, MAX_DURATION);
        }
        int duration;
        if (absVelocity > 0) {
            int halfWidth = containerSize / 2;
            float distanceRatio = distanceInfluenceForSnapDuration(Math.min(1f, Math.abs(willMoved) / (float) itemSized));
            float distance = halfWidth + halfWidth * distanceRatio;
            duration = 5 * Math.round(1000 * Math.abs(distance / absVelocity));
        } else {
            final float pageDelta = (float) Math.abs(willMoved) / itemSized;
            duration = (int) ((pageDelta + 1) * MAX_DURATION / 2);
        }
        return Math.min(duration, MAX_DURATION);
    }

    private int computeScrollDuration(int absWillMoved, int absVelocity, int containerSize, int maxDuration) {
        final int halfContainerSize = containerSize / 2;
        final float distanceRatio = Math.min(1.f, 1.f * absWillMoved / containerSize);
        final float distance = halfContainerSize + halfContainerSize *
                distanceInfluenceForSnapDuration(distanceRatio);
        final int duration;
        if (absVelocity > 0) {
            duration = 4 * Math.round(1000 * Math.abs(distance / absVelocity));
        } else {
            duration = (int) (((absWillMoved / (float) containerSize) + 1) * maxDuration / 2);
        }
        return Math.min(duration, maxDuration);
    }

    @Override
    public void computeScroll() {
        OverScroller scroller = mScrollerUsed ? getScroller() : null;
        if (scroller != null && scroller.computeScrollOffset()) {
            int oldX = getScrollX();
            int oldY = getScrollY();
            int x = scroller.getCurrX();
            int y = scroller.getCurrY();
            if (oldX != x || oldY != y) {
                scrollTo(x, y);
            }
            ViewCompat.postInvalidateOnAnimation(this);
        } else {
            if (mScrollerUsed) {
                markAsWillIdle();
            }
        }
    }

    private float distanceInfluenceForSnapDuration(float f) {
        f -= 0.5f; // center the values about 0.
        f *= 0.3f * Math.PI / 2.0f;
        return (float) Math.sin(f);
    }

    private void enableLayers(boolean enable) {
        final int childCount = getChildCount();
        final int layerType = enable ? ViewCompat.LAYER_TYPE_HARDWARE : ViewCompat.LAYER_TYPE_NONE;
        for (int i = 0; i < childCount; i++) {
            View child = getChildAt(i);
            if (child != mPageHeaderView && child != mPageFooterView) {
                ViewCompat.setLayerType(child, layerType, null);
            }
        }
    }

    private boolean setCurrentItem(int willItem) {
        if (mCurrItem != willItem || mPrevItem == -1) {
            int preItem = mCurrItem == willItem ? mPrevItem : mCurrItem;
            mPrevItem = mCurrItem;
            mCurrItem = willItem;
            print(String.format("selectChanged  $$$$:%d >>>>>>>>> %d", preItem, mCurrItem));
            if (mPageListener != null) {
                mPageListener.onPageSelected(willItem);
            }
            return true;
        }
        return false;
    }

    private boolean setScrollState(int newState) {
        if (mScrollState != newState) {
            int preState = mScrollState;
            mScrollState = newState;
            print(String.format("stateChanged  ####:%d >>>>>>>>> %d", preState, mScrollState));
            if (mScrollListener != null) {
                mScrollListener.onScrollStateChanged(mScrollState, preState);
            }
            if (mPageListener != null) {
                mPageListener.onPageScrollStateChanged(mScrollState);
            }
            if (mPageTransformer != null) {
                // PageTransformers can do complex things that benefit from hardware layers.
                enableLayers(newState != ViewPager.SCROLL_STATE_IDLE);
            }
            return true;
        }
        return false;
    }

    @Override
    protected void onScrollChanged(int l, int t, int ol, int ot) {
        super.onScrollChanged(l, t, ol, ot);
        if (mScrollListener != null) {
            mScrollListener.onScrollChanged(l, t, ol, ot);
        }
        mNeedResolveFloatOffset = false;
        boolean horizontal = mOrientation == HORIZONTAL;
        int scrollLength = horizontal ? l : t;
        if (mFloatViewStartIndex >= 0) {
            mSwapFloatViewIndex = calculateSwapFirstFloatViewIndex(scrollLength, horizontal);
        }
        if (mPageHeaderView != null || mPageFooterView != null) {
            resolvePageHeaderFooterOffset(scrollLength, horizontal);
        }
        if (mFloatViewStartMode == FLOAT_VIEW_SCROLL || mFloatViewEndMode == FLOAT_VIEW_SCROLL) {
            resolveFloatViewOffset(scrollLength, horizontal);
        }
        if (mPageListener != null || mPageTransformer != null) {
            resolvePageOffset(scrollLength, getPageItemCount());
        }
    }

    private int resolveScrollViewFirstViewIndex(int scrollOffset, boolean horizontal) {
        int count = getChildCount();
        int minVisibleStart, maxVisibleEnd;
        int paddingStart, paddingEnd, containerSize;
        if (horizontal) {
            paddingStart = getPaddingLeft();
            paddingEnd = getPaddingRight();
            containerSize = getWidth();
        } else {
            paddingStart = getPaddingTop();
            paddingEnd = getPaddingBottom();
            containerSize = getHeight();
        }
        minVisibleStart = scrollOffset + paddingStart;
        maxVisibleEnd = minVisibleStart + (containerSize - paddingStart - paddingEnd);
        int childStart, childEnd, pageItemIndex = 0;
        for (int i = 0; i < count; i++) {
            View child = getChildAt(i);
            if (child.getVisibility() == View.GONE || child == mPageHeaderView || child == mPageFooterView)
                continue;
            if (horizontal) {
                childStart = child.getLeft();
                childEnd = child.getRight();
            } else {
                childStart = child.getTop();
                childEnd = child.getBottom();
            }
            if ((childStart >= minVisibleStart && childStart <= maxVisibleEnd)
                    || (childEnd >= minVisibleStart && childEnd <= maxVisibleEnd)
                    || (childStart >= minVisibleStart && childEnd <= maxVisibleEnd)
                    || (childStart < minVisibleStart && childEnd > maxVisibleEnd)) {
                break;
            }
            pageItemIndex++;
        }
        return pageItemIndex;
    }

    private void resolvePageHeaderFooterOffset(int scrollLength, boolean horizontal) {
        if (mPageHeaderView != null && mPageHeaderView.getParent() == this) {
            if (horizontal) {
                mPageHeaderView.setTranslationX(scrollLength);
            } else {
                mPageHeaderView.setTranslationY(scrollLength);
            }
        }
        if (mPageFooterView != null && mPageFooterView.getParent() == this) {
            if (horizontal) {
                mPageFooterView.setTranslationX(scrollLength);
            } else {
                mPageFooterView.setTranslationY(scrollLength);
            }
        }
    }

    private void resolveFloatViewOffset(int scrolled, boolean horizontal) {
        float viewTranslated;
        int wantTranslated;
        if (mFloatViewStartMode == FLOAT_VIEW_SCROLL) {
            View view = getPageItemView(mFloatViewStartIndex);
            PageScrollView.LayoutParams params = (LayoutParams) view.getLayoutParams();
            if (horizontal) {
                wantTranslated = scrolled - (view.getLeft() + params.leftMargin);
                viewTranslated = view.getTranslationX();
            } else {
                wantTranslated = scrolled - (view.getTop() + params.topMargin);
                viewTranslated = view.getTranslationY();
            }
            wantTranslated = Math.max(0, wantTranslated);
            if (wantTranslated != viewTranslated) {
                if (horizontal) {
                    view.setTranslationX(wantTranslated);
                } else {
                    view.setTranslationY(wantTranslated);
                }
            }
        }
        if (mFloatViewEndMode == FLOAT_VIEW_SCROLL) {
            View view = getPageItemView(mFloatViewEndIndex);
            PageScrollView.LayoutParams params = (LayoutParams) view.getLayoutParams();
            int scrollRange;
            if (horizontal) {
                scrollRange = getScrollRangeHorizontal();
                wantTranslated = scrolled - scrollRange + (mContentWidth - (view.getRight() + params.rightMargin));
                viewTranslated = view.getTranslationX();
            } else {
                scrollRange = getScrollRangeVertical();
                wantTranslated = scrolled - scrollRange + (mContentHeight - (view.getBottom() + params.bottomMargin));
                viewTranslated = view.getTranslationY();
            }
            wantTranslated = Math.min(0, wantTranslated);
            if (wantTranslated != viewTranslated) {
                if (horizontal) {
                    view.setTranslationX(wantTranslated);
                } else {
                    view.setTranslationY(wantTranslated);
                }
            }
        }
    }

    private void resolvePageOffset(int scrollLength, int pageItemCount) {
        int targetOffset = computeCenterScrollOffset(mCurrItem, pageItemCount);
        int prevIndex = mCurrItem;
        if (scrollLength > targetOffset && prevIndex < pageItemCount - 1) {
            prevIndex++;
        }
        if (scrollLength < targetOffset && prevIndex > 0) {
            prevIndex--;
        }
        int minIndex, maxIndex, minOffset, maxOffset;
        if (prevIndex > mCurrItem) {
            minIndex = mCurrItem;
            minOffset = targetOffset;
            maxIndex = prevIndex;
            maxOffset = maxIndex == minIndex ? minOffset : computeCenterScrollOffset(maxIndex, pageItemCount);
        } else {
            maxIndex = mCurrItem;
            maxOffset = targetOffset;
            minIndex = prevIndex;
            minOffset = minIndex == maxIndex ? maxOffset : computeCenterScrollOffset(minIndex, pageItemCount);
        }
        int distance = maxOffset - minOffset;
        if (distance > 0) {
            int positionOffsetPixels = scrollLength - minOffset;
            float positionOffset = positionOffsetPixels / (float) distance;
            dispatchPageOffset(minIndex, positionOffset, positionOffsetPixels, pageItemCount);
        } else {
            dispatchPageOffset(minIndex, 0, 0, pageItemCount);
        }
    }

    private void dispatchPageOffset(int index, float offset, int offsetPixels, int pageItemCount) {
        if (mPageListener != null) {
            mPageListener.onPageScrolled(index, offset, offsetPixels);
        }
        if (mPageTransformer != null) {
            int count = getChildCount();
            int minVisibleStart, maxVisibleEnd, scrollOffset;
            int paddingStart, paddingEnd, adjustVisible;
            boolean horizontal = mOrientation == HORIZONTAL;
            if (horizontal) {
                paddingStart = getPaddingLeft();
                paddingEnd = getPaddingRight();
                scrollOffset = getScrollX();
                adjustVisible = getWidth();
            } else {
                paddingStart = getPaddingTop();
                paddingEnd = getPaddingBottom();
                scrollOffset = getScrollY();
                adjustVisible = getHeight();
            }
            minVisibleStart = scrollOffset + paddingStart;
            maxVisibleEnd = minVisibleStart + (adjustVisible - paddingStart - paddingEnd);
            adjustVisible = adjustVisible >> 2;
            minVisibleStart -= adjustVisible;
            maxVisibleEnd += adjustVisible;
            int childStart, childEnd, pageItemIndex = 0;
            int translatedChildCount = 0;
            for (int i = 0; i < count; i++) {
                View child = getChildAt(i);
                if (child.getVisibility() == View.GONE || child == mPageHeaderView || child == mPageFooterView)
                    continue;
                if (horizontal) {
                    childStart = child.getLeft();
                    childEnd = child.getRight();
                } else {
                    childStart = child.getTop();
                    childEnd = child.getBottom();
                }
                if ((childStart >= minVisibleStart && childStart <= maxVisibleEnd)
                        || (childEnd >= minVisibleStart && childEnd <= maxVisibleEnd)
                        || (childStart >= minVisibleStart && childEnd <= maxVisibleEnd)
                        || (childStart < minVisibleStart && childEnd > maxVisibleEnd)) {
                    PageScrollView.LayoutParams params = (LayoutParams) child.getLayoutParams();
                    int contentLength = horizontal ? (child.getWidth() + params.getMarginHorizontal()) : (child.getHeight() + params.getMarginVertical());
                    if (mMiddleMargin > 0) {
                        if (pageItemIndex == 0 || pageItemIndex == pageItemCount - 1) {
                            contentLength += (mMiddleMargin / 2);
                        } else {
                            contentLength += mMiddleMargin;
                        }
                    }
                    float transformerPosition = (scrollOffset - computeCenterScrollOffset(pageItemIndex, pageItemCount)) / (float) contentLength;
                    mPageTransformer.transformPage(child, transformerPosition);
                    translatedChildCount++;
                } else {
                    if (translatedChildCount > 0) break;
                }
                pageItemIndex++;
            }
        }
    }

    @Override
    public void removeAllViewsInLayout() {
        super.removeAllViewsInLayout();
        mCurrItem = 0;
        mPrevItem = -1;
        mAttachLayouted = false;
        mScrollInfo.set(-1, -1, -1, -1);
    }

    @Override
    public PageScrollView.LayoutParams generateLayoutParams(AttributeSet attrs) {
        return new PageScrollView.LayoutParams(getContext(), attrs);
    }

    @Override
    protected PageScrollView.LayoutParams generateDefaultLayoutParams() {
        return new PageScrollView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    @Override
    protected PageScrollView.LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
        return new PageScrollView.LayoutParams(p);
    }

    @Override
    protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
        return p instanceof PageScrollView.LayoutParams;
    }

    public static class LayoutParams extends MarginLayoutParams {
        public int gravity = -1;

        public LayoutParams(Context c, AttributeSet attrs) {
            super(c, attrs);
            TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.PageScrollView);
            gravity = a.getInt(R.styleable.PageScrollView_android_layout_gravity, -1);
            a.recycle();
        }

        public LayoutParams(int width, int height) {
            super(width, height);
        }

        public LayoutParams(int width, int height, int gravity) {
            super(width, height);
            this.gravity = gravity;
        }

        public LayoutParams(ViewGroup.LayoutParams source) {
            super(source);
        }

        public LayoutParams(ViewGroup.MarginLayoutParams source) {
            super(source);
        }

        public LayoutParams(PageScrollView.LayoutParams source) {
            super(source);
            this.gravity = source.gravity;
        }

        public int getMarginHorizontal() {
            return leftMargin + rightMargin;
        }

        public int getMarginVertical() {
            return topMargin + bottomMargin;
        }
    }

    public interface OnScrollChangeListener {
        void onScrollChanged(int scrollX, int scrollY, int oldScrollX, int oldScrollY);

        void onScrollStateChanged(int state, int oldState);
    }

    OnScrollChangeListener mScrollListener;

    public void setOnScrollListener(OnScrollChangeListener l) {
        mScrollListener = l;
    }
}
