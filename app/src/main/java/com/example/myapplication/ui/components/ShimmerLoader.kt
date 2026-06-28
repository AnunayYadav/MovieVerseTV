package com.example.myapplication.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ShimmerRow(
    isLandscape: Boolean = false,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )

    val shimmerColors = listOf(
        Color(0xFF1E1E1E),
        Color(0xFF2C2C2C),
        Color(0xFF1E1E1E)
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim.value, y = translateAnim.value)
    )

    Column(modifier = modifier.padding(vertical = 12.dp)) {
        Box(
            modifier = Modifier
                .padding(start = 32.dp, bottom = 12.dp)
                .width(180.dp)
                .height(24.dp)
                .background(brush, shape = RoundedCornerShape(4.dp))
        )
        LazyRow(
            contentPadding = PaddingValues(horizontal = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            userScrollEnabled = false
        ) {
            items(6) {
                Box(
                    modifier = Modifier
                        .width(if (isLandscape) 260.dp else 160.dp)
                        .aspectRatio(if (isLandscape) 16f / 9f else 2f / 3f)
                        .background(brush, shape = RoundedCornerShape(8.dp))
                )
            }
        }
    }
}
