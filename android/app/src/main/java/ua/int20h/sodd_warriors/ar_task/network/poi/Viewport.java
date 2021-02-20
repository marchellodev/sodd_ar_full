package ua.int20h.sodd_warriors.ar_task.network.poi;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Viewport {

    @SerializedName("northeast")
    @Expose
    private Northeast northeast;
    @SerializedName("southwest")
    @Expose
    private Southwest southwest;

    public void setNortheast(Northeast northeast) {
        this.northeast = northeast;
    }

    public void setSouthwest(Southwest southwest) {
        this.southwest = southwest;
    }

}
