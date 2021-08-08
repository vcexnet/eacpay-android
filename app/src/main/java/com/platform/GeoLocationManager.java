package com.platform;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;

import com.eacpay.EacApp;
import com.eacpay.tools.threads.BRExecutor;
import com.eacpay.tools.util.Utils;

import org.eclipse.jetty.continuation.Continuation;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import timber.log.Timber;
public class GeoLocationManager {
    private Session session;
    private Continuation continuation;
    private Request baseRequest;
    private LocationManager locationManager;

    private static GeoLocationManager instance;

    public static GeoLocationManager getInstance() {
        if (instance == null) instance = new GeoLocationManager();
        return instance;
    }

    public void getOneTimeGeoLocation(Continuation cont, Request req) {
        this.continuation = cont;
        this.baseRequest = req;
        final Context app = EacApp.getBreadContext();
        if (app == null)
            return;
        locationManager = (LocationManager) app.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) {
            Timber.i("getOneTimeGeoLocation: locationManager is null!");
            return;
        }
        BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(app, Manifest.permission.ACCESS_FINE_LOCATION) !=
                        PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(app, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Timber.e(new RuntimeException("getOneTimeGeoLocation, can't happen"));
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        });
    }

    void startGeoSocket(Session sess) {
        session = sess;

        final Context app = EacApp.getBreadContext();
        if (app == null)
            return;
        final LocationManager locationManager = (LocationManager) app.getSystemService(Context.LOCATION_SERVICE);

        BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(app, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(app, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Timber.e(new RuntimeException("startGeoSocket, can't happen"));
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, socketLocationListener);
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, socketLocationListener);
            }
        });
    }

    public void stopGeoSocket() {
        final Context app = EacApp.getBreadContext();
        if (app == null)
            return;
        final LocationManager locationManager = (LocationManager) app.getSystemService(Context.LOCATION_SERVICE);
        BRExecutor.getInstance().forMainThreadTasks().execute(new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(app, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(app, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    RuntimeException ex = new RuntimeException("stopGeoSocket, can't happen");
                    Timber.e(ex);
                    throw ex;
                }
                locationManager.removeUpdates(socketLocationListener);
            }
        });
    }

    // Define a listener that responds to location updates
    private LocationListener socketLocationListener = new LocationListener() {
        private boolean sending;

        public void onLocationChanged(Location location) {
            // Called when a new location is found by the network location provider.
            if (sending) return;
            sending = true;
            if (session != null && session.isOpen()) {
                final String jsonLocation = getJsonLocation(location);
                BRExecutor.getInstance().forBackgroundTasks().execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            session.getRemote().sendString(jsonLocation);
                        } catch (IOException e) {
                            Timber.e(e);
                        } finally {
                            sending = false;
                        }
                    }
                });
            } else {
                sending = false;
            }
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };

    private LocationListener locationListener = new LocationListener() {
        private boolean processing;

        public void onLocationChanged(final Location location) {
            if (processing) return;
            processing = true;
            BRExecutor.getInstance().forBackgroundTasks().execute(new Runnable() {
                @Override
                public void run() {
                    // Called when a new location is found by the network location provider.
                    if (continuation != null && baseRequest != null) {
                        String jsonLocation = getJsonLocation(location);
                        try {
                            if (!Utils.isNullOrEmpty(jsonLocation)) {
                                try {
                                    ((HttpServletResponse) continuation.getServletResponse()).setStatus(200);
                                    continuation.getServletResponse().getOutputStream().write(jsonLocation.getBytes("UTF-8"));
                                    baseRequest.setHandled(true);
                                    continuation.complete();
                                    continuation = null;
                                } catch (IOException e) {
                                    Timber.e(e);
                                }
                            } else {
                                try {
                                    ((HttpServletResponse) continuation.getServletResponse()).sendError(500);
                                    baseRequest.setHandled(true);
                                    continuation.complete();
                                    continuation = null;
                                } catch (IOException e) {
                                    Timber.e(e);
                                }
                                Timber.e(new NullPointerException("onLocationChanged: " + jsonLocation));
                            }
                        } catch (Exception e) {
                            Timber.e(e);
                        } finally {
                            processing = false;
                            Context app = EacApp.getBreadContext();
                            if (app == null || ActivityCompat.checkSelfPermission(app, Manifest.permission.ACCESS_FINE_LOCATION)
                                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(app,
                                    Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                Timber.d("onLocationChanged: PERMISSION DENIED for removeUpdates");
                            } else {
                                locationManager.removeUpdates(locationListener);
                            }
                        }
                    }
                }
            });
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        public void onProviderEnabled(String provider) {
        }

        public void onProviderDisabled(String provider) {
        }
    };

    public static String getJsonLocation(Location location) {
        try {
            JSONObject responseJson = new JSONObject();

            JSONObject coordObj = new JSONObject();
            coordObj.put("latitude", location.getLatitude());
            coordObj.put("longitude", location.getLongitude());

            responseJson.put("timestamp", location.getTime());
            responseJson.put("coordinate", coordObj);
            responseJson.put("altitude", location.getAltitude());
            responseJson.put("horizontal_accuracy", location.getAccuracy());
            responseJson.put("description", "");
            return responseJson.toString();
        } catch (JSONException e) {
            Timber.e(e);
        }
        return null;

    }

}