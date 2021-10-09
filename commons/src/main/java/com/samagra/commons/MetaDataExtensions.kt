package com.samagra.commons

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.samagra.commons.models.metadata.Actors
import com.samagra.commons.models.metadata.AssessmentTypes
import com.samagra.commons.models.metadata.Designations
import com.samagra.commons.models.metadata.Subjects

object MetaDataExtensions {

    private val gson by lazy { Gson() }

    fun convertActorsToJson(actors: ArrayList<Actors>): String? {
        val list = mutableListOf<Actors>()
        actors.forEach {
            list.add(Actors(it.id, it.name))
        }
        return gson.toJson(list)
    }

    private fun getActorsList(actorsString: String): ArrayList<Actors>? {
        val type = object : TypeToken<ArrayList<Actors>>() {}.type
        return gson.fromJson<ArrayList<Actors>>(actorsString, type)
    }

    fun convertAssessmentTypesToJson(assessmentTypes: ArrayList<AssessmentTypes>): String? {
        val list = mutableListOf<AssessmentTypes>()
        assessmentTypes.forEach {
            list.add(AssessmentTypes(it.id, it.name))
        }
        return gson.toJson(list)
    }

    fun getAssessmentTypesList(actorsString: String): ArrayList<AssessmentTypes>? {
        val type = object : TypeToken<ArrayList<AssessmentTypes>>() {}.type
        return gson.fromJson<ArrayList<AssessmentTypes>>(actorsString, type)
    }

    fun convertSubjectsToJson(subjects: ArrayList<Subjects>): String? {
        val list = mutableListOf<Subjects>()
        subjects.forEach {
            list.add(Subjects(it.id, it.name as String))
        }
        return gson.toJson(list)
    }

    fun getSubjectsList(actorsString: String): ArrayList<Subjects>? {
        val type = object : TypeToken<ArrayList<Subjects>>() {}.type
        return gson.fromJson<ArrayList<Subjects>>(actorsString, type)
    }

    fun convertDesignationsToJson(designations: ArrayList<Designations>): String? {
        val list = mutableListOf<Designations>()
        designations.forEach {
            list.add(Designations(it.id, it.name as String))
        }
        return gson.toJson(list)
    }

    fun getDesignationsList(actorsString: String): ArrayList<Designations>? {
        val type = object : TypeToken<ArrayList<Designations>>() {}.type
        return gson.fromJson<ArrayList<Designations>>(actorsString, type)
    }

    fun getActorId(actor: String?, actorsListJson: String): Int {
        getActorsList(actorsListJson)?.forEach {
            if (it.name.equals(actor, true)) {
                return it.id ?: 0
            }
        }
        return 0
    }

    fun getDesignationId(designation: String?, designationListJson: String): Int {
        getDesignationsList(designationListJson)?.forEach {
            if (it.name.equals(designation, true)) {
                return it.id ?: 0
            }
        }
        return 0
    }

    fun getSubjectId(subject: String?, subjectListJson: String): Int {
        getSubjectsList(subjectListJson)?.forEach {
            if (it.name.equals(subject, true)) {
                return it.id ?: 0
            }
        }
        return 0
    }

    fun getAssessmentTypeId(assessmentType: String?, assessmentTypeListJson: String): Int {
        getSubjectsList(assessmentTypeListJson)?.forEach {
            if (it.name.equals(assessmentType, true)) {
                return it.id ?: 0
            }
        }
        return 0
    }

    fun getDesignationFromId(designationId: Int, designationListJson: String): String {
        getDesignationsList(designationListJson)?.forEach {
            if (it.id == designationId) {
                return it.name ?: ""
            }
        }
        return ""
    }

    @JvmStatic
    fun getSubjectFromId(subjectId: Int, subjectListJson: String): String {
        getSubjectsList(subjectListJson)?.forEach {
            if (it.id == subjectId) {
                return it.name ?: ""
            }
        }
        return ""
    }

    fun getAssessmentTypeFromId(assessmentTypeId: Int, assessmentTypeListJson: String): String {
        getAssessmentTypesList(assessmentTypeListJson)?.forEach {
            if (it.id == assessmentTypeId) {
                return it.name ?: ""
            }
        }
        return ""
    }

    fun getActorFromActorId(actorId: Int, actorListJson: String): String {
        getActorsList(actorListJson)?.forEach {
            if (it.id == actorId) {
                return it.name ?: ""
            }
        }
        return ""
    }
}