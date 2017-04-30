package com.rexy.example;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.rexy.pagescrollview.R;
import com.rexy.widget.PageScrollView;
import com.rexy.widget.TestPageTransformer;

/**
 * Created by rexy on 17/5/2.
 */

public class ExampleFragment extends Fragment implements CompoundButton.OnCheckedChangeListener {
    public static final String KEY_VERTICAL = "KEY_VERTICAL";
    private int mVisibleStatus = -1;
    protected float mDensity;
    protected PageScrollView mPageScrollView;
    protected boolean mContentVertical;
    protected TestPageTransformer mPageTransformer = new TestPageTransformer(true);
    public static void log(CharSequence msg) {
        Log.d("PageScrollView", String.valueOf(msg));
    }

    ToggleButton mToggleAnim;
    ToggleButton mToggleCenter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle arg = getArguments();
        if (arg != null) {
            mContentVertical = arg.getBoolean(KEY_VERTICAL);
        }
        mContentVertical = !mContentVertical;
        setContentOrientationInner(!mContentVertical, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mVisibleStatus == -1) {
            mVisibleStatus = 1;
            onFragmentVisibleChanged(true, true);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mVisibleStatus == 1) {
            mVisibleStatus = -1;
            onFragmentVisibleChanged(false, true);
        }
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        mVisibleStatus = hidden ? 0 : 1;
        onFragmentVisibleChanged(mVisibleStatus == 1, false);
    }


    protected void onFragmentVisibleChanged(boolean visible, boolean fromLifecycle) {
        if (visible) {
            if (getActivity() instanceof PageLayoutExampleActivity) {
                ((PageLayoutExampleActivity) getActivity()).setViewOrientation(mContentVertical);
            }
        }
    }

    public void setContentOrientation(boolean vertical) {
        setContentOrientationInner(vertical, false);
    }

    protected void initView(View root) {
        mPageScrollView = (PageScrollView) root.findViewById(R.id.pageScrollView);
        mToggleAnim = (ToggleButton) root.findViewById(R.id.toggleTransform);
        mToggleCenter = (ToggleButton) root.findViewById(R.id.toggleChildCenter);
        mToggleAnim.setOnCheckedChangeListener(this);
        mToggleCenter.setOnCheckedChangeListener(this);
    }

    protected boolean setContentOrientationInner(boolean vertical, boolean init) {
        if (mContentVertical != vertical) {
            mContentVertical = vertical;
            mPageTransformer.setOrientation(vertical);
            mPageScrollView.setOrientation(vertical ? PageScrollView.VERTICAL : PageScrollView.HORIZONTAL);
            if (init) {
                adjustTransformAnimation(mToggleAnim.isChecked());
                adjustChildLayoutCenter(mToggleCenter.isChecked());
            }
            return true;
        }
        return false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mDensity = context.getResources().getDisplayMetrics().density;
    }

    public void adjustTransformAnimation(boolean haveAnim) {
        mPageScrollView.setPageTransformer(haveAnim ? mPageTransformer : null);
    }

    public void adjustChildLayoutCenter(boolean layoutCenter) {
        mPageScrollView.setChildCenter(layoutCenter);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (mToggleAnim == buttonView) {
            adjustTransformAnimation(isChecked);
        }
        if (mToggleCenter == buttonView) {
            adjustChildLayoutCenter(isChecked);
        }
    }
}
