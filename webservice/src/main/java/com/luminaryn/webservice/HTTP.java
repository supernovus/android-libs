package com.luminaryn.webservice;

import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;

abstract public class HTTP {
    public final OkHttpClient http;

    public final static int LOG_NONE     = 0;
    public final static int LOG_ERRORS   = 1;
    public final static int LOG_WARNINGS = 2;
    public final static int LOG_DEBUG    = 3;

    public String baseURL = "";
    public String TAG = "com.luminaryn.webservice";
    public int logLevel = LOG_NONE;

    HTTP() {
        http = new OkHttpClient();
    }

    public void setBaseUrl(String url) {
        baseURL = url;
    }

    public void setTag(String tag) {
        TAG = tag;
    }

    public Request.Builder getRequest(String uri) {
        String url = baseURL + uri;
        Request.Builder builder = new Request.Builder()
                .url(url);
        return builder;
    }

    public void httpRequest(Request request, Callback callback) {
        http.newCall(request).enqueue(callback);
    }

}
