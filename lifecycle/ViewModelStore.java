package androidx.lifecycle;

import java.util.HashMap;

public class ViewModelStore {
    private final HashMap<String, ViewModel> mMap = new HashMap<>();

    /* access modifiers changed from: package-private */
    public final ViewModel a(String str) {
        return this.mMap.get(str);
    }

    /* access modifiers changed from: package-private */
    public final void a(String str, ViewModel viewModel) {
        ViewModel put = this.mMap.put(str, viewModel);
        if (put != null) {
            put.b();
        }
    }

    public final void clear() {
        for (ViewModel a : this.mMap.values()) {
            a.a();
        }
        this.mMap.clear();
    }
}
