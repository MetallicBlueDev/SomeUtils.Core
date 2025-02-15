package com.metallicbluedev.core;

import com.metallicbluedev.factory.*;
import com.metallicbluedev.threading.*;

/**
 *
 * @author Sébastien Villemain
 */
public interface MainProcess extends ServiceProcess, EntityProcess {

    public boolean canRunInMain();

}
