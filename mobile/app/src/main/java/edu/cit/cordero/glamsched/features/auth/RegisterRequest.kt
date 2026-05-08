package edu.cit.cordero.glamsched.features.auth

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val role: String
)
