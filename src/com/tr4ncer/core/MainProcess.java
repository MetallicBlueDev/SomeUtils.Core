package com.tr4ncer.core;

import com.tr4ncer.factory.*;
import com.tr4ncer.threading.*;

/**
 *
 * @author Sébastien Villemain
 */
public interface MainProcess extends ServiceProcess, EntityProcess {

    public boolean canRunInMain();

}
