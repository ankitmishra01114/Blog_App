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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date

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

        binding.addBlogButton.setOnClickListener {
            val title = binding.blogTitle.text.toString().trim()
            val description = binding.blogDescription.text.toString().trim()


            if (title.isEmpty() || description.isEmpty()){
                Toast.makeText(this, "Please Fill All The Fields", Toast.LENGTH_SHORT).show()
            }

            // Get current user
            val user: FirebaseUser? = auth.currentUser

            if (user != null){
                val userId: String = user.uid
                val userName: String? = user.displayName?:"Anonymous"
                val userImageUrl: String = user.photoUrl?.toString() ?: ""

                // Fetch user name and user profile from database
                userReference.child(userId).addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val userData: UserData? = snapshot.getValue(UserData::class.java)

                        if (userData != null) {
                            val userNameFromDB: String = userData.name
                            val userImageUrlFromDB: String = userData.profileImageUrl

                            val currentDate = SimpleDateFormat("yyyy-MM-dd").format(Date())

                            // Create a blogItemModel
                            val blogItemModel = BlogItemModel(
                                title,
                                userNameFromDB,
                                currentDate,
                                description,
                                likeCount = 0,
                                userImageUrlFromDB
                            )
                            // Generate a unique key for the blog post
                            val key: String? = databaseReference.push().key

                            if (key != null) {
                                val blogReference: DatabaseReference = databaseReference.child(key)
                                blogReference.setValue(blogItemModel).addOnSuccessListener {
                                    if (it.isSuccessfull){
                                        finish()
                                    }else{
                                        Toast.makeText(this@AddArticleActivity, "Failed to add blog", Toast.LENGTH_SHORT).show()
                                    }
                                }

                            }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        TODO("Not yet implemented")
                    }
                })
            }
        }

    }
}