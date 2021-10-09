package org.odk.collect.android

data class OdkFormsDownloadInLocalResponseData(
    var success: Int, var totalExpected: Int, var failedDownloadFmIdsList:ArrayList<String>
)
