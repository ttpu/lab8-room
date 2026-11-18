package uz.student.roomnotes.network

import com.google.gson.annotations.SerializedName

// 20-leksiya, 6-slayd: JSON ning faqat kerakli qismini olamiz
data class User(
    val login: String,
    val id: Long,
    @SerializedName("avatar_url")
    val avatarUrl: String
)
