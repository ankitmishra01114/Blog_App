package com.devloook.blogapp.Model

data class BlogItemModel(
    val heading: String = "null",
    val userName: String = "null",
    val date: String = "null",
    val post: String = "null",
    val likeCount: Int = 0,
    val profileImage: String = "null"
)
