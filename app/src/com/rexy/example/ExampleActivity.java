package com.rexy.example;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TextView;

import com.rexy.pagescrollview.R;

/**
 * Created by rexy on 17/4/11.
 */

public class ExampleActivity extends FragmentActivity implements View.OnClickListener {
    TextView mToggleViewPage;
    TextView mToggleOrientation;
    String[] mFragmentTags = new String[]{"ScrollView", "ViewPager"};
    int mVisibleFragmentIndex = 1;

    boolean mViewAsScrollView;
    boolean mViewAsVertical;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_example);
        mToggleViewPage = (TextView) findViewById(R.id.toggleViewPager);
        mToggleOrientation = (TextView) findViewById(R.id.toggleOrientation);
        mToggleViewPage.setOnClickListener(this);
        mToggleOrientation.setOnClickListener(this);
        switchToFragment(mVisibleFragmentIndex, 1 - mVisibleFragmentIndex);
    }

    public boolean getDefaultViewTypeOrientation(boolean scrollView) {
        if (scrollView) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mToggleOrientation) {
            setViewOrientationInner(!mViewAsVertical, true);
        }
        if (v == mToggleViewPage) {
            int willIndex = 1 - mVisibleFragmentIndex;
            switchToFragment(willIndex, mVisibleFragmentIndex);
            mVisibleFragmentIndex = willIndex;
        }
    }

    public void setViewOrientation(boolean vertical) {
        setViewOrientationInner(vertical, false);
    }

    public void setViewOrientationInner(boolean vertical, boolean notify) {
        if (mViewAsVertical != vertical) {
            mViewAsVertical = vertical;
            mToggleOrientation.setText(vertical ? "VERTICAL" : "HORIZONTAL");
            if (notify) {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(mFragmentTags[mVisibleFragmentIndex]);
                if (fragment instanceof ExampleFragment) {
                    ((ExampleFragment) fragment).setContentOrientation(vertical);
                }
            }
        }
    }

    private void setViewType(boolean scrollView) {
        if (mViewAsScrollView != scrollView) {
            mViewAsScrollView = scrollView;
            mToggleViewPage.setText(scrollView ? mFragmentTags[0] : mFragmentTags[1]);
        }
    }

    private void setViewTypeAndOrientation(boolean scrollView, boolean vertical) {
        mViewAsScrollView = !scrollView;
        mViewAsVertical = !vertical;
        setViewType(scrollView);
        setViewOrientationInner(vertical, false);
    }

    private void switchToFragment(int willIndex, int oldIndex) {
        boolean scrollView = willIndex == 0;
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        Fragment showFragment = fm.findFragmentByTag(mFragmentTags[willIndex]);
        Fragment hideFragment = fm.findFragmentByTag(mFragmentTags[oldIndex]);
        if (showFragment == null) {
            boolean initVertical = getDefaultViewTypeOrientation(scrollView);
            setViewTypeAndOrientation(scrollView, initVertical);
            Bundle arg = new Bundle();
            arg.putBoolean(ExampleFragment.KEY_VERTICAL, initVertical);
            Class<? extends ExampleFragment> fragmentClass = scrollView ? ExampleScrollViewFragment.class : ExamplePageViewFragment.class;
            showFragment = Fragment.instantiate(this, fragmentClass.getName(), arg);
            ft.add(R.id.fragmentContainer, showFragment, mFragmentTags[willIndex]);
        } else {
            setViewType(scrollView);
            ft.show(showFragment);
        }
        if (hideFragment != null) {
            ft.hide(hideFragment);
        }
        ft.commitAllowingStateLoss();
    }
}
