package com.spilab.percom21;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.spilab.percom21.database.LocationBeanRealm;
import com.spilab.percom21.database.LocationBeanRealmModule;
import com.spilab.percom21.service.MQTTService;
import com.spilab.percom21.service.MqttClient;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;


public class MainActivity extends AppCompatActivity /*implements OnMapReadyCallback*/ {


    private static final String TYPE_UTF8_CHARSET = "UTF-8";
    private Date currentTime;
    private boolean flag = false;
    private static final String TAG = "Location";
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;
    private static final int STORAGE_PERMISSION_REQUEST_CODE = 4321;

    private Boolean mLocationPermissionsGranted = false;
    private Boolean storagePermissionsGranted = false;


    private Button buttonSendRequest;
    public static TextView percentageRisk;


    private static final String CARPETA_RAIZ = "HeatmapV3/";



    Intent mServiceIntent;

    private Gson gson;

    private static String serverIp;

    private boolean startRealm = false;


    private MqttClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonSendRequest=(Button) findViewById(R.id.buttonSendRequest);
        percentageRisk=(TextView) findViewById(R.id.textPercentage);
        client = new MqttClient();

        buttonSendRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                //SEND REQUEST

                JSONObject content = null;
                JSONObject params = null;
                try {
                    content = new JSONObject();
                    params = new JSONObject();
                    params.put("beginDate","2020-01-24T04:00:28Z");
                    params.put("endDate","2020-01-26T23:32:28Z");
                    params.put("xmin","37.342856");
                    params.put("xmax","37.411237");
                    params.put("ymin","-6.004290");
                    params.put("ymax","-5.947577");



                    content.put("resource", "Map");
                    content.put("method", "getHeatmaps");
                    content.put("sender", MQTTService.getId());
                    content.put("params", params);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                try {
                    client.publishMessage( MQTTService.getClient(), String.valueOf(content),1,"Covid19PERCOM/request");
                } catch (MqttException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
        });



        getStoragePermission();
        startServiceMQTT();
        gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();


        /////// PARSE LOCALIZACIONES ESTATICAS //////////////
        ArrayList<LocationBeanRealm> localizaciones = new ArrayList<>();

        //GET FICHERO SIMULACION

        String simulationName = getIntent().getStringExtra("param");
        String result;
        if (simulationName == null) {

            Log.e("Error: ", "No se ha introducido ningún nombre para la simulacion");
            result = loadJSONFromAsset("locs.json");
            Log.e("SIMULATION NAME: ", "locs");
            Toast toast1 = Toast.makeText(this, "SIMULATION NAME: DEFAULT", Toast.LENGTH_LONG);
            toast1.show();
        } else {
            Log.e("SIMULATION NAME: ", simulationName);
            Toast toast1 = Toast.makeText(this, "SIMULATION NAME: "+ simulationName, Toast.LENGTH_LONG);
            toast1.show();

            result = loadJSONFromAsset(simulationName + ".json");
        }
        localizaciones = gson.fromJson(result, new TypeToken<List<LocationBeanRealm>>() {
        }.getType());

        Log.e("LISTA LOCALIZACIONES: ", String.valueOf(localizaciones.size()));

        //TODO: Guardar localizaciones en la base de datos.
        /////////////////////////////////////////////////////////
        guardarLocsStaticas(localizaciones);

