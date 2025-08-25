package com.devloook.blogapp

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.devloook.blogapp.R
import com.bumptech.glide.request.RequestOptions
import com.devloook.blogapp.Model.BlogItemModel
import com.devloook.blogapp.databinding.ActivityReadMoreBinding

class ReadMoreActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReadMoreBinding
    override fun onCreate(savedInstanceState: Bundle?) {

        binding = ActivityReadMoreBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        binding.backButton.setImageResource(R.drawable.stroke1)
        binding.backButton.setOnClickListener {
            finish()
        }

        val blogs: BlogItemModel? = intent.getParcelableExtra<BlogItemModel>("blogItem")

        //Using of Scrolling Movement Method is use for make Blog Description Scrollable

        binding.blogDescriptionTextView.movementMethod = ScrollingMovementMethod()

        if(blogs != null){
            // Retrive User Related Data here e. x blog title etc.
            binding.titleText.text = blogs.heading
            binding.userName.text = blogs.userName
            binding.date.text = blogs.date
            binding.blogDescriptionTextView.text = blogs.post

            val userImageUri: String? = blogs.profileImage
            Glide.with(this)
                .load(userImageUri)
                .apply(RequestOptions.circleCropTransform())
                .into(binding.profileImage)

        }else{
            Toast.makeText(this, "Failed to Load Blogs", Toast.LENGTH_SHORT).show()
        }

    }
}