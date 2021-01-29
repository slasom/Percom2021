package com.spilab.percom21.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.spilab.percom21.MainActivity;
import com.spilab.percom21.R;
import com.spilab.percom21.demo.DemoUtils;
import com.spilab.percom21.locationmanager.LocationManager;
import com.spilab.percom21.model.LocationFrequency;
import com.spilab.percom21.resource.HeatMapResource;
import com.spilab.percom21.response.HeatMapResponse;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Type;
import java.util.List;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;


public class MQTTService extends Service {

    private static final String TAG = "MqttMessageService";
    private MqttClient mqttClient;
    private static MqttAndroidClient mqttAndroidClient;
    public static Boolean subscribed = false;

    //Client ID
    private static AdvertisingIdClient.Info mInfo;

    Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();

    public MQTTService() {
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate");

        mInfo = null;
        new GetAdvertisingID().execute();
        //configureMQTT();

    }

    public static MqttAndroidClient getClient() {
        return mqttAndroidClient;
    }

    public static String getId() {
        return mInfo.getId();
    }


    private void configureMQTT() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());

        mqttClient = new MqttClient();


        mqttAndroidClient = mqttClient.getMqttClient(getApplicationContext(), MQTTConfiguration.MQTT_BROKER_URL, mInfo.getId());

        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                if (!subscribed) {
                    subscribeTopic(getApplicationContext(), "Covid19PERCOMDevices");
                    subscribeTopic(getApplicationContext(), DemoUtils.getDeviceID());
                    subscribeTopic(getApplicationContext(), DemoUtils.getDeviceID() + "/request");
                    Log.d(TAG, "Subscribed to request");

                    //Change state on MainActivity TextView
                    Intent intentDevice = new Intent();
                    intentDevice.putExtra("state", true);
                    intentDevice.setAction("STATE");
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentDevice);

                }
            }

            @Override
            public void connectionLost(Throwable throwable) {
                Log.d(TAG, "Service connection lost");
                subscribeTopic(getApplicationContext(), "Covid19PERCOMDevices");
                subscribeTopic(getApplicationContext(), DemoUtils.getDeviceID());
                subscribeTopic(getApplicationContext(), DemoUtils.getDeviceID() + "/request");

                //Change state on MainActivity TextView
                Intent intentDevice = new Intent();
                intentDevice.putExtra("state", false);
                intentDevice.setAction("STATE");
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intentDevice);
            }

            @Override
            public void messageArrived(String s, MqttMessage mqttMessage) {
                Log.d(TAG, " - Message!! from " + s);

                // Parse message
                String msg = new String(mqttMessage.getPayload());


                if (s.equals(DemoUtils.getDeviceID())) {
                    JSONArray json = null;
                    try {
                        json = new JSONArray(msg);
                        Type listType = new TypeToken<List<LocationFrequency>>() {
                        }.getType();
                        List<LocationFrequency> list = gson.fromJson(String.valueOf(json), listType);

                        calculateRisk(list);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {

                    JSONObject json = null;
                    try {
                        json = new JSONObject(msg);
                        Log.i("Msg received by MQTT: ", json.toString());
                        executeAPI(json);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }

    private void calculateRisk(List<LocationFrequency> heatmapPositiveCovid) {

        int percentageRisk = 0;
        if (heatmapPositiveCovid.size() > 0) {

            List<LocationFrequency> locations = LocationManager.getLocationHistoryByDate(DemoUtils.beginDate, DemoUtils.endDate);
            List<LocationFrequency> result = LocationManager.matchesHeatmaps(locations, heatmapPositiveCovid);

            if (result.size() >= 10) {
                percentageRisk = 100;
                MainActivity.percentageRisk.setText(percentageRisk + "%");
                MainActivity.state.setText("Complete");
            } else {
                percentageRisk = result.size() * 10;
                MainActivity.percentageRisk.setText(percentageRisk + "%");
                MainActivity.state.setText("Complete");
            }


        }
            //SEND RESULT
            MainActivity.state.setText("Complete");
            JSONObject content = null;
            JSONObject body = null;
            try {
                content = new JSONObject();
                body = new JSONObject();
                ///S1 Y S2
                content.put("idRequest", DemoUtils.getIdRequest());
                body.put("riskPercentage", percentageRisk);
                content.put("body", body);

                MqttClient client = new MqttClient();
                Log.i("SENT RESULT: ", String.valueOf(content));
                client.publishMessage(MQTTService.getClient(), String.valueOf(content), 1, "Covid19PERCOM");

            } catch (JSONException e) {
                e.printStackTrace();
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (MqttException e) {
                e.printStackTrace();
            }


    }

    private void executeAPI(JSONObject data) throws JSONException {

        switch (data.getString("resource")) {

            case "Map":
                try {
                    HeatMapResponse mapresponse = gson.fromJson(String.valueOf(data), HeatMapResponse.class);

                    if (!mapresponse.getDevice().equals(DemoUtils.getDeviceID())) {
                        Exception error = new HeatMapResource(getApplicationContext()).executeMethod(mapresponse);

                        if (error != null) {
                            Log.e("Error", error.toString());
                            break;
                        }

                        showToastInIntentService("Resource Execution: " + mapresponse.getResource() + " | Method: " + mapresponse.getMethod());

                    }
                } catch (Exception e) {
                    Log.e("Err MapResponse", e.getMessage());
                }
                break;
        }
    }


    private class GetAdvertisingID extends AsyncTask<Void, Void, AdvertisingIdClient.Info> {

        @Override
        protected AdvertisingIdClient.Info doInBackground(Void... voids) {
            AdvertisingIdClient.Info info = null;
            try {
                info = AdvertisingIdClient.getAdvertisingIdInfo(getApplicationContext());
            } catch (IOException e) {
                e.printStackTrace();
            } catch (GooglePlayServicesNotAvailableException e) {
                e.printStackTrace();
            } catch (GooglePlayServicesRepairableException e) {
                e.printStackTrace();
            }
            return info;
        }

        @Override
        protected void onPostExecute(AdvertisingIdClient.Info info) {
            mInfo = info;
            configureMQTT();
        }
    }

    private void showToastInIntentService(final String sText) {
        final Context MyContext = this;

        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast toast1 = Toast.makeText(MyContext, sText, Toast.LENGTH_LONG);
                toast1.show();
            }
        });
    }

    ;


    private void startMyOwnForeground() {

        String NOTIFICATION_CHANNEL_ID = "com.spilab.percom21";
        String channelName = "Background Service";
        NotificationChannel chan = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);
        } else {


            NotificationCompat.Builder notificationBuilder = new
                    NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            Notification notification = notificationBuilder.setOngoing(true)
                    .setContentTitle(this.getString(R.string.app_name))
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            startForeground(2, notification);
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");

        Intent broadcastIntent = new Intent();
        broadcastIntent.setAction("restartservice");
        broadcastIntent.setClass(this, Restarter.class);
        this.sendBroadcast(broadcastIntent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void subscribeTopic(Context ctx, String topic) {
        if (!topic.isEmpty()) {
            try {
                mqttClient.subscribe(mqttAndroidClient, topic, 1);

                Toast.makeText(ctx, "Subscribed to: " + topic, Toast.LENGTH_SHORT).show();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }


}