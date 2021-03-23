package androidx.loader.app;

import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.collection.SparseArrayCompat;
import androidx.core.util.DebugUtils;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelStore;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.lang.reflect.Modifier;

class LoaderManagerImpl extends LoaderManager {
    static boolean a;
    @NonNull
    private final LifecycleOwner mLifecycleOwner;
    @NonNull
    private final LoaderViewModel mLoaderViewModel;

    public static class LoaderInfo<D> extends MutableLiveData<D> implements Loader.OnLoadCompleteListener<D> {
        @Nullable
        private final Bundle mArgs;
        private final int mId;
        private LifecycleOwner mLifecycleOwner;
        @NonNull
        private final Loader<D> mLoader;
        private LoaderObserver<D> mObserver;
        private Loader<D> mPriorLoader;

        LoaderInfo(int i, @Nullable Bundle bundle, @NonNull Loader<D> loader, @Nullable Loader<D> loader2) {
            this.mId = i;
            this.mArgs = bundle;
            this.mLoader = loader;
            this.mPriorLoader = loader2;
            this.mLoader.registerListener(i, this);
        }

        /* access modifiers changed from: package-private */
        @MainThread
        @NonNull
        public Loader<D> a(@NonNull LifecycleOwner lifecycleOwner, @NonNull LoaderManager.LoaderCallbacks<D> loaderCallbacks) {
            LoaderObserver<D> loaderObserver = new LoaderObserver<>(this.mLoader, loaderCallbacks);
            observe(lifecycleOwner, loaderObserver);
            LoaderObserver<D> loaderObserver2 = this.mObserver;
            if (loaderObserver2 != null) {
                removeObserver(loaderObserver2);
            }
            this.mLifecycleOwner = lifecycleOwner;
            this.mObserver = loaderObserver;
            return this.mLoader;
        }

        /* access modifiers changed from: package-private */
        @MainThread
        public Loader<D> a(boolean z) {
            if (LoaderManagerImpl.a) {
                Log.v("LoaderManager", "  Destroying: " + this);
            }
            this.mLoader.cancelLoad();
            this.mLoader.abandon();
            LoaderObserver<D> loaderObserver = this.mObserver;
            if (loaderObserver != null) {
                removeObserver(loaderObserver);
                if (z) {
                    loaderObserver.b();
                }
            }
            this.mLoader.unregisterListener(this);
            if ((loaderObserver == null || loaderObserver.a()) && !z) {
                return this.mLoader;
            }
            this.mLoader.reset();
            return this.mPriorLoader;
        }

        /* access modifiers changed from: protected */
        public void b() {
            if (LoaderManagerImpl.a) {
                Log.v("LoaderManager", "  Starting: " + this);
            }
            this.mLoader.startLoading();
        }

        /* access modifiers changed from: protected */
        public void c() {
            if (LoaderManagerImpl.a) {
                Log.v("LoaderManager", "  Stopping: " + this);
            }
            this.mLoader.stopLoading();
        }

        /* access modifiers changed from: package-private */
        @NonNull
        public Loader<D> d() {
            return this.mLoader;
        }

        public void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
            printWriter.print(str);
            printWriter.print("mId=");
            printWriter.print(this.mId);
            printWriter.print(" mArgs=");
            printWriter.println(this.mArgs);
            printWriter.print(str);
            printWriter.print("mLoader=");
            printWriter.println(this.mLoader);
            Loader<D> loader = this.mLoader;
            loader.dump(str + "  ", fileDescriptor, printWriter, strArr);
            if (this.mObserver != null) {
                printWriter.print(str);
                printWriter.print("mCallbacks=");
                printWriter.println(this.mObserver);
                LoaderObserver<D> loaderObserver = this.mObserver;
                loaderObserver.dump(str + "  ", printWriter);
            }
            printWriter.print(str);
            printWriter.print("mData=");
            printWriter.println(d().dataToString(getValue()));
            printWriter.print(str);
            printWriter.print("mStarted=");
            printWriter.println(hasActiveObservers());
        }

