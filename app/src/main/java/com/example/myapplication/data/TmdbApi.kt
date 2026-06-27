package com.example.myapplication.data

import com.example.myapplication.model.MovieResponse
import com.example.myapplication.model.MovieDetailsResponse
import com.example.myapplication.model.SeasonDetailsResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TmdbApi {
    @GET("trending/all/day")
    suspend fun getTrending(
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false
    ): MovieResponse

    @GET("trending/movie/day")
    suspend fun getTrendingMovies(
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MovieResponse

    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false
    ): MovieResponse

    @GET("discover/movie")
    suspend fun getMoviesByLanguage(
        @Query("with_original_language") languageCode: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("release_date.lte") releasedBefore: String = "2025-12-31" // Dynamic date would be better
    ): MovieResponse

    @GET("discover/movie")
    suspend fun getMoviesByProvider(
        @Query("with_watch_providers") providerId: Int,
        @Query("watch_region") region: String = "US",
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false
    ): MovieResponse

    @GET("discover/tv")
    suspend fun getTvByProvider(
        @Query("with_watch_providers") providerId: Int,
        @Query("watch_region") region: String = "US",
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false
    ): MovieResponse

    @GET("tv/top_rated")
    suspend fun getTopRatedTv(
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MovieResponse

    @GET("tv/on_the_air")
    suspend fun getOnTheAirTv(
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MovieResponse

    @GET("discover/movie")
    suspend fun getMoviesByGenre(
        @Query("with_genres") genreId: Int,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MovieResponse

    @GET("discover/tv")
    suspend fun getTvByGenre(
        @Query("with_genres") genreId: Int,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1
    ): MovieResponse

    @GET("trending/tv/day")
    suspend fun getTrendingTv(
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false
    ): MovieResponse

    @GET("discover/tv")
    suspend fun getPopularTv(
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false,
        @Query("without_genres") withoutGenres: String = "16" // Exclude Animation/Anime from standard TV
    ): MovieResponse

    @GET("discover/movie")
    suspend fun getAnime(
        @Query("with_genres") genreId: String = "16",
        @Query("with_keywords") keywords: String = "210024",
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false
    ): MovieResponse

    @GET("search/multi")
    suspend fun search(
        @Query("query") query: String,
        @Query("language") language: String = "en-US",
        @Query("page") page: Int = 1,
        @Query("include_adult") includeAdult: Boolean = false
    ): MovieResponse

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("append_to_response") append: String = "credits,videos,recommendations"
    ): MovieDetailsResponse

    @GET("tv/{tv_id}")
    suspend fun getTvDetails(
        @Path("tv_id") tvId: Int,
        @Query("append_to_response") append: String = "credits,videos,recommendations"
    ): MovieDetailsResponse

    @GET("tv/{tv_id}/season/{season_number}")
    suspend fun getSeasonDetails(
        @Path("tv_id") tvId: Int,
        @Path("season_number") seasonNumber: Int
    ): SeasonDetailsResponse
}
