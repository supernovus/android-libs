package com.luminaryn.webservice;

import android.util.Log;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;

public class JSON extends HTTP {
    static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    public JSON() {
        super();
    }

    public JSON(String url) {
        super();
        baseURL = url;
    }

    public interface Handler {
        void handle (JSONObject response);
    }

    class JSONCallback implements Callback {
        private Handler handler;
        public JSON ws;

        JSONCallback(Handler handler, JSON ws) {
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

    public RequestBody getBody(JSONObject data) {
        return RequestBody.create(data.toString(), JSON);
    }

    public void GET(String uri, Handler handler) {
        httpRequest(getRequest(uri).get().build(), new JSONCallback(handler, this));
    }

    public void POST(String uri, JSONObject data, Handler handler) {
        httpRequest(getRequest(uri).post(getBody(data)).build(), new JSONCallback(handler, this));
    }

    public void PUT(String uri, JSONObject data, Handler handler) {
        httpRequest(getRequest(uri).put(getBody(data)).build(), new JSONCallback(handler, this));
    }

    public void DELETE(String uri, Handler handler) {
        httpRequest(getRequest(uri).delete().build(), new JSONCallback(handler, this));
    }

    public JSONObject errorMsg(String msg) {
        JSONObject json = new JSONObject();
        try {
            json.put("success", false);
            JSONArray errors = new JSONArray();
            if (msg.isEmpty()) {
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
