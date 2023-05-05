package com.putu.storyapp.data.response

import com.google.gson.annotations.SerializedName
import com.putu.storyapp.data.model.UserModel

data class LoginResponse(

    @field:SerializedName("error")
    var error: Boolean? = null,

    @field:SerializedName("message")
    var message: String? = null,

    @field:SerializedName("loginResult")
    var loginResult: UserModel? = null

)
