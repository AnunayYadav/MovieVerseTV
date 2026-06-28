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
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusRestorer
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.focusGroup
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.focus.onFocusChanged
import com.example.myapplication.ui.components.GenreRow
import com.example.myapplication.ui.components.GenreItem
import com.example.myapplication.ui.components.HeroBanner
import com.example.myapplication.ui.components.MovieRow
import com.example.myapplication.ui.components.ShimmerRow
import com.example.myapplication.ui.components.ProviderRow
import com.example.myapplication.ui.components.ProviderItem
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
                Box(modifier = Modifier.fillMaxSize().background(Color(0xFF000000))) {
                    AppNavigation()
                }
            }
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun AppNavigation(viewModel: HomeViewModel = viewModel()) {
    BackHandler(enabled = viewModel.currentScreen != AppScreen.Main) {
        viewModel.navigateBack()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val isMain = viewModel.currentScreen == AppScreen.Main
        val mainModifier = if (isMain) {
            Modifier
                .fillMaxSize()
                .focusRestorer()
                .focusGroup()
        } else {
            Modifier
                .fillMaxSize()
                .focusProperties { canFocus = false }
        }
        Box(modifier = mainModifier) {
            MainScreen(viewModel)
        }

        val isGenreActive = viewModel.activeGenreName != null
        if (isGenreActive) {
            val isGenreFocused = viewModel.currentScreen == AppScreen.Genre
            val genreModifier = if (isGenreFocused) {
                Modifier
                    .fillMaxSize()
                    .focusRestorer()
                    .focusGroup()
            } else {
                Modifier
                    .fillMaxSize()
                    .focusProperties { canFocus = false }
            }
            Box(modifier = genreModifier) {
                GenreScreenContent(viewModel)
            }
        }

        if (viewModel.currentScreen == AppScreen.Details) {
            viewModel.selectedMovie?.let { movie ->
                DetailsScreen(
                    movie = movie,
                    viewModel = viewModel,
                    onPlayClick = { viewModel.navigateToPlayer(movie) },
                    onBackClick = { viewModel.navigateBack() },
                    onMovieClick = { viewModel.navigateToDetails(it) }
                )
            }
        }

        if (viewModel.currentScreen == AppScreen.Player) {
            viewModel.selectedMovie?.let { movie ->
                PlayerScreen(
                    movie = movie,
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )
            }
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
    
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFF000000))) {
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
            onLoadMore = { viewModel.loadMoreGenreMovies() },
            requestInitialFocus = true
        )
    }
}

@OptIn(ExperimentalTvMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun MainScreen(viewModel: HomeViewModel) {
    val tabFocusRequesters = remember { List(TvTab.entries.size) { FocusRequester() } }

    LaunchedEffect(Unit) {
        tabFocusRequesters[viewModel.selectedTab.ordinal].requestFocus()
    }

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
                modifier = Modifier
                    .focusGroup()
                    .focusProperties {
                        onEnter = {
                            tabFocusRequesters[viewModel.selectedTab.ordinal]
                        }
                        up = FocusRequester.Cancel
                    }
            ) {
                TvTab.entries.forEachIndexed { index, tab ->
                    val isSelected = viewModel.selectedTab == tab
                    var isTabFocused by remember { mutableStateOf(false) }

                    Tab(
                        selected = isSelected,
                        onFocus = { 
                            if (viewModel.selectedTab != tab) {
                                viewModel.onTabSelected(tab) 
                            }
                        },
                        onClick = { viewModel.onTabSelected(tab) },
                        modifier = Modifier
                            .focusRequester(tabFocusRequesters[index])
                            .onFocusChanged { isTabFocused = it.isFocused }
                    ) {
                        Text(
                            text = when(tab) {
                                TvTab.Home -> "Home"
                                TvTab.TvShows -> "TV Shows"
                                TvTab.Anime -> "Anime"
                                TvTab.Search -> "Search"
                            },
                            modifier = Modifier
                                .background(
                                    color = when {
                                        isTabFocused -> Color.White.copy(alpha = 0.2f)
                                        isSelected -> Color(0xFFE50914).copy(alpha = 0.15f)
                                        else -> Color.Transparent
                                    },
                                    shape = RoundedCornerShape(20.dp)
                                )
                                .padding(horizontal = 20.dp, vertical = 10.dp),
                            color = when {
                                isTabFocused -> Color.White
                                isSelected -> Color(0xFFE50914)
                                else -> Color.White.copy(alpha = 0.6f)
                            },
                            fontWeight = if (isSelected || isTabFocused) FontWeight.Bold else FontWeight.Medium,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            val selected = viewModel.selectedTab

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = if (selected == TvTab.Home) 1f else 0f }
                    .focusProperties { canFocus = selected == TvTab.Home }
            ) {
                HomeScreenContent(viewModel)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = if (selected == TvTab.TvShows) 1f else 0f }
                    .focusProperties { canFocus = selected == TvTab.TvShows }
            ) {
                TvShowsScreenContent(viewModel)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = if (selected == TvTab.Anime) 1f else 0f }
                    .focusProperties { canFocus = selected == TvTab.Anime }
            ) {
                AnimeScreenContent(viewModel)
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { alpha = if (selected == TvTab.Search) 1f else 0f }
                    .focusProperties { canFocus = selected == TvTab.Search }
            ) {
                SearchScreen(viewModel)
            }
        }
    }
}

