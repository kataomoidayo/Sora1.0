package com.putu.storyapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.putu.storyapp.data.model.UserModel
import com.putu.storyapp.data.response.UploadResponse
import com.putu.storyapp.data.retrofit.ApiConfig
import com.putu.storyapp.extra.UserPreferences
import kotlinx.coroutines.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PostStoryViewModel(private val pref: UserPreferences) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private var job: Job? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onError("Exception handled : ${throwable.localizedMessage}")
    }


    fun postStory(token: String, file: MultipartBody.Part, description: RequestBody) {
        _isLoading.value = true
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val client = ApiConfig.getApiService().postStory("Bearer $token", file, description)
            withContext(Dispatchers.Main) {
                client.enqueue(object : Callback<UploadResponse> {
                    override fun onResponse(call: Call<UploadResponse>, response: Response<UploadResponse>) {
                        _isLoading.value = false
                        if (response.isSuccessful) {
                            if (response.body() != null) {
                                response.body()!!.message?.let {
                                    onError(it)
                                }
                            }
                        } else {
                            onError("Error : ${response.message()}")
                        }
                    }

                    override fun onFailure(call: Call<UploadResponse>, t: Throwable) {
                        _isLoading.value = false
                        onError("onFailure")
                    }
                })
            }
        }
    }

    fun getUser(): LiveData<UserModel> {
        return pref.getUser().asLiveData()
    }

    private fun onError(message: String) {
        _errorMessage.value = message
        _isLoading.value = false
    }

    override fun onCleared() {
        super.onCleared()
        job?.cancel()
    }
}