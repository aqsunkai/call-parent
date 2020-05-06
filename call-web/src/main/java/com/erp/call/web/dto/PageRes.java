package com.erp.call.web.dto;

import java.util.List;

public class PageRes {
    private Boolean running;

    private List<String> failName;

    private List<String> uploadName;

    public Boolean isRunning() {
        return running;
    }

    public void setRunning(Boolean running) {
        this.running = running;
    }

    public List<String> getFailName() {
        return failName;
    }

    public void setFailName(List<String> failName) {
        this.failName = failName;
    }

    public List<String> getUploadName() {
        return uploadName;
    }

    public void setUploadName(List<String> uploadName) {
        this.uploadName = uploadName;
    }
}
