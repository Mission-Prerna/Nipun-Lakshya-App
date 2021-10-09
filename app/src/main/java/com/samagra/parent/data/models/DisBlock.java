package com.samagra.parent.data.models;

import java.io.Serializable;
import java.util.ArrayList;

public class DisBlock implements Serializable {
    public ArrayList<DB> getResponse() {
        return response;
    }

    private ArrayList<DB> response;
}
