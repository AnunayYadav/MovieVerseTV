package com.example.myapplication.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Exact clean base URL of your website's proxy
    private const val BASE_URL = "https://movieverseofficial.vercel.app/api/tmdb/"

    val tmdbApi: TmdbApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TmdbApi::class.java)
    }

    val anilistApi: AnilistApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://graphql.anilist.co/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(AnilistApi::class.java)
    }
}
