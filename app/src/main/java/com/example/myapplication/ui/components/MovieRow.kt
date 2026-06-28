package com.example.myapplication.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.MaterialTheme
import androidx.tv.material3.Text
import com.example.myapplication.model.Movie

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun MovieRow(
    title: String,
    movies: List<Movie>,
    modifier: Modifier = Modifier,
    isLandscape: Boolean = false,
    onMovieClick: (Movie) -> Unit,
    onMovieFocus: (Movie) -> Unit,
    onLoadMore: () -> Unit = {},
    requestInitialFocus: Boolean = false
) {
    if (movies.isEmpty()) return

    val firstItemFocusRequester = remember { FocusRequester() }

    if (requestInitialFocus) {
        LaunchedEffect(movies) {
            if (movies.isNotEmpty()) {
                firstItemFocusRequester.requestFocus()
            }
        }
    }

    Column(modifier = modifier.padding(vertical = 12.dp)) {
        if (title.isNotEmpty()) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.padding(start = 32.dp, bottom = 12.dp)
            )
        }
        LazyRow(
            contentPadding = PaddingValues(horizontal = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = if (requestInitialFocus) {
                Modifier.focusProperties { up = FocusRequester.Cancel }
            } else {
                Modifier
            }
        ) {
            itemsIndexed(
                items = movies,
                key = { _, movie -> movie.id }
            ) { index, movie ->
                if (index >= movies.size - 5) {
                    LaunchedEffect(movies.size) {
                        onLoadMore()
                    }
                }
                MovieCard(
                    movie = movie,
                    isLandscape = isLandscape,
                    onClick = { onMovieClick(movie) },
                    onFocus = { onMovieFocus(movie) },
                    modifier = if (index == 0 && requestInitialFocus) {
                        Modifier.focusRequester(firstItemFocusRequester)
                    } else {
                        Modifier
                    }
                )
            }
        }
    }
}
