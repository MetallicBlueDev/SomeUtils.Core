package com.metallicbluedev.dto;

import jakarta.xml.bind.annotation.*;

/**
 *
 * @author Sébastien Villemain
 */
@XmlRootElement(name = "AppSettings")
public class AppSettings {

    private boolean writeLogFile = true;
    private int logMaxBackup = 10;
    private int logMaxLength = 1572864;
    private boolean autoStartPerformanceMeasurement = false;
    private float performanceMeasurementWaitUserFactor = 1F;

    public int getLogMaxBackup() {
        return logMaxBackup;
    }

    public int getLogMaxLength() {
        return logMaxLength;
    }

    public float getPerformanceMeasurementWaitUserFactor() {
        return performanceMeasurementWaitUserFactor;
    }

    public boolean isAutoStartPerformanceMeasurement() {
        return autoStartPerformanceMeasurement;
    }

    public boolean isWriteLogFile() {
        return writeLogFile;
    }

    public void setAutoStartPerformanceMeasurement(boolean autoStartPerformanceMeasurement) {
        this.autoStartPerformanceMeasurement = autoStartPerformanceMeasurement;
    }

    public void setLogMaxBackup(int logMaxBackup) {
        this.logMaxBackup = logMaxBackup;
    }

    public void setLogMaxLength(int logMaxLength) {
        this.logMaxLength = logMaxLength;
    }

    public void setPerformanceMeasurementWaitUserFactor(float performanceMeasurementWaitUserFactor) {
        this.performanceMeasurementWaitUserFactor = performanceMeasurementWaitUserFactor;
    }

    public void setWriteLogFile(boolean writeLogFile) {
        this.writeLogFile = writeLogFile;
    }
}
