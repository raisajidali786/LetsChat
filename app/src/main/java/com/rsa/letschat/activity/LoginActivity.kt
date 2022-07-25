package com.rsa.letschat.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.rsa.letschat.R
import com.rsa.letschat.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLoginBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_login)
        clickListener()
    }

    private fun clickListener() {
        binding.btnRegister.setOnClickListener {
            letMeMove(RegisterActivity())
        }
        binding.btnLogin.setOnClickListener {
            letMeMove((MainActivity()))
        }
        binding.tvForgetPassword.setOnClickListener {
            letMeMove((ForgetPasswordActivity()))
        }
        binding.showPassword.setOnClickListener {
            binding.password.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.showPassword.visibility = View.GONE
            binding.hidePassword.visibility = View.VISIBLE
        }
        binding.hidePassword.setOnClickListener {
            binding.password.inputType =
                InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.showPassword.visibility = View.VISIBLE
            binding.hidePassword.visibility = View.GONE
        }

    }

    private fun letMeMove(activity: Activity) {
        startActivity(Intent(this, activity::class.java))
    }
}