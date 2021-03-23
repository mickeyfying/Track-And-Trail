package androidx.lifecycle;

import androidx.annotation.CallSuper;
import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.arch.core.internal.SafeIterableMap;
import java.util.Iterator;
import java.util.Map;

public class MediatorLiveData<T> extends MutableLiveData<T> {
    private SafeIterableMap<LiveData<?>, Source<?>> mSources = new SafeIterableMap<>();

    private static class Source<V> implements Observer<V> {
        final LiveData<V> a;
        final Observer<? super V> b;
        int c = -1;

        Source(LiveData<V> liveData, Observer<? super V> observer) {
            this.a = liveData;
            this.b = observer;
        }

        /* access modifiers changed from: package-private */
        public void a() {
            this.a.observeForever(this);
        }

        /* access modifiers changed from: package-private */
        public void b() {
            this.a.removeObserver(this);
        }

        public void onChanged(@Nullable V v) {
            if (this.c != this.a.a()) {
                this.c = this.a.a();
                this.b.onChanged(v);
            }
        }
    }

    @MainThread
    public <S> void addSource(@NonNull LiveData<S> liveData, @NonNull Observer<? super S> observer) {
        Source source = new Source(liveData, observer);
        Source putIfAbsent = this.mSources.putIfAbsent(liveData, source);
        if (putIfAbsent != null && putIfAbsent.b != observer) {
            throw new IllegalArgumentException("This source was already added with the different observer");
        } else if (putIfAbsent == null && hasActiveObservers()) {
            source.a();
        }
    }

    /* access modifiers changed from: protected */
    @CallSuper
    public void b() {
        Iterator<Map.Entry<LiveData<?>, Source<?>>> it = this.mSources.iterator();
        while (it.hasNext()) {
            ((Source) it.next().getValue()).a();
        }
    }

    /* access modifiers changed from: protected */
    @CallSuper
    public void c() {
        Iterator<Map.Entry<LiveData<?>, Source<?>>> it = this.mSources.iterator();
        while (it.hasNext()) {
            ((Source) it.next().getValue()).b();
        }
    }

    @MainThread
    public <S> void removeSource(@NonNull LiveData<S> liveData) {
        Source remove = this.mSources.remove(liveData);
        if (remove != null) {
            remove.b();
        }
    }
}
