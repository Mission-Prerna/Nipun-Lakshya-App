package com.samagra.commons;

/**
 * Created by Umang Bhola on 16/5/20.
 * Samagra- Transforming Governance
 */
/**
 * Base event that all RxEvent classes should extend.
 * All classes being passed through the event bus should have names ending in RxEvent so that other
 * developers know its purpose.
 */
public abstract class REvent {
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
