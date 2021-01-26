package com.spilab.percom21.demo;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DemoUtils {

    public static Date beginDate;
    public static Date endDate;

    static {
        try {
//            beginDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2020-01-24 03:00:28");
//            endDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2020-01-26 23:32:28");

            beginDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2020-02-19 03:00:28");
            endDate = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse("2020-02-21 23:32:28");

        } catch (ParseException e) {
            e.printStackTrace();
        }
    }


}
