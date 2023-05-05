package com.putu.storyapp.viewmodel

import androidx.lifecycle.*
import com.putu.storyapp.extra.UserPreferences
import com.putu.storyapp.data.response.LoginResponse
import com.putu.storyapp.data.retrofit.ApiConfig
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginViewModel(private val pref: UserPreferences) : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading : LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage : LiveData<String> = _errorMessage

    private var job: Job? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onError("Exception handled : ${throwable.localizedMessage}")
    }


    fun login(email: String, password: String) {
        _isLoading.value = true
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val client = ApiConfig.getApiService().login(email, password)
            withContext(Dispatchers.Main) {
                client.enqueue(object : Callback<LoginResponse> {
                    override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                        _isLoading.value = false
                        if (response.isSuccessful) {
                            if(response.body() != null) {
                                response.body()!!.loginResult?.let {
                                    saveAuthToken(it.token)
                                }

                                response.body()!!.message?.let {
                                    onError(it)
                                }
                            }
                        } else {
                            onError("Error : ${response.message()}")
                        }
                    }

                    override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                        _isLoading.value = false
                        onError("onFailure")
                    }
                })
            }
        }
    }

    fun saveAuthToken(token: String) {
        viewModelScope.launch {
            pref.saveAuthToken(token)
        }
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
