package com.luminaryn.webservice;

import android.os.Handler;
import android.util.Log;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;
import okio.BufferedSink;
import okio.Okio;

/**
 * This is a static utility class. It does nothing on it's own
 * except provide some extra classes providing a simple callback
 * interface for handling file downloads. This requires that we have
 * WRITE access to the target location!
 *
 * Example usage in your web service class:
 *
 * public void downloadFile(String url, String filepath, Download.FilesResponseHandler handler) {
 *     sendRequest(new Request.Builder().url(url).build(),
 *      new Download.FileCallback(filepath, this, handler));
 * }
 *
 * public void downloadFile(String url, String filepath) {
 *     sendRequest(new Request.Builder().url(url).build(),
 *      new Download.FileCallback(filepath, this));
 * }
 *
 */
public class Download {
    public static class FileCallback implements Callback {
        private String targetPath;
        public HTTP ws;
        public FileResponseHandler handler;

        public FileCallback(String targetPath, HTTP ws) {
            this.targetPath = targetPath;
            this.ws = ws;
            this.handler = null;
        }

        public FileCallback(String targetPath, HTTP ws, FileResponseHandler handler) {
            this.targetPath = targetPath;
            this.ws = ws;
            this.handler = handler;
        }

        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
            Log.e(ws.TAG, "Failure downloading file: "+e.getMessage());
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
            } catch (Exception e) {
                Log.v(ws.TAG, "Exception occurred trying to save downloaded file: "+e.getMessage());
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
