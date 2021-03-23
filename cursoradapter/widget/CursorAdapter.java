package androidx.cursoradapter.widget;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.FilterQueryProvider;
import android.widget.Filterable;
import androidx.annotation.RestrictTo;
import androidx.cursoradapter.widget.CursorFilter;

public abstract class CursorAdapter extends BaseAdapter implements Filterable, CursorFilter.CursorFilterClient {
    @Deprecated
    public static final int FLAG_AUTO_REQUERY = 1;
    public static final int FLAG_REGISTER_CONTENT_OBSERVER = 2;
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    protected boolean a;
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    protected boolean b;
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    protected Cursor c;
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    protected Context d;
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    protected int e;
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    protected ChangeObserver f;
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    protected DataSetObserver g;
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    protected CursorFilter h;
    @RestrictTo({RestrictTo.Scope.LIBRARY_GROUP})
    protected FilterQueryProvider i;

    private class ChangeObserver extends ContentObserver {
        ChangeObserver() {
            super(new Handler());
        }

        public boolean deliverSelfNotifications() {
            return true;
        }

        public void onChange(boolean z) {
            CursorAdapter.this.a();
        }
    }

    private class MyDataSetObserver extends DataSetObserver {
        MyDataSetObserver() {
        }

        public void onChanged() {
            CursorAdapter cursorAdapter = CursorAdapter.this;
            cursorAdapter.a = true;
            cursorAdapter.notifyDataSetChanged();
        }

        public void onInvalidated() {
            CursorAdapter cursorAdapter = CursorAdapter.this;
            cursorAdapter.a = false;
            cursorAdapter.notifyDataSetInvalidated();
        }
    }

    @Deprecated
    public CursorAdapter(Context context, Cursor cursor) {
        a(context, cursor, 1);
    }

    public CursorAdapter(Context context, Cursor cursor, int i2) {
        a(context, cursor, i2);
    }

    public CursorAdapter(Context context, Cursor cursor, boolean z) {
        a(context, cursor, z ? 1 : 2);
    }

    /* access modifiers changed from: protected */
    public void a() {
        Cursor cursor;
        if (this.b && (cursor = this.c) != null && !cursor.isClosed()) {
            this.a = this.c.requery();
        }
    }

    /* access modifiers changed from: package-private */
    public void a(Context context, Cursor cursor, int i2) {
        MyDataSetObserver myDataSetObserver;
        boolean z = false;
        if ((i2 & 1) == 1) {
            i2 |= 2;
            this.b = true;
        } else {
            this.b = false;
        }
        if (cursor != null) {
            z = true;
        }
        this.c = cursor;
        this.a = z;
        this.d = context;
        this.e = z ? cursor.getColumnIndexOrThrow("_id") : -1;
        if ((i2 & 2) == 2) {
            this.f = new ChangeObserver();
            myDataSetObserver = new MyDataSetObserver();
        } else {
            myDataSetObserver = null;
            this.f = null;
        }
        this.g = myDataSetObserver;
        if (z) {
            ChangeObserver changeObserver = this.f;
            if (changeObserver != null) {
                cursor.registerContentObserver(changeObserver);
            }
            DataSetObserver dataSetObserver = this.g;
            if (dataSetObserver != null) {
                cursor.registerDataSetObserver(dataSetObserver);
            }
        }
    }

    public abstract void bindView(View view, Context context, Cursor cursor);

    public void changeCursor(Cursor cursor) {
        Cursor swapCursor = swapCursor(cursor);
        if (swapCursor != null) {
            swapCursor.close();
        }
    }

    public CharSequence convertToString(Cursor cursor) {
        return cursor == null ? "" : cursor.toString();
    }

    public int getCount() {
        Cursor cursor;
        if (!this.a || (cursor = this.c) == null) {
            return 0;
        }
        return cursor.getCount();
    }

    public Cursor getCursor() {
        return this.c;
    }

    public View getDropDownView(int i2, View view, ViewGroup viewGroup) {
        if (!this.a) {
            return null;
        }
        this.c.moveToPosition(i2);
        if (view == null) {
            view = newDropDownView(this.d, this.c, viewGroup);
        }
        bindView(view, this.d, this.c);
        return view;
    }

    public Filter getFilter() {
        if (this.h == null) {
            this.h = new CursorFilter(this);
        }
        return this.h;
    }

    public FilterQueryProvider getFilterQueryProvider() {
        return this.i;
    }

    public Object getItem(int i2) {
        Cursor cursor;
        if (!this.a || (cursor = this.c) == null) {
            return null;
        }
        cursor.moveToPosition(i2);
        return this.c;
    }

    public long getItemId(int i2) {
        Cursor cursor;
        if (!this.a || (cursor = this.c) == null || !cursor.moveToPosition(i2)) {
            return 0;
        }
        return this.c.getLong(this.e);
    }

    public View getView(int i2, View view, ViewGroup viewGroup) {
        if (!this.a) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        } else if (this.c.moveToPosition(i2)) {
            if (view == null) {
                view = newView(this.d, this.c, viewGroup);
            }
            bindView(view, this.d, this.c);
            return view;
        } else {
            throw new IllegalStateException("couldn't move cursor to position " + i2);
        }
    }

    public boolean hasStableIds() {
        return true;
    }

    public View newDropDownView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return newView(context, cursor, viewGroup);
    }

    public abstract View newView(Context context, Cursor cursor, ViewGroup viewGroup);

    public Cursor runQueryOnBackgroundThread(CharSequence charSequence) {
        FilterQueryProvider filterQueryProvider = this.i;
        return filterQueryProvider != null ? filterQueryProvider.runQuery(charSequence) : this.c;
    }

    public void setFilterQueryProvider(FilterQueryProvider filterQueryProvider) {
        this.i = filterQueryProvider;
    }

    public Cursor swapCursor(Cursor cursor) {
        Cursor cursor2 = this.c;
        if (cursor == cursor2) {
            return null;
        }
        if (cursor2 != null) {
            ChangeObserver changeObserver = this.f;
            if (changeObserver != null) {
                cursor2.unregisterContentObserver(changeObserver);
            }
            DataSetObserver dataSetObserver = this.g;
            if (dataSetObserver != null) {
                cursor2.unregisterDataSetObserver(dataSetObserver);
            }
        }
        this.c = cursor;
        if (cursor != null) {
            ChangeObserver changeObserver2 = this.f;
            if (changeObserver2 != null) {
                cursor.registerContentObserver(changeObserver2);
            }
            DataSetObserver dataSetObserver2 = this.g;
            if (dataSetObserver2 != null) {
                cursor.registerDataSetObserver(dataSetObserver2);
            }
            this.e = cursor.getColumnIndexOrThrow("_id");
            this.a = true;
            notifyDataSetChanged();
        } else {
            this.e = -1;
            this.a = false;
            notifyDataSetInvalidated();
        }
        return cursor2;
    }
}
