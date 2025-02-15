package com.metallicbluedev.core;

import com.metallicbluedev.dto.*;
import com.metallicbluedev.factory.*;
import com.metallicbluedev.io.*;
import com.metallicbluedev.logger.*;
import java.nio.file.*;
import javax.swing.event.*;

/**
 *
 * @author Sébastien Villemain
 * @param <E>
 */
public class XmlSettingManager<E extends AppSettings> implements SettingManager {

    /**
     * Liste des écouteurs d'événements.
     */
    private final EventListenerList listeners;

    private final XmlPropertyInfo<E> propertyInfo;

    protected XmlSettingManager(Class<E> type) {
        listeners = new EventListenerList();

        if (!FactoryManager.hasInstance(MainManager.class)) {
            throw new IllegalStateException("MainManager is null");
        }

        Path defaultPath = Path.of(MainManager.getInstance().getMainAppClass().getSimpleName() + ".xml");
        propertyInfo = new XmlPropertyInfo<>(defaultPath, type);
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
        LoggerManager.getInstance().addInformation("Loading application setting");

        if (path != null) {
            propertyInfo.setPath(path);
        }
        return propertyInfo.load();
    }

    @Override
    public boolean loaded() {
        return propertyInfo.getXmlObject() != null;
    }

    @Override
    public boolean save() {
        LoggerManager.getInstance().addInformation("Saving application setting");

        propertyInfo.setDefaultXmlObjectOnNull();
        return propertyInfo.save();
    }

    @Override
    public boolean canWriteLogFile() {
        return getSettings().isWriteLogFile();
    }

    @Override
    public String getLogFilePath() {
        String applicationPath = MainManager.getInstance().getApplicationPath();
        String simpleName = MainManager.getInstance().getMainAppClass().getSimpleName();
        return applicationPath + simpleName + "EventLog%g." + LoggerManager.EXTENSION_NAME;
    }

    @Override
    public int getLogMaxBackup() {
        return getSettings().getLogMaxBackup();
    }

    @Override
    public int getLogMaxLength() {
        return getSettings().getLogMaxLength();
    }

    @Override
    public void createProcess() {
        propertyInfo.load();

        if (getSettings().isAutoStartPerformanceMeasurement()) {
            PerformanceMeasurement measurement = FactoryManager.getInstance(PerformanceMeasurement.class);

            if (!measurement.running()) {
                measurement.start();
            }
            measurement.setWaitUserFactor(getSettings().getPerformanceMeasurementWaitUserFactor());
        }
    }

    @Override
    public void destroyProcess() {
    }

    @Override
    public String getInformation() {
        return getClass().getSimpleName();
    }

    public void setDefaultSettingsOnNull() {
        propertyInfo.setDefaultXmlObjectOnNull();
    }

    public E getSettings() {
        return propertyInfo.getXmlObjectOrDefault();
    }
}
