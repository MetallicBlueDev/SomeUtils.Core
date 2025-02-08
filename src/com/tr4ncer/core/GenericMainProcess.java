package com.tr4ncer.core;

import com.tr4ncer.logger.*;

/**
 *
 * @author Sébastien Villemain
 */
public class GenericMainProcess implements MainProcess {

    private boolean running = false;

    @Override
    public boolean canRunInMain() {
        return true;
    }

    @Override
    public void createProcess() {
    }

    @Override
    public void destroyProcess() {
    }

    @Override
    public String getInformation() {
        return getClass().getSimpleName();
    }

    @Override
    public final void run() {
        LoggerManager.getInstance().addInformation("Running " + getInformation());

        try {
            running = true;
            onRunning();
        } catch (Exception e) {
            LoggerManager.getInstance().addError(e);
        } finally {
            running = false;
        }
    }

    @Override
    public final boolean running() {
        return running;
    }

    @Override
    public final void start() {
        LoggerManager.getInstance().addInformation("Starting " + getInformation());

        if (!running) {
            running = true;
            onStart();
        }
    }

    @Override
    public final void stop() {
        LoggerManager.getInstance().addInformation("Stopping " + getInformation());

        if (running) {
            running = false;
            onStop();
        }
    }

    protected void onStart() {
    }

    protected void onRunning() {
    }

    protected void onStop() {
    }
}
