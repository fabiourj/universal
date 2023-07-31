package com.sherdle.universal.util;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.appbar.AppBarLayout;
import com.sherdle.universal.R;

public class CustomScrollingViewBehavior extends AppBarLayout.ScrollingViewBehavior  implements View.OnLayoutChangeListener{

    private static final int SCROLL_DIRECTION_UP = -1;

    private boolean isElevated;
    private View appBarLayout;

    private boolean dynamicElevation;

    public CustomScrollingViewBehavior() {
        super();
    }

    public CustomScrollingViewBehavior(Context context, AttributeSet set) {
        super(context, set);
    }

    //-----
    // Disables hide-scroll when content can't scroll
    //-----

    /**
    @Override
    public boolean onMeasureChild(CoordinatorLayout parent, View child, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec,
                                  int heightUsed) {
        if (child.getLayoutParams().height == -1) {
            List<View> dependencies = parent.getDependencies(child);
            if (dependencies.isEmpty()) {
                return false;
            }

            final AppBarLayout appBar = findFirstAppBarLayout(dependencies);
            if (appBar != null && ViewCompat.isLaidOut(appBar)) {
                int availableHeight = View.MeasureSpec.getSize(parentHeightMeasureSpec);
                if (availableHeight == 0) {
                    availableHeight = parent.getHeight();
                }

                final int height = availableHeight - appBar.getMeasuredHeight();
                int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.AT_MOST);

                parent.onMeasureChild(child, parentWidthMeasureSpec, widthUsed, heightMeasureSpec, heightUsed);
                Log.v("INFO", "child: " + child.getClass().getName());
                Log.v("INFO", "child: " + ((ViewGroup) child).getChildAt(0).getClass().getName());
                int childContentHeight = getContentHeight(child);

                Log.v("INFO", "childcontentheight: " + childContentHeight + " height: " + height);
                if (childContentHeight <= height) {
                    updateToolbar(parent, appBar, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed, false);

                    heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);
                    parent.onMeasureChild(child, parentWidthMeasureSpec, widthUsed, heightMeasureSpec, heightUsed);

                    return true;
                } else {
                    updateToolbar(parent, appBar, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed, true);

                    return super.onMeasureChild(parent, child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed);
                }
            }
        }

        return false;
    }

    private static int getContentHeight(View view) {
        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;

            int contentHeight = 0;
            for (int index = 0; index < viewGroup.getChildCount(); ++index) {
                View child = viewGroup.getChildAt(index);
                contentHeight += child.getMeasuredHeight();
            }
            return contentHeight;
        } else {
            return view.getMeasuredHeight();
        }
    }

    private static AppBarLayout findFirstAppBarLayout(List<View> views) {
        int i = 0;

        for (int z = views.size(); i < z; ++i) {
            View view = views.get(i);
            if (view instanceof AppBarLayout) {
                return (AppBarLayout) view;
            }
        }

        throw new IllegalArgumentException("Missing AppBarLayout in CoordinatorLayout dependencies");
    }

    private void updateToolbar(CoordinatorLayout parent, AppBarLayout appBar, int parentWidthMeasureSpec, int widthUsed, int parentHeightMeasureSpec,
                               int heightUsed, boolean toggle) {
        toggleToolbarScroll(appBar, toggle);

        appBar.forceLayout();
        parent.onMeasureChild(appBar, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed);
    }

    private void toggleToolbarScroll(AppBarLayout appBar, boolean toggle) {
        for (int index = 0; index < appBar.getChildCount(); ++index) {
            View child = appBar.getChildAt(index);

            if (child instanceof Toolbar) {
                Toolbar toolbar = (Toolbar) child;
                AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) toolbar.getLayoutParams();
                int scrollFlags = params.getScrollFlags();

                if (toggle) {
                    scrollFlags |= AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL;
                } else {
                    scrollFlags &= ~AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL;
                }

                params.setScrollFlags(scrollFlags);
            }
        }
    }**/

    //-----
    // Allows for elevation to change when scrolling
    //-----

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child,
                                   View dependency) {
        parent.addOnLayoutChangeListener(this);

        if (dependency instanceof AppBarLayout)
            this.appBarLayout =  dependency;

        return super.layoutDependsOn(parent, child, dependency);

    }

    @Override
    public boolean onStartNestedScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                       @NonNull View child, @NonNull View directTargetChild,
                                       @NonNull View target, int axes, int type) {
        // Ensure we react to vertical scrolling
        return axes == ViewCompat.SCROLL_AXIS_VERTICAL ||
                super.onStartNestedScroll(coordinatorLayout, child, directTargetChild,
                        target, axes, type);
    }


    @Override
    public void onNestedPreScroll(@NonNull CoordinatorLayout coordinatorLayout,
                                  @NonNull View child, @NonNull View target,
                                  int dx, int dy, @NonNull int[] consumed, int type) {

        if (dynamicElevation) {
            if (target instanceof SwipeRefreshLayout) {
                SwipeRefreshLayout swipeRefreshLayout = (SwipeRefreshLayout) target;
                if (swipeRefreshLayout.getChildCount() > 0 && swipeRefreshLayout.getChildAt(0) instanceof RecyclerView) {
                    target = swipeRefreshLayout.getChildAt(0);
                }
            }

            if (target instanceof RecyclerView) {
                RecyclerView recyclerView = (RecyclerView) target;

                if (!recyclerView.canScrollVertically(SCROLL_DIRECTION_UP)) {
                    if (isElevated) {
                        ViewGroup parent = (ViewGroup) child.getParent();
                        if (parent != null) {
                            setElevated(false, child.getContext());
                        }
                    }
                    isElevated = false;
                } else {
                    if (!isElevated) {
                        ViewGroup parent = (ViewGroup) child.getParent();
                        if (parent != null) {
                            setElevated(true, child.getContext());
                        }
                    }
                    isElevated = true;
                }
            }
        }

        super.onNestedPreScroll(coordinatorLayout, child, target, dx, dy, consumed, type);
    }

    @Override
    public void onLayoutChange(View view, int i, int i1, int i2, int i3, int i4, int i5, int i6, int i7) {
        if (dynamicElevation) {
            setElevated(false, view.getContext());
            isElevated = false;
        } else {
            setElevated(true, view.getContext());
            isElevated = true;
        }
    }

    private void setElevated(boolean elevated, Context context){
        if (appBarLayout == null) return;
        ViewCompat.setElevation(appBarLayout, elevated ? toolbarElevation(context) : 0);
    }

    public void setDynamicElevation(boolean dynamicElevation) {
        this.dynamicElevation = dynamicElevation;
    }

    private static float toolbarElevation(Context context) {
        Resources r = context.getResources();
        return r.getDimension(R.dimen.toolbar_elevation);
    }
}
