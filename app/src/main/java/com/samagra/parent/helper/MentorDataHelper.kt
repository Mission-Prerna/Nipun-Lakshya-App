/*
* Helper is used to call mentor and meta data api async
* (Reason for async call is mentor data is interdependent on meta data response)
* that will help to fetch
* #mentorDetails, meta data
* #Schools ; Related to the mentor with visit status,
* #Home statics (Overviews) ; Related to the mentor
* */

package com.samagra.parent.helper

import androidx.preference.PreferenceManager
import com.google.gson.Gson
import com.morziz.network.config.ClientType
import com.morziz.network.network.Network
import com.posthog.android.PostHog
import com.samagra.ancillaryscreens.data.model.RetrofitService
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.commons.MetaDataExtensions
import com.samagra.commons.constants.Constants
import com.samagra.commons.constants.UserConstants
import com.samagra.commons.models.Result
import com.samagra.commons.models.mentordetails.HomeOverviewRemoteResponse
import com.samagra.commons.models.mentordetails.MentorDetailsRemoteResponse
import com.samagra.commons.models.mentordetails.MentorRemoteResponse
import com.samagra.commons.models.metadata.MetaDataRemoteResponse
import com.samagra.commons.models.schoolsresponsedata.SchoolsData
import com.samagra.commons.models.submitresultsdata.ResultsVisitData
import com.samagra.commons.posthog.PRODUCT
import com.samagra.commons.posthog.PostHogManager
import com.samagra.commons.utils.CommonConstants
import com.samagra.commons.utils.NetworkStateManager
import com.samagra.commons.utils.RemoteConfigUtils
import com.samagra.grove.logging.Grove
import com.samagra.parent.MyApplication
import com.samagra.parent.UtilityFunctions
import com.samagra.parent.ui.assessmenthome.HomeOverviewData
import com.samagra.parent.ui.getBearerAuthToken
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit

object MentorDataHelper {

    val apiService by lazy { generateApiService() }

    private val gson by lazy { Gson() }

    suspend fun fetchMentorData(
        enforce: Boolean = false,
        prefs: CommonsPrefsHelperImpl
    ): StateFlow<Boolean?> {
        val remoteResponseStatus: MutableStateFlow<Boolean?> = MutableStateFlow(null)
        Timber.d("fetchMentorData: ")
        if (NetworkStateManager.instance?.networkConnectivityStatus != true) {
            Timber.d("fetchMentorData: no network")
            remoteResponseStatus.emit(false)
        }
        try {
            val responseMentor = CoroutineScope(Dispatchers.IO).async {
                apiService?.fetchMentorData(apiKey = prefs.getBearerAuthToken())
            }
            if (checkMetaDataDuration(enforce, prefs)) {
                val responseMeta: Deferred<MetaDataRemoteResponse?> =
                    CoroutineScope(Dispatchers.IO).async {
                        apiService?.fetchMetaData(apiKey = prefs.getBearerAuthToken())
                    }
                val metaData = responseMeta.await()
                MetaDataHelper.setMetaDataResponse(metaData, prefs)
            }
            val mentorData: MentorDetailsRemoteResponse? = responseMentor.await()
            mentorData?.let {
                Timber.d("fetchMentorData: response got")
                parseMentorData(it.mentor, prefs)
                parseSchoolsData(it.schoolList)
                parseHomeOverviewData(it.homeOverview, prefs)
                remoteResponseStatus.emit(true)
            }
        } catch (t: Exception) {
            Timber.e(t, "fetchMentorData error: %s", t.message)
            remoteResponseStatus.emit(false)
        }
        return remoteResponseStatus
    }

    private fun checkMetaDataDuration(enforce: Boolean, prefs: CommonsPrefsHelperImpl): Boolean {
        if (!enforce) {
            val hoursFromPreviousFetch = TimeUnit.MILLISECONDS.toHours(
                Date().time - prefs.previousMetadataFetch
            )
            Timber.d("fetchMetaData hours from previous fetch: $hoursFromPreviousFetch")
            val minHourDiffForFetch = RemoteConfigUtils.getFirebaseRemoteConfigInstance()
                .getDouble(RemoteConfigUtils.METADATA_FETCH_DELTA_IN_HOURS)
            Timber.d("fetchMetaData min diff for fetch: $minHourDiffForFetch")
            if (hoursFromPreviousFetch < minHourDiffForFetch) {
                return false
            }
        }
        return true
    }

