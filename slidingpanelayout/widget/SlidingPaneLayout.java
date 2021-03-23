package androidx.slidingpanelayout.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.core.content.ContextCompat;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.customview.view.AbsSavedState;
import androidx.customview.widget.ViewDragHelper;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;

public class SlidingPaneLayout extends ViewGroup {
    private static final int DEFAULT_FADE_COLOR = -858993460;
    private static final int DEFAULT_OVERHANG_SIZE = 32;
    private static final int MIN_FLING_VELOCITY = 400;
    private static final String TAG = "SlidingPaneLayout";
    View a;
    float b;
    int c;
    boolean d;
    final ViewDragHelper e;
    boolean f;
    final ArrayList<DisableLayerRunnable> g;
    private boolean mCanSlide;
    private int mCoveredFadeColor;
    private boolean mDisplayListReflectionLoaded;
    private boolean mFirstLayout;
    private Method mGetDisplayList;
    private float mInitialMotionX;
    private float mInitialMotionY;
    private final int mOverhangSize;
    private PanelSlideListener mPanelSlideListener;
    private int mParallaxBy;
    private float mParallaxOffset;
    private Field mRecreateDisplayList;
    private Drawable mShadowDrawableLeft;
    private Drawable mShadowDrawableRight;
    private int mSliderFadeColor;
    private final Rect mTmpRect;

    class AccessibilityDelegate extends AccessibilityDelegateCompat {
        private final Rect mTmpRect = new Rect();

        AccessibilityDelegate() {
        }

        private void copyNodeInfoNoChildren(AccessibilityNodeInfoCompat accessibilityNodeInfoCompat, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat2) {
            Rect rect = this.mTmpRect;
            accessibilityNodeInfoCompat2.getBoundsInParent(rect);
            accessibilityNodeInfoCompat.setBoundsInParent(rect);
            accessibilityNodeInfoCompat2.getBoundsInScreen(rect);
            accessibilityNodeInfoCompat.setBoundsInScreen(rect);
            accessibilityNodeInfoCompat.setVisibleToUser(accessibilityNodeInfoCompat2.isVisibleToUser());
            accessibilityNodeInfoCompat.setPackageName(accessibilityNodeInfoCompat2.getPackageName());
            accessibilityNodeInfoCompat.setClassName(accessibilityNodeInfoCompat2.getClassName());
            accessibilityNodeInfoCompat.setContentDescription(accessibilityNodeInfoCompat2.getContentDescription());
            accessibilityNodeInfoCompat.setEnabled(accessibilityNodeInfoCompat2.isEnabled());
            accessibilityNodeInfoCompat.setClickable(accessibilityNodeInfoCompat2.isClickable());
            accessibilityNodeInfoCompat.setFocusable(accessibilityNodeInfoCompat2.isFocusable());
            accessibilityNodeInfoCompat.setFocused(accessibilityNodeInfoCompat2.isFocused());
            accessibilityNodeInfoCompat.setAccessibilityFocused(accessibilityNodeInfoCompat2.isAccessibilityFocused());
            accessibilityNodeInfoCompat.setSelected(accessibilityNodeInfoCompat2.isSelected());
            accessibilityNodeInfoCompat.setLongClickable(accessibilityNodeInfoCompat2.isLongClickable());
            accessibilityNodeInfoCompat.addAction(accessibilityNodeInfoCompat2.getActions());
            accessibilityNodeInfoCompat.setMovementGranularities(accessibilityNodeInfoCompat2.getMovementGranularities());
        }

        public boolean filter(View view) {
            return SlidingPaneLayout.this.e(view);
        }

        public void onInitializeAccessibilityEvent(View view, AccessibilityEvent accessibilityEvent) {
            super.onInitializeAccessibilityEvent(view, accessibilityEvent);
            accessibilityEvent.setClassName(SlidingPaneLayout.class.getName());
        }

        public void onInitializeAccessibilityNodeInfo(View view, AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            AccessibilityNodeInfoCompat obtain = AccessibilityNodeInfoCompat.obtain(accessibilityNodeInfoCompat);
            super.onInitializeAccessibilityNodeInfo(view, obtain);
            copyNodeInfoNoChildren(accessibilityNodeInfoCompat, obtain);
            obtain.recycle();
            accessibilityNodeInfoCompat.setClassName(SlidingPaneLayout.class.getName());
            accessibilityNodeInfoCompat.setSource(view);
            ViewParent parentForAccessibility = ViewCompat.getParentForAccessibility(view);
            if (parentForAccessibility instanceof View) {
                accessibilityNodeInfoCompat.setParent((View) parentForAccessibility);
            }
            int childCount = SlidingPaneLayout.this.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View childAt = SlidingPaneLayout.this.getChildAt(i);
                if (!filter(childAt) && childAt.getVisibility() == 0) {
                    ViewCompat.setImportantForAccessibility(childAt, 1);
                    accessibilityNodeInfoCompat.addChild(childAt);
                }
            }
        }

