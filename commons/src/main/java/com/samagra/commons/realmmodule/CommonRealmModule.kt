package com.samagra.commons.realmmodule

import com.samagra.commons.models.chaptersdata.ChapterMapping
import com.samagra.commons.models.schoolsresponsedata.SchoolsData
import com.samagra.commons.models.submitresultsdata.ResultsVisitData
import com.samagra.commons.models.surveydata.AssessmentSurveyModel
import io.realm.annotations.RealmModule

@RealmModule(
    library = true,
    classes = [SchoolsData::class, ResultsVisitData::class, AssessmentSurveyModel::class, ChapterMapping::class]
)
class CommonRealmModule