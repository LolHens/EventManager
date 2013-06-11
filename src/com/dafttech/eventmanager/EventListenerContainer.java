package com.dafttech.eventmanager;

public class EventListenerContainer {
    volatile protected Object eventListener = null;
    volatile protected Object extraInstance = null;
    volatile protected int priority = 0;
    volatile protected Object[] filter = null;

    protected EventListenerContainer(Object eventListener, int priority, Object[] filter) {
        this.eventListener = eventListener;
        this.priority = priority;
        this.filter = filter;
    }

    protected EventListenerContainer(Object eventListener, Object extraInstance, int priority, Object[] filter) {
        this.eventListener = eventListener;
        this.extraInstance = extraInstance;
        this.priority = priority;
        this.filter = filter;
    }

    @Override
    public boolean equals(Object paramObject) {
        if (paramObject instanceof EventListenerContainer) {
            return paramObject == this;
        } else {
            return paramObject == eventListener;
        }
    }
}