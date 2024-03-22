package com.samagra.parent.helper

import androidx.preference.PreferenceManager
import com.samagra.commons.models.chaptersdata.ChapterMapping
import com.samagra.commons.models.schoolsresponsedata.SchoolsData
import com.samagra.commons.models.submitresultsdata.ResultsVisitData
import com.samagra.commons.models.surveydata.AssessmentSurveyModel
import com.samagra.commons.posthog.*
import com.samagra.commons.posthog.data.Cdata
import com.samagra.commons.posthog.data.Edata
import com.samagra.commons.realmmodule.CommonRealmModule
import com.samagra.parent.MyApplication
import io.realm.*
import timber.log.Timber

object RealmStoreHelper {

    private var deleteSchemaOnMigration: Boolean = false
    private var version: Long = 0

    fun getDefaultInstance(): Realm {
        val configBuilder = RealmConfiguration.Builder()
            .name(MyApplication::class.java.name)
            .schemaVersion(5)
            .modules(CommonRealmModule())
        if (deleteSchemaOnMigration) {
            configBuilder.deleteRealmIfMigrationNeeded()
        } else {
            configBuilder.migration(NLMigrations())
        }
        val realmConfiguration = configBuilder.build()
        Realm.setDefaultConfiguration(realmConfiguration)
        return Realm.getInstance(realmConfiguration)
    }

    suspend fun insertFinalResultsList(resultsList: ArrayList<ResultsVisitData>): Boolean {
        Timber.i("Inserting results data in Realm!")
        for (listItem in resultsList) {
            getDefaultInstance().executeTransaction { t ->
                t.copyToRealm(listItem)
            }
        }
        return true
    }

    fun deleteVisitResults(list: ArrayList<String?>) {
        Timber.i("Deleting results data from Realm!")
        getDefaultInstance().executeTransaction { t ->
            t.where(ResultsVisitData::class.java).`in`("flowUUID", list.toTypedArray()).findAll()
                .deleteAllFromRealm()
        }
    }

    suspend fun getFinalResults(): ArrayList<ResultsVisitData> {
        var allFinalResults: ArrayList<ResultsVisitData> = ArrayList()
        getDefaultInstance().executeTransaction {
            allFinalResults = it.copyFromRealm(
                it.where(ResultsVisitData::class.java).findAll()
            ) as ArrayList<ResultsVisitData>
        }
        return allFinalResults
    }

    fun getAssessmentResults(): ArrayList<ResultsVisitData> {
        val realm = getDefaultInstance()
        return realm.copyFromRealm(
            realm.where(ResultsVisitData::class.java).findAll()
        ) as ArrayList<ResultsVisitData>
    }

    fun insertSchools(schoolsList: ArrayList<SchoolsData>) {
        getDefaultInstance().executeTransaction { realmTransaction ->
            realmTransaction.copyToRealm(schoolsList)
        }
    }

    fun deleteSchools() {
        getDefaultInstance().executeTransaction { realmTransaction ->
            realmTransaction.delete(SchoolsData::class.java)
        }
    }

    fun getSchoolsData(): ArrayList<SchoolsData> {
        val realm = getDefaultInstance()
        realm.beginTransaction()
        val schoolsListDB: ArrayList<SchoolsData> = realm.copyFromRealm(
            realm.where(SchoolsData::class.java).findAll()
        ) as ArrayList<SchoolsData>
        realm.commitTransaction()
        realm.close()
        return schoolsListDB.ifEmpty {
            ArrayList()
        }
    }

    fun updateSchoolsVisitStatus(udise: Long) {
        val realm = getDefaultInstance()
        realm.executeTransaction {
            val schoolsData: SchoolsData? =
                realm.where(SchoolsData::class.java).equalTo("udise", udise).findFirst()
            schoolsData?.let {
                it.visitStatus = true
            } ?: run {
                Timber.e("Failed to update data on SchoolsData REALM table, School is null!")
            }
        }
    }

    fun clearAllTables(): Int {
        val realm = getDefaultInstance()
        realm.beginTransaction()
        realm.deleteAll()
        realm.commitTransaction()
        realm.close()
        return 1
    }

    fun insertChapterMapping(chapterMappingList: ArrayList<ChapterMapping>) {
        getDefaultInstance().executeTransaction { realmTransaction ->
            realmTransaction.copyToRealm(chapterMappingList)
        }
    }

    @JvmStatic
    fun getChapterMapping(): ArrayList<ChapterMapping> {
        val realm = getDefaultInstance()
        realm.beginTransaction()
        val chapterMappingList: ArrayList<ChapterMapping> = realm.copyFromRealm(
            realm.where(ChapterMapping::class.java).findAll()
        ) as ArrayList<ChapterMapping>
        realm.commitTransaction()
        realm.close()
        return chapterMappingList.ifEmpty {
            ArrayList()
        }
    }

    fun deleteChapterMapping() {
        getDefaultInstance().executeTransaction { realmTransaction ->
            realmTransaction.delete(ChapterMapping::class.java)
        }
    }

    fun insertSurveyResults(surveyData: AssessmentSurveyModel) {
        getDefaultInstance().executeTransaction { t ->
            t.copyToRealm(surveyData)
        }
    }

