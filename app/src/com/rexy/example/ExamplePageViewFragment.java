package com.rexy.example;

import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.rexy.pagescrollview.R;
import com.rexy.widget.HorizontalSlideTab;
import com.rexy.widget.PageScrollView;

/**
 * Created by rexy on 17/5/2.
 */

public class ExamplePageViewFragment extends ExampleFragment {

    HorizontalSlideTab mSlideTab;
    ToggleButton mToggleHeader;
    ToggleButton mToggleFooter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_example_viewpage, container, false);
        initView(root);
        return root;
    }

    @Override
    protected void initView(View root) {
        super.initView(root);
        mSlideTab = (HorizontalSlideTab) root.findViewById(R.id.pageTabs);
        mToggleHeader = (ToggleButton) root.findViewById(R.id.togglePageHeader);
        mToggleFooter = (ToggleButton) root.findViewById(R.id.togglePageFooter);
        mToggleHeader.setOnCheckedChangeListener(this);
        mToggleFooter.setOnCheckedChangeListener(this);
        initPageTab(mPageScrollView, mSlideTab);
    }

    private void initPageTab(final PageScrollView pageLayout, final HorizontalSlideTab tabHost) {
        final View.OnClickListener pageClick1 = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int index = pageLayout.indexOfPageItemView(v);
                if (index >= 0) {
                    pageLayout.scrollToCenter(index, 0, -1);
                }
            }
        };
        int pageItemCount = pageLayout.getPageItemCount();
        for (int i = 0; i < pageItemCount; i++) {
            View child = pageLayout.getPageItemView(i);
            child.setOnClickListener(pageClick1);
            if (child instanceof TextView) {
                CharSequence text = ((TextView) child).getText();
                tabHost.addTabItem(text.subSequence(TextUtils.indexOf(text, 'i'), text.length()), true);
            }
        }

        tabHost.setTabClickListener(new HorizontalSlideTab.ITabClickEvent() {
            @Override
            public boolean onTabClicked(HorizontalSlideTab parent, View cur, int curPos, View pre, int prePos) {
                pageLayout.scrollToCenter(curPos, 0, -1);
                return false;
            }
        });
        pageLayout.setPageListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                tabHost.callPageScrolled(position, positionOffset);
            }

            @Override
            public void onPageSelected(int position) {
                tabHost.callPageSelected(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                tabHost.callPageScrollStateChanged(state, pageLayout.getCurrentItem());
            }
        });
    }

    @Override
    public boolean setContentOrientationInner(boolean vertical, boolean init) {
        boolean handled = super.setContentOrientationInner(vertical, init);
        if (handled) {
            adjustPageHeaderAndFooter(vertical, true, mToggleHeader.isChecked());
            adjustPageHeaderAndFooter(vertical, false, mToggleFooter.isChecked());
        }
        return handled;
    }

    private void adjustPageHeaderAndFooter(boolean vertical, boolean header, boolean needAdded) {
        TextView pageHeaderFooter = null;
        if (needAdded) {
            pageHeaderFooter = (TextView) LayoutInflater.from(getActivity()).inflate(vertical ?
                    R.layout.page_headerfooter_vertical : R.layout.page_headerfooter_horizontal, mPageScrollView, false);
            if (header) {
                pageHeaderFooter.setText("I am page Header");
            } else {
                pageHeaderFooter.setText("I am page Footer");
            }
        }
        if (header) {
            mPageScrollView.setPageHeaderView(pageHeaderFooter);
        } else {
            mPageScrollView.setPageFooterView(pageHeaderFooter);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        super.onCheckedChanged(buttonView, isChecked);
        if (buttonView == mToggleHeader) {
            adjustPageHeaderAndFooter(mPageScrollView.getOrientation() == PageScrollView.VERTICAL, true, isChecked);
        }
        if (buttonView == mToggleFooter) {
            adjustPageHeaderAndFooter(mPageScrollView.getOrientation() == PageScrollView.VERTICAL, false, isChecked);
        }
    }
}