package com.samagra.commons;
import java.util.Date;

public class InternetStatus {

    public InternetStatus(boolean currentStatus, Date lastChangeTimestamp) {
        this.currentStatus = currentStatus;
        this.lastChangeTimestamp = lastChangeTimestamp;
    }

    public boolean isCurrentStatus() {
        return currentStatus;
    }

    public void setCurrentStatus(boolean currentStatus) {
        this.currentStatus = currentStatus;
    }

    public Date getLastChangeTimestamp() {
        return lastChangeTimestamp;
    }

    public void setLastChangeTimestamp(Date lastChangeTimestamp) {
        this.lastChangeTimestamp = lastChangeTimestamp;
    }

    boolean currentStatus;
    Date lastChangeTimestamp;
}
