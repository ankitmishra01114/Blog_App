package com.devloook.blogapp

import com.devloook.blogapp.Model.UserData
import com.devloook.blogapp.Model.BlogItemModel
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.devloook.blogapp.databinding.ActivityAddArticleBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*

class AddArticleActivity : AppCompatActivity() {

    private val binding: ActivityAddArticleBinding by lazy {
        ActivityAddArticleBinding.inflate(layoutInflater)
    }

    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("blogs")
    private val userReference: DatabaseReference = FirebaseDatabase.getInstance().getReference("users")
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        // Back button
        binding.imageButton.setOnClickListener { finish() }

        binding.addBlogButton.setOnClickListener {
            val title = binding.blogTitle.text.toString().trim()
            val description = binding.blogDescription.text.toString().trim()

            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please Fill All The Fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user: FirebaseUser? = auth.currentUser
            if (user != null) {
                val userId = user.uid

                // Fetch user data from DB
                userReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userData: UserData? = snapshot.getValue(UserData::class.java)
                        if (userData != null) {
                            val userNameFromDB = userData.name
                            val userImageUrlFromDB = userData.profileImageUrl

                            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                            val key: String? = databaseReference.push().key

                            if (key != null) {
                                val blogItemModel = BlogItemModel(
                                    heading = title,
                                    userName = userNameFromDB,
                                    date = currentDate,
                                    post = description,
                                    likeCount = 0,
                                    profileImage = userImageUrlFromDB,
                                    postId = key,
                                    likedBy = mutableListOf(), // initialize empty list
                                    isSaved = false
                                )

                                databaseReference.child(key).setValue(blogItemModel)
                                    .addOnSuccessListener {
                                        Toast.makeText(this@AddArticleActivity, "Blog Added Successfully!", Toast.LENGTH_SHORT).show()
                                        finish()
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this@AddArticleActivity, "Failed to add blog: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@AddArticleActivity, "Failed to fetch user data: ${error.message}", Toast.LENGTH_SHORT).show()
                    }
                })
            } else {
                Toast.makeText(this, "Please Login First", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
