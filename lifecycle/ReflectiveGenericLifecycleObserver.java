package androidx.lifecycle;

import androidx.lifecycle.ClassesInfoCache;
import androidx.lifecycle.Lifecycle;

class ReflectiveGenericLifecycleObserver implements LifecycleEventObserver {
    private final ClassesInfoCache.CallbackInfo mInfo = ClassesInfoCache.a.a(this.mWrapped.getClass());
    private final Object mWrapped;

    ReflectiveGenericLifecycleObserver(Object obj) {
        this.mWrapped = obj;
    }

    public void onStateChanged(LifecycleOwner lifecycleOwner, Lifecycle.Event event) {
        this.mInfo.a(lifecycleOwner, event, this.mWrapped);
    }
}
