package com.samagra.commons.models

import java.io.Serializable

data class OdkResultData(val totalQuestions:Int , val totalMarks:String = "", val results : ArrayList<Results> = ArrayList()):Serializable

//answer 0 false and 1 true.
data class Results(var question:String, var answer:String):Serializable
