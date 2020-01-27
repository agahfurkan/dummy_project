package com.example.fd.wifisignal;


/**
 * Created by fd on 1/10/2018.
 */

public class WifiModel implements Comparable {
    private String ssid;
    private String level;
    private String bssid;
    private int freq;

    String getBssid() {
        return bssid;
    }

    void setBssid(String bssid) {
        this.bssid = bssid;
    }

    int getFreq() {
        return freq;
    }

    void setFreq(int freq) {
        this.freq = freq;
    }

    String getSsid() {
        return ssid;
    }

    void setSsid(String ssid) {
        this.ssid = ssid;
    }

    String getLevel() {
        return level;
    }

    void setLevel(String level) {
        this.level = level;
    }

    @Override
    public int compareTo(Object o) {
        int compareVal = Integer.valueOf(((WifiModel) o).getLevel());
        return compareVal - Integer.valueOf(level);
    }
}
