package com.platform.middlewares;

import com.platform.interfaces.Middleware;
import com.platform.interfaces.Plugin;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import timber.log.Timber;
public class HTTPRouter implements Middleware {
    Set<Plugin> plugins;

    public HTTPRouter() {
        plugins = new LinkedHashSet<>();
    }

    @Override
    public boolean handle(String target, org.eclipse.jetty.server.Request baseRequest, HttpServletRequest request, HttpServletResponse response) {
        for (Plugin plugin : plugins) {
            boolean success = plugin.handle(target, baseRequest, request, response);
            if (success) {
                Timber.d("plugin: " + plugin.getClass().getName().substring(plugin.getClass().getName().lastIndexOf(".") + 1) + " succeeded:" + request.getRequestURL());
                return true;
            }
        }
        return false;
    }

    public void appendPlugin(Plugin plugin) {
        plugins.add(plugin);
    }
}
