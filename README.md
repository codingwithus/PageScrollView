PageScrollView
==============

PageScrollView widget that is a customized ViewGroup having function like `ScrollView` & `ViewPager` .

**the sample using interaction image,both use a simple PageScrollView without nest another ViewGroup:**

![can't show scrollview style image][scrollview]
![can't show viewpager style image][viewpager]

**support basic function as below listed:**

* layout orientation either Horizontal and Vertical .
* scroll any child to its start and end position to be fixed .
* interface PageTransformer , OnPageChangeListener and OnScrollChangeListener ;
* maxWidth&maxHeight,content gravity and all of its child layout_gravity .
* smooth scroll  any child to its start or centre with a optional offset and anim duration.

Why to Usage
============

  1. when you want use a simple ScrollView with some child ceiling top or bottom , or you don't like to nest a LinearLayout in it as usually,and your want a specified max width or height.
  2. when you need use a ViewPager to display different width and height child and can make any selected item center parent,or need add a header or footer view to its edge.

  so don't hesitate to use PageScrollView , it can support all above interaction requirement.
   
How to Usage
============

*For a working implementation of this project see the [`app/com.rexy`][1] folder.*

  1. Edit layout xml file , add attr properties to PageScrollView,then include any child widgets in it just like in LinearLayout.
       ``` xml
       <com.rexy.widget.PageScrollView                        
              android:id="@+id/pageScrollView"                
              android:layout_width="wrap_content"             
              android:layout_height="wrap_content" 
              android:layout_gravity="center" 
              android:minWidth="100dp"
              android:maxWidth="400dp"
              android:minHeight="100dp"
              android:maxHeight="900dp"
              android:orientation="horizontal" 
              android:gravity="center"                        
              rexy:childCenter="true"                         
              rexy:floatViewEndIndex="-1"                          
              rexy:floatViewStartIndex="-1"                        
              rexy:middleMargin="10dp"                        
              rexy:overFlingDistance="0dp"                    
              rexy:viewPagerStyle="true"                       
              rexy:sizeFixedPercent="0">                      
              <include layout="@layout/merge_childs_layout" />
       </com.rexy.widget.PageScrollView>                      
       ```

  2.  *(Optional)* In your `onCreate` method (or `onCreateView` for a fragment), set support properties as above attr do.
      ``` java
      //set PageScrollView as you need to overwriting the attr properities.
      PageScrollView scrollView = (PageScrollView)findViewById(R.id.pageScrollView);
      //layout orientation HORIZONTAL or VERTICAL.
      scrollView.setOrientation(PageScrollView.VERTICAL); 
      
      //only ViewPager style it will scroll as ViewPager and OnPageChangeListener can be efficient
      scrollView.setViewPagerStyle(false);
      
      //each item measure fixed size for percent of parent width or height.
      scrollView.setSizeFixedPercent(0);
      
      //which item to fixed scroll to start or end [0,pageScrollView.getItemCount()-1],-1 to ignore.
      scrollView.setFloatViewStartIndex(0);
      scrollView.setFloatViewEndIndex(pageScrollView.getItemCount()-1);
      
      //force layout all its childs gravity as Gravity.CENTER.
      scrollView.setChildCenter(true);
      
      //set layout margin for each item between at the layout orientation.
      scrollView.setMiddleMargin(30);
      
      //set max width or height for this container.
      scrollview.setMaxWidth(400);
      scrollview.setMaxHeigh(800);
      ```

  3. *(Optional)* bind event for PageScrollView.
     ``` java
     //continued from above 
     scrollView.setPageHeadView(headerView);
     scrollView.setPageFooterView(footerView);
     scrollView.setPageTransformer(new PageScrollView.PageTransformer() {
         @Override
         public void transformPage(View view, float position, boolean horizontal) {
             //realized your transform animation for this view.
         }
         @Override
         public void recoverTransformPage(View view, boolean horizontal) {
             //clean your transform animation for this view.
         }
     });
     PageScrollView.OnPageChangeListener pagerScrollListener = new PageScrollView.OnPageChangeListener() {
         @Override
         public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
             //when selected item scroll from its center.
         }
         @Override
         public void onPageSelected(int position, int oldPosition) {
             // position current selected item ,oldPosition previous selected item
         }
         @Override
         public void onScrollChanged(int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
             //when content scrolled,see View.onScrollChanged
         }
         @Override
         public void onScrollStateChanged(int state, int oldState) {
             // SCROLL_STATE_IDLE = 0; //scroll stopped .
             // SCROLL_STATE_DRAGGING = 1;//dragged scroll started .
             // SCROLL_STATE_SETTLING = 2;//fling scroll started .
         }
     };
     scrollView.setOnScrollChangeListener(pagerScrollListener);
     scrollView.setOnPageChangeListener(pagerScrollListener);
     ```

 [scrollview]:image/example_type_scrollview.gif "scrollview type but no need to nest a single ViewGroup,just use as a LinearLayout"
 [viewpager]:image/example_type_viewpager.gif  "viewpager type but not support PageAdapter"
 [1]:app/src/com/rexy/example/PageLayoutExampleActivity.java "activity entry"