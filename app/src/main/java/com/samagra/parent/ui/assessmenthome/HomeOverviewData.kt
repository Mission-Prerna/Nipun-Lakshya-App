package com.samagra.parent.ui.assessmenthome

import com.samagra.commons.models.mentordetails.TeacherOverviewData

data class HomeOverviewData(
    var schoolsVisited: Int = 0,
    var studentsAssessed: Int = 0,
    var avgTimePerStudent: Int = 0,
    var grade1Students: Int = 0,
    var grade2Students: Int = 0,
    var grade3Students: Int = 0,
    var teacherOverviewData: TeacherOverviewData? = null
)