    private fun parseHomeOverviewData(
        homeOverview: HomeOverviewRemoteResponse?,
        prefs: CommonsPrefsHelperImpl
    ) {
        Timber.d("parseHomeOverviewData: $homeOverview")
        homeOverview?.let {
            var grade1Count = 0
            var grade2Count = 0
            var grade3Count = 0
            it.grades?.forEach { grades ->
                when (grades.grade) {
                    1 -> {
                        grade1Count = grades.totalAssessments ?: 0
                    }
                    2 -> {
                        grade2Count = grades.totalAssessments ?: 0
                    }
                    3 -> {
                        grade3Count = grades.totalAssessments ?: 0
                    }
                }
            }
            val homeOverviewData = HomeOverviewData(
                it.visitedSchools ?: 0,
                it.totalAssessments ?: 0,
                it.averageAssessmentTime ?: 0,
                grade1Count,
                grade2Count,
                grade3Count,
                it.teacherOverView
            )
            val mentorOverviewDetailsString = UtilityFunctions.toJson(homeOverviewData)
            prefs.saveMentorOverViewDetails(mentorOverviewDetailsString)
        }
        Timber.d("parseHomeOverviewData: end")
    }

    fun setOverviewCalculations(
        finalResultsRealm: java.util.ArrayList<ResultsVisitData>,
        overviewDataFromPrefs: HomeOverviewData
    ): HomeOverviewData {
        val dataManager = AssessmentDataManager()
        val totalSchoolsVisited = HashSet<String>()
        var grade1Count = 0
        var grade2Count = 0
        var grade3Count = 0
        val grade1Set = HashSet<String>()
        val grade2Set = HashSet<String>()
        val grade3Set = HashSet<String>()
        var totalSec: Long = 0
        finalResultsRealm.forEachIndexed { _, resultDataRealm ->
            totalSchoolsVisited.add(resultDataRealm.udise_code ?: "")
            if (resultDataRealm.grade == 1) {
                if (resultDataRealm.studentSession.isNullOrEmpty()) {
                    grade1Count += resultDataRealm.no_of_student ?: 0
                } else {
                    grade1Set.add(resultDataRealm.studentSession ?: "")
                }
            } else if (resultDataRealm.grade == 2) {
                if (resultDataRealm.studentSession.isNullOrEmpty()) {
                    grade2Count += resultDataRealm.no_of_student ?: 0
                } else {
                    grade2Set.add(resultDataRealm.studentSession ?: "")
                }
            } else if (resultDataRealm.grade == 3) {
                if (resultDataRealm.studentSession.isNullOrEmpty()) {
                    grade3Count += resultDataRealm.no_of_student ?: 0
                } else {
                    grade3Set.add(resultDataRealm.studentSession ?: "")
                }
            } else {
                Grove.e("Grade is : ${resultDataRealm.grade}, which is other than 1,2,3")
            }
            totalSec += dataManager.getTotalSeconds(resultDataRealm.total_time_taken ?: "")
        }
        grade1Count += grade1Set.size
        grade2Count += grade2Set.size
        grade3Count += grade3Set.size
        val totalStudentsAssessed = grade1Count + grade2Count + grade3Count
        return HomeOverviewData(
            totalSchoolsVisited.size + overviewDataFromPrefs.schoolsVisited,
            totalStudentsAssessed + overviewDataFromPrefs.studentsAssessed,
            overviewDataFromPrefs.avgTimePerStudent,
            grade1Count + overviewDataFromPrefs.grade1Students,
            grade2Count + overviewDataFromPrefs.grade2Students,
            grade3Count + overviewDataFromPrefs.grade3Students,
            overviewDataFromPrefs.teacherOverviewData
        )
    }

    fun getOverviewDataFromPrefs(
        overviewString: String?
    ): HomeOverviewData? {
        var overviewFromPrefs: HomeOverviewData? = null
        try {
            if (!overviewString.isNullOrEmpty()) {
                overviewFromPrefs = Gson().fromJson(overviewString, HomeOverviewData::class.java)
            }
        } catch (e: Exception) {
            overviewFromPrefs = null
        }
        return overviewFromPrefs
    }

