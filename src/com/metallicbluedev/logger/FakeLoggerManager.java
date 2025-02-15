package com.metallicbluedev.logger;

import java.util.logging.*;

/**
 *
 * @author Sébastien Villemain
 */
public class FakeLoggerManager implements CentralizedLoggerManager {

    @Override
    public void addError(String message, Exception ex) {
    }

    @Override
    public void addError(Exception ex) {
    }

    @Override
    public void addError(String message) {
    }

    @Override
    public void addDebug(String message) {
    }

    @Override
    public void addInformation(String message) {
    }

    @Override
    public void addListener(LoggerListener listener) {
    }

    @Override
    public void addMessage(LogRecord message) {
    }

    @Override
    public void addWarning(String message) {
    }

    @Override
    public void setLevelOff() {
    }

    @Override
    public void createProcess() {
    }

    @Override
    public void destroyProcess() {
    }

    @Override
    public String getBaseLoggerName() {
        return getClass().getSimpleName();
    }

    @Override
    public String getInformation() {
        return getClass().getSimpleName();
    }

    @Override
    public LoggerEvent getLastDebugEvent() {
        return null;
    }

    @Override
    public String getLastDebugMessage() {
        return null;
    }

    @Override
    public LoggerEvent getLastErrorEvent() {
        return null;
    }

    @Override
    public String getLastErrorMessage() {
        return null;
    }

    @Override
    public LoggerEvent getLastEvent() {
        return null;
    }

    @Override
    public LoggerEvent getLastInformationEvent() {
        return null;
    }

    @Override
    public String getLastInformationMessage() {
        return null;
    }

    @Override
    public String getLastMessage() {
        return null;
    }

    @Override
    public LoggerEvent getLastWarningEvent() {
        return null;
    }

    @Override
    public String getLastWarningMessage() {
        return null;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public void removeListener(LoggerListener listener) {
    }

    @Override
    public void setFilePath(String filePath) {
    }

    @Override
    public void setMaxBackup(int maxBackup) {
    }

    @Override
    public void setMaxLength(int maxLength) {
    }

}
