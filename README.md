PageScrollView
======================

PageScrollView widget that is a customized ViewGroup having function like `ScrollView` & `ViewPager` .

**the sample using interaction image,both use a simple PageScrollView without nest another ViewGroup:**

![can't show scrollview style image][scrollview]
![can't show viewpager style image][viewpager]

**support basic function as below listed:**

* layout orientation either Horizontal and Vertical .
* scroll any child to its start and end position to be fixed .
* interface PageTransformer , OnPageChangeListener and OnScrollChangeListener ;
* maxWidth&maxHeight,content gravity and all of its child layout_gravity .
* smooth scroll  any child to its start or center with a optional offset and anim duration.

Usage
=====

*For a working implementation of this project see the [`app/com.rexy`][1] folder.*

  1. Edit layout xml file , add attr properties to PageScrollView,then include any child widgets in it just like in LinearLayout.
       ``` xml
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
       ```

  2.  *(Optional)* In your `onCreate` method (or `onCreateView` for a fragment), set support properties as above attr do.
      ``` java
      //set PageScrollView as you need to overwriting the attr properities.
      PageScrollView scrollView = (PageScrollView)findViewById(R.id.pageScrollView);
      scrollView.setOrientation(PageScrollView.VERTICAL);
      scrollView.setPageViewStyle(false);
      scrollView.setSizeFixedPercent(0);
      scrollView.setFloatViewStart(1);
      scrollView.setFloatViewEnd(pageScrollView.getPageItemCount()-1);
      scrollView.setChildCenter(true);
      scrollview.setMaxWidth(400);
      scrollview.setMaxHeigh(800);
      ```

  3. *(Optional)* bind event for PageScrollView.
     ``` java
     //continued from above
     scrollView.setPageTransformer(new YourPageTransformer());
     scrollView.setPageHeadView(headerView);
     scrollView.setPageFooterView(footerView);
     scrollView.setPageListener(yourOnPageChangeListener);
     scrollView.setOnScrollListener(yourOnScrollChangeListener);
     ```

 [scrollview]:image/example_type_scrollview.gif "scrollview type but no need to nest a single ViewGroup,just use as a LinearLayout"
 [viewpager]:image/example_type_viewpager.gif  "viewpager type but not support PageAdapter"
 [1]:app/src/com/rexy/example/PageLayoutExampleActivity.java "activity entry"