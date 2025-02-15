package com.metallicbluedev.threading;

import com.metallicbluedev.core.*;
import com.metallicbluedev.logger.*;

/**
 *
 * @author Sébastien Villemain
 */
public class ServiceMainProcess extends GenericMainProcess {

    private Thread thread = null;

    @Override
    public boolean canRunInMain() {
        return false;
    }

    @Override
    protected void onStart() {
        if (thread == null) {
            thread = new ThreadHolderTask(this);
            thread.start();
        }
    }

    @Override
    protected void onStop() {
        if (thread != null) {
            try {
                thread.join();
            } catch (InterruptedException ex) {
                LoggerManager.getInstance().addError(ex);
            }
        }
    }

}
