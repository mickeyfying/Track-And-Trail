package androidx.vectordrawable.graphics.drawable;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.collection.ArrayMap;
import androidx.core.content.res.TypedArrayUtils;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;

public class AnimatedVectorDrawableCompat extends VectorDrawableCommon implements Animatable2Compat {
    private static final String ANIMATED_VECTOR = "animated-vector";
    private static final boolean DBG_ANIMATION_VECTOR_DRAWABLE = false;
    private static final String LOGTAG = "AnimatedVDCompat";
    private static final String TARGET = "target";
    ArrayList<Animatable2Compat.AnimationCallback> b;
    final Drawable.Callback c;
    private AnimatedVectorDrawableCompatState mAnimatedVectorState;
    private Animator.AnimatorListener mAnimatorListener;
    private ArgbEvaluator mArgbEvaluator;
    private Context mContext;

    private static class AnimatedVectorDrawableCompatState extends Drawable.ConstantState {
        int a;
        VectorDrawableCompat b;
        AnimatorSet c;
        ArrayList<Animator> d;
        ArrayMap<Animator, String> e;

        public AnimatedVectorDrawableCompatState(Context context, AnimatedVectorDrawableCompatState animatedVectorDrawableCompatState, Drawable.Callback callback, Resources resources) {
            if (animatedVectorDrawableCompatState != null) {
                this.a = animatedVectorDrawableCompatState.a;
                VectorDrawableCompat vectorDrawableCompat = animatedVectorDrawableCompatState.b;
                if (vectorDrawableCompat != null) {
                    Drawable.ConstantState constantState = vectorDrawableCompat.getConstantState();
                    this.b = (VectorDrawableCompat) (resources != null ? constantState.newDrawable(resources) : constantState.newDrawable());
                    this.b = (VectorDrawableCompat) this.b.mutate();
                    this.b.setCallback(callback);
                    this.b.setBounds(animatedVectorDrawableCompatState.b.getBounds());
                    this.b.a(false);
                }
                ArrayList<Animator> arrayList = animatedVectorDrawableCompatState.d;
                if (arrayList != null) {
                    int size = arrayList.size();
                    this.d = new ArrayList<>(size);
                    this.e = new ArrayMap<>(size);
                    for (int i = 0; i < size; i++) {
                        Animator animator = animatedVectorDrawableCompatState.d.get(i);
                        Animator clone = animator.clone();
                        String str = animatedVectorDrawableCompatState.e.get(animator);
                        clone.setTarget(this.b.a(str));
                        this.d.add(clone);
                        this.e.put(clone, str);
                    }
                    setupAnimatorSet();
                }
            }
        }

        public int getChangingConfigurations() {
            return this.a;
        }

        public Drawable newDrawable() {
            throw new IllegalStateException("No constant state support for SDK < 24.");
        }

        public Drawable newDrawable(Resources resources) {
            throw new IllegalStateException("No constant state support for SDK < 24.");
        }

        public void setupAnimatorSet() {
            if (this.c == null) {
                this.c = new AnimatorSet();
            }
            this.c.playTogether(this.d);
        }
    }

    @RequiresApi(24)
    private static class AnimatedVectorDrawableDelegateState extends Drawable.ConstantState {
        private final Drawable.ConstantState mDelegateState;

        public AnimatedVectorDrawableDelegateState(Drawable.ConstantState constantState) {
            this.mDelegateState = constantState;
        }

        public boolean canApplyTheme() {
            return this.mDelegateState.canApplyTheme();
        }

        public int getChangingConfigurations() {
            return this.mDelegateState.getChangingConfigurations();
        }

        public Drawable newDrawable() {
            AnimatedVectorDrawableCompat animatedVectorDrawableCompat = new AnimatedVectorDrawableCompat();
            animatedVectorDrawableCompat.a = this.mDelegateState.newDrawable();
            animatedVectorDrawableCompat.a.setCallback(animatedVectorDrawableCompat.c);
            return animatedVectorDrawableCompat;
        }

        public Drawable newDrawable(Resources resources) {
            AnimatedVectorDrawableCompat animatedVectorDrawableCompat = new AnimatedVectorDrawableCompat();
            animatedVectorDrawableCompat.a = this.mDelegateState.newDrawable(resources);
            animatedVectorDrawableCompat.a.setCallback(animatedVectorDrawableCompat.c);
            return animatedVectorDrawableCompat;
        }

