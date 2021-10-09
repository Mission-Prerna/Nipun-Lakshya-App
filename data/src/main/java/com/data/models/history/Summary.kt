package com.data.models.history

import com.google.gson.annotations.SerializedName

class Summary(
    val total: Int,
    val assessed: Int,
    val successful: Int,
    val period: String,
    val year: Int,
    val month: Int,
    @SerializedName("updated_at")
    val updatedAt: Long
)