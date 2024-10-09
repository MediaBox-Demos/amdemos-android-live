package com.alivc.live.baselive_pull.bean.rmsbean;

/**
 * Auto-generated: 2024-08-06 14:42:42
 */
public class RMSStream {

    private String uid;
    private int paneid;
    private int zorder;
    private int x;
    private int y;
    private int w;
    private int h;
    private int type;
    private int status;
    private int cameraDisabled;
    private int muted;
    private int vol;
    private int vad;
    private int netQuality;

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUid() {
        return uid;
    }

    public void setPaneid(int paneid) {
        this.paneid = paneid;
    }

    public int getPaneid() {
        return paneid;
    }

    public void setZorder(int zorder) {
        this.zorder = zorder;
    }

    public int getZorder() {
        return zorder;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getX() {
        return x;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getY() {
        return y;
    }

    public void setW(int w) {
        this.w = w;
    }

    public int getW() {
        return w;
    }

    public void setH(int h) {
        this.h = h;
    }

    public int getH() {
        return h;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

    public void setCameraDisabled(int cameraDisabled) {
        this.cameraDisabled = cameraDisabled;
    }

    public int getCameraDisabled() {
        return cameraDisabled;
    }

    public void setMuted(int muted) {
        this.muted = muted;
    }

    public int getMuted() {
        return muted;
    }

    public void setVol(int vol) {
        this.vol = vol;
    }

    public int getVol() {
        return vol;
    }

    public void setVad(int vad) {
        this.vad = vad;
    }

    public int getVad() {
        return vad;
    }

    public void setNetQuality(int netQuality) {
        this.netQuality = netQuality;
    }

    public int getNetQuality() {
        return netQuality;
    }

    @Override
    public String toString() {
        return "Stream{" +
                "uid='" + uid + '\'' +
                ", paneid=" + paneid +
                ", zorder=" + zorder +
                ", x=" + x +
                ", y=" + y +
                ", w=" + w +
                ", h=" + h +
                ", type=" + type +
                ", status=" + status +
                ", cameraDisabled=" + cameraDisabled +
                ", muted=" + muted +
                ", vol=" + vol +
                ", vad=" + vad +
                ", netQuality=" + netQuality +
                '}';
    }
}