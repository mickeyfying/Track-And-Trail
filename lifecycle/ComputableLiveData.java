package androidx.lifecycle;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;
import androidx.annotation.WorkerThread;
import androidx.arch.core.executor.ArchTaskExecutor;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;

@RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
public abstract class ComputableLiveData<T> {
    final Executor a;
    final LiveData<T> b;
    final AtomicBoolean c;
    final AtomicBoolean d;
    @VisibleForTesting
    final Runnable e;
    @VisibleForTesting
    final Runnable f;

    public ComputableLiveData() {
        this(ArchTaskExecutor.getIOThreadExecutor());
    }

    public ComputableLiveData(@NonNull Executor executor) {
        this.c = new AtomicBoolean(true);
        this.d = new AtomicBoolean(false);
        this.e = new Runnable() {
            /* JADX WARNING: Removed duplicated region for block: B:0:0x0000 A[LOOP_START, MTH_ENTER_BLOCK] */
            @androidx.annotation.WorkerThread
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void run() {
                /*
                    r5 = this;
                L_0x0000:
                    androidx.lifecycle.ComputableLiveData r0 = androidx.lifecycle.ComputableLiveData.this
                    java.util.concurrent.atomic.AtomicBoolean r0 = r0.d
                    r1 = 1
                    r2 = 0
                    boolean r0 = r0.compareAndSet(r2, r1)
                    if (r0 == 0) goto L_0x003b
                    r0 = 0
                    r3 = r0
                    r0 = 0
                L_0x000f:
                    androidx.lifecycle.ComputableLiveData r4 = androidx.lifecycle.ComputableLiveData.this     // Catch:{ all -> 0x0032 }
                    java.util.concurrent.atomic.AtomicBoolean r4 = r4.c     // Catch:{ all -> 0x0032 }
                    boolean r4 = r4.compareAndSet(r1, r2)     // Catch:{ all -> 0x0032 }
                    if (r4 == 0) goto L_0x0021
                    androidx.lifecycle.ComputableLiveData r0 = androidx.lifecycle.ComputableLiveData.this     // Catch:{ all -> 0x0032 }
                    java.lang.Object r3 = r0.a()     // Catch:{ all -> 0x0032 }
                    r0 = 1
                    goto L_0x000f
                L_0x0021:
                    if (r0 == 0) goto L_0x002a
                    androidx.lifecycle.ComputableLiveData r1 = androidx.lifecycle.ComputableLiveData.this     // Catch:{ all -> 0x0032 }
                    androidx.lifecycle.LiveData<T> r1 = r1.b     // Catch:{ all -> 0x0032 }
                    r1.postValue(r3)     // Catch:{ all -> 0x0032 }
                L_0x002a:
                    androidx.lifecycle.ComputableLiveData r1 = androidx.lifecycle.ComputableLiveData.this
                    java.util.concurrent.atomic.AtomicBoolean r1 = r1.d
                    r1.set(r2)
                    goto L_0x003c
                L_0x0032:
                    r0 = move-exception
                    androidx.lifecycle.ComputableLiveData r1 = androidx.lifecycle.ComputableLiveData.this
                    java.util.concurrent.atomic.AtomicBoolean r1 = r1.d
                    r1.set(r2)
                    throw r0
                L_0x003b:
                    r0 = 0
                L_0x003c:
                    if (r0 == 0) goto L_0x0048
                    androidx.lifecycle.ComputableLiveData r0 = androidx.lifecycle.ComputableLiveData.this
                    java.util.concurrent.atomic.AtomicBoolean r0 = r0.c
                    boolean r0 = r0.get()
                    if (r0 != 0) goto L_0x0000
                L_0x0048:
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: androidx.lifecycle.ComputableLiveData.AnonymousClass2.run():void");
            }
        };
        this.f = new Runnable() {
            @MainThread
            public void run() {
                boolean hasActiveObservers = ComputableLiveData.this.b.hasActiveObservers();
                if (ComputableLiveData.this.c.compareAndSet(false, true) && hasActiveObservers) {
                    ComputableLiveData computableLiveData = ComputableLiveData.this;
                    computableLiveData.a.execute(computableLiveData.e);
                }
            }
        };
        this.a = executor;
        this.b = new LiveData<T>() {
            /* access modifiers changed from: protected */
            public void b() {
                ComputableLiveData computableLiveData = ComputableLiveData.this;
                computableLiveData.a.execute(computableLiveData.e);
            }
        };
    }

    /* access modifiers changed from: protected */
    @WorkerThread
    public abstract T a();

    @NonNull
    public LiveData<T> getLiveData() {
        return this.b;
    }

    public void invalidate() {
        ArchTaskExecutor.getInstance().executeOnMainThread(this.f);
    }
}
