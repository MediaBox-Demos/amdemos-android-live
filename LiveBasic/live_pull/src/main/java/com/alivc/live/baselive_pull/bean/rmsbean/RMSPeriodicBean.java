package com.alivc.live.baselive_pull.bean.rmsbean;

import java.util.List;

/**
 * Auto-generated: 2024-08-06 14:42:42
 */
public class RMSPeriodicBean {

    private RMSCanvas canvas;
    private List<RMSStream> stream;
    private String source;
    private String ver;
    private long ts;

    public void setCanvas(RMSCanvas canvas) {
        this.canvas = canvas;
    }

    public RMSCanvas getCanvas() {
        return canvas;
    }

    public void setStream(List<RMSStream> stream) {
        this.stream = stream;
    }

    public List<RMSStream> getStream() {
        return stream;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSource() {
        return source;
    }

    public void setVer(String ver) {
        this.ver = ver;
    }

    public String getVer() {
        return ver;
    }

    public void setTs(long ts) {
        this.ts = ts;
    }

    public long getTs() {
        return ts;
    }

    @Override
    public String toString() {
        return "JsonRootBean{" +
                "canvas=" + canvas +
                ", stream=" + stream +
                ", source='" + source + '\'' +
                ", ver='" + ver + '\'' +
                ", ts=" + ts +
                '}';
    }
}