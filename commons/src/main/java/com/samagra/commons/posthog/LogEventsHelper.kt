package com.samagra.commons.posthog

import android.content.Context
import androidx.preference.PreferenceManager
import com.samagra.commons.posthog.data.Cdata
import com.samagra.commons.posthog.data.Edata
import com.samagra.commons.posthog.data.Object

object LogEventsHelper {

    fun addTelemetryEventOnAppProcessKill(
        grade: String,
        subject: String,
        assessmentType: String,
        context: Context?,
        screen: String,
        pId: String
    ) {
        val cDataList = ArrayList<Cdata>()
        cDataList.add(Cdata("grade", grade))
        cDataList.add(Cdata("subject", subject))
        cDataList.add(Cdata("assessment_type", assessmentType))
        val properties = PostHogManager.createProperties(
            screen,
            EVENT_TYPE_SCREEN_VIEW,
            EID_IMPRESSION,
            PostHogManager.createContext(
                APP_ID,
                pId, cDataList
            ),
            Edata(pId, TYPE_SUMMARY),
            Object.Builder().id(STORE_RESULT_ON_PROCESS_KILL).type(OBJ_TYPE_UI_ELEMENT).build(),
            PreferenceManager.getDefaultSharedPreferences(context)
        )
        PostHogManager.capture(context!!, EVENT_APP_PROCESS_KILLED, properties)
    }

    fun addEventOnStartWorkFlow(
        competencyId: String,
        grade: String,
        subject: String,
        currentStudentCount: Int,
        assessmentType: String,
        context: Context?,
        screen: String,
        pId: String
    ) {
        val cDataList = ArrayList<Cdata>()
        cDataList.add(Cdata("currentCompetencyId", competencyId))
        cDataList.add(Cdata("currentStudent", currentStudentCount.toString()))
        cDataList.add(Cdata("grade", grade))
        cDataList.add(Cdata("subject", subject))
        cDataList.add(Cdata("assessment_type", assessmentType))
        val properties = PostHogManager.createProperties(
            screen,
            EVENT_TYPE_USER_ACTION,
            EID_INTERACT,
            PostHogManager.createContext(
                APP_ID,
                pId, cDataList
            ),
            Edata(pId, TYPE_CLICK),
            Object.Builder().id(WORKFLOW_START).type(OBJ_TYPE_UI_ELEMENT).build(),
            PreferenceManager.getDefaultSharedPreferences(context)
        )
        if (context != null)
            PostHogManager.capture(context, EVENT_START_WORKFLOW, properties)
    }

    fun addEventOnNextStudentSelection(
        assessmentType: String,
        context: Context,
        screen: String,
    ) {
        val cDataList = java.util.ArrayList<Cdata>()
        cDataList.add(Cdata("assessment_type", assessmentType))
        val properties = PostHogManager.createProperties(
            screen,
            EVENT_TYPE_USER_ACTION,
            EID_INTERACT,
            PostHogManager.createContext(APP_ID, NL_APP_INDIVIDUAL_RESULT, cDataList),
            Edata(NL_APP_INDIVIDUAL_RESULT, TYPE_CLICK),
            Object.Builder().id(NEXT_STUDENT_ASSESSMENT_BUTTON).type(OBJ_TYPE_UI_ELEMENT).build(),
            PreferenceManager.getDefaultSharedPreferences(context)
        )
        PostHogManager.capture(context, EVENT_NEXT_STUDENT, properties)
    }

    fun setPostHogEventOnStartFlow(
        context: Context,
        assessmentType: String,
        competencyId: String? = null,
        screen: String,
        pId: String,
        ePageId: String
    ) {
        val cDataList = ArrayList<Cdata>()
        competencyId?.let {
            cDataList.add(Cdata("competencyId", competencyId))
        }
        cDataList.add(Cdata("assessment_type", assessmentType))
        val properties = PostHogManager.createProperties(
            screen,
            EVENT_TYPE_USER_ACTION,
            EID_INTERACT,
            PostHogManager.createContext(
                APP_ID,
                pId, cDataList
            ),
            Edata(ePageId, TYPE_CLICK),
            Object.Builder().id(START_ASSESSMENT_NEXT_BUTTON).type(OBJ_TYPE_UI_ELEMENT).build(),
            PreferenceManager.getDefaultSharedPreferences(context)
        )
        PostHogManager.capture(context, EVENT_COMPETENCY_SELECTION, properties)
    }

    fun setSubmitResultEvent(
        context: Context,
        nipunStatus: String? = null,
    ) {
        val cDataList = ArrayList<Cdata>()
        nipunStatus?.let {
            cDataList.add(Cdata("nipun_status", it))
        }
        val properties = PostHogManager.createProperties(
            FINAL_SCORECARD_SCREEN,
            EVENT_TYPE_USER_ACTION,
            EID_INTERACT,
            PostHogManager.createContext(
                APP_ID,
                NL_APP_FINAL_RESULT, cDataList
            ),
            Edata(NL_FINAL_RESULT, TYPE_CLICK),
            Object.Builder().id(SUBMIT_FINAL_RESULT_BUTTON).type(OBJ_TYPE_UI_ELEMENT).build(),
            PreferenceManager.getDefaultSharedPreferences(context)
        )
        PostHogManager.capture(context, EVENT_SUBMIT_FINAL_RESULT, properties)
    }

    @JvmStatic
    fun setEventOnJwtFailure(context: Context) {
        val properties = PostHogManager.createProperties(
            SPLASH_SCREEN,
            EVENT_TYPE_SYSTEM,
            EID_INTERACT,
            PostHogManager.createContext(
                APP_ID,
                NL_APP_SPLASH_SCREEN, arrayListOf()
            ),
            Edata(NL_SPLASH_SCREEN, TYPE_CLICK),
            Object.Builder().id(SUBMIT_FINAL_RESULT_BUTTON).type(OBJ_TYPE_UI_ELEMENT).build(),
            PreferenceManager.getDefaultSharedPreferences(context)
        )
        PostHogManager.capture(context, EVENT_JWT_REFRESH_TOKEN_FAILURE, properties)
    }

    fun addEventOnBoloResultCallback(
        totalTime: Long,
        correctWords: Int,
        wordsPerMinute: Int,
        context: Context,
        screen: String,
        checkFluency: Boolean
    ) {
        val cDataList = java.util.ArrayList<Cdata>()
        cDataList.add(Cdata("totalTime", totalTime.toString()))
        cDataList.add(Cdata("correctWords", correctWords.toString()))
        cDataList.add(Cdata("wordsPerMinute", wordsPerMinute.toString()))
        cDataList.add(Cdata("checkFluency", checkFluency.toString()))
        val properties = PostHogManager.createProperties(
            screen,
            EVENT_TYPE_SUMMARY,
            EID_IMPRESSION,
            PostHogManager.createContext(APP_ID, NL_APP_INDIVIDUAL_RESULT, cDataList),
            Edata(NL_APP_INDIVIDUAL_RESULT, TYPE_CLICK),
            Object.Builder().id(BOLO_RESULT_CALLBACK).type(OBJ_TYPE_UI_ELEMENT).build(),
            PreferenceManager.getDefaultSharedPreferences(context)
        )
        PostHogManager.capture(context, EVENT_BOLO_RESULT_SUCCESS_CALLBACK, properties)
    }
}

