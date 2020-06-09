package com.luminaryn.webservice;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

abstract public class HTTP {
    final OkHttpClient http;
    final String BASE_URL;

    final static int LOG_NONE     = 0;
    final static int LOG_ERRORS   = 1;
    final static int LOG_WARNINGS = 2;
    final static int LOG_DEBUG    = 3;

    String TAG = "com.luminaryn.webservice";
    int logLevel = LOG_NONE;

    HTTP(String url) {
        http = new OkHttpClient();
        BASE_URL = url;
    }

    Request.Builder getRequest(String uri) {
        String url = BASE_URL + uri;
        Request.Builder builder = new Request.Builder()
                .url(url);
        return builder;
    }

    void httpRequest(Request request, Callback callback) {
        http.newCall(request).enqueue(callback);
    }

}
