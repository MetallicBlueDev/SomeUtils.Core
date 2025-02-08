package com.tr4ncer.taskprogress;

import java.util.*;

/**
 *
 * @author Sébastien Villemain
 */
public class TaskProgressEvent extends EventObject {

    private static final long serialVersionUID = 1L;

    private final double value;
    private final TaskProgressState state;

    public TaskProgressEvent(Object source, double value, TaskProgressState state) {
        super(source);
        this.value = value;
        this.state = state;
    }

    public TaskProgressState getState() {
        return state;
    }

    public double getValue() {
        return value;
    }
}
