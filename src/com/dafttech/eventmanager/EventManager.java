package com.dafttech.eventmanager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.dafttech.eventmanager.exception.MissingEventTypeException;
import com.dafttech.eventmanager.exception.WrongEventListenerAnnotationUsageException;

public class EventManager {
    volatile protected List<EventType> events = new ArrayList<EventType>();

    volatile public AsyncEventQueue asyncEventQueue = new AsyncEventQueue();

    public EventManager() {
    }

    /**
     * Used to get the instance of the EventType registered with that name.
     * 
     * @param name
     *            String
     * @return EventType
     */
    public EventType getEventByName(String name) {
        return new EventTypeGetter(name).getFromList(events);
    }

    /**
     * Used to get the instance of the EventType registered with that id.
     * 
     * @param id
     *            int
     * @return EventType
     */
    public EventType getEventById(int id) {
        return new EventTypeGetter(id).getFromList(events);
    }

    /**
     * Used to register an EventListener created with annotations to call the
     * annotated methods.
     * 
     * @param eventListener
     *            Object - Instance of the listening class
     * @param filter
     *            Object... - Sets the filter that is customizable in EventType
     *            subclasses
     */
    public void registerEventListener(Object eventListener, Object... filter) {
        registerPrioritizedEventListener(eventListener, EventType.PRIORITY_STANDARD, filter);
    }

    /**
     * Used to register an EventListener with a specific priority created with
     * annotations to call the annotated methods.
     * 
     * @param eventListener
     *            Object - Instance of the listening class
     * @param priority
     *            int - Higher priority = earlier called
     * @param filter
     *            Object... - Sets the filter that is customizable in EventType
     *            subclasses
     */
    public void registerPrioritizedEventListener(Object eventListener, int priority, Object... filter) {
        EventType event = null;
        for (Method method : eventListener.getClass().getMethods()) {
            if (method.isAnnotationPresent(EventListener.class)) {
                if (method.getParameterTypes().length == 2 && method.getParameterTypes()[0] == Event.class
                        && method.getParameterTypes()[1] == Object[].class) {
                    for (String allowedEvent : method.getAnnotation(EventListener.class).eventNames()) {
                        event = getEventByName(allowedEvent);
                        if (event != null) {
                            event.addEventListenerContainer(new EventListenerContainer(eventListener, method, priority, filter));
                        } else {
                            throw new MissingEventTypeException(allowedEvent);
                        }
                    }
                } else {
                    throw new WrongEventListenerAnnotationUsageException();
                }
            }
        }
    }

    /**
     * Used to unregister an EventListener in all events.
     * 
     * @param eventListener
     *            Object - Instance of the listening class
     */
    public final void unregisterEventListener(Object eventListener) {
        for (EventType eventType : events) {
            eventType.eventListenerContainer.remove(eventListener);
        }
    }
}