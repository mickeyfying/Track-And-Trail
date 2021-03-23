package androidx.loader.content;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.core.os.OperationCanceledException;
import androidx.core.util.TimeUtils;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

public abstract class AsyncTaskLoader<D> extends Loader<D> {
    volatile AsyncTaskLoader<D>.LoadTask j;
    volatile AsyncTaskLoader<D>.LoadTask k;
    long l;
    long m;
    private final Executor mExecutor;
    Handler n;

    final class LoadTask extends ModernAsyncTask<Void, Void, D> implements Runnable {
        boolean c;
        private final CountDownLatch mDone = new CountDownLatch(1);

        LoadTask() {
        }

        /* access modifiers changed from: protected */
        public D a(Void... voidArr) {
            try {
                return AsyncTaskLoader.this.h();
            } catch (OperationCanceledException e) {
                if (isCancelled()) {
                    return null;
                }
                throw e;
            }
        }

        /* access modifiers changed from: protected */
        public void b(D d2) {
            try {
                AsyncTaskLoader.this.a(this, d2);
            } finally {
                this.mDone.countDown();
            }
        }

        /* access modifiers changed from: protected */
        public void c(D d2) {
            try {
                AsyncTaskLoader.this.b(this, d2);
            } finally {
                this.mDone.countDown();
            }
        }

        public void run() {
            this.c = false;
            AsyncTaskLoader.this.g();
        }

        public void waitForLoader() {
            try {
                this.mDone.await();
            } catch (InterruptedException unused) {
            }
        }
    }

    public AsyncTaskLoader(@NonNull Context context) {
        this(context, ModernAsyncTask.THREAD_POOL_EXECUTOR);
    }

    private AsyncTaskLoader(@NonNull Context context, @NonNull Executor executor) {
        super(context);
        this.m = -10000;
        this.mExecutor = executor;
    }

    /* access modifiers changed from: package-private */
    public void a(AsyncTaskLoader<D>.LoadTask loadTask, D d) {
        onCanceled(d);
        if (this.k == loadTask) {
            rollbackContentChanged();
            this.m = SystemClock.uptimeMillis();
            this.k = null;
            deliverCancellation();
            g();
        }
    }

    /* access modifiers changed from: package-private */
    public void b(AsyncTaskLoader<D>.LoadTask loadTask, D d) {
        if (this.j != loadTask) {
            a(loadTask, d);
        } else if (isAbandoned()) {
            onCanceled(d);
        } else {
            commitContentChanged();
            this.m = SystemClock.uptimeMillis();
            this.j = null;
            deliverResult(d);
        }
    }

    /* access modifiers changed from: protected */
    public boolean b() {
        if (this.j == null) {
            return false;
        }
        if (!this.e) {
            this.h = true;
        }
        if (this.k != null) {
            if (this.j.c) {
                this.j.c = false;
                this.n.removeCallbacks(this.j);
            }
            this.j = null;
            return false;
        } else if (this.j.c) {
            this.j.c = false;
            this.n.removeCallbacks(this.j);
            this.j = null;
            return false;
        } else {
            boolean cancel = this.j.cancel(false);
            if (cancel) {
                this.k = this.j;
                cancelLoadInBackground();
            }
            this.j = null;
            return cancel;
        }
    }

    /* access modifiers changed from: protected */
    public void c() {
        super.c();
        cancelLoad();
        this.j = new LoadTask();
        g();
    }

    public void cancelLoadInBackground() {
    }

    @Deprecated
    public void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        super.dump(str, fileDescriptor, printWriter, strArr);
        if (this.j != null) {
            printWriter.print(str);
            printWriter.print("mTask=");
            printWriter.print(this.j);
            printWriter.print(" waiting=");
            printWriter.println(this.j.c);
        }
        if (this.k != null) {
            printWriter.print(str);
            printWriter.print("mCancellingTask=");
            printWriter.print(this.k);
            printWriter.print(" waiting=");
            printWriter.println(this.k.c);
        }
        if (this.l != 0) {
            printWriter.print(str);
            printWriter.print("mUpdateThrottle=");
            TimeUtils.formatDuration(this.l, printWriter);
            printWriter.print(" mLastLoadCompleteTime=");
            TimeUtils.formatDuration(this.m, SystemClock.uptimeMillis(), printWriter);
            printWriter.println();
        }
    }

    /* access modifiers changed from: package-private */
    public void g() {
        if (this.k == null && this.j != null) {
            if (this.j.c) {
                this.j.c = false;
                this.n.removeCallbacks(this.j);
            }
            if (this.l <= 0 || SystemClock.uptimeMillis() >= this.m + this.l) {
                this.j.executeOnExecutor(this.mExecutor, (Params[]) null);
                return;
            }
            this.j.c = true;
            this.n.postAtTime(this.j, this.m + this.l);
        }
    }

    /* access modifiers changed from: protected */
    @Nullable
    public D h() {
        return loadInBackground();
    }

    public boolean isLoadInBackgroundCanceled() {
        return this.k != null;
    }

    @Nullable
    public abstract D loadInBackground();

    public void onCanceled(@Nullable D d) {
    }

    public void setUpdateThrottle(long j2) {
        this.l = j2;
        if (j2 != 0) {
            this.n = new Handler();
        }
    }

    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    public void waitForLoader() {
        AsyncTaskLoader<D>.LoadTask loadTask = this.j;
        if (loadTask != null) {
            loadTask.waitForLoader();
        }
    }
}
