package edu.cit.cordero.glamsched.features.dashboard

data class ServiceDto(
    val id: Long = 0,
    val name: String? = null,
    val description: String? = null,
    val price: Double? = null,
    val category: String? = null,
    val duration: String? = null,
    val artistId: Long? = null,
    val artistName: String? = null,
    val artistProfileImage: String? = null,
    val photos: List<String>? = null,
    val reactionCount: Long = 0,
    val likedByMe: Boolean = false,
    val followedByMe: Boolean = false
)
