Android PageScrollView
======================

PageScrollView widget that is a customized ViewGroup having function like `ScrollView` & `ViewPager` .

**support basic function as below listed:**

* layout orientation either Horizontal and Vertical ;
* scroll fixed any child to its start and end positon ;
* interface ViewPager.PageTransformer and ViewPager.OnPageChangeListener ans OnScrollChangeListener ;
* maxWidth&maxHeight,content gravity and all of its childs layout_gravity.
* smoonth scroll to any child start or its center .


**the sample use interaction image:**
![can't show scrollview style image][1]
![can't show viewpager style image][2]


Usage
=====

*For a working implementation of this project see the `app/com.rexy` folder.*

  1. Edit layout xml file , add attr properties to PageScrollView,then include any child widgets in it just like in LinearLayout.

        <com.rexy.widget.PageScrollView
               android:id="@+id/pageScrollView"
               android:orientation="horizontal"
               android:layout_width="wrap_content"
               android:layout_height="wrap_content"
               android:gravity="center"
               android:layout_gravity="center"
               rexy:childCenter="true"
               rexy:floatViewEnd="-1"
               rexy:floatViewStart="-1"
               rexy:middleMargin="10dp"
               rexy:overFlingDistance="0dp"
               rexy:pageViewStyle="true"
               rexy:sizeFixedPercent="0">
               <include layout="@layout/merge_childs_layout" />
           </com.rexy.widget.PageScrollView>

  2.  *(Optional)* In your `onCreate` method (or `onCreateView` for a fragment), set support properties as above attr do.

         //Set PageScrollView as you need .
         PageScrollView scrollView = (PageScrollView)findViewById(R.id.pageScrollView);
         scrollView.setOrientation(PageScrollView.VERTICAL);
         scrollView.setPageViewStyle(false);
         scrollView.setSizeFixedPercent(0);
         scrollView.setFloatViewEnd(pageScrollView.getPageItemCount()-1);
         scrollView.setFloatViewStart(1);
         scrollView.setChildCenter(true);
         scrollview.setMaxWidth(400);
         scrollview.setMaxHeigh(800);

  3. *(Optional)* bind event for PageScrollView.

         //continued from above
         scrollView.setPageTransformer(new YourPageTransformer());
         scrollView.setPageHeadView(headerView);
         scrollView.setPageFooterView(headerView);
         scrollView.setPageListener(yourOnPageChangeListener);
         scrollView.setOnScrollListener(yourOnScrollChangeListener);


 [1]: file://\/Users/renzheng/AndroidWorkspace/github/PageScrollView/image/example_type_scrollview.gif "it's not a scrollview"
 [2]: file://\/Users/renzheng/AndroidWorkspace/github/PageScrollView/image/example_type_viewpager.gif "it's not a viewpager"
