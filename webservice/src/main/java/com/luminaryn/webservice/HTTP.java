package com.luminaryn.webservice;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

abstract public class HTTP {
    public final OkHttpClient http;
    public final String BASE_URL;

    public final static int LOG_NONE     = 0;
    public final static int LOG_ERRORS   = 1;
    public final static int LOG_WARNINGS = 2;
    public final static int LOG_DEBUG    = 3;

    public String TAG = "com.luminaryn.webservice";
    public int logLevel = LOG_NONE;

    HTTP(String url) {
        http = new OkHttpClient();
        BASE_URL = url;
    }

    public Request.Builder getRequest(String uri) {
        String url = BASE_URL + uri;
        Request.Builder builder = new Request.Builder()
                .url(url);
        return builder;
    }

    public void httpRequest(Request request, Callback callback) {
        http.newCall(request).enqueue(callback);
    }

}
