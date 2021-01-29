package com.spilab.percom21.demo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DemoUtils {

    public static Date beginDate;
    public static Date endDate;
    public static String deviceID;
    public static String idRequest;

    public static String getIdRequest() {
        return idRequest;
    }

    public static void setIdRequest(String idRequest) {
        DemoUtils.idRequest = idRequest;
    }

    public static String getDeviceID() {
        return deviceID;
    }

    public static void setDeviceID(String deviceID) {
        DemoUtils.deviceID = deviceID;
    }

    static {
        try {

            beginDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2020-01-24 03:00:28");
            endDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2020-01-26 23:32:28");

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


}
