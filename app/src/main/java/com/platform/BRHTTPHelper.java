package com.platform;


import com.eacpay.tools.util.Utils;

import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;

import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import timber.log.Timber;
public class BRHTTPHelper {
    public static final String TAG = BRHTTPHelper.class.getName();

    public static boolean handleError(int err, String errMess, Request baseRequest, HttpServletResponse resp) {
        try {
            baseRequest.setHandled(true);
            if (Utils.isNullOrEmpty(errMess))
                resp.sendError(err);
            else
                resp.sendError(err, errMess);
        } catch (IOException e) {
            Timber.e(e);
        }
        return true;
    }
//    return BRHTTPHelper.handleError(500, "context is null", baseRequest, response);
//    return BRHTTPHelper.handleSuccess(200, null, baseRequest, response, null);

    public static boolean handleSuccess(int code, byte[] body, Request baseRequest, HttpServletResponse resp, String contentType) {
        try {
            resp.setStatus(code);
            if (contentType != null && !contentType.isEmpty())
                resp.setContentType(contentType);
            if (body != null)
                resp.getOutputStream().write(body);
            baseRequest.setHandled(true);
        } catch (IOException e) {
            Timber.e(e);
        }
        return true;
    }

    public static byte[] getBody(HttpServletRequest request) {
        if (request == null) return null;
        byte[] rawData = null;
        try {
            InputStream body = request.getInputStream();
            rawData = IOUtils.toByteArray(body);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return rawData;
    }
}
