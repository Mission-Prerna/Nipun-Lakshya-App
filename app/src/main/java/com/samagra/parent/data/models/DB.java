package com.samagra.parent.data.models;

import java.io.Serializable;

public class DB  implements Serializable {
    public String getDistrict() {
        return district;
    }

    public String getBlock() {
        return block;
    }

    private String district;
    private String block;
}
