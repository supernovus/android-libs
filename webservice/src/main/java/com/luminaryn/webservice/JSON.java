package com.luminaryn.webservice;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A class for communicating with JSON-based web services.
 *
 * Can be used standalone, or (preferrably) use a sub-class to add your method calls.
 */
public class JSON extends HTTP {
    public static final MediaType TYPE_JSON = MediaType.get("application/json; charset=utf-8");

    public JSON() {
        super();
    }

    public JSON(String url) {
        super();
        baseURL = url;
    }
    
    public interface JSONResponseHandler extends ResponseHandler {
        void handle(JSONObject data);
    }

    public static abstract class JSONUIResponseHandler
            extends UIResponseHandler
            implements JSONResponseHandler
    {
        public abstract Runnable setup(JSONObject data);
    }
    
    public static class JSONCallback implements Callback {
        private JSONResponseHandler handler;
        public JSON ws;

        JSONCallback(JSONResponseHandler handler, JSON ws) {
            this.handler = handler;
            this.ws = ws;
        }

        @Override
        public void onFailure(@NotNull Call call, @NotNull IOException e) {
            handler.handle(ws.errorMsg("http_failure"));
        }

        @Override
        public void onResponse(@NotNull Call call, @NotNull Response response)
        {
            handler.handle(ws.jsonResponse(response));
        }
    }

    public RequestBody jsonBody(JSONObject data) {
        return RequestBody.create(data.toString(), TYPE_JSON);
    }

    public void GET(String uri, JSONResponseHandler handler) {
        sendRequest(makeRequest(uri).get().build(), new JSONCallback(handler, this));
    }

    public void POST(String uri, RequestBody data, JSONResponseHandler handler) {
        sendRequest(makeRequest(uri).post(data).build(), new JSONCallback(handler, this));
    }

    public void POST(String uri, JSONObject data, JSONResponseHandler handler) {
        POST(uri, jsonBody(data), handler);
    }

    public void POST(String uri, Map<String,Object> data, JSONResponseHandler handler) {
        POST(uri, new JSONObject(data), handler);
    }

    public void PUT(String uri, RequestBody data, JSONResponseHandler handler) {
        sendRequest(makeRequest(uri).put(data).build(), new JSONCallback(handler, this));
    }

    public void PUT(String uri, JSONObject data, JSONResponseHandler handler) {
        PUT(uri, jsonBody(data), handler);
    }

    public void PUT(String uri, Map<String,Object> data, JSONResponseHandler handler) {
        PUT(uri, new JSONObject(data), handler);
    }

    public void PATCH(String uri, RequestBody data, JSONResponseHandler handler) {
        sendRequest(makeRequest(uri).patch(data).build(), new JSONCallback(handler, this));
    }

    public void PATCH(String uri, JSONObject data, JSONResponseHandler handler) {
        PATCH(uri, jsonBody(data), handler);
    }

    public void PATCH(String uri, Map<String,Object> data, JSONResponseHandler handler) {
        PATCH(uri, new JSONObject(data), handler);
    }

    public void DELETE(String uri, JSONResponseHandler handler) {
        sendRequest(makeRequest(uri).delete().build(), new JSONCallback(handler, this));
    }

    public JSONObject errorMsg(String msg) {
        JSONObject json = new JSONObject();
        try {
            json.put("success", false);
            JSONArray errors = new JSONArray();
            if (msg.isEmpty()) {
                if (logLevel >= LOG_WARNINGS)
                    Log.d(TAG, "Empty message passed to errorMsg");
                errors.put("unknown_error");
            }
            else {
                errors.put(msg);
            }
            json.put("errors", errors);
        } catch (JSONException e)
        {
            if (logLevel >= LOG_ERRORS)
                Log.d(TAG, "JSON error when building error object, WTF?");

        }
        return json;
    }

    public JSONObject jsonResponse(Response response) {
        if (!response.isSuccessful()) { return errorMsg("http_status"); }
        try {
            String body = response.body().string();
            if (logLevel >= LOG_DEBUG)
                Log.d(TAG, "Response body: "+body);
            JSONObject json = new JSONObject(body);
            return json;
        }
        catch (IOException e) { return errorMsg("response_body_parsing"); }
        catch (JSONException e) { return errorMsg("json_parsing"); }
    }

}
