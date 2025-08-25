package com.devloook.blogapp.adapter

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.devloook.blogapp.Model.BlogItemModel
import com.devloook.blogapp.R
import com.devloook.blogapp.ReadMoreActivity
import com.devloook.blogapp.databinding.BlogItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class BlogAdapter(private val items: MutableList<BlogItemModel>) : RecyclerView.Adapter<BlogAdapter.BlogViewHolder>() {

    private val databaseReference: DatabaseReference = FirebaseDatabase.getInstance().reference
    private val currentUser = FirebaseAuth.getInstance().currentUser

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogViewHolder {
        val inflater: LayoutInflater = LayoutInflater.from(parent.context)
        val binding: BlogItemBinding = BlogItemBinding.inflate(inflater, parent, false)
        return BlogViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BlogViewHolder, position: Int) {
        val blogItem: BlogItemModel = items[position]
        holder.bind(blogItem)
    }

    override fun getItemCount(): Int = items.size

    inner class BlogViewHolder(private val binding: BlogItemBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(blogItemModel: BlogItemModel) {
            val postId: String = blogItemModel.postId
            val context = binding.root.context

            binding.heading.text = blogItemModel.heading
            Glide.with(binding.profile.context).load(blogItemModel.profileImage).into(binding.profile)
            binding.userName.text = blogItemModel.userName
            binding.date.text = blogItemModel.date
            binding.post.text = blogItemModel.post
            binding.likeCount.text = blogItemModel.likeCount.toString()

            binding.readMoreButton.setOnClickListener {
                val intent = Intent(context, ReadMoreActivity::class.java)
                intent.putExtra("blogItem", blogItemModel)
                context.startActivity(intent)
            }

            val postLikeReference = databaseReference.child("blogs").child(postId).child("likes")
            currentUser?.uid?.let { uid ->
                postLikeReference.child(uid).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val liked = snapshot.exists()
                        updateLikeButtonImage(binding, liked)
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }

            binding.likeButton.setOnClickListener {
                if (currentUser != null) {
                    handleLikeButtonClicked(postId, blogItemModel, binding)
                } else {
                    Toast.makeText(context, "Please Login First", Toast.LENGTH_SHORT).show()
                }
            }

            // SAVE BUTTON STATE UPDATE
            currentUser?.uid?.let { uid ->
                val savedRef = databaseReference.child("users").child(uid).child("savedPosts").child(postId)
                savedRef.addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        val saved = snapshot.exists()
                        updateSaveButtonImage(binding, saved)
                    }

                    override fun onCancelled(error: DatabaseError) {}
                })
            }

            binding.postSaveButton.setOnClickListener {
                if (currentUser != null) {
                    handleSaveButtonClicked(postId, blogItemModel, binding)
                } else {
                    Toast.makeText(context, "Please Login First", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleLikeButtonClicked(postId: String, blogItemModel: BlogItemModel, binding: BlogItemBinding) {
        val userRef = databaseReference.child("users").child(currentUser!!.uid)
        val postLikeRef = databaseReference.child("blogs").child(postId).child("likes")

        postLikeRef.child(currentUser.uid).addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    userRef.child("likes").child(postId).removeValue()
                    postLikeRef.child(currentUser.uid).removeValue()
                    blogItemModel.likedBy?.remove(currentUser.uid)

                    val newLikeCount = (blogItemModel.likeCount - 1).coerceAtLeast(0)
                    blogItemModel.likeCount = newLikeCount
                    binding.likeCount.text = newLikeCount.toString()
                    updateLikeButtonImage(binding, false)
                    databaseReference.child("blogs").child(postId).child("likeCount").setValue(newLikeCount)
                } else {
                    userRef.child("likes").child(postId).setValue(true)
                    postLikeRef.child(currentUser.uid).setValue(true)
                    blogItemModel.likedBy?.add(currentUser.uid)

                    val newLikeCount = blogItemModel.likeCount + 1
                    blogItemModel.likeCount = newLikeCount
                    binding.likeCount.text = newLikeCount.toString()
                    updateLikeButtonImage(binding, true)
                    databaseReference.child("blogs").child(postId).child("likeCount").setValue(newLikeCount)
                }
                notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun updateLikeButtonImage(binding: BlogItemBinding, liked: Boolean) {
        binding.likeButton.setImageResource(if (liked) R.drawable.icon2 else R.drawable.icon1)
    }

    private fun handleSaveButtonClicked(postId: String, blogItemModel: BlogItemModel, binding: BlogItemBinding) {
        val userRef = databaseReference.child("users").child(currentUser!!.uid)
        val savePostRef = userRef.child("savedPosts").child(postId)
        val context = binding.root.context

        savePostRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    savePostRef.removeValue().addOnSuccessListener {
                        blogItemModel.isSaved = false
                        updateSaveButtonImage(binding, false)
                        Toast.makeText(context, "Blog Unsaved", Toast.LENGTH_SHORT).show()
                        notifyDataSetChanged()
                    }.addOnFailureListener {
                        Toast.makeText(context, "Failed to Unsave Blog", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    savePostRef.setValue(true).addOnSuccessListener {
                        blogItemModel.isSaved = true
                        updateSaveButtonImage(binding, true)
                        Toast.makeText(context, "Blog Saved!", Toast.LENGTH_SHORT).show()
                        notifyDataSetChanged()
                    }.addOnFailureListener {
                        Toast.makeText(context, "Failed to Save Blog", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(context, "Database Error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateSaveButtonImage(binding: BlogItemBinding, saved: Boolean) {
        binding.postSaveButton.setImageResource(if (saved) R.drawable.vector1 else R.drawable.combined_shape4)
    }

    fun updateData(savedBlogsArticles: List<BlogItemModel>) {
        items.clear()
        items.addAll(savedBlogsArticles)
        notifyDataSetChanged()
    }
}