    fun deleteSurveyResults() {
        getDefaultInstance().executeTransaction { t ->
            t.delete(AssessmentSurveyModel::class.java)
        }
    }

    suspend fun getSurveyResults(): ArrayList<AssessmentSurveyModel> {
        var allSurveyResults: ArrayList<AssessmentSurveyModel> = ArrayList()
        getDefaultInstance().executeTransaction {
            allSurveyResults = it.copyFromRealm(
                it.where(AssessmentSurveyModel::class.java).findAll()
            ) as ArrayList<AssessmentSurveyModel>
        }
        return allSurveyResults
    }

    fun getSurveys(): ArrayList<AssessmentSurveyModel> {
        val realm = getDefaultInstance()
        return realm.copyFromRealm(
            realm.where(AssessmentSurveyModel::class.java).findAll()
        ) as ArrayList<AssessmentSurveyModel>
    }

    /*
    Database versions
    * First time no migration with 0 and after migration 0
    * Second time 0 migration after migration 1
    * Third time 1 to 2 in app version 1.3.1 release
    * */

    private class NLMigrations : RealmMigration {
        override fun migrate(realm: DynamicRealm, oldVersion: Long, newVersion: Long) {
            Timber.d(oldVersion.toString())
            Timber.d(newVersion.toString())
            version = oldVersion
            val schema = realm.schema
            if (version == 0L) {
                migrateV0ToV1(schema)
            }
            if (version == 1L) {
                migrateV1ToV2(schema)
            }
            if (version == 2L) {
                migrateV2ToV3(schema, oldVersion)
            }
            if (version == 3L) {
                migrateV3ToV4()
            }
            if (version == 4L) {
                migrateV4ToV5(schema, oldVersion)
            }
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (javaClass != other?.javaClass) return false
            return true
        }

        override fun hashCode(): Int {
            return javaClass.hashCode()
        }


    }

    private fun migrateV4ToV5(schema: RealmSchema?, oldVersion: Long) {
        Timber.d("migrateV4ToV5: called")
        try {
            Timber.d("migrateV4ToV5: start")
            schema?.get("SchoolsData")?.apply {
                addField("schoolLat", Double::class.java)
                setNullable("schoolLat",true)
                addField("schoolLong", Double::class.java)
                setNullable("schoolLong",true)
                addField("geofencingEnabled", Boolean::class.java)
                setNullable("geofencingEnabled",true)
            }
            Timber.d("migrateV4ToV5: migration complete")
        } catch (e: Exception) {
            Timber.e("Migration failed: ${e.printStackTrace()}")
            deleteSchemaOnMigration = true
            sendMigrationFailedEvent(schema, oldVersion, e)
        } finally {
            Timber.d("migrateV4ToV5: migration final")
            version++
        }
    }

    private fun migrateV3ToV4() {
        deleteSchemaOnMigration = true
    }

    private fun migrateV2ToV3(schema: RealmSchema?, oldVersion: Long) {
        try {
            schema?.get("ResultsVisitData")!!
                .addField("assessment_type", String::class.java, FieldAttribute.REQUIRED)
            schema.get("ChapterMapping")!!
                .addField("assessment_type", String::class.java, FieldAttribute.REQUIRED)
        } catch (e: Exception) {
            Timber.e(e)
            deleteSchemaOnMigration = true
            sendMigrationFailedEvent(schema, oldVersion, e)
        } finally {
            version++
        }
    }

    private fun sendMigrationFailedEvent(schema: RealmSchema?, oldVersion: Long, e: Exception) {
        try {
            val sb = StringBuilder()
            if (schema != null && schema.all != null) {
                schema.all.forEach {
                    sb.append(it.className)
                    sb.append(it.fieldNames)
                    sb.append("\n\n")
                }
            }
            val cDataList = ArrayList<Cdata>()
            cDataList.add(Cdata("current-schema", sb.toString()))
            cDataList.add(Cdata("old-version", oldVersion.toString()))
            cDataList.add(Cdata("exception", e.stackTraceToString()))
            val properties = PostHogManager.createProperties(
                SPLASH_SCREEN,
                EVENT_TYPE_SYSTEM,
                EID_SYSTEM,
                PostHogManager.createContext(APP_ID, NL_APP_DB_MIGRATION, cDataList),
                Edata(NL_APP_DB_MIGRATION, TYPE_SYSTEM_EXCEPTION),
                null,
                PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance().applicationContext)
            )
            PostHogManager.capture(
                MyApplication.getInstance(),
                EVENT_MIGRATION_FAILED,
                properties
            )
        } catch (e: Exception) {
            Timber.e(e)
        }
    }

    private fun migrateV1ToV2(schema: RealmSchema) {
        schema.create("AssessmentSurveyModel").addField("userName", String::class.java)
            .addField("grade", Int::class.java).addField("subject", String::class.java)
            .addField("results", String::class.java).addField("schoolUdise", Long::class.java)
            .addField("actor", String::class.java)
        version++
    }

    private fun migrateV0ToV1(schema: RealmSchema) {
        if (!schema.get("ResultsVisitData")!!.hasField("studentSession")) {
            schema.get("ResultsVisitData")!!
                .addField("studentSession", String::class.java)
        }
        version++
    }

}
