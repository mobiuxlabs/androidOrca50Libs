package com.zebra.model;


import com.zebra.util.AppUtils;

/**
 * Created by SUJEET KUMAR on 21-May-21.
 */
public class Barcode extends BaseModel {

    private String name;
    private String hex;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHex() {
        hex = AppUtils.generateHexEPC(getName());
        return hex;
    }

    public void setHex(String hex) {
        this.hex = hex;
    }

    @Override
    public String toString() {
        return getName();
    }
}
