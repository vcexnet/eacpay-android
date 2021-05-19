package com.platform;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketError;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;

import timber.log.Timber;
@WebSocket
public class BRGeoWebSocketHandler {

    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        Timber.d("GeoSocketClosed: statusCode=%s, reason=%s", statusCode, reason);
        GeoLocationManager.getInstance().stopGeoSocket();
    }

    @OnWebSocketError
    public void onError(Throwable t) {
        Timber.e(t, "GeoSocketError");
        GeoLocationManager.getInstance().stopGeoSocket();
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        Timber.d("GeoSocketConnected: %s", session.getRemoteAddress().getAddress());
        GeoLocationManager.getInstance().startGeoSocket(session);
    }

    @OnWebSocketMessage
    public void onMessage(String message) {
        Timber.d("GeoSocketMessage: %s", message);
    }
}
