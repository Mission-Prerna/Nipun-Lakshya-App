package org.odk.collect.android.contracts

import org.odk.collect.android.forms.Form

interface FormsCallback {
    fun onProceed(forms: List<Form>)
}