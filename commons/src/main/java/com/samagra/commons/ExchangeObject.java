package com.samagra.commons;

import android.content.Intent;

/**
 * Exchange object is used to exchange data between two modules. This object is sent over the {@link RxBus}
 * which is an event bus created using ReactiveX classes. This class cannot be directly used and instead one of its
 * child classes - {@link SignalExchangeObject}, {@link DataExchangeObject} or {@link EventExchangeObject} must
 * be used to create an object of this class.
 *
 * @author Pranav Sharma
 */
public class ExchangeObject {
    public ExchangeObjectTypes type;
    public Modules to;
    public Modules from;

    /**
     * Private constructor indicating that the class cannot be instantiated from outside this class.
     *
     * @param type - {@link ExchangeObjectTypes} which notifies the type of {@link ExchangeObject}.
     * @param to   - Indicates the {@link Modules} to which this {@link ExchangeObject} is sent.
     * @param from - Indicates the {@link Modules} from whic this {@link ExchangeObject} originated.
     */
    private ExchangeObject(ExchangeObjectTypes type, Modules to, Modules from) {
        this.type = type;
        this.to = to;
        this.from = from;
    }

    /**
     * This is a type of {@link ExchangeObject} that is used to give signal to launch an intent from
     * one module to another.
     */
    public static class SignalExchangeObject extends ExchangeObject {

        public Intent intentToLaunch;
        // indicates if the intent should be launched in a new task, clearing the activity back-stack.
        public boolean shouldStartAsNewTask = false;

        /**
         * Public Constructor used to create a {@link SignalExchangeObject}
         *
         * @param to             - Indicates the {@link Modules} to which this {@link ExchangeObject} is sent.
         * @param from           - Indicates the {@link Modules} from whic this {@link ExchangeObject} originated.
         * @param intentToLaunch - The {@link Intent} that should be launched upon receiving this object
         *                       on the {@link RxBus}
         */
        public SignalExchangeObject(Modules to, Modules from, Intent intentToLaunch) {
            super(ExchangeObjectTypes.SIGNAL, to, from);
            this.intentToLaunch = intentToLaunch;
        }

        /**
         * Public Constructor used to create a {@link SignalExchangeObject}
         *
         * @param to                   - Indicates the {@link Modules} to which this {@link ExchangeObject} is sent.
         * @param from                 - Indicates the {@link Modules} from whic this {@link ExchangeObject} originated.
         * @param intentToLaunch       - The {@link Intent} that should be launched upon receiving this object
         *                             on the {@link RxBus}
         * @param shouldStartAsNewTask - a boolean to indicate if the intentToLaunch should be lauched on a new task,
         *                             invalidating the current activity back-stack
         */
        public SignalExchangeObject(Modules to, Modules from, Intent intentToLaunch, boolean shouldStartAsNewTask) {
            super(ExchangeObjectTypes.SIGNAL, to, from);
            this.intentToLaunch = intentToLaunch;
            this.shouldStartAsNewTask = shouldStartAsNewTask;
        }
    }

    /**
     * This type of {@link ExchangeObject} is used to exchange Object values through the {@link RxBus}.
     * This class uses Java generics. This means the Object to exchange can be of any type.
     */
    public static class DataExchangeObject<T> extends ExchangeObject {
        public T data;

        /**
         * Public Constructor used to create a {@link DataExchangeObject}
         *
         * @param to   - Indicates the {@link Modules} to which this {@link ExchangeObject} is sent.
         * @param from - Indicates the {@link Modules} from whic this {@link ExchangeObject} originated.
         * @param data - A generic type indicating the value to exchange.
         */
        public DataExchangeObject(Modules to, Modules from, T data) {
            super(ExchangeObjectTypes.DATA_EXCHANGE, to, from);
            this.data = data;
        }
    }


    /**
     * This type of {@link ExchangeObject} is used to exchange Object values through the {@link RxBus}.
     * This class uses Java generics. This means the Object to exchange can be of any type.
     */
    public static class NotificationExchangeObject extends ExchangeObject {
        public PushNotification data;

        /**
         * Public Constructor used to create a {@link DataExchangeObject}
         *
         * @param to   - Indicates the {@link Modules} to which this {@link ExchangeObject} is sent.
         * @param from - Indicates the {@link Modules} from whic this {@link ExchangeObject} originated.
         * @param data - A generic type indicating the value to exchange.
         */
        public NotificationExchangeObject(Modules to, Modules from, PushNotification data) {
            super(ExchangeObjectTypes.DATA_EXCHANGE, to, from);
            this.data = data;
        }

    }


    /**
     * An {@link ExchangeObject} that notifies the {@link RxBus} about an event being triggered. This
     * event should be declared in the {@link CustomEvents}. Based on the type of event received by the {@link RxBus},
     * various actions could be performed.
     */
    public static class EventExchangeObject extends ExchangeObject {

        public CustomEvents customEvents;

        /**
         * Public Constructor used to create a {@link EventExchangeObject}
         *
         * @param to           - Indicates the {@link Modules} to which this {@link ExchangeObject} is sent.
         * @param from         - Indicates the {@link Modules} from whic this {@link ExchangeObject} originated.
         * @param customEvents - Indicates the type of event triggered.
         */
        public EventExchangeObject(Modules to, Modules from, CustomEvents customEvents) {
            super(ExchangeObjectTypes.EVENT, to, from);
            this.customEvents = customEvents;
        }
    }

    /**
     * An enum indicating the type of {@link ExchangeObject}
     *
     * @see ExchangeObject#ExchangeObject(ExchangeObjectTypes, Modules, Modules)
     */
    public enum ExchangeObjectTypes {
        SIGNAL, // Indicates a signal to start some new Activity through an intent. The class to start is passed in the data field
        DATA_EXCHANGE, // Indicates Data Exchange between Modules
        EVENT // Indicates an event has occurred.
    }
}
