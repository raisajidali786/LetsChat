package com.rsa.letschat.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.rsa.letschat.data.LoginRepository

class RegisterUserViewModel : ViewModel() {
    val registerUserLiveData = MutableLiveData<String>()
    private val registerUserRepository = LoginRepository()


}