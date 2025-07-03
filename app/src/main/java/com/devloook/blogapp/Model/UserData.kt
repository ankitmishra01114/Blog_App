package com.devloook.blogapp.Model

data class UserData(
    val name: String = "",
    val email: String = "",
    val profileImageUrl: String = ""
){
    constructor() : this(name = "", email = "", profileImageUrl = "")
}
