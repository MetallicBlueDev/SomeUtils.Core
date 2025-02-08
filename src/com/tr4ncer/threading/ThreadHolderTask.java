package com.tr4ncer.threading;

/**
 *
 * @author Sébastien Villemain
 */
public class ThreadHolderTask extends Thread {

    private final ServiceProcess task;

    public ThreadHolderTask(ServiceProcess task) {
        this(null, task, task.getClass().getSimpleName());
    }

    public ThreadHolderTask(ServiceProcess task, String name) {
        this(null, task, name);
    }

    public ThreadHolderTask(ThreadGroup group, ServiceProcess task, String name) {
        super(group, task, name);
        this.task = task;

        if (name == null || name.isBlank()) {
            setName(task.getClass().getSimpleName());
        }
    }

    public ServiceProcess getTask() {
        return task;
    }

}
