package com.example.myapplication.model

import com.google.gson.annotations.SerializedName

data class Movie(
    val id: Int,
    val title: String?,
    val name: String?,
    val overview: String,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("vote_average") val voteAverage: Double,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("first_air_date") val firstAirDate: String?,
    @SerializedName("media_type") val mediaType: String?
) {
    val displayTitle: String
        get() = title ?: name ?: "Unknown"

    // High quality sizes for TV
    val fullPosterPath: String
        get() = "https://image.tmdb.org/t/p/w500$posterPath"

    val fullBackdropPath: String
        get() = "https://image.tmdb.org/t/p/w1280$backdropPath"
}

data class MovieResponse(
    val results: List<Movie>
)
