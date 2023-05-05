package com.putu.storyapp.data.model

import com.google.gson.annotations.SerializedName

data class UserModel(

    @field:SerializedName("userId")
    var userId: String,

    @field:SerializedName("name")
    var name: String,

    @field:SerializedName("token")
    var token: String,

    var isLogin: Boolean

)
