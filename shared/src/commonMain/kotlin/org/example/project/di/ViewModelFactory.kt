package org.example.project.di

import io.ktor.client.HttpClient
import org.example.project.features.Reviews.ReviewsViewModel

expect fun createHttpClient(): HttpClient

object ViewModelFactory {
    private val httpClient: HttpClient = createHttpClient()
    private val bearerToken = ""

    fun createViewModel(): ReviewsViewModel{
        return ReviewsViewModel()
    }
}