        public Drawable newDrawable(Resources resources, Resources.Theme theme) {
            AnimatedVectorDrawableCompat animatedVectorDrawableCompat = new AnimatedVectorDrawableCompat();
            animatedVectorDrawableCompat.a = this.mDelegateState.newDrawable(resources, theme);
            animatedVectorDrawableCompat.a.setCallback(animatedVectorDrawableCompat.c);
            return animatedVectorDrawableCompat;
        }
    }

    AnimatedVectorDrawableCompat() {
        this((Context) null, (AnimatedVectorDrawableCompatState) null, (Resources) null);
    }

    private AnimatedVectorDrawableCompat(@Nullable Context context) {
        this(context, (AnimatedVectorDrawableCompatState) null, (Resources) null);
    }

    private AnimatedVectorDrawableCompat(@Nullable Context context, @Nullable AnimatedVectorDrawableCompatState animatedVectorDrawableCompatState, @Nullable Resources resources) {
        this.mArgbEvaluator = null;
        this.mAnimatorListener = null;
        this.b = null;
        this.c = new Drawable.Callback() {
            public void invalidateDrawable(Drawable drawable) {
                AnimatedVectorDrawableCompat.this.invalidateSelf();
            }

            public void scheduleDrawable(Drawable drawable, Runnable runnable, long j) {
                AnimatedVectorDrawableCompat.this.scheduleSelf(runnable, j);
            }

            public void unscheduleDrawable(Drawable drawable, Runnable runnable) {
                AnimatedVectorDrawableCompat.this.unscheduleSelf(runnable);
            }
        };
        this.mContext = context;
        if (animatedVectorDrawableCompatState != null) {
            this.mAnimatedVectorState = animatedVectorDrawableCompatState;
        } else {
            this.mAnimatedVectorState = new AnimatedVectorDrawableCompatState(context, animatedVectorDrawableCompatState, this.c, resources);
        }
    }

