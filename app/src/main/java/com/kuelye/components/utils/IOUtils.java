package com.kuelye.components.utils;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;

public final class IOUtils {

    private static final int BUFFER_SIZE = 1024;

    public static String readFullyAndCloseSilently(InputStream /**@Nullable*/ in
            , String /**@NotNull*/ encoding)
            throws IOException {
        try {
            return new String(readFully(in), encoding);
        } finally {
            closeSilenty(in);
        }
    }

    public static void writeBitmapAndCloseSilently(OutputStream /**@Nullable*/ out
            , Bitmap /**@NotNull*/ bitmap
            , Bitmap.CompressFormat /**@NotNull*/ compressFormat
            , int quality) {
        bitmap.compress(compressFormat, quality, out);
        closeSilenty(out);
    }

    public static void closeSilenty(Closeable /**@Nullable*/ c) {
        if (c == null) {
            return;
        }

        try {
            c.close();
        } catch (IOException e) {
            // ignore
        }
    }

    // ------------------- PRIVATE -------------------

    private static byte[] readFully(InputStream in) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final byte[] buffer = new byte[BUFFER_SIZE];
        int length;

        while ((length = in.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }

        return baos.toByteArray();
    }

}
