package com.samagra.parent.ui.student_learning.studenthome

import org.odk.collect.android.OdkFormsDownloadInLocalResponseData
import org.odk.collect.android.formmanagement.ServerFormDetails

interface OdkResponseListener {
    fun onFailure( data: OdkFormsDownloadInLocalResponseData?)
    fun onUpdateLoaderStatus(value: Int){}
    fun renderLayoutVisible(msg:String, status:Int)
    fun startFormDownloading(newAssessmentsToBeDownloaded: HashMap<String, ServerFormDetails>){}
    fun showFailureDownloadMessage()
}