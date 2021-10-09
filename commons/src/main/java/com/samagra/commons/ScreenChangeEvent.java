package com.samagra.commons;

/**
 * Created by Umang Bhola on 17/5/20.
 * Samagra- Transforming Governance
 */
public class ScreenChangeEvent extends REvent {
    private String sourceScreen;
    private String destinationScreen;

    public ScreenChangeEvent(String sourceScreen, String destinationScreen) {
        this.sourceScreen = sourceScreen;
        this.destinationScreen = destinationScreen;
    }

    public String getSourceScreen() {
        return sourceScreen;
    }

    public String getDestinationScreen() {
        return destinationScreen;
    }
}
