package com.data.db

import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import com.data.db.models.ExaminerInsight
import com.data.db.models.Insight
import com.data.db.models.MentorInsight
import com.data.models.submissions.SubmitResultsModel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.samagra.commons.models.schoolsresponsedata.SchoolsData
import java.util.*

@ProvidedTypeConverter
class Convertors {

    @TypeConverter
    fun fromDate(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun toDate(timestamp: Long?): Date? {
        return timestamp?.let { Date(it) }
    }

    @TypeConverter
    fun toList(value: String?): MutableList<String> {
        val listType = object : TypeToken<MutableList<String?>?>() {}.type
        return Gson().fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: MutableList<String>): String {
        return Gson().toJson(list)
    }

    @TypeConverter
    fun fromSubmitResultsModel(value: SubmitResultsModel): String {
        return Gson().toJson(value)
    }

    @TypeConverter
    fun toSubmitResultsModel(value: String): SubmitResultsModel {
        return Gson().fromJson(value, object : TypeToken<SubmitResultsModel>() {}.type)
    }

    @TypeConverter
    fun fromIntList(grades: List<Int>): String {
        return grades.joinToString(",")
    }

    @TypeConverter
    fun toIntList(gradesString: String): List<Int> {
        return gradesString.split(",").map { it.toInt() }
    }

    @TypeConverter
    fun fromTeacherInsightsList(insightsList: List<Insight>): String {
        return Gson().toJson(insightsList)
    }

    @TypeConverter
    fun toTeacherInsightsList(insightsListJson: String): List<Insight> {
        val type = object : TypeToken<List<Insight>>() {}.type
        return Gson().fromJson(insightsListJson, type)
    }

    @TypeConverter
    fun fromExaminerInsightsList(insightsList: List<ExaminerInsight>): String {
        return Gson().toJson(insightsList)
    }

    @TypeConverter
    fun toExaminerInsightsList(insightsListJson: String): List<ExaminerInsight> {
        val type = object : TypeToken<List<ExaminerInsight>>() {}.type
        return Gson().fromJson(insightsListJson, type)
    }

    @TypeConverter
    fun fromMentorInsightsList(insightsList: List<MentorInsight>): String {
        return Gson().toJson(insightsList)
    }

    @TypeConverter
    fun toMentorInsightsList(insightsListJson: String): List<MentorInsight> {
        val type = object : TypeToken<List<MentorInsight>>() {}.type
        return Gson().fromJson(insightsListJson, type)
    }

    @TypeConverter
    fun fromSchoolList(schoolList: ArrayList<SchoolsData>?): String {
        return Gson().toJson(schoolList)
    }

    @TypeConverter
    fun toSchoolList(schoolListJson: String): ArrayList<SchoolsData>? {
        val type = object : TypeToken< ArrayList<SchoolsData>?>() {}.type
        return Gson().fromJson(schoolListJson, type)
    }
}