        // check location permission
        if (storagePermissionsGranted && mLocationPermissionsGranted) {
             startServiceMQTT();

            Log.e("Start", " START SERVICE");
        } else {
            Log.e("Permisos:", "No tiene todos los permisos activos");
        }

    }





    public void restartAPP(Double timeout){
        Intent mStartActivity = new Intent(getApplicationContext(), MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(getApplicationContext(), mPendingIntentId,mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager)getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, (long) (System.currentTimeMillis() + timeout), mPendingIntent);
        System.exit(0);
    }






    public String loadJSONFromAsset(String filename) {
        String json = null;
        try {
            InputStream is = getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }


    private void writeFileExternalStorage(Double lat, Double lng) {

        try {
            File myExternalFile = new File(Environment.getExternalStorageDirectory(), CARPETA_RAIZ);

            if (!myExternalFile.exists())
                myExternalFile.mkdir();

            String request= "new google.maps.LatLng("+lat+","+lng+"),\n";


            Writer output;
            output = new BufferedWriter(new FileWriter(myExternalFile+ File.separator + "example.txt",true));  //clears file every time
            output.append(request);
            output.close();

            Log.d(TAG, " - File WRITED successfully");

        } catch (IOException e) {
            Log.d(TAG, " - Error LOADED file");
            e.printStackTrace();
        }

    }

    public void guardarLocsStaticas(ArrayList<LocationBeanRealm> localizaciones) {

        // To start Realm once
        if (!this.startRealm) {
            Log.i("HEATMAP-INIT", "Starting Realm...");
            try {
                Realm.init(this);

                RealmConfiguration realmConfiguration = new RealmConfiguration.Builder()
                        .modules(new LocationBeanRealmModule())
                        .name("Database.realm")
                        .deleteRealmIfMigrationNeeded()
                        .build();
                Realm.deleteRealm(realmConfiguration);

                Realm.setDefaultConfiguration(realmConfiguration);
                this.startRealm = true;
                Log.i("HEATMAP-INIT", "Realm started successfully");
            } catch (Exception e) {
                Log.e("HEATMAP-INIT", "Error during start Realm: " + e.getMessage());
            }

        }

        // Store the location in the Realm database
        Realm realm = Realm.getDefaultInstance();

        for (int i = 0; i < localizaciones.size(); i++) {

            //writeFileExternalStorage(localizaciones.get(i).getLat(),localizaciones.get(i).getLng());

            realm.beginTransaction();
            LocationBeanRealm lbr = realm.createObject(LocationBeanRealm.class);
            lbr.setLat(localizaciones.get(i).getLat());
            lbr.setLng(localizaciones.get(i).getLng());
            lbr.setTimestamp(localizaciones.get(i).getTimestamp());
            realm.commitTransaction();

        }



    }

    private void startServiceMQTT() {
        //serverIp = MQTTConfiguration.MQTT_BROKER_URL.split("//")[1].split(":")[0];
        // Stopping service if running
        Log.i("MQTT", " Start mqtt service");
        MQTTService service = new MQTTService();
        //MqttMessageService service = new MqttMessageService();
        mServiceIntent = new Intent(this, service.getClass());


       // mServiceIntent.putExtra("profile", profile);

        boolean run = isMyServiceRunning(service.getClass());
          Log.d(TAG, " - Run1: " + run);
          if (!isMyServiceRunning(service.getClass())) {
              //mServiceIntent.putExtra("profile", profile);
            startService(mServiceIntent);

          }
          Log.d(TAG, " - Run1: " + run);

    }







    private void getStoragePermission() {
        Log.d(TAG, "getStoragePermission: getting storage permissions");
        String[] permissionsStorage = {Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            storagePermissionsGranted = true;

        } else {
            ActivityCompat.requestPermissions(this,
                    permissionsStorage,
                    STORAGE_PERMISSION_REQUEST_CODE);
        }


    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: called.");

        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE: {
                mLocationPermissionsGranted = false;
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            getStoragePermission();
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    mLocationPermissionsGranted = true;
                    getStoragePermission();
                }
            }

            case STORAGE_PERMISSION_REQUEST_CODE: {
                storagePermissionsGranted = false;
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            storagePermissionsGranted = false;
                            Log.d(TAG, "onRequestPermissionsResult: permission failed");
                            return;
                        }
                    }
                    Log.d(TAG, "onRequestPermissionsResult: permission granted");
                    storagePermissionsGranted = true;

                }
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(broadcastReceiver, new IntentFilter("RESTART"));

    }

    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(MainActivity.this).registerReceiver(broadcastReceiver, new IntentFilter("RESTART"));

    }


    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Double timeout = (Double) intent.getExtras().get("timeout");
            restartAPP(timeout);
        }
    };
    //TODO: cAMBIAR PARA QUE VUELVA A PINTAR



    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                Log.i("Service status", "Running");
                return true;
            }
        }
        Log.i("Service status", "Not running");
        return false;
    }




}
