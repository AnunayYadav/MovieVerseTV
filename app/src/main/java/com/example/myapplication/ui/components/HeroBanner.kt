package com.example.myapplication.ui.components

import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import androidx.compose.foundation.focusGroup
import com.example.myapplication.model.Movie

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun HeroBanner(
    movies: List<Movie>,
    onPlayClick: (Movie) -> Unit,
    modifier: Modifier = Modifier
) {
    if (movies.isEmpty()) return

    Carousel(
        itemCount = movies.size.coerceAtMost(10),
        modifier = modifier
            .fillMaxWidth()
            .height(450.dp)
            .padding(bottom = 20.dp)
            .focusGroup(),
        contentTransformStartToEnd = fadeIn().togetherWith(fadeOut()),
        contentTransformEndToStart = fadeIn().togetherWith(fadeOut())
    ) { index ->
        val movie = movies[index]
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = movie.fullBackdropPath,
                contentDescription = movie.displayTitle,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.4f), Color.Transparent, Color(0xFF0F0F0F)),
                            startY = 0f
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.8f), Color.Transparent),
                            startX = 0f,
                            endX = 1200f
                        )
                    )
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(start = 58.dp, bottom = 48.dp)
            ) {
                Text(
                    text = movie.displayTitle,
                    style = MaterialTheme.typography.displayMedium,
                    color = Color.White
                )
                Text(
                    text = movie.overview,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 3,
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth(0.6f)
                )
                
                Button(
                    onClick = { onPlayClick(movie) },
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Text("Watch Now")
                }
            }
        }
    }
}
