/**
 * Heatmap API
 * Esta api nos proporcionará las posiciones de los distintos dispositivos conectados para generar un mapa de calor.
 * <p>
 * OpenAPI spec version: 1.0
 * <p>
 * <p>
 * NOTE: This class is auto generated by OpenAPI Generator (https://openapi-generator.tech).
 * https://openapi-generator.tech
 * Do not edit the class manually.
 */

package com.spilab.percom21.resource;


import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.spilab.percom21.locationmanager.LocationManager;
import com.spilab.percom21.model.LocationFrequency;
import com.spilab.percom21.response.HeatMapResponse;
import com.spilab.percom21.service.MQTTService;
import com.spilab.percom21.service.MqttClient;


import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class HeatMapResource {

    private Context context;
    private RequestQueue request;

    private HeatMapResponse mapResponse;

    private Gson gson;
    private MqttClient client;


    public HeatMapResource(Context context) {
        this.context = context;
        //request = Volley.newRequestQueue(context);
        gson =  new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();
        client = new MqttClient();
    }

    public Exception executeMethod(HeatMapResponse response) throws MqttException, UnsupportedEncodingException {
        mapResponse = response;


        switch (response.getMethod()) {
            case "getHeatmaps":
                getHeatmaps(response.getParams().getbeginDate(), response.getParams().getendDate(), response.getParams().getXMin(), response.getParams().getXMax(), response.getParams().getYMin(), response.getParams().getYMax());
                break;
                default:
                client.publishMessage( MQTTService.getClient(), "Error: Not Found Method",1,mapResponse.getSender());
                return new Exception("Not found method.");

//            case "getSCHeatmaps":
//                getSCHeatmaps(response.getParams().getbeginDate(),response.getParams().getendDate(),response.getParams().getlatitude(),response.getParams().getlongitude(),response.getParams().getradius(),response.getParams().getdevices());
//                break;
        }

        return null;
    }


    public void getHeatmaps(Date beginDate, Date endDate, Double xmin, Double xmax, Double ymin, Double ymax) {


        List<LocationFrequency> locations = LocationManager.getLocationHistoryV2(beginDate, endDate, xmin, xmax, ymin, ymax);

        try {
            sendReply(mapResponse.getSender(), mapResponse.getIdRequest(), new JSONArray(gson.toJson(locations)));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

//    /**
//     * Get the locations frequency processed in the aggregator
//     *
//     * @param beginDate init date
//     * @param endDate end date
//     * @param latitude latitude
//     * @param longitude longitude
//     * @param radius radius
//     * @param devices number of devices
//     * @return List<LocationFrequency>
//     */
//    public List<LocationFrequency> getSCHeatmaps(Date beginDate, Date endDate, Double latitude, Double longitude, Double radius, Integer devices) {
//
//        //new GetLocations().execute();
//
//        List<LocationBeanRealm> locations = LocationManager.getLocationsFilter(beginDate, endDate);
//        Log.e("Locations size", String.valueOf(locations.size()));
//        try {
//            sendReply(mapResponse.getSender(), mapResponse.getIdRequest(), new JSONArray(gson.toJson(locations)), devices);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//
//        return null;
//    }

    private void sendReply(String url, String idRequest, JSONArray list) throws MqttException, UnsupportedEncodingException{


        JSONObject content = null;
        try {

            content = new JSONObject();
            content.put("idRequest", idRequest);
            content.put("body", list);


        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.e("LocationFrequency", String.valueOf(content));

        client.publishMessage( MQTTService.getClient(), String.valueOf(content),1,mapResponse.getSender());


//        jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, content, new Response.Listener<JSONObject>() {
//            @Override
//            public void onResponse(JSONObject response) {
//                Log.d("OK: ", String.valueOf(response));
//
//            }
//        }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                Toast.makeText(context, error.toString(), Toast.LENGTH_LONG).show();
//                System.out.println();
//                Log.d("ERROR: ", error.toString());
//            }
//        }
//        );
//
//        request.add(jsonObjectRequest);

    }

    private static <T> List<List<T>> partition(List<T> input, int size) {
        List<List<T>> lists = new ArrayList<List<T>>();
        for (int i = 0; i < input.size(); i += size) {
            lists.add(input.subList(i, Math.min(input.size(), i + size)));
        }
        return lists;
    }


    private void enviarRespuesta(String url, String id, JSONArray array) {
        JsonObjectRequest jsonObjectRequest = null;

        String jString = "{\n" +
                "                \"date\": \"bbbb\",\n" +
                "                \"latitude\": 39.4786221,\n" +
                "                \"longitude\": -6.3419389\n" +
                "            }";

        JSONObject content = null;
        JSONObject body = null;
        try {
            body = new JSONObject();
            body.put("personaX", array);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            content = new JSONObject();
            content.put("idRequest", id);
            content.put("body", body);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        jsonObjectRequest = new JsonObjectRequest(Request.Method.POST, url, content, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                Log.d("OK: ", String.valueOf(response));

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, error.toString(), Toast.LENGTH_LONG).show();
                System.out.println();
                Log.d("ERROR: ", error.toString());
            }
        }
        );


        request.add(jsonObjectRequest);
    }


}
