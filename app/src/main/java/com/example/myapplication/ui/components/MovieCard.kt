package com.example.myapplication.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.tv.material3.Border
import androidx.tv.material3.Card
import androidx.tv.material3.CardDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import coil.compose.AsyncImage
import com.example.myapplication.model.Movie
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MovieCard(
    movie: Movie,
    modifier: Modifier = Modifier,
    isLandscape: Boolean = false,
    onClick: (Movie) -> Unit,
    onFocus: (Movie) -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    Card(
        onClick = { onClick(movie) },
        modifier = modifier
            .width(if (isLandscape) 260.dp else 160.dp)
            .aspectRatio(if (isLandscape) 16f / 9f else 2f / 3f)
            .onFocusChanged { 
                isFocused = it.isFocused
                if (it.isFocused) onFocus(movie) 
            }
            // Increase Z-index when focused to prevent neighbors from clipping
            .zIndex(if (isFocused) 1f else 0f),
        scale = CardDefaults.scale(focusedScale = 1.1f),
        border = CardDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(2.dp, Color.White),
                inset = 0.dp
            )
        )
    ) {
        AsyncImage(
            model = if (isLandscape) movie.fullBackdropPath else movie.fullPosterPath,
            contentDescription = movie.displayTitle,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
    }
}
