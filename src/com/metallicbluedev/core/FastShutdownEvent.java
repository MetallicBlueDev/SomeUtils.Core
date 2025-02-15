package com.metallicbluedev.core;

import java.util.*;

/**
 *
 * @author Sébastien Villemain
 */
public class FastShutdownEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    public FastShutdownEvent(Object source) {
        super(source);
    }

}