        /* access modifiers changed from: package-private */
        /* JADX WARNING: Code restructure failed: missing block: B:3:0x0008, code lost:
            r0 = r2.mObserver;
         */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean e() {
            /*
                r2 = this;
                boolean r0 = r2.hasActiveObservers()
                r1 = 0
                if (r0 != 0) goto L_0x0008
                return r1
            L_0x0008:
                androidx.loader.app.LoaderManagerImpl$LoaderObserver<D> r0 = r2.mObserver
                if (r0 == 0) goto L_0x0013
                boolean r0 = r0.a()
                if (r0 != 0) goto L_0x0013
                r1 = 1
            L_0x0013:
                return r1
            */
            throw new UnsupportedOperationException("Method not decompiled: androidx.loader.app.LoaderManagerImpl.LoaderInfo.e():boolean");
        }

        /* access modifiers changed from: package-private */
        public void f() {
            LifecycleOwner lifecycleOwner = this.mLifecycleOwner;
            LoaderObserver<D> loaderObserver = this.mObserver;
            if (lifecycleOwner != null && loaderObserver != null) {
                super.removeObserver(loaderObserver);
                observe(lifecycleOwner, loaderObserver);
            }
        }

        public void onLoadComplete(@NonNull Loader<D> loader, @Nullable D d) {
            if (LoaderManagerImpl.a) {
                Log.v("LoaderManager", "onLoadComplete: " + this);
            }
            if (Looper.myLooper() == Looper.getMainLooper()) {
                setValue(d);
                return;
            }
            if (LoaderManagerImpl.a) {
                Log.w("LoaderManager", "onLoadComplete was incorrectly called on a background thread");
            }
            postValue(d);
        }

        public void removeObserver(@NonNull Observer<? super D> observer) {
            super.removeObserver(observer);
            this.mLifecycleOwner = null;
            this.mObserver = null;
        }

        public void setValue(D d) {
            super.setValue(d);
            Loader<D> loader = this.mPriorLoader;
            if (loader != null) {
                loader.reset();
                this.mPriorLoader = null;
            }
        }

