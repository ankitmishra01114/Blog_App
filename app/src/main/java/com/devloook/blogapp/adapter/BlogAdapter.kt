package com.devloook.blogapp.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.devloook.blogapp.R
import com.bumptech.glide.Glide
import com.devloook.blogapp.Model.BlogItemModel
import com.devloook.blogapp.ReadMoreActivity
import com.devloook.blogapp.databinding.BlogItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener


class BlogAdapter(private val items: List<BlogItemModel>): RecyclerView.Adapter<BlogAdapter.BlogViewHolder>() {

    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val currentUser = FirebaseAuth.getInstance().currentUser


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(parent.context)
        val binding: BlogItemBinding = BlogItemBinding.inflate(inflater, parent, false)
        return BlogViewHolder(binding)
    }



    override fun onBindViewHolder(holder: BlogViewHolder,position: Int) {
        val blogItem: BlogItemModel = items[position]
        holder.bind(blogItem)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class BlogViewHolder(private val binding: BlogItemBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(blogItemModel: BlogItemModel) {

            val postId: String = blogItemModel.postId
            val context = binding.root.context

            binding.heading.text = blogItemModel.heading
            Glide.with(binding.profile.context)
                .load(blogItemModel.profileImage)
                .into(binding.profile)
            binding.userName.text = blogItemModel.userName
            binding.date.text = blogItemModel.date
            binding.post.text = blogItemModel.post
            binding.likeCount.text = blogItemModel.likeCount.toString()

            // Set on Click Listener
            // For Read More Button when clicking on it will redirect to ReadMoreActivity and open the blog in big screen
            binding.readMoreButton.setOnClickListener {
                // Handle click event
                val context = binding.root.context
                val intent = Intent(context, ReadMoreActivity::class.java)
                intent.putExtra("blogItem", blogItemModel)
                context.startActivity(intent)
            }

            // Check If Current User has already liked the post Like the Post and update the like button accordingly
            val postLikeReference = databaseReference.child("blogs").child(postId).child("likes")

            val currentUserLiked: Unit? = currentUser?.uid?.let { uid ->
                postLikeReference.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.exists()){
                            binding.likeButton.setImageResource(R.drawable.icon2)
                        }else{
                            binding.likeButton.setImageResource(R.drawable.icon1)
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {

                    }

                })

            }

            // Handle Like Button Clicks
            binding.likeButton.setOnClickListener {
                if (currentUser != null){
                    handleLikeButtonClicked(postId, blogItemModel, binding)
                }else{
                    Toast.makeText(context, "Please Login First", Toast.LENGTH_SHORT).show()
                }
            }


            // Handle Save Button Clicks
            binding.postSaveButton.setOnClickListener {
                if (currentUser != null){
                    handleSaveButtonClicked(postId, blogItemModel, binding)
                }else{
                    Toast.makeText(context, "Please Login First", Toast.LENGTH_SHORT).show()
                }
            }



        }

    }


    // Method for handle like functionality, Like or Unlike the post & Increment and Decrement the like count
    private fun handleLikeButtonClicked(postId: String, blogItemModel: BlogItemModel, binding: BlogItemBinding) {
        val userReference = databaseReference.child("users").child(currentUser!!.uid)
        val postLikeReference = databaseReference.child("blogs").child(postId).child("likes")

        // Check User has already like the post, So unlike it

        postLikeReference.child(currentUser.uid).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()){
                    userReference.child("likes").child(postId).removeValue()
                        .addOnSuccessListener {
                            postLikeReference.child(currentUser.uid).removeValue()
                            blogItemModel.likedBy?.remove(currentUser.uid)


                            // Decrement the like in the database
                            val newLikeCount = if (blogItemModel.likeCount > 0) blogItemModel.likeCount - 1 else 0
                            blogItemModel.likeCount = newLikeCount

                            updateLikeButtonImage(binding, false)

                            // Update in Firebase (no need to add "blogs" again)
                            databaseReference.child("blogs").child(postId).child("likeCount").setValue(newLikeCount)


                            notifyDataSetChanged()
                        }
                        .addOnFailureListener { e ->
                            Log.e("LikedClicked", "onDataChange: Failed to Unlike the Blog $e", )
                        }
                }else{
                    // User has not liked the post, so like it
                    userReference.child("likes").child(postId).setValue(true)
                        .addOnSuccessListener {
                            postLikeReference.child(currentUser.uid).setValue(true)
                            blogItemModel.likedBy?.add(currentUser.uid)


                            // Increment the like count in the database
                            val newLikeCount = blogItemModel.likeCount + 1
                            blogItemModel.likeCount = newLikeCount

                            updateLikeButtonImage(binding, true)

                            // Update in Firebase (no need to add "blogs" again)
                            databaseReference.child("blogs").child(postId).child("likeCount").setValue(newLikeCount)


                            notifyDataSetChanged()
                        }
                        .addOnFailureListener { e ->
                            Log.e("LikedClicked", "onDataChange: Failed to Like the Blog $e", )
                        }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }

    // Method for updating Like Button Image
    private fun updateLikeButtonImage(binding: BlogItemBinding, liked: Boolean){
        binding.likeButton.setImageResource(if (liked) R.drawable.icon2 else R.drawable.icon1 )
    }


    // Method for handle Save Post functionality, Save or UnSave the post
    private fun handleSaveButtonClicked(postId: String, blogItemModel: BlogItemModel, binding: BlogItemBinding){
        val userReference = databaseReference.child("users").child(currentUser!!.uid)
        userReference.child("savedPosts").child(postId).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                // Check if data is available or not
                if(snapshot.exists()){
                    // The blog is currently saved by the user, So unsaved it
                    userReference.child("savePosts").child(postId).removeValue()
                        .addOnSuccessListener {
                            // Update the UI
                            val clickedBlogItem: BlogItemModel? = items.find{ it.postId == postId}
                            clickedBlogItem?.isSaved = false

                            notifyDataSetChanged()

                            val context: Context? = binding.root.context
                            Toast.makeText(context, "Blog Unsaved", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener{ e ->
                            val context: Context? = binding.root.context
                            Toast.makeText(context, "Failed to Unsaved the Blog", Toast.LENGTH_SHORT).show()
                        }

                    // Change the Save button image
                    binding.postSaveButton.setImageResource(R.drawable.combined_shape4)
                }else{
                    // The blog is not saved by the user, So save it
                    userReference.child("savedPosts").child(postId).setValue(true)
                        .addOnSuccessListener {
                            // Update the UI
                            val clickedBlogItem: BlogItemModel? = items.find{ it.postId == postId}
                            clickedBlogItem?.isSaved = true

                            notifyDataSetChanged()

                            val context: Context? = binding.root.context
                            Toast.makeText(context, "Blog Saved!", Toast.LENGTH_SHORT).show()

                        }
                        .addOnFailureListener {
                            val context: Context? = binding.root.context
                            Toast.makeText(context, "Failed to Save the Blog", Toast.LENGTH_SHORT).show()
                        }
                }



            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

}