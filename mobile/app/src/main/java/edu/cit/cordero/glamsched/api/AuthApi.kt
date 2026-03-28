package edu.cit.cordero.glamsched.api

import edu.cit.cordero.glamsched.model.request.LoginRequest
import edu.cit.cordero.glamsched.model.request.RegisterRequest
import edu.cit.cordero.glamsched.model.response.ApiResponse
import edu.cit.cordero.glamsched.model.response.AuthResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("auth/register")
    fun register(@Body request: RegisterRequest): Call<ApiResponse<AuthResponse>>

    @POST("auth/login")
    fun login(@Body request: LoginRequest): Call<ApiResponse<AuthResponse>>
}
