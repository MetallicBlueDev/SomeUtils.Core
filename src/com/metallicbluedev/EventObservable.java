package com.metallicbluedev;

import java.util.*;

/**
 *
 * @author Sébastien Villemain
 * @param <L>
 */
public interface EventObservable<L extends EventObserver<? extends EventObject>> {

    public void addListener(L listener);

    public void removeListener(L listener);

}
