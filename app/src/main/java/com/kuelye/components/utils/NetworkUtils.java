package com.kuelye.components.utils;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static com.kuelye.components.utils.IOUtils.readFullyAndCloseSilently;

public final class NetworkUtils {

    private static final String RESPONSE_ENCODING_DEFAULT = "UTF-8";

    public static String getResponse(String request) throws IOException {
        return getResponse(request, RESPONSE_ENCODING_DEFAULT);
    }

    public static String getResponse(String request, String responseEncoding) throws IOException {
        final URL url = new URL(request);
        final InputStream in = url.openConnection().getInputStream();
        return readFullyAndCloseSilently(in, responseEncoding);
    }

}
