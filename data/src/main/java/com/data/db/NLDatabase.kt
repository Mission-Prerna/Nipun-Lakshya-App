package com.data.db

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.data.db.dao.AssessmentSchoolHistoryDao
import com.data.db.dao.AssessmentStateDao
import com.data.db.dao.AssessmentSubmissionsDao
import com.data.db.dao.CycleDetailsDao
import com.data.db.dao.ExaminerPerformanceInsightsDao
import com.data.db.dao.MetadataDao
import com.data.db.dao.SchoolSubmissionsDao
import com.data.db.dao.SchoolsDao
import com.data.db.dao.SchoolsStatusHistoryDao
import com.data.db.dao.StudentsAssessmentHistoryDao
import com.data.db.dao.StudentsDao
import com.data.db.dao.TeacherPerformanceInsightsDao
import com.data.db.models.ExaminerPerformanceInsightsItem
import com.data.db.models.TeacherPerformanceInsightsItem
import com.data.db.models.entity.Actor
import com.data.db.models.entity.AssessmentSchoolHistory
import com.data.db.models.entity.AssessmentState
import com.data.db.models.entity.AssessmentSubmission
import com.data.db.models.entity.AssessmentType
import com.data.db.models.entity.Competency
import com.data.db.models.entity.CycleDetails
import com.data.db.models.entity.Designation
import com.data.db.models.entity.ReferenceIds
import com.data.db.models.entity.School
import com.data.db.models.entity.SchoolStatusHistory
import com.data.db.models.entity.SchoolSubmission
import com.data.db.models.entity.Student
import com.data.db.models.entity.StudentAssessmentHistory
import com.data.db.models.entity.Subjects

@Database(
    entities = [
        Student::class,
        StudentAssessmentHistory::class,
        Actor::class,
        AssessmentType::class,
        Competency::class,
        Subjects::class,
        Designation::class,
        ReferenceIds::class,
        AssessmentState::class,
        AssessmentSubmission::class,
        AssessmentSchoolHistory::class,
        TeacherPerformanceInsightsItem::class,
        ExaminerPerformanceInsightsItem::class,
        School::class,
        SchoolStatusHistory::class,
        CycleDetails::class,
        SchoolSubmission::class,
    ],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2),
    ],
    exportSchema = true
)
@TypeConverters(Convertors::class)
abstract class NLDatabase : RoomDatabase() {

    abstract fun getExaminerPerformanceInsightsDao(): ExaminerPerformanceInsightsDao

    abstract fun getTeacherPerformanceInsightsDao(): TeacherPerformanceInsightsDao

    abstract fun getStudentsDao(): StudentsDao

    abstract fun getMetadataDao(): MetadataDao

    abstract fun getMentorDetailsDao(): CycleDetailsDao

    abstract fun getAssessmentStateDao(): AssessmentStateDao

    abstract fun getAssessmentHistoryDao(): StudentsAssessmentHistoryDao

    abstract fun getAssessmentSubmissionDao(): AssessmentSubmissionsDao

    abstract fun getAssessmentSchoolHistoryDao(): AssessmentSchoolHistoryDao

    abstract fun getSchoolDao(): SchoolsDao

    abstract fun getSchoolStatusHistory(): SchoolsStatusHistoryDao

    abstract fun getSchoolSubmissionDao(): SchoolSubmissionsDao
}
