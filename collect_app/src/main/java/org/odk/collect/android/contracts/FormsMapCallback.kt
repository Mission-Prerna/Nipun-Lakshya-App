package org.odk.collect.android.contracts

import org.odk.collect.android.formmanagement.ServerFormDetails
import org.odk.collect.android.forms.Form
import java.util.HashMap

interface FormsMapCallback {
    fun onProceed(forms : HashMap<String, ServerFormDetails>)
}