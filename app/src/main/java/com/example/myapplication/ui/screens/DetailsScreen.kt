package com.example.myapplication.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.draw.clip
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.example.myapplication.data.RetrofitClient
import com.example.myapplication.data.Providers
import com.example.myapplication.model.Episode
import com.example.myapplication.model.Movie
import com.example.myapplication.model.MovieDetailsResponse
import com.example.myapplication.model.CollectionResponse
import com.example.myapplication.ui.HomeViewModel
import com.example.myapplication.ui.components.MovieRow
import kotlinx.coroutines.launch
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.onFocusChanged
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
    var collectionDetails by remember { mutableStateOf<CollectionResponse?>(null) }
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
                
                details?.belongsToCollection?.let { collInfo ->
                    try {
                        collectionDetails = RetrofitClient.tmdbApi.getCollectionDetails(collInfo.id)
                    } catch (e: Exception) { e.printStackTrace() }
                }

                if (isTv && details?.seasons?.isNotEmpty() == true) {
                    val seasonNum = details?.seasons?.first()?.seasonNumber ?: 1
                    val seasonDetails = RetrofitClient.tmdbApi.getSeasonDetails(movie.id, seasonNum)
                    episodes = seasonDetails.episodes
                }
            } catch (e: Exception) { e.printStackTrace() }
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF000000))) {
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
                        colors = listOf(Color.Black.copy(alpha = 0.6f), Color.Transparent, Color(0xFF000000)),
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

            // Dynamic Metadata row optimized for low-end TVs
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                val roundedRating = remember(movie.voteAverage) { (round(movie.voteAverage * 10) / 10).toString() }
                Text(
                    text = "$roundedRating Rating",
                    color = Color.Yellow,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.width(16.dp))
                
                val releaseYear = remember(movie.releaseDate, movie.firstAirDate) {
                    val date = movie.releaseDate ?: movie.firstAirDate ?: ""
                    if (date.length >= 4) date.substring(0, 4) else date
                }
                if (releaseYear.isNotEmpty()) {
                    Text(
                        text = releaseYear,
                        color = Color.LightGray,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }

                val runtimeText = remember(details) {
                    details?.let { d ->
                        if (isTv) {
                            val seasonsCount = d.seasons?.size ?: 0
                            if (seasonsCount > 0) "$seasonsCount ${if (seasonsCount == 1) "Season" else "Seasons"}" else null
                        } else {
                            d.runtime?.let { r ->
                                val hours = r / 60
                                val mins = r % 60
                                if (hours > 0) "${hours}h ${mins}m" else "${mins}m"
                            }
                        }
                    }
                }
                if (runtimeText != null) {
                    Text(
                        text = runtimeText,
                        color = Color.LightGray,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                }

                Box(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = if (!isTv && movie.voteAverage >= 7.0) "4K Ultra HD" else "HD",
                        color = Color.White,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Genre tags
            val genresList = remember(details) { details?.genres?.map { it.name } ?: emptyList() }
            if (genresList.isNotEmpty()) {
                Text(
                    text = genresList.joinToString("  ·  "),
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray.copy(alpha = 0.8f),
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Select Source Row
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

            Spacer(modifier = Modifier.height(12.dp))

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

            Spacer(modifier = Modifier.height(20.dp))

            // Watch & Action Buttons styled like Premium Netflix CTAs
            Row(modifier = Modifier.padding(top = 16.dp)) {
                val isTvShow = isTv
                if (isTvShow) {
                    if (episodes.isNotEmpty()) {
                        Button(
                            onClick = {
                                viewModel.navigateToPlayer(movie, viewModel.selectedSeason, episodes.first().episodeNumber)
                            },
                            modifier = Modifier.padding(end = 16.dp),
                            colors = ButtonDefaults.colors(
                                containerColor = Color.White.copy(alpha = 0.2f),
                                contentColor = Color.White,
                                focusedContainerColor = Color.White,
                                focusedContentColor = Color.Black
                            ),
                            shape = ButtonDefaults.shape(shape = RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = "▶  Play S${viewModel.selectedSeason}:E${episodes.first().episodeNumber}",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                        }
                    }
                } else {
                    Button(
                        onClick = onPlayClick,
                        modifier = Modifier.padding(end = 16.dp),
                        colors = ButtonDefaults.colors(
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = Color.White,
                            focusedContainerColor = Color.White,
                            focusedContentColor = Color.Black
                        ),
                        shape = ButtonDefaults.shape(shape = RoundedCornerShape(8.dp))
                    ) {
                        Text(
                            text = "▶  Watch Now",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }

                OutlinedButton(
                    onClick = onBackClick,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Transparent,
                        contentColor = Color.White,
                        focusedContainerColor = Color.White.copy(alpha = 0.2f),
                        focusedContentColor = Color.White
                    ),
                    border = ButtonDefaults.outlinedButtonBorder(
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
                        focusedBorder = BorderStroke(2.dp, Color.White)
                    ),
                    shape = ButtonDefaults.shape(shape = RoundedCornerShape(8.dp))
                ) {
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
                    items(details!!.seasons!!, key = { it.id }) { season ->
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
                LazyRow(
                    contentPadding = PaddingValues(end = 32.dp),
                    modifier = Modifier.padding(vertical = 16.dp)
                ) {
                    items(episodes, key = { it.id }) { episode ->
                        Card(
                            onClick = { 
                                viewModel.navigateToPlayer(movie, viewModel.selectedSeason, episode.episodeNumber)
                            },
                            modifier = Modifier
                                .width(280.dp)
                                .padding(end = 20.dp),
                            shape = CardDefaults.shape(RoundedCornerShape(8.dp)),
                            scale = CardDefaults.scale(focusedScale = 1.08f),
                            border = CardDefaults.border(
                                focusedBorder = Border(
                                    border = BorderStroke(2.dp, Color.White),
                                    inset = 0.dp
                                )
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                ) {
                                    AsyncImage(
                                        model = if (episode.stillPath != null) "https://image.tmdb.org/t/p/w300${episode.stillPath}" else "https://p-nt-www-2.akamaized.net/media/76be7020-f09b-11ef-807d-53609805908b/02-Logo.jpg",
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                Text(
                                    text = "Episode ${episode.episodeNumber}: ${episode.name ?: "TBA"}",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Color.White,
                                    modifier = Modifier.padding(top = 12.dp)
                                )
                                if (!episode.overview.isNullOrEmpty()) {
                                    Text(
                                        text = episode.overview,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.LightGray.copy(alpha = 0.8f),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Franchise Collection section placed directly before Similar Movies
            val collParts = remember(collectionDetails) { collectionDetails?.parts ?: emptyList() }
            if (collParts.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))
                MovieRow(
                    title = collectionDetails?.name ?: "Franchise Collection",
                    movies = collParts,
                    onMovieClick = onMovieClick,
                    onMovieFocus = {}
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

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
