package com.devloook.blogapp.Model

data class BlogItemModel(
    val heading: String,
    val userName: String,
    val date: String,
    val post: String,
    val likeCount: Int,
    val imageUrl: String
)
