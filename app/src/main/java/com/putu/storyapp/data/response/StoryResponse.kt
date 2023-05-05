package com.putu.storyapp.data.response

import com.google.gson.annotations.SerializedName
import com.putu.storyapp.data.model.StoryModel

data class StoryResponse(

    @field:SerializedName("error")
    val error: Boolean? = null,

    @field:SerializedName("message")
    val message: String? = null,

    @field:SerializedName("listStory")
    val stories: List<StoryModel>? = null

)
