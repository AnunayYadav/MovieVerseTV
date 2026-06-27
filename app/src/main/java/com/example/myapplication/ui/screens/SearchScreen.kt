package com.example.myapplication.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import coil.compose.AsyncImage
import com.example.myapplication.ui.HomeViewModel
import com.example.myapplication.ui.components.MovieCard

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun SearchScreen(viewModel: HomeViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = "https://p-nt-www-2.akamaized.net/media/76be7020-f09b-11ef-807d-53609805908b/02-Logo.jpg",
                contentDescription = "MovieVerse Logo",
                modifier = Modifier.height(40.dp).width(40.dp)
            )
            Spacer(modifier = Modifier.width(24.dp))
            OutlinedTextField(
                value = viewModel.searchQuery,
                onValueChange = { viewModel.onSearchQueryChange(it) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search movies, shows, anime...", color = Color.Gray) },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.White,
                    unfocusedBorderColor = Color.DarkGray,
                    cursorColor = Color.White,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (viewModel.searchResults.isEmpty() && viewModel.searchQuery.length > 2) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "No results found for \"${viewModel.searchQuery}\"",
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.Gray
                )
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(viewModel.searchResults) { index, movie ->
                    if (index >= viewModel.searchResults.size - 10) {
                        LaunchedEffect(viewModel.searchResults.size) {
                            viewModel.loadMoreSearch()
                        }
                    }
                    MovieCard(
                        movie = movie,
                        onClick = { viewModel.navigateToDetails(it) },
                        onFocus = { viewModel.updateFeaturedMovie(it) }
                    )
                }
            }
        }
    }
}
