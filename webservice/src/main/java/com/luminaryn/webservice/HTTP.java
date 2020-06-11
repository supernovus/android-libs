package com.luminaryn.webservice;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

/**
 * Simple wrapper for OkHttp.
 *
 * Not meant to be used on it's own, see JSON for a real implementation.
 */
abstract public class HTTP {
    public final OkHttpClient http;

    public final static int LOG_NONE     = 0;
    public final static int LOG_ERRORS   = 1;
    public final static int LOG_WARNINGS = 2;
    public final static int LOG_DEBUG    = 3;

    public static final MediaType TYPE_DEFAULT_FILE = MediaType.get("application/octet-stream");

    public String baseURL = "";
    public String TAG = "com.luminaryn.webservice";
    public int logLevel = LOG_NONE;

    public HTTP() {
        http = new OkHttpClient();
    }

    public void setBaseUrl(String url) {
        baseURL = url;
    }

    public void setTag(String tag) {
        TAG = tag;
    }

    public void setLogLevel(int level) {
        logLevel = level;
    }

    public static Handler getUIHandler() {
        return new Handler(Looper.getMainLooper());
    }

    public Request.Builder makeRequest(String uri) {
        String url = baseURL + uri;
        Request.Builder builder = new Request.Builder()
                .url(url);
        return builder;
    }

    public MultipartBody.Builder formBody() {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        return builder;
    }

    public MultipartBody.Builder formBody(String fileFormName, File file, MediaType contentType) {
        return formBody().addFormDataPart(fileFormName, file.getName(), RequestBody.create(file, contentType));
    }

    public MultipartBody.Builder formBody(String fileFormName, File file) {
        return formBody(fileFormName, file, TYPE_DEFAULT_FILE);
    }

    public void sendRequest(Request request, Callback callback) {
        http.newCall(request).enqueue(callback);
    }

    public static class FileDownloadCallback implements Callback {
        private String targetPath;
        public HTTP ws;
        public FileResponseHandler handler;

        FileDownloadCallback(String targetPath, HTTP ws) {
            this.targetPath = targetPath;
            this.ws = ws;
            this.handler = null;
        }

        FileDownloadCallback(String targetPath, HTTP ws, FileResponseHandler handler) {
            this.targetPath = targetPath;
            this.ws = ws;
            this.handler = handler;
        }

        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
            Log.e(ws.TAG, "Failure downloading file");
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response)
        {
            if (!response.isSuccessful()) return;
            try {
                File download = new File(targetPath);
                BufferedSink sink = Okio.buffer(Okio.sink(download));
                sink.writeAll(response.body().source());
                sink.close();
                if (handler != null) {
                    handler.handle(download);
                }
            } catch (IOException e) {
                Log.v(ws.TAG, "IOException occurred trying to save downloaded file.");
            }
        }
    }

    public interface FileResponseHandler {
        void handle(File file);
    }

    public static abstract class FileUIResponseHandler implements FileResponseHandler {
        public abstract Runnable setup(File file);
        public Handler getUIHandler() {
            return HTTP.getUIHandler();
        }
        public void handle(File file) {
            getUIHandler().post(this.setup(file));
        }
    }

}
