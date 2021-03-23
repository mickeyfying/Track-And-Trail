package android.support.v4.os;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.v4.os.IResultReceiver;
import androidx.annotation.RestrictTo;

@SuppressLint({"BanParcelableUsage"})
@RestrictTo({RestrictTo.Scope.LIBRARY_GROUP_PREFIX})
public class ResultReceiver implements Parcelable {
    public static final Parcelable.Creator<ResultReceiver> CREATOR = new Parcelable.Creator<ResultReceiver>() {
        public ResultReceiver createFromParcel(Parcel parcel) {
            return new ResultReceiver(parcel);
        }

        public ResultReceiver[] newArray(int i) {
            return new ResultReceiver[i];
        }
    };
    final boolean a;
    final Handler b;
    IResultReceiver c;

    class MyResultReceiver extends IResultReceiver.Stub {
        MyResultReceiver() {
        }

        public void send(int i, Bundle bundle) {
            ResultReceiver resultReceiver = ResultReceiver.this;
            Handler handler = resultReceiver.b;
            if (handler != null) {
                handler.post(new MyRunnable(i, bundle));
            } else {
                resultReceiver.a(i, bundle);
            }
        }
    }

    class MyRunnable implements Runnable {
        final int a;
        final Bundle b;

        MyRunnable(int i, Bundle bundle) {
            this.a = i;
            this.b = bundle;
        }

        public void run() {
            ResultReceiver.this.a(this.a, this.b);
        }
    }

    public ResultReceiver(Handler handler) {
        this.a = true;
        this.b = handler;
    }

    ResultReceiver(Parcel parcel) {
        this.a = false;
        this.b = null;
        this.c = IResultReceiver.Stub.asInterface(parcel.readStrongBinder());
    }

    /* access modifiers changed from: protected */
    public void a(int i, Bundle bundle) {
    }

    public int describeContents() {
        return 0;
    }

    public void send(int i, Bundle bundle) {
        if (this.a) {
            Handler handler = this.b;
            if (handler != null) {
                handler.post(new MyRunnable(i, bundle));
            } else {
                a(i, bundle);
            }
        } else {
            IResultReceiver iResultReceiver = this.c;
            if (iResultReceiver != null) {
                try {
                    iResultReceiver.send(i, bundle);
                } catch (RemoteException unused) {
                }
            }
        }
    }

    public void writeToParcel(Parcel parcel, int i) {
        synchronized (this) {
            if (this.c == null) {
                this.c = new MyResultReceiver();
            }
            parcel.writeStrongBinder(this.c.asBinder());
        }
    }
}
