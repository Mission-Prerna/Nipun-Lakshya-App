package org.odk.collect.android.listeners

interface FormProcessListener {

    /** Called when the form processing starts. */
    fun onProcessingStart();

    /** Called when the form process is successful. */
    fun onProcessed()

    /** Called when the form process fails. */
    fun onCancelled(e: Exception)
}