        public String toString() {
            StringBuilder sb = new StringBuilder(64);
            sb.append("LoaderInfo{");
            sb.append(Integer.toHexString(System.identityHashCode(this)));
            sb.append(" #");
            sb.append(this.mId);
            sb.append(" : ");
            DebugUtils.buildShortClassTag(this.mLoader, sb);
            sb.append("}}");
            return sb.toString();
        }
    }

    static class LoaderObserver<D> implements Observer<D> {
        @NonNull
        private final LoaderManager.LoaderCallbacks<D> mCallback;
        private boolean mDeliveredData = false;
        @NonNull
        private final Loader<D> mLoader;

        LoaderObserver(@NonNull Loader<D> loader, @NonNull LoaderManager.LoaderCallbacks<D> loaderCallbacks) {
            this.mLoader = loader;
            this.mCallback = loaderCallbacks;
        }

        /* access modifiers changed from: package-private */
        public boolean a() {
            return this.mDeliveredData;
        }

        /* access modifiers changed from: package-private */
        @MainThread
        public void b() {
            if (this.mDeliveredData) {
                if (LoaderManagerImpl.a) {
                    Log.v("LoaderManager", "  Resetting: " + this.mLoader);
                }
                this.mCallback.onLoaderReset(this.mLoader);
            }
        }

        public void dump(String str, PrintWriter printWriter) {
            printWriter.print(str);
            printWriter.print("mDeliveredData=");
            printWriter.println(this.mDeliveredData);
        }

        public void onChanged(@Nullable D d) {
            if (LoaderManagerImpl.a) {
                Log.v("LoaderManager", "  onLoadFinished in " + this.mLoader + ": " + this.mLoader.dataToString(d));
            }
            this.mCallback.onLoadFinished(this.mLoader, d);
            this.mDeliveredData = true;
        }

        public String toString() {
            return this.mCallback.toString();
        }
    }

    static class LoaderViewModel extends ViewModel {
        private static final ViewModelProvider.Factory FACTORY = new ViewModelProvider.Factory() {
            @NonNull
            public <T extends ViewModel> T create(@NonNull Class<T> cls) {
                return new LoaderViewModel();
            }
        };
        private boolean mCreatingLoader = false;
        private SparseArrayCompat<LoaderInfo> mLoaders = new SparseArrayCompat<>();

        LoaderViewModel() {
        }

        @NonNull
        static LoaderViewModel a(ViewModelStore viewModelStore) {
            return (LoaderViewModel) new ViewModelProvider(viewModelStore, FACTORY).get(LoaderViewModel.class);
        }

        /* access modifiers changed from: package-private */
        public <D> LoaderInfo<D> a(int i) {
            return this.mLoaders.get(i);
        }

        /* access modifiers changed from: package-private */
        public void a(int i, @NonNull LoaderInfo loaderInfo) {
            this.mLoaders.put(i, loaderInfo);
        }

        /* access modifiers changed from: protected */
        public void b() {
            super.b();
            int size = this.mLoaders.size();
            for (int i = 0; i < size; i++) {
                this.mLoaders.valueAt(i).a(true);
            }
            this.mLoaders.clear();
        }

        /* access modifiers changed from: package-private */
        public void b(int i) {
            this.mLoaders.remove(i);
        }

        /* access modifiers changed from: package-private */
        public void c() {
            this.mCreatingLoader = false;
        }

        /* access modifiers changed from: package-private */
        public boolean d() {
            int size = this.mLoaders.size();
            for (int i = 0; i < size; i++) {
                if (this.mLoaders.valueAt(i).e()) {
                    return true;
                }
            }
            return false;
        }

        public void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
            if (this.mLoaders.size() > 0) {
                printWriter.print(str);
                printWriter.println("Loaders:");
                String str2 = str + "    ";
                for (int i = 0; i < this.mLoaders.size(); i++) {
                    LoaderInfo valueAt = this.mLoaders.valueAt(i);
                    printWriter.print(str);
                    printWriter.print("  #");
                    printWriter.print(this.mLoaders.keyAt(i));
                    printWriter.print(": ");
                    printWriter.println(valueAt.toString());
                    valueAt.dump(str2, fileDescriptor, printWriter, strArr);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public boolean e() {
            return this.mCreatingLoader;
        }

        /* access modifiers changed from: package-private */
        public void f() {
            int size = this.mLoaders.size();
            for (int i = 0; i < size; i++) {
                this.mLoaders.valueAt(i).f();
            }
        }

        /* access modifiers changed from: package-private */
        public void g() {
            this.mCreatingLoader = true;
        }
    }

    LoaderManagerImpl(@NonNull LifecycleOwner lifecycleOwner, @NonNull ViewModelStore viewModelStore) {
        this.mLifecycleOwner = lifecycleOwner;
        this.mLoaderViewModel = LoaderViewModel.a(viewModelStore);
    }

    /* JADX INFO: finally extract failed */
    @MainThread
    @NonNull
    private <D> Loader<D> createAndInstallLoader(int i, @Nullable Bundle bundle, @NonNull LoaderManager.LoaderCallbacks<D> loaderCallbacks, @Nullable Loader<D> loader) {
        try {
            this.mLoaderViewModel.g();
            Loader<D> onCreateLoader = loaderCallbacks.onCreateLoader(i, bundle);
            if (onCreateLoader != null) {
                if (onCreateLoader.getClass().isMemberClass()) {
                    if (!Modifier.isStatic(onCreateLoader.getClass().getModifiers())) {
                        throw new IllegalArgumentException("Object returned from onCreateLoader must not be a non-static inner member class: " + onCreateLoader);
                    }
                }
                LoaderInfo loaderInfo = new LoaderInfo(i, bundle, onCreateLoader, loader);
                if (a) {
                    Log.v("LoaderManager", "  Created new loader " + loaderInfo);
                }
                this.mLoaderViewModel.a(i, loaderInfo);
                this.mLoaderViewModel.c();
                return loaderInfo.a(this.mLifecycleOwner, loaderCallbacks);
            }
            throw new IllegalArgumentException("Object returned from onCreateLoader must not be null");
        } catch (Throwable th) {
            this.mLoaderViewModel.c();
            throw th;
        }
    }

    @MainThread
    public void destroyLoader(int i) {
        if (this.mLoaderViewModel.e()) {
            throw new IllegalStateException("Called while creating a loader");
        } else if (Looper.getMainLooper() == Looper.myLooper()) {
            if (a) {
                Log.v("LoaderManager", "destroyLoader in " + this + " of " + i);
            }
            LoaderInfo a2 = this.mLoaderViewModel.a(i);
            if (a2 != null) {
                a2.a(true);
                this.mLoaderViewModel.b(i);
            }
        } else {
            throw new IllegalStateException("destroyLoader must be called on the main thread");
        }
    }

    @Deprecated
    public void dump(String str, FileDescriptor fileDescriptor, PrintWriter printWriter, String[] strArr) {
        this.mLoaderViewModel.dump(str, fileDescriptor, printWriter, strArr);
    }

    @Nullable
    public <D> Loader<D> getLoader(int i) {
        if (!this.mLoaderViewModel.e()) {
            LoaderInfo a2 = this.mLoaderViewModel.a(i);
            if (a2 != null) {
                return a2.d();
            }
            return null;
        }
        throw new IllegalStateException("Called while creating a loader");
    }

    public boolean hasRunningLoaders() {
        return this.mLoaderViewModel.d();
    }

    @MainThread
    @NonNull
    public <D> Loader<D> initLoader(int i, @Nullable Bundle bundle, @NonNull LoaderManager.LoaderCallbacks<D> loaderCallbacks) {
        if (this.mLoaderViewModel.e()) {
            throw new IllegalStateException("Called while creating a loader");
        } else if (Looper.getMainLooper() == Looper.myLooper()) {
            LoaderInfo a2 = this.mLoaderViewModel.a(i);
            if (a) {
                Log.v("LoaderManager", "initLoader in " + this + ": args=" + bundle);
            }
            if (a2 == null) {
                return createAndInstallLoader(i, bundle, loaderCallbacks, (Loader) null);
            }
            if (a) {
                Log.v("LoaderManager", "  Re-using existing loader " + a2);
            }
            return a2.a(this.mLifecycleOwner, loaderCallbacks);
        } else {
            throw new IllegalStateException("initLoader must be called on the main thread");
        }
    }

    public void markForRedelivery() {
        this.mLoaderViewModel.f();
    }

    @MainThread
    @NonNull
    public <D> Loader<D> restartLoader(int i, @Nullable Bundle bundle, @NonNull LoaderManager.LoaderCallbacks<D> loaderCallbacks) {
        if (this.mLoaderViewModel.e()) {
            throw new IllegalStateException("Called while creating a loader");
        } else if (Looper.getMainLooper() == Looper.myLooper()) {
            if (a) {
                Log.v("LoaderManager", "restartLoader in " + this + ": args=" + bundle);
            }
            LoaderInfo a2 = this.mLoaderViewModel.a(i);
            Loader loader = null;
            if (a2 != null) {
                loader = a2.a(false);
            }
            return createAndInstallLoader(i, bundle, loaderCallbacks, loader);
        } else {
            throw new IllegalStateException("restartLoader must be called on the main thread");
        }
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(128);
        sb.append("LoaderManager{");
        sb.append(Integer.toHexString(System.identityHashCode(this)));
        sb.append(" in ");
        DebugUtils.buildShortClassTag(this.mLifecycleOwner, sb);
        sb.append("}}");
        return sb.toString();
    }
}
