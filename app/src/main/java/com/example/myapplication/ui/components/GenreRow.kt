package com.example.myapplication.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.*
import coil.compose.AsyncImage

data class GenreItem(val id: Int, val name: String, val posterUrl: String)
data class ProviderItem(val id: Int, val name: String, val logoUrl: String)

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
                GenreCard(genre, onClick = { onGenreClick(genre.id, genre.name) })
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun GenreCard(genre: GenreItem, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }

    val (gradient, letter) = getGenreStyle(genre.name)

    Card(
        onClick = onClick,
        modifier = Modifier
            .width(200.dp)
            .height(100.dp)
            .onFocusChanged { isFocused = it.isFocused },
        shape = CardDefaults.shape(RoundedCornerShape(12.dp)),
        scale = CardDefaults.scale(focusedScale = 1.1f),
        border = CardDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(2.dp, Color.White),
                inset = 0.dp
            )
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(gradient)
        ) {
            // Large semi-transparent background letter
            Text(
                text = letter,
                style = TextStyle(
                    fontSize = 110.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White.copy(alpha = 0.08f)
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .offset(x = 12.dp, y = 24.dp)
            )

            Column(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 16.dp, end = 16.dp)
            ) {
                Text(
                    text = genre.name,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                if (isFocused) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Explore >",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ProviderRow(
    title: String,
    providers: List<ProviderItem>,
    onProviderClick: (Int, String) -> Unit
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
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(providers) { provider ->
                ProviderCard(provider, onClick = { onProviderClick(provider.id, provider.name) })
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun ProviderCard(provider: ProviderItem, onClick: () -> Unit) {
    var isFocused by remember { mutableStateOf(false) }

    Card(
        onClick = onClick,
        modifier = Modifier
            .width(160.dp)
            .height(90.dp)
            .onFocusChanged { isFocused = it.isFocused },
        shape = CardDefaults.shape(RoundedCornerShape(12.dp)),
        scale = CardDefaults.scale(focusedScale = 1.1f),
        border = CardDefaults.border(
            focusedBorder = Border(
                border = BorderStroke(2.dp, Color.White),
                inset = 0.dp
            )
        )
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(16.dp)
        ) {
            AsyncImage(
                model = provider.logoUrl,
                contentDescription = provider.name,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        }
    }
}

private fun getGenreStyle(name: String): Pair<Brush, String> {
    val (colors, letter) = when (name.lowercase()) {
        "action" -> listOf(Color(0xFFA9082B), Color(0xFF5D0014)) to "A"
        "adventure" -> listOf(Color(0xFF3F51B5), Color(0xFF1A237E)) to "A"
        "animation" -> listOf(Color(0xFFE65100), Color(0xFFBF360C)) to "A"
        "comedy" -> listOf(Color(0xFF00796B), Color(0xFF004D40)) to "C"
        "crime" -> listOf(Color(0xFF0D47A1), Color(0xFF01579B)) to "C"
        "documentary" -> listOf(Color(0xFF6A1B9A), Color(0xFF4A148C)) to "D"
        "drama" -> listOf(Color(0xFF004D40), Color(0xFF00241A)) to "D"
        "horror" -> listOf(Color(0xFF3E2723), Color(0xFF1B0000)) to "H"
        "sci-fi", "science fiction" -> listOf(Color(0xFF006064), Color(0xFF00363A)) to "S"
        "romance" -> listOf(Color(0xFFC2185B), Color(0xFF880E4F)) to "R"
        else -> listOf(Color(0xFF263238), Color(0xFF212121)) to (name.firstOrNull()?.toString()?.uppercase() ?: "G")
    }
    return Brush.horizontalGradient(colors) to letter
}
