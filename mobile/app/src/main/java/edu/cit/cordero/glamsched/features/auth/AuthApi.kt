package edu.cit.cordero.glamsched.features.auth

import edu.cit.cordero.glamsched.shared.ApiResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/register")
    fun register(@Body request: RegisterRequest): Call<ApiResponse<AuthResponse>>

    @POST("auth/login")
    fun login(@Body request: LoginRequest): Call<ApiResponse<AuthResponse>>
}
