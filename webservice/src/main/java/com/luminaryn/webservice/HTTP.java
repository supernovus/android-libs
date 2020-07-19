package com.luminaryn.webservice;

import android.os.Handler;
import android.os.Looper;

import java.io.File;

import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;

/**
 * Simple wrapper for OkHttp.
 *
 * Not meant to be used on it's own, see JSON for a real implementation.
 */
abstract public class HTTP {
    protected final OkHttpClient http;

    public final static int LOG_NONE     = 0;
    public final static int LOG_ERRORS   = 1;
    public final static int LOG_WARNINGS = 2;
    public final static int LOG_DEBUG    = 3;

    public static final MediaType TYPE_DEFAULT_FILE = MediaType.get("application/octet-stream");

    protected final static String DEFAULT_TAG = "com.luminaryn.webservice";
    protected final static int DEFAULT_LOG_LEVEL = LOG_NONE;

    protected final String baseURL;
    protected final String TAG;
    protected final int logLevel;

    /**
     * Create an HTTP client with all defaults.
     */
    public HTTP() {
        baseURL = "";
        TAG = DEFAULT_TAG;
        logLevel = DEFAULT_LOG_LEVEL;
        http = new OkHttpClient();
    }

    /**
     * Create an HTTP client with mostly defaults, but a baseURL set.
     * @param baseURL
     */
    public HTTP(String baseURL) {
        this.baseURL = baseURL;
        TAG = DEFAULT_TAG;
        logLevel = DEFAULT_LOG_LEVEL;
        http = new OkHttpClient();
    }

    /**
     * Used by HTTP.Builder.build() to create an HTTP client with customized settings.
     *
     * @param httpBuilder
     */
    protected HTTP(Builder httpBuilder) {
        baseURL = httpBuilder.baseURL;
        TAG = httpBuilder.TAG;
        logLevel = httpBuilder.logLevel;
        if (httpBuilder.okClientBuilder == null) {
            http = new OkHttpClient();
        }
        else {
            http = httpBuilder.okClientBuilder.build();
        }
    }

    /**
     * Get the OkHttpClient instance.
     *
     * @return
     */
    public OkHttpClient getClient () {
        return http;
    }

    /**
     * Get a handler in the main UI looper.
     *
     * @return
     */
    public static Handler getUIHandler() {
        return new Handler(Looper.getMainLooper());
    }

    /**
     * Return an okhttp3.Request.Builder with a URL set.
     * Will automatically prepend the baseURL.
     *
     * @param uri
     * @return
     */
    public Request.Builder makeRequest(String uri) {
        String url = baseURL + uri;
        Request.Builder builder = new Request.Builder()
                .url(url);
        return builder;
    }

    /**
     * Get a okhttp3.MultipartBody.Builder instance set to the
     * multipart/form-data MIME type.
     *
     * @return
     */
    public MultipartBody.Builder formBody() {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        return builder;
    }

    public MultipartBody.Builder formBody(String fileFormName, File file, MediaType contentType) {
        return formBody().addFormDataPart(fileFormName, file.getName(), RequestBody.create(file, contentType));
    }

    public MultipartBody.Builder formBody(String fileFormName, File file, String contentType) {
        return formBody(fileFormName, file, MediaType.get(contentType));
    }

    public MultipartBody.Builder formBody(String fileFormName, File file) {
        return formBody(fileFormName, file, TYPE_DEFAULT_FILE);
    }

    public MultipartBody.Builder formBody(String fileFormName, String fileName, byte[] byteArray, MediaType contentType) {
        return formBody().addFormDataPart(fileFormName, fileName, RequestBody.create(byteArray, contentType));
    }

    public MultipartBody.Builder formBody(String fileFormName, String fileName, byte[] byteArray, String contentType) {
        return formBody(fileFormName, fileName, byteArray, MediaType.get(contentType));
    }

    public MultipartBody.Builder formBody(String fileFormName, String fileName, byte[] byteArray) {
        return formBody(fileFormName, fileName, byteArray, TYPE_DEFAULT_FILE);
    }

    /**
     * Send a request and enqueue a callback to handle the response.
     *
     * @param request
     * @param callback
     */
    public void sendRequest(Request request, Callback callback) {
        http.newCall(request).enqueue(callback);
    }

    /**
     * An abstract class for a Builder, add a build() method specific to your child class.
     */
    protected abstract static class Builder<T extends Builder<T>> {
        protected String baseURL = "";
        protected String TAG = DEFAULT_TAG;
        protected int logLevel = DEFAULT_LOG_LEVEL;
        protected OkHttpClient.Builder okClientBuilder = null;

        abstract protected T getThis();

        public T setBaseUrl(String url) {
            baseURL = url;
            return getThis();
        }

        public T setTag(String tag) {
            TAG = tag;
            return getThis();
        }

        public T setLogLevel(int level) {
            logLevel = level;
            return getThis();
        }

        public T setClientBuilder (OkHttpClient.Builder clientBuilder) {
            okClientBuilder = clientBuilder;
            return getThis();
        }

        public static OkHttpClient.Builder okBuilder () { return new OkHttpClient.Builder(); }

        public OkHttpClient.Builder clientBuilder() {
            if (okClientBuilder == null) {
                setClientBuilder(okBuilder());
            }
            return okClientBuilder;
        }
    }

}
