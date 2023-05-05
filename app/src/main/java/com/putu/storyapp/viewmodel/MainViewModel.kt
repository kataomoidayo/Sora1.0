package com.putu.storyapp.viewmodel

import androidx.lifecycle.*
import com.putu.storyapp.data.model.StoryModel
import com.putu.storyapp.data.model.UserModel
import com.putu.storyapp.data.response.StoryResponse
import com.putu.storyapp.data.retrofit.ApiConfig
import com.putu.storyapp.extra.UserPreferences
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel(private val pref: UserPreferences): ViewModel() {

    private val _storyList = MutableLiveData<List<StoryModel>?>()
    val storyList : MutableLiveData<List<StoryModel>?> = _storyList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading : LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage : LiveData<String> = _errorMessage

    private var job: Job? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onError("Exception handled : ${throwable.localizedMessage}")
    }


    fun getAllStories(token : String) {
        _isLoading.value = true
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val client = ApiConfig.getApiService().getAllStories("Bearer $token")
            withContext(Dispatchers.Main) {
                client.enqueue(object : Callback<StoryResponse> {
                    override fun onResponse(call: Call<StoryResponse>, response: Response<StoryResponse>) {
                        _isLoading.value = false
                        if (response.isSuccessful) {
                            if (response.body() != null) {
                                _storyList.value = response.body()!!.stories
                                response.body()!!.message?.let {
                                    onError(it)
                                }
                            }
                        } else {
                            onError("Error : ${response.message()}")
                        }
                    }

                    override fun onFailure(call: Call<StoryResponse>, t: Throwable) {
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