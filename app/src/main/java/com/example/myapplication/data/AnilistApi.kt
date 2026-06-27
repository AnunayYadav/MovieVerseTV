package com.example.myapplication.data

import com.example.myapplication.model.AnilistRequest
import com.example.myapplication.model.AnilistSearchResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AnilistApi {
    @POST("/")
    suspend fun searchAnime(@Body request: AnilistRequest): AnilistSearchResponse
}
