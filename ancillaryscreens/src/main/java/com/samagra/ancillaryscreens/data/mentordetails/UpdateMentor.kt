package com.samagra.ancillaryscreens.data.mentordetails

import com.hasura.model.UpdateMentorPinMutation

data class UpdateMentor(
    val returning: MutableList<UpdateMentorPinMutation.Returning>
)