@Composable
fun HomeScreenContent(viewModel: HomeViewModel) {
    val scrollState = rememberScrollState()
    val movieGenres = remember {
        listOf(
            GenreItem(28, "Action", ""),
            GenreItem(12, "Adventure", ""),
            GenreItem(16, "Animation", ""),
            GenreItem(35, "Comedy", ""),
            GenreItem(18, "Drama", ""),
            GenreItem(27, "Horror", ""),
            GenreItem(878, "Sci-Fi", ""),
            GenreItem(10749, "Romance", "")
        )
    }
    val providers = remember {
        listOf(
            ProviderItem(8, "Netflix", "https://upload.wikimedia.org/wikipedia/commons/thumb/0/08/Netflix_2015_logo.svg/256px-Netflix_2015_logo.svg.png"),
            ProviderItem(119, "Amazon Prime Video", "https://upload.wikimedia.org/wikipedia/commons/thumb/1/11/Amazon_Prime_Video_logo.svg/256px-Amazon_Prime_Video_logo.svg.png"),
            ProviderItem(350, "Apple TV", "https://upload.wikimedia.org/wikipedia/commons/1/16/AppleTVLogo.png"),
            ProviderItem(337, "Disney+ Hotstar", "https://upload.wikimedia.org/wikipedia/commons/f/fe/JioHotstar_logo.jpg"),
            ProviderItem(15, "Hulu", "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f9/Hulu_logo_%282018%29.svg/256px-Hulu_logo_%282018%29.svg.png"),
            ProviderItem(283, "Crunchyroll", "https://upload.wikimedia.org/wikipedia/commons/thumb/f/f6/Crunchyroll_Logo.svg/256px-Crunchyroll_Logo.svg.png"),
            ProviderItem(384, "HBO Max", "https://upload.wikimedia.org/wikipedia/commons/thumb/b/b3/HBO_Max_%282025%29.svg/256px-HBO_Max_%282025%29.svg.png")
        )
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        if (viewModel.trendingMovies.isNotEmpty()) {
            HeroBanner(
                movies = viewModel.trendingMovies,
                onPlayClick = { viewModel.navigateToDetails(it) }
            )
        } else if (viewModel.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(450.dp)
                    .background(Color(0xFF1E1E1E))
            )
        }
        
        if (viewModel.isLoading && viewModel.popularMovies.isEmpty()) {
            ShimmerRow(isLandscape = false)
            ShimmerRow(isLandscape = true)
            ShimmerRow(isLandscape = false)
        } else {
            if (viewModel.continueWatching.isNotEmpty()) {
                MovieRow(
                    title = "Continue Watching",
                    movies = viewModel.continueWatching,
                    onMovieClick = { viewModel.navigateToDetails(it) },
                    onMovieFocus = {}
                )
            }
            
            MovieRow("Popular Movies", viewModel.popularMovies, onMovieClick = { viewModel.navigateToDetails(it) }, onMovieFocus = {}, onLoadMore = { viewModel.loadMorePopular() })
            MovieRow("Netflix Originals", viewModel.netflixHome, onMovieClick = { viewModel.navigateToDetails(it) }, onMovieFocus = {}, onLoadMore = { viewModel.loadMoreNetflix() })
            MovieRow("Prime Video Picks", viewModel.primeHome, onMovieClick = { viewModel.navigateToDetails(it) }, onMovieFocus = {}, onLoadMore = { viewModel.loadMoreNetflix() })
            MovieRow("Disney+ Collection", viewModel.disneyHome, onMovieClick = { viewModel.navigateToDetails(it) }, onMovieFocus = {}, onLoadMore = { viewModel.loadMoreNetflix() })
            MovieRow("Hindi Hits", viewModel.hindiHome, onMovieClick = { viewModel.navigateToDetails(it) }, onMovieFocus = {}, onLoadMore = { viewModel.loadMoreRegional() })
            MovieRow("South Indian Blockbusters", viewModel.southHome, onMovieClick = { viewModel.navigateToDetails(it) }, onMovieFocus = {}, onLoadMore = { viewModel.loadMoreRegional() })
            
            GenreRow("Explore by Genre", movieGenres, viewModel.movieGenrePosters, onGenreClick = { id, name -> viewModel.onGenreClick(id, name) })
            ProviderRow("Browse by Provider", providers, onProviderClick = { id, name -> viewModel.onProviderClick(id, name) })
        }
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun TvShowsScreenContent(viewModel: HomeViewModel) {
    val scrollState = rememberScrollState()
    val tvGenres = remember {
        listOf(
            GenreItem(10759, "Action & Adventure", ""),
            GenreItem(16, "Animation", ""),
            GenreItem(35, "Comedy", ""),
            GenreItem(18, "Drama", ""),
            GenreItem(10765, "Sci-Fi & Fantasy", ""),
            GenreItem(9648, "Mystery", ""),
            GenreItem(80, "Crime", "")
        )
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        if (viewModel.trendingTv.isNotEmpty()) {
            HeroBanner(
                movies = viewModel.trendingTv,
                onPlayClick = { viewModel.navigateToDetails(it) }
            )
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
        
        GenreRow("TV Genres", tvGenres, viewModel.tvGenrePosters, onGenreClick = { id, name -> viewModel.onGenreClick(id, name) })
        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun AnimeScreenContent(viewModel: HomeViewModel) {
    val scrollState = rememberScrollState()
    val animeGenres = remember {
        listOf(
            GenreItem(28, "Action", ""),
            GenreItem(12, "Adventure", ""),
            GenreItem(35, "Comedy", ""),
            GenreItem(18, "Drama", ""),
            GenreItem(27, "Horror", ""),
            GenreItem(878, "Sci-Fi", ""),
            GenreItem(10749, "Romance", "")
        )
    }

    Column(modifier = Modifier.fillMaxSize().verticalScroll(scrollState)) {
        if (viewModel.latestAnime.isNotEmpty()) {
            HeroBanner(
                movies = viewModel.latestAnime,
                onPlayClick = { viewModel.navigateToDetails(it) }
            )
        }
        
        MovieRow("Latest Anime", viewModel.latestAnime, onMovieClick = { viewModel.navigateToDetails(it) }, onMovieFocus = {}, onLoadMore = { viewModel.loadMoreAnime() })
        MovieRow("Trending Right Now", viewModel.trendingAnime, onMovieClick = { viewModel.navigateToDetails(it) }, onMovieFocus = {}, onLoadMore = { viewModel.loadMoreAnime() })
        MovieRow("All-Time Popular", viewModel.popularAnime, onMovieClick = { viewModel.navigateToDetails(it) }, onMovieFocus = {}, onLoadMore = { viewModel.loadMoreAnime() })
        MovieRow("Top Ranked Masterpieces", viewModel.topRatedAnime, onMovieClick = { viewModel.navigateToDetails(it) }, onMovieFocus = {}, onLoadMore = { viewModel.loadMoreAnime() })
        MovieRow("Crunchyroll Collection", viewModel.crunchyrollAnime, onMovieClick = { viewModel.navigateToDetails(it) }, onMovieFocus = {}, onLoadMore = { viewModel.loadMoreAnime() })
        MovieRow("Seasonal Hits", viewModel.seasonalAnime, onMovieClick = { viewModel.navigateToDetails(it) }, onMovieFocus = {}, onLoadMore = { viewModel.loadMoreAnime() })
        
        GenreRow("Anime Moods", animeGenres, viewModel.animeGenrePosters, onGenreClick = { id, name -> viewModel.onGenreClick(id, name) })
        Spacer(modifier = Modifier.height(48.dp))
    }
}