    public static void clearAnimationCallbacks(Drawable drawable) {
        if (drawable instanceof Animatable) {
            if (Build.VERSION.SDK_INT >= 24) {
                ((AnimatedVectorDrawable) drawable).clearAnimationCallbacks();
            } else {
                ((AnimatedVectorDrawableCompat) drawable).clearAnimationCallbacks();
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x0049 A[Catch:{ IOException | XmlPullParserException -> 0x005e }] */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x0056 A[Catch:{ IOException | XmlPullParserException -> 0x005e }] */
    @androidx.annotation.Nullable
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat create(@androidx.annotation.NonNull android.content.Context r6, @androidx.annotation.DrawableRes int r7) {
        /*
            java.lang.String r0 = "parser error"
            java.lang.String r1 = "AnimatedVDCompat"
            int r2 = android.os.Build.VERSION.SDK_INT
            r3 = 24
            if (r2 < r3) goto L_0x0030
            androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat r0 = new androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat
            r0.<init>(r6)
            android.content.res.Resources r1 = r6.getResources()
            android.content.res.Resources$Theme r6 = r6.getTheme()
            android.graphics.drawable.Drawable r6 = androidx.core.content.res.ResourcesCompat.getDrawable(r1, r7, r6)
            r0.a = r6
            android.graphics.drawable.Drawable r6 = r0.a
            android.graphics.drawable.Drawable$Callback r7 = r0.c
            r6.setCallback(r7)
            androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat$AnimatedVectorDrawableDelegateState r6 = new androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat$AnimatedVectorDrawableDelegateState
            android.graphics.drawable.Drawable r7 = r0.a
            android.graphics.drawable.Drawable$ConstantState r7 = r7.getConstantState()
            r6.<init>(r7)
            return r0
        L_0x0030:
            android.content.res.Resources r2 = r6.getResources()
            android.content.res.XmlResourceParser r7 = r2.getXml(r7)     // Catch:{ XmlPullParserException -> 0x0060, IOException -> 0x005e }
            android.util.AttributeSet r2 = android.util.Xml.asAttributeSet(r7)     // Catch:{ XmlPullParserException -> 0x0060, IOException -> 0x005e }
        L_0x003c:
            int r3 = r7.next()     // Catch:{ XmlPullParserException -> 0x0060, IOException -> 0x005e }
            r4 = 2
            if (r3 == r4) goto L_0x0047
            r5 = 1
            if (r3 == r5) goto L_0x0047
            goto L_0x003c
        L_0x0047:
            if (r3 != r4) goto L_0x0056
            android.content.res.Resources r3 = r6.getResources()     // Catch:{ XmlPullParserException -> 0x0060, IOException -> 0x005e }
            android.content.res.Resources$Theme r4 = r6.getTheme()     // Catch:{ XmlPullParserException -> 0x0060, IOException -> 0x005e }
            androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat r6 = createFromXmlInner(r6, r3, r7, r2, r4)     // Catch:{ XmlPullParserException -> 0x0060, IOException -> 0x005e }
            return r6
        L_0x0056:
            org.xmlpull.v1.XmlPullParserException r6 = new org.xmlpull.v1.XmlPullParserException     // Catch:{ XmlPullParserException -> 0x0060, IOException -> 0x005e }
            java.lang.String r7 = "No start tag found"
            r6.<init>(r7)     // Catch:{ XmlPullParserException -> 0x0060, IOException -> 0x005e }
            throw r6     // Catch:{ XmlPullParserException -> 0x0060, IOException -> 0x005e }
        L_0x005e:
            r6 = move-exception
            goto L_0x0061
        L_0x0060:
            r6 = move-exception
        L_0x0061:
            android.util.Log.e(r1, r0, r6)
            r6 = 0
            return r6
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat.create(android.content.Context, int):androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat");
    }

    public static AnimatedVectorDrawableCompat createFromXmlInner(Context context, Resources resources, XmlPullParser xmlPullParser, AttributeSet attributeSet, Resources.Theme theme) {
        AnimatedVectorDrawableCompat animatedVectorDrawableCompat = new AnimatedVectorDrawableCompat(context);
        animatedVectorDrawableCompat.inflate(resources, xmlPullParser, attributeSet, theme);
        return animatedVectorDrawableCompat;
    }

    public static void registerAnimationCallback(Drawable drawable, Animatable2Compat.AnimationCallback animationCallback) {
        if (drawable != null && animationCallback != null && (drawable instanceof Animatable)) {
            if (Build.VERSION.SDK_INT >= 24) {
                registerPlatformCallback((AnimatedVectorDrawable) drawable, animationCallback);
            } else {
                ((AnimatedVectorDrawableCompat) drawable).registerAnimationCallback(animationCallback);
            }
        }
    }

    @RequiresApi(23)
    private static void registerPlatformCallback(@NonNull AnimatedVectorDrawable animatedVectorDrawable, @NonNull Animatable2Compat.AnimationCallback animationCallback) {
        animatedVectorDrawable.registerAnimationCallback(animationCallback.a());
    }

    private void removeAnimatorSetListener() {
        Animator.AnimatorListener animatorListener = this.mAnimatorListener;
        if (animatorListener != null) {
            this.mAnimatedVectorState.c.removeListener(animatorListener);
            this.mAnimatorListener = null;
        }
    }

    private void setupAnimatorsForTarget(String str, Animator animator) {
        animator.setTarget(this.mAnimatedVectorState.b.a(str));
        if (Build.VERSION.SDK_INT < 21) {
            setupColorAnimator(animator);
        }
        AnimatedVectorDrawableCompatState animatedVectorDrawableCompatState = this.mAnimatedVectorState;
        if (animatedVectorDrawableCompatState.d == null) {
            animatedVectorDrawableCompatState.d = new ArrayList<>();
            this.mAnimatedVectorState.e = new ArrayMap<>();
        }
        this.mAnimatedVectorState.d.add(animator);
        this.mAnimatedVectorState.e.put(animator, str);
    }

    private void setupColorAnimator(Animator animator) {
        ArrayList<Animator> childAnimations;
        if ((animator instanceof AnimatorSet) && (childAnimations = ((AnimatorSet) animator).getChildAnimations()) != null) {
            for (int i = 0; i < childAnimations.size(); i++) {
                setupColorAnimator(childAnimations.get(i));
            }
        }
        if (animator instanceof ObjectAnimator) {
            ObjectAnimator objectAnimator = (ObjectAnimator) animator;
            String propertyName = objectAnimator.getPropertyName();
            if ("fillColor".equals(propertyName) || "strokeColor".equals(propertyName)) {
                if (this.mArgbEvaluator == null) {
                    this.mArgbEvaluator = new ArgbEvaluator();
                }
                objectAnimator.setEvaluator(this.mArgbEvaluator);
            }
        }
    }

    public static boolean unregisterAnimationCallback(Drawable drawable, Animatable2Compat.AnimationCallback animationCallback) {
        if (drawable == null || animationCallback == null || !(drawable instanceof Animatable)) {
            return false;
        }
        return Build.VERSION.SDK_INT >= 24 ? unregisterPlatformCallback((AnimatedVectorDrawable) drawable, animationCallback) : ((AnimatedVectorDrawableCompat) drawable).unregisterAnimationCallback(animationCallback);
    }

    @RequiresApi(23)
    private static boolean unregisterPlatformCallback(AnimatedVectorDrawable animatedVectorDrawable, Animatable2Compat.AnimationCallback animationCallback) {
        return animatedVectorDrawable.unregisterAnimationCallback(animationCallback.a());
    }

    public void applyTheme(Resources.Theme theme) {
        Drawable drawable = this.a;
        if (drawable != null) {
            DrawableCompat.applyTheme(drawable, theme);
        }
    }

    public boolean canApplyTheme() {
        Drawable drawable = this.a;
        if (drawable != null) {
            return DrawableCompat.canApplyTheme(drawable);
        }
        return false;
    }

    public void clearAnimationCallbacks() {
        Drawable drawable = this.a;
        if (drawable != null) {
            ((AnimatedVectorDrawable) drawable).clearAnimationCallbacks();
            return;
        }
        removeAnimatorSetListener();
        ArrayList<Animatable2Compat.AnimationCallback> arrayList = this.b;
        if (arrayList != null) {
            arrayList.clear();
        }
    }

    public /* bridge */ /* synthetic */ void clearColorFilter() {
        super.clearColorFilter();
    }

    public void draw(Canvas canvas) {
        Drawable drawable = this.a;
        if (drawable != null) {
            drawable.draw(canvas);
            return;
        }
        this.mAnimatedVectorState.b.draw(canvas);
        if (this.mAnimatedVectorState.c.isStarted()) {
            invalidateSelf();
        }
    }

    public int getAlpha() {
        Drawable drawable = this.a;
        return drawable != null ? DrawableCompat.getAlpha(drawable) : this.mAnimatedVectorState.b.getAlpha();
    }

    public int getChangingConfigurations() {
        Drawable drawable = this.a;
        return drawable != null ? drawable.getChangingConfigurations() : super.getChangingConfigurations() | this.mAnimatedVectorState.a;
    }

    public ColorFilter getColorFilter() {
        Drawable drawable = this.a;
        return drawable != null ? DrawableCompat.getColorFilter(drawable) : this.mAnimatedVectorState.b.getColorFilter();
    }

    public Drawable.ConstantState getConstantState() {
        Drawable drawable = this.a;
        if (drawable == null || Build.VERSION.SDK_INT < 24) {
            return null;
        }
        return new AnimatedVectorDrawableDelegateState(drawable.getConstantState());
    }

    public /* bridge */ /* synthetic */ Drawable getCurrent() {
        return super.getCurrent();
    }

    public int getIntrinsicHeight() {
        Drawable drawable = this.a;
        return drawable != null ? drawable.getIntrinsicHeight() : this.mAnimatedVectorState.b.getIntrinsicHeight();
    }

    public int getIntrinsicWidth() {
        Drawable drawable = this.a;
        return drawable != null ? drawable.getIntrinsicWidth() : this.mAnimatedVectorState.b.getIntrinsicWidth();
    }

    public /* bridge */ /* synthetic */ int getMinimumHeight() {
        return super.getMinimumHeight();
    }

    public /* bridge */ /* synthetic */ int getMinimumWidth() {
        return super.getMinimumWidth();
    }

    public int getOpacity() {
        Drawable drawable = this.a;
        return drawable != null ? drawable.getOpacity() : this.mAnimatedVectorState.b.getOpacity();
    }

    public /* bridge */ /* synthetic */ boolean getPadding(Rect rect) {
        return super.getPadding(rect);
    }

    public /* bridge */ /* synthetic */ int[] getState() {
        return super.getState();
    }

    public /* bridge */ /* synthetic */ Region getTransparentRegion() {
        return super.getTransparentRegion();
    }

    public void inflate(Resources resources, XmlPullParser xmlPullParser, AttributeSet attributeSet) {
        inflate(resources, xmlPullParser, attributeSet, (Resources.Theme) null);
    }

    public void inflate(Resources resources, XmlPullParser xmlPullParser, AttributeSet attributeSet, Resources.Theme theme) {
        TypedArray obtainAttributes;
        Drawable drawable = this.a;
        if (drawable != null) {
            DrawableCompat.inflate(drawable, resources, xmlPullParser, attributeSet, theme);
            return;
        }
        int eventType = xmlPullParser.getEventType();
        int depth = xmlPullParser.getDepth() + 1;
        while (eventType != 1 && (xmlPullParser.getDepth() >= depth || eventType != 3)) {
            if (eventType == 2) {
                String name = xmlPullParser.getName();
                if (ANIMATED_VECTOR.equals(name)) {
                    obtainAttributes = TypedArrayUtils.obtainAttributes(resources, theme, attributeSet, AndroidResources.e);
                    int resourceId = obtainAttributes.getResourceId(0, 0);
                    if (resourceId != 0) {
                        VectorDrawableCompat create = VectorDrawableCompat.create(resources, resourceId, theme);
                        create.a(false);
                        create.setCallback(this.c);
                        VectorDrawableCompat vectorDrawableCompat = this.mAnimatedVectorState.b;
                        if (vectorDrawableCompat != null) {
                            vectorDrawableCompat.setCallback((Drawable.Callback) null);
                        }
                        this.mAnimatedVectorState.b = create;
                    }
                } else if (TARGET.equals(name)) {
                    obtainAttributes = resources.obtainAttributes(attributeSet, AndroidResources.f);
                    String string = obtainAttributes.getString(0);
                    int resourceId2 = obtainAttributes.getResourceId(1, 0);
                    if (resourceId2 != 0) {
                        Context context = this.mContext;
                        if (context != null) {
                            setupAnimatorsForTarget(string, AnimatorInflaterCompat.loadAnimator(context, resourceId2));
                        } else {
                            obtainAttributes.recycle();
                            throw new IllegalStateException("Context can't be null when inflating animators");
                        }
                    }
                } else {
                    continue;
                }
                obtainAttributes.recycle();
            }
            eventType = xmlPullParser.next();
        }
        this.mAnimatedVectorState.setupAnimatorSet();
    }

    public boolean isAutoMirrored() {
        Drawable drawable = this.a;
        return drawable != null ? DrawableCompat.isAutoMirrored(drawable) : this.mAnimatedVectorState.b.isAutoMirrored();
    }

    public boolean isRunning() {
        Drawable drawable = this.a;
        return drawable != null ? ((AnimatedVectorDrawable) drawable).isRunning() : this.mAnimatedVectorState.c.isRunning();
    }

    public boolean isStateful() {
        Drawable drawable = this.a;
        return drawable != null ? drawable.isStateful() : this.mAnimatedVectorState.b.isStateful();
    }

    public /* bridge */ /* synthetic */ void jumpToCurrentState() {
        super.jumpToCurrentState();
    }

    public Drawable mutate() {
        Drawable drawable = this.a;
        if (drawable != null) {
            drawable.mutate();
        }
        return this;
    }

    /* access modifiers changed from: protected */
    public void onBoundsChange(Rect rect) {
        Drawable drawable = this.a;
        if (drawable != null) {
            drawable.setBounds(rect);
        } else {
            this.mAnimatedVectorState.b.setBounds(rect);
        }
    }

    /* access modifiers changed from: protected */
    public boolean onLevelChange(int i) {
        Drawable drawable = this.a;
        return drawable != null ? drawable.setLevel(i) : this.mAnimatedVectorState.b.setLevel(i);
    }

    /* access modifiers changed from: protected */
    public boolean onStateChange(int[] iArr) {
        Drawable drawable = this.a;
        return drawable != null ? drawable.setState(iArr) : this.mAnimatedVectorState.b.setState(iArr);
    }

    public void registerAnimationCallback(@NonNull Animatable2Compat.AnimationCallback animationCallback) {
        Drawable drawable = this.a;
        if (drawable != null) {
            registerPlatformCallback((AnimatedVectorDrawable) drawable, animationCallback);
        } else if (animationCallback != null) {
            if (this.b == null) {
                this.b = new ArrayList<>();
            }
            if (!this.b.contains(animationCallback)) {
                this.b.add(animationCallback);
                if (this.mAnimatorListener == null) {
                    this.mAnimatorListener = new AnimatorListenerAdapter() {
                        public void onAnimationEnd(Animator animator) {
                            ArrayList arrayList = new ArrayList(AnimatedVectorDrawableCompat.this.b);
                            int size = arrayList.size();
                            for (int i = 0; i < size; i++) {
                                ((Animatable2Compat.AnimationCallback) arrayList.get(i)).onAnimationEnd(AnimatedVectorDrawableCompat.this);
                            }
                        }

                        public void onAnimationStart(Animator animator) {
                            ArrayList arrayList = new ArrayList(AnimatedVectorDrawableCompat.this.b);
                            int size = arrayList.size();
                            for (int i = 0; i < size; i++) {
                                ((Animatable2Compat.AnimationCallback) arrayList.get(i)).onAnimationStart(AnimatedVectorDrawableCompat.this);
                            }
                        }
                    };
                }
                this.mAnimatedVectorState.c.addListener(this.mAnimatorListener);
            }
        }
    }

    public void setAlpha(int i) {
        Drawable drawable = this.a;
        if (drawable != null) {
            drawable.setAlpha(i);
        } else {
            this.mAnimatedVectorState.b.setAlpha(i);
        }
    }

    public void setAutoMirrored(boolean z) {
        Drawable drawable = this.a;
        if (drawable != null) {
            DrawableCompat.setAutoMirrored(drawable, z);
        } else {
            this.mAnimatedVectorState.b.setAutoMirrored(z);
        }
    }

    public /* bridge */ /* synthetic */ void setChangingConfigurations(int i) {
        super.setChangingConfigurations(i);
    }

    public /* bridge */ /* synthetic */ void setColorFilter(int i, PorterDuff.Mode mode) {
        super.setColorFilter(i, mode);
    }

    public void setColorFilter(ColorFilter colorFilter) {
        Drawable drawable = this.a;
        if (drawable != null) {
            drawable.setColorFilter(colorFilter);
        } else {
            this.mAnimatedVectorState.b.setColorFilter(colorFilter);
        }
    }

    public /* bridge */ /* synthetic */ void setFilterBitmap(boolean z) {
        super.setFilterBitmap(z);
    }

    public /* bridge */ /* synthetic */ void setHotspot(float f, float f2) {
        super.setHotspot(f, f2);
    }

    public /* bridge */ /* synthetic */ void setHotspotBounds(int i, int i2, int i3, int i4) {
        super.setHotspotBounds(i, i2, i3, i4);
    }

    public /* bridge */ /* synthetic */ boolean setState(int[] iArr) {
        return super.setState(iArr);
    }

    public void setTint(int i) {
        Drawable drawable = this.a;
        if (drawable != null) {
            DrawableCompat.setTint(drawable, i);
        } else {
            this.mAnimatedVectorState.b.setTint(i);
        }
    }

    public void setTintList(ColorStateList colorStateList) {
        Drawable drawable = this.a;
        if (drawable != null) {
            DrawableCompat.setTintList(drawable, colorStateList);
        } else {
            this.mAnimatedVectorState.b.setTintList(colorStateList);
        }
    }

    public void setTintMode(PorterDuff.Mode mode) {
        Drawable drawable = this.a;
        if (drawable != null) {
            DrawableCompat.setTintMode(drawable, mode);
        } else {
            this.mAnimatedVectorState.b.setTintMode(mode);
        }
    }

    public boolean setVisible(boolean z, boolean z2) {
        Drawable drawable = this.a;
        if (drawable != null) {
            return drawable.setVisible(z, z2);
        }
        this.mAnimatedVectorState.b.setVisible(z, z2);
        return super.setVisible(z, z2);
    }

    public void start() {
        Drawable drawable = this.a;
        if (drawable != null) {
            ((AnimatedVectorDrawable) drawable).start();
        } else if (!this.mAnimatedVectorState.c.isStarted()) {
            this.mAnimatedVectorState.c.start();
            invalidateSelf();
        }
    }

    public void stop() {
        Drawable drawable = this.a;
        if (drawable != null) {
            ((AnimatedVectorDrawable) drawable).stop();
        } else {
            this.mAnimatedVectorState.c.end();
        }
    }

    public boolean unregisterAnimationCallback(@NonNull Animatable2Compat.AnimationCallback animationCallback) {
        Drawable drawable = this.a;
        if (drawable != null) {
            unregisterPlatformCallback((AnimatedVectorDrawable) drawable, animationCallback);
        }
        ArrayList<Animatable2Compat.AnimationCallback> arrayList = this.b;
        if (arrayList == null || animationCallback == null) {
            return false;
        }
        boolean remove = arrayList.remove(animationCallback);
        if (this.b.size() == 0) {
            removeAnimatorSetListener();
        }
        return remove;
    }
}
