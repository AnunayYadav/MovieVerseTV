package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.example.myapplication.ui.HomeViewModel
import com.example.myapplication.ui.TvTab
import com.example.myapplication.ui.AppScreen
import com.example.myapplication.ui.components.GenreRow
import com.example.myapplication.ui.components.HeroBanner
import com.example.myapplication.ui.components.MovieRow
import com.example.myapplication.ui.screens.SearchScreen
import com.example.myapplication.ui.screens.DetailsScreen
import com.example.myapplication.ui.screens.PlayerScreen
import com.example.myapplication.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalTvMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MyApplicationTheme {
                Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F0F))) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation(viewModel: HomeViewModel = viewModel()) {
    BackHandler(enabled = viewModel.currentScreen != AppScreen.Main) {
        viewModel.navigateBack()
    }

    when (viewModel.currentScreen) {
        AppScreen.Main -> MainScreen(viewModel)
        AppScreen.Genre -> GenreScreenContent(viewModel)
        AppScreen.Details -> viewModel.selectedMovie?.let { movie ->
            DetailsScreen(
                movie = movie,
                viewModel = viewModel,
                onPlayClick = { viewModel.navigateToPlayer(movie) },
                onBackClick = { viewModel.navigateBack() },
                onMovieClick = { viewModel.navigateToDetails(it) }
            )
        }
        AppScreen.Player -> viewModel.selectedMovie?.let { movie ->
            PlayerScreen(
                movie = movie,
                viewModel = viewModel,
                onBack = { viewModel.navigateBack() }
            )
        }
    }
}

