package com.platform.middlewares;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.breadwallet.BreadApp;
import com.platform.APIClient;
import com.platform.BRHTTPHelper;
import com.platform.HTTPServer;
import com.platform.interfaces.Middleware;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import timber.log.Timber;
public class HTTPIndexMiddleware implements Middleware {

    @Override
    public boolean handle(String target, org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
        Timber.d("handling: " + target + " " + baseRequest.getMethod());
        Context app = BreadApp.getBreadContext();
        if (app == null) {
            Timber.i("handle: app is null!");
            return true;
        }

        String indexFile = APIClient.getInstance(app).getExtractedPath(app, rTrim(target, "/") + "/index.html");

        File temp = new File(indexFile);
        if (!temp.exists()) {
            return false;
        }

        try {
            byte[] body = FileUtils.readFileToByteArray(temp);
            Assert.assertNotNull(body);
            Assert.assertNotSame(body.length, 0);
            response.setHeader("Content-Length", String.valueOf(body.length));
            return BRHTTPHelper.handleSuccess(200, body, baseRequest, response, "text/html;charset=utf-8");
        } catch (IOException e) {
            Timber.e(e, "handle: error sending response: ");
            return BRHTTPHelper.handleError(500, null, baseRequest, response);
        }
    }

    public String rTrim(String str, String piece) {
        if (str.endsWith(piece)) {
            return str.substring(str.lastIndexOf(piece), str.length());
        }
        return str;
    }
}
