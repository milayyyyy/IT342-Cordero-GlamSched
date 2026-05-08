package edu.cit.cordero.glamsched.shared

import edu.cit.cordero.glamsched.features.booking.ReviewDto
import edu.cit.cordero.glamsched.features.dashboard.AppointmentDto
import edu.cit.cordero.glamsched.features.dashboard.ServiceDto
import edu.cit.cordero.glamsched.features.dashboard.UserDto
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface GlamApi {

    // ── Services ──
    @GET("api/services")
    fun getServices(@Query("clientId") clientId: Long): Call<ApiResponse<List<ServiceDto>>>

    @GET("api/services/artist/{artistId}")
    fun getServicesByArtist(
        @Path("artistId") artistId: Long,
        @Query("clientId") clientId: Long
    ): Call<ApiResponse<List<ServiceDto>>>

    @POST("api/services/create")
    fun createService(
        @Query("artistId") artistId: Long,
        @Body body: Map<String, Any?>
    ): Call<ApiResponse<ServiceDto>>

    @PATCH("api/services/{id}")
    fun updateService(
        @Path("id") serviceId: Long,
        @Body body: Map<String, Any?>
    ): Call<ApiResponse<ServiceDto>>

    @PATCH("api/services/{id}/photos")
    fun updateServicePhotos(
        @Path("id") serviceId: Long,
        @Body body: Map<String, Any?>
    ): Call<ApiResponse<ServiceDto>>

    @DELETE("api/services/{id}")
    fun deleteService(@Path("id") serviceId: Long): Call<ApiResponse<String>>

    @POST("api/services/{id}/react")
    fun toggleReaction(
        @Path("id") serviceId: Long,
        @Query("clientId") clientId: Long
    ): Call<ApiResponse<Map<String, Any>>>

    // ── Appointments ──
    @GET("api/appointments")
    fun getAppointments(@Query("clientId") clientId: Long): Call<ApiResponse<List<AppointmentDto>>>

    @GET("api/appointments/artist/{artistId}")
    fun getArtistAppointments(@Path("artistId") artistId: Long): Call<ApiResponse<List<AppointmentDto>>>

    @POST("api/appointments")
    fun createAppointment(@Body appointment: AppointmentDto): Call<ApiResponse<AppointmentDto>>

    @PUT("api/appointments/{id}/status")
    fun updateAppointmentStatus(
        @Path("id") appointmentId: Long,
        @Query("status") status: String
    ): Call<ApiResponse<AppointmentDto>>

    // ── Users ──
    @POST("api/users/{artistId}/follow")
    fun toggleFollow(
        @Path("artistId") artistId: Long,
        @Query("clientId") clientId: Long
    ): Call<ApiResponse<Map<String, Any>>>

    @GET("api/users/{id}/profile")
    fun getArtistProfile(
        @Path("id") artistId: Long,
        @Query("clientId") clientId: Long
    ): Call<ApiResponse<UserDto>>

    @GET("api/users/{id}")
    fun getUserById(@Path("id") userId: Long): Call<ApiResponse<UserDto>>

    @GET("api/users/{id}/stats")
    fun getUserStats(@Path("id") userId: Long): Call<ApiResponse<Map<String, Any>>>

    @PUT("api/users/{id}")
    fun updateUserProfile(
        @Path("id") userId: Long,
        @Body updates: Map<String, Any?>
    ): Call<ApiResponse<UserDto>>

    @PUT("api/users/{id}/password")
    fun changePassword(
        @Path("id") userId: Long,
        @Body body: Map<String, String>
    ): Call<ApiResponse<String>>

    @PUT("api/users/{id}/photo")
    fun updateUserPhoto(
        @Path("id") userId: Long,
        @Body body: Map<String, String>
    ): Call<ApiResponse<UserDto>>

    @PUT("api/users/{id}/cover")
    fun updateUserCover(
        @Path("id") userId: Long,
        @Body body: Map<String, String>
    ): Call<ApiResponse<UserDto>>

    // ── Reviews ──
    @GET("api/reviews/artist/{artistId}")
    fun getReviews(@Path("artistId") artistId: Long): Call<ApiResponse<List<ReviewDto>>>

    @POST("api/reviews")
    fun addReview(@Body review: ReviewDto): Call<ApiResponse<ReviewDto>>
}
