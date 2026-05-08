package edu.cit.cordero.glamsched.features.dashboard

data class UserDto(
    val id: Long = 0,
    val name: String? = null,
    val email: String? = null,
    val role: String? = null,
    val phone: String? = null,
    val address: String? = null,
    val bio: String? = null,
    val profileImage: String? = null,
    val coverImage: String? = null,
    val createdAt: String? = null,
    val followerCount: Long = 0,
    val followedByMe: Boolean = false
)
