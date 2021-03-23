package androidx.loader.content;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.DebugUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class Loader<D> {
    int a;
    OnLoadCompleteListener<D> b;
    OnLoadCanceledListener<D> c;
    Context d;
    boolean e = false;
    boolean f = false;
    boolean g = true;
    boolean h = false;
    boolean i = false;

    public final class ForceLoadContentObserver extends ContentObserver {
        public ForceLoadContentObserver() {
            super(new Handler());
        }

        public boolean deliverSelfNotifications() {
            return true;
        }

        public void onChange(boolean z) {
            Loader.this.onContentChanged();
        }
    }

    public interface OnLoadCanceledListener<D> {
        void onLoadCanceled(@NonNull Loader<D> loader);
    }

    public interface OnLoadCompleteListener<D> {
        void onLoadComplete(@NonNull Loader<D> loader, @Nullable D d);
    }

    public Loader(@NonNull Context context) {
        this.d = context.getApplicationContext();
    }

    /* access modifiers changed from: protected */
    @MainThread
    public void a() {
    }

    @MainThread
    public void abandon() {
        this.f = true;
        a();
    }

    /* access modifiers changed from: protected */
    @MainThread
    public boolean b() {
        return false;
    }

    /* access modifiers changed from: protected */
    @MainThread
    public void c() {
    }

    @MainThread
    public boolean cancelLoad() {
        return b();
    }

    public void commitContentChanged() {
        this.i = false;
    }

    /* access modifiers changed from: protected */
    @MainThread
    public void d() {
    }

    @NonNull
    public String dataToString(@Nullable D d2) {
        StringBuilder sb = new StringBuilder(64);
        DebugUtils.buildShortClassTag(d2, sb);
        sb.append("}");
        return sb.toString();
    }

    @MainThread
    public void deliverCancellation() {
        OnLoadCanceledListener<D> onLoadCanceledListener = this.c;
        if (onLoadCanceledListener != null) {
            onLoadCanceledListener.onLoadCanceled(this);
        }
    }

    @MainThread
    public void deliverResult(@Nullable D d2) {
        OnLoadCompleteListener<D> onLoadCompleteListener = this.b;
        if (onLoadCompleteListener != null) {
            onLoadCompleteListener.onLoadComplete(this, d2);
        }
    }

    @Deprecated
    public void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        printWriter.print(str);
        printWriter.print("mId=");
        printWriter.print(this.a);
        printWriter.print(" mListener=");
        printWriter.println(this.b);
        if (this.e || this.h || this.i) {
            printWriter.print(str);
            printWriter.print("mStarted=");
            printWriter.print(this.e);
            printWriter.print(" mContentChanged=");
            printWriter.print(this.h);
            printWriter.print(" mProcessingChange=");
            printWriter.println(this.i);
        }
        if (this.f || this.g) {
            printWriter.print(str);
            printWriter.print("mAbandoned=");
            printWriter.print(this.f);
            printWriter.print(" mReset=");
            printWriter.println(this.g);
        }
    }

    /* access modifiers changed from: protected */
    @MainThread
    public void e() {
    }

    /* access modifiers changed from: protected */
    @MainThread
    public void f() {
    }

    @MainThread
    public void forceLoad() {
        c();
    }

    @NonNull
    public Context getContext() {
        return this.d;
    }

    public int getId() {
        return this.a;
    }

    public boolean isAbandoned() {
        return this.f;
    }

    public boolean isReset() {
        return this.g;
    }

    public boolean isStarted() {
        return this.e;
    }

    @MainThread
    public void onContentChanged() {
        if (this.e) {
            forceLoad();
        } else {
            this.h = true;
        }
    }

    @MainThread
    public void registerListener(int i2, @NonNull OnLoadCompleteListener<D> onLoadCompleteListener) {
        if (this.b == null) {
            this.b = onLoadCompleteListener;
            this.a = i2;
            return;
        }
        throw new IllegalStateException("There is already a listener registered");
    }

    @MainThread
    public void registerOnLoadCanceledListener(@NonNull OnLoadCanceledListener<D> onLoadCanceledListener) {
        if (this.c == null) {
            this.c = onLoadCanceledListener;
            return;
        }
        throw new IllegalStateException("There is already a listener registered");
    }

    @MainThread
    public void reset() {
        d();
        this.g = true;
        this.e = false;
        this.f = false;
        this.h = false;
        this.i = false;
    }

    public void rollbackContentChanged() {
        if (this.i) {
            onContentChanged();
        }
    }

    @MainThread
    public final void startLoading() {
        this.e = true;
        this.g = false;
        this.f = false;
        e();
    }

    @MainThread
    public void stopLoading() {
        this.e = false;
        f();
    }

    public boolean takeContentChanged() {
        boolean z = this.h;
        this.h = false;
        this.i |= z;
        return z;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(64);
        DebugUtils.buildShortClassTag(this, sb);
        sb.append(" id=");
        sb.append(this.a);
        sb.append("}");
        return sb.toString();
    }

    @MainThread
    public void unregisterListener(@NonNull OnLoadCompleteListener<D> onLoadCompleteListener) {
        OnLoadCompleteListener<D> onLoadCompleteListener2 = this.b;
        if (onLoadCompleteListener2 == null) {
            throw new IllegalStateException("No listener register");
        } else if (onLoadCompleteListener2 == onLoadCompleteListener) {
            this.b = null;
        } else {
            throw new IllegalArgumentException("Attempting to unregister the wrong listener");
        }
    }

    @MainThread
    public void unregisterOnLoadCanceledListener(@NonNull OnLoadCanceledListener<D> onLoadCanceledListener) {
        OnLoadCanceledListener<D> onLoadCanceledListener2 = this.c;
        if (onLoadCanceledListener2 == null) {
            throw new IllegalStateException("No listener register");
        } else if (onLoadCanceledListener2 == onLoadCanceledListener) {
            this.c = null;
        } else {
            throw new IllegalArgumentException("Attempting to unregister the wrong listener");
        }
    }
}
