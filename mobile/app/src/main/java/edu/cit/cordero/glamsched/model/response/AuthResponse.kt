package edu.cit.cordero.glamsched.model.response

data class AuthResponse(
    val user: UserData?,
    val accessToken: String?,
    val refreshToken: String?
)

data class UserData(
    val id: Long?,
    val email: String?,
    val fullName: String?,
    val role: String?
)
