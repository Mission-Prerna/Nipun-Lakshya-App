package com.assessment.flow.workflowengine.odk

import com.samagra.commons.models.schoolsresponsedata.SchoolsData

/**
 * Any ODK properties or data can be added here.
 */
data class OdkProperties(
    var formID: String = "",
    var showInstructions: Boolean = true,
    var grade: Int = 0,
    var subject: String = "",
    var studentCount: Int = 0,
    var competencyName: String = "",
    var competencyId: String = "",
    var subjectId: Int = 0
) : java.io.Serializable

