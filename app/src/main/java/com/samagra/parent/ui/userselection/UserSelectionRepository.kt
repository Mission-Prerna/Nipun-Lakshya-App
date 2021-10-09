package com.samagra.parent.ui.userselection

import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.commons.basemvvm.BaseRepository
import com.samagra.commons.utils.RemoteConfigUtils

class UserSelectionRepository : BaseRepository() {

    fun downloadFormsLength(prefs: CommonsPrefsHelperImpl) {
        val quesLength = RemoteConfigUtils.getFirebaseRemoteConfigInstance()
            .getLong(RemoteConfigUtils.ODK_FORM_QUES_LENGTH)
        prefs.saveODKFormQuesLength(quesLength.toInt())
    }

    fun downloadWorkFlowConfigFromRemoteConfig(): String {
        return RemoteConfigUtils.getFirebaseRemoteConfigInstance()
            .getString(RemoteConfigUtils.ASSESSMENT_WORKFLOW_CONFIG)
    }
}
