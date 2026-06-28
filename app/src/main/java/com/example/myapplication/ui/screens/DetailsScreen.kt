package com.example.myapplication.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.example.myapplication.data.RetrofitClient
import com.example.myapplication.data.Providers
import com.example.myapplication.model.Episode
import com.example.myapplication.model.Movie
import com.example.myapplication.model.MovieDetailsResponse
import com.example.myapplication.ui.HomeViewModel
import com.example.myapplication.ui.components.MovieRow
import kotlinx.coroutines.launch
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusProperties
import kotlin.math.round

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun DetailsScreen(
    movie: Movie,
    viewModel: HomeViewModel,
    onPlayClick: () -> Unit,
    onBackClick: () -> Unit,
    onMovieClick: (Movie) -> Unit
) {
    var details by remember { mutableStateOf<MovieDetailsResponse?>(null) }
    var episodes by remember { mutableStateOf<List<Episode>>(emptyList()) }
    val scope = rememberCoroutineScope()
    val isTv = movie.mediaType == "tv" || movie.firstAirDate != null

    val initialFocusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        initialFocusRequester.requestFocus()
    }

    LaunchedEffect(movie.id) {
        scope.launch {
            try {
                details = if (isTv) {
                    RetrofitClient.tmdbApi.getTvDetails(movie.id)
                } else {
                    RetrofitClient.tmdbApi.getMovieDetails(movie.id)
                }
                
                if (isTv && details?.seasons?.isNotEmpty() == true) {
                    val seasonNum = details?.seasons?.first()?.seasonNumber ?: 1
                    val seasonDetails = RetrofitClient.tmdbApi.getSeasonDetails(movie.id, seasonNum)
                    episodes = seasonDetails.episodes
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F0F))) {
        AsyncImage(
            model = movie.fullBackdropPath,
            contentDescription = null,
            modifier = Modifier.fillMaxWidth().height(600.dp),
            contentScale = ContentScale.Crop
        )
        
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent, Color(0xFF0F0F0F)),
                        startY = 0f,
                        endY = 1200f
                    )
                )
        )
        
        // Horizontal vignette for text readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent),
                        startX = 0f,
                        endX = 1000f
                    )
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 58.dp)
        ) {
            Spacer(modifier = Modifier.height(320.dp))

            Text(
                text = movie.displayTitle,
                style = MaterialTheme.typography.displayLarge,
                color = Color.White
            )

            Row(verticalAlignment = Alignment.CenterVertically) {
                val roundedRating = (round(movie.voteAverage * 10) / 10).toString()
                Text(
                    text = "$roundedRating Rating",
                    color = Color.Yellow,
                    style = MaterialTheme.typography.labelLarge
                )
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = movie.releaseDate ?: movie.firstAirDate ?: "",
                    color = Color.LightGray,
                    style = MaterialTheme.typography.labelLarge
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // FIX: Using standardized provider list from Providers.kt
            Text("Select Source:", style = MaterialTheme.typography.titleMedium, color = Color.White)
            LazyRow(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .focusProperties { up = FocusRequester.Cancel }
            ) {
                items(Providers.NAMES.size) { index ->
                    Button(
                        onClick = { viewModel.selectedProviderIndex = index },
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .then(if (index == viewModel.selectedProviderIndex) Modifier.focusRequester(initialFocusRequester) else Modifier),
                        colors = ButtonDefaults.colors(
                            containerColor = if (viewModel.selectedProviderIndex == index) Color(0xFFE50914) else Color(0xFF1A1A1A)
                        )
                    ) {
                        Text(Providers.NAMES[index])
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Select Language:", style = MaterialTheme.typography.titleMedium, color = Color.White)
            LazyRow(modifier = Modifier.padding(vertical = 12.dp)) {
                items(Providers.LANGUAGES.size) { index ->
                    Button(
                        onClick = { viewModel.selectedLanguageIndex = index },
                        modifier = Modifier.padding(end = 12.dp),
                        colors = ButtonDefaults.colors(
                            containerColor = if (viewModel.selectedLanguageIndex == index) Color(0xFFE50914) else Color(0xFF1A1A1A)
                        )
                    ) {
                        Text(Providers.LANGUAGES[index])
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row {
                if (!isTv) {
                    Button(onClick = onPlayClick) {
                        Text("Watch Now")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                }
                OutlinedButton(onClick = onBackClick) {
                    Text("Go Back")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = movie.overview,
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.fillMaxWidth(0.7f)
            )

            if (isTv && details?.seasons != null) {
                Spacer(modifier = Modifier.height(48.dp))
                Text("Seasons", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                LazyRow(modifier = Modifier.padding(vertical = 16.dp)) {
                    items(details!!.seasons!!) { season ->
                        Button(
                            onClick = { 
                                viewModel.selectedSeason = season.seasonNumber
                                scope.launch {
                                    try {
                                        val res = RetrofitClient.tmdbApi.getSeasonDetails(movie.id, season.seasonNumber)
                                        episodes = res.episodes
                                    } catch (e: Exception) { e.printStackTrace() }
                                }
                            },
                            modifier = Modifier.padding(end = 16.dp),
                            colors = ButtonDefaults.colors(
                                containerColor = if (viewModel.selectedSeason == season.seasonNumber) Color(0xFFE50914) else Color(0xFF222222)
                            )
                        ) {
                            Text(season.name ?: "Season ${season.seasonNumber}")
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                Text("Episodes", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                LazyRow(modifier = Modifier.padding(vertical = 16.dp)) {
                    items(episodes) { episode ->
                        Card(
                            onClick = { 
                                viewModel.navigateToPlayer(movie, viewModel.selectedSeason, episode.episodeNumber)
                            },
                            modifier = Modifier.width(260.dp).padding(end = 20.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                AsyncImage(
                                    model = "https://image.tmdb.org/t/p/w300${episode.stillPath}",
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxWidth().height(140.dp),
                                    contentScale = ContentScale.Crop
                                )
                                Text(
                                    text = "E${episode.episodeNumber}: ${episode.name}",
                                    maxLines = 1,
                                    style = MaterialTheme.typography.labelLarge,
                                    modifier = Modifier.padding(top = 12.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(64.dp))

            details?.recommendations?.results?.let { recs ->
                MovieRow(
                    title = "More Like This",
                    movies = recs,
                    onMovieClick = onMovieClick,
                    onMovieFocus = {}
                )
            }

            Spacer(modifier = Modifier.height(64.dp))
        }
    }
}
