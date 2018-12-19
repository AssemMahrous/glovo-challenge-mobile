
package com.challenge.glovo.glovo.network.models;

import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class City {

    @SerializedName("working_area")
    @Expose
    private List<String> workingArea = null;
    @SerializedName("code")
    @Expose
    private String code;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("country_code")
    @Expose
    private String countryCode;
    private Boolean isShown = false;

    public List<String> getWorkingArea() {
        return workingArea;
    }

    public void setWorkingArea(List<String> workingArea) {
        this.workingArea = workingArea;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public Boolean getVisible() {
        return isShown;
    }

    public void setVisible(Boolean visible) {
        this.isShown = visible;
    }

    @NonNull
    @Override
    public String toString() {
        return name;
    }

}
