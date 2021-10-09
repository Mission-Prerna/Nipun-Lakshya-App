package com.data

import com.data.db.models.ExaminerPerformanceInsightsItem
import com.data.db.models.TeacherPerformanceInsightsItem
import com.data.db.models.entity.Actor
import com.data.db.models.entity.AssessmentSchoolHistory
import com.data.db.models.entity.AssessmentState
import com.data.db.models.entity.AssessmentSubmission
import com.data.db.models.entity.Competency
import com.data.db.models.entity.CycleDetails
import com.data.db.models.entity.School
import com.data.db.models.entity.SchoolStatusHistory
import com.data.db.models.entity.SchoolSubmission
import com.data.db.models.entity.Student
import com.data.db.models.entity.StudentAssessmentHistory
import com.data.db.models.entity.Subjects
import com.data.db.models.helper.FlowStateStatus
import com.data.models.submissions.SubmitResultsModel
import java.util.Random

class MockData {
    companion object {
        //AssessmentSchoolHistoryDao
        fun getMockAssessmentSchoolHistory(): AssessmentSchoolHistory {
            return AssessmentSchoolHistory(
                grade = 1,
                total = 50,
                assessed = 30,
                successful = 20,
                period = "Quarterly",
                year = 2023,
                month = 12,
                updatedAt = System.currentTimeMillis()
            )
        }

        //AssessmentStateDao
        fun getAssessmentState1() = AssessmentState(0,"123", mutableListOf(),1,FlowType.ODK,"passed",
            FlowStateStatus.PENDING)
        fun getAssessmentState2() = AssessmentState(1,"456", mutableListOf(),2,FlowType.ODK,"passed",
            FlowStateStatus.COMPLETED)
        fun getAssessmentState3() = AssessmentState(1,"789", mutableListOf(),2,FlowType.ODK,"passed",
            FlowStateStatus.COMPLETED)

        fun getSubject1() = Subjects(123,"Maths")
        fun getSubject2() = Subjects(345,"Hindi")

        fun getCompetency1() = Competency(1,123,12,"dummy",1,"Jan")
        fun getCompetency2() = Competency(2,345,12,"dummy",1,"Jan")

        fun getStudent1() = Student("123","Ujjwal",12,20,false,12345)
        fun getStudent2() = Student("456","Rahul",12,21,false,12345)

        //AssessmentSubmissionsDao
        fun getDummyNullAssessmentSubmission(): AssessmentSubmission {
            return AssessmentSubmission().apply {
                id = 0
                studentId = "123"
                studentSubmissions = null
            }
        }

        fun getDummyAssessmentSubmission(): AssessmentSubmission {
            return AssessmentSubmission().apply {
                id = 0
                studentId = "123"
                studentSubmissions = generateDummySubmitResultsModel()
            }
        }

        private fun generateDummySubmitResultsModel(): SubmitResultsModel {
            return SubmitResultsModel(
                submissionDate = System.currentTimeMillis(),
                grade = 10,
                subjectId = 1,
                mentor_id = 101,
                actorId = 201,
                blockId = 301,
                assessmentTypeId = 401,
                udise = 1234567890L,
                noOfStudent = 50,
                studentResults = mutableListOf(),
                appVersionCode = 1
            )
        }

        //CycleDetailsDao
        fun getDummyCycleDetails(): CycleDetails {
            return CycleDetails(
                endDate = "2023-12-31",
                id = 3,
                name = "Cycle 3",
                startDate = "2023-12-01",
                class1NipunPercentage = 75,
                class2NipunPercentage = 75,
                class3NipunPercentage = 75
            )
        }

        fun getDummyCycleDetailsNew(): CycleDetails {
            return CycleDetails(
                endDate = "2024-01-31",
                id = 4,
                name = "Cycle 4",
                startDate = "2024-01-01",
                class1NipunPercentage = 75,
                class2NipunPercentage = 75,
                class3NipunPercentage = 75
            )
        }

        //ExaminerPerformanceInsightsDao
        fun getMockInsightsExaminer(): List<ExaminerPerformanceInsightsItem>{
            val insight = ExaminerPerformanceInsightsItem(
                insights = emptyList(),
                period = "Dec-Jan",
                cycle_id = 3,
                updated_at = System.currentTimeMillis()
            )

            val insight2 = ExaminerPerformanceInsightsItem(
                insights = emptyList(),
                period = "Feb-March",
                cycle_id = 4,
                updated_at = System.currentTimeMillis()
            )
            return listOf(insight, insight2)
        }

        fun getMockConflictInsightsExaminer(): List<ExaminerPerformanceInsightsItem>{
            val insight = ExaminerPerformanceInsightsItem(
                insights = emptyList(),
                period = "Dec-Jan",
                cycle_id = 3,
                updated_at = System.currentTimeMillis()
            )

            val insight2 = ExaminerPerformanceInsightsItem(
                insights = emptyList(),
                period = "Feb-March",
                cycle_id = 4,
                updated_at = System.currentTimeMillis()
            )

            val insight3 = ExaminerPerformanceInsightsItem(
                insights = emptyList(),
                period = "Feb-March",
                cycle_id = 4,
                updated_at = System.currentTimeMillis()
            )

            return listOf(insight, insight2, insight3)
        }

        //MetadataDao
        fun getDummyActors(): List<Actor> {
            val actor = Actor(-2,"DummyName")
            val actor2 = Actor(0,"DummyName2")
            return listOf(actor, actor2)
        }

        //SchoolsDao
        fun getDummySchoolData(): School {
            return School(
                udise = (1000000000L..9999999999L).random(),
                schoolName = listOf("SchoolA", "SchoolB", "SchoolC", "SchoolD").random(),
                schoolId = (1..1000).random(),
                visitStatus = listOf(true, false, null).random(),
                district = listOf("DistrictX", "DistrictY", "DistrictZ").random(),
                districtId = listOf(1, 2, null).random(),
                block = listOf("BlockP", "BlockQ", "BlockR").random(),
                blockId = listOf(101, 102, null).random(),
                nyayPanchayat = listOf("NP1", "NP2", "NP3").random(),
                nyayPanchayatId = listOf(201, 202, null).random(),
                schoolLat = (10.0 + Random().nextDouble() * (20.0 - 10.0)),
                schoolLong = (70.0 + Random().nextDouble() * (80.0 - 70.0)),
                geofencingEnabled = listOf(true, false, null).random()
            )
        }

        //SchoolStatusHistoryDao
        fun generateDummySchool(): School {
            return School(
                udise = 1234567890L,
                schoolName = "Dummy School",
                schoolId = 987,
                visitStatus = true,
                district = "Dummy District",
                districtId = 456,
                block = "Dummy Block",
                blockId = 789,
                nyayPanchayat = "Dummy Nyay Panchayat",
                nyayPanchayatId = 123,
                schoolLat = 15.0,
                schoolLong = 75.0,
                geofencingEnabled = false
            )
        }

        fun generateAnotherDummySchool(): School {
            return School(
                udise = 9876543210L,
                schoolName = "Another Dummy School",
                schoolId = 654,
                visitStatus = false,
                district = "Another Dummy District",
                districtId = 321,
                block = "Another Dummy Block",
                blockId = 987,
                nyayPanchayat = "Another Dummy Nyay Panchayat",
                nyayPanchayatId = 567,
                schoolLat = 18.0,
                schoolLong = 78.0,
                geofencingEnabled = true
            )
        }

        fun getSchoolStatusHistory() = SchoolStatusHistory(1234567890L,"nipun",System.currentTimeMillis(),3)
        fun getSchoolStatusHistory2() = SchoolStatusHistory(9876543210L,"notnipun",System.currentTimeMillis(),2)
        fun getSchoolStatusHistory3() = SchoolStatusHistory(8888888888L,"nipun",System.currentTimeMillis(),4)

        //SchoolSubmissionsDao
        fun getDummySchoolData2(): School {
            return School(
                udise = 123,
                schoolName = listOf("SchoolA", "SchoolB", "SchoolC", "SchoolD").random(),
                schoolId = (1..1000).random(),
                visitStatus = listOf(true, false, null).random(),
                district = listOf("DistrictX", "DistrictY", "DistrictZ").random(),
                districtId = listOf(1, 2, null).random(),
                block = listOf("BlockP", "BlockQ", "BlockR").random(),
                blockId = listOf(101, 102, null).random(),
                nyayPanchayat = listOf("NP1", "NP2", "NP3").random(),
                nyayPanchayatId = listOf(201, 202, null).random(),
                schoolLat = (10.0 + Random().nextDouble() * (20.0 - 10.0)),
                schoolLong = (70.0 + Random().nextDouble() * (80.0 - 70.0)),
                geofencingEnabled = listOf(true, false, null).random()
            )
        }

        fun getDummySchoolSubmissionData(): SchoolSubmission {
            return SchoolSubmission().apply {
                id = 0
                udise = 123
                cycleId = 3
            }
        }

        //StudentsAssessmentHistoryDao
        fun getMockStudents(): List<Student>{
            val student = Student(
                id = "1",
                name = "Ujjwal",
                grade = 10,
                rollNo = 123,
                isPlaceHolderStudent = false,
                schoolUdise = 123
            )
            val student2 = Student(
                id = "2",
                name = "Rahul",
                grade = 11,
                rollNo = 124,
                isPlaceHolderStudent = false,
                schoolUdise = 123
            )
            val student3 = Student(
                id = "3",
                name = "Ajay",
                grade = 12,
                rollNo = 125,
                isPlaceHolderStudent = false,
                schoolUdise = 123
            )
            val student4 = Student(
                id = "4",
                name = "Rohit",
                grade = 9,
                rollNo = 126,
                isPlaceHolderStudent = false,
                schoolUdise = 123
            )
            val student5 = Student(
                id = "5",
                name = "Ronit",
                grade = 10,
                rollNo = 127,
                isPlaceHolderStudent = false,
                schoolUdise = 123
            )
            return listOf(student, student2, student3, student4, student5)
        }

        fun getMockStudentsAssessmentHistory(): List<StudentAssessmentHistory>{
            val studentAssessmentHistory = StudentAssessmentHistory(
                id = "1",
                status = "Pending",
                lastAssessmentDate = System.currentTimeMillis(),
                month = 12,
                year = 2023,
                cycleId = 1,
                udise = 123
            )
            val studentAssessmentHistory2 = StudentAssessmentHistory(
                id = "2",
                status = "Pending",
                lastAssessmentDate = System.currentTimeMillis(),
                month = 12,
                year = 2023,
                cycleId = 1,
                udise = 123
            )
            val studentAssessmentHistory3 = StudentAssessmentHistory(
                id = "3",
                status = "Pending",
                lastAssessmentDate = System.currentTimeMillis(),
                month = 12,
                year = 2023,
                cycleId = 1,
                udise = 123
            )
            val studentAssessmentHistory4 = StudentAssessmentHistory(
                id = "4",
                status = "Pending",
                lastAssessmentDate = System.currentTimeMillis(),
                month = 11,
                year = 2023,
                cycleId = 1,
                udise = 123
            )
            val studentAssessmentHistory5 = StudentAssessmentHistory(
                id = "4",
                status = "Pending",
                lastAssessmentDate = System.currentTimeMillis(),
                month = 12,
                year = 2023,
                cycleId = 1,
                udise = 123
            )
            return listOf(studentAssessmentHistory, studentAssessmentHistory2, studentAssessmentHistory3, studentAssessmentHistory4, studentAssessmentHistory5)
        }

        fun getMockStudentsAssessmentHistoryInvalidData(): List<StudentAssessmentHistory>{
            val studentAssessmentHistory = StudentAssessmentHistory(
                id = "1",
                status = "Pending",
                lastAssessmentDate = System.currentTimeMillis(),
                month = 12,
                year = 2023,
                cycleId = 1,
                udise = 123
            )
            val studentAssessmentHistory2 = StudentAssessmentHistory(
                id = "2",
                status = "Pending",
                lastAssessmentDate = System.currentTimeMillis(),
                month = 12,
                year = 2023,
                cycleId = 1,
                udise = 123
            )
            val studentAssessmentHistory3 = StudentAssessmentHistory(
                id = "3",
                status = "Pending",
                lastAssessmentDate = System.currentTimeMillis(),
                month = 12,
                year = 2023,
                cycleId = 1,
                udise = 123
            )
            val studentAssessmentHistory4 = StudentAssessmentHistory(
                id = "4",
                status = "Pending",
                lastAssessmentDate = System.currentTimeMillis(),
                month = 11,
                year = 2023,
                cycleId = 1,
                udise = 123
            )
            val studentAssessmentHistory5 = StudentAssessmentHistory(
                id = "6", // not linked to parent column, foreign key, should throw exception
                status = "Pending",
                lastAssessmentDate = System.currentTimeMillis(),
                month = 12,
                year = 2023,
                cycleId = 1,
                udise = 123
            )
            return listOf(studentAssessmentHistory, studentAssessmentHistory2, studentAssessmentHistory3, studentAssessmentHistory4, studentAssessmentHistory5)
        }

        //StudentsDao
        fun getMockStudents2(): List<Student> {
            val student = Student(
                id = "1",
                name = "Ujjwal",
                grade = 10,
                rollNo = 123,
                isPlaceHolderStudent = false,
                schoolUdise = 12345
            )
            val student2 = Student(
                id = "2",
                name = "Rahul",
                grade = 11,
                rollNo = 124,
                isPlaceHolderStudent = false,
                schoolUdise = 12345
            )
            val student3 = Student(
                id = "3",
                name = "Ajay",
                grade = 12,
                rollNo = 125,
                isPlaceHolderStudent = false,
                schoolUdise = 12345
            )
            val student4 = Student(
                id = "4",
                name = "Rohit",
                grade = 9,
                rollNo = 126,
                isPlaceHolderStudent = false,
                schoolUdise = 12345
            )
            val student5 = Student(
                id = "5",
                name = "Ronit",
                grade = 10,
                rollNo = 127,
                isPlaceHolderStudent = false,
                schoolUdise = 12345
            )
            return listOf(student, student2, student3, student4, student5)
        }

        //TeacherPerformanceInsightsDao
        fun getMockInsightsTeacher(): List<TeacherPerformanceInsightsItem>{
            val insight = TeacherPerformanceInsightsItem(
                insights = emptyList(),
                period = "Dec-Jan",
                type = "Any",
                month = 12,
                year = 2023,
                updated_at = System.currentTimeMillis()
            )

            val insight2 = TeacherPerformanceInsightsItem(
                insights = emptyList(),
                period = "Feb-March",
                type = "Any",
                month = 2,
                year = 2023,
                updated_at = System.currentTimeMillis()
            )
            return listOf(insight, insight2)
        }

        fun getMockConflictInsightsTeacher(): List<TeacherPerformanceInsightsItem>{
            val insight = TeacherPerformanceInsightsItem(
                insights = emptyList(),
                period = "Dec-Jan",
                type = "Any",
                month = 12,
                year = 2023,
                updated_at = System.currentTimeMillis()
            )

            val insight2 = TeacherPerformanceInsightsItem(
                insights = emptyList(),
                period = "Feb-March",
                type = "Any",
                month = 2,
                year = 2023,
                updated_at = System.currentTimeMillis()
            )

            val insight3 = TeacherPerformanceInsightsItem(
                insights = emptyList(),
                period = "Feb-March",
                type = "Any",
                month = 2,
                year = 2023,
                updated_at = System.currentTimeMillis()
            )
            return listOf(insight, insight2, insight3)
        }

    }
}