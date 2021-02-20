package ua.int20h.sodd_warriors.ar_task.network;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import ua.int20h.sodd_warriors.ar_task.network.geocode.Result;

import java.util.List;

public class GeocodeResponse {

    @SerializedName("results")
    @Expose
    private List<Result> results = null;
    @SerializedName("status")
    @Expose
    private String status;

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

}
