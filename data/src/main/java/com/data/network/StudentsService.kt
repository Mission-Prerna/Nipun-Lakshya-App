package com.data.network

import com.data.db.models.entity.Student
import com.data.db.models.StudentAssessmentHistoryCompleteInfo
import com.morziz.network.annotation.Retry
import com.morziz.network.annotation.RetryPolicyType
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface StudentsService {

    @GET("school/{udise}/students")
    @Retry(retryPolicy = RetryPolicyType.exponential, retryCount = 2)
    suspend fun getStudents(@Path("udise") udise: String, @Header("authorization") token : String): Response<MutableList<Student>>

    @GET("school/{udise}/students/result")
    @Retry(retryPolicy = RetryPolicyType.exponential, retryCount = 2)
    suspend fun getStudentAssessmentHistoryInfo(
        @Path("udise") udise: String,
        @Header("authorization") token : String,
        @Header("Accept-Language") lang: String,
        @Query("grade") grade: String,
        @Query("month") month: Int,
        @Query("year") year: Int
    ): Response<List<StudentAssessmentHistoryCompleteInfo>>

}