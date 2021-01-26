package com.spilab.percom21.response;



import com.google.gson.annotations.SerializedName;




public class DeviceResponse {

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

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getIdRequest() {
        return idRequest;
    }

    public void setIdRequest(String idRequest) {
        this.idRequest = idRequest;
    }

    public class Params {
    
      
        @SerializedName("waittime")
        private Double waittime;



      
        public Double gettimeout() {
            return waittime;
        }


    }


}



