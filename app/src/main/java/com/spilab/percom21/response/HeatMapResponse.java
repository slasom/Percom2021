package com.spilab.percom21.response;


import com.google.gson.annotations.SerializedName;

import java.util.Date;


public class HeatMapResponse {

    @SerializedName("resource")
    private String resource;

    @SerializedName("method")
    private String method;

    @SerializedName("params")
    private Params params;

    @SerializedName("sender")
    private String sender;

    @SerializedName("idRequest")
    private String idRequest;

    public String getIdRequest() {
        return idRequest;
    }

    public void setIdRequest(String idRequest) {
        this.idRequest = idRequest;
    }

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Params getParams() {
        return params;
    }

    public void setParams(Params params) {
        this.params = params;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSender() {
        return sender;
    }

    public class Params {

        @SerializedName("beginDate")
        private Date beginDate;


        @SerializedName("endDate")
        private Date endDate;


        @SerializedName("xmin")
        private Double xmin;


        @SerializedName("xmax")
        private Double xmax;

        @SerializedName("ymin")
        private Double ymin;


        @SerializedName("ymax")
        private Double ymax;



        public Date getbeginDate() {
            return beginDate;
        }


        public Date getendDate() {
            return endDate;
        }


        public Double getXMin() { return xmin; }

        public Double getXMax() {
            return xmax;
        }

        public Double getYMin() {
            return ymin;
        }

        public Double getYMax() {
            return ymax;
        }







    }


}



