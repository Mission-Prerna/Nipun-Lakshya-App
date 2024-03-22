package com.morziz.network.helpers;

import com.morziz.network.utils.ErrorMessagesN;

import java.io.IOException;


/**
 * Created by Ankit Maheswari on 05/09/18.
 */

public class NoConnectivityException extends IOException {

    @Override
    public String getMessage() {
        return ErrorMessagesN.NETWORK_ISSUE;
    }
}
