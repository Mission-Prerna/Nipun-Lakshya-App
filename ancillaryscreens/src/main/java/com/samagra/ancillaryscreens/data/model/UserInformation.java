package com.samagra.ancillaryscreens.data.model;

import java.io.Serializable;
import java.util.List;

public class UserInformation implements Serializable {
    private int total;
    private List<Users> usersList;

    public List<Users> UserInformation() {
        return usersList;
    }

    public int getTotal() {
        return total;
    }
}
