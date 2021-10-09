package com.samagra.commons.posthog

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.posthog.android.PostHog
import com.posthog.android.Properties
import com.samagra.commons.constants.Constants
import com.samagra.commons.constants.UserConstants
import com.samagra.commons.models.Result
import com.samagra.commons.posthog.data.*
import java.lang.Exception
import timber.log.Timber
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit

object PostHogManager {

    private val baseMap = HashMap<String, String>()
    private val gson by lazy { Gson() }
    private val keys = ArrayList<String>()

    fun capture(context: Context, eventName: String, properties: Properties) {
        try {
            PostHog.with(context).capture(eventName, properties)
        } catch (e: Exception) {
            Timber.e(e.toString())
        }
    }

    fun reset(context: Context) {
        PostHog.with(context).reset();
    }

    @JvmStatic
    fun init(context: Context, serverUrl: String, apiKey: String) {
        if (keys.contains(serverUrl)) {
            return
        }
        try {
            val postHog = PostHog.Builder(context, apiKey, serverUrl)
                .flushInterval(10, TimeUnit.SECONDS)
                .flushQueueSize(10)
                .captureApplicationLifecycleEvents() // Record certain application events automatically!
                .logLevel(PostHog.LogLevel.VERBOSE)
                .build()
            PostHog.setSingletonInstance(postHog)
            keys.add(serverUrl)
            val preferences = PreferenceManager.getDefaultSharedPreferences(context)
            val mentorDetailsString = preferences.getString(UserConstants.MENTOR_DETAIL, "")
            if (mentorDetailsString?.isNotEmpty() == true) {
                val mentorDetails =
                    Gson().fromJson(mentorDetailsString, Result::class.java)
                if (mentorDetails.id > 0) {
                    Timber.d("init of post hog setting identity as mentorId : ${mentorDetails.id}")
                    postHog.identify(mentorDetails.id.toString())
                } else {
                    Timber.d("init of post hog mentor id is 0.")
                }
            } else {
                Timber.d( "init of post hog mentor details are null or empty!")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun createBaseMap(
        product: String?,
        identifier: String?,
        actorId: String?,
        actorType: String? = "school",
        selectedUser: String?,
        prefs: SharedPreferences? = null
    ) {
        baseMap["product"] = product!!
        baseMap["identifier"] = identifier!!
        baseMap["actorId"] = actorId!!
        baseMap["actorType"] = actorType!!
        baseMap["selectedUser"] = selectedUser!!
        /*
        * store data each time once base map is created.
        * */
        val baseMapDataJson: String = gson.toJson(baseMap)
        prefs?.edit()?.putString(Constants.FALLBACK_BASEMAP, baseMapDataJson)?.apply()

        Timber.d( "base map $baseMap")
        Timber.d( "base map string $baseMapDataJson")
    }

    fun createProperties(
        page: String?,
        eventType: String?,
        eid: String?,
        context: com.samagra.commons.posthog.data.Context?,
        eData: Edata?,
        objectData: Object?,
        prefs: SharedPreferences? = null
    ): Properties {
        if (baseMap.isNullOrEmpty()) {
            Log.e("-->>", "base map on properties is null!!!")
            val fallbackBaseMapData = prefs?.getString(Constants.FALLBACK_BASEMAP, "")
            if (fallbackBaseMapData?.isNotEmpty() == true) {
                Timber.d( "base map on properties prefs basemap is null!!!")
                Timber.d( "base map on properties data from prefs $fallbackBaseMapData")
                val empMapType: Type = object : TypeToken<Map<String, String>?>() {}.type
                val nameEmployeeMap: Map<String, String> =
                    gson.fromJson(fallbackBaseMapData, empMapType)
                try {
                    Timber.e("BaseMap is empty and handling telemetry from fallback flow!")
                    createBaseMap(
                        nameEmployeeMap["product"],
                        nameEmployeeMap["identifier"],
                        nameEmployeeMap["actorId"],
                        nameEmployeeMap["actorType"],
                        nameEmployeeMap["selectedUser"]
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                    Timber.e(e)
                }
            } else {
                Timber.d( "base map on properties login is null or empty")
            }
        } else {
            Timber.d( "base map on properties base map is not empty $baseMap")
        }
        val properties = Properties()
        properties["product"] = baseMap["product"]
        properties["identifier"] = baseMap["identifier"]
        properties["actor"] = Gson().fromJson(
            Actor(baseMap["actorId"], baseMap["actorType"]).toString(),
            HashMap::class.java
        )
        if (!page.isNullOrBlank()) {
            properties["page"] = page
        }
        if (!eventType.isNullOrBlank()) {
            properties["eventType"] = eventType
        }
        if (!eid.isNullOrBlank()) {
            properties["eid"] = eid
        }
        if (context != null) {
            properties["context"] = Gson().fromJson(
                context.toString(),
                HashMap::class.java
            )
        }
        if (eData != null) {
            properties["eData"] = Gson().fromJson(
                eData.toString(),
                HashMap::class.java
            )
        }
        if (objectData != null) {
            properties["object"] = Gson().fromJson(
                objectData.toString(),
                HashMap::class.java
            )
        }
        return properties;
    }

    fun createContext(
        id: String,
        pid: String,
        dataList: ArrayList<Cdata>
    ): com.samagra.commons.posthog.data.Context {
        val cDataList = ArrayList<Cdata>()
        cDataList.add(Cdata(type = "selectedUser", id = baseMap["selectedUser"]))
        cDataList.addAll(dataList)
        return com.samagra.commons.posthog.data.Context.Builder()
            .channel(CONTEXT_CHANNEL)
            .pdata(Pdata.Builder().id(id).pid(pid).build())
            .cdata(cDataList)
            .build()
    }
}