package com.dafttech.eventmanager;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class EventManager {
    public static final EventType WHITELIST = new EventType();

    volatile protected Map<EventType, List<EventListenerContainer>> registeredListeners = new HashMap<EventType, List<EventListenerContainer>>();

    public EventManager() {
    }

    /**
     * Used to register an EventListener created with annotations to call the
     * annotated methods.
     * 
     * @param eventListener
     *            Object - Instance of the listening class
     * @param blacklist
     *            EventType... - Sets a blacklist of EventTypes that are then
     *            not registered in the given eventListener class. That can be
     *            converted to a whitelist if you put EventManager.WHITELIST at
     *            the first place.
     */
    public final void registerEventListener(Object eventListener, EventType... blacklist) {
        if (blacklist.length == 1 && blacklist[0] == WHITELIST) return;
        boolean isStatic = eventListener.getClass() == Class.class;
        Class<?> eventListenerClass = isStatic ? (Class<?>) eventListener : eventListener.getClass();
        EventListener annotation = null;
        boolean isListenerStatic = false;
        EventType type = null;
        for (Method method : getAnnotatedMethods(eventListenerClass, EventListener.class, true, null, (Class<?>) null)) {
            annotation = method.getAnnotation(EventListener.class);
            isListenerStatic = Modifier.isStatic(method.getModifiers());
            if (!isStatic || isListenerStatic) {
                for (String requestedEvent : annotation.value()) {
                    type = EventType.types.get(requestedEvent);
                    if (type != null) {
                        if (type.isValidEventManager(this)
                                && (blacklist.length == 0 || blacklist[0] == WHITELIST && Arrays.asList(blacklist).contains(type) || blacklist[0] != WHITELIST
                                        && !Arrays.asList(blacklist).contains(type))) {
                            addEventListenerContainer(type, new EventListenerContainer(isListenerStatic,
                                    isListenerStatic ? eventListenerClass : eventListener, method, annotation));
                        }
                    } else {
                        throw new NoSuchElementException(requestedEvent);
                    }
                }
            }
        }
    }

    public final void tryRegisterEventListener(String staticEventListener, EventType... blacklist) {
        try {
            registerEventListener(Class.forName(staticEventListener), blacklist);
        } catch (ClassNotFoundException e) {
        }
    }

    public final void unregisterEventListener(Object eventListener, EventType... blacklist) {
        if (blacklist.length == 1 && blacklist[0] == WHITELIST) return;
        List<EventListenerContainer> listeners, listenersRead;
        for (EventType type : registeredListeners.keySet()) {
            if (blacklist.length == 0 || blacklist[0] == WHITELIST && Arrays.asList(blacklist).contains(type)
                    || blacklist[0] != WHITELIST && !Arrays.asList(blacklist).contains(type)) {
                listeners = registeredListeners.get(type);
                if (listeners != null && listeners.size() > 0) {
                    listenersRead = new ArrayList<EventListenerContainer>(listeners);
                    for (EventListenerContainer container : listenersRead)
                        if (container.equals(eventListener)) listeners.remove(container);
                }
            }
        }
    }

    /**
     * Used to unregister an EventListener in all events.
     * 
     * @param type
     *            EventType - The EventType you want to unregister.
     * @param eventListener
     *            Object - The eventListener.
     */
    @Deprecated
    public final void unregisterEventListener(EventType type, Object eventListener) {
        if (registeredListeners.containsKey(type)) {
            List<EventListenerContainer> listeners = registeredListeners.get(type);
            if (listeners != null) {
                List<EventListenerContainer> listenersRead = new ArrayList<EventListenerContainer>(listeners);
                for (EventListenerContainer eventListenerContainer : listenersRead)
                    if (eventListenerContainer.getEventListener() == eventListener) listeners.remove(eventListenerContainer);
                if (listeners.size() == 0) registeredListeners.remove(type);
            }
        }
    }

    private final void addEventListenerContainer(EventType type, EventListenerContainer eventListenerContainer) {
        if (!registeredListeners.containsKey(type) || registeredListeners.get(type) == null)
            registeredListeners.put(type, new ArrayList<EventListenerContainer>());
        List<EventListenerContainer> listeners = registeredListeners.get(type);
        EventListenerContainer container;
        for (int i = 0; i < listeners.size(); i++) {
            container = listeners.get(i);
            if (container == eventListenerContainer) return;
            if (container.getPriority() < eventListenerContainer.getPriority()) {
                listeners.add(i, eventListenerContainer);
                return;
            }
        }
        listeners.add(eventListenerContainer);
    }

    /**
     * Calls this event and asks all registered EventListeners and sends the
     * objects to them.
     * 
     * @param type
     *            EventType - The EventType you want to call.
     * @param objects
     *            Object... - You can send any objects to the registered
     *            classes.
     * @return Event - to manage the called event such as getting the output and
     *         checking if the event was cancelled
     */
    public final Event callSync(EventType type, Object... objects) {
        Event event = new Event(this, type, objects);
        List<EventListenerContainer> listeners = registeredListeners.get(type);
        if (listeners != null) {
            listeners = new ArrayList<EventListenerContainer>(listeners);
        } else {
            listeners = new ArrayList<EventListenerContainer>();
        }
        event.schedule(listeners);
        return event;
    }

    /**
     * Calls this event in another thread that has to be started with
     * eventManagerInstance.asyncEventQueue.start(). It asks all registered
     * EventListeners and sends the objects to them.
     * 
     * @param type
     *            EventType - The EventType you want to call.
     * @param objects
     *            Object... - You can send any objects to the registered
     *            classes.
     * @return Event - to manage the called event such as checking if the event
     *         is done, getting the output and checking if the event was
     *         cancelled
     */
    public final Event callAsync(EventType type, Object... objects) {
        Event event = new Event(this, type, objects);
        List<EventListenerContainer> listeners = registeredListeners.get(type);
        if (listeners != null) {
            listeners = new ArrayList<EventListenerContainer>(listeners);
        } else {
            listeners = new ArrayList<EventListenerContainer>();
        }
        new AsyncEventThread(event, listeners);
        return event;
    }

    // STATIC METHODS

    /**
     * Used to get an array of null objects (or false for boolean, 0 for
     * numbers, etc.) for dynamic instantiation. You can also insert objects for
     * specific types instead of null.
     * 
     * @param argTypes
     *            Class<?>[] - the argument Types
     * @param appliedArgs
     *            Object... - The arg types that are replaced with objects
     *            Example: ([Class<?>[] argTypes], String.class, "test")
     * @return Object[] - Array with null or objects (look above)
     */
    public static final Object[] argTypesToArgArray(Class<?>[] argTypes, Object... appliedArgs) {
        Object[] args = new Object[argTypes.length];
        for (int i1 = 0; i1 < args.length; i1++) {
            for (int i2 = 0; i2 + 1 < appliedArgs.length; i2 += 2) {
                if (argTypes[i1] == appliedArgs[i2]) {
                    args[i1] = appliedArgs[i2 + 1];
                    break;
                }
            }
            if (args[i1] == null && argTypes[i1].isPrimitive()) {
                if (argTypes[i1] == boolean.class) {
                    args[i1] = false;
                } else if (argTypes[i1] == char.class) {
                    args[i1] = '\u0000';
                } else {
                    args[i1] = 0;
                }
            }
        }
        return args;
    }

    /**
     * REQUIRES THE CLASS TO HAVE EITHER A FIELD OR A METHOD WITH THE INSTANCE
     * ANNOTATED WITH THE @INSTANCE ANNOTATION
     * 
     * @param targetClass
     *            Class<T> - The class, you want the instance from
     * @return T - The instance already casted to the targetClass
     */
    @SuppressWarnings("unchecked")
    public static final <T> T getInstance(Class<T> targetClass, Object... methodArgs) {
        for (Field field : getAnnotatedFields(targetClass, Instance.class, false, null)) {
            if (Modifier.isStatic(field.getModifiers())) {
                try {
                    return (T) field.get(null);
                } catch (Exception e) {
                }
            }
        }
        for (Method method : getAnnotatedMethods(targetClass, Instance.class, false, null)) {
            if (Modifier.isStatic(method.getModifiers())) {
                try {
                    return (T) method.invoke(null, methodArgs);
                } catch (Exception e) {
                }
            }
        }
        return null;
    }

    public static final List<Method> getAnnotatedMethods(Class<?> targetClass, Class<? extends Annotation> annotation,
            boolean throwException, Class<?> reqType, Class<?>... reqArgs) {
        List<Method> methods = new ArrayList<Method>();
        for (Method method : getAllDeclaredMethods(targetClass)) {
            if (method.isAnnotationPresent(annotation)) {
                if ((reqType == null || method.getReturnType() == reqType)
                        && (reqArgs == null || reqArgs.length == 1 && reqArgs[0] == null || Arrays.equals(
                                method.getParameterTypes(), reqArgs))) {
                    methods.add(method);
                } else if (throwException) {
                    String errorMessage = "\nat " + targetClass.getName() + " at Annotation " + annotation.getName() + ":";
                    errorMessage += "\nexpected: " + reqType.getName() + " with " + (reqArgs.length == 0 ? "no args" : "args:");
                    for (Class<?> arg : reqArgs)
                        errorMessage += ", " + arg.getName();
                    errorMessage += "\nand got:  " + method.getReturnType() + " with "
                            + (method.getParameterTypes().length == 0 ? "no args" : "args:");
                    for (Class<?> arg : method.getParameterTypes())
                        errorMessage += ", " + arg.getName();
                    errorMessage += ".";
                    throw new IllegalArgumentException(errorMessage);
                }
            }
        }
        return methods;
    }

    public static final List<Field> getAnnotatedFields(Class<?> targetClass, Class<? extends Annotation> annotation,
            boolean throwException, Class<?> reqType) {
        List<Field> fields = new ArrayList<Field>();
        if (reqType == void.class) return fields;
        for (Field field : getAllDeclaredFields(targetClass)) {
            if (field.isAnnotationPresent(annotation)) {
                if (reqType == null || field.getType() == reqType) {
                    fields.add(field);
                } else if (throwException) {
                    String errorMessage = "\nat " + targetClass.getName() + " at Annotation " + annotation.getName() + ":";
                    errorMessage += "\nexpected: " + reqType.getName();
                    errorMessage += "\nand got:  " + field.getType();
                    errorMessage += ".";
                    throw new IllegalArgumentException(errorMessage);
                }
            }
        }
        return fields;
    }

    public static final List<Method> getAllDeclaredMethods(Class<?> targetClass) {
        return getAllDeclaredMethods(targetClass, null);
    }

    public static final List<Field> getAllDeclaredFields(Class<?> targetClass) {
        return getAllDeclaredFields(targetClass, null);
    }

    private static final List<Method> getAllDeclaredMethods(Class<?> targetClass, List<Method> methods) {
        if (methods == null) methods = new ArrayList<Method>();
        try {
            for (Method method : targetClass.getDeclaredMethods())
                if (!methods.contains(method)) {
                    method.setAccessible(true);
                    methods.add(method);
                }
        } catch (NoClassDefFoundError e) {
        }
        if (targetClass.getSuperclass() != null) getAllDeclaredMethods(targetClass.getSuperclass(), methods);
        return methods;
    }

    private static final List<Field> getAllDeclaredFields(Class<?> targetClass, List<Field> fields) {
        if (fields == null) fields = new ArrayList<Field>();
        try {
            for (Field field : targetClass.getDeclaredFields())
                if (!fields.contains(field)) {
                    field.setAccessible(true);
                    fields.add(field);
                }
        } catch (NoClassDefFoundError e) {
        }
        if (targetClass.getSuperclass() != null) getAllDeclaredFields(targetClass.getSuperclass(), fields);
        return fields;
    }
}