    private fun parseSchoolsData(
        schoolList: ArrayList<SchoolsData>?
    ) {
        Timber.d("parseSchoolsData size: ${schoolList?.size}")
        Timber.d("parseSchoolsData first data: ${schoolList?.get(0)}")
        schoolList?.let {
            RealmStoreHelper.deleteSchools()
            RealmStoreHelper.insertSchools(it)
        }
        Timber.d("parseSchoolsData: end")
    }

    fun parseMentorData(info: MentorRemoteResponse?, prefs: CommonsPrefsHelperImpl) {
        Timber.d("parseMentorData: $info")
        info?.let {
            val mentorDetailsToSave = Result(
                id = it.id ?: 0,
                designation_id = it.designationId ?: 0,
                district_id = it.districtId ?: 0,
                district_name = it.districtName ?: "",
                block_id = it.blockId ?: 0,
                block_town_name = it.blockTownName ?: "",
                officer_name = it.officerName ?: "",
                phone_no = it.phoneNo.toString(),
                schoolId = it.teacherSchoolListMapping?.schoolList?.schoolId ?: 0,
                schoolDistrict = it.teacherSchoolListMapping?.schoolList?.district ?: "",
                schoolDistrictId = it.teacherSchoolListMapping?.schoolList?.districtId ?: 0,
                schoolBlock = it.teacherSchoolListMapping?.schoolList?.block ?: "",
                schoolBlockId = it.teacherSchoolListMapping?.schoolList?.blockId ?: 0,
                schoolNyayPanchayat = it.teacherSchoolListMapping?.schoolList?.nyayPanchayat ?: "",
                schoolNyayPanchayatId = it.teacherSchoolListMapping?.schoolList?.nyayPanchayatId
                    ?: 0,
                schoolName = it.teacherSchoolListMapping?.schoolList?.schoolName ?: "",
                udise = it.teacherSchoolListMapping?.schoolList?.udise ?: 0,
                actorId = getActorIdFiltered(it),
                schoolLat = it.teacherSchoolListMapping?.schoolList?.schoolLat ?: 0.0,
                schoolLong = it.teacherSchoolListMapping?.schoolList?.schoolLong ?: 0.0,
                schoolGeoFenceEnabled = it.teacherSchoolListMapping?.schoolList?.geofencingEnabled
                    ?: true
            )
            val actor = MetaDataExtensions.getActorFromActorId(
                actorId = getActorIdFiltered(it),
                actorListJson = prefs.actorsListJson
            )
            if (actor.equals(Constants.USER_EXAMINER, true)) {
                prefs.saveAssessmentType(Constants.STATE_LED_ASSESSMENT)
            }
            prefs.saveSelectedUser(actor)
            Timber.d("saved selectedUser is : ${prefs.selectedUser}")
            createPostHogBaseMap(it, prefs)
            prefs.saveMentorDetails(gson.toJson(mentorDetailsToSave))
        }
        Timber.d("parseMentorData: end")
    }

    private fun getActorIdFiltered(response: MentorRemoteResponse): Int {
        return if (response.actorId == UserConstants.DIET_MENTOR_INT) {
            1
        } else {
            response.actorId ?: 0
        }
    }

    /*
    * Creating base map for Actors except parent
    * */
    private fun createPostHogBaseMap(
        response: MentorRemoteResponse,
        prefs: CommonsPrefsHelperImpl
    ) {
        val designation =
            MetaDataExtensions.getDesignationFromId(
                response.designationId ?: 0,
                prefs.designationsListJson
            )
        PostHogManager.createBaseMap(
            PRODUCT,
            response.id.toString(),
            response.id.toString(),
            designation,
            prefs.selectedUser,
            PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance().applicationContext)
        )
        PostHog.with(MyApplication.getInstance().applicationContext)
            .identify(response.id.toString())

    }

    //todo check if AssessmentService is nullable then what happens
    //todo add this method in one place
    private fun generateApiService(): RetrofitService? {
        return Network.getClient(
            ClientType.RETROFIT,
            RetrofitService::class.java,
            CommonConstants.IDENTITY_APP_SERVICE
        )
    }
}