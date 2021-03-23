package androidx.swiperefreshlayout.widget;

import android.animation.Animator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.Drawable;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.core.util.Preconditions;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class CircularProgressDrawable extends Drawable implements Animatable {
    private static final int ANIMATION_DURATION = 1332;
    private static final int ARROW_HEIGHT = 5;
    private static final int ARROW_HEIGHT_LARGE = 6;
    private static final int ARROW_WIDTH = 10;
    private static final int ARROW_WIDTH_LARGE = 12;
    private static final float CENTER_RADIUS = 7.5f;
    private static final float CENTER_RADIUS_LARGE = 11.0f;
    private static final int[] COLORS = {-16777216};
    private static final float COLOR_CHANGE_OFFSET = 0.75f;
    public static final int DEFAULT = 1;
    private static final float GROUP_FULL_ROTATION = 216.0f;
    public static final int LARGE = 0;
    private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    private static final Interpolator MATERIAL_INTERPOLATOR = new FastOutSlowInInterpolator();
    private static final float MAX_PROGRESS_ARC = 0.8f;
    private static final float MIN_PROGRESS_ARC = 0.01f;
    private static final float RING_ROTATION = 0.20999998f;
    private static final float SHRINK_OFFSET = 0.5f;
    private static final float STROKE_WIDTH = 2.5f;
    private static final float STROKE_WIDTH_LARGE = 3.0f;
    float a;
    boolean b;
    private Animator mAnimator;
    private Resources mResources;
    private final Ring mRing = new Ring();
    private float mRotation;

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    @Retention(RetentionPolicy.SOURCE)
    public @interface ProgressDrawableSize {
    }

    private static class Ring {
        final RectF a = new RectF();
        final Paint b = new Paint();
        final Paint c = new Paint();
        final Paint d = new Paint();
        float e = 0.0f;
        float f = 0.0f;
        float g = 0.0f;
        float h = 5.0f;
        int[] i;
        int j;
        float k;
        float l;
        float m;
        boolean n;
        Path o;
        float p = 1.0f;
        float q;
        int r;
        int s;
        int t = 255;
        int u;

        Ring() {
            this.b.setStrokeCap(Paint.Cap.SQUARE);
            this.b.setAntiAlias(true);
            this.b.setStyle(Paint.Style.STROKE);
            this.c.setStyle(Paint.Style.FILL);
            this.c.setAntiAlias(true);
            this.d.setColor(0);
        }

        /* access modifiers changed from: package-private */
        public int a() {
            return this.t;
        }

        /* access modifiers changed from: package-private */
        public void a(float f2) {
            if (f2 != this.p) {
                this.p = f2;
            }
        }

        /* access modifiers changed from: package-private */
        public void a(float f2, float f3) {
            this.r = (int) f2;
            this.s = (int) f3;
        }

        /* access modifiers changed from: package-private */
        public void a(int i2) {
            this.t = i2;
        }

        /* access modifiers changed from: package-private */
        public void a(Canvas canvas, float f2, float f3, RectF rectF) {
            if (this.n) {
                Path path = this.o;
                if (path == null) {
                    this.o = new Path();
                    this.o.setFillType(Path.FillType.EVEN_ODD);
                } else {
                    path.reset();
                }
                this.o.moveTo(0.0f, 0.0f);
                this.o.lineTo(((float) this.r) * this.p, 0.0f);
                Path path2 = this.o;
                float f4 = this.p;
                path2.lineTo((((float) this.r) * f4) / 2.0f, ((float) this.s) * f4);
                this.o.offset(((Math.min(rectF.width(), rectF.height()) / 2.0f) + rectF.centerX()) - ((((float) this.r) * this.p) / 2.0f), rectF.centerY() + (this.h / 2.0f));
                this.o.close();
                this.c.setColor(this.u);
                this.c.setAlpha(this.t);
                canvas.save();
                canvas.rotate(f2 + f3, rectF.centerX(), rectF.centerY());
                canvas.drawPath(this.o, this.c);
                canvas.restore();
            }
        }

        /* access modifiers changed from: package-private */
        public void a(Canvas canvas, Rect rect) {
            RectF rectF = this.a;
            float f2 = this.q;
            float f3 = (this.h / 2.0f) + f2;
            if (f2 <= 0.0f) {
                f3 = (((float) Math.min(rect.width(), rect.height())) / 2.0f) - Math.max((((float) this.r) * this.p) / 2.0f, this.h / 2.0f);
            }
            rectF.set(((float) rect.centerX()) - f3, ((float) rect.centerY()) - f3, ((float) rect.centerX()) + f3, ((float) rect.centerY()) + f3);
            float f4 = this.e;
            float f5 = this.g;
            float f6 = (f4 + f5) * 360.0f;
            float f7 = ((this.f + f5) * 360.0f) - f6;
            this.b.setColor(this.u);
            this.b.setAlpha(this.t);
            float f8 = this.h / 2.0f;
            rectF.inset(f8, f8);
            canvas.drawCircle(rectF.centerX(), rectF.centerY(), rectF.width() / 2.0f, this.d);
            float f9 = -f8;
            rectF.inset(f9, f9);
            canvas.drawArc(rectF, f6, f7, false, this.b);
            a(canvas, f6, f7, rectF);
        }

        /* access modifiers changed from: package-private */
        public void a(ColorFilter colorFilter) {
            this.b.setColorFilter(colorFilter);
        }

        /* access modifiers changed from: package-private */
        public void a(Paint.Cap cap) {
            this.b.setStrokeCap(cap);
        }

        /* access modifiers changed from: package-private */
        public void a(boolean z) {
            if (this.n != z) {
                this.n = z;
            }
        }

        /* access modifiers changed from: package-private */
        public void a(@NonNull int[] iArr) {
            this.i = iArr;
            d(0);
        }

        /* access modifiers changed from: package-private */
        public float b() {
            return (float) this.s;
        }

        /* access modifiers changed from: package-private */
        public void b(float f2) {
            this.q = f2;
        }

        /* access modifiers changed from: package-private */
        public void b(int i2) {
            this.d.setColor(i2);
        }

        /* access modifiers changed from: package-private */
        public float c() {
            return this.p;
        }

        /* access modifiers changed from: package-private */
        public void c(float f2) {
            this.f = f2;
        }

        /* access modifiers changed from: package-private */
        public void c(int i2) {
            this.u = i2;
        }

        /* access modifiers changed from: package-private */
        public float d() {
            return (float) this.r;
        }

        /* access modifiers changed from: package-private */
        public void d(float f2) {
            this.g = f2;
        }

        /* access modifiers changed from: package-private */
        public void d(int i2) {
            this.j = i2;
            this.u = this.i[this.j];
        }

        /* access modifiers changed from: package-private */
        public int e() {
            return this.d.getColor();
        }

        /* access modifiers changed from: package-private */
        public void e(float f2) {
            this.e = f2;
        }

        /* access modifiers changed from: package-private */
        public float f() {
            return this.q;
        }

        /* access modifiers changed from: package-private */
        public void f(float f2) {
            this.h = f2;
            this.b.setStrokeWidth(f2);
        }

        /* access modifiers changed from: package-private */
        public int[] g() {
            return this.i;
        }

        /* access modifiers changed from: package-private */
        public float h() {
            return this.f;
        }

        /* access modifiers changed from: package-private */
        public int i() {
            return this.i[j()];
        }

        /* access modifiers changed from: package-private */
        public int j() {
            return (this.j + 1) % this.i.length;
        }

        /* access modifiers changed from: package-private */
        public float k() {
            return this.g;
        }

        /* access modifiers changed from: package-private */
        public boolean l() {
            return this.n;
        }

        /* access modifiers changed from: package-private */
        public float m() {
            return this.e;
        }

        /* access modifiers changed from: package-private */
        public int n() {
            return this.i[this.j];
        }

        /* access modifiers changed from: package-private */
        public float o() {
            return this.l;
        }

        /* access modifiers changed from: package-private */
        public float p() {
            return this.m;
        }

        /* access modifiers changed from: package-private */
        public float q() {
            return this.k;
        }

        /* access modifiers changed from: package-private */
        public Paint.Cap r() {
            return this.b.getStrokeCap();
        }

        /* access modifiers changed from: package-private */
        public float s() {
            return this.h;
        }

        /* access modifiers changed from: package-private */
        public void t() {
            d(j());
        }

        /* access modifiers changed from: package-private */
        public void u() {
            this.k = 0.0f;
            this.l = 0.0f;
            this.m = 0.0f;
            e(0.0f);
            c(0.0f);
            d(0.0f);
        }

        /* access modifiers changed from: package-private */
        public void v() {
            this.k = this.e;
            this.l = this.f;
            this.m = this.g;
        }
    }

    public CircularProgressDrawable(@NonNull Context context) {
        this.mResources = ((Context) Preconditions.checkNotNull(context)).getResources();
        this.mRing.a(COLORS);
        setStrokeWidth(STROKE_WIDTH);
        setupAnimators();
    }

    private void applyFinishTranslation(float f, Ring ring) {
        a(f, ring);
        ring.e(ring.q() + (((ring.o() - MIN_PROGRESS_ARC) - ring.q()) * f));
        ring.c(ring.o());
        ring.d(ring.p() + ((((float) (Math.floor((double) (ring.p() / MAX_PROGRESS_ARC)) + 1.0d)) - ring.p()) * f));
    }

    private int evaluateColorChange(float f, int i, int i2) {
        int i3 = (i >> 24) & 255;
        int i4 = (i >> 16) & 255;
        int i5 = (i >> 8) & 255;
        int i6 = i & 255;
        return ((i3 + ((int) (((float) (((i2 >> 24) & 255) - i3)) * f))) << 24) | ((i4 + ((int) (((float) (((i2 >> 16) & 255) - i4)) * f))) << 16) | ((i5 + ((int) (((float) (((i2 >> 8) & 255) - i5)) * f))) << 8) | (i6 + ((int) (f * ((float) ((i2 & 255) - i6)))));
    }

    private float getRotation() {
        return this.mRotation;
    }

    private void setRotation(float f) {
        this.mRotation = f;
    }

    private void setSizeParameters(float f, float f2, float f3, float f4) {
        Ring ring = this.mRing;
        float f5 = this.mResources.getDisplayMetrics().density;
        ring.f(f2 * f5);
        ring.b(f * f5);
        ring.d(0);
        ring.a(f3 * f5, f4 * f5);
    }

    private void setupAnimators() {
        final Ring ring = this.mRing;
        ValueAnimator ofFloat = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        ofFloat.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float floatValue = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                CircularProgressDrawable.this.a(floatValue, ring);
                CircularProgressDrawable.this.a(floatValue, ring, false);
                CircularProgressDrawable.this.invalidateSelf();
            }
        });
        ofFloat.setRepeatCount(-1);
        ofFloat.setRepeatMode(1);
        ofFloat.setInterpolator(LINEAR_INTERPOLATOR);
        ofFloat.addListener(new Animator.AnimatorListener() {
            public void onAnimationCancel(Animator animator) {
            }

            public void onAnimationEnd(Animator animator) {
            }

            public void onAnimationRepeat(Animator animator) {
                CircularProgressDrawable.this.a(1.0f, ring, true);
                ring.v();
                ring.t();
                CircularProgressDrawable circularProgressDrawable = CircularProgressDrawable.this;
                if (circularProgressDrawable.b) {
                    circularProgressDrawable.b = false;
                    animator.cancel();
                    animator.setDuration(1332);
                    animator.start();
                    ring.a(false);
                    return;
                }
                circularProgressDrawable.a += 1.0f;
            }

            public void onAnimationStart(Animator animator) {
                CircularProgressDrawable.this.a = 0.0f;
            }
        });
        this.mAnimator = ofFloat;
    }

    /* access modifiers changed from: package-private */
    public void a(float f, Ring ring) {
        ring.c(f > COLOR_CHANGE_OFFSET ? evaluateColorChange((f - COLOR_CHANGE_OFFSET) / 0.25f, ring.n(), ring.i()) : ring.n());
    }

    /* access modifiers changed from: package-private */
    public void a(float f, Ring ring, boolean z) {
        float f2;
        float f3;
        if (this.b) {
            applyFinishTranslation(f, ring);
        } else if (f != 1.0f || z) {
            float p = ring.p();
            if (f < SHRINK_OFFSET) {
                float f4 = f / SHRINK_OFFSET;
                float q = ring.q();
                float f5 = q;
                f2 = (MATERIAL_INTERPOLATOR.getInterpolation(f4) * 0.79f) + MIN_PROGRESS_ARC + q;
                f3 = f5;
            } else {
                float f6 = (f - SHRINK_OFFSET) / SHRINK_OFFSET;
                f2 = ring.q() + 0.79f;
                f3 = f2 - (((1.0f - MATERIAL_INTERPOLATOR.getInterpolation(f6)) * 0.79f) + MIN_PROGRESS_ARC);
            }
            float f7 = (f + this.a) * GROUP_FULL_ROTATION;
            ring.e(f3);
            ring.c(f2);
            ring.d(p + (RING_ROTATION * f));
            setRotation(f7);
        }
    }

    public void draw(Canvas canvas) {
        Rect bounds = getBounds();
        canvas.save();
        canvas.rotate(this.mRotation, bounds.exactCenterX(), bounds.exactCenterY());
        this.mRing.a(canvas, bounds);
        canvas.restore();
    }

    public int getAlpha() {
        return this.mRing.a();
    }

    public boolean getArrowEnabled() {
        return this.mRing.l();
    }

    public float getArrowHeight() {
        return this.mRing.b();
    }

    public float getArrowScale() {
        return this.mRing.c();
    }

    public float getArrowWidth() {
        return this.mRing.d();
    }

    public int getBackgroundColor() {
        return this.mRing.e();
    }

    public float getCenterRadius() {
        return this.mRing.f();
    }

    @NonNull
    public int[] getColorSchemeColors() {
        return this.mRing.g();
    }

    public float getEndTrim() {
        return this.mRing.h();
    }

    public int getOpacity() {
        return -3;
    }

    public float getProgressRotation() {
        return this.mRing.k();
    }

    public float getStartTrim() {
        return this.mRing.m();
    }

    @NonNull
    public Paint.Cap getStrokeCap() {
        return this.mRing.r();
    }

    public float getStrokeWidth() {
        return this.mRing.s();
    }

    public boolean isRunning() {
        return this.mAnimator.isRunning();
    }

    public void setAlpha(int i) {
        this.mRing.a(i);
        invalidateSelf();
    }

    public void setArrowDimensions(float f, float f2) {
        this.mRing.a(f, f2);
        invalidateSelf();
    }

    public void setArrowEnabled(boolean z) {
        this.mRing.a(z);
        invalidateSelf();
    }

    public void setArrowScale(float f) {
        this.mRing.a(f);
        invalidateSelf();
    }

    public void setBackgroundColor(int i) {
        this.mRing.b(i);
        invalidateSelf();
    }

    public void setCenterRadius(float f) {
        this.mRing.b(f);
        invalidateSelf();
    }

    public void setColorFilter(ColorFilter colorFilter) {
        this.mRing.a(colorFilter);
        invalidateSelf();
    }

    public void setColorSchemeColors(@NonNull int... iArr) {
        this.mRing.a(iArr);
        this.mRing.d(0);
        invalidateSelf();
    }

    public void setProgressRotation(float f) {
        this.mRing.d(f);
        invalidateSelf();
    }

    public void setStartEndTrim(float f, float f2) {
        this.mRing.e(f);
        this.mRing.c(f2);
        invalidateSelf();
    }

    public void setStrokeCap(@NonNull Paint.Cap cap) {
        this.mRing.a(cap);
        invalidateSelf();
    }

    public void setStrokeWidth(float f) {
        this.mRing.f(f);
        invalidateSelf();
    }

    public void setStyle(int i) {
        float f;
        float f2;
        float f3;
        float f4;
        if (i == 0) {
            f = CENTER_RADIUS_LARGE;
            f4 = STROKE_WIDTH_LARGE;
            f3 = 12.0f;
            f2 = 6.0f;
        } else {
            f = CENTER_RADIUS;
            f4 = STROKE_WIDTH;
            f3 = 10.0f;
            f2 = 5.0f;
        }
        setSizeParameters(f, f4, f3, f2);
        invalidateSelf();
    }

    public void start() {
        long j;
        Animator animator;
        this.mAnimator.cancel();
        this.mRing.v();
        if (this.mRing.h() != this.mRing.m()) {
            this.b = true;
            animator = this.mAnimator;
            j = 666;
        } else {
            this.mRing.d(0);
            this.mRing.u();
            animator = this.mAnimator;
            j = 1332;
        }
        animator.setDuration(j);
        this.mAnimator.start();
    }

    public void stop() {
        this.mAnimator.cancel();
        setRotation(0.0f);
        this.mRing.a(false);
        this.mRing.d(0);
        this.mRing.u();
        invalidateSelf();
    }
}
