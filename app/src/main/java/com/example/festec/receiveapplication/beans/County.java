package com.example.festec.receiveapplication.beans;

import org.litepal.crud.DataSupport;

public class County extends DataSupport {
    private int id;

    private String countryname;

    private int countrycode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getCountryname() {
        return countryname;
    }

    public void setCountryname(String countryname) {
        this.countryname = countryname;
    }

    public int getCountrycode() {
        return countrycode;
    }

    public void setCountrycode(int countrycode) {
        this.countrycode = countrycode;
    }

}

