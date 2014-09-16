package com.kuelye.components.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class IOUtils {

    private static final int BUFFER_SIZE = 1024;

    public static String readFullyAndClose(InputStream inputStream, String encoding)
            throws IOException {
        try {
            return new String(readFully(inputStream), encoding);
        } finally {
            inputStream.close();
        }
    }

    public static String readFully(InputStream inputStream, String encoding) throws IOException {
        return new String(readFully(inputStream), encoding);
    }

    // ------------------- PRIVATE -------------------

    private static byte[] readFully(InputStream inputStream) throws IOException {
        final ByteArrayOutputStream baos = new ByteArrayOutputStream();
        final byte[] buffer = new byte[BUFFER_SIZE];
        int length;

        while ((length = inputStream.read(buffer)) != -1) {
            baos.write(buffer, 0, length);
        }

        return baos.toByteArray();
    }

}
