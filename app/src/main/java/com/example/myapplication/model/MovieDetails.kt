package com.example.myapplication.model

import com.google.gson.annotations.SerializedName

data class MovieDetailsResponse(
    val id: Int,
    val title: String?,
    val name: String?,
    val overview: String,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("vote_average") val voteAverage: Double,
    @SerializedName("release_date") val releaseDate: String?,
    @SerializedName("first_air_date") val firstAirDate: String?,
    val genres: List<Genre>?,
    val credits: Credits?,
    val videos: VideoResponse?,
    val recommendations: MovieResponse?,
    val seasons: List<Season>?
)

data class Season(
    val id: Int,
    @SerializedName("season_number") val seasonNumber: Int,
    val name: String?,
    val overview: String?,
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("episode_count") val episodeCount: Int?
)

data class SeasonDetailsResponse(
    val id: Int,
    val episodes: List<Episode>
)

data class Episode(
    val id: Int,
    @SerializedName("episode_number") val episodeNumber: Int,
    val name: String?,
    val overview: String?,
    @SerializedName("still_path") val stillPath: String?
)

data class Genre(val id: Int, val name: String)

data class Credits(val cast: List<CastMember>)

data class CastMember(
    val id: Int,
    val name: String,
    val character: String,
    @SerializedName("profile_path") val profilePath: String?
)

data class VideoResponse(val results: List<Video>)

data class Video(
    val key: String,
    val site: String,
    val type: String
)
