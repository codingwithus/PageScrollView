package com.rexy.example;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

import com.rexy.pagescrollview.R;
import com.rexy.widget.PageScrollView;

/**
 * Created by rexy on 17/5/2.
 */

public class ExampleScrollViewFragment extends ExampleFragment {

    ToggleButton mToggleFloatStart;
    ToggleButton mToggleFloatEnd;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_example_scrollview, container, false);
        initView(root);
        return root;
    }

    @Override
    protected void initView(View root) {
        super.initView(root);
        mToggleFloatStart = (ToggleButton) root.findViewById(R.id.toggleFloatFirst);
        mToggleFloatEnd = (ToggleButton) root.findViewById(R.id.toggleFloatEnd);
        mToggleFloatStart.setOnCheckedChangeListener(this);
        mToggleFloatEnd.setOnCheckedChangeListener(this);
        initPageScrollViewItemClick(mPageScrollView);
    }

    @Override
    public boolean setContentOrientationInner(boolean vertical, boolean init) {
        adjustFloatViewParams(vertical);
        boolean handled = super.setContentOrientationInner(vertical, init);
        if (handled && init) {
            adjustFloatIndex(true, mToggleFloatStart.isChecked());
            adjustFloatIndex(false, mToggleFloatEnd.isChecked());
        }
        return handled;
    }

    private void initPageScrollViewItemClick(final PageScrollView pageLayout) {
        final View.OnClickListener pageClick1 = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = pageLayout.indexOfPageItemView(v);
                if (index >= 0) {
                    pageLayout.scrollTo(index, 0, -1);
                }
            }
        };
        int pageItemCount = pageLayout.getPageItemCount();
        for (int i = 0; i < pageItemCount; i++) {
            pageLayout.getPageItemView(i).setOnClickListener(pageClick1);
        }
    }

    private void adjustFloatViewParams(boolean vertical) {
        int pageItemCount = mPageScrollView.getPageItemCount();
        if (pageItemCount >= 2) {
            PageScrollView.LayoutParams lp1 = (PageScrollView.LayoutParams) mPageScrollView.getPageItemView(0).getLayoutParams();
            PageScrollView.LayoutParams lp2 = (PageScrollView.LayoutParams) mPageScrollView.getPageItemView(pageItemCount - 1).getLayoutParams();
            int sizeShort = (int) (mDensity * 60);
            int sizeLong = (int) (mDensity * 350);
            if (vertical) {
                lp1.width = lp2.width = sizeLong;
                lp1.height = lp2.height = sizeShort;
            } else {
                lp1.width = lp2.width = sizeShort;
                lp1.height = lp2.height = sizeLong;
            }
        }
    }

    private void adjustFloatIndex(boolean header, boolean needAdded) {
        int floatIndex = -1;
        if (needAdded) {
            floatIndex = header ? 0 : mPageScrollView.getPageItemCount() - 1;
        }
        if (header) {
            mPageScrollView.setFloatViewStart(floatIndex);
        } else {
            mPageScrollView.setFloatViewEnd(floatIndex);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        super.onCheckedChanged(buttonView, isChecked);
        if (buttonView == mToggleFloatStart) {
            adjustFloatIndex(true, isChecked);
        }
        if (buttonView == mToggleFloatEnd) {
            adjustFloatIndex(false, isChecked);
        }
    }
}
