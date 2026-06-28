package com.example.myapplication.ui

import android.app.Application
import android.content.Context
import androidx.compose.runtime.*
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.RetrofitClient
import com.example.myapplication.model.Movie
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.joinAll
import java.text.SimpleDateFormat
import java.util.*

data class WatchProgress(
    val mediaId: Int,
    val title: String,
    val posterPath: String?,
    val backdropPath: String?,
    val mediaType: String,
    val season: Int = 1,
    val episode: Int = 1,
    val watched: Float,
    val duration: Float,
    val lastUpdated: Long = System.currentTimeMillis()
)

enum class TvTab { Home, TvShows, Anime, Search }
enum class AppScreen { Main, Details, Player, Genre }

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val context = application.applicationContext
    
    var currentScreen by mutableStateOf(AppScreen.Main)
    var selectedTab by mutableStateOf(TvTab.Home)
    
    val continueWatching = mutableStateListOf<Movie>()

    fun loadContinueWatching() {
        val prefs = context.getSharedPreferences("continue_watching", Context.MODE_PRIVATE)
        val gson = Gson()
        val listType = object : TypeToken<List<WatchProgress>>() {}.type
        val json = prefs.getString("progress_list", null)
        if (json != null) {
            try {
                val list = gson.fromJson<List<WatchProgress>>(json, listType) ?: emptyList()
                continueWatching.clear()
                continueWatching.addAll(list.map { progress ->
                    Movie(
                        id = progress.mediaId,
                        title = if (progress.mediaType == "movie") progress.title else null,
                        name = if (progress.mediaType == "tv") progress.title else null,
                        overview = if (progress.mediaType == "tv") "Resume: S${progress.season} E${progress.episode}" else "Resume Watching",
                        posterPath = progress.posterPath,
                        backdropPath = progress.backdropPath,
                        voteAverage = 0.0,
                        releaseDate = null,
                        firstAirDate = if (progress.mediaType == "tv") "tv" else null,
                        mediaType = progress.mediaType
                    )
                })
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveProgress(mediaId: Int, title: String, posterPath: String?, backdropPath: String?, mediaType: String, season: Int, episode: Int, watched: Float, duration: Float) {
        val progress = WatchProgress(
            mediaId = mediaId,
            title = title,
            posterPath = posterPath,
            backdropPath = backdropPath,
            mediaType = mediaType,
            season = season,
            episode = episode,
            watched = watched,
            duration = duration
        )

        val prefs = context.getSharedPreferences("continue_watching", Context.MODE_PRIVATE)
        val gson = Gson()
        val listType = object : TypeToken<List<WatchProgress>>() {}.type
        val json = prefs.getString("progress_list", null)
        val list = if (json != null) {
            try {
                gson.fromJson<MutableList<WatchProgress>>(json, listType) ?: mutableListOf()
            } catch (e: Exception) {
                mutableListOf()
            }
        } else {
            mutableListOf()
        }

        // If watched is near duration (e.g. > 90%), remove it from continue watching
        if (duration > 0 && watched / duration > 0.9f) {
            list.removeAll { it.mediaId == mediaId }
        } else {
            list.removeAll { it.mediaId == mediaId }
            list.add(0, progress)
            if (list.size > 20) {
                list.removeAt(list.size - 1)
            }
        }

        prefs.edit().putString("progress_list", gson.toJson(list)).apply()
        loadContinueWatching()
    }
    var selectedMovie by mutableStateOf<Movie?>(null)
    
    var selectedSeason by mutableIntStateOf(1)
    var selectedEpisode by mutableIntStateOf(1)
    var selectedProviderIndex by mutableIntStateOf(4)
    var selectedLanguageIndex by mutableIntStateOf(0)

    var isLoading by mutableStateOf(false)

    // Genre Filtering State
    var activeGenreName by mutableStateOf<String?>(null)
    var activeGenreId by mutableIntStateOf(0)
    var activeGenreTab by mutableStateOf(TvTab.Home)
    val genreMovies = mutableStateListOf<Movie>()
    var genrePage = 1

    // Genre Backgrounds
    val movieGenrePosters = mutableStateMapOf<Int, String>()
    val tvGenrePosters = mutableStateMapOf<Int, String>()
    val animeGenrePosters = mutableStateMapOf<Int, String>()

    // Huge Content State
    val trendingMovies = mutableStateListOf<Movie>()
    val popularMovies = mutableStateListOf<Movie>()
    val netflixHome = mutableStateListOf<Movie>()
    val primeHome = mutableStateListOf<Movie>()
    val disneyHome = mutableStateListOf<Movie>()
    val hindiHome = mutableStateListOf<Movie>()
    val southHome = mutableStateListOf<Movie>()
    
    val trendingTv = mutableStateListOf<Movie>()
    val popularTv = mutableStateListOf<Movie>()
    val topRatedTv = mutableStateListOf<Movie>()
    val onTheAirTv = mutableStateListOf<Movie>()
    val netflixTv = mutableStateListOf<Movie>()
    val primeTv = mutableStateListOf<Movie>()
    val disneyTv = mutableStateListOf<Movie>()
    val hindiTv = mutableStateListOf<Movie>()
    val southTv = mutableStateListOf<Movie>()
    val hollywoodTv = mutableStateListOf<Movie>()
    
    val latestAnime = mutableStateListOf<Movie>()
    val crunchyrollAnime = mutableStateListOf<Movie>()
    val seasonalAnime = mutableStateListOf<Movie>()
    val trendingAnime = mutableStateListOf<Movie>()
    val topRatedAnime = mutableStateListOf<Movie>()
    val popularAnime = mutableStateListOf<Movie>()
    
    val searchResults = mutableStateListOf<Movie>()
    var searchQuery by mutableStateOf("")
    private var searchJob: Job? = null

    private val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    init {
        loadContinueWatching()
        fetchAllData()
        fetchGenrePosters()
    }

    fun onTabSelected(tab: TvTab) {
        selectedTab = tab
    }

    fun onGenreClick(genreId: Int, genreName: String) {
        activeGenreName = genreName
        activeGenreId = genreId
        activeGenreTab = selectedTab
        genreMovies.clear()
        genrePage = 1
        currentScreen = AppScreen.Genre
        loadMoreGenreMovies()
    }

    fun loadMoreGenreMovies() {
        viewModelScope.launch {
            try {
                val response = when (activeGenreTab) {
                    TvTab.TvShows -> RetrofitClient.tmdbApi.getTvByGenre(activeGenreId, page = genrePage++)
                    TvTab.Anime -> RetrofitClient.tmdbApi.getAnime(genreId = "16,$activeGenreId", page = genrePage++)
                    else -> RetrofitClient.tmdbApi.getMoviesByGenre(activeGenreId, page = genrePage++)
                }
                genreMovies.addAll(filterReleased(response.results))
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    private fun fetchGenrePosters() {
        val movieGenres = listOf(28, 12, 16, 35, 18, 27, 878, 10749)
        movieGenres.forEach { id ->
            viewModelScope.launch {
                try {
                    val movieRes = RetrofitClient.tmdbApi.getMoviesByGenre(id, page = 1)
                    movieRes.results.firstOrNull { it.backdropPath != null }?.fullBackdropPath?.let {
                        movieGenrePosters[id] = it
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
        }

        val tvGenres = listOf(10759, 16, 35, 18, 10765, 9648, 80)
        tvGenres.forEach { id ->
            viewModelScope.launch {
                try {
                    val tvRes = RetrofitClient.tmdbApi.getTvByGenre(id, page = 1)
                    tvRes.results.firstOrNull { it.backdropPath != null }?.fullBackdropPath?.let {
                        tvGenrePosters[id] = it
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
        }

        val animeGenres = listOf(28, 12, 35, 18, 27, 878, 10749)
        animeGenres.forEach { id ->
            viewModelScope.launch {
                try {
                    val animeGenreRes = RetrofitClient.tmdbApi.getAnime(genreId = "16,$id", page = 1)
                    animeGenreRes.results.firstOrNull { it.backdropPath != null }?.fullBackdropPath?.let {
                        animeGenrePosters[id] = it
                    }
                } catch (e: Exception) { e.printStackTrace() }
            }
        }
    }

    fun navigateToDetails(movie: Movie) {
        selectedMovie = movie
        selectedSeason = 1
        selectedEpisode = 1
        currentScreen = AppScreen.Details
    }

    fun navigateToPlayer(movie: Movie, season: Int = 1, episode: Int = 1) {
        selectedMovie = movie
        selectedSeason = season
        selectedEpisode = episode
        currentScreen = AppScreen.Player
    }

    fun navigateBack() {
        when (currentScreen) {
            AppScreen.Player -> currentScreen = AppScreen.Details
            AppScreen.Details -> currentScreen = if (activeGenreName != null) AppScreen.Genre else AppScreen.Main
            AppScreen.Genre -> {
                currentScreen = AppScreen.Main
                activeGenreName = null
            }
            AppScreen.Main -> {}
        }
    }

    fun updateFeaturedMovie(movie: Movie) {}

    private fun fetchAllData() {
        viewModelScope.launch {
            isLoading = true
            try {
                val jobs = listOf(
                    launch { loadTrendingInternal() },
                    launch { loadPopularInternal() },
                    launch { loadNetflixInternal() },
                    launch { loadRegionalInternal() },
                    launch { loadTvInternal() },
                    launch { loadAnimeInternal() }
                )
                jobs.joinAll()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    private fun filterReleased(movies: List<Movie>): List<Movie> {
        return movies.filter { 
            (it.releaseDate == null || it.releaseDate <= currentDate) && 
            (it.firstAirDate == null || it.firstAirDate <= currentDate) 
        }
    }

    private var trendingPage = 1
    private var popularPage = 1
    private var netflixPage = 1
    private var primePage = 1
    private var disneyPage = 1
    private var hindiPage = 1
    private var southPage = 1

    private var trendingTvPage = 1
    private var popularTvPage = 1
    private var netflixTvPage = 1
    private var primeTvPage = 1
    private var disneyTvPage = 1
    private var topRatedTvPage = 1
    private var onTheAirTvPage = 1
    private var hindiTvPage = 1
    private var southTvPage = 1
    private var hollywoodTvPage = 1

    private var animeMoviesPage = 1
    private var searchPage = 1

    private suspend fun loadTrendingInternal() {
        try {
            // Use specific trending movies for Home carousel
            val response = RetrofitClient.tmdbApi.getTrendingMovies(page = trendingPage++)
            val filtered = filterReleased(response.results)
            if (filtered.isNotEmpty()) {
                trendingMovies.addAll(filtered)
            } else {
                // Fallback to popular if trending is empty or filtered out
                val popular = RetrofitClient.tmdbApi.getPopularMovies(page = 1)
                trendingMovies.addAll(filterReleased(popular.results))
            }
        } catch (e: Exception) { 
            e.printStackTrace()
            // Emergency fallback to popular movies
            try {
                val popular = RetrofitClient.tmdbApi.getPopularMovies(page = 1)
                trendingMovies.addAll(filterReleased(popular.results))
            } catch (ex: Exception) { ex.printStackTrace() }
        }
    }

    private suspend fun loadPopularInternal() {
        try {
            val response = RetrofitClient.tmdbApi.getPopularMovies(page = popularPage++)
            popularMovies.addAll(filterReleased(response.results))
        } catch (e: Exception) { e.printStackTrace() }
    }

    private suspend fun loadNetflixInternal() {
        try {
            netflixHome.addAll(filterReleased(RetrofitClient.tmdbApi.getMoviesByProvider(8, page = netflixPage++).results))
            primeHome.addAll(filterReleased(RetrofitClient.tmdbApi.getMoviesByProvider(119, page = primePage++).results))
            disneyHome.addAll(filterReleased(RetrofitClient.tmdbApi.getMoviesByProvider(337, page = disneyPage++).results))
        } catch (e: Exception) { e.printStackTrace() }
    }

    private suspend fun loadRegionalInternal() {
        try {
            hindiHome.addAll(filterReleased(RetrofitClient.tmdbApi.getMoviesByLanguage("hi", page = hindiPage++).results))
            southHome.addAll(filterReleased(RetrofitClient.tmdbApi.getMoviesByLanguage("te", page = southPage++).results))
        } catch (e: Exception) { e.printStackTrace() }
    }

    private suspend fun loadTvInternal() {
        try {
            trendingTv.addAll(filterReleased(RetrofitClient.tmdbApi.getTrendingTv(page = trendingTvPage++).results.filter { it.mediaType == "tv" }))
            popularTv.addAll(filterReleased(RetrofitClient.tmdbApi.getPopularTv(page = popularTvPage++).results))
            
            topRatedTv.addAll(filterReleased(RetrofitClient.tmdbApi.getTopRatedTv(page = topRatedTvPage++).results))
            onTheAirTv.addAll(filterReleased(RetrofitClient.tmdbApi.getOnTheAirTv(page = onTheAirTvPage++).results))

            netflixTv.addAll(filterReleased(RetrofitClient.tmdbApi.getTvByProvider(8, page = netflixTvPage++).results))
            primeTv.addAll(filterReleased(RetrofitClient.tmdbApi.getTvByProvider(119, page = primeTvPage++).results))
            disneyTv.addAll(filterReleased(RetrofitClient.tmdbApi.getTvByProvider(337, page = disneyTvPage++).results))
            
            hindiTv.addAll(filterReleased(RetrofitClient.tmdbApi.getMoviesByLanguage("hi", page = hindiTvPage++).results.filter { it.mediaType == "tv" }))
            southTv.addAll(filterReleased(RetrofitClient.tmdbApi.getMoviesByLanguage("te", page = southTvPage++).results.filter { it.mediaType == "tv" }))
            hollywoodTv.addAll(filterReleased(RetrofitClient.tmdbApi.getMoviesByLanguage("en", page = hollywoodTvPage++).results.filter { it.mediaType == "tv" }))
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun loadMoreTrending() = viewModelScope.launch { loadTrendingInternal() }
    fun loadMorePopular() = viewModelScope.launch { loadPopularInternal() }
    fun loadMoreNetflix() = viewModelScope.launch { loadNetflixInternal() }
    fun loadMoreRegional() = viewModelScope.launch { loadRegionalInternal() }

    fun loadMorePopularTv() = viewModelScope.launch { 
        try {
            popularTv.addAll(filterReleased(RetrofitClient.tmdbApi.getPopularTv(page = popularTvPage++).results))
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun loadMoreTopRatedTv() = viewModelScope.launch {
        try {
            topRatedTv.addAll(filterReleased(RetrofitClient.tmdbApi.getTopRatedTv(page = topRatedTvPage++).results))
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun loadMoreOnTheAirTv() = viewModelScope.launch {
        try {
            onTheAirTv.addAll(filterReleased(RetrofitClient.tmdbApi.getOnTheAirTv(page = onTheAirTvPage++).results))
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun loadMoreNetflixTv() = viewModelScope.launch {
        try {
            netflixTv.addAll(filterReleased(RetrofitClient.tmdbApi.getTvByProvider(8, page = netflixTvPage++).results))
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun loadMorePrimeTv() = viewModelScope.launch {
        try {
            primeTv.addAll(filterReleased(RetrofitClient.tmdbApi.getTvByProvider(119, page = primeTvPage++).results))
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun loadMoreDisneyTv() = viewModelScope.launch {
        try {
            disneyTv.addAll(filterReleased(RetrofitClient.tmdbApi.getTvByProvider(337, page = disneyTvPage++).results))
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun loadMoreHindiTv() = viewModelScope.launch {
        try {
            hindiTv.addAll(filterReleased(RetrofitClient.tmdbApi.getMoviesByLanguage("hi", page = hindiTvPage++).results.filter { it.mediaType == "tv" }))
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun loadMoreSouthTv() = viewModelScope.launch {
        try {
            southTv.addAll(filterReleased(RetrofitClient.tmdbApi.getMoviesByLanguage("te", page = southTvPage++).results.filter { it.mediaType == "tv" }))
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun loadMoreHollywoodTv() = viewModelScope.launch {
        try {
            hollywoodTv.addAll(filterReleased(RetrofitClient.tmdbApi.getMoviesByLanguage("en", page = hollywoodTvPage++).results.filter { it.mediaType == "tv" }))
        } catch (e: Exception) { e.printStackTrace() }
    }

    private suspend fun loadAnimeInternal() {
        try {
            val res = RetrofitClient.tmdbApi.getAnime(page = animeMoviesPage++)
            latestAnime.addAll(filterReleased(res.results))
            trendingAnime.addAll(filterReleased(res.results).shuffled())
            popularAnime.addAll(filterReleased(res.results).sortedByDescending { it.voteAverage })
            crunchyrollAnime.addAll(filterReleased(res.results).filter { it.voteAverage > 7.5 })
            seasonalAnime.addAll(filterReleased(res.results).reversed())
            topRatedAnime.addAll(filterReleased(res.results).sortedByDescending { it.voteAverage })
        } catch (e: Exception) { e.printStackTrace() }
    }

    fun loadMoreAnime() = viewModelScope.launch { loadAnimeInternal() }

    fun onSearchQueryChange(query: String) {
        searchQuery = query
        searchJob?.cancel()
        searchResults.clear()
        searchPage = 1
        if (query.length > 2) {
            searchJob = viewModelScope.launch {
                delay(500)
                loadMoreSearch()
            }
        }
    }

    fun loadMoreSearch() {
        if (searchQuery.length <= 2) return
        viewModelScope.launch {
            try {
                val response = RetrofitClient.tmdbApi.search(query = searchQuery, page = searchPage++)
                searchResults.addAll(filterReleased(response.results))
            } catch (e: Exception) { e.printStackTrace() }
        }
    }
}
