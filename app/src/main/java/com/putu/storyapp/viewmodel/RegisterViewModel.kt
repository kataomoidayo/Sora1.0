package com.putu.storyapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.putu.storyapp.data.response.RegisterResponse
import com.putu.storyapp.data.retrofit.ApiConfig
import kotlinx.coroutines.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterViewModel : ViewModel() {

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading : LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage : LiveData<String> = _errorMessage

    private var job: Job? = null

    private val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        onError("Exception handled : ${throwable.localizedMessage}")
    }


    fun register (name: String, email: String, password: String) {
        _isLoading.value = true
        job = CoroutineScope(Dispatchers.IO + exceptionHandler).launch {
            val client = ApiConfig.getApiService().register(name, email, password)
            withContext(Dispatchers.Main) {
                client.enqueue(object : Callback<RegisterResponse> {
                    override fun onResponse(call: Call<RegisterResponse>, response: Response<RegisterResponse>) {
                        _isLoading.value = false
                        if (response.isSuccessful) {
                            if(response.body() != null) {
                                response.body()!!.message?.let {
                                    onError(it)
                                }
                            }
                        } else {
                            onError("Error : ${response.message()}")
                        }
                    }

                    override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                        _isLoading.value = false
                        onError("onFailure")
                    }
                })
            }
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