        public boolean onRequestSendAccessibilityEvent(ViewGroup viewGroup, View view, AccessibilityEvent accessibilityEvent) {
            if (!filter(view)) {
                return super.onRequestSendAccessibilityEvent(viewGroup, view, accessibilityEvent);
            }
            return false;
        }
    }

    private class DisableLayerRunnable implements Runnable {
        final View a;

        DisableLayerRunnable(View view) {
            this.a = view;
        }

        public void run() {
            if (this.a.getParent() == SlidingPaneLayout.this) {
                this.a.setLayerType(0, (Paint) null);
                SlidingPaneLayout.this.d(this.a);
            }
            SlidingPaneLayout.this.g.remove(this);
        }
    }

    private class DragHelperCallback extends ViewDragHelper.Callback {
        DragHelperCallback() {
        }

        public int clampViewPositionHorizontal(View view, int i, int i2) {
            LayoutParams layoutParams = (LayoutParams) SlidingPaneLayout.this.a.getLayoutParams();
            if (SlidingPaneLayout.this.a()) {
                int width = SlidingPaneLayout.this.getWidth() - ((SlidingPaneLayout.this.getPaddingRight() + layoutParams.rightMargin) + SlidingPaneLayout.this.a.getWidth());
                return Math.max(Math.min(i, width), width - SlidingPaneLayout.this.c);
            }
            int paddingLeft = SlidingPaneLayout.this.getPaddingLeft() + layoutParams.leftMargin;
            return Math.min(Math.max(i, paddingLeft), SlidingPaneLayout.this.c + paddingLeft);
        }

        public int clampViewPositionVertical(View view, int i, int i2) {
            return view.getTop();
        }

        public int getViewHorizontalDragRange(View view) {
            return SlidingPaneLayout.this.c;
        }

        public void onEdgeDragStarted(int i, int i2) {
            SlidingPaneLayout slidingPaneLayout = SlidingPaneLayout.this;
            slidingPaneLayout.e.captureChildView(slidingPaneLayout.a, i2);
        }

        public void onViewCaptured(View view, int i) {
            SlidingPaneLayout.this.b();
        }

        public void onViewDragStateChanged(int i) {
            SlidingPaneLayout slidingPaneLayout;
            boolean z;
            if (SlidingPaneLayout.this.e.getViewDragState() == 0) {
                SlidingPaneLayout slidingPaneLayout2 = SlidingPaneLayout.this;
                if (slidingPaneLayout2.b == 0.0f) {
                    slidingPaneLayout2.f(slidingPaneLayout2.a);
                    SlidingPaneLayout slidingPaneLayout3 = SlidingPaneLayout.this;
                    slidingPaneLayout3.a(slidingPaneLayout3.a);
                    slidingPaneLayout = SlidingPaneLayout.this;
                    z = false;
                } else {
                    slidingPaneLayout2.b(slidingPaneLayout2.a);
                    slidingPaneLayout = SlidingPaneLayout.this;
                    z = true;
                }
                slidingPaneLayout.f = z;
            }
        }

        public void onViewPositionChanged(View view, int i, int i2, int i3, int i4) {
            SlidingPaneLayout.this.a(i);
            SlidingPaneLayout.this.invalidate();
        }

        public void onViewReleased(View view, float f, float f2) {
            int i;
            LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
            if (SlidingPaneLayout.this.a()) {
                int paddingRight = SlidingPaneLayout.this.getPaddingRight() + layoutParams.rightMargin;
                if (f < 0.0f || (f == 0.0f && SlidingPaneLayout.this.b > 0.5f)) {
                    paddingRight += SlidingPaneLayout.this.c;
                }
                i = (SlidingPaneLayout.this.getWidth() - paddingRight) - SlidingPaneLayout.this.a.getWidth();
            } else {
                i = layoutParams.leftMargin + SlidingPaneLayout.this.getPaddingLeft();
                if (f > 0.0f || (f == 0.0f && SlidingPaneLayout.this.b > 0.5f)) {
                    i += SlidingPaneLayout.this.c;
                }
            }
            SlidingPaneLayout.this.e.settleCapturedViewAt(i, view.getTop());
            SlidingPaneLayout.this.invalidate();
        }

        public boolean tryCaptureView(View view, int i) {
            if (SlidingPaneLayout.this.d) {
                return false;
            }
            return ((LayoutParams) view.getLayoutParams()).a;
        }
    }

    public static class LayoutParams extends ViewGroup.MarginLayoutParams {
        private static final int[] ATTRS = {16843137};
        boolean a;
        boolean b;
        Paint c;
        public float weight = 0.0f;

        public LayoutParams() {
            super(-1, -1);
        }

        public LayoutParams(int i, int i2) {
            super(i, i2);
        }

        public LayoutParams(@NonNull Context context, @Nullable AttributeSet attributeSet) {
            super(context, attributeSet);
            TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, ATTRS);
            this.weight = obtainStyledAttributes.getFloat(0, 0.0f);
            obtainStyledAttributes.recycle();
        }

        public LayoutParams(@NonNull ViewGroup.LayoutParams layoutParams) {
            super(layoutParams);
        }

        public LayoutParams(@NonNull ViewGroup.MarginLayoutParams marginLayoutParams) {
            super(marginLayoutParams);
        }

        public LayoutParams(@NonNull LayoutParams layoutParams) {
            super(layoutParams);
            this.weight = layoutParams.weight;
        }
    }

    public interface PanelSlideListener {
        void onPanelClosed(@NonNull View view);

        void onPanelOpened(@NonNull View view);

        void onPanelSlide(@NonNull View view, float f);
    }

    static class SavedState extends AbsSavedState {
        public static final Parcelable.Creator<SavedState> CREATOR = new Parcelable.ClassLoaderCreator<SavedState>() {
            public SavedState createFromParcel(Parcel parcel) {
                return new SavedState(parcel, (ClassLoader) null);
            }

            public SavedState createFromParcel(Parcel parcel, ClassLoader classLoader) {
                return new SavedState(parcel, (ClassLoader) null);
            }

            public SavedState[] newArray(int i) {
                return new SavedState[i];
            }
        };
        boolean a;

        SavedState(Parcel parcel, ClassLoader classLoader) {
            super(parcel, classLoader);
            this.a = parcel.readInt() != 0;
        }

        SavedState(Parcelable parcelable) {
            super(parcelable);
        }

        public void writeToParcel(Parcel parcel, int i) {
            super.writeToParcel(parcel, i);
            parcel.writeInt(this.a ? 1 : 0);
        }
    }

    public static class SimplePanelSlideListener implements PanelSlideListener {
        public void onPanelClosed(View view) {
        }

        public void onPanelOpened(View view) {
        }

        public void onPanelSlide(View view, float f) {
        }
    }

    public SlidingPaneLayout(@NonNull Context context) {
        this(context, (AttributeSet) null);
    }

    public SlidingPaneLayout(@NonNull Context context, @Nullable AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public SlidingPaneLayout(@NonNull Context context, @Nullable AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        this.mSliderFadeColor = DEFAULT_FADE_COLOR;
        this.mFirstLayout = true;
        this.mTmpRect = new Rect();
        this.g = new ArrayList<>();
        float f2 = context.getResources().getDisplayMetrics().density;
        this.mOverhangSize = (int) ((32.0f * f2) + 0.5f);
        setWillNotDraw(false);
        ViewCompat.setAccessibilityDelegate(this, new AccessibilityDelegate());
        ViewCompat.setImportantForAccessibility(this, 1);
        this.e = ViewDragHelper.create(this, 0.5f, new DragHelperCallback());
        this.e.setMinVelocity(f2 * 400.0f);
    }

    private boolean closePane(View view, int i) {
        if (!this.mFirstLayout && !a(0.0f, i)) {
            return false;
        }
        this.f = false;
        return true;
    }

    private void dimChildView(View view, float f2, int i) {
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        if (f2 > 0.0f && i != 0) {
            int i2 = (((int) (((float) ((-16777216 & i) >>> 24)) * f2)) << 24) | (i & 16777215);
            if (layoutParams.c == null) {
                layoutParams.c = new Paint();
            }
            layoutParams.c.setColorFilter(new PorterDuffColorFilter(i2, PorterDuff.Mode.SRC_OVER));
            if (view.getLayerType() != 2) {
                view.setLayerType(2, layoutParams.c);
            }
            d(view);
        } else if (view.getLayerType() != 0) {
            Paint paint = layoutParams.c;
            if (paint != null) {
                paint.setColorFilter((ColorFilter) null);
            }
            DisableLayerRunnable disableLayerRunnable = new DisableLayerRunnable(view);
            this.g.add(disableLayerRunnable);
            ViewCompat.postOnAnimation(this, disableLayerRunnable);
        }
    }

    private boolean openPane(View view, int i) {
        if (!this.mFirstLayout && !a(1.0f, i)) {
            return false;
        }
        this.f = true;
        return true;
    }

    /* JADX WARNING: Removed duplicated region for block: B:10:0x0023  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void parallaxOtherViews(float r10) {
        /*
            r9 = this;
            boolean r0 = r9.a()
            android.view.View r1 = r9.a
            android.view.ViewGroup$LayoutParams r1 = r1.getLayoutParams()
            androidx.slidingpanelayout.widget.SlidingPaneLayout$LayoutParams r1 = (androidx.slidingpanelayout.widget.SlidingPaneLayout.LayoutParams) r1
            boolean r2 = r1.b
            r3 = 0
            if (r2 == 0) goto L_0x001c
            if (r0 == 0) goto L_0x0016
            int r1 = r1.rightMargin
            goto L_0x0018
        L_0x0016:
            int r1 = r1.leftMargin
        L_0x0018:
            if (r1 > 0) goto L_0x001c
            r1 = 1
            goto L_0x001d
        L_0x001c:
            r1 = 0
        L_0x001d:
            int r2 = r9.getChildCount()
        L_0x0021:
            if (r3 >= r2) goto L_0x0059
            android.view.View r4 = r9.getChildAt(r3)
            android.view.View r5 = r9.a
            if (r4 != r5) goto L_0x002c
            goto L_0x0056
        L_0x002c:
            float r5 = r9.mParallaxOffset
            r6 = 1065353216(0x3f800000, float:1.0)
            float r5 = r6 - r5
            int r7 = r9.mParallaxBy
            float r8 = (float) r7
            float r5 = r5 * r8
            int r5 = (int) r5
            r9.mParallaxOffset = r10
            float r8 = r6 - r10
            float r7 = (float) r7
            float r8 = r8 * r7
            int r7 = (int) r8
            int r5 = r5 - r7
            if (r0 == 0) goto L_0x0044
            int r5 = -r5
        L_0x0044:
            r4.offsetLeftAndRight(r5)
            if (r1 == 0) goto L_0x0056
            float r5 = r9.mParallaxOffset
            if (r0 == 0) goto L_0x004f
            float r5 = r5 - r6
            goto L_0x0051
        L_0x004f:
            float r5 = r6 - r5
        L_0x0051:
            int r6 = r9.mCoveredFadeColor
            r9.dimChildView(r4, r5, r6)
        L_0x0056:
            int r3 = r3 + 1
            goto L_0x0021
        L_0x0059:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.slidingpanelayout.widget.SlidingPaneLayout.parallaxOtherViews(float):void");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:6:0x0010, code lost:
        r4 = r4.getBackground();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static boolean viewIsOpaque(android.view.View r4) {
        /*
            boolean r0 = r4.isOpaque()
            r1 = 1
            if (r0 == 0) goto L_0x0008
            return r1
        L_0x0008:
            int r0 = android.os.Build.VERSION.SDK_INT
            r2 = 18
            r3 = 0
            if (r0 < r2) goto L_0x0010
            return r3
        L_0x0010:
            android.graphics.drawable.Drawable r4 = r4.getBackground()
            if (r4 == 0) goto L_0x0020
            int r4 = r4.getOpacity()
            r0 = -1
            if (r4 != r0) goto L_0x001e
            goto L_0x001f
        L_0x001e:
            r1 = 0
        L_0x001f:
            return r1
        L_0x0020:
            return r3
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.slidingpanelayout.widget.SlidingPaneLayout.viewIsOpaque(android.view.View):boolean");
    }

    /* access modifiers changed from: package-private */
    public void a(int i) {
        if (this.a == null) {
            this.b = 0.0f;
            return;
        }
        boolean a2 = a();
        LayoutParams layoutParams = (LayoutParams) this.a.getLayoutParams();
        int width = this.a.getWidth();
        if (a2) {
            i = (getWidth() - i) - width;
        }
        this.b = ((float) (i - ((a2 ? getPaddingRight() : getPaddingLeft()) + (a2 ? layoutParams.rightMargin : layoutParams.leftMargin)))) / ((float) this.c);
        if (this.mParallaxBy != 0) {
            parallaxOtherViews(this.b);
        }
        if (layoutParams.b) {
            dimChildView(this.a, this.b, this.mSliderFadeColor);
        }
        c(this.a);
    }

    /* access modifiers changed from: package-private */
    public void a(View view) {
        PanelSlideListener panelSlideListener = this.mPanelSlideListener;
        if (panelSlideListener != null) {
            panelSlideListener.onPanelClosed(view);
        }
        sendAccessibilityEvent(32);
    }

    /* access modifiers changed from: package-private */
    public boolean a() {
        return ViewCompat.getLayoutDirection(this) == 1;
    }

    /* access modifiers changed from: package-private */
    public boolean a(float f2, int i) {
        int i2;
        if (!this.mCanSlide) {
            return false;
        }
        boolean a2 = a();
        LayoutParams layoutParams = (LayoutParams) this.a.getLayoutParams();
        if (a2) {
            i2 = (int) (((float) getWidth()) - ((((float) (getPaddingRight() + layoutParams.rightMargin)) + (f2 * ((float) this.c))) + ((float) this.a.getWidth())));
        } else {
            i2 = (int) (((float) (getPaddingLeft() + layoutParams.leftMargin)) + (f2 * ((float) this.c)));
        }
        ViewDragHelper viewDragHelper = this.e;
        View view = this.a;
        if (!viewDragHelper.smoothSlideViewTo(view, i2, view.getTop())) {
            return false;
        }
        b();
        ViewCompat.postInvalidateOnAnimation(this);
        return true;
    }

    /* access modifiers changed from: package-private */
    public void b() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            View childAt = getChildAt(i);
            if (childAt.getVisibility() == 4) {
                childAt.setVisibility(0);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void b(View view) {
        PanelSlideListener panelSlideListener = this.mPanelSlideListener;
        if (panelSlideListener != null) {
            panelSlideListener.onPanelOpened(view);
        }
        sendAccessibilityEvent(32);
    }

    /* access modifiers changed from: package-private */
    public void c(View view) {
        PanelSlideListener panelSlideListener = this.mPanelSlideListener;
        if (panelSlideListener != null) {
            panelSlideListener.onPanelSlide(view, this.b);
        }
    }

    @Deprecated
    public boolean canSlide() {
        return this.mCanSlide;
    }

    /* access modifiers changed from: protected */
    public boolean checkLayoutParams(ViewGroup.LayoutParams layoutParams) {
        return (layoutParams instanceof LayoutParams) && super.checkLayoutParams(layoutParams);
    }

    public boolean closePane() {
        return closePane(this.a, 0);
    }

    public void computeScroll() {
        if (!this.e.continueSettling(true)) {
            return;
        }
        if (!this.mCanSlide) {
            this.e.abort();
        } else {
            ViewCompat.postInvalidateOnAnimation(this);
        }
    }

    /* access modifiers changed from: package-private */
    public void d(View view) {
        Field field;
        int i = Build.VERSION.SDK_INT;
        if (i >= 17) {
            ViewCompat.setLayerPaint(view, ((LayoutParams) view.getLayoutParams()).c);
            return;
        }
        if (i >= 16) {
            if (!this.mDisplayListReflectionLoaded) {
                try {
                    this.mGetDisplayList = View.class.getDeclaredMethod("getDisplayList", (Class[]) null);
                } catch (NoSuchMethodException e2) {
                    Log.e(TAG, "Couldn't fetch getDisplayList method; dimming won't work right.", e2);
                }
                try {
                    this.mRecreateDisplayList = View.class.getDeclaredField("mRecreateDisplayList");
                    this.mRecreateDisplayList.setAccessible(true);
                } catch (NoSuchFieldException e3) {
                    Log.e(TAG, "Couldn't fetch mRecreateDisplayList field; dimming will be slow.", e3);
                }
                this.mDisplayListReflectionLoaded = true;
            }
            if (this.mGetDisplayList == null || (field = this.mRecreateDisplayList) == null) {
                view.invalidate();
                return;
            }
            try {
                field.setBoolean(view, true);
                this.mGetDisplayList.invoke(view, (Object[]) null);
            } catch (Exception e4) {
                Log.e(TAG, "Error refreshing display list state", e4);
            }
        }
        ViewCompat.postInvalidateOnAnimation(this, view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
    }

    public void draw(Canvas canvas) {
        int i;
        int i2;
        super.draw(canvas);
        Drawable drawable = a() ? this.mShadowDrawableRight : this.mShadowDrawableLeft;
        View childAt = getChildCount() > 1 ? getChildAt(1) : null;
        if (childAt != null && drawable != null) {
            int top = childAt.getTop();
            int bottom = childAt.getBottom();
            int intrinsicWidth = drawable.getIntrinsicWidth();
            if (a()) {
                i2 = childAt.getRight();
                i = intrinsicWidth + i2;
            } else {
                int left = childAt.getLeft();
                int i3 = left - intrinsicWidth;
                i = left;
                i2 = i3;
            }
            drawable.setBounds(i2, top, i, bottom);
            drawable.draw(canvas);
        }
    }

    /* access modifiers changed from: protected */
    public boolean drawChild(Canvas canvas, View view, long j) {
        LayoutParams layoutParams = (LayoutParams) view.getLayoutParams();
        int save = canvas.save();
        if (this.mCanSlide && !layoutParams.a && this.a != null) {
            canvas.getClipBounds(this.mTmpRect);
            if (a()) {
                Rect rect = this.mTmpRect;
                rect.left = Math.max(rect.left, this.a.getRight());
            } else {
                Rect rect2 = this.mTmpRect;
                rect2.right = Math.min(rect2.right, this.a.getLeft());
            }
            canvas.clipRect(this.mTmpRect);
        }
        boolean drawChild = super.drawChild(canvas, view, j);
        canvas.restoreToCount(save);
        return drawChild;
    }

    /* access modifiers changed from: package-private */
    public boolean e(View view) {
        if (view == null) {
            return false;
        }
        return this.mCanSlide && ((LayoutParams) view.getLayoutParams()).b && this.b > 0.0f;
    }

    /* access modifiers changed from: package-private */
    public void f(View view) {
        int i;
        int i2;
        int i3;
        int i4;
        View childAt;
        boolean z;
        View view2 = view;
        boolean a2 = a();
        int width = a2 ? getWidth() - getPaddingRight() : getPaddingLeft();
        int paddingLeft = a2 ? getPaddingLeft() : getWidth() - getPaddingRight();
        int paddingTop = getPaddingTop();
        int height = getHeight() - getPaddingBottom();
        if (view2 == null || !viewIsOpaque(view)) {
            i4 = 0;
            i3 = 0;
            i2 = 0;
            i = 0;
        } else {
            i4 = view.getLeft();
            i3 = view.getRight();
            i2 = view.getTop();
            i = view.getBottom();
        }
        int childCount = getChildCount();
        int i5 = 0;
        while (true) {
            if (i5 < childCount && (childAt = getChildAt(i5)) != view2) {
                if (childAt.getVisibility() == 8) {
                    z = a2;
                } else {
                    z = a2;
                    childAt.setVisibility((Math.max(a2 ? paddingLeft : width, childAt.getLeft()) < i4 || Math.max(paddingTop, childAt.getTop()) < i2 || Math.min(a2 ? width : paddingLeft, childAt.getRight()) > i3 || Math.min(height, childAt.getBottom()) > i) ? 0 : 4);
                }
                i5++;
                view2 = view;
                a2 = z;
            } else {
                return;
            }
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
        return layoutParams instanceof ViewGroup.MarginLayoutParams ? new LayoutParams((ViewGroup.MarginLayoutParams) layoutParams) : new LayoutParams(layoutParams);
    }

    @ColorInt
    public int getCoveredFadeColor() {
        return this.mCoveredFadeColor;
    }

    @Px
    public int getParallaxDistance() {
        return this.mParallaxBy;
    }

    @ColorInt
    public int getSliderFadeColor() {
        return this.mSliderFadeColor;
    }

    public boolean isOpen() {
        return !this.mCanSlide || this.b == 1.0f;
    }

    public boolean isSlideable() {
        return this.mCanSlide;
    }

    /* access modifiers changed from: protected */
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mFirstLayout = true;
    }

    /* access modifiers changed from: protected */
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mFirstLayout = true;
        int size = this.g.size();
        for (int i = 0; i < size; i++) {
            this.g.get(i).run();
        }
        this.g.clear();
    }

    public boolean onInterceptTouchEvent(MotionEvent motionEvent) {
        boolean z;
        View childAt;
        int actionMasked = motionEvent.getActionMasked();
        if (!this.mCanSlide && actionMasked == 0 && getChildCount() > 1 && (childAt = getChildAt(1)) != null) {
            this.f = !this.e.isViewUnder(childAt, (int) motionEvent.getX(), (int) motionEvent.getY());
        }
        if (!this.mCanSlide || (this.d && actionMasked != 0)) {
            this.e.cancel();
            return super.onInterceptTouchEvent(motionEvent);
        } else if (actionMasked == 3 || actionMasked == 1) {
            this.e.cancel();
            return false;
        } else {
            if (actionMasked == 0) {
                this.d = false;
                float x = motionEvent.getX();
                float y = motionEvent.getY();
                this.mInitialMotionX = x;
                this.mInitialMotionY = y;
                if (this.e.isViewUnder(this.a, (int) x, (int) y) && e(this.a)) {
                    z = true;
                    return this.e.shouldInterceptTouchEvent(motionEvent) || z;
                }
            } else if (actionMasked == 2) {
                float x2 = motionEvent.getX();
                float y2 = motionEvent.getY();
                float abs = Math.abs(x2 - this.mInitialMotionX);
                float abs2 = Math.abs(y2 - this.mInitialMotionY);
                if (abs > ((float) this.e.getTouchSlop()) && abs2 > abs) {
                    this.e.cancel();
                    this.d = true;
                    return false;
                }
            }
            z = false;
            if (this.e.shouldInterceptTouchEvent(motionEvent)) {
                return true;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onLayout(boolean z, int i, int i2, int i3, int i4) {
        int i5;
        int i6;
        int i7;
        int i8;
        int i9;
        boolean a2 = a();
        ViewDragHelper viewDragHelper = this.e;
        if (a2) {
            viewDragHelper.setEdgeTrackingEnabled(2);
        } else {
            viewDragHelper.setEdgeTrackingEnabled(1);
        }
        int i10 = i3 - i;
        int paddingRight = a2 ? getPaddingRight() : getPaddingLeft();
        int paddingLeft = a2 ? getPaddingLeft() : getPaddingRight();
        int paddingTop = getPaddingTop();
        int childCount = getChildCount();
        if (this.mFirstLayout) {
            this.b = (!this.mCanSlide || !this.f) ? 0.0f : 1.0f;
        }
        int i11 = paddingRight;
        int i12 = i11;
        for (int i13 = 0; i13 < childCount; i13++) {
            View childAt = getChildAt(i13);
            if (childAt.getVisibility() != 8) {
                LayoutParams layoutParams = (LayoutParams) childAt.getLayoutParams();
                int measuredWidth = childAt.getMeasuredWidth();
                if (layoutParams.a) {
                    int i14 = i10 - paddingLeft;
                    int min = (Math.min(i11, i14 - this.mOverhangSize) - i12) - (layoutParams.leftMargin + layoutParams.rightMargin);
                    this.c = min;
                    int i15 = a2 ? layoutParams.rightMargin : layoutParams.leftMargin;
                    layoutParams.b = ((i12 + i15) + min) + (measuredWidth / 2) > i14;
                    int i16 = (int) (((float) min) * this.b);
                    i6 = i15 + i16 + i12;
                    this.b = ((float) i16) / ((float) this.c);
                    i5 = 0;
                } else if (!this.mCanSlide || (i9 = this.mParallaxBy) == 0) {
                    i6 = i11;
                    i5 = 0;
                } else {
                    i5 = (int) ((1.0f - this.b) * ((float) i9));
                    i6 = i11;
                }
                if (a2) {
                    i7 = (i10 - i6) + i5;
                    i8 = i7 - measuredWidth;
                } else {
                    i8 = i6 - i5;
                    i7 = i8 + measuredWidth;
                }
                childAt.layout(i8, paddingTop, i7, childAt.getMeasuredHeight() + paddingTop);
                i11 += childAt.getWidth();
                i12 = i6;
            }
        }
        if (this.mFirstLayout) {
            if (this.mCanSlide) {
                if (this.mParallaxBy != 0) {
                    parallaxOtherViews(this.b);
                }
                if (((LayoutParams) this.a.getLayoutParams()).b) {
                    dimChildView(this.a, this.b, this.mSliderFadeColor);
                }
            } else {
                for (int i17 = 0; i17 < childCount; i17++) {
                    dimChildView(getChildAt(i17), 0.0f, this.mSliderFadeColor);
                }
            }
            f(this.a);
        }
        this.mFirstLayout = false;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:117:0x01e4  */
    /* JADX WARNING: Removed duplicated region for block: B:120:0x01fa  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x010c  */
    /* JADX WARNING: Removed duplicated region for block: B:58:0x010e  */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0116  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onMeasure(int r22, int r23) {
        /*
            r21 = this;
            r0 = r21
            int r1 = android.view.View.MeasureSpec.getMode(r22)
            int r2 = android.view.View.MeasureSpec.getSize(r22)
            int r3 = android.view.View.MeasureSpec.getMode(r23)
            int r4 = android.view.View.MeasureSpec.getSize(r23)
            r5 = 300(0x12c, float:4.2E-43)
            r6 = -2147483648(0xffffffff80000000, float:-0.0)
            r7 = 1073741824(0x40000000, float:2.0)
            if (r1 == r7) goto L_0x0030
            boolean r8 = r21.isInEditMode()
            if (r8 == 0) goto L_0x0028
            if (r1 != r6) goto L_0x0023
            goto L_0x0047
        L_0x0023:
            if (r1 != 0) goto L_0x0047
            r2 = 300(0x12c, float:4.2E-43)
            goto L_0x0047
        L_0x0028:
            java.lang.IllegalStateException r1 = new java.lang.IllegalStateException
            java.lang.String r2 = "Width must have an exact value or MATCH_PARENT"
            r1.<init>(r2)
            throw r1
        L_0x0030:
            if (r3 != 0) goto L_0x0047
            boolean r1 = r21.isInEditMode()
            if (r1 == 0) goto L_0x003f
            if (r3 != 0) goto L_0x0047
            r3 = -2147483648(0xffffffff80000000, float:-0.0)
            r4 = 300(0x12c, float:4.2E-43)
            goto L_0x0047
        L_0x003f:
            java.lang.IllegalStateException r1 = new java.lang.IllegalStateException
            java.lang.String r2 = "Height must not be UNSPECIFIED"
            r1.<init>(r2)
            throw r1
        L_0x0047:
            r1 = 0
            if (r3 == r6) goto L_0x005b
            if (r3 == r7) goto L_0x004f
            r4 = 0
            r5 = 0
            goto L_0x0067
        L_0x004f:
            int r5 = r21.getPaddingTop()
            int r4 = r4 - r5
            int r5 = r21.getPaddingBottom()
            int r4 = r4 - r5
            r5 = r4
            goto L_0x0067
        L_0x005b:
            int r5 = r21.getPaddingTop()
            int r4 = r4 - r5
            int r5 = r21.getPaddingBottom()
            int r4 = r4 - r5
            r5 = r4
            r4 = 0
        L_0x0067:
            int r8 = r21.getPaddingLeft()
            int r8 = r2 - r8
            int r9 = r21.getPaddingRight()
            int r8 = r8 - r9
            int r9 = r21.getChildCount()
            r10 = 2
            if (r9 <= r10) goto L_0x0080
            java.lang.String r10 = "SlidingPaneLayout"
            java.lang.String r11 = "onMeasure: More than two child views are not supported."
            android.util.Log.e(r10, r11)
        L_0x0080:
            r10 = 0
            r0.a = r10
            r13 = r4
            r14 = r8
            r4 = 0
            r11 = 0
            r12 = 0
        L_0x0088:
            r15 = 8
            r16 = 1
            if (r4 >= r9) goto L_0x0122
            android.view.View r6 = r0.getChildAt(r4)
            android.view.ViewGroup$LayoutParams r18 = r6.getLayoutParams()
            r7 = r18
            androidx.slidingpanelayout.widget.SlidingPaneLayout$LayoutParams r7 = (androidx.slidingpanelayout.widget.SlidingPaneLayout.LayoutParams) r7
            int r10 = r6.getVisibility()
            if (r10 != r15) goto L_0x00a4
            r7.b = r1
            goto L_0x0119
        L_0x00a4:
            float r10 = r7.weight
            r15 = 0
            int r19 = (r10 > r15 ? 1 : (r10 == r15 ? 0 : -1))
            if (r19 <= 0) goto L_0x00b1
            float r12 = r12 + r10
            int r10 = r7.width
            if (r10 != 0) goto L_0x00b1
            goto L_0x0119
        L_0x00b1:
            int r10 = r7.leftMargin
            int r15 = r7.rightMargin
            int r10 = r10 + r15
            int r15 = r7.width
            r1 = -2
            if (r15 != r1) goto L_0x00c6
            int r1 = r8 - r10
            r10 = -2147483648(0xffffffff80000000, float:-0.0)
            int r1 = android.view.View.MeasureSpec.makeMeasureSpec(r1, r10)
            r10 = 1073741824(0x40000000, float:2.0)
            goto L_0x00d8
        L_0x00c6:
            int r1 = r7.width
            r15 = -1
            if (r1 != r15) goto L_0x00d0
            int r1 = r8 - r10
            r10 = 1073741824(0x40000000, float:2.0)
            goto L_0x00d4
        L_0x00d0:
            r10 = 1073741824(0x40000000, float:2.0)
            int r1 = r7.width
        L_0x00d4:
            int r1 = android.view.View.MeasureSpec.makeMeasureSpec(r1, r10)
        L_0x00d8:
            int r15 = r7.height
            r10 = -2
            if (r15 != r10) goto L_0x00e4
            r10 = -2147483648(0xffffffff80000000, float:-0.0)
        L_0x00df:
            int r15 = android.view.View.MeasureSpec.makeMeasureSpec(r5, r10)
            goto L_0x00f4
        L_0x00e4:
            int r10 = r7.height
            r15 = -1
            if (r10 != r15) goto L_0x00ec
            r10 = 1073741824(0x40000000, float:2.0)
            goto L_0x00df
        L_0x00ec:
            r10 = 1073741824(0x40000000, float:2.0)
            int r15 = r7.height
            int r15 = android.view.View.MeasureSpec.makeMeasureSpec(r15, r10)
        L_0x00f4:
            r6.measure(r1, r15)
            int r1 = r6.getMeasuredWidth()
            int r10 = r6.getMeasuredHeight()
            r15 = -2147483648(0xffffffff80000000, float:-0.0)
            if (r3 != r15) goto L_0x0109
            if (r10 <= r13) goto L_0x0109
            int r13 = java.lang.Math.min(r10, r5)
        L_0x0109:
            int r14 = r14 - r1
            if (r14 >= 0) goto L_0x010e
            r1 = 1
            goto L_0x010f
        L_0x010e:
            r1 = 0
        L_0x010f:
            r7.a = r1
            r1 = r1 | r11
            boolean r7 = r7.a
            if (r7 == 0) goto L_0x0118
            r0.a = r6
        L_0x0118:
            r11 = r1
        L_0x0119:
            int r4 = r4 + 1
            r1 = 0
            r6 = -2147483648(0xffffffff80000000, float:-0.0)
            r7 = 1073741824(0x40000000, float:2.0)
            goto L_0x0088
        L_0x0122:
            if (r11 != 0) goto L_0x0129
            r1 = 0
            int r3 = (r12 > r1 ? 1 : (r12 == r1 ? 0 : -1))
            if (r3 <= 0) goto L_0x021a
        L_0x0129:
            int r1 = r0.mOverhangSize
            int r1 = r8 - r1
            r3 = 0
        L_0x012e:
            if (r3 >= r9) goto L_0x021a
            android.view.View r4 = r0.getChildAt(r3)
            int r6 = r4.getVisibility()
            if (r6 != r15) goto L_0x0141
        L_0x013a:
            r20 = r1
        L_0x013c:
            r1 = 0
            r6 = 1073741824(0x40000000, float:2.0)
            goto L_0x0212
        L_0x0141:
            android.view.ViewGroup$LayoutParams r6 = r4.getLayoutParams()
            androidx.slidingpanelayout.widget.SlidingPaneLayout$LayoutParams r6 = (androidx.slidingpanelayout.widget.SlidingPaneLayout.LayoutParams) r6
            int r7 = r4.getVisibility()
            if (r7 != r15) goto L_0x014e
            goto L_0x013a
        L_0x014e:
            int r7 = r6.width
            if (r7 != 0) goto L_0x015b
            float r7 = r6.weight
            r10 = 0
            int r7 = (r7 > r10 ? 1 : (r7 == r10 ? 0 : -1))
            if (r7 <= 0) goto L_0x015b
            r7 = 1
            goto L_0x015c
        L_0x015b:
            r7 = 0
        L_0x015c:
            if (r7 == 0) goto L_0x0160
            r10 = 0
            goto L_0x0164
        L_0x0160:
            int r10 = r4.getMeasuredWidth()
        L_0x0164:
            if (r11 == 0) goto L_0x01aa
            android.view.View r15 = r0.a
            if (r4 == r15) goto L_0x01aa
            int r15 = r6.width
            if (r15 >= 0) goto L_0x013a
            if (r10 > r1) goto L_0x0177
            float r10 = r6.weight
            r15 = 0
            int r10 = (r10 > r15 ? 1 : (r10 == r15 ? 0 : -1))
            if (r10 <= 0) goto L_0x013a
        L_0x0177:
            if (r7 == 0) goto L_0x0198
            int r7 = r6.height
            r10 = -2
            if (r7 != r10) goto L_0x0187
            r7 = -2147483648(0xffffffff80000000, float:-0.0)
            int r6 = android.view.View.MeasureSpec.makeMeasureSpec(r5, r7)
            r7 = 1073741824(0x40000000, float:2.0)
            goto L_0x01a2
        L_0x0187:
            int r7 = r6.height
            r10 = -1
            if (r7 != r10) goto L_0x0193
            r7 = 1073741824(0x40000000, float:2.0)
            int r6 = android.view.View.MeasureSpec.makeMeasureSpec(r5, r7)
            goto L_0x01a2
        L_0x0193:
            r7 = 1073741824(0x40000000, float:2.0)
            int r6 = r6.height
            goto L_0x019e
        L_0x0198:
            r7 = 1073741824(0x40000000, float:2.0)
            int r6 = r4.getMeasuredHeight()
        L_0x019e:
            int r6 = android.view.View.MeasureSpec.makeMeasureSpec(r6, r7)
        L_0x01a2:
            int r10 = android.view.View.MeasureSpec.makeMeasureSpec(r1, r7)
            r4.measure(r10, r6)
            goto L_0x013a
        L_0x01aa:
            float r7 = r6.weight
            r15 = 0
            int r7 = (r7 > r15 ? 1 : (r7 == r15 ? 0 : -1))
            if (r7 <= 0) goto L_0x013a
            int r7 = r6.width
            if (r7 != 0) goto L_0x01d8
            int r7 = r6.height
            r15 = -2
            if (r7 != r15) goto L_0x01c5
            r7 = -2147483648(0xffffffff80000000, float:-0.0)
            int r17 = android.view.View.MeasureSpec.makeMeasureSpec(r5, r7)
            r15 = r17
            r7 = 1073741824(0x40000000, float:2.0)
            goto L_0x01e2
        L_0x01c5:
            int r7 = r6.height
            r15 = -1
            if (r7 != r15) goto L_0x01d3
            r7 = 1073741824(0x40000000, float:2.0)
            int r20 = android.view.View.MeasureSpec.makeMeasureSpec(r5, r7)
            r15 = r20
            goto L_0x01e2
        L_0x01d3:
            r7 = 1073741824(0x40000000, float:2.0)
            int r15 = r6.height
            goto L_0x01de
        L_0x01d8:
            r7 = 1073741824(0x40000000, float:2.0)
            int r15 = r4.getMeasuredHeight()
        L_0x01de:
            int r15 = android.view.View.MeasureSpec.makeMeasureSpec(r15, r7)
        L_0x01e2:
            if (r11 == 0) goto L_0x01fa
            int r7 = r6.leftMargin
            int r6 = r6.rightMargin
            int r7 = r7 + r6
            int r6 = r8 - r7
            r20 = r1
            r7 = 1073741824(0x40000000, float:2.0)
            int r1 = android.view.View.MeasureSpec.makeMeasureSpec(r6, r7)
            if (r10 == r6) goto L_0x013c
            r4.measure(r1, r15)
            goto L_0x013c
        L_0x01fa:
            r20 = r1
            r1 = 0
            int r7 = java.lang.Math.max(r1, r14)
            float r6 = r6.weight
            float r7 = (float) r7
            float r6 = r6 * r7
            float r6 = r6 / r12
            int r6 = (int) r6
            int r10 = r10 + r6
            r6 = 1073741824(0x40000000, float:2.0)
            int r7 = android.view.View.MeasureSpec.makeMeasureSpec(r10, r6)
            r4.measure(r7, r15)
        L_0x0212:
            int r3 = r3 + 1
            r1 = r20
            r15 = 8
            goto L_0x012e
        L_0x021a:
            int r1 = r21.getPaddingTop()
            int r13 = r13 + r1
            int r1 = r21.getPaddingBottom()
            int r13 = r13 + r1
            r0.setMeasuredDimension(r2, r13)
            r0.mCanSlide = r11
            androidx.customview.widget.ViewDragHelper r1 = r0.e
            int r1 = r1.getViewDragState()
            if (r1 == 0) goto L_0x0238
            if (r11 != 0) goto L_0x0238
            androidx.customview.widget.ViewDragHelper r1 = r0.e
            r1.abort()
        L_0x0238:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.slidingpanelayout.widget.SlidingPaneLayout.onMeasure(int, int):void");
    }

    /* access modifiers changed from: protected */
    public void onRestoreInstanceState(Parcelable parcelable) {
        if (!(parcelable instanceof SavedState)) {
            super.onRestoreInstanceState(parcelable);
            return;
        }
        SavedState savedState = (SavedState) parcelable;
        super.onRestoreInstanceState(savedState.getSuperState());
        if (savedState.a) {
            openPane();
        } else {
            closePane();
        }
        this.f = savedState.a;
    }

    /* access modifiers changed from: protected */
    public Parcelable onSaveInstanceState() {
        SavedState savedState = new SavedState(super.onSaveInstanceState());
        savedState.a = isSlideable() ? isOpen() : this.f;
        return savedState;
    }

    /* access modifiers changed from: protected */
    public void onSizeChanged(int i, int i2, int i3, int i4) {
        super.onSizeChanged(i, i2, i3, i4);
        if (i != i3) {
            this.mFirstLayout = true;
        }
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        if (!this.mCanSlide) {
            return super.onTouchEvent(motionEvent);
        }
        this.e.processTouchEvent(motionEvent);
        int actionMasked = motionEvent.getActionMasked();
        if (actionMasked == 0) {
            float x = motionEvent.getX();
            float y = motionEvent.getY();
            this.mInitialMotionX = x;
            this.mInitialMotionY = y;
        } else if (actionMasked == 1 && e(this.a)) {
            float x2 = motionEvent.getX();
            float y2 = motionEvent.getY();
            float f2 = x2 - this.mInitialMotionX;
            float f3 = y2 - this.mInitialMotionY;
            int touchSlop = this.e.getTouchSlop();
            if ((f2 * f2) + (f3 * f3) < ((float) (touchSlop * touchSlop)) && this.e.isViewUnder(this.a, (int) x2, (int) y2)) {
                closePane(this.a, 0);
            }
        }
        return true;
    }

    public boolean openPane() {
        return openPane(this.a, 0);
    }

    public void requestChildFocus(View view, View view2) {
        super.requestChildFocus(view, view2);
        if (!isInTouchMode() && !this.mCanSlide) {
            this.f = view == this.a;
        }
    }

    public void setCoveredFadeColor(@ColorInt int i) {
        this.mCoveredFadeColor = i;
    }

    public void setPanelSlideListener(@Nullable PanelSlideListener panelSlideListener) {
        this.mPanelSlideListener = panelSlideListener;
    }

    public void setParallaxDistance(@Px int i) {
        this.mParallaxBy = i;
        requestLayout();
    }

    @Deprecated
    public void setShadowDrawable(Drawable drawable) {
        setShadowDrawableLeft(drawable);
    }

    public void setShadowDrawableLeft(@Nullable Drawable drawable) {
        this.mShadowDrawableLeft = drawable;
    }

    public void setShadowDrawableRight(@Nullable Drawable drawable) {
        this.mShadowDrawableRight = drawable;
    }

    @Deprecated
    public void setShadowResource(@DrawableRes int i) {
        setShadowDrawable(getResources().getDrawable(i));
    }

    public void setShadowResourceLeft(int i) {
        setShadowDrawableLeft(ContextCompat.getDrawable(getContext(), i));
    }

    public void setShadowResourceRight(int i) {
        setShadowDrawableRight(ContextCompat.getDrawable(getContext(), i));
    }

    public void setSliderFadeColor(@ColorInt int i) {
        this.mSliderFadeColor = i;
    }

    @Deprecated
    public void smoothSlideClosed() {
        closePane();
    }

    @Deprecated
    public void smoothSlideOpen() {
        openPane();
    }
}
