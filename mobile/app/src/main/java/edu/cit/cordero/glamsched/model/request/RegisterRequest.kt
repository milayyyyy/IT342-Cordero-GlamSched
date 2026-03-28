package edu.cit.cordero.glamsched.model.request

data class RegisterRequest(
    val fullName: String,
    val email: String,
    val password: String,
    val role: String
)
