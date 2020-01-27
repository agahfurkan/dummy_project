package com.example.fd.wifisignal;

public class PointModel {
    private String pointName, x, y;

    public PointModel(String pointName, String x, String y) {
        this.pointName = pointName;
        this.x = x;
        this.y = y;
    }

    public String getPointName() {
        return pointName;
    }

    public void setPointName(String pointName) {
        this.pointName = pointName;
    }

    public String getX() {
        return x;
    }

    public void setX(String x) {
        this.x = x;
    }

    public String getY() {
        return y;
    }

    public void setY(String y) {
        this.y = y;
    }
}
