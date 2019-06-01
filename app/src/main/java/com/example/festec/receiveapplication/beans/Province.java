package com.example.festec.receiveapplication.beans;


import org.litepal.crud.DataSupport;

public class Province extends DataSupport {
    private int id;

    private String provinvename;

    private int provincecode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProvinvename() {
        return provinvename;
    }

    public void setProvinvename(String provinvename) {
        this.provinvename = provinvename;
    }

    public int getProvincecode() {
        return provincecode;
    }

    public void setProvincecode(int provincecode) {
        this.provincecode = provincecode;
    }




}
