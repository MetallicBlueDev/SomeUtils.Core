package com.tr4ncer.core;

import com.tr4ncer.factory.*;
import com.tr4ncer.io.*;
import com.tr4ncer.logger.*;
import java.nio.file.*;
import javax.swing.event.*;

/**
 *
 * @author Sébastien Villemain
 */
public class SimplePropertySettingManager implements SettingManager {

    /**
     * Liste des écouteurs d'événements.
     */
    private final EventListenerList listeners;

    private final SimplePropertyInfo propertyInfo;

    protected SimplePropertySettingManager() {
        listeners = new EventListenerList();

        if (!FactoryManager.hasInstance(MainManager.class)) {
            throw new IllegalStateException("MainManager is null");
        }

        Path defaultPath = Path.of(MainManager.getInstance().getMainAppClass().getSimpleName() + ".properties");
        propertyInfo = new SimplePropertyInfo(defaultPath);
    }

    @Override
    public void addListener(SettingListener listener) {
        listeners.add(SettingListener.class, listener);
    }

    @Override
    public void removeListener(SettingListener listener) {
        listeners.remove(SettingListener.class, listener);
    }

    @Override
    public boolean load(Path path) {
        if (path != null) {
            propertyInfo.setPath(path);
        }
        return propertyInfo.load();
    }

    @Override
    public boolean loaded() {
        return propertyInfo.getProperties() != null;
    }

    @Override
    public boolean save() {
        return propertyInfo.save();
    }

    @Override
    public boolean canWriteLogFile() {
        return propertyInfo.getBoolean("WriteLogFile", true);
    }

    @Override
    public String getLogFilePath() {
        String applicationPath = MainManager.getInstance().getApplicationPath();
        String simpleName = MainManager.getInstance().getMainAppClass().getSimpleName();
        return applicationPath + simpleName + "EventLog%g." + LoggerManager.EXTENSION_NAME;
    }

    @Override
    public int getLogMaxBackup() {
        return propertyInfo.getInt("LogMaxBackup", 10);
    }

    @Override
    public int getLogMaxLength() {
        return propertyInfo.getInt("LogMaxLength", 1572864);
    }

    @Override
    public void createProcess() {
        propertyInfo.load();

        if (autoStartPerformanceMeasurement()) {
            PerformanceMeasurement measurement = FactoryManager.getInstance(PerformanceMeasurement.class);

            if (!measurement.running()) {
                measurement.start();
            }
            measurement.setWaitUserFactor(getPerformanceMeasurementWaitUserFactor());
        }
    }

    @Override
    public void destroyProcess() {
    }

    @Override
    public String getInformation() {
        return getClass().getSimpleName();
    }

    public boolean autoStartPerformanceMeasurement() {
        return propertyInfo.getBoolean("AutoStartPerformanceMeasurement", false);
    }

    public float getPerformanceMeasurementWaitUserFactor() {
        return propertyInfo.getFloat("PerformanceMeasurementWaitUserFactor", 1F);
    }

    protected SimplePropertyInfo getPropertyInfo() {
        return propertyInfo;
    }

}