@Composable
fun GenreScreenContent(viewModel: HomeViewModel) {
    val scrollState = rememberScrollState()
    val titleSuffix = when(viewModel.activeGenreTab) {
        TvTab.Anime -> "Anime"
        TvTab.TvShows -> "TV Shows"
        else -> "Movies & Shows"
    }
    
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F0F))) {
        Text(
            text = "${viewModel.activeGenreName} $titleSuffix",
            style = MaterialTheme.typography.headlineLarge,
            color = Color.White,
            modifier = Modifier.padding(32.dp)
        )
        
        MovieRow(
            title = "", 
            movies = viewModel.genreMovies, 
            onMovieClick = { viewModel.navigateToDetails(it) }, 
            onMovieFocus = {},
            onLoadMore = { viewModel.loadMoreGenreMovies() }
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MainScreen(viewModel: HomeViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp, start = 32.dp, end = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = R.drawable.logo,
                contentDescription = "MovieVerse Logo",
                modifier = Modifier
                    .height(40.dp)
                    .width(40.dp)
            )
            
            Spacer(modifier = Modifier.width(24.dp))

            TabRow(
                selectedTabIndex = viewModel.selectedTab.ordinal,
            ) {
                TvTab.entries.forEach { tab ->
                    Tab(
                        selected = viewModel.selectedTab == tab,
                        onFocus = { viewModel.onTabSelected(tab) },
                        onClick = { viewModel.onTabSelected(tab) }
                    ) {
                        Text(
                            text = when(tab) {
                                TvTab.Home -> "Home"
                                TvTab.TvShows -> "TV Shows"
                                TvTab.Anime -> "Anime"
                                TvTab.Search -> "Search"
                            },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (viewModel.selectedTab) {
                TvTab.Home -> HomeScreenContent(viewModel)
                TvTab.TvShows -> TvShowsScreenContent(viewModel)
                TvTab.Anime -> AnimeScreenContent(viewModel)
                TvTab.Search -> SearchScreen(viewModel)
            }
        }
    }
}

@Composable
fun HomeScreenContent(viewModel: HomeViewModel) {
    val scrollState = rememberScrollState()

    if (viewModel.isLoading && viewModel.trendingMovies.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = Color.Red)
        }
    } else {
        Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
            // Re-enforce HeroBanner with focusable key
            key(viewModel.trendingMovies.size) {
                HeroBanner(movies = viewModel.trendingMovies, onPlayClick = { viewModel.navigateToDetails(it) })
            }
            
            MovieRow("Popular Movies", viewModel.popularMovies, onMovieClick = { viewModel.navigateToDetails(it) }, onMovieFocus = {}, onLoadMore = { viewModel.loadMorePopular() })
            MovieRow("Netflix Originals", viewModel.netflixHome, onMovieClick = { viewModel.navigateToDetails(it) }, onMovieFocus = {}, onLoadMore = { viewModel.loadMoreNetflix() })
            MovieRow("Prime Video Picks", viewModel.primeHome, onMovieClick = { viewModel.navigateToDetails(it) }, onMovieFocus = {}, onLoadMore = { viewModel.loadMoreNetflix() })
            MovieRow("Disney+ Collection", viewModel.disneyHome, onMovieClick = { viewModel.navigateToDetails(it) }, onMovieFocus = {}, onLoadMore = { viewModel.loadMoreNetflix() })
            MovieRow("Hindi Hits", viewModel.hindiHome, onMovieClick = { viewModel.navigateToDetails(it) }, onMovieFocus = {}, onLoadMore = { viewModel.loadMoreRegional() })
            MovieRow("South Indian Blockbusters", viewModel.southHome, onMovieClick = { viewModel.navigateToDetails(it) }, onMovieFocus = {}, onLoadMore = { viewModel.loadMoreRegional() })
            
            GenreRow("Explore by Genre", viewModel.movieGenrePosters, onGenreClick = { id, name -> viewModel.onGenreClick(id, name) })
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

@Composable
fun TvShowsScreenContent(viewModel: HomeViewModel) {
    val scrollState = rememberScrollState()
    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        key(viewModel.trendingTv.size) {
            HeroBanner(movies = viewModel.trendingTv, onPlayClick = { viewModel.navigateToDetails(it) })
        }
        
        MovieRow("Popular TV Shows", viewModel.popularTv, onMovieClick = { viewModel.navigateToDetails(it) }, onMovieFocus = {}, onLoadMore = { viewModel.loadMorePopularTv() })
        MovieRow("Top Rated Series", viewModel.topRatedTv, onMovieClick = { viewModel.navigateToDetails(it) }, onMovieFocus = {}, onLoadMore = { viewModel.loadMoreTopRatedTv() })
        MovieRow("Airing Today", viewModel.onTheAirTv, onMovieClick = { viewModel.navigateToDetails(it) }, onMovieFocus = {}, onLoadMore = { viewModel.loadMoreOnTheAirTv() })
        MovieRow("Popular on Netflix", viewModel.netflixTv, onMovieClick = { viewModel.navigateToDetails(it) }, onMovieFocus = {}, onLoadMore = { viewModel.loadMoreNetflixTv() })
        MovieRow("Prime Video Picks", viewModel.primeTv, onMovieClick = { viewModel.navigateToDetails(it) }, onMovieFocus = {}, onLoadMore = { viewModel.loadMorePrimeTv() })
        MovieRow("Disney+ Collection", viewModel.disneyTv, onMovieClick = { viewModel.navigateToDetails(it) }, onMovieFocus = {}, onLoadMore = { viewModel.loadMoreDisneyTv() })
        MovieRow("Hindi TV Hits", viewModel.hindiTv, onMovieClick = { viewModel.navigateToDetails(it) }, onMovieFocus = {}, onLoadMore = { viewModel.loadMoreHindiTv() })
        MovieRow("South Indian Series", viewModel.southTv, onMovieClick = { viewModel.navigateToDetails(it) }, onMovieFocus = {}, onLoadMore = { viewModel.loadMoreSouthTv() })
        MovieRow("Hollywood TV Picks", viewModel.hollywoodTv, onMovieClick = { viewModel.navigateToDetails(it) }, onMovieFocus = {}, onLoadMore = { viewModel.loadMoreHollywoodTv() })
        
        GenreRow("TV Genres", viewModel.tvGenrePosters, onGenreClick = { id, name -> viewModel.onGenreClick(id, name) })
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun AnimeScreenContent(viewModel: HomeViewModel) {
    val scrollState = rememberScrollState()
    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        key(viewModel.latestAnime.size) {
            HeroBanner(movies = viewModel.latestAnime, onPlayClick = { viewModel.navigateToDetails(it) })
        }
        
        MovieRow("Latest Anime", viewModel.latestAnime, onMovieClick = { viewModel.navigateToDetails(it) }, onMovieFocus = {}, onLoadMore = { viewModel.loadMoreAnime() })
        MovieRow("Trending Right Now", viewModel.trendingAnime, onMovieClick = { viewModel.navigateToDetails(it) }, onMovieFocus = {}, onLoadMore = { viewModel.loadMoreAnime() })
        MovieRow("All-Time Popular", viewModel.popularAnime, onMovieClick = { viewModel.navigateToDetails(it) }, onMovieFocus = {}, onLoadMore = { viewModel.loadMoreAnime() })
        MovieRow("Top Ranked Masterpieces", viewModel.topRatedAnime, onMovieClick = { viewModel.navigateToDetails(it) }, onMovieFocus = {}, onLoadMore = { viewModel.loadMoreAnime() })
        MovieRow("Crunchyroll Collection", viewModel.crunchyrollAnime, onMovieClick = { viewModel.navigateToDetails(it) }, onMovieFocus = {}, onLoadMore = { viewModel.loadMoreAnime() })
        MovieRow("Seasonal Hits", viewModel.seasonalAnime, onMovieClick = { viewModel.navigateToDetails(it) }, onMovieFocus = {}, onLoadMore = { viewModel.loadMoreAnime() })
        
        GenreRow("Anime Moods", viewModel.animeGenrePosters, onGenreClick = { id, name -> viewModel.onGenreClick(id, name) })
        Spacer(modifier = Modifier.height(48.dp))
    }
}
