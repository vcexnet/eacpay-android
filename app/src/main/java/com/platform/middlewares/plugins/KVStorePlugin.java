package com.platform.middlewares.plugins;

import android.content.Context;

import com.eacpay.EacApp;
import com.platform.APIClient;
import com.platform.BRHTTPHelper;
import com.platform.interfaces.Plugin;
import com.platform.kvstore.CompletionObject;
import com.platform.kvstore.RemoteKVStore;
import com.platform.kvstore.ReplicatedKVStore;
import com.platform.sqlite.KVItem;

import org.eclipse.jetty.server.Request;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import timber.log.Timber;
public class KVStorePlugin implements Plugin {

    @Override
    public boolean handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
        if (target.startsWith("/_kv/")) {
            Timber.d("handling: " + target + " " + baseRequest.getMethod());
            String key = target.replace("/_kv/", "");
            Context app = EacApp.getBreadContext();
            if (app == null) {
                Timber.i("handle: context is null: " + target + " " + baseRequest.getMethod());
                return BRHTTPHelper.handleError(500, "context is null", baseRequest, response);
            }
            if (key.isEmpty()) {
                Timber.i("handle: missing key argument: " + target + " " + baseRequest.getMethod());
                return BRHTTPHelper.handleError(400, null, baseRequest, response);
            }

            RemoteKVStore remote = RemoteKVStore.getInstance(APIClient.getInstance(app));
            ReplicatedKVStore store = ReplicatedKVStore.getInstance(app, remote);
            switch (request.getMethod()) {
                case "GET":
                    Timber.d("handle: " + target + " " + baseRequest.getMethod() + ", key: " + key);
                    CompletionObject getObj = store.get(getKey(key), 0);
                    KVItem kv = getObj.kv;

                    if (kv == null || kv.deleted > 0) {
                        Timber.w("handle: kv store does not contain the kv: %s", key);
                        return BRHTTPHelper.handleError(404, null, baseRequest, decorateResponse(0, 0, response));
                    }
                    try {
                        JSONObject test = new JSONObject(new String(kv.value)); //just check for validity
                    } catch (JSONException e) {
                        Timber.e(e);
                        Timber.d("handle: the json is not valid: for key: " + key + ", " + target + " " + baseRequest.getMethod());
                        store.delete(getKey(key), kv.version);
                        return BRHTTPHelper.handleError(404, null, baseRequest, decorateResponse(kv.version, kv.time, response));
                    }

                    if (kv.deleted > 0) {
                        Timber.w("handle: the key is gone: " + target + " " + baseRequest.getMethod());
                        return BRHTTPHelper.handleError(410, "Gone", baseRequest, decorateResponse(kv.version, kv.time, response));
                    }
                    return BRHTTPHelper.handleSuccess(200, kv.value, baseRequest, decorateResponse(kv.version, kv.time, response), "application/json");
                case "PUT":
                    Timber.d("handle:" + target + " " + baseRequest.getMethod() + ", key: " + key);
                    // Read from request
                    byte[] rawData = BRHTTPHelper.getBody(request);

                    if (rawData == null) {
                        Timber.i("handle: missing request body: " + target + " " + baseRequest.getMethod());
                        return BRHTTPHelper.handleError(400, null, baseRequest, response);
                    }

                    String strVersion = request.getHeader("if-none-match");
                    if (strVersion == null) {
                        Timber.i("handle: missing If-None-Match header, set to `0` if creating a new key: " + target + " " + baseRequest.getMethod());
                        return BRHTTPHelper.handleError(400, null, baseRequest, response);
                    }
                    String ct = request.getHeader("content-type");
                    if (ct == null || !ct.equalsIgnoreCase("application/json")) {
                        Timber.i("handle: can only set application/json request bodies: " + target + " " + baseRequest.getMethod());
                        return BRHTTPHelper.handleError(400, null, baseRequest, response);
                    }

                    long version = Long.valueOf(strVersion);

                    CompletionObject setObj = store.set(new KVItem(version, 0, getKey(key), rawData, System.currentTimeMillis(), 0));
                    if (setObj.err != null) {
                        Timber.i("handle: error setting the key: " + key + ", err: " + setObj.err);
                        int errCode = transformErrorToResponseCode(setObj.err);
                        return BRHTTPHelper.handleError(errCode, null, baseRequest, response);
                    }

                    return BRHTTPHelper.handleSuccess(204, null, baseRequest, decorateResponse(setObj.version, setObj.time, response), null);
                case "DELETE":
                    Timber.d("handle: : " + target + " " + baseRequest.getMethod() + ", key: " + key);
                    strVersion = request.getHeader("if-none-match");
                    Timber.d("handle: missing If-None-Match header: " + target + " " + baseRequest.getMethod());

                    if (strVersion == null) {
                        Timber.d("handle: if-none-match is missing, sending 400");
                        return BRHTTPHelper.handleError(400, null, baseRequest, response);
                    }

                    CompletionObject delObj;
                    try {
                        delObj = store.delete(getKey(key), Long.parseLong(strVersion));
                    } catch (NumberFormatException e) {
                        Timber.e(e);
                        return BRHTTPHelper.handleError(500, null, baseRequest, response);
                    }
                    if (delObj == null || delObj.err != null) {
                        int err = 500;

                        if (delObj != null) {
                            Timber.d("handle: error deleting key: " + key + ", err: " + delObj.err);
                            err = transformErrorToResponseCode(delObj.err);
                        } else {
                            Timber.d("handle: error deleting key: " + key + ", delObj is null");
                        }
                        return BRHTTPHelper.handleError(err, null, baseRequest, response);
                    }
                    response.setHeader("ETag", String.valueOf(delObj.version));
                    response.addHeader("Cache-Control", "max-age=0, must-revalidate");
                    SimpleDateFormat dateFormat = new SimpleDateFormat(
                            "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
                    String rfc1123 = dateFormat.format(delObj.time);
                    response.setHeader("Last-Modified", rfc1123);
                    return BRHTTPHelper.handleSuccess(204, null, baseRequest, response, null);
            }
        }

        return false;
    }

    private HttpServletResponse decorateResponse(long ver, long time, HttpServletResponse response) {
        response.addHeader("Cache-Control", "max-age=0, must-revalidate");
        response.addHeader("ETag", String.valueOf(ver));

        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        String rfc1123 = dateFormat.format(time);
        response.setHeader("Content-Type", "application/json");
        response.addHeader("Last-Modified", rfc1123);
        return response;
    }

    private String getKey(String key) {
        if (key == null) Timber.d("getKey: key is null");
        return "plat-" + key;
    }

    private int transformErrorToResponseCode(CompletionObject.RemoteKVStoreError err) {
        switch (err) {
            case notFound:
                return 404;
            case conflict:
                return 409;
            default:
                Timber.d("transformErrorToResponseCode: unexpected error: " + err.name());
                return 500;
        }
    }

}
