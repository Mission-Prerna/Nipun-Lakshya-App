package com.assessment.flow.workflowengine.bolo.instruction

import android.app.Application
import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import com.assessment.R
import com.assessment.flow.workflowengine.bolo.ReadAlongProperties
import com.samagra.commons.posthog.APP_ID
import com.samagra.commons.posthog.BOLO_START_ASSESSMENT_BUTTON
import com.samagra.commons.posthog.EID_INTERACT
import com.samagra.commons.posthog.EVENT_RA_COMPETENCY_SELECTION
import com.samagra.commons.posthog.EVENT_TYPE_USER_ACTION
import com.samagra.commons.posthog.NL_APP_RA_INSTRUCTION
import com.samagra.commons.posthog.NL_SPOT_ASSESSMENT
import com.samagra.commons.posthog.OBJ_TYPE_UI_ELEMENT
import com.samagra.commons.posthog.PostHogManager
import com.samagra.commons.posthog.RA_INSTRUCTION_SCREEN
import com.samagra.commons.posthog.TYPE_CLICK
import com.samagra.commons.posthog.data.Cdata
import com.samagra.commons.posthog.data.Edata
import com.samagra.commons.posthog.data.Object
import com.samagra.commons.utils.CommonConstants
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

private const val HINDI = "Hindi"
@HiltViewModel
class ReadAlongInstructionViewModel @Inject constructor(val application: Application): ViewModel() {

    private var _subjectInstructionText = MutableLiveData<String>()
    val subjectInstructionText : LiveData<String> = _subjectInstructionText

    fun checkSubjectAndSetInstructionText(props: ReadAlongProperties) {
        if (props.subject == HINDI) {
            _subjectInstructionText.value = application.getString(R.string.sub_ins_text_hindi)
        } else {
            _subjectInstructionText.value = application.getString(R.string.sub_ins_text_others)
        }
    }

    fun checkFluencyIfNeed(props: ReadAlongProperties, totalTime: Long, correctWords: Int): Int {
        return if (props.isCheckFluency) {
            var totalTimeNew = totalTime
            if (totalTimeNew < 1000) totalTimeNew = 1000
            val l = totalTimeNew / 1000
            val wordsPerMinute = (correctWords * 60) / l
            wordsPerMinute.toInt()
        } else {
            correctWords
        }
    }

    fun setPostHogEventSelectRACompetency(context: Context, props: ReadAlongProperties, bookId: String) {
        val cDataList = ArrayList<Cdata>()
        cDataList.add(Cdata("module", CommonConstants.BOLO))
        cDataList.add(Cdata("competencyId", props.competencyId))
        cDataList.add(Cdata("bookId", bookId))
        val properties = PostHogManager.createProperties(
            RA_INSTRUCTION_SCREEN,
            EVENT_TYPE_USER_ACTION,
            EID_INTERACT,
            PostHogManager.createContext(APP_ID, NL_APP_RA_INSTRUCTION, cDataList),
            Edata(NL_SPOT_ASSESSMENT, TYPE_CLICK),
            Object.Builder().id(BOLO_START_ASSESSMENT_BUTTON).type(OBJ_TYPE_UI_ELEMENT).build(),
            PreferenceManager.getDefaultSharedPreferences(context)
        )
        PostHogManager.capture(context, EVENT_RA_COMPETENCY_SELECTION, properties)
    }

}