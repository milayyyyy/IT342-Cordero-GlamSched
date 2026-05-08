package edu.cit.cordero.glamsched.features.dashboard

data class AppointmentDto(
    val id: Long = 0,
    val clientId: Long? = null,
    val artistId: Long? = null,
    val serviceId: Long? = null,
    val serviceName: String? = null,
    val artistName: String? = null,
    val clientName: String? = null,
    val date: String? = null,
    val time: String? = null,
    val status: String? = null,
    val notes: String? = null,
    val createdAt: String? = null,
    var price: Double? = null,
    var paymentMethod: String? = null
)
