package android.support.v4.graphics;

import android.content.Context;
import android.content.res.Resources;
import android.os.Process;
import android.util.Log;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

public class TypefaceCompatUtil {
    private static final String CACHE_FILE_PREFIX = ".font";
    private static final String TAG = "TypefaceCompatUtil";

    private TypefaceCompatUtil() {
    }

    public static File getTempFile(Context context) {
        String str = CACHE_FILE_PREFIX + Process.myPid() + "-" + Process.myTid() + "-";
        int i = 0;
        while (i < 100) {
            File file = new File(context.getCacheDir(), str + i);
            try {
                if (file.createNewFile()) {
                    return file;
                }
                i++;
            } catch (IOException unused) {
            }
        }
        return null;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001b, code lost:
        r1 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0024, code lost:
        throw r1;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static java.nio.ByteBuffer mmap(java.io.File r7) {
        /*
            java.io.FileInputStream r0 = new java.io.FileInputStream     // Catch:{ IOException -> 0x0025 }
            r0.<init>(r7)     // Catch:{ IOException -> 0x0025 }
            java.nio.channels.FileChannel r1 = r0.getChannel()     // Catch:{ all -> 0x0019 }
            long r5 = r1.size()     // Catch:{ all -> 0x0019 }
            java.nio.channels.FileChannel$MapMode r2 = java.nio.channels.FileChannel.MapMode.READ_ONLY     // Catch:{ all -> 0x0019 }
            r3 = 0
            java.nio.MappedByteBuffer r7 = r1.map(r2, r3, r5)     // Catch:{ all -> 0x0019 }
            r0.close()     // Catch:{ IOException -> 0x0025 }
            return r7
        L_0x0019:
            r7 = move-exception
            throw r7     // Catch:{ all -> 0x001b }
        L_0x001b:
            r1 = move-exception
            r0.close()     // Catch:{ all -> 0x0020 }
            goto L_0x0024
        L_0x0020:
            r0 = move-exception
            r7.addSuppressed(r0)     // Catch:{ IOException -> 0x0025 }
        L_0x0024:
            throw r1     // Catch:{ IOException -> 0x0025 }
        L_0x0025:
            r7 = 0
            return r7
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.graphics.TypefaceCompatUtil.mmap(java.io.File):java.nio.ByteBuffer");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:16:0x002e, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:?, code lost:
        r7.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0033, code lost:
        r7 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:?, code lost:
        r8.addSuppressed(r7);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0037, code lost:
        throw r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x003a, code lost:
        r8 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x003b, code lost:
        if (r6 != null) goto L_0x003d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:?, code lost:
        r6.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0045, code lost:
        throw r8;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static java.nio.ByteBuffer mmap(android.content.Context r6, android.os.CancellationSignal r7, android.net.Uri r8) {
        /*
            android.content.ContentResolver r6 = r6.getContentResolver()
            java.lang.String r0 = "r"
            android.os.ParcelFileDescriptor r6 = r6.openFileDescriptor(r8, r0, r7)     // Catch:{ IOException -> 0x0046 }
            java.io.FileInputStream r7 = new java.io.FileInputStream     // Catch:{ all -> 0x0038 }
            java.io.FileDescriptor r8 = r6.getFileDescriptor()     // Catch:{ all -> 0x0038 }
            r7.<init>(r8)     // Catch:{ all -> 0x0038 }
            java.nio.channels.FileChannel r0 = r7.getChannel()     // Catch:{ all -> 0x002c }
            long r4 = r0.size()     // Catch:{ all -> 0x002c }
            java.nio.channels.FileChannel$MapMode r1 = java.nio.channels.FileChannel.MapMode.READ_ONLY     // Catch:{ all -> 0x002c }
            r2 = 0
            java.nio.MappedByteBuffer r8 = r0.map(r1, r2, r4)     // Catch:{ all -> 0x002c }
            r7.close()     // Catch:{ all -> 0x0038 }
            if (r6 == 0) goto L_0x002b
            r6.close()     // Catch:{ IOException -> 0x0046 }
        L_0x002b:
            return r8
        L_0x002c:
            r8 = move-exception
            throw r8     // Catch:{ all -> 0x002e }
        L_0x002e:
            r0 = move-exception
            r7.close()     // Catch:{ all -> 0x0033 }
            goto L_0x0037
        L_0x0033:
            r7 = move-exception
            r8.addSuppressed(r7)     // Catch:{ all -> 0x0038 }
        L_0x0037:
            throw r0     // Catch:{ all -> 0x0038 }
        L_0x0038:
            r7 = move-exception
            throw r7     // Catch:{ all -> 0x003a }
        L_0x003a:
            r8 = move-exception
            if (r6 == 0) goto L_0x0045
            r6.close()     // Catch:{ all -> 0x0041 }
            goto L_0x0045
        L_0x0041:
            r6 = move-exception
            r7.addSuppressed(r6)     // Catch:{ IOException -> 0x0046 }
        L_0x0045:
            throw r8     // Catch:{ IOException -> 0x0046 }
        L_0x0046:
            r6 = 0
            return r6
        */
        throw new UnsupportedOperationException("Method not decompiled: android.support.v4.graphics.TypefaceCompatUtil.mmap(android.content.Context, android.os.CancellationSignal, android.net.Uri):java.nio.ByteBuffer");
    }

    public static ByteBuffer copyToDirectBuffer(Context context, Resources resources, int i) {
        File tempFile = getTempFile(context);
        if (tempFile == null) {
            return null;
        }
        try {
            if (!copyToFile(tempFile, resources, i)) {
                return null;
            }
            ByteBuffer mmap = mmap(tempFile);
            tempFile.delete();
            return mmap;
        } finally {
            tempFile.delete();
        }
    }

    public static boolean copyToFile(File file, InputStream inputStream) {
        FileOutputStream fileOutputStream = null;
        try {
            FileOutputStream fileOutputStream2 = new FileOutputStream(file, false);
            try {
                byte[] bArr = new byte[1024];
                while (true) {
                    int read = inputStream.read(bArr);
                    if (read != -1) {
                        fileOutputStream2.write(bArr, 0, read);
                    } else {
                        closeQuietly(fileOutputStream2);
                        return true;
                    }
                }
            } catch (IOException e) {
                e = e;
                fileOutputStream = fileOutputStream2;
                try {
                    Log.e(TAG, "Error copying resource contents to temp file: " + e.getMessage());
                    closeQuietly(fileOutputStream);
                    return false;
                } catch (Throwable th) {
                    th = th;
                    closeQuietly(fileOutputStream);
                    throw th;
                }
            } catch (Throwable th2) {
                th = th2;
                fileOutputStream = fileOutputStream2;
                closeQuietly(fileOutputStream);
                throw th;
            }
        } catch (IOException e2) {
            e = e2;
            Log.e(TAG, "Error copying resource contents to temp file: " + e.getMessage());
            closeQuietly(fileOutputStream);
            return false;
        }
    }

    public static boolean copyToFile(File file, Resources resources, int i) {
        InputStream inputStream;
        try {
            inputStream = resources.openRawResource(i);
            try {
                boolean copyToFile = copyToFile(file, inputStream);
                closeQuietly(inputStream);
                return copyToFile;
            } catch (Throwable th) {
                th = th;
                closeQuietly(inputStream);
                throw th;
            }
        } catch (Throwable th2) {
            th = th2;
            inputStream = null;
            closeQuietly(inputStream);
            throw th;
        }
    }

    public static void closeQuietly(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException unused) {
            }
        }
    }
}
