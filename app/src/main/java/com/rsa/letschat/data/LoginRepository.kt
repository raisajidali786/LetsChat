package com.rsa.letschat.data

import android.text.TextUtils
import android.util.Patterns
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.rsa.letschat.model.UserModel

class LoginRepository {
    val loginUserLiveData = MutableLiveData<String>()
    private val db = FirebaseFirestore.getInstance()

    fun signInUser(email: String, name: String, password: String, userImage: String = "") {
        if (TextUtils.isEmpty(email)) {
            loginUserLiveData.value = "Please enter email"
            return
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            loginUserLiveData.value = "Please valid email"
            return
        }
        if (TextUtils.isEmpty(name)) {
            loginUserLiveData.value = "Please enter name"
            return
        }
        if (TextUtils.isEmpty(password)){
            loginUserLiveData.value = "Please enter password"
            return
        }
        if (password.length<6){
            loginUserLiveData.value = "Password should be minimum 6 digit"
            return
        }
        if (password.length>16){
            loginUserLiveData.value = "Password should be maximum 16 digit"
            return
        }
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                if (it.isSuccessful){

                    val user = UserModel(name,userImage,email)

                    db.collection("Users").document()
                        .set(user)
                        .addOnSuccessListener {
                            loginUserLiveData.value = "REGISTER SUCCESSFULLY"
                        }
                        .addOnFailureListener {
                            loginUserLiveData.value = "Something Wrong"
                        }

                }else{
                    loginUserLiveData.value = "Something Wrong"
                }
            }
            .addOnFailureListener {
                loginUserLiveData.value = "Something Wrong"
            }
    }
}