package com.devloook.blogapp

import android.os.Bundle
import android.widget.ImageButton
import com.devloook.blogapp.Model.BlogItemModel
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.devloook.blogapp.adapter.BlogAdapter
import com.devloook.blogapp.databinding.ActivitySavedArticlesBinding
import com.devloook.blogapp.databinding.BlogItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import okhttp3.Dispatcher


class SavedArticlesActivity : AppCompatActivity() {

    private val binding: ActivitySavedArticlesBinding by lazy{
        ActivitySavedArticlesBinding.inflate(layoutInflater)
    }
    private val savedBlogsArticles = mutableListOf<BlogItemModel>()
    private lateinit var blogAdapter: BlogAdapter
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        val backButton: ImageButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener { finish() }

        // Initialize blogAdapter
        blogAdapter = BlogAdapter(savedBlogsArticles.filter { it.isSaved }.toMutableList())

        val recyclerView : RecyclerView = binding.savedArticlesRecyclerView
        recyclerView.adapter = blogAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Get the userId of user
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userReference = FirebaseDatabase.getInstance()
                .getReference("users").child(userId).child("savedPosts")

            userReference.addListenerForSingleValueEvent(object: ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (postSnapshot: DataSnapshot in snapshot.children){
                        val postId: String? = postSnapshot.key
                        val isSaved: Boolean = postSnapshot.value as Boolean

                        if(postId != null && isSaved){
                            // Fetch the Corresponding Blog Item and PostId using a coroutine
                            CoroutineScope(Dispatchers.IO).launch {
                                val blogItem = fetchBlogItem(postId)

                                if (blogItem != null) {
                                    savedBlogsArticles.add(blogItem)

                                    launch(Dispatchers.Main){
                                        blogAdapter.updateData(savedBlogsArticles)
                                    }
                                }
                            }
                        }
                    }
                }



                override fun onCancelled(error: DatabaseError) {

                }
            })
        }

    }
    private suspend fun fetchBlogItem(postId: String): BlogItemModel? {
        val blogReference = FirebaseDatabase.getInstance().getReference("blogs")
        
        return try {
            val dataSnapshot: DataSnapshot = blogReference.child(postId).get().await()
            val blogData: BlogItemModel? = dataSnapshot.getValue(BlogItemModel::class.java)
            blogData
        } catch (e: Exception){
            null
        }
    }
}