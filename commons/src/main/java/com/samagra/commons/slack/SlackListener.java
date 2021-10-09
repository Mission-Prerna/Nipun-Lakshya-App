package com.samagra.commons.slack;


public interface SlackListener {
    /**
     * The operation succeeded
     * @param s
     */
    void onDone(String s);

}