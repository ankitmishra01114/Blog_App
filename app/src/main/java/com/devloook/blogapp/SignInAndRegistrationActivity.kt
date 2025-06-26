package com.devloook.blogapp

import android.opengl.Visibility
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.devloook.blogapp.databinding.ActivitySignInAndRegistrationBinding

class SignInAndRegistrationActivity : AppCompatActivity() {
    private val binding: ActivitySignInAndRegistrationBinding by lazy {
        ActivitySignInAndRegistrationBinding.inflate(layoutInflater)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)


        val action = intent.getStringExtra("action")
        // Adjust Visibility For Login
        if(action == "login"){
            binding.loginEmailAddress.visibility = View.VISIBLE
            binding.loginPassword.visibility = View.VISIBLE
            binding.loginButton.visibility = View.VISIBLE

            binding.registerName.visibility = View.GONE
            binding.registerEmail.visibility = View.GONE
            binding.registerPassword.visibility = View.GONE
            binding.cardView.visibility = View.GONE
            binding.registerButton.isEnabled = false
            binding.registerButton.alpha = 0.5f
            binding.registerNewHere.isEnabled = false
            binding.registerNewHere.alpha = 0.5f
        }else if (action == "register"){
            binding.loginButton.isEnabled = false
            binding.loginButton.alpha = 0.5f

            binding.loginEmailAddress.visibility = View.GONE
            binding.loginPassword.visibility = View.GONE

            binding.registerName.visibility = View.VISIBLE
            binding.registerEmail.visibility = View.VISIBLE
            binding.registerPassword.visibility = View.VISIBLE
            binding.cardView.visibility = View.VISIBLE

        }

    }
}