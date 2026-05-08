package edu.cit.cordero.glamsched.features.booking

data class ReviewDto(
    val id: Long? = null,
    val artistId: Long = 0L,
    val clientId: Long = 0L,
    val clientName: String? = null,
    val rating: Int = 5,
    val comment: String? = null,
    val createdAt: String? = null
)
