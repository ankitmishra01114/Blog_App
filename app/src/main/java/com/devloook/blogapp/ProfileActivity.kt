package com.devloook.blogapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.devloook.blogapp.databinding.ActivityProfileBinding
import com.devloook.blogapp.register.WelcomeActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    private val binding: ActivityProfileBinding by lazy {
        ActivityProfileBinding.inflate(layoutInflater)
    }
    private lateinit var databaseReference: DatabaseReference
    private lateinit var auth: FirebaseAuth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.backButton.setOnClickListener { finish() }

        // Initialize Firebase
        auth = FirebaseAuth.getInstance()
        databaseReference = FirebaseDatabase.getInstance().reference.child("users")

        val userId: String? = auth.currentUser?.uid


        if (userId != null) {
            loadUserProfileData(userId)
        }

        binding.addNewBlogButton.setOnClickListener{
            startActivity(Intent(this,AddArticleActivity::class.java))
        }

        // Log Out the user and send back to login register page
        binding.logOutButton.setOnClickListener {
            // Sign out the current user
            FirebaseAuth.getInstance().signOut()

            // Redirect to LoginActivity (or Splash/Welcome)
            val intent = Intent(this, WelcomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)

            finish() // Close current activity so user can't go back
        }

    }

    private fun loadUserProfileData(userId: String) {
        val userReference: DatabaseReference = databaseReference.child(userId)

        // Load User Profile Image
        userReference.child("profileImage").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val profileImageUrl = snapshot.getValue(String::class.java)
                if (profileImageUrl != null){
                    Glide.with(this@ProfileActivity)
                        .load(profileImageUrl)
                        .into(binding.userProfile)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProfileActivity,"Failed to load user image 😒",Toast.LENGTH_SHORT).show()
            }

        })

        // Load user name
        userReference.child("name").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val userName = snapshot.getValue(String::class.java)

                if (userName != null){
                    binding.userName.text = userName
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }
}