package androidx.viewpager.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.FocusFinder;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SoundEffectConstants;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.animation.Interpolator;
import android.widget.EdgeEffect;
import android.widget.Scroller;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.core.content.ContextCompat;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.customview.view.AbsSavedState;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ViewPager extends ViewGroup {
    private static final int CLOSE_ENOUGH = 2;
    private static final Comparator<ItemInfo> COMPARATOR = new Comparator<ItemInfo>() {
        public int compare(ItemInfo itemInfo, ItemInfo itemInfo2) {
            return itemInfo.b - itemInfo2.b;
        }
    };
    private static final boolean DEBUG = false;
    private static final int DEFAULT_GUTTER_SIZE = 16;
    private static final int DEFAULT_OFFSCREEN_PAGES = 1;
    private static final int DRAW_ORDER_DEFAULT = 0;
    private static final int DRAW_ORDER_FORWARD = 1;
    private static final int DRAW_ORDER_REVERSE = 2;
    private static final int INVALID_POINTER = -1;
    private static final int MAX_SETTLE_DURATION = 600;
    private static final int MIN_DISTANCE_FOR_FLING = 25;
    private static final int MIN_FLING_VELOCITY = 400;
    public static final int SCROLL_STATE_DRAGGING = 1;
    public static final int SCROLL_STATE_IDLE = 0;
    public static final int SCROLL_STATE_SETTLING = 2;
    private static final String TAG = "ViewPager";
    private static final boolean USE_CACHE = false;
    static final int[] c = {16842931};
    private static final Interpolator sInterpolator = new Interpolator() {
        public float getInterpolation(float f) {
            float f2 = f - 1.0f;
            return (f2 * f2 * f2 * f2 * f2) + 1.0f;
        }
    };
    private static final ViewPositionComparator sPositionComparator = new ViewPositionComparator();
    PagerAdapter a;
    int b;
    private int mActivePointerId = -1;
    private List<OnAdapterChangeListener> mAdapterChangeListeners;
    private int mBottomPageBounds;
    private boolean mCalledSuper;
    private int mChildHeightMeasureSpec;
    private int mChildWidthMeasureSpec;
    private int mCloseEnough;
    private int mDecorChildCount;
    private int mDefaultGutterSize;
    private int mDrawingOrder;
    private ArrayList<View> mDrawingOrderedChildren;
    private final Runnable mEndScrollRunnable = new Runnable() {
        public void run() {
            ViewPager.this.setScrollState(0);
            ViewPager.this.e();
        }
    };
    private int mExpectedAdapterCount;
    private long mFakeDragBeginTime;
    private boolean mFakeDragging;
    private boolean mFirstLayout = true;
    private float mFirstOffset = -3.4028235E38f;
    private int mFlingDistance;
    private int mGutterSize;
    private boolean mInLayout;
    private float mInitialMotionX;
    private float mInitialMotionY;
    private OnPageChangeListener mInternalPageChangeListener;
    private boolean mIsBeingDragged;
    private boolean mIsScrollStarted;
    private boolean mIsUnableToDrag;
    private final ArrayList<ItemInfo> mItems = new ArrayList<>();
    private float mLastMotionX;
    private float mLastMotionY;
    private float mLastOffset = Float.MAX_VALUE;
    private EdgeEffect mLeftEdge;
    private Drawable mMarginDrawable;
    private int mMaximumVelocity;
    private int mMinimumVelocity;
    private boolean mNeedCalculatePageOffsets = false;
    private PagerObserver mObserver;
    private int mOffscreenPageLimit = 1;
    private OnPageChangeListener mOnPageChangeListener;
    private List<OnPageChangeListener> mOnPageChangeListeners;
    private int mPageMargin;
    private PageTransformer mPageTransformer;
    private int mPageTransformerLayerType;
    private boolean mPopulatePending;
    private Parcelable mRestoredAdapterState = null;
    private ClassLoader mRestoredClassLoader = null;
    private int mRestoredCurItem = -1;
    private EdgeEffect mRightEdge;
    private int mScrollState = 0;
    private Scroller mScroller;
    private boolean mScrollingCacheEnabled;
    private final ItemInfo mTempItem = new ItemInfo();
    private final Rect mTempRect = new Rect();
    private int mTopPageBounds;
    private int mTouchSlop;
    private VelocityTracker mVelocityTracker;

    @Inherited
    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface DecorView {
    }

    static class ItemInfo {
        Object a;
        int b;
        boolean c;
        float d;
        float e;

        ItemInfo() {
        }
    }

    public static class LayoutParams extends ViewGroup.LayoutParams {
        float a = 0.0f;
        boolean b;
        int c;
        int d;
        public int gravity;
        public boolean isDecor;

        public LayoutParams() {
            super(-1, -1);
        }

        public LayoutParams(Context context, AttributeSet attributeSet) {
            super(context, attributeSet);
            TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, ViewPager.c);
            this.gravity = obtainStyledAttributes.getInteger(0, 48);
            obtainStyledAttributes.recycle();
        }
    }

    class MyAccessibilityDelegate extends AccessibilityDelegateCompat {
        MyAccessibilityDelegate() {
        }

        private boolean canScroll() {
            PagerAdapter pagerAdapter = ViewPager.this.a;
            return pagerAdapter != null && pagerAdapter.getCount() > 1;
        }

        public void onInitializeAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent) {
            PagerAdapter pagerAdapter;
            super.onInitializeAccessibilityEvent(view, accessibilityEvent);
            accessibilityEvent.setClassName(ViewPager.class.getName());
            accessibilityEvent.setScrollable(canScroll());
            if (accessibilityEvent.getEventType() == 4096 && (pagerAdapter = ViewPager.this.a) != null) {
                accessibilityEvent.setItemCount(pagerAdapter.getCount());
                accessibilityEvent.setFromIndex(ViewPager.this.b);
                accessibilityEvent.setToIndex(ViewPager.this.b);
            }
        }

        public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfoCompat);
            accessibilityNodeInfoCompat.setClassName(ViewPager.class.getName());
            accessibilityNodeInfoCompat.setScrollable(canScroll());
            if (ViewPager.this.canScrollHorizontally(1)) {
                accessibilityNodeInfoCompat.addAction(4096);
            }
            if (ViewPager.this.canScrollHorizontally(-1)) {
                accessibilityNodeInfoCompat.addAction(8192);
            }
        }

        public boolean performAccessibilityAction(View view, int i, Bundle bundle) {
            ViewPager viewPager;
            int i2;
            if (super.performAccessibilityAction(view, i, bundle)) {
                return true;
            }
            if (i != 4096) {
                if (i != 8192 || !ViewPager.this.canScrollHorizontally(-1)) {
                    return false;
                }
                viewPager = ViewPager.this;
                i2 = viewPager.b - 1;
            } else if (!ViewPager.this.canScrollHorizontally(1)) {
                return false;
            } else {
                viewPager = ViewPager.this;
                i2 = viewPager.b + 1;
            }
            viewPager.setCurrentItem(i2);
            return true;
        }
    }

    public interface OnAdapterChangeListener {
        void onAdapterChanged(@NonNull ViewPager viewPager, @Nullable PagerAdapter pagerAdapter, @Nullable PagerAdapter pagerAdapter2);
    }

    public interface OnPageChangeListener {
        void onPageScrollStateChanged(int i);

        void onPageScrolled(int i, float f, @Px int i2);

        void onPageSelected(int i);
    }

    public interface PageTransformer {
        void transformPage(@NonNull View view, float f);
    }

    private class PagerObserver extends DataSetObserver {
        PagerObserver() {
        }

        public void onChanged() {
            ViewPager.this.a();
        }

        public void onInvalidated() {
            ViewPager.this.a();
        }
    }

    public static class SavedState extends AbsSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.ClassLoaderCreator<SavedState>() {
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel, (ClassLoader) null);
            }

            public SavedState createFromParcel(Parcel parcel, ClassLoader classLoader) {
                return new SavedState(parcel, classLoader);
            }

            public SavedState[] newArray(int i) {
                return new SavedState[i];
            }
        };
        int a;
        Parcelable b;
        ClassLoader c;

        SavedState(Parcel parcel, ClassLoader classLoader) {
            super(parcel, classLoader);
            classLoader = classLoader == null ? SavedState.class.getClassLoader() : classLoader;
            this.a = parcel.readInt();
            this.b = parcel.readParcelable(classLoader);
            this.c = classLoader;
        }

        public SavedState(@NonNull Parcelable parcelable) {
            super(parcelable);
        }

        public String toString() {
            return "FragmentPager.SavedState{" + Integer.toHexString(System.identityHashCode(this)) + " position=" + this.a + "}";
        }

        public void writeToParcel(Parcel parcel, int i) {
            super.writeToParcel(parcel, i);
            parcel.writeInt(this.a);
            parcel.writeParcelable(this.b, i);
        }
    }

    public static class SimpleOnPageChangeListener implements OnPageChangeListener {
        public void onPageScrollStateChanged(int i) {
        }

        public void onPageScrolled(int i, float f, int i2) {
        }

        public void onPageSelected(int i) {
        }
    }

    static class ViewPositionComparator implements Comparator<View> {
        ViewPositionComparator() {
        }

        public int compare(View view, View view2) {
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            LayoutParams layoutParams2 = (LayoutParams) view2.getLayoutParams();
            boolean z = layoutParams.isDecor;
            return z != layoutParams2.isDecor ? z ? 1 : -1 : layoutParams.c - layoutParams2.c;
        }
    }

    public ViewPager(@NonNull Context context) {
        super(context);
        b();
    }

    public ViewPager(@NonNull Context context, @Nullable AttributeSet attributeSet) {
        super(context, attributeSet);
        b();
    }

    private void calculatePageOffsets(ItemInfo itemInfo, int i, ItemInfo itemInfo2) {
        int i2;
        int i3;
        ItemInfo itemInfo3;
        ItemInfo itemInfo4;
        int count = this.a.getCount();
        int clientWidth = getClientWidth();
        float f = clientWidth > 0 ? ((float) this.mPageMargin) / ((float) clientWidth) : 0.0f;
        if (itemInfo2 != null) {
            int i4 = itemInfo2.b;
            int i5 = itemInfo.b;
            if (i4 < i5) {
                float f2 = itemInfo2.e + itemInfo2.d + f;
                int i6 = i4 + 1;
                int i7 = 0;
                while (i6 <= itemInfo.b && i7 < this.mItems.size()) {
                    while (true) {
                        itemInfo4 = this.mItems.get(i7);
                        if (i6 > itemInfo4.b && i7 < this.mItems.size() - 1) {
                            i7++;
                        }
                    }
                    while (i6 < itemInfo4.b) {
                        f2 += this.a.getPageWidth(i6) + f;
                        i6++;
                    }
                    itemInfo4.e = f2;
                    f2 += itemInfo4.d + f;
                    i6++;
                }
            } else if (i4 > i5) {
                int size = this.mItems.size() - 1;
                float f3 = itemInfo2.e;
                while (true) {
                    i4--;
                    if (i4 < itemInfo.b || size < 0) {
                        break;
                    }
                    while (true) {
                        itemInfo3 = this.mItems.get(size);
                        if (i4 < itemInfo3.b && size > 0) {
                            size--;
                        }
                    }
                    while (i4 > itemInfo3.b) {
                        f3 -= this.a.getPageWidth(i4) + f;
                        i4--;
                    }
                    f3 -= itemInfo3.d + f;
                    itemInfo3.e = f3;
                }
            }
        }
        int size2 = this.mItems.size();
        float f4 = itemInfo.e;
        int i8 = itemInfo.b;
        int i9 = i8 - 1;
        this.mFirstOffset = i8 == 0 ? f4 : -3.4028235E38f;
        int i10 = count - 1;
        this.mLastOffset = itemInfo.b == i10 ? (itemInfo.e + itemInfo.d) - 1.0f : Float.MAX_VALUE;
        int i11 = i - 1;
        while (i11 >= 0) {
            ItemInfo itemInfo5 = this.mItems.get(i11);
            while (true) {
                i3 = itemInfo5.b;
                if (i9 <= i3) {
                    break;
                }
                f4 -= this.a.getPageWidth(i9) + f;
                i9--;
            }
            f4 -= itemInfo5.d + f;
            itemInfo5.e = f4;
            if (i3 == 0) {
                this.mFirstOffset = f4;
            }
            i11--;
            i9--;
        }
        float f5 = itemInfo.e + itemInfo.d + f;
        int i12 = itemInfo.b + 1;
        int i13 = i + 1;
        while (i13 < size2) {
            ItemInfo itemInfo6 = this.mItems.get(i13);
            while (true) {
                i2 = itemInfo6.b;
                if (i12 >= i2) {
                    break;
                }
                f5 += this.a.getPageWidth(i12) + f;
                i12++;
            }
            if (i2 == i10) {
                this.mLastOffset = (itemInfo6.d + f5) - 1.0f;
            }
            itemInfo6.e = f5;
            f5 += itemInfo6.d + f;
            i13++;
            i12++;
        }
        this.mNeedCalculatePageOffsets = false;
    }

    private void completeScroll(boolean z) {
        boolean z2 = this.mScrollState == 2;
        if (z2) {
            setScrollingCacheEnabled(false);
            if (!this.mScroller.isFinished()) {
                this.mScroller.abortAnimation();
                int scrollX = getScrollX();
                int scrollY = getScrollY();
                int currX = this.mScroller.getCurrX();
                int currY = this.mScroller.getCurrY();
                if (!(scrollX == currX && scrollY == currY)) {
                    scrollTo(currX, currY);
                    if (currX != scrollX) {
                        pageScrolled(currX);
                    }
                }
            }
        }
        this.mPopulatePending = false;
        boolean z3 = z2;
        for (int i = 0; i < this.mItems.size(); i++) {
            ItemInfo itemInfo = this.mItems.get(i);
            if (itemInfo.c) {
                itemInfo.c = false;
                z3 = true;
            }
        }
        if (!z3) {
            return;
        }
        if (z) {
            ViewCompat.postOnAnimation(this, this.mEndScrollRunnable);
        } else {
            this.mEndScrollRunnable.run();
        }
    }

    private int determineTargetPage(int i, float f, int i2, int i3) {
        if (Math.abs(i3) <= this.mFlingDistance || Math.abs(i2) <= this.mMinimumVelocity) {
            i += (int) (f + (i >= this.b ? 0.4f : 0.6f));
        } else if (i2 <= 0) {
            i++;
        }
        if (this.mItems.size() <= 0) {
            return i;
        }
        ArrayList<ItemInfo> arrayList = this.mItems;
        return Math.max(this.mItems.get(0).b, Math.min(i, arrayList.get(arrayList.size() - 1).b));
    }

    private void dispatchOnPageScrolled(int i, float f, int i2) {
        OnPageChangeListener onPageChangeListener = this.mOnPageChangeListener;
        if (onPageChangeListener != null) {
            onPageChangeListener.onPageScrolled(i, f, i2);
        }
        List<OnPageChangeListener> list = this.mOnPageChangeListeners;
        if (list != null) {
            int size = list.size();
            for (int i3 = 0; i3 < size; i3++) {
                OnPageChangeListener onPageChangeListener2 = this.mOnPageChangeListeners.get(i3);
                if (onPageChangeListener2 != null) {
                    onPageChangeListener2.onPageScrolled(i, f, i2);
                }
            }
        }
        OnPageChangeListener onPageChangeListener3 = this.mInternalPageChangeListener;
        if (onPageChangeListener3 != null) {
            onPageChangeListener3.onPageScrolled(i, f, i2);
        }
    }

    private void dispatchOnPageSelected(int i) {
        OnPageChangeListener onPageChangeListener = this.mOnPageChangeListener;
        if (onPageChangeListener != null) {
            onPageChangeListener.onPageSelected(i);
        }
        List<OnPageChangeListener> list = this.mOnPageChangeListeners;
        if (list != null) {
            int size = list.size();
            for (int i2 = 0; i2 < size; i2++) {
                OnPageChangeListener onPageChangeListener2 = this.mOnPageChangeListeners.get(i2);
                if (onPageChangeListener2 != null) {
                    onPageChangeListener2.onPageSelected(i);
                }
            }
        }
        OnPageChangeListener onPageChangeListener3 = this.mInternalPageChangeListener;
        if (onPageChangeListener3 != null) {
            onPageChangeListener3.onPageSelected(i);
        }
    }

    private void dispatchOnScrollStateChanged(int i) {
        OnPageChangeListener onPageChangeListener = this.mOnPageChangeListener;
        if (onPageChangeListener != null) {
            onPageChangeListener.onPageScrollStateChanged(i);
        }
        List<OnPageChangeListener> list = this.mOnPageChangeListeners;
        if (list != null) {
            int size = list.size();
            for (int i2 = 0; i2 < size; i2++) {
                OnPageChangeListener onPageChangeListener2 = this.mOnPageChangeListeners.get(i2);
                if (onPageChangeListener2 != null) {
                    onPageChangeListener2.onPageScrollStateChanged(i);
                }
            }
        }
        OnPageChangeListener onPageChangeListener3 = this.mInternalPageChangeListener;
        if (onPageChangeListener3 != null) {
            onPageChangeListener3.onPageScrollStateChanged(i);
        }
    }

    private void enableLayers(boolean z) {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            getChildAt(i).setLayerType(z ? this.mPageTransformerLayerType : 0, (Paint) null);
        }
    }

    private void endDrag() {
        this.mIsBeingDragged = false;
        this.mIsUnableToDrag = false;
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker != null) {
            velocityTracker.recycle();
            this.mVelocityTracker = null;
        }
    }

    private Rect getChildRectInPagerCoordinates(Rect rect, View view) {
        if (rect == null) {
            rect = new Rect();
        }
        if (view == null) {
            rect.set(0, 0, 0, 0);
            return rect;
        }
        rect.left = view.getLeft();
        rect.right = view.getRight();
        rect.top = view.getTop();
        rect.bottom = view.getBottom();
        ViewParent parent = view.getParent();
        while ((parent instanceof ViewGroup) && parent != this) {
            ViewGroup viewGroup = (ViewGroup) parent;
            rect.left += viewGroup.getLeft();
            rect.right += viewGroup.getRight();
            rect.top += viewGroup.getTop();
            rect.bottom += viewGroup.getBottom();
            parent = viewGroup.getParent();
        }
        return rect;
    }

    private int getClientWidth() {
        return (getMeasuredWidth() - getPaddingLeft()) - getPaddingRight();
    }

    private ItemInfo infoForCurrentScrollPosition() {
        int i;
        int clientWidth = getClientWidth();
        float scrollX = clientWidth > 0 ? ((float) getScrollX()) / ((float) clientWidth) : 0.0f;
        float f = clientWidth > 0 ? ((float) this.mPageMargin) / ((float) clientWidth) : 0.0f;
        ItemInfo itemInfo = null;
        int i2 = 0;
        boolean z = true;
        int i3 = -1;
        float f2 = 0.0f;
        float f3 = 0.0f;
        while (i2 < this.mItems.size()) {
            ItemInfo itemInfo2 = this.mItems.get(i2);
            if (!z && itemInfo2.b != (i = i3 + 1)) {
                itemInfo2 = this.mTempItem;
                itemInfo2.e = f2 + f3 + f;
                itemInfo2.b = i;
                itemInfo2.d = this.a.getPageWidth(itemInfo2.b);
                i2--;
            }
            f2 = itemInfo2.e;
            float f4 = itemInfo2.d + f2 + f;
            if (!z && scrollX < f2) {
                return itemInfo;
            }
            if (scrollX < f4 || i2 == this.mItems.size() - 1) {
                return itemInfo2;
            }
            i3 = itemInfo2.b;
            f3 = itemInfo2.d;
            i2++;
            itemInfo = itemInfo2;
            z = false;
        }
        return itemInfo;
    }

    private static boolean isDecorView(@NonNull View view) {
        return view.getClass().getAnnotation(DecorView.class) != null;
    }

    private boolean isGutterDrag(float f, float f2) {
        return (f < ((float) this.mGutterSize) && f2 > 0.0f) || (f > ((float) (getWidth() - this.mGutterSize)) && f2 < 0.0f);
    }

    private void onSecondaryPointerUp(MotionEvent motionEvent) {
        int actionIndex = motionEvent.getActionIndex();
        if (motionEvent.getPointerId(actionIndex) == this.mActivePointerId) {
            int i = actionIndex == 0 ? 1 : 0;
            this.mLastMotionX = motionEvent.getX(i);
            this.mActivePointerId = motionEvent.getPointerId(i);
            VelocityTracker velocityTracker = this.mVelocityTracker;
            if (velocityTracker != null) {
                velocityTracker.clear();
            }
        }
    }

    private boolean pageScrolled(int i) {
        if (this.mItems.size() != 0) {
            ItemInfo infoForCurrentScrollPosition = infoForCurrentScrollPosition();
            int clientWidth = getClientWidth();
            int i2 = this.mPageMargin;
            int i3 = clientWidth + i2;
            float f = (float) clientWidth;
            int i4 = infoForCurrentScrollPosition.b;
            float f2 = ((((float) i) / f) - infoForCurrentScrollPosition.e) / (infoForCurrentScrollPosition.d + (((float) i2) / f));
            this.mCalledSuper = false;
            a(i4, f2, (int) (((float) i3) * f2));
            if (this.mCalledSuper) {
                return true;
            }
            throw new IllegalStateException("onPageScrolled did not call superclass implementation");
        } else if (this.mFirstLayout) {
            return false;
        } else {
            this.mCalledSuper = false;
            a(0, 0.0f, 0);
            if (this.mCalledSuper) {
                return false;
            }
            throw new IllegalStateException("onPageScrolled did not call superclass implementation");
        }
    }

    private boolean performDrag(float f) {
        boolean z;
        boolean z2;
        float f2 = this.mLastMotionX - f;
        this.mLastMotionX = f;
        float scrollX = ((float) getScrollX()) + f2;
        float clientWidth = (float) getClientWidth();
        float f3 = this.mFirstOffset * clientWidth;
        float f4 = this.mLastOffset * clientWidth;
        boolean z3 = false;
        ItemInfo itemInfo = this.mItems.get(0);
        ArrayList<ItemInfo> arrayList = this.mItems;
        ItemInfo itemInfo2 = arrayList.get(arrayList.size() - 1);
        if (itemInfo.b != 0) {
            f3 = itemInfo.e * clientWidth;
            z = false;
        } else {
            z = true;
        }
        if (itemInfo2.b != this.a.getCount() - 1) {
            f4 = itemInfo2.e * clientWidth;
            z2 = false;
        } else {
            z2 = true;
        }
        if (scrollX < f3) {
            if (z) {
                this.mLeftEdge.onPull(Math.abs(f3 - scrollX) / clientWidth);
                z3 = true;
            }
            scrollX = f3;
        } else if (scrollX > f4) {
            if (z2) {
                this.mRightEdge.onPull(Math.abs(scrollX - f4) / clientWidth);
                z3 = true;
            }
            scrollX = f4;
        }
        int i = (int) scrollX;
        this.mLastMotionX += scrollX - ((float) i);
        scrollTo(i, getScrollY());
        pageScrolled(i);
        return z3;
    }

    private void recomputeScrollPosition(int i, int i2, int i3, int i4) {
        int min;
        if (i2 <= 0 || this.mItems.isEmpty()) {
            ItemInfo a2 = a(this.b);
            min = (int) ((a2 != null ? Math.min(a2.e, this.mLastOffset) : 0.0f) * ((float) ((i - getPaddingLeft()) - getPaddingRight())));
            if (min != getScrollX()) {
                completeScroll(false);
            } else {
                return;
            }
        } else if (!this.mScroller.isFinished()) {
            this.mScroller.setFinalX(getCurrentItem() * getClientWidth());
            return;
        } else {
            min = (int) ((((float) getScrollX()) / ((float) (((i2 - getPaddingLeft()) - getPaddingRight()) + i4))) * ((float) (((i - getPaddingLeft()) - getPaddingRight()) + i3)));
        }
        scrollTo(min, getScrollY());
    }

    private void removeNonDecorViews() {
        int i = 0;
        while (i < getChildCount()) {
            if (!((LayoutParams) getChildAt(i).getLayoutParams()).isDecor) {
                removeViewAt(i);
                i--;
            }
            i++;
        }
    }

    private void requestParentDisallowInterceptTouchEvent(boolean z) {
        ViewParent parent = getParent();
        if (parent != null) {
            parent.requestDisallowInterceptTouchEvent(z);
        }
    }

    private boolean resetTouch() {
        this.mActivePointerId = -1;
        endDrag();
        this.mLeftEdge.onRelease();
        this.mRightEdge.onRelease();
        return this.mLeftEdge.isFinished() || this.mRightEdge.isFinished();
    }

    private void scrollToItem(int i, boolean z, int i2, boolean z2) {
        ItemInfo a2 = a(i);
        int clientWidth = a2 != null ? (int) (((float) getClientWidth()) * Math.max(this.mFirstOffset, Math.min(a2.e, this.mLastOffset))) : 0;
        if (z) {
            a(clientWidth, 0, i2);
            if (z2) {
                dispatchOnPageSelected(i);
                return;
            }
            return;
        }
        if (z2) {
            dispatchOnPageSelected(i);
        }
        completeScroll(false);
        scrollTo(clientWidth, 0);
        pageScrolled(clientWidth);
    }

    private void setScrollingCacheEnabled(boolean z) {
        if (this.mScrollingCacheEnabled != z) {
            this.mScrollingCacheEnabled = z;
        }
    }

    private void sortChildDrawingOrder() {
        if (this.mDrawingOrder != 0) {
            ArrayList<View> arrayList = this.mDrawingOrderedChildren;
            if (arrayList == null) {
                this.mDrawingOrderedChildren = new ArrayList<>();
            } else {
                arrayList.clear();
            }
            int childCount = getChildCount();
            for (int i = 0; i < childCount; i++) {
                this.mDrawingOrderedChildren.add(getChildAt(i));
            }
            Collections.sort(this.mDrawingOrderedChildren, sPositionComparator);
        }
    }

    /* access modifiers changed from: package-private */
    public float a(float f) {
        return (float) Math.sin((double) ((f - 0.5f) * 0.47123894f));
    }

    /* access modifiers changed from: package-private */
    public ItemInfo a(int i) {
        for (int i2 = 0; i2 < this.mItems.size(); i2++) {
            ItemInfo itemInfo = this.mItems.get(i2);
            if (itemInfo.b == i) {
                return itemInfo;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public ItemInfo a(int i, int i2) {
        ItemInfo itemInfo = new ItemInfo();
        itemInfo.b = i;
        itemInfo.a = this.a.instantiateItem((ViewGroup) this, i);
        itemInfo.d = this.a.getPageWidth(i);
        if (i2 < 0 || i2 >= this.mItems.size()) {
            this.mItems.add(itemInfo);
        } else {
            this.mItems.add(i2, itemInfo);
        }
        return itemInfo;
    }

    /* access modifiers changed from: package-private */
    public ItemInfo a(View view) {
        while (true) {
            ViewParent parent = view.getParent();
            if (parent == this) {
                return b(view);
            }
            if (parent == null || !(parent instanceof View)) {
                return null;
            }
            view = (View) parent;
        }
    }

    /* access modifiers changed from: package-private */
    public OnPageChangeListener a(OnPageChangeListener onPageChangeListener) {
        OnPageChangeListener onPageChangeListener2 = this.mInternalPageChangeListener;
        this.mInternalPageChangeListener = onPageChangeListener;
        return onPageChangeListener2;
    }

    /* access modifiers changed from: package-private */
    public void a() {
        int count = this.a.getCount();
        this.mExpectedAdapterCount = count;
        boolean z = this.mItems.size() < (this.mOffscreenPageLimit * 2) + 1 && this.mItems.size() < count;
        int i = this.b;
        int i2 = 0;
        boolean z2 = false;
        while (i2 < this.mItems.size()) {
            ItemInfo itemInfo = this.mItems.get(i2);
            int itemPosition = this.a.getItemPosition(itemInfo.a);
            if (itemPosition != -1) {
                if (itemPosition == -2) {
                    this.mItems.remove(i2);
                    i2--;
                    if (!z2) {
                        this.a.startUpdate((ViewGroup) this);
                        z2 = true;
                    }
                    this.a.destroyItem((ViewGroup) this, itemInfo.b, itemInfo.a);
                    int i3 = this.b;
                    if (i3 == itemInfo.b) {
                        i = Math.max(0, Math.min(i3, count - 1));
                    }
                } else {
                    int i4 = itemInfo.b;
                    if (i4 != itemPosition) {
                        if (i4 == this.b) {
                            i = itemPosition;
                        }
                        itemInfo.b = itemPosition;
                    }
                }
                z = true;
            }
            i2++;
        }
        if (z2) {
            this.a.finishUpdate((ViewGroup) this);
        }
        Collections.sort(this.mItems, COMPARATOR);
        if (z) {
            int childCount = getChildCount();
            for (int i5 = 0; i5 < childCount; i5++) {
                LayoutParams layoutParams = (LayoutParams) getChildAt(i5).getLayoutParams();
                if (!layoutParams.isDecor) {
                    layoutParams.a = 0.0f;
                }
            }
            a(i, false, true);
            requestLayout();
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0066  */
    @androidx.annotation.CallSuper
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void a(int r13, float r14, int r15) {
        /*
            r12 = this;
            int r0 = r12.mDecorChildCount
            r1 = 0
            r2 = 1
            if (r0 <= 0) goto L_0x006d
            int r0 = r12.getScrollX()
            int r3 = r12.getPaddingLeft()
            int r4 = r12.getPaddingRight()
            int r5 = r12.getWidth()
            int r6 = r12.getChildCount()
            r7 = r4
            r4 = r3
            r3 = 0
        L_0x001d:
            if (r3 >= r6) goto L_0x006d
            android.view.View r8 = r12.getChildAt(r3)
            android.view.ViewGroup$LayoutParams r9 = r8.getLayoutParams()
            androidx.viewpager.widget.ViewPager$LayoutParams r9 = (androidx.viewpager.widget.ViewPager.LayoutParams) r9
            boolean r10 = r9.isDecor
            if (r10 != 0) goto L_0x002e
            goto L_0x006a
        L_0x002e:
            int r9 = r9.gravity
            r9 = r9 & 7
            if (r9 == r2) goto L_0x004f
            r10 = 3
            if (r9 == r10) goto L_0x0049
            r10 = 5
            if (r9 == r10) goto L_0x003c
            r9 = r4
            goto L_0x005e
        L_0x003c:
            int r9 = r5 - r7
            int r10 = r8.getMeasuredWidth()
            int r9 = r9 - r10
            int r10 = r8.getMeasuredWidth()
            int r7 = r7 + r10
            goto L_0x005b
        L_0x0049:
            int r9 = r8.getWidth()
            int r9 = r9 + r4
            goto L_0x005e
        L_0x004f:
            int r9 = r8.getMeasuredWidth()
            int r9 = r5 - r9
            int r9 = r9 / 2
            int r9 = java.lang.Math.max(r9, r4)
        L_0x005b:
            r11 = r9
            r9 = r4
            r4 = r11
        L_0x005e:
            int r4 = r4 + r0
            int r10 = r8.getLeft()
            int r4 = r4 - r10
            if (r4 == 0) goto L_0x0069
            r8.offsetLeftAndRight(r4)
        L_0x0069:
            r4 = r9
        L_0x006a:
            int r3 = r3 + 1
            goto L_0x001d
        L_0x006d:
            r12.dispatchOnPageScrolled(r13, r14, r15)
            androidx.viewpager.widget.ViewPager$PageTransformer r13 = r12.mPageTransformer
            if (r13 == 0) goto L_0x00a1
            int r13 = r12.getScrollX()
            int r14 = r12.getChildCount()
        L_0x007c:
            if (r1 >= r14) goto L_0x00a1
            android.view.View r15 = r12.getChildAt(r1)
            android.view.ViewGroup$LayoutParams r0 = r15.getLayoutParams()
            androidx.viewpager.widget.ViewPager$LayoutParams r0 = (androidx.viewpager.widget.ViewPager.LayoutParams) r0
            boolean r0 = r0.isDecor
            if (r0 == 0) goto L_0x008d
            goto L_0x009e
        L_0x008d:
            int r0 = r15.getLeft()
            int r0 = r0 - r13
            float r0 = (float) r0
            int r3 = r12.getClientWidth()
            float r3 = (float) r3
            float r0 = r0 / r3
            androidx.viewpager.widget.ViewPager$PageTransformer r3 = r12.mPageTransformer
            r3.transformPage(r15, r0)
        L_0x009e:
            int r1 = r1 + 1
            goto L_0x007c
        L_0x00a1:
            r12.mCalledSuper = r2
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.viewpager.widget.ViewPager.a(int, float, int):void");
    }

    /* access modifiers changed from: package-private */
    public void a(int i, int i2, int i3) {
        int i4;
        if (getChildCount() == 0) {
            setScrollingCacheEnabled(false);
            return;
        }
        Scroller scroller = this.mScroller;
        if (scroller != null && !scroller.isFinished()) {
            i4 = this.mIsScrollStarted ? this.mScroller.getCurrX() : this.mScroller.getStartX();
            this.mScroller.abortAnimation();
            setScrollingCacheEnabled(false);
        } else {
            i4 = getScrollX();
        }
        int i5 = i4;
        int scrollY = getScrollY();
        int i6 = i - i5;
        int i7 = i2 - scrollY;
        if (i6 == 0 && i7 == 0) {
            completeScroll(false);
            e();
            setScrollState(0);
            return;
        }
        setScrollingCacheEnabled(true);
        setScrollState(2);
        int clientWidth = getClientWidth();
        int i8 = clientWidth / 2;
        float f = (float) clientWidth;
        float f2 = (float) i8;
        float a2 = f2 + (a(Math.min(1.0f, (((float) Math.abs(i6)) * 1.0f) / f)) * f2);
        int abs = Math.abs(i3);
        int min = Math.min(abs > 0 ? Math.round(Math.abs(a2 / ((float) abs)) * 1000.0f) * 4 : (int) (((((float) Math.abs(i6)) / ((f * this.a.getPageWidth(this.b)) + ((float) this.mPageMargin))) + 1.0f) * 100.0f), MAX_SETTLE_DURATION);
        this.mIsScrollStarted = false;
        this.mScroller.startScroll(i5, scrollY, i6, i7, min);
        ViewCompat.postInvalidateOnAnimation(this);
    }

    /* access modifiers changed from: package-private */
    public void a(int i, boolean z, boolean z2) {
        a(i, z, z2, 0);
    }

    /* access modifiers changed from: package-private */
    public void a(int i, boolean z, boolean z2, int i2) {
        PagerAdapter pagerAdapter = this.a;
        if (pagerAdapter == null || pagerAdapter.getCount() <= 0) {
            setScrollingCacheEnabled(false);
        } else if (z2 || this.b != i || this.mItems.size() == 0) {
            boolean z3 = true;
            if (i < 0) {
                i = 0;
            } else if (i >= this.a.getCount()) {
                i = this.a.getCount() - 1;
            }
            int i3 = this.mOffscreenPageLimit;
            int i4 = this.b;
            if (i > i4 + i3 || i < i4 - i3) {
                for (int i5 = 0; i5 < this.mItems.size(); i5++) {
                    this.mItems.get(i5).c = true;
                }
            }
            if (this.b == i) {
                z3 = false;
            }
            if (this.mFirstLayout) {
                this.b = i;
                if (z3) {
                    dispatchOnPageSelected(i);
                }
                requestLayout();
                return;
            }
            b(i);
            scrollToItem(i, z, i2, z3);
        } else {
            setScrollingCacheEnabled(false);
        }
    }

    /* access modifiers changed from: protected */
    public boolean a(View view, boolean z, int i, int i2, int i3) {
        int i4;
        View view2 = view;
        if (view2 instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view2;
            int scrollX = view.getScrollX();
            int scrollY = view.getScrollY();
            for (int childCount = viewGroup.getChildCount() - 1; childCount >= 0; childCount--) {
                View childAt = viewGroup.getChildAt(childCount);
                int i5 = i2 + scrollX;
                if (i5 >= childAt.getLeft() && i5 < childAt.getRight() && (i4 = i3 + scrollY) >= childAt.getTop() && i4 < childAt.getBottom()) {
                    if (a(childAt, true, i, i5 - childAt.getLeft(), i4 - childAt.getTop())) {
                        return true;
                    }
                }
            }
        }
        return z && view.canScrollHorizontally(-i);
    }

    public void addFocusables(ArrayList<View> arrayList, int i, int i2) {
        ItemInfo b2;
        int size = arrayList.size();
        int descendantFocusability = getDescendantFocusability();
        if (descendantFocusability != 393216) {
            for (int i3 = 0; i3 < getChildCount(); i3++) {
                View childAt = getChildAt(i3);
                if (childAt.getVisibility() == 0 && (b2 = b(childAt)) != null && b2.b == this.b) {
                    childAt.addFocusables(arrayList, i, i2);
                }
            }
        }
        if ((descendantFocusability == 262144 && size != arrayList.size()) || !isFocusable()) {
            return;
        }
        if (((i2 & 1) != 1 || !isInTouchMode() || isFocusableInTouchMode()) && arrayList != null) {
            arrayList.add(this);
        }
    }

    public void addOnAdapterChangeListener(@NonNull OnAdapterChangeListener onAdapterChangeListener) {
        if (this.mAdapterChangeListeners == null) {
            this.mAdapterChangeListeners = new ArrayList();
        }
        this.mAdapterChangeListeners.add(onAdapterChangeListener);
    }

    public void addOnPageChangeListener(@NonNull OnPageChangeListener onPageChangeListener) {
        if (this.mOnPageChangeListeners == null) {
            this.mOnPageChangeListeners = new ArrayList();
        }
        this.mOnPageChangeListeners.add(onPageChangeListener);
    }

    public void addTouchables(ArrayList<View> arrayList) {
        ItemInfo b2;
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            if (childAt.getVisibility() == 0 && (b2 = b(childAt)) != null && b2.b == this.b) {
                childAt.addTouchables(arrayList);
            }
        }
    }

    public void addView(View view, int i, ViewGroup.LayoutParams layoutParams) {
        if (!checkLayoutParams(layoutParams)) {
            layoutParams = generateLayoutParams(layoutParams);
        }
        LayoutParams layoutParams2 = (LayoutParams) layoutParams;
        layoutParams2.isDecor |= isDecorView(view);
        if (!this.mInLayout) {
            super.addView(view, i, layoutParams);
        } else if (layoutParams2 == null || !layoutParams2.isDecor) {
            layoutParams2.b = true;
            addViewInLayout(view, i, layoutParams);
        } else {
            throw new IllegalStateException("Cannot add pager decor view during layout");
        }
    }

    public boolean arrowScroll(int i) {
        boolean d;
        boolean z;
        View findFocus = findFocus();
        boolean z2 = false;
        View view = null;
        if (findFocus != this) {
            if (findFocus != null) {
                ViewParent parent = findFocus.getParent();
                while (true) {
                    if (!(parent instanceof ViewGroup)) {
                        z = false;
                        break;
                    } else if (parent == this) {
                        z = true;
                        break;
                    } else {
                        parent = parent.getParent();
                    }
                }
                if (!z) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(findFocus.getClass().getSimpleName());
                    for (ViewParent parent2 = findFocus.getParent(); parent2 instanceof ViewGroup; parent2 = parent2.getParent()) {
                        sb.append(" => ");
                        sb.append(parent2.getClass().getSimpleName());
                    }
                    Log.e(TAG, "arrowScroll tried to find focus based on non-child current focused view " + sb.toString());
                }
            }
            view = findFocus;
        }
        View findNextFocus = FocusFinder.getInstance().findNextFocus(this, view, i);
        if (findNextFocus != null && findNextFocus != view) {
            if (i == 17) {
                int i2 = getChildRectInPagerCoordinates(this.mTempRect, findNextFocus).left;
                int i3 = getChildRectInPagerCoordinates(this.mTempRect, view).left;
                if (view != null && i2 >= i3) {
                    d = c();
                    z2 = d;
                }
            } else if (i == 66) {
                int i4 = getChildRectInPagerCoordinates(this.mTempRect, findNextFocus).left;
                int i5 = getChildRectInPagerCoordinates(this.mTempRect, view).left;
                if (view != null && i4 <= i5) {
                    d = d();
                    z2 = d;
                }
            }
            d = findNextFocus.requestFocus();
            z2 = d;
        } else if (i == 17 || i == 1) {
            z2 = c();
        } else if (i == 66 || i == 2) {
            z2 = d();
        }
        if (z2) {
            playSoundEffect(SoundEffectConstants.getContantForFocusDirection(i));
        }
        return z2;
    }

    /* access modifiers changed from: package-private */
    public ItemInfo b(View view) {
        for (int i = 0; i < this.mItems.size(); i++) {
            ItemInfo itemInfo = this.mItems.get(i);
            if (this.a.isViewFromObject(view, itemInfo.a)) {
                return itemInfo;
            }
        }
        return null;
    }

    /* access modifiers changed from: package-private */
    public void b() {
        setWillNotDraw(false);
        setDescendantFocusability(262144);
        setFocusable(true);
        Context context = getContext();
        this.mScroller = new Scroller(context, sInterpolator);
        ViewConfiguration viewConfiguration = ViewConfiguration.get(context);
        float f = context.getResources().getDisplayMetrics().density;
        this.mTouchSlop = viewConfiguration.getScaledPagingTouchSlop();
        this.mMinimumVelocity = (int) (400.0f * f);
        this.mMaximumVelocity = viewConfiguration.getScaledMaximumFlingVelocity();
        this.mLeftEdge = new EdgeEffect(context);
        this.mRightEdge = new EdgeEffect(context);
        this.mFlingDistance = (int) (25.0f * f);
        this.mCloseEnough = (int) (2.0f * f);
        this.mDefaultGutterSize = (int) (f * 16.0f);
        ViewCompat.setAccessibilityDelegate(this, new MyAccessibilityDelegate());
        if (ViewCompat.getImportantForAccessibility(this) == 0) {
            ViewCompat.setImportantForAccessibility(this, 1);
        }
        ViewCompat.setOnApplyWindowInsetsListener(this, new OnApplyWindowInsetsListener() {
            private final Rect mTempRect = new Rect();

            public WindowInsetsCompat onApplyWindowInsets(View view, WindowInsetsCompat windowInsetsCompat) {
                WindowInsetsCompat onApplyWindowInsets = ViewCompat.onApplyWindowInsets(view, windowInsetsCompat);
                if (onApplyWindowInsets.isConsumed()) {
                    return onApplyWindowInsets;
                }
                Rect rect = this.mTempRect;
                rect.left = onApplyWindowInsets.getSystemWindowInsetLeft();
                rect.top = onApplyWindowInsets.getSystemWindowInsetTop();
                rect.right = onApplyWindowInsets.getSystemWindowInsetRight();
                rect.bottom = onApplyWindowInsets.getSystemWindowInsetBottom();
                int childCount = ViewPager.this.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    WindowInsetsCompat dispatchApplyWindowInsets = ViewCompat.dispatchApplyWindowInsets(ViewPager.this.getChildAt(i), onApplyWindowInsets);
                    rect.left = Math.min(dispatchApplyWindowInsets.getSystemWindowInsetLeft(), rect.left);
                    rect.top = Math.min(dispatchApplyWindowInsets.getSystemWindowInsetTop(), rect.top);
                    rect.right = Math.min(dispatchApplyWindowInsets.getSystemWindowInsetRight(), rect.right);
                    rect.bottom = Math.min(dispatchApplyWindowInsets.getSystemWindowInsetBottom(), rect.bottom);
                }
                return onApplyWindowInsets.replaceSystemWindowInsets(rect.left, rect.top, rect.right, rect.bottom);
            }
        });
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0060, code lost:
        if (r9 == r10) goto L_0x0067;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0066, code lost:
        r8 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x00c3, code lost:
        if (r15 >= 0) goto L_0x00e1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x00d1, code lost:
        if (r15 >= 0) goto L_0x00e1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:56:0x00df, code lost:
        if (r15 >= 0) goto L_0x00e1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00ea, code lost:
        r5 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:82:0x013f, code lost:
        if (r4 < r0.mItems.size()) goto L_0x0141;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:84:0x014a, code lost:
        r5 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:89:0x015d, code lost:
        if (r4 < r0.mItems.size()) goto L_0x0141;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:91:0x016f, code lost:
        if (r4 < r0.mItems.size()) goto L_0x0141;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void b(int r18) {
        /*
            r17 = this;
            r0 = r17
            r1 = r18
            int r2 = r0.b
            if (r2 == r1) goto L_0x000f
            androidx.viewpager.widget.ViewPager$ItemInfo r2 = r0.a((int) r2)
            r0.b = r1
            goto L_0x0010
        L_0x000f:
            r2 = 0
        L_0x0010:
            androidx.viewpager.widget.PagerAdapter r1 = r0.a
            if (r1 != 0) goto L_0x0018
            r17.sortChildDrawingOrder()
            return
        L_0x0018:
            boolean r1 = r0.mPopulatePending
            if (r1 == 0) goto L_0x0020
            r17.sortChildDrawingOrder()
            return
        L_0x0020:
            android.os.IBinder r1 = r17.getWindowToken()
            if (r1 != 0) goto L_0x0027
            return
        L_0x0027:
            androidx.viewpager.widget.PagerAdapter r1 = r0.a
            r1.startUpdate((android.view.ViewGroup) r0)
            int r1 = r0.mOffscreenPageLimit
            int r4 = r0.b
            int r4 = r4 - r1
            r5 = 0
            int r4 = java.lang.Math.max(r5, r4)
            androidx.viewpager.widget.PagerAdapter r6 = r0.a
            int r6 = r6.getCount()
            int r7 = r6 + -1
            int r8 = r0.b
            int r8 = r8 + r1
            int r1 = java.lang.Math.min(r7, r8)
            int r7 = r0.mExpectedAdapterCount
            if (r6 != r7) goto L_0x01f2
            r7 = 0
        L_0x004a:
            java.util.ArrayList<androidx.viewpager.widget.ViewPager$ItemInfo> r8 = r0.mItems
            int r8 = r8.size()
            if (r7 >= r8) goto L_0x0066
            java.util.ArrayList<androidx.viewpager.widget.ViewPager$ItemInfo> r8 = r0.mItems
            java.lang.Object r8 = r8.get(r7)
            androidx.viewpager.widget.ViewPager$ItemInfo r8 = (androidx.viewpager.widget.ViewPager.ItemInfo) r8
            int r9 = r8.b
            int r10 = r0.b
            if (r9 < r10) goto L_0x0063
            if (r9 != r10) goto L_0x0066
            goto L_0x0067
        L_0x0063:
            int r7 = r7 + 1
            goto L_0x004a
        L_0x0066:
            r8 = 0
        L_0x0067:
            if (r8 != 0) goto L_0x0071
            if (r6 <= 0) goto L_0x0071
            int r8 = r0.b
            androidx.viewpager.widget.ViewPager$ItemInfo r8 = r0.a(r8, r7)
        L_0x0071:
            r9 = 0
            if (r8 == 0) goto L_0x017f
            int r10 = r7 + -1
            if (r10 < 0) goto L_0x0081
            java.util.ArrayList<androidx.viewpager.widget.ViewPager$ItemInfo> r11 = r0.mItems
            java.lang.Object r11 = r11.get(r10)
            androidx.viewpager.widget.ViewPager$ItemInfo r11 = (androidx.viewpager.widget.ViewPager.ItemInfo) r11
            goto L_0x0082
        L_0x0081:
            r11 = 0
        L_0x0082:
            int r12 = r17.getClientWidth()
            r13 = 1073741824(0x40000000, float:2.0)
            if (r12 > 0) goto L_0x008c
            r3 = 0
            goto L_0x0099
        L_0x008c:
            float r14 = r8.d
            float r14 = r13 - r14
            int r15 = r17.getPaddingLeft()
            float r15 = (float) r15
            float r3 = (float) r12
            float r15 = r15 / r3
            float r3 = r14 + r15
        L_0x0099:
            int r14 = r0.b
            int r14 = r14 + -1
            r15 = r10
            r10 = r7
            r7 = 0
        L_0x00a0:
            if (r14 < 0) goto L_0x00f0
            int r16 = (r7 > r3 ? 1 : (r7 == r3 ? 0 : -1))
            if (r16 < 0) goto L_0x00c6
            if (r14 >= r4) goto L_0x00c6
            if (r11 != 0) goto L_0x00ab
            goto L_0x00f0
        L_0x00ab:
            int r5 = r11.b
            if (r14 != r5) goto L_0x00ec
            boolean r5 = r11.c
            if (r5 != 0) goto L_0x00ec
            java.util.ArrayList<androidx.viewpager.widget.ViewPager$ItemInfo> r5 = r0.mItems
            r5.remove(r15)
            androidx.viewpager.widget.PagerAdapter r5 = r0.a
            java.lang.Object r11 = r11.a
            r5.destroyItem((android.view.ViewGroup) r0, (int) r14, (java.lang.Object) r11)
            int r15 = r15 + -1
            int r10 = r10 + -1
            if (r15 < 0) goto L_0x00ea
            goto L_0x00e1
        L_0x00c6:
            if (r11 == 0) goto L_0x00d4
            int r5 = r11.b
            if (r14 != r5) goto L_0x00d4
            float r5 = r11.d
            float r7 = r7 + r5
            int r15 = r15 + -1
            if (r15 < 0) goto L_0x00ea
            goto L_0x00e1
        L_0x00d4:
            int r5 = r15 + 1
            androidx.viewpager.widget.ViewPager$ItemInfo r5 = r0.a(r14, r5)
            float r5 = r5.d
            float r7 = r7 + r5
            int r10 = r10 + 1
            if (r15 < 0) goto L_0x00ea
        L_0x00e1:
            java.util.ArrayList<androidx.viewpager.widget.ViewPager$ItemInfo> r5 = r0.mItems
            java.lang.Object r5 = r5.get(r15)
            androidx.viewpager.widget.ViewPager$ItemInfo r5 = (androidx.viewpager.widget.ViewPager.ItemInfo) r5
            goto L_0x00eb
        L_0x00ea:
            r5 = 0
        L_0x00eb:
            r11 = r5
        L_0x00ec:
            int r14 = r14 + -1
            r5 = 0
            goto L_0x00a0
        L_0x00f0:
            float r3 = r8.d
            int r4 = r10 + 1
            int r5 = (r3 > r13 ? 1 : (r3 == r13 ? 0 : -1))
            if (r5 >= 0) goto L_0x0173
            java.util.ArrayList<androidx.viewpager.widget.ViewPager$ItemInfo> r5 = r0.mItems
            int r5 = r5.size()
            if (r4 >= r5) goto L_0x0109
            java.util.ArrayList<androidx.viewpager.widget.ViewPager$ItemInfo> r5 = r0.mItems
            java.lang.Object r5 = r5.get(r4)
            androidx.viewpager.widget.ViewPager$ItemInfo r5 = (androidx.viewpager.widget.ViewPager.ItemInfo) r5
            goto L_0x010a
        L_0x0109:
            r5 = 0
        L_0x010a:
            if (r12 > 0) goto L_0x010e
            r7 = 0
            goto L_0x0116
        L_0x010e:
            int r7 = r17.getPaddingRight()
            float r7 = (float) r7
            float r11 = (float) r12
            float r7 = r7 / r11
            float r7 = r7 + r13
        L_0x0116:
            int r11 = r0.b
        L_0x0118:
            int r11 = r11 + 1
            if (r11 >= r6) goto L_0x0173
            int r12 = (r3 > r7 ? 1 : (r3 == r7 ? 0 : -1))
            if (r12 < 0) goto L_0x014c
            if (r11 <= r1) goto L_0x014c
            if (r5 != 0) goto L_0x0125
            goto L_0x0173
        L_0x0125:
            int r12 = r5.b
            if (r11 != r12) goto L_0x0172
            boolean r12 = r5.c
            if (r12 != 0) goto L_0x0172
            java.util.ArrayList<androidx.viewpager.widget.ViewPager$ItemInfo> r12 = r0.mItems
            r12.remove(r4)
            androidx.viewpager.widget.PagerAdapter r12 = r0.a
            java.lang.Object r5 = r5.a
            r12.destroyItem((android.view.ViewGroup) r0, (int) r11, (java.lang.Object) r5)
            java.util.ArrayList<androidx.viewpager.widget.ViewPager$ItemInfo> r5 = r0.mItems
            int r5 = r5.size()
            if (r4 >= r5) goto L_0x014a
        L_0x0141:
            java.util.ArrayList<androidx.viewpager.widget.ViewPager$ItemInfo> r5 = r0.mItems
            java.lang.Object r5 = r5.get(r4)
            androidx.viewpager.widget.ViewPager$ItemInfo r5 = (androidx.viewpager.widget.ViewPager.ItemInfo) r5
            goto L_0x0172
        L_0x014a:
            r5 = 0
            goto L_0x0172
        L_0x014c:
            if (r5 == 0) goto L_0x0160
            int r12 = r5.b
            if (r11 != r12) goto L_0x0160
            float r5 = r5.d
            float r3 = r3 + r5
            int r4 = r4 + 1
            java.util.ArrayList<androidx.viewpager.widget.ViewPager$ItemInfo> r5 = r0.mItems
            int r5 = r5.size()
            if (r4 >= r5) goto L_0x014a
            goto L_0x0141
        L_0x0160:
            androidx.viewpager.widget.ViewPager$ItemInfo r5 = r0.a(r11, r4)
            int r4 = r4 + 1
            float r5 = r5.d
            float r3 = r3 + r5
            java.util.ArrayList<androidx.viewpager.widget.ViewPager$ItemInfo> r5 = r0.mItems
            int r5 = r5.size()
            if (r4 >= r5) goto L_0x014a
            goto L_0x0141
        L_0x0172:
            goto L_0x0118
        L_0x0173:
            r0.calculatePageOffsets(r8, r10, r2)
            androidx.viewpager.widget.PagerAdapter r1 = r0.a
            int r2 = r0.b
            java.lang.Object r3 = r8.a
            r1.setPrimaryItem((android.view.ViewGroup) r0, (int) r2, (java.lang.Object) r3)
        L_0x017f:
            androidx.viewpager.widget.PagerAdapter r1 = r0.a
            r1.finishUpdate((android.view.ViewGroup) r0)
            int r1 = r17.getChildCount()
            r2 = 0
        L_0x0189:
            if (r2 >= r1) goto L_0x01b2
            android.view.View r3 = r0.getChildAt(r2)
            android.view.ViewGroup$LayoutParams r4 = r3.getLayoutParams()
            androidx.viewpager.widget.ViewPager$LayoutParams r4 = (androidx.viewpager.widget.ViewPager.LayoutParams) r4
            r4.d = r2
            boolean r5 = r4.isDecor
            if (r5 != 0) goto L_0x01af
            float r5 = r4.a
            int r5 = (r5 > r9 ? 1 : (r5 == r9 ? 0 : -1))
            if (r5 != 0) goto L_0x01af
            androidx.viewpager.widget.ViewPager$ItemInfo r3 = r0.b((android.view.View) r3)
            if (r3 == 0) goto L_0x01af
            float r5 = r3.d
            r4.a = r5
            int r3 = r3.b
            r4.c = r3
        L_0x01af:
            int r2 = r2 + 1
            goto L_0x0189
        L_0x01b2:
            r17.sortChildDrawingOrder()
            boolean r1 = r17.hasFocus()
            if (r1 == 0) goto L_0x01f1
            android.view.View r1 = r17.findFocus()
            if (r1 == 0) goto L_0x01c6
            androidx.viewpager.widget.ViewPager$ItemInfo r3 = r0.a((android.view.View) r1)
            goto L_0x01c7
        L_0x01c6:
            r3 = 0
        L_0x01c7:
            if (r3 == 0) goto L_0x01cf
            int r1 = r3.b
            int r2 = r0.b
            if (r1 == r2) goto L_0x01f1
        L_0x01cf:
            r1 = 0
        L_0x01d0:
            int r2 = r17.getChildCount()
            if (r1 >= r2) goto L_0x01f1
            android.view.View r2 = r0.getChildAt(r1)
            androidx.viewpager.widget.ViewPager$ItemInfo r3 = r0.b((android.view.View) r2)
            if (r3 == 0) goto L_0x01ee
            int r3 = r3.b
            int r4 = r0.b
            if (r3 != r4) goto L_0x01ee
            r3 = 2
            boolean r2 = r2.requestFocus(r3)
            if (r2 == 0) goto L_0x01ee
            goto L_0x01f1
        L_0x01ee:
            int r1 = r1 + 1
            goto L_0x01d0
        L_0x01f1:
            return
        L_0x01f2:
            android.content.res.Resources r1 = r17.getResources()     // Catch:{ NotFoundException -> 0x01ff }
            int r2 = r17.getId()     // Catch:{ NotFoundException -> 0x01ff }
            java.lang.String r1 = r1.getResourceName(r2)     // Catch:{ NotFoundException -> 0x01ff }
            goto L_0x0207
        L_0x01ff:
            int r1 = r17.getId()
            java.lang.String r1 = java.lang.Integer.toHexString(r1)
        L_0x0207:
            java.lang.IllegalStateException r2 = new java.lang.IllegalStateException
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = "The application's PagerAdapter changed the adapter's contents without calling PagerAdapter#notifyDataSetChanged! Expected adapter item count: "
            r3.append(r4)
            int r4 = r0.mExpectedAdapterCount
            r3.append(r4)
            java.lang.String r4 = ", found: "
            r3.append(r4)
            r3.append(r6)
            java.lang.String r4 = " Pager id: "
            r3.append(r4)
            r3.append(r1)
            java.lang.String r1 = " Pager class: "
            r3.append(r1)
            java.lang.Class<androidx.viewpager.widget.ViewPager> r1 = androidx.viewpager.widget.ViewPager.class
            r3.append(r1)
            java.lang.String r1 = " Problematic adapter: "
            r3.append(r1)
            androidx.viewpager.widget.PagerAdapter r1 = r0.a
            java.lang.Class r1 = r1.getClass()
            r3.append(r1)
            java.lang.String r1 = r3.toString()
            r2.<init>(r1)
            goto L_0x0249
        L_0x0248:
            throw r2
        L_0x0249:
            goto L_0x0248
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.viewpager.widget.ViewPager.b(int):void");
    }

    public boolean beginFakeDrag() {
        if (this.mIsBeingDragged) {
            return false;
        }
        this.mFakeDragging = true;
        setScrollState(1);
        this.mLastMotionX = 0.0f;
        this.mInitialMotionX = 0.0f;
        VelocityTracker velocityTracker = this.mVelocityTracker;
        if (velocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        } else {
            velocityTracker.clear();
        }
        long uptimeMillis = SystemClock.uptimeMillis();
        MotionEvent obtain = MotionEvent.obtain(uptimeMillis, uptimeMillis, 0, 0.0f, 0.0f, 0);
        this.mVelocityTracker.addMovement(obtain);
        obtain.recycle();
        this.mFakeDragBeginTime = uptimeMillis;
        return true;
    }

    /* access modifiers changed from: package-private */
    public boolean c() {
        int i = this.b;
        if (i <= 0) {
            return false;
        }
        setCurrentItem(i - 1, true);
        return true;
    }

    public boolean canScrollHorizontally(int i) {
        if (this.a == null) {
            return false;
        }
        int clientWidth = getClientWidth();
        int scrollX = getScrollX();
        return i < 0 ? scrollX > ((int) (((float) clientWidth) * this.mFirstOffset)) : i > 0 && scrollX < ((int) (((float) clientWidth) * this.mLastOffset));
    }

    /* access modifiers changed from: protected */
    public boolean checkLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return (layoutParams instanceof LayoutParams) && super.checkLayoutParams(layoutParams);
    }

    public void clearOnPageChangeListeners() {
        List<OnPageChangeListener> list = this.mOnPageChangeListeners;
        if (list != null) {
            list.clear();
        }
    }

    public void computeScroll() {
        this.mIsScrollStarted = true;
        if (this.mScroller.isFinished() || !this.mScroller.computeScrollOffset()) {
            completeScroll(true);
            return;
        }
        int scrollX = getScrollX();
        int scrollY = getScrollY();
        int currX = this.mScroller.getCurrX();
        int currY = this.mScroller.getCurrY();
        if (!(scrollX == currX && scrollY == currY)) {
            scrollTo(currX, currY);
            if (!pageScrolled(currX)) {
                this.mScroller.abortAnimation();
                scrollTo(0, currY);
            }
        }
        ViewCompat.postInvalidateOnAnimation(this);
    }

    /* access modifiers changed from: package-private */
    public boolean d() {
        PagerAdapter pagerAdapter = this.a;
        if (pagerAdapter == null || this.b >= pagerAdapter.getCount() - 1) {
            return false;
        }
        setCurrentItem(this.b + 1, true);
        return true;
    }

    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        return super.dispatchKeyEvent(keyEvent) || executeKeyEvent(keyEvent);
    }

    public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
        ItemInfo b2;
        if (accessibilityEvent.getEventType() == 4096) {
            return super.dispatchPopulateAccessibilityEvent(accessibilityEvent);
        }
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            if (childAt.getVisibility() == 0 && (b2 = b(childAt)) != null && b2.b == this.b && childAt.dispatchPopulateAccessibilityEvent(accessibilityEvent)) {
                return true;
            }
        }
        return false;
    }

    public void draw(Canvas canvas) {
        PagerAdapter pagerAdapter;
        super.draw(canvas);
        int overScrollMode = getOverScrollMode();
        boolean z = false;
        if (overScrollMode == 0 || (overScrollMode == 1 && (pagerAdapter = this.a) != null && pagerAdapter.getCount() > 1)) {
            if (!this.mLeftEdge.isFinished()) {
                int save = canvas.save();
                int height = (getHeight() - getPaddingTop()) - getPaddingBottom();
                int width = getWidth();
                canvas.rotate(270.0f);
                canvas.translate((float) ((-height) + getPaddingTop()), this.mFirstOffset * ((float) width));
                this.mLeftEdge.setSize(height, width);
                z = false | this.mLeftEdge.draw(canvas);
                canvas.restoreToCount(save);
            }
            if (!this.mRightEdge.isFinished()) {
                int save2 = canvas.save();
                int width2 = getWidth();
                int height2 = (getHeight() - getPaddingTop()) - getPaddingBottom();
                canvas.rotate(90.0f);
                canvas.translate((float) (-getPaddingTop()), (-(this.mLastOffset + 1.0f)) * ((float) width2));
                this.mRightEdge.setSize(height2, width2);
                z |= this.mRightEdge.draw(canvas);
                canvas.restoreToCount(save2);
            }
        } else {
            this.mLeftEdge.finish();
            this.mRightEdge.finish();
        }
        if (z) {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /* access modifiers changed from: protected */
    public void drawableStateChanged() {
        super.drawableStateChanged();
        Drawable drawable = this.mMarginDrawable;
        if (drawable != null && drawable.isStateful()) {
            drawable.setState(getDrawableState());
        }
    }

    /* access modifiers changed from: package-private */
    public void e() {
        b(this.b);
    }

    public void endFakeDrag() {
        if (this.mFakeDragging) {
            if (this.a != null) {
                VelocityTracker velocityTracker = this.mVelocityTracker;
                velocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumVelocity);
                int xVelocity = (int) velocityTracker.getXVelocity(this.mActivePointerId);
                this.mPopulatePending = true;
                int clientWidth = getClientWidth();
                int scrollX = getScrollX();
                ItemInfo infoForCurrentScrollPosition = infoForCurrentScrollPosition();
                a(determineTargetPage(infoForCurrentScrollPosition.b, ((((float) scrollX) / ((float) clientWidth)) - infoForCurrentScrollPosition.e) / infoForCurrentScrollPosition.d, xVelocity, (int) (this.mLastMotionX - this.mInitialMotionX)), true, true, xVelocity);
            }
            endDrag();
            this.mFakeDragging = false;
            return;
        }
        throw new IllegalStateException("No fake drag in progress. Call beginFakeDrag first.");
    }

    public boolean executeKeyEvent(@NonNull KeyEvent keyEvent) {
        int i;
        if (keyEvent.getAction() == 0) {
            int keyCode = keyEvent.getKeyCode();
            if (keyCode != 21) {
                if (keyCode != 22) {
                    if (keyCode == 61) {
                        if (keyEvent.hasNoModifiers()) {
                            return arrowScroll(2);
                        }
                        if (keyEvent.hasModifiers(1)) {
                            return arrowScroll(1);
                        }
                    }
                } else if (keyEvent.hasModifiers(2)) {
                    return d();
                } else {
                    i = 66;
                }
            } else if (keyEvent.hasModifiers(2)) {
                return c();
            } else {
                i = 17;
            }
            return arrowScroll(i);
        }
        return false;
    }

    public void fakeDragBy(float f) {
        if (!this.mFakeDragging) {
            throw new IllegalStateException("No fake drag in progress. Call beginFakeDrag first.");
        } else if (this.a != null) {
            this.mLastMotionX += f;
            float scrollX = ((float) getScrollX()) - f;
            float clientWidth = (float) getClientWidth();
            float f2 = this.mFirstOffset * clientWidth;
            float f3 = this.mLastOffset * clientWidth;
            ItemInfo itemInfo = this.mItems.get(0);
            ArrayList<ItemInfo> arrayList = this.mItems;
            ItemInfo itemInfo2 = arrayList.get(arrayList.size() - 1);
            if (itemInfo.b != 0) {
                f2 = itemInfo.e * clientWidth;
            }
            if (itemInfo2.b != this.a.getCount() - 1) {
                f3 = itemInfo2.e * clientWidth;
            }
            if (scrollX < f2) {
                scrollX = f2;
            } else if (scrollX > f3) {
                scrollX = f3;
            }
            int i = (int) scrollX;
            this.mLastMotionX += scrollX - ((float) i);
            scrollTo(i, getScrollY());
            pageScrolled(i);
            MotionEvent obtain = MotionEvent.obtain(this.mFakeDragBeginTime, SystemClock.uptimeMillis(), 2, this.mLastMotionX, 0.0f, 0);
            this.mVelocityTracker.addMovement(obtain);
            obtain.recycle();
        }
    }

    /* access modifiers changed from: protected */
    public ViewGroup.LayoutParams generateDefaultLayoutParams() {
        return new LayoutParams();
    }

    public ViewGroup.LayoutParams generateLayoutParams(AttributeSet attributeSet) {
        return new LayoutParams(getContext(), attributeSet);
    }

    /* access modifiers changed from: protected */
    public ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return generateDefaultLayoutParams();
    }

    @Nullable
    public PagerAdapter getAdapter() {
        return this.a;
    }

    /* access modifiers changed from: protected */
    public int getChildDrawingOrder(int i, int i2) {
        if (this.mDrawingOrder == 2) {
            i2 = (i - 1) - i2;
        }
        return ((LayoutParams) this.mDrawingOrderedChildren.get(i2).getLayoutParams()).d;
    }

    public int getCurrentItem() {
        return this.b;
    }

    public int getOffscreenPageLimit() {
        return this.mOffscreenPageLimit;
    }

    public int getPageMargin() {
        return this.mPageMargin;
    }

    public boolean isFakeDragging() {
        return this.mFakeDragging;
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mFirstLayout = true;
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        removeCallbacks(this.mEndScrollRunnable);
        Scroller scroller = this.mScroller;
        if (scroller != null && !scroller.isFinished()) {
            this.mScroller.abortAnimation();
        }
        super.onDetachedFromWindow();
    }

    /* access modifiers changed from: protected */
    public void onDraw(Canvas canvas) {
        float f;
        float f2;
        super.onDraw(canvas);
        if (this.mPageMargin > 0 && this.mMarginDrawable != null && this.mItems.size() > 0 && this.a != null) {
            int scrollX = getScrollX();
            int width = getWidth();
            float f3 = (float) width;
            float f4 = ((float) this.mPageMargin) / f3;
            int i = 0;
            ItemInfo itemInfo = this.mItems.get(0);
            float f5 = itemInfo.e;
            int size = this.mItems.size();
            int i2 = itemInfo.b;
            int i3 = this.mItems.get(size - 1).b;
            while (i2 < i3) {
                while (i2 > itemInfo.b && i < size) {
                    i++;
                    itemInfo = this.mItems.get(i);
                }
                if (i2 == itemInfo.b) {
                    float f6 = itemInfo.e;
                    float f7 = itemInfo.d;
                    f = (f6 + f7) * f3;
                    f5 = f6 + f7 + f4;
                } else {
                    float pageWidth = this.a.getPageWidth(i2);
                    f = (f5 + pageWidth) * f3;
                    f5 += pageWidth + f4;
                }
                if (((float) this.mPageMargin) + f > ((float) scrollX)) {
                    f2 = f4;
                    this.mMarginDrawable.setBounds(Math.round(f), this.mTopPageBounds, Math.round(((float) this.mPageMargin) + f), this.mBottomPageBounds);
                    this.mMarginDrawable.draw(canvas);
                } else {
                    Canvas canvas2 = canvas;
                    f2 = f4;
                }
                if (f <= ((float) (scrollX + width))) {
                    i2++;
                    f4 = f2;
                } else {
                    return;
                }
            }
        }
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        MotionEvent motionEvent2 = motionEvent;
        int action = motionEvent.getAction() & 255;
        if (action == 3 || action == 1) {
            resetTouch();
            return false;
        }
        if (action != 0) {
            if (this.mIsBeingDragged) {
                return true;
            }
            if (this.mIsUnableToDrag) {
                return false;
            }
        }
        if (action == 0) {
            float x = motionEvent.getX();
            this.mInitialMotionX = x;
            this.mLastMotionX = x;
            float y = motionEvent.getY();
            this.mInitialMotionY = y;
            this.mLastMotionY = y;
            this.mActivePointerId = motionEvent2.getPointerId(0);
            this.mIsUnableToDrag = false;
            this.mIsScrollStarted = true;
            this.mScroller.computeScrollOffset();
            if (this.mScrollState != 2 || Math.abs(this.mScroller.getFinalX() - this.mScroller.getCurrX()) <= this.mCloseEnough) {
                completeScroll(false);
                this.mIsBeingDragged = false;
            } else {
                this.mScroller.abortAnimation();
                this.mPopulatePending = false;
                e();
                this.mIsBeingDragged = true;
                requestParentDisallowInterceptTouchEvent(true);
                setScrollState(1);
            }
        } else if (action == 2) {
            int i = this.mActivePointerId;
            if (i != -1) {
                int findPointerIndex = motionEvent2.findPointerIndex(i);
                float x2 = motionEvent2.getX(findPointerIndex);
                float f = x2 - this.mLastMotionX;
                float abs = Math.abs(f);
                float y2 = motionEvent2.getY(findPointerIndex);
                float abs2 = Math.abs(y2 - this.mInitialMotionY);
                if (f != 0.0f && !isGutterDrag(this.mLastMotionX, f)) {
                    if (a(this, false, (int) f, (int) x2, (int) y2)) {
                        this.mLastMotionX = x2;
                        this.mLastMotionY = y2;
                        this.mIsUnableToDrag = true;
                        return false;
                    }
                }
                if (abs > ((float) this.mTouchSlop) && abs * 0.5f > abs2) {
                    this.mIsBeingDragged = true;
                    requestParentDisallowInterceptTouchEvent(true);
                    setScrollState(1);
                    this.mLastMotionX = f > 0.0f ? this.mInitialMotionX + ((float) this.mTouchSlop) : this.mInitialMotionX - ((float) this.mTouchSlop);
                    this.mLastMotionY = y2;
                    setScrollingCacheEnabled(true);
                } else if (abs2 > ((float) this.mTouchSlop)) {
                    this.mIsUnableToDrag = true;
                }
                if (this.mIsBeingDragged && performDrag(x2)) {
                    ViewCompat.postInvalidateOnAnimation(this);
                }
            }
        } else if (action == 6) {
            onSecondaryPointerUp(motionEvent);
        }
        if (this.mVelocityTracker == null) {
            this.mVelocityTracker = VelocityTracker.obtain();
        }
        this.mVelocityTracker.addMovement(motionEvent2);
        return this.mIsBeingDragged;
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        boolean z2;
        ItemInfo b2;
        int i5;
        int i6;
        int childCount = getChildCount();
        int i7 = i3 - i;
        int i8 = i4 - i2;
        int paddingLeft = getPaddingLeft();
        int paddingTop = getPaddingTop();
        int paddingRight = getPaddingRight();
        int paddingBottom = getPaddingBottom();
        int scrollX = getScrollX();
        int i9 = paddingBottom;
        int i10 = 0;
        int i11 = paddingTop;
        int i12 = paddingLeft;
        for (int i13 = 0; i13 < childCount; i13++) {
            View childAt = getChildAt(i13);
            if (childAt.getVisibility() != 8) {
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                if (layoutParams.isDecor) {
                    int i14 = layoutParams.gravity;
                    int i15 = i14 & 7;
                    int i16 = i14 & 112;
                    if (i15 == 1) {
                        i5 = Math.max((i7 - childAt.getMeasuredWidth()) / 2, i12);
                    } else if (i15 == 3) {
                        i5 = i12;
                        i12 = childAt.getMeasuredWidth() + i12;
                    } else if (i15 != 5) {
                        i5 = i12;
                    } else {
                        i5 = (i7 - paddingRight) - childAt.getMeasuredWidth();
                        paddingRight += childAt.getMeasuredWidth();
                    }
                    if (i16 == 16) {
                        i6 = Math.max((i8 - childAt.getMeasuredHeight()) / 2, i11);
                    } else if (i16 == 48) {
                        i6 = i11;
                        i11 = childAt.getMeasuredHeight() + i11;
                    } else if (i16 != 80) {
                        i6 = i11;
                    } else {
                        i6 = (i8 - i9) - childAt.getMeasuredHeight();
                        i9 += childAt.getMeasuredHeight();
                    }
                    int i17 = i5 + scrollX;
                    childAt.layout(i17, i6, childAt.getMeasuredWidth() + i17, i6 + childAt.getMeasuredHeight());
                    i10++;
                }
            }
        }
        int i18 = (i7 - i12) - paddingRight;
        for (int i19 = 0; i19 < childCount; i19++) {
            View childAt2 = getChildAt(i19);
            if (childAt2.getVisibility() != 8) {
                LayoutParams layoutParams2 = (LayoutParams) childAt2.getLayoutParams();
                if (!layoutParams2.isDecor && (b2 = b(childAt2)) != null) {
                    float f = (float) i18;
                    int i20 = ((int) (b2.e * f)) + i12;
                    if (layoutParams2.b) {
                        layoutParams2.b = false;
                        childAt2.measure(View.MeasureSpec.makeMeasureSpec((int) (f * layoutParams2.a), 1073741824), View.MeasureSpec.makeMeasureSpec((i8 - i11) - i9, 1073741824));
                    }
                    childAt2.layout(i20, i11, childAt2.getMeasuredWidth() + i20, childAt2.getMeasuredHeight() + i11);
                }
            }
        }
        this.mTopPageBounds = i11;
        this.mBottomPageBounds = i8 - i9;
        this.mDecorChildCount = i10;
        if (this.mFirstLayout) {
            z2 = false;
            scrollToItem(this.b, false, 0, false);
        } else {
            z2 = false;
        }
        this.mFirstLayout = z2;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0084  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x008b  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x0090  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x0095  */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x00a4  */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x00aa  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onMeasure(int r14, int r15) {
        /*
            r13 = this;
            r0 = 0
            int r14 = android.view.ViewGroup.getDefaultSize(r0, r14)
            int r15 = android.view.ViewGroup.getDefaultSize(r0, r15)
            r13.setMeasuredDimension(r14, r15)
            int r14 = r13.getMeasuredWidth()
            int r15 = r14 / 10
            int r1 = r13.mDefaultGutterSize
            int r15 = java.lang.Math.min(r15, r1)
            r13.mGutterSize = r15
            int r15 = r13.getPaddingLeft()
            int r14 = r14 - r15
            int r15 = r13.getPaddingRight()
            int r14 = r14 - r15
            int r15 = r13.getMeasuredHeight()
            int r1 = r13.getPaddingTop()
            int r15 = r15 - r1
            int r1 = r13.getPaddingBottom()
            int r15 = r15 - r1
            int r1 = r13.getChildCount()
            r2 = r15
            r15 = r14
            r14 = 0
        L_0x0039:
            r3 = 8
            r4 = 1
            r5 = 1073741824(0x40000000, float:2.0)
            if (r14 >= r1) goto L_0x00b4
            android.view.View r6 = r13.getChildAt(r14)
            int r7 = r6.getVisibility()
            if (r7 == r3) goto L_0x00b1
            android.view.ViewGroup$LayoutParams r3 = r6.getLayoutParams()
            androidx.viewpager.widget.ViewPager$LayoutParams r3 = (androidx.viewpager.widget.ViewPager.LayoutParams) r3
            if (r3 == 0) goto L_0x00b1
            boolean r7 = r3.isDecor
            if (r7 == 0) goto L_0x00b1
            int r7 = r3.gravity
            r8 = r7 & 7
            r7 = r7 & 112(0x70, float:1.57E-43)
            r9 = 48
            if (r7 == r9) goto L_0x0067
            r9 = 80
            if (r7 != r9) goto L_0x0065
            goto L_0x0067
        L_0x0065:
            r7 = 0
            goto L_0x0068
        L_0x0067:
            r7 = 1
        L_0x0068:
            r9 = 3
            if (r8 == r9) goto L_0x0070
            r9 = 5
            if (r8 != r9) goto L_0x006f
            goto L_0x0070
        L_0x006f:
            r4 = 0
        L_0x0070:
            r8 = -2147483648(0xffffffff80000000, float:-0.0)
            if (r7 == 0) goto L_0x0077
            r8 = 1073741824(0x40000000, float:2.0)
            goto L_0x007c
        L_0x0077:
            if (r4 == 0) goto L_0x007c
            r9 = 1073741824(0x40000000, float:2.0)
            goto L_0x007e
        L_0x007c:
            r9 = -2147483648(0xffffffff80000000, float:-0.0)
        L_0x007e:
            int r10 = r3.width
            r11 = -1
            r12 = -2
            if (r10 == r12) goto L_0x008b
            if (r10 == r11) goto L_0x0087
            goto L_0x0088
        L_0x0087:
            r10 = r15
        L_0x0088:
            r8 = 1073741824(0x40000000, float:2.0)
            goto L_0x008c
        L_0x008b:
            r10 = r15
        L_0x008c:
            int r3 = r3.height
            if (r3 == r12) goto L_0x0095
            if (r3 == r11) goto L_0x0093
            goto L_0x0097
        L_0x0093:
            r3 = r2
            goto L_0x0097
        L_0x0095:
            r3 = r2
            r5 = r9
        L_0x0097:
            int r8 = android.view.View.MeasureSpec.makeMeasureSpec(r10, r8)
            int r3 = android.view.View.MeasureSpec.makeMeasureSpec(r3, r5)
            r6.measure(r8, r3)
            if (r7 == 0) goto L_0x00aa
            int r3 = r6.getMeasuredHeight()
            int r2 = r2 - r3
            goto L_0x00b1
        L_0x00aa:
            if (r4 == 0) goto L_0x00b1
            int r3 = r6.getMeasuredWidth()
            int r15 = r15 - r3
        L_0x00b1:
            int r14 = r14 + 1
            goto L_0x0039
        L_0x00b4:
            int r14 = android.view.View.MeasureSpec.makeMeasureSpec(r15, r5)
            r13.mChildWidthMeasureSpec = r14
            int r14 = android.view.View.MeasureSpec.makeMeasureSpec(r2, r5)
            r13.mChildHeightMeasureSpec = r14
            r13.mInLayout = r4
            r13.e()
            r13.mInLayout = r0
            int r14 = r13.getChildCount()
        L_0x00cb:
            if (r0 >= r14) goto L_0x00f5
            android.view.View r1 = r13.getChildAt(r0)
            int r2 = r1.getVisibility()
            if (r2 == r3) goto L_0x00f2
            android.view.ViewGroup$LayoutParams r2 = r1.getLayoutParams()
            androidx.viewpager.widget.ViewPager$LayoutParams r2 = (androidx.viewpager.widget.ViewPager.LayoutParams) r2
            if (r2 == 0) goto L_0x00e3
            boolean r4 = r2.isDecor
            if (r4 != 0) goto L_0x00f2
        L_0x00e3:
            float r4 = (float) r15
            float r2 = r2.a
            float r4 = r4 * r2
            int r2 = (int) r4
            int r2 = android.view.View.MeasureSpec.makeMeasureSpec(r2, r5)
            int r4 = r13.mChildHeightMeasureSpec
            r1.measure(r2, r4)
        L_0x00f2:
            int r0 = r0 + 1
            goto L_0x00cb
        L_0x00f5:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.viewpager.widget.ViewPager.onMeasure(int, int):void");
    }

    /* access modifiers changed from: protected */
    public boolean onRequestFocusInDescendants(int i, Rect rect) {
        int i2;
        int i3;
        ItemInfo b2;
        int childCount = getChildCount();
        int i4 = -1;
        if ((i & 2) != 0) {
            i4 = childCount;
            i3 = 0;
            i2 = 1;
        } else {
            i3 = childCount - 1;
            i2 = -1;
        }
        while (i3 != i4) {
            View childAt = getChildAt(i3);
            if (childAt.getVisibility() == 0 && (b2 = b(childAt)) != null && b2.b == this.b && childAt.requestFocus(i, rect)) {
                return true;
            }
            i3 += i2;
        }
        return false;
    }

    public void onRestoreInstanceState(Parcelable parcelable) {
        if (!(parcelable instanceof SavedState)) {
            super.onRestoreInstanceState(parcelable);
            return;
        }
        SavedState savedState = (SavedState) parcelable;
        super.onRestoreInstanceState(savedState.getSuperState());
        PagerAdapter pagerAdapter = this.a;
        if (pagerAdapter != null) {
            pagerAdapter.restoreState(savedState.b, savedState.c);
            a(savedState.a, false, true);
            return;
        }
        this.mRestoredCurItem = savedState.a;
        this.mRestoredAdapterState = savedState.b;
        this.mRestoredClassLoader = savedState.c;
    }

    public Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        savedState.a = this.b;
        PagerAdapter pagerAdapter = this.a;
        if (pagerAdapter != null) {
            savedState.b = pagerAdapter.saveState();
        }
        return savedState;
    }

    /* access modifiers changed from: protected */
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        if (i != i3) {
            int i5 = this.mPageMargin;
            recomputeScrollPosition(i, i3, i5, i5);
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:56:0x0151  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onTouchEvent(android.view.MotionEvent r8) {
        /*
            r7 = this;
            boolean r0 = r7.mFakeDragging
            r1 = 1
            if (r0 == 0) goto L_0x0006
            return r1
        L_0x0006:
            int r0 = r8.getAction()
            r2 = 0
            if (r0 != 0) goto L_0x0014
            int r0 = r8.getEdgeFlags()
            if (r0 == 0) goto L_0x0014
            return r2
        L_0x0014:
            androidx.viewpager.widget.PagerAdapter r0 = r7.a
            if (r0 == 0) goto L_0x0155
            int r0 = r0.getCount()
            if (r0 != 0) goto L_0x0020
            goto L_0x0155
        L_0x0020:
            android.view.VelocityTracker r0 = r7.mVelocityTracker
            if (r0 != 0) goto L_0x002a
            android.view.VelocityTracker r0 = android.view.VelocityTracker.obtain()
            r7.mVelocityTracker = r0
        L_0x002a:
            android.view.VelocityTracker r0 = r7.mVelocityTracker
            r0.addMovement(r8)
            int r0 = r8.getAction()
            r0 = r0 & 255(0xff, float:3.57E-43)
            if (r0 == 0) goto L_0x012f
            if (r0 == r1) goto L_0x00e2
            r3 = 2
            if (r0 == r3) goto L_0x0073
            r3 = 3
            if (r0 == r3) goto L_0x0068
            r3 = 5
            if (r0 == r3) goto L_0x0058
            r3 = 6
            if (r0 == r3) goto L_0x0047
            goto L_0x014f
        L_0x0047:
            r7.onSecondaryPointerUp(r8)
            int r0 = r7.mActivePointerId
            int r0 = r8.findPointerIndex(r0)
            float r8 = r8.getX(r0)
            r7.mLastMotionX = r8
            goto L_0x014f
        L_0x0058:
            int r0 = r8.getActionIndex()
            float r3 = r8.getX(r0)
            r7.mLastMotionX = r3
            int r8 = r8.getPointerId(r0)
            goto L_0x014d
        L_0x0068:
            boolean r8 = r7.mIsBeingDragged
            if (r8 == 0) goto L_0x014f
            int r8 = r7.b
            r7.scrollToItem(r8, r1, r2, r2)
            goto L_0x012a
        L_0x0073:
            boolean r0 = r7.mIsBeingDragged
            if (r0 != 0) goto L_0x00ce
            int r0 = r7.mActivePointerId
            int r0 = r8.findPointerIndex(r0)
            r3 = -1
            if (r0 != r3) goto L_0x0082
            goto L_0x012a
        L_0x0082:
            float r3 = r8.getX(r0)
            float r4 = r7.mLastMotionX
            float r4 = r3 - r4
            float r4 = java.lang.Math.abs(r4)
            float r0 = r8.getY(r0)
            float r5 = r7.mLastMotionY
            float r5 = r0 - r5
            float r5 = java.lang.Math.abs(r5)
            int r6 = r7.mTouchSlop
            float r6 = (float) r6
            int r6 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1))
            if (r6 <= 0) goto L_0x00ce
            int r4 = (r4 > r5 ? 1 : (r4 == r5 ? 0 : -1))
            if (r4 <= 0) goto L_0x00ce
            r7.mIsBeingDragged = r1
            r7.requestParentDisallowInterceptTouchEvent(r1)
            float r4 = r7.mInitialMotionX
            float r3 = r3 - r4
            r5 = 0
            int r3 = (r3 > r5 ? 1 : (r3 == r5 ? 0 : -1))
            if (r3 <= 0) goto L_0x00b7
            int r3 = r7.mTouchSlop
            float r3 = (float) r3
            float r4 = r4 + r3
            goto L_0x00bb
        L_0x00b7:
            int r3 = r7.mTouchSlop
            float r3 = (float) r3
            float r4 = r4 - r3
        L_0x00bb:
            r7.mLastMotionX = r4
            r7.mLastMotionY = r0
            r7.setScrollState(r1)
            r7.setScrollingCacheEnabled(r1)
            android.view.ViewParent r0 = r7.getParent()
            if (r0 == 0) goto L_0x00ce
            r0.requestDisallowInterceptTouchEvent(r1)
        L_0x00ce:
            boolean r0 = r7.mIsBeingDragged
            if (r0 == 0) goto L_0x014f
            int r0 = r7.mActivePointerId
            int r0 = r8.findPointerIndex(r0)
            float r8 = r8.getX(r0)
            boolean r8 = r7.performDrag(r8)
            r2 = r2 | r8
            goto L_0x014f
        L_0x00e2:
            boolean r0 = r7.mIsBeingDragged
            if (r0 == 0) goto L_0x014f
            android.view.VelocityTracker r0 = r7.mVelocityTracker
            r2 = 1000(0x3e8, float:1.401E-42)
            int r3 = r7.mMaximumVelocity
            float r3 = (float) r3
            r0.computeCurrentVelocity(r2, r3)
            int r2 = r7.mActivePointerId
            float r0 = r0.getXVelocity(r2)
            int r0 = (int) r0
            r7.mPopulatePending = r1
            int r2 = r7.getClientWidth()
            int r3 = r7.getScrollX()
            androidx.viewpager.widget.ViewPager$ItemInfo r4 = r7.infoForCurrentScrollPosition()
            int r5 = r7.mPageMargin
            float r5 = (float) r5
            float r2 = (float) r2
            float r5 = r5 / r2
            int r6 = r4.b
            float r3 = (float) r3
            float r3 = r3 / r2
            float r2 = r4.e
            float r3 = r3 - r2
            float r2 = r4.d
            float r2 = r2 + r5
            float r3 = r3 / r2
            int r2 = r7.mActivePointerId
            int r2 = r8.findPointerIndex(r2)
            float r8 = r8.getX(r2)
            float r2 = r7.mInitialMotionX
            float r8 = r8 - r2
            int r8 = (int) r8
            int r8 = r7.determineTargetPage(r6, r3, r0, r8)
            r7.a(r8, r1, r1, r0)
        L_0x012a:
            boolean r2 = r7.resetTouch()
            goto L_0x014f
        L_0x012f:
            android.widget.Scroller r0 = r7.mScroller
            r0.abortAnimation()
            r7.mPopulatePending = r2
            r7.e()
            float r0 = r8.getX()
            r7.mInitialMotionX = r0
            r7.mLastMotionX = r0
            float r0 = r8.getY()
            r7.mInitialMotionY = r0
            r7.mLastMotionY = r0
            int r8 = r8.getPointerId(r2)
        L_0x014d:
            r7.mActivePointerId = r8
        L_0x014f:
            if (r2 == 0) goto L_0x0154
            androidx.core.view.ViewCompat.postInvalidateOnAnimation(r7)
        L_0x0154:
            return r1
        L_0x0155:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.viewpager.widget.ViewPager.onTouchEvent(android.view.MotionEvent):boolean");
    }

    public void removeOnAdapterChangeListener(@NonNull OnAdapterChangeListener onAdapterChangeListener) {
        List<OnAdapterChangeListener> list = this.mAdapterChangeListeners;
        if (list != null) {
            list.remove(onAdapterChangeListener);
        }
    }

    public void removeOnPageChangeListener(@NonNull OnPageChangeListener onPageChangeListener) {
        List<OnPageChangeListener> list = this.mOnPageChangeListeners;
        if (list != null) {
            list.remove(onPageChangeListener);
        }
    }

    public void removeView(View view) {
        if (this.mInLayout) {
            removeViewInLayout(view);
        } else {
            super.removeView(view);
        }
    }

    public void setAdapter(@Nullable PagerAdapter pagerAdapter) {
        PagerAdapter pagerAdapter2 = this.a;
        if (pagerAdapter2 != null) {
            pagerAdapter2.a((DataSetObserver) null);
            this.a.startUpdate((ViewGroup) this);
            for (int i = 0; i < this.mItems.size(); i++) {
                ItemInfo itemInfo = this.mItems.get(i);
                this.a.destroyItem((ViewGroup) this, itemInfo.b, itemInfo.a);
            }
            this.a.finishUpdate((ViewGroup) this);
            this.mItems.clear();
            removeNonDecorViews();
            this.b = 0;
            scrollTo(0, 0);
        }
        PagerAdapter pagerAdapter3 = this.a;
        this.a = pagerAdapter;
        this.mExpectedAdapterCount = 0;
        if (this.a != null) {
            if (this.mObserver == null) {
                this.mObserver = new PagerObserver();
            }
            this.a.a(this.mObserver);
            this.mPopulatePending = false;
            boolean z = this.mFirstLayout;
            this.mFirstLayout = true;
            this.mExpectedAdapterCount = this.a.getCount();
            if (this.mRestoredCurItem >= 0) {
                this.a.restoreState(this.mRestoredAdapterState, this.mRestoredClassLoader);
                a(this.mRestoredCurItem, false, true);
                this.mRestoredCurItem = -1;
                this.mRestoredAdapterState = null;
                this.mRestoredClassLoader = null;
            } else if (!z) {
                e();
            } else {
                requestLayout();
            }
        }
        List<OnAdapterChangeListener> list = this.mAdapterChangeListeners;
        if (list != null && !list.isEmpty()) {
            int size = this.mAdapterChangeListeners.size();
            for (int i2 = 0; i2 < size; i2++) {
                this.mAdapterChangeListeners.get(i2).onAdapterChanged(this, pagerAdapter3, pagerAdapter);
            }
        }
    }

    public void setCurrentItem(int i) {
        this.mPopulatePending = false;
        a(i, !this.mFirstLayout, false);
    }

    public void setCurrentItem(int i, boolean z) {
        this.mPopulatePending = false;
        a(i, z, false);
    }

    public void setOffscreenPageLimit(int i) {
        if (i < 1) {
            Log.w(TAG, "Requested offscreen page limit " + i + " too small; defaulting to " + 1);
            i = 1;
        }
        if (i != this.mOffscreenPageLimit) {
            this.mOffscreenPageLimit = i;
            e();
        }
    }

    @Deprecated
    public void setOnPageChangeListener(OnPageChangeListener onPageChangeListener) {
        this.mOnPageChangeListener = onPageChangeListener;
    }

    public void setPageMargin(int i) {
        int i2 = this.mPageMargin;
        this.mPageMargin = i;
        int width = getWidth();
        recomputeScrollPosition(width, width, i, i2);
        requestLayout();
    }

    public void setPageMarginDrawable(@DrawableRes int i) {
        setPageMarginDrawable(ContextCompat.getDrawable(getContext(), i));
    }

    public void setPageMarginDrawable(@Nullable Drawable drawable) {
        this.mMarginDrawable = drawable;
        if (drawable != null) {
            refreshDrawableState();
        }
        setWillNotDraw(drawable == null);
        invalidate();
    }

    public void setPageTransformer(boolean z, @Nullable PageTransformer pageTransformer) {
        setPageTransformer(z, pageTransformer, 2);
    }

    public void setPageTransformer(boolean z, @Nullable PageTransformer pageTransformer, int i) {
        int i2 = 1;
        boolean z2 = pageTransformer != null;
        boolean z3 = z2 != (this.mPageTransformer != null);
        this.mPageTransformer = pageTransformer;
        setChildrenDrawingOrderEnabled(z2);
        if (z2) {
            if (z) {
                i2 = 2;
            }
            this.mDrawingOrder = i2;
            this.mPageTransformerLayerType = i;
        } else {
            this.mDrawingOrder = 0;
        }
        if (z3) {
            e();
        }
    }

    /* access modifiers changed from: package-private */
    public void setScrollState(int i) {
        if (this.mScrollState != i) {
            this.mScrollState = i;
            if (this.mPageTransformer != null) {
                enableLayers(i != 0);
            }
            dispatchOnScrollStateChanged(i);
        }
    }

    /* access modifiers changed from: protected */
    public boolean verifyDrawable(Drawable drawable) {
        return super.verifyDrawable(drawable) || drawable == this.mMarginDrawable;
    }
}
