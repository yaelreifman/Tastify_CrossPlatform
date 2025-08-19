package org.example.project.data

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

object GeocodingRepository {

    private const val BASE_URL = "https://maps.googleapis.com/maps/api/geocode/json"
    private const val API_KEY = "AIzaSyC3FvmE6h_7Xea8pZMs46zxH1kcTfLn0lE"

    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
            })
        }
    }

    suspend fun getCoordinatesFromAddress(address: String): Pair<Double, Double>? =
        withContext(Dispatchers.IO) {
            try {
                // 🕐 Timeout ל-5 שניות
                withTimeout(5000) {
                    val response: GeocodingResponse = client.get(BASE_URL) {
                        parameter("address", address)
                        parameter("key", API_KEY)
                    }.body()

                    val location = response.results.firstOrNull()?.geometry?.location
                    if (location != null) {
                        println("✅ Geocoding success: $address -> ${location.lat}, ${location.lng}")
                        Pair(location.lat, location.lng)
                    } else {
                        println("⚠️ Geocoding failed for: $address (status=${response.status})")
                        null
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                println("❌ Geocoding exception: ${e.message}")
                // 🟢 Fallback לתל אביב
                Pair(32.0853, 34.7818)
            }
        }
}

@Serializable
data class GeocodingResponse(
    val results: List<GeocodingResult> = emptyList(),
    val status: String = ""
)

@Serializable
data class GeocodingResult(
    val geometry: Geometry
)

@Serializable
data class Geometry(
    val location: Location
)

@Serializable
data class Location(
    val lat: Double,
    val lng: Double
)
