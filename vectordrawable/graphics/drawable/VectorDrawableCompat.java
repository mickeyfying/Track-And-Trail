package androidx.vectordrawable.graphics.drawable;

import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;
import androidx.collection.ArrayMap;
import androidx.core.content.res.ComplexColorCompat;
import androidx.core.content.res.TypedArrayUtils;
import androidx.core.graphics.PathParser;
import androidx.core.graphics.drawable.DrawableCompat;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class VectorDrawableCompat extends VectorDrawableCommon {
    private static final boolean DBG_VECTOR_DRAWABLE = false;
    private static final int LINECAP_BUTT = 0;
    private static final int LINECAP_ROUND = 1;
    private static final int LINECAP_SQUARE = 2;
    private static final int LINEJOIN_BEVEL = 2;
    private static final int LINEJOIN_MITER = 0;
    private static final int LINEJOIN_ROUND = 1;
    private static final int MAX_CACHED_BITMAP_SIZE = 2048;
    private static final String SHAPE_CLIP_PATH = "clip-path";
    private static final String SHAPE_GROUP = "group";
    private static final String SHAPE_PATH = "path";
    private static final String SHAPE_VECTOR = "vector";
    static final PorterDuff.Mode b = PorterDuff.Mode.SRC_IN;
    private boolean mAllowCaching;
    private Drawable.ConstantState mCachedConstantStateDelegate;
    private ColorFilter mColorFilter;
    private boolean mMutated;
    private PorterDuffColorFilter mTintFilter;
    private final Rect mTmpBounds;
    private final float[] mTmpFloats;
    private final Matrix mTmpMatrix;
    private VectorDrawableCompatState mVectorState;

    private static class VClipPath extends VPath {
        VClipPath() {
        }

        VClipPath(VClipPath vClipPath) {
            super(vClipPath);
        }

        private void updateStateFromTypedArray(TypedArray typedArray, XmlPullParser xmlPullParser) {
            String string = typedArray.getString(0);
            if (string != null) {
                this.b = string;
            }
            String string2 = typedArray.getString(1);
            if (string2 != null) {
                this.a = PathParser.createNodesFromPathData(string2);
            }
            this.c = TypedArrayUtils.getNamedInt(typedArray, xmlPullParser, "fillType", 2, 0);
        }

        public void inflate(Resources resources, AttributeSet attributeSet, Resources.Theme theme, XmlPullParser xmlPullParser) {
            if (TypedArrayUtils.hasAttribute(xmlPullParser, "pathData")) {
                TypedArray obtainAttributes = TypedArrayUtils.obtainAttributes(resources, theme, attributeSet, AndroidResources.d);
                updateStateFromTypedArray(obtainAttributes, xmlPullParser);
                obtainAttributes.recycle();
            }
        }

        public boolean isClipPath() {
            return true;
        }
    }

    private static class VFullPath extends VPath {
        ComplexColorCompat e;
        float f = 0.0f;
        ComplexColorCompat g;
        float h = 1.0f;
        float i = 1.0f;
        float j = 0.0f;
        float k = 1.0f;
        float l = 0.0f;
        Paint.Cap m = Paint.Cap.BUTT;
        private int[] mThemeAttrs;
        Paint.Join n = Paint.Join.MITER;
        float o = 4.0f;

        VFullPath() {
        }

        VFullPath(VFullPath vFullPath) {
            super(vFullPath);
            this.mThemeAttrs = vFullPath.mThemeAttrs;
            this.e = vFullPath.e;
            this.f = vFullPath.f;
            this.h = vFullPath.h;
            this.g = vFullPath.g;
            this.c = vFullPath.c;
            this.i = vFullPath.i;
            this.j = vFullPath.j;
            this.k = vFullPath.k;
            this.l = vFullPath.l;
            this.m = vFullPath.m;
            this.n = vFullPath.n;
            this.o = vFullPath.o;
        }

        private Paint.Cap getStrokeLineCap(int i2, Paint.Cap cap) {
            return i2 != 0 ? i2 != 1 ? i2 != 2 ? cap : Paint.Cap.SQUARE : Paint.Cap.ROUND : Paint.Cap.BUTT;
        }

        private Paint.Join getStrokeLineJoin(int i2, Paint.Join join) {
            return i2 != 0 ? i2 != 1 ? i2 != 2 ? join : Paint.Join.BEVEL : Paint.Join.ROUND : Paint.Join.MITER;
        }

        private void updateStateFromTypedArray(TypedArray typedArray, XmlPullParser xmlPullParser, Resources.Theme theme) {
            this.mThemeAttrs = null;
            if (TypedArrayUtils.hasAttribute(xmlPullParser, "pathData")) {
                String string = typedArray.getString(0);
                if (string != null) {
                    this.b = string;
                }
                String string2 = typedArray.getString(2);
                if (string2 != null) {
                    this.a = PathParser.createNodesFromPathData(string2);
                }
                Resources.Theme theme2 = theme;
                this.g = TypedArrayUtils.getNamedComplexColor(typedArray, xmlPullParser, theme2, "fillColor", 1, 0);
                this.i = TypedArrayUtils.getNamedFloat(typedArray, xmlPullParser, "fillAlpha", 12, this.i);
                this.m = getStrokeLineCap(TypedArrayUtils.getNamedInt(typedArray, xmlPullParser, "strokeLineCap", 8, -1), this.m);
                this.n = getStrokeLineJoin(TypedArrayUtils.getNamedInt(typedArray, xmlPullParser, "strokeLineJoin", 9, -1), this.n);
                this.o = TypedArrayUtils.getNamedFloat(typedArray, xmlPullParser, "strokeMiterLimit", 10, this.o);
                this.e = TypedArrayUtils.getNamedComplexColor(typedArray, xmlPullParser, theme2, "strokeColor", 3, 0);
                this.h = TypedArrayUtils.getNamedFloat(typedArray, xmlPullParser, "strokeAlpha", 11, this.h);
                this.f = TypedArrayUtils.getNamedFloat(typedArray, xmlPullParser, "strokeWidth", 4, this.f);
                this.k = TypedArrayUtils.getNamedFloat(typedArray, xmlPullParser, "trimPathEnd", 6, this.k);
                this.l = TypedArrayUtils.getNamedFloat(typedArray, xmlPullParser, "trimPathOffset", 7, this.l);
                this.j = TypedArrayUtils.getNamedFloat(typedArray, xmlPullParser, "trimPathStart", 5, this.j);
                this.c = TypedArrayUtils.getNamedInt(typedArray, xmlPullParser, "fillType", 13, this.c);
            }
        }

        public void applyTheme(Resources.Theme theme) {
            int[] iArr = this.mThemeAttrs;
        }

        public boolean canApplyTheme() {
            return this.mThemeAttrs != null;
        }

        /* access modifiers changed from: package-private */
        public float getFillAlpha() {
            return this.i;
        }

        /* access modifiers changed from: package-private */
        @ColorInt
        public int getFillColor() {
            return this.g.getColor();
        }

        /* access modifiers changed from: package-private */
        public float getStrokeAlpha() {
            return this.h;
        }

        /* access modifiers changed from: package-private */
        @ColorInt
        public int getStrokeColor() {
            return this.e.getColor();
        }

        /* access modifiers changed from: package-private */
        public float getStrokeWidth() {
            return this.f;
        }

        /* access modifiers changed from: package-private */
        public float getTrimPathEnd() {
            return this.k;
        }

        /* access modifiers changed from: package-private */
        public float getTrimPathOffset() {
            return this.l;
        }

        /* access modifiers changed from: package-private */
        public float getTrimPathStart() {
            return this.j;
        }

        public void inflate(Resources resources, AttributeSet attributeSet, Resources.Theme theme, XmlPullParser xmlPullParser) {
            TypedArray obtainAttributes = TypedArrayUtils.obtainAttributes(resources, theme, attributeSet, AndroidResources.c);
            updateStateFromTypedArray(obtainAttributes, xmlPullParser, theme);
            obtainAttributes.recycle();
        }

        public boolean isStateful() {
            return this.g.isStateful() || this.e.isStateful();
        }

        public boolean onStateChanged(int[] iArr) {
            return this.e.onStateChanged(iArr) | this.g.onStateChanged(iArr);
        }

        /* access modifiers changed from: package-private */
        public void setFillAlpha(float f2) {
            this.i = f2;
        }

        /* access modifiers changed from: package-private */
        public void setFillColor(int i2) {
            this.g.setColor(i2);
        }

        /* access modifiers changed from: package-private */
        public void setStrokeAlpha(float f2) {
            this.h = f2;
        }

        /* access modifiers changed from: package-private */
        public void setStrokeColor(int i2) {
            this.e.setColor(i2);
        }

        /* access modifiers changed from: package-private */
        public void setStrokeWidth(float f2) {
            this.f = f2;
        }

        /* access modifiers changed from: package-private */
        public void setTrimPathEnd(float f2) {
            this.k = f2;
        }

        /* access modifiers changed from: package-private */
        public void setTrimPathOffset(float f2) {
            this.l = f2;
        }

        /* access modifiers changed from: package-private */
        public void setTrimPathStart(float f2) {
            this.j = f2;
        }
    }

    private static class VGroup extends VObject {
        final Matrix a = new Matrix();
        final ArrayList<VObject> b = new ArrayList<>();
        float c = 0.0f;
        final Matrix d = new Matrix();
        int e;
        private String mGroupName = null;
        private float mPivotX = 0.0f;
        private float mPivotY = 0.0f;
        private float mScaleX = 1.0f;
        private float mScaleY = 1.0f;
        private int[] mThemeAttrs;
        private float mTranslateX = 0.0f;
        private float mTranslateY = 0.0f;

        public VGroup() {
            super();
        }

        public VGroup(VGroup vGroup, ArrayMap<String, Object> arrayMap) {
            super();
            VPath vPath;
            this.c = vGroup.c;
            this.mPivotX = vGroup.mPivotX;
            this.mPivotY = vGroup.mPivotY;
            this.mScaleX = vGroup.mScaleX;
            this.mScaleY = vGroup.mScaleY;
            this.mTranslateX = vGroup.mTranslateX;
            this.mTranslateY = vGroup.mTranslateY;
            this.mThemeAttrs = vGroup.mThemeAttrs;
            this.mGroupName = vGroup.mGroupName;
            this.e = vGroup.e;
            String str = this.mGroupName;
            if (str != null) {
                arrayMap.put(str, this);
            }
            this.d.set(vGroup.d);
            ArrayList<VObject> arrayList = vGroup.b;
            for (int i = 0; i < arrayList.size(); i++) {
                VObject vObject = arrayList.get(i);
                if (vObject instanceof VGroup) {
                    this.b.add(new VGroup((VGroup) vObject, arrayMap));
                } else {
                    if (vObject instanceof VFullPath) {
                        vPath = new VFullPath((VFullPath) vObject);
                    } else if (vObject instanceof VClipPath) {
                        vPath = new VClipPath((VClipPath) vObject);
                    } else {
                        throw new IllegalStateException("Unknown object in the tree!");
                    }
                    this.b.add(vPath);
                    String str2 = vPath.b;
                    if (str2 != null) {
                        arrayMap.put(str2, vPath);
                    }
                }
            }
        }

        private void updateLocalMatrix() {
            this.d.reset();
            this.d.postTranslate(-this.mPivotX, -this.mPivotY);
            this.d.postScale(this.mScaleX, this.mScaleY);
            this.d.postRotate(this.c, 0.0f, 0.0f);
            this.d.postTranslate(this.mTranslateX + this.mPivotX, this.mTranslateY + this.mPivotY);
        }

        private void updateStateFromTypedArray(TypedArray typedArray, XmlPullParser xmlPullParser) {
            this.mThemeAttrs = null;
            this.c = TypedArrayUtils.getNamedFloat(typedArray, xmlPullParser, "rotation", 5, this.c);
            this.mPivotX = typedArray.getFloat(1, this.mPivotX);
            this.mPivotY = typedArray.getFloat(2, this.mPivotY);
            this.mScaleX = TypedArrayUtils.getNamedFloat(typedArray, xmlPullParser, "scaleX", 3, this.mScaleX);
            this.mScaleY = TypedArrayUtils.getNamedFloat(typedArray, xmlPullParser, "scaleY", 4, this.mScaleY);
            this.mTranslateX = TypedArrayUtils.getNamedFloat(typedArray, xmlPullParser, "translateX", 6, this.mTranslateX);
            this.mTranslateY = TypedArrayUtils.getNamedFloat(typedArray, xmlPullParser, "translateY", 7, this.mTranslateY);
            String string = typedArray.getString(0);
            if (string != null) {
                this.mGroupName = string;
            }
            updateLocalMatrix();
        }

        public String getGroupName() {
            return this.mGroupName;
        }

        public Matrix getLocalMatrix() {
            return this.d;
        }

        public float getPivotX() {
            return this.mPivotX;
        }

        public float getPivotY() {
            return this.mPivotY;
        }

        public float getRotation() {
            return this.c;
        }

        public float getScaleX() {
            return this.mScaleX;
        }

        public float getScaleY() {
            return this.mScaleY;
        }

        public float getTranslateX() {
            return this.mTranslateX;
        }

        public float getTranslateY() {
            return this.mTranslateY;
        }

        public void inflate(Resources resources, AttributeSet attributeSet, Resources.Theme theme, XmlPullParser xmlPullParser) {
            TypedArray obtainAttributes = TypedArrayUtils.obtainAttributes(resources, theme, attributeSet, AndroidResources.b);
            updateStateFromTypedArray(obtainAttributes, xmlPullParser);
            obtainAttributes.recycle();
        }

        public boolean isStateful() {
            for (int i = 0; i < this.b.size(); i++) {
                if (this.b.get(i).isStateful()) {
                    return true;
                }
            }
            return false;
        }

        public boolean onStateChanged(int[] iArr) {
            boolean z = false;
            for (int i = 0; i < this.b.size(); i++) {
                z |= this.b.get(i).onStateChanged(iArr);
            }
            return z;
        }

        public void setPivotX(float f) {
            if (f != this.mPivotX) {
                this.mPivotX = f;
                updateLocalMatrix();
            }
        }

        public void setPivotY(float f) {
            if (f != this.mPivotY) {
                this.mPivotY = f;
                updateLocalMatrix();
            }
        }

        public void setRotation(float f) {
            if (f != this.c) {
                this.c = f;
                updateLocalMatrix();
            }
        }

        public void setScaleX(float f) {
            if (f != this.mScaleX) {
                this.mScaleX = f;
                updateLocalMatrix();
            }
        }

        public void setScaleY(float f) {
            if (f != this.mScaleY) {
                this.mScaleY = f;
                updateLocalMatrix();
            }
        }

        public void setTranslateX(float f) {
            if (f != this.mTranslateX) {
                this.mTranslateX = f;
                updateLocalMatrix();
            }
        }

        public void setTranslateY(float f) {
            if (f != this.mTranslateY) {
                this.mTranslateY = f;
                updateLocalMatrix();
            }
        }
    }

    private static abstract class VObject {
        private VObject() {
        }

        public boolean isStateful() {
            return false;
        }

        public boolean onStateChanged(int[] iArr) {
            return false;
        }
    }

    private static abstract class VPath extends VObject {
        protected PathParser.PathDataNode[] a = null;
        String b;
        int c = 0;
        int d;

        public VPath() {
            super();
        }

        public VPath(VPath vPath) {
            super();
            this.b = vPath.b;
            this.d = vPath.d;
            this.a = PathParser.deepCopyNodes(vPath.a);
        }

        public void applyTheme(Resources.Theme theme) {
        }

        public boolean canApplyTheme() {
            return false;
        }

        public PathParser.PathDataNode[] getPathData() {
            return this.a;
        }

        public String getPathName() {
            return this.b;
        }

        public boolean isClipPath() {
            return false;
        }

        public String nodesToString(PathParser.PathDataNode[] pathDataNodeArr) {
            String str = " ";
            int i = 0;
            while (i < pathDataNodeArr.length) {
                float[] fArr = pathDataNodeArr[i].mParams;
                String str2 = str + pathDataNodeArr[i].mType + ":";
                for (int i2 = 0; i2 < fArr.length; i2++) {
                    str2 = str2 + fArr[i2] + ",";
                }
                i++;
                str = str2;
            }
            return str;
        }

        public void printVPath(int i) {
            String str = "";
            for (int i2 = 0; i2 < i; i2++) {
                str = str + "    ";
            }
            Log.v("VectorDrawableCompat", str + "current path is :" + this.b + " pathData is " + nodesToString(this.a));
        }

        public void setPathData(PathParser.PathDataNode[] pathDataNodeArr) {
            if (!PathParser.canMorph(this.a, pathDataNodeArr)) {
                this.a = PathParser.deepCopyNodes(pathDataNodeArr);
            } else {
                PathParser.updateNodes(this.a, pathDataNodeArr);
            }
        }

        public void toPath(Path path) {
            path.reset();
            PathParser.PathDataNode[] pathDataNodeArr = this.a;
            if (pathDataNodeArr != null) {
                PathParser.PathDataNode.nodesToPath(pathDataNodeArr, path);
            }
        }
    }

    private static class VPathRenderer {
        private static final Matrix IDENTITY_MATRIX = new Matrix();
        Paint a;
        Paint b;
        final VGroup c;
        float d;
        float e;
        float f;
        float g;
        int h;
        String i;
        Boolean j;
        final ArrayMap<String, Object> k;
        private int mChangingConfigurations;
        private final Matrix mFinalPathMatrix;
        private final Path mPath;
        private PathMeasure mPathMeasure;
        private final Path mRenderPath;

        public VPathRenderer() {
            this.mFinalPathMatrix = new Matrix();
            this.d = 0.0f;
            this.e = 0.0f;
            this.f = 0.0f;
            this.g = 0.0f;
            this.h = 255;
            this.i = null;
            this.j = null;
            this.k = new ArrayMap<>();
            this.c = new VGroup();
            this.mPath = new Path();
            this.mRenderPath = new Path();
        }

        public VPathRenderer(VPathRenderer vPathRenderer) {
            this.mFinalPathMatrix = new Matrix();
            this.d = 0.0f;
            this.e = 0.0f;
            this.f = 0.0f;
            this.g = 0.0f;
            this.h = 255;
            this.i = null;
            this.j = null;
            this.k = new ArrayMap<>();
            this.c = new VGroup(vPathRenderer.c, this.k);
            this.mPath = new Path(vPathRenderer.mPath);
            this.mRenderPath = new Path(vPathRenderer.mRenderPath);
            this.d = vPathRenderer.d;
            this.e = vPathRenderer.e;
            this.f = vPathRenderer.f;
            this.g = vPathRenderer.g;
            this.mChangingConfigurations = vPathRenderer.mChangingConfigurations;
            this.h = vPathRenderer.h;
            this.i = vPathRenderer.i;
            String str = vPathRenderer.i;
            if (str != null) {
                this.k.put(str, this);
            }
            this.j = vPathRenderer.j;
        }

        private static float cross(float f2, float f3, float f4, float f5) {
            return (f2 * f5) - (f3 * f4);
        }

        private void drawGroupTree(VGroup vGroup, Matrix matrix, Canvas canvas, int i2, int i3, ColorFilter colorFilter) {
            vGroup.a.set(matrix);
            vGroup.a.preConcat(vGroup.d);
            canvas.save();
            for (int i4 = 0; i4 < vGroup.b.size(); i4++) {
                VObject vObject = vGroup.b.get(i4);
                if (vObject instanceof VGroup) {
                    drawGroupTree((VGroup) vObject, vGroup.a, canvas, i2, i3, colorFilter);
                } else if (vObject instanceof VPath) {
                    drawPath(vGroup, (VPath) vObject, canvas, i2, i3, colorFilter);
                }
            }
            canvas.restore();
        }

        private void drawPath(VGroup vGroup, VPath vPath, Canvas canvas, int i2, int i3, ColorFilter colorFilter) {
            float f2 = ((float) i2) / this.f;
            float f3 = ((float) i3) / this.g;
            float min = Math.min(f2, f3);
            Matrix matrix = vGroup.a;
            this.mFinalPathMatrix.set(matrix);
            this.mFinalPathMatrix.postScale(f2, f3);
            float matrixScale = getMatrixScale(matrix);
            if (matrixScale != 0.0f) {
                vPath.toPath(this.mPath);
                Path path = this.mPath;
                this.mRenderPath.reset();
                if (vPath.isClipPath()) {
                    this.mRenderPath.setFillType(vPath.c == 0 ? Path.FillType.WINDING : Path.FillType.EVEN_ODD);
                    this.mRenderPath.addPath(path, this.mFinalPathMatrix);
                    canvas.clipPath(this.mRenderPath);
                    return;
                }
                VFullPath vFullPath = (VFullPath) vPath;
                if (!(vFullPath.j == 0.0f && vFullPath.k == 1.0f)) {
                    float f4 = vFullPath.j;
                    float f5 = vFullPath.l;
                    float f6 = (f4 + f5) % 1.0f;
                    float f7 = (vFullPath.k + f5) % 1.0f;
                    if (this.mPathMeasure == null) {
                        this.mPathMeasure = new PathMeasure();
                    }
                    this.mPathMeasure.setPath(this.mPath, false);
                    float length = this.mPathMeasure.getLength();
                    float f8 = f6 * length;
                    float f9 = f7 * length;
                    path.reset();
                    if (f8 > f9) {
                        this.mPathMeasure.getSegment(f8, length, path, true);
                        this.mPathMeasure.getSegment(0.0f, f9, path, true);
                    } else {
                        this.mPathMeasure.getSegment(f8, f9, path, true);
                    }
                    path.rLineTo(0.0f, 0.0f);
                }
                this.mRenderPath.addPath(path, this.mFinalPathMatrix);
                if (vFullPath.g.willDraw()) {
                    ComplexColorCompat complexColorCompat = vFullPath.g;
                    if (this.b == null) {
                        this.b = new Paint(1);
                        this.b.setStyle(Paint.Style.FILL);
                    }
                    Paint paint = this.b;
                    if (complexColorCompat.isGradient()) {
                        Shader shader = complexColorCompat.getShader();
                        shader.setLocalMatrix(this.mFinalPathMatrix);
                        paint.setShader(shader);
                        paint.setAlpha(Math.round(vFullPath.i * 255.0f));
                    } else {
                        paint.setShader((Shader) null);
                        paint.setAlpha(255);
                        paint.setColor(VectorDrawableCompat.a(complexColorCompat.getColor(), vFullPath.i));
                    }
                    paint.setColorFilter(colorFilter);
                    this.mRenderPath.setFillType(vFullPath.c == 0 ? Path.FillType.WINDING : Path.FillType.EVEN_ODD);
                    canvas.drawPath(this.mRenderPath, paint);
                }
                if (vFullPath.e.willDraw()) {
                    ComplexColorCompat complexColorCompat2 = vFullPath.e;
                    if (this.a == null) {
                        this.a = new Paint(1);
                        this.a.setStyle(Paint.Style.STROKE);
                    }
                    Paint paint2 = this.a;
                    Paint.Join join = vFullPath.n;
                    if (join != null) {
                        paint2.setStrokeJoin(join);
                    }
                    Paint.Cap cap = vFullPath.m;
                    if (cap != null) {
                        paint2.setStrokeCap(cap);
                    }
                    paint2.setStrokeMiter(vFullPath.o);
                    if (complexColorCompat2.isGradient()) {
                        Shader shader2 = complexColorCompat2.getShader();
                        shader2.setLocalMatrix(this.mFinalPathMatrix);
                        paint2.setShader(shader2);
                        paint2.setAlpha(Math.round(vFullPath.h * 255.0f));
                    } else {
                        paint2.setShader((Shader) null);
                        paint2.setAlpha(255);
                        paint2.setColor(VectorDrawableCompat.a(complexColorCompat2.getColor(), vFullPath.h));
                    }
                    paint2.setColorFilter(colorFilter);
                    paint2.setStrokeWidth(vFullPath.f * min * matrixScale);
                    canvas.drawPath(this.mRenderPath, paint2);
                }
            }
        }

        private float getMatrixScale(Matrix matrix) {
            float[] fArr = {0.0f, 1.0f, 1.0f, 0.0f};
            matrix.mapVectors(fArr);
            float cross = cross(fArr[0], fArr[1], fArr[2], fArr[3]);
            float max = Math.max((float) Math.hypot((double) fArr[0], (double) fArr[1]), (float) Math.hypot((double) fArr[2], (double) fArr[3]));
            if (max > 0.0f) {
                return Math.abs(cross) / max;
            }
            return 0.0f;
        }

        public void draw(Canvas canvas, int i2, int i3, ColorFilter colorFilter) {
            drawGroupTree(this.c, IDENTITY_MATRIX, canvas, i2, i3, colorFilter);
        }

        public float getAlpha() {
            return ((float) getRootAlpha()) / 255.0f;
        }

        public int getRootAlpha() {
            return this.h;
        }

        public boolean isStateful() {
            if (this.j == null) {
                this.j = Boolean.valueOf(this.c.isStateful());
            }
            return this.j.booleanValue();
        }

        public boolean onStateChanged(int[] iArr) {
            return this.c.onStateChanged(iArr);
        }

        public void setAlpha(float f2) {
            setRootAlpha((int) (f2 * 255.0f));
        }

        public void setRootAlpha(int i2) {
            this.h = i2;
        }
    }

    private static class VectorDrawableCompatState extends Drawable.ConstantState {
        int a;
        VPathRenderer b;
        ColorStateList c;
        PorterDuff.Mode d;
        boolean e;
        Bitmap f;
        ColorStateList g;
        PorterDuff.Mode h;
        int i;
        boolean j;
        boolean k;
        Paint l;

        public VectorDrawableCompatState() {
            this.c = null;
            this.d = VectorDrawableCompat.b;
            this.b = new VPathRenderer();
        }

        public VectorDrawableCompatState(VectorDrawableCompatState vectorDrawableCompatState) {
            this.c = null;
            this.d = VectorDrawableCompat.b;
            if (vectorDrawableCompatState != null) {
                this.a = vectorDrawableCompatState.a;
                this.b = new VPathRenderer(vectorDrawableCompatState.b);
                Paint paint = vectorDrawableCompatState.b.b;
                if (paint != null) {
                    this.b.b = new Paint(paint);
                }
                Paint paint2 = vectorDrawableCompatState.b.a;
                if (paint2 != null) {
                    this.b.a = new Paint(paint2);
                }
                this.c = vectorDrawableCompatState.c;
                this.d = vectorDrawableCompatState.d;
                this.e = vectorDrawableCompatState.e;
            }
        }

        public boolean canReuseBitmap(int i2, int i3) {
            return i2 == this.f.getWidth() && i3 == this.f.getHeight();
        }

        public boolean canReuseCache() {
            return !this.k && this.g == this.c && this.h == this.d && this.j == this.e && this.i == this.b.getRootAlpha();
        }

        public void createCachedBitmapIfNeeded(int i2, int i3) {
            if (this.f == null || !canReuseBitmap(i2, i3)) {
                this.f = Bitmap.createBitmap(i2, i3, Bitmap.Config.ARGB_8888);
                this.k = true;
            }
        }

        public void drawCachedBitmapWithRootAlpha(Canvas canvas, ColorFilter colorFilter, Rect rect) {
            canvas.drawBitmap(this.f, (Rect) null, rect, getPaint(colorFilter));
        }

        public int getChangingConfigurations() {
            return this.a;
        }

        public Paint getPaint(ColorFilter colorFilter) {
            if (!hasTranslucentRoot() && colorFilter == null) {
                return null;
            }
            if (this.l == null) {
                this.l = new Paint();
                this.l.setFilterBitmap(true);
            }
            this.l.setAlpha(this.b.getRootAlpha());
            this.l.setColorFilter(colorFilter);
            return this.l;
        }

        public boolean hasTranslucentRoot() {
            return this.b.getRootAlpha() < 255;
        }

        public boolean isStateful() {
            return this.b.isStateful();
        }

        @NonNull
        public Drawable newDrawable() {
            return new VectorDrawableCompat(this);
        }

        @NonNull
        public Drawable newDrawable(Resources resources) {
            return new VectorDrawableCompat(this);
        }

        public boolean onStateChanged(int[] iArr) {
            boolean onStateChanged = this.b.onStateChanged(iArr);
            this.k |= onStateChanged;
            return onStateChanged;
        }

        public void updateCacheStates() {
            this.g = this.c;
            this.h = this.d;
            this.i = this.b.getRootAlpha();
            this.j = this.e;
            this.k = false;
        }

        public void updateCachedBitmap(int i2, int i3) {
            this.f.eraseColor(0);
            this.b.draw(new Canvas(this.f), i2, i3, (ColorFilter) null);
        }
    }

    @RequiresApi(24)
    private static class VectorDrawableDelegateState extends Drawable.ConstantState {
        private final Drawable.ConstantState mDelegateState;

        public VectorDrawableDelegateState(Drawable.ConstantState constantState) {
            this.mDelegateState = constantState;
        }

        public boolean canApplyTheme() {
            return this.mDelegateState.canApplyTheme();
        }

        public int getChangingConfigurations() {
            return this.mDelegateState.getChangingConfigurations();
        }

        public Drawable newDrawable() {
            VectorDrawableCompat vectorDrawableCompat = new VectorDrawableCompat();
            vectorDrawableCompat.a = (VectorDrawable) this.mDelegateState.newDrawable();
            return vectorDrawableCompat;
        }

        public Drawable newDrawable(Resources resources) {
            VectorDrawableCompat vectorDrawableCompat = new VectorDrawableCompat();
            vectorDrawableCompat.a = (VectorDrawable) this.mDelegateState.newDrawable(resources);
            return vectorDrawableCompat;
        }

        public Drawable newDrawable(Resources resources, Resources.Theme theme) {
            VectorDrawableCompat vectorDrawableCompat = new VectorDrawableCompat();
            vectorDrawableCompat.a = (VectorDrawable) this.mDelegateState.newDrawable(resources, theme);
            return vectorDrawableCompat;
        }
    }

    VectorDrawableCompat() {
        this.mAllowCaching = true;
        this.mTmpFloats = new float[9];
        this.mTmpMatrix = new Matrix();
        this.mTmpBounds = new Rect();
        this.mVectorState = new VectorDrawableCompatState();
    }

    VectorDrawableCompat(@NonNull VectorDrawableCompatState vectorDrawableCompatState) {
        this.mAllowCaching = true;
        this.mTmpFloats = new float[9];
        this.mTmpMatrix = new Matrix();
        this.mTmpBounds = new Rect();
        this.mVectorState = vectorDrawableCompatState;
        this.mTintFilter = a(this.mTintFilter, vectorDrawableCompatState.c, vectorDrawableCompatState.d);
    }

    static int a(int i, float f) {
        return (i & 16777215) | (((int) (((float) Color.alpha(i)) * f)) << 24);
    }

    /* JADX WARNING: Removed duplicated region for block: B:12:0x0038 A[Catch:{ IOException | XmlPullParserException -> 0x0045 }] */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x003d A[Catch:{ IOException | XmlPullParserException -> 0x0045 }] */
    @androidx.annotation.Nullable
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static androidx.vectordrawable.graphics.drawable.VectorDrawableCompat create(@androidx.annotation.NonNull android.content.res.Resources r6, @androidx.annotation.DrawableRes int r7, @androidx.annotation.Nullable android.content.res.Resources.Theme r8) {
        /*
            java.lang.String r0 = "parser error"
            java.lang.String r1 = "VectorDrawableCompat"
            int r2 = android.os.Build.VERSION.SDK_INT
            r3 = 24
            if (r2 < r3) goto L_0x0023
            androidx.vectordrawable.graphics.drawable.VectorDrawableCompat r0 = new androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
            r0.<init>()
            android.graphics.drawable.Drawable r6 = androidx.core.content.res.ResourcesCompat.getDrawable(r6, r7, r8)
            r0.a = r6
            androidx.vectordrawable.graphics.drawable.VectorDrawableCompat$VectorDrawableDelegateState r6 = new androidx.vectordrawable.graphics.drawable.VectorDrawableCompat$VectorDrawableDelegateState
            android.graphics.drawable.Drawable r7 = r0.a
            android.graphics.drawable.Drawable$ConstantState r7 = r7.getConstantState()
            r6.<init>(r7)
            r0.mCachedConstantStateDelegate = r6
            return r0
        L_0x0023:
            android.content.res.XmlResourceParser r7 = r6.getXml(r7)     // Catch:{ XmlPullParserException -> 0x0047, IOException -> 0x0045 }
            android.util.AttributeSet r2 = android.util.Xml.asAttributeSet(r7)     // Catch:{ XmlPullParserException -> 0x0047, IOException -> 0x0045 }
        L_0x002b:
            int r3 = r7.next()     // Catch:{ XmlPullParserException -> 0x0047, IOException -> 0x0045 }
            r4 = 2
            if (r3 == r4) goto L_0x0036
            r5 = 1
            if (r3 == r5) goto L_0x0036
            goto L_0x002b
        L_0x0036:
            if (r3 != r4) goto L_0x003d
            androidx.vectordrawable.graphics.drawable.VectorDrawableCompat r6 = createFromXmlInner(r6, r7, r2, r8)     // Catch:{ XmlPullParserException -> 0x0047, IOException -> 0x0045 }
            return r6
        L_0x003d:
            org.xmlpull.v1.XmlPullParserException r6 = new org.xmlpull.v1.XmlPullParserException     // Catch:{ XmlPullParserException -> 0x0047, IOException -> 0x0045 }
            java.lang.String r7 = "No start tag found"
            r6.<init>(r7)     // Catch:{ XmlPullParserException -> 0x0047, IOException -> 0x0045 }
            throw r6     // Catch:{ XmlPullParserException -> 0x0047, IOException -> 0x0045 }
        L_0x0045:
            r6 = move-exception
            goto L_0x0048
        L_0x0047:
            r6 = move-exception
        L_0x0048:
            android.util.Log.e(r1, r0, r6)
            r6 = 0
            return r6
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.vectordrawable.graphics.drawable.VectorDrawableCompat.create(android.content.res.Resources, int, android.content.res.Resources$Theme):androidx.vectordrawable.graphics.drawable.VectorDrawableCompat");
    }

    public static VectorDrawableCompat createFromXmlInner(Resources resources, XmlPullParser xmlPullParser, AttributeSet attributeSet, Resources.Theme theme) {
        VectorDrawableCompat vectorDrawableCompat = new VectorDrawableCompat();
        vectorDrawableCompat.inflate(resources, xmlPullParser, attributeSet, theme);
        return vectorDrawableCompat;
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v9, resolved type: androidx.vectordrawable.graphics.drawable.VectorDrawableCompat$VClipPath} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v15, resolved type: androidx.vectordrawable.graphics.drawable.VectorDrawableCompat$VFullPath} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v16, resolved type: androidx.vectordrawable.graphics.drawable.VectorDrawableCompat$VClipPath} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v17, resolved type: androidx.vectordrawable.graphics.drawable.VectorDrawableCompat$VClipPath} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r3v18, resolved type: androidx.vectordrawable.graphics.drawable.VectorDrawableCompat$VClipPath} */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void inflateInternal(android.content.res.Resources r11, org.xmlpull.v1.XmlPullParser r12, android.util.AttributeSet r13, android.content.res.Resources.Theme r14) {
        /*
            r10 = this;
            androidx.vectordrawable.graphics.drawable.VectorDrawableCompat$VectorDrawableCompatState r0 = r10.mVectorState
            androidx.vectordrawable.graphics.drawable.VectorDrawableCompat$VPathRenderer r1 = r0.b
            java.util.ArrayDeque r2 = new java.util.ArrayDeque
            r2.<init>()
            androidx.vectordrawable.graphics.drawable.VectorDrawableCompat$VGroup r3 = r1.c
            r2.push(r3)
            int r3 = r12.getEventType()
            int r4 = r12.getDepth()
            r5 = 1
            int r4 = r4 + r5
            r6 = 1
        L_0x0019:
            if (r3 == r5) goto L_0x00c4
            int r7 = r12.getDepth()
            r8 = 3
            if (r7 >= r4) goto L_0x0024
            if (r3 == r8) goto L_0x00c4
        L_0x0024:
            r7 = 2
            java.lang.String r9 = "group"
            if (r3 != r7) goto L_0x00af
            java.lang.String r3 = r12.getName()
            java.lang.Object r7 = r2.peek()
            androidx.vectordrawable.graphics.drawable.VectorDrawableCompat$VGroup r7 = (androidx.vectordrawable.graphics.drawable.VectorDrawableCompat.VGroup) r7
            java.lang.String r8 = "path"
            boolean r8 = r8.equals(r3)
            if (r8 == 0) goto L_0x0059
            androidx.vectordrawable.graphics.drawable.VectorDrawableCompat$VFullPath r3 = new androidx.vectordrawable.graphics.drawable.VectorDrawableCompat$VFullPath
            r3.<init>()
            r3.inflate(r11, r13, r14, r12)
            java.util.ArrayList<androidx.vectordrawable.graphics.drawable.VectorDrawableCompat$VObject> r6 = r7.b
            r6.add(r3)
            java.lang.String r6 = r3.getPathName()
            if (r6 == 0) goto L_0x0057
            androidx.collection.ArrayMap<java.lang.String, java.lang.Object> r6 = r1.k
            java.lang.String r7 = r3.getPathName()
            r6.put(r7, r3)
        L_0x0057:
            r6 = 0
            goto L_0x007d
        L_0x0059:
            java.lang.String r8 = "clip-path"
            boolean r8 = r8.equals(r3)
            if (r8 == 0) goto L_0x0085
            androidx.vectordrawable.graphics.drawable.VectorDrawableCompat$VClipPath r3 = new androidx.vectordrawable.graphics.drawable.VectorDrawableCompat$VClipPath
            r3.<init>()
            r3.inflate(r11, r13, r14, r12)
            java.util.ArrayList<androidx.vectordrawable.graphics.drawable.VectorDrawableCompat$VObject> r7 = r7.b
            r7.add(r3)
            java.lang.String r7 = r3.getPathName()
            if (r7 == 0) goto L_0x007d
            androidx.collection.ArrayMap<java.lang.String, java.lang.Object> r7 = r1.k
            java.lang.String r8 = r3.getPathName()
            r7.put(r8, r3)
        L_0x007d:
            int r7 = r0.a
            int r3 = r3.d
        L_0x0081:
            r3 = r3 | r7
            r0.a = r3
            goto L_0x00be
        L_0x0085:
            boolean r3 = r9.equals(r3)
            if (r3 == 0) goto L_0x00be
            androidx.vectordrawable.graphics.drawable.VectorDrawableCompat$VGroup r3 = new androidx.vectordrawable.graphics.drawable.VectorDrawableCompat$VGroup
            r3.<init>()
            r3.inflate(r11, r13, r14, r12)
            java.util.ArrayList<androidx.vectordrawable.graphics.drawable.VectorDrawableCompat$VObject> r7 = r7.b
            r7.add(r3)
            r2.push(r3)
            java.lang.String r7 = r3.getGroupName()
            if (r7 == 0) goto L_0x00aa
            androidx.collection.ArrayMap<java.lang.String, java.lang.Object> r7 = r1.k
            java.lang.String r8 = r3.getGroupName()
            r7.put(r8, r3)
        L_0x00aa:
            int r7 = r0.a
            int r3 = r3.e
            goto L_0x0081
        L_0x00af:
            if (r3 != r8) goto L_0x00be
            java.lang.String r3 = r12.getName()
            boolean r3 = r9.equals(r3)
            if (r3 == 0) goto L_0x00be
            r2.pop()
        L_0x00be:
            int r3 = r12.next()
            goto L_0x0019
        L_0x00c4:
            if (r6 != 0) goto L_0x00c7
            return
        L_0x00c7:
            org.xmlpull.v1.XmlPullParserException r11 = new org.xmlpull.v1.XmlPullParserException
            java.lang.String r12 = "no path defined"
            r11.<init>(r12)
            goto L_0x00d0
        L_0x00cf:
            throw r11
        L_0x00d0:
            goto L_0x00cf
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.vectordrawable.graphics.drawable.VectorDrawableCompat.inflateInternal(android.content.res.Resources, org.xmlpull.v1.XmlPullParser, android.util.AttributeSet, android.content.res.Resources$Theme):void");
    }

    private boolean needMirroring() {
        return Build.VERSION.SDK_INT >= 17 && isAutoMirrored() && DrawableCompat.getLayoutDirection(this) == 1;
    }

    private static PorterDuff.Mode parseTintModeCompat(int i, PorterDuff.Mode mode) {
        if (i == 3) {
            return PorterDuff.Mode.SRC_OVER;
        }
        if (i == 5) {
            return PorterDuff.Mode.SRC_IN;
        }
        if (i == 9) {
            return PorterDuff.Mode.SRC_ATOP;
        }
        switch (i) {
            case 14:
                return PorterDuff.Mode.MULTIPLY;
            case 15:
                return PorterDuff.Mode.SCREEN;
            case 16:
                return PorterDuff.Mode.ADD;
            default:
                return mode;
        }
    }

    private void printGroupTree(VGroup vGroup, int i) {
        String str = "";
        for (int i2 = 0; i2 < i; i2++) {
            str = str + "    ";
        }
        Log.v("VectorDrawableCompat", str + "current group is :" + vGroup.getGroupName() + " rotation is " + vGroup.c);
        StringBuilder sb = new StringBuilder();
        sb.append(str);
        sb.append("matrix is :");
        sb.append(vGroup.getLocalMatrix().toString());
        Log.v("VectorDrawableCompat", sb.toString());
        for (int i3 = 0; i3 < vGroup.b.size(); i3++) {
            VObject vObject = vGroup.b.get(i3);
            if (vObject instanceof VGroup) {
                printGroupTree((VGroup) vObject, i + 1);
            } else {
                ((VPath) vObject).printVPath(i + 1);
            }
        }
    }

    private void updateStateFromTypedArray(TypedArray typedArray, XmlPullParser xmlPullParser, Resources.Theme theme) {
        VectorDrawableCompatState vectorDrawableCompatState = this.mVectorState;
        VPathRenderer vPathRenderer = vectorDrawableCompatState.b;
        vectorDrawableCompatState.d = parseTintModeCompat(TypedArrayUtils.getNamedInt(typedArray, xmlPullParser, "tintMode", 6, -1), PorterDuff.Mode.SRC_IN);
        ColorStateList namedColorStateList = TypedArrayUtils.getNamedColorStateList(typedArray, xmlPullParser, theme, "tint", 1);
        if (namedColorStateList != null) {
            vectorDrawableCompatState.c = namedColorStateList;
        }
        vectorDrawableCompatState.e = TypedArrayUtils.getNamedBoolean(typedArray, xmlPullParser, "autoMirrored", 5, vectorDrawableCompatState.e);
        vPathRenderer.f = TypedArrayUtils.getNamedFloat(typedArray, xmlPullParser, "viewportWidth", 7, vPathRenderer.f);
        vPathRenderer.g = TypedArrayUtils.getNamedFloat(typedArray, xmlPullParser, "viewportHeight", 8, vPathRenderer.g);
        if (vPathRenderer.f <= 0.0f) {
            throw new XmlPullParserException(typedArray.getPositionDescription() + "<vector> tag requires viewportWidth > 0");
        } else if (vPathRenderer.g > 0.0f) {
            vPathRenderer.d = typedArray.getDimension(3, vPathRenderer.d);
            vPathRenderer.e = typedArray.getDimension(2, vPathRenderer.e);
            if (vPathRenderer.d <= 0.0f) {
                throw new XmlPullParserException(typedArray.getPositionDescription() + "<vector> tag requires width > 0");
            } else if (vPathRenderer.e > 0.0f) {
                vPathRenderer.setAlpha(TypedArrayUtils.getNamedFloat(typedArray, xmlPullParser, "alpha", 4, vPathRenderer.getAlpha()));
                String string = typedArray.getString(0);
                if (string != null) {
                    vPathRenderer.i = string;
                    vPathRenderer.k.put(string, vPathRenderer);
                }
            } else {
                throw new XmlPullParserException(typedArray.getPositionDescription() + "<vector> tag requires height > 0");
            }
        } else {
            throw new XmlPullParserException(typedArray.getPositionDescription() + "<vector> tag requires viewportHeight > 0");
        }
    }

    /* access modifiers changed from: package-private */
    public PorterDuffColorFilter a(PorterDuffColorFilter porterDuffColorFilter, ColorStateList colorStateList, PorterDuff.Mode mode) {
        if (colorStateList == null || mode == null) {
            return null;
        }
        return new PorterDuffColorFilter(colorStateList.getColorForState(getState(), 0), mode);
    }

    /* access modifiers changed from: package-private */
    public Object a(String str) {
        return this.mVectorState.b.k.get(str);
    }

    /* access modifiers changed from: package-private */
    public void a(boolean z) {
        this.mAllowCaching = z;
    }

    public /* bridge */ /* synthetic */ void applyTheme(Resources.Theme theme) {
        super.applyTheme(theme);
    }

    public boolean canApplyTheme() {
        Drawable drawable = this.a;
        if (drawable == null) {
            return false;
        }
        DrawableCompat.canApplyTheme(drawable);
        return false;
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
        copyBounds(this.mTmpBounds);
        if (this.mTmpBounds.width() > 0 && this.mTmpBounds.height() > 0) {
            ColorFilter colorFilter = this.mColorFilter;
            if (colorFilter == null) {
                colorFilter = this.mTintFilter;
            }
            canvas.getMatrix(this.mTmpMatrix);
            this.mTmpMatrix.getValues(this.mTmpFloats);
            float abs = Math.abs(this.mTmpFloats[0]);
            float abs2 = Math.abs(this.mTmpFloats[4]);
            float abs3 = Math.abs(this.mTmpFloats[1]);
            float abs4 = Math.abs(this.mTmpFloats[3]);
            if (!(abs3 == 0.0f && abs4 == 0.0f)) {
                abs = 1.0f;
                abs2 = 1.0f;
            }
            int min = Math.min(2048, (int) (((float) this.mTmpBounds.width()) * abs));
            int min2 = Math.min(2048, (int) (((float) this.mTmpBounds.height()) * abs2));
            if (min > 0 && min2 > 0) {
                int save = canvas.save();
                Rect rect = this.mTmpBounds;
                canvas.translate((float) rect.left, (float) rect.top);
                if (needMirroring()) {
                    canvas.translate((float) this.mTmpBounds.width(), 0.0f);
                    canvas.scale(-1.0f, 1.0f);
                }
                this.mTmpBounds.offsetTo(0, 0);
                this.mVectorState.createCachedBitmapIfNeeded(min, min2);
                if (!this.mAllowCaching) {
                    this.mVectorState.updateCachedBitmap(min, min2);
                } else if (!this.mVectorState.canReuseCache()) {
                    this.mVectorState.updateCachedBitmap(min, min2);
                    this.mVectorState.updateCacheStates();
                }
                this.mVectorState.drawCachedBitmapWithRootAlpha(canvas, colorFilter, this.mTmpBounds);
                canvas.restoreToCount(save);
            }
        }
    }

    public int getAlpha() {
        Drawable drawable = this.a;
        return drawable != null ? DrawableCompat.getAlpha(drawable) : this.mVectorState.b.getRootAlpha();
    }

    public int getChangingConfigurations() {
        Drawable drawable = this.a;
        return drawable != null ? drawable.getChangingConfigurations() : super.getChangingConfigurations() | this.mVectorState.getChangingConfigurations();
    }

    public ColorFilter getColorFilter() {
        Drawable drawable = this.a;
        return drawable != null ? DrawableCompat.getColorFilter(drawable) : this.mColorFilter;
    }

    public Drawable.ConstantState getConstantState() {
        Drawable drawable = this.a;
        if (drawable != null && Build.VERSION.SDK_INT >= 24) {
            return new VectorDrawableDelegateState(drawable.getConstantState());
        }
        this.mVectorState.a = getChangingConfigurations();
        return this.mVectorState;
    }

    public /* bridge */ /* synthetic */ Drawable getCurrent() {
        return super.getCurrent();
    }

    public int getIntrinsicHeight() {
        Drawable drawable = this.a;
        return drawable != null ? drawable.getIntrinsicHeight() : (int) this.mVectorState.b.e;
    }

    public int getIntrinsicWidth() {
        Drawable drawable = this.a;
        return drawable != null ? drawable.getIntrinsicWidth() : (int) this.mVectorState.b.d;
    }

    public /* bridge */ /* synthetic */ int getMinimumHeight() {
        return super.getMinimumHeight();
    }

    public /* bridge */ /* synthetic */ int getMinimumWidth() {
        return super.getMinimumWidth();
    }

    public int getOpacity() {
        Drawable drawable = this.a;
        if (drawable != null) {
            return drawable.getOpacity();
        }
        return -3;
    }

    public /* bridge */ /* synthetic */ boolean getPadding(Rect rect) {
        return super.getPadding(rect);
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
    public float getPixelSize() {
        VPathRenderer vPathRenderer;
        VectorDrawableCompatState vectorDrawableCompatState = this.mVectorState;
        if (vectorDrawableCompatState == null || (vPathRenderer = vectorDrawableCompatState.b) == null) {
            return 1.0f;
        }
        float f = vPathRenderer.d;
        if (f == 0.0f) {
            return 1.0f;
        }
        float f2 = vPathRenderer.e;
        if (f2 == 0.0f) {
            return 1.0f;
        }
        float f3 = vPathRenderer.g;
        if (f3 == 0.0f) {
            return 1.0f;
        }
        float f4 = vPathRenderer.f;
        if (f4 == 0.0f) {
            return 1.0f;
        }
        return Math.min(f4 / f, f3 / f2);
    }

    public /* bridge */ /* synthetic */ int[] getState() {
        return super.getState();
    }

    public /* bridge */ /* synthetic */ Region getTransparentRegion() {
        return super.getTransparentRegion();
    }

    public void inflate(Resources resources, XmlPullParser xmlPullParser, AttributeSet attributeSet) {
        Drawable drawable = this.a;
        if (drawable != null) {
            drawable.inflate(resources, xmlPullParser, attributeSet);
        } else {
            inflate(resources, xmlPullParser, attributeSet, (Resources.Theme) null);
        }
    }

    public void inflate(Resources resources, XmlPullParser xmlPullParser, AttributeSet attributeSet, Resources.Theme theme) {
        Drawable drawable = this.a;
        if (drawable != null) {
            DrawableCompat.inflate(drawable, resources, xmlPullParser, attributeSet, theme);
            return;
        }
        VectorDrawableCompatState vectorDrawableCompatState = this.mVectorState;
        vectorDrawableCompatState.b = new VPathRenderer();
        TypedArray obtainAttributes = TypedArrayUtils.obtainAttributes(resources, theme, attributeSet, AndroidResources.a);
        updateStateFromTypedArray(obtainAttributes, xmlPullParser, theme);
        obtainAttributes.recycle();
        vectorDrawableCompatState.a = getChangingConfigurations();
        vectorDrawableCompatState.k = true;
        inflateInternal(resources, xmlPullParser, attributeSet, theme);
        this.mTintFilter = a(this.mTintFilter, vectorDrawableCompatState.c, vectorDrawableCompatState.d);
    }

    public void invalidateSelf() {
        Drawable drawable = this.a;
        if (drawable != null) {
            drawable.invalidateSelf();
        } else {
            super.invalidateSelf();
        }
    }

    public boolean isAutoMirrored() {
        Drawable drawable = this.a;
        return drawable != null ? DrawableCompat.isAutoMirrored(drawable) : this.mVectorState.e;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x0019, code lost:
        r0 = r1.mVectorState.c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x000f, code lost:
        r0 = r1.mVectorState;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isStateful() {
        /*
            r1 = this;
            android.graphics.drawable.Drawable r0 = r1.a
            if (r0 == 0) goto L_0x0009
            boolean r0 = r0.isStateful()
            return r0
        L_0x0009:
            boolean r0 = super.isStateful()
            if (r0 != 0) goto L_0x0028
            androidx.vectordrawable.graphics.drawable.VectorDrawableCompat$VectorDrawableCompatState r0 = r1.mVectorState
            if (r0 == 0) goto L_0x0026
            boolean r0 = r0.isStateful()
            if (r0 != 0) goto L_0x0028
            androidx.vectordrawable.graphics.drawable.VectorDrawableCompat$VectorDrawableCompatState r0 = r1.mVectorState
            android.content.res.ColorStateList r0 = r0.c
            if (r0 == 0) goto L_0x0026
            boolean r0 = r0.isStateful()
            if (r0 == 0) goto L_0x0026
            goto L_0x0028
        L_0x0026:
            r0 = 0
            goto L_0x0029
        L_0x0028:
            r0 = 1
        L_0x0029:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.vectordrawable.graphics.drawable.VectorDrawableCompat.isStateful():boolean");
    }

    public /* bridge */ /* synthetic */ void jumpToCurrentState() {
        super.jumpToCurrentState();
    }

    public Drawable mutate() {
        Drawable drawable = this.a;
        if (drawable != null) {
            drawable.mutate();
            return this;
        }
        if (!this.mMutated && super.mutate() == this) {
            this.mVectorState = new VectorDrawableCompatState(this.mVectorState);
            this.mMutated = true;
        }
        return this;
    }

    /* access modifiers changed from: protected */
    public void onBoundsChange(Rect rect) {
        Drawable drawable = this.a;
        if (drawable != null) {
            drawable.setBounds(rect);
        }
    }

    /* access modifiers changed from: protected */
    public boolean onStateChange(int[] iArr) {
        PorterDuff.Mode mode;
        Drawable drawable = this.a;
        if (drawable != null) {
            return drawable.setState(iArr);
        }
        boolean z = false;
        VectorDrawableCompatState vectorDrawableCompatState = this.mVectorState;
        ColorStateList colorStateList = vectorDrawableCompatState.c;
        if (!(colorStateList == null || (mode = vectorDrawableCompatState.d) == null)) {
            this.mTintFilter = a(this.mTintFilter, colorStateList, mode);
            invalidateSelf();
            z = true;
        }
        if (!vectorDrawableCompatState.isStateful() || !vectorDrawableCompatState.onStateChanged(iArr)) {
            return z;
        }
        invalidateSelf();
        return true;
    }

    public void scheduleSelf(Runnable runnable, long j) {
        Drawable drawable = this.a;
        if (drawable != null) {
            drawable.scheduleSelf(runnable, j);
        } else {
            super.scheduleSelf(runnable, j);
        }
    }

    public void setAlpha(int i) {
        Drawable drawable = this.a;
        if (drawable != null) {
            drawable.setAlpha(i);
        } else if (this.mVectorState.b.getRootAlpha() != i) {
            this.mVectorState.b.setRootAlpha(i);
            invalidateSelf();
        }
    }

    public void setAutoMirrored(boolean z) {
        Drawable drawable = this.a;
        if (drawable != null) {
            DrawableCompat.setAutoMirrored(drawable, z);
        } else {
            this.mVectorState.e = z;
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
            return;
        }
        this.mColorFilter = colorFilter;
        invalidateSelf();
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
            setTintList(ColorStateList.valueOf(i));
        }
    }

    public void setTintList(ColorStateList colorStateList) {
        Drawable drawable = this.a;
        if (drawable != null) {
            DrawableCompat.setTintList(drawable, colorStateList);
            return;
        }
        VectorDrawableCompatState vectorDrawableCompatState = this.mVectorState;
        if (vectorDrawableCompatState.c != colorStateList) {
            vectorDrawableCompatState.c = colorStateList;
            this.mTintFilter = a(this.mTintFilter, colorStateList, vectorDrawableCompatState.d);
            invalidateSelf();
        }
    }

    public void setTintMode(PorterDuff.Mode mode) {
        Drawable drawable = this.a;
        if (drawable != null) {
            DrawableCompat.setTintMode(drawable, mode);
            return;
        }
        VectorDrawableCompatState vectorDrawableCompatState = this.mVectorState;
        if (vectorDrawableCompatState.d != mode) {
            vectorDrawableCompatState.d = mode;
            this.mTintFilter = a(this.mTintFilter, vectorDrawableCompatState.c, mode);
            invalidateSelf();
        }
    }

    public boolean setVisible(boolean z, boolean z2) {
        Drawable drawable = this.a;
        return drawable != null ? drawable.setVisible(z, z2) : super.setVisible(z, z2);
    }

    public void unscheduleSelf(Runnable runnable) {
        Drawable drawable = this.a;
        if (drawable != null) {
            drawable.unscheduleSelf(runnable);
        } else {
            super.unscheduleSelf(runnable);
        }
    }
}
