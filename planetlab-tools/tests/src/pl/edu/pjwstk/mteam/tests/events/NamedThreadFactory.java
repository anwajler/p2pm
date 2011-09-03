package pl.edu.pjwstk.mteam.tests.events;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;

public class NamedThreadFactory implements ThreadFactory {

    private final String name;
    private final int priority;
    private final AtomicLong counter;

    public NamedThreadFactory(String name) {
        this(name, Thread.NORM_PRIORITY);
    }

    public NamedThreadFactory(String name, int priority) {
        if (name == null || name.isEmpty()) {
            throw new IllegalArgumentException("NamedThreadFactory must have a proper name.");
        }

        this.name = name;
        this.priority = priority;
        this.counter = new AtomicLong(1);
    }

    public Thread newThread(Runnable runnable) {
        Thread t = new Thread(runnable, this.name + '.' + this.counter.getAndIncrement());
        t.setPriority(this.priority);
        return t;
    }

}

