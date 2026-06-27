package com.example.myapplication.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import coil.compose.AsyncImage

data class GenreItem(val id: Int, val name: String, val posterUrl: String)

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun GenreRow(
    title: String,
    genres: List<GenreItem>,
    genrePosters: Map<Int, String> = emptyMap(),
    onGenreClick: (Int, String) -> Unit
) {
    Column(modifier = Modifier.padding(vertical = 16.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.padding(start = 32.dp, bottom = 12.dp)
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(genres) { genre ->
                val posterUrl = genrePosters[genre.id] ?: "https://image.tmdb.org/t/p/w500/6xKCYjBv6mSgN4RPv9Yv9vYv9vY.jpg"
                GenreCard(genre.copy(posterUrl = posterUrl), onClick = { onGenreClick(genre.id, genre.name) })
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun GenreCard(genre: GenreItem, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .width(180.dp)
            .height(100.dp),
        shape = CardDefaults.shape(RoundedCornerShape(12.dp))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = genre.posterUrl,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                alpha = 0.7f
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                        )
                    )
            )
            Text(
                text = genre.name,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}
