package com.tr4ncer;

import java.util.*;

/**
 *
 * @author Sébastien Villemain
 * @param <E>
 */
public interface EventObserver<E extends EventObject> extends EventListener {

    public void onChanged(E event);
}
