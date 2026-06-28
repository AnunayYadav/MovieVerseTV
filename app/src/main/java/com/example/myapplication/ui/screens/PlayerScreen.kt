package com.example.myapplication.ui.screens

import android.view.KeyEvent
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.tv.material3.*
import com.example.myapplication.data.RetrofitClient
import com.example.myapplication.data.Providers
import com.example.myapplication.model.AnilistRequest
import com.example.myapplication.model.Movie
import com.example.myapplication.ui.HomeViewModel
import kotlinx.coroutines.delay
import java.io.ByteArrayInputStream

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun PlayerScreen(
    movie: Movie,
    viewModel: HomeViewModel,
    onBack: () -> Unit
) {
    val tmdbId = movie.id
    var anilistId by remember { mutableStateOf<Int?>(null) }
    var isWebViewLoading by remember { mutableStateOf(true) }

    var isRendered by remember { mutableStateOf(false) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var lastLoadedUrl by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        delay(350) // Allow screen transition / ripple animation to finish smoothly
        isRendered = true
    }

    DisposableEffect(Unit) {
        onDispose {
            webViewRef?.apply {
                stopLoading()
                clearHistory()
                removeAllViews()
                destroy()
            }
        }
    }

    val season = viewModel.selectedSeason
    val episode = viewModel.selectedEpisode
    val isTv = movie.mediaType == "tv" || movie.firstAirDate != null

    LaunchedEffect(movie.id) {
        val query = "query (\$search: String) { Page(page: 1, perPage: 1) { media(search: \$search, type: ANIME) { id } } }"
        val variables = mapOf("search" to movie.displayTitle)
        try {
            val response = RetrofitClient.anilistApi.searchAnime(AnilistRequest(query, variables))
            anilistId = response.data.Page.media.firstOrNull()?.id
        } catch (e: Exception) { e.printStackTrace() }
    }

    var showControls by remember { mutableStateOf(false) }

    Box(modifier = Modifier
        .fillMaxSize()
        .background(Color.Black)
        .onKeyEvent {
            if (it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_MENU || 
                it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
                showControls = !showControls
                true
            } else false
        }
    ) {
        if (isRendered) {
            AndroidView(
                factory = { context ->
                    val wv = WebView(context).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        requestFocus()
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            mediaPlaybackRequiresUserGesture = false
                            javaScriptCanOpenWindowsAutomatically = false
                            setSupportMultipleWindows(false)
                            userAgentString = "Mozilla/5.0 (Linux; Android 10; SmartTV) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.132 Mobile Safari/537.36"
                        }
                        webViewClient = object : WebViewClient() {
                            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
                                isWebViewLoading = true
                            }
                            override fun onPageFinished(view: WebView?, url: String?) {
                                isWebViewLoading = false
                            }
                            override fun shouldInterceptRequest(view: WebView?, request: WebResourceRequest?): WebResourceResponse? {
                                val url = request?.url?.toString() ?: ""
                                val adKeywords = listOf("doubleclick", "googlead", "adsystem", "pop", "click", "track", "banner", "pixel", "captcha", "robot")
                                if (adKeywords.any { url.contains(it, ignoreCase = true) }) {
                                    return WebResourceResponse("text/plain", "UTF-8", ByteArrayInputStream("".toByteArray()))
                                }
                                return super.shouldInterceptRequest(view, request)
                            }
                            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                                val url = request?.url?.toString() ?: ""
                                val allowed = listOf("player", "vid", "embed", "weserv", "vidsrc", "peachify", "zxcstream", "cinesrc")
                                return !allowed.any { url.contains(it) }
                            }
                        }
                        
                        addJavascriptInterface(object {
                            @JavascriptInterface
                            fun postMessage(message: String) {
                                try {
                                    val json = com.google.gson.JsonParser.parseString(message).asJsonObject
                                    val type = json.get("type")?.asString ?: ""
                                    
                                    if (type == "cinesrc:timeupdate") {
                                        val current = json.get("currentTime")?.asFloat ?: 0f
                                        val dur = json.get("duration")?.asFloat ?: 0f
                                        if (current > 0 && dur > 0) {
                                            viewModel.saveProgress(
                                                mediaId = movie.id,
                                                title = movie.displayTitle,
                                                posterPath = movie.posterPath,
                                                backdropPath = movie.backdropPath,
                                                mediaType = if (isTv) "tv" else "movie",
                                                season = season,
                                                episode = episode,
                                                watched = current,
                                                duration = dur
                                            )
                                        }
                                        return
                                    }

                                    val data = json.getAsJsonObject("data")
                                    if (data != null) {
                                        val current = data.get("currentTime")?.asFloat ?: 0f
                                        val dur = data.get("duration")?.asFloat ?: 0f
                                        if (current > 0 && dur > 0) {
                                            viewModel.saveProgress(
                                                mediaId = movie.id,
                                                title = movie.displayTitle,
                                                posterPath = movie.posterPath,
                                                backdropPath = movie.backdropPath,
                                                mediaType = if (isTv) "tv" else "movie",
                                                season = season,
                                                episode = episode,
                                                watched = current,
                                                duration = dur
                                            )
                                        }
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }, "AndroidBridge")
                    }
                    webViewRef = wv
                    wv
                },
                update = { webView ->
                    val targetUrl = Providers.getUrl(
                        index = viewModel.selectedProviderIndex,
                        tmdbId = tmdbId,
                        anilistId = anilistId,
                        isTv = isTv,
                        season = season,
                        episode = episode,
                        language = Providers.LANGUAGES[viewModel.selectedLanguageIndex],
                        subtitle = "English"
                    )
                    if (lastLoadedUrl != targetUrl) {
                        lastLoadedUrl = targetUrl
                        val htmlWrapper = """
                            <!DOCTYPE html>
                            <html>
                            <head>
                                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                                <style>
                                    html, body { margin: 0; padding: 0; width: 100%; height: 100%; overflow: hidden; background-color: black; }
                                    iframe { border: none; width: 100%; height: 100%; }
                                </style>
                            </head>
                            <body>
                                <iframe id="player" src="$targetUrl" allow="autoplay; fullscreen; picture-in-picture" allowfullscreen></iframe>
                                <script>
                                    window.addEventListener('message', (event) => {
                                        if (window.AndroidBridge && event.data) {
                                            window.AndroidBridge.postMessage(JSON.stringify(event.data));
                                        }
                                    });
                                </script>
                            </body>
                            </html>
                        """.trimIndent()
                        webView.loadDataWithBaseURL(targetUrl, htmlWrapper, "text/html", "UTF-8", null)
                    }
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        if (!isRendered || isWebViewLoading) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.align(Alignment.Center)) {
                    CircularProgressIndicator(color = Color.Red)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("Loading Player...", color = Color.White)
                }
            }
        }

        AnimatedVisibility(
            visible = showControls,
            enter = slideInHorizontally { it } + fadeIn(),
            exit = slideOutHorizontally { it } + fadeOut(),
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(300.dp)
                    .background(Color.Black.copy(alpha = 0.95f))
                    .padding(24.dp)
            ) {
                Column {
                    Text("Settings", style = MaterialTheme.typography.headlineSmall, color = Color.White)
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
                        Text("Select Source", style = MaterialTheme.typography.titleMedium, color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp))
                        Providers.NAMES.forEachIndexed { index, name ->
                            Button(
                                onClick = { 
                                    viewModel.selectedProviderIndex = index
                                    showControls = false
                                    isWebViewLoading = true
                                },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = ButtonDefaults.colors(
                                    containerColor = if (viewModel.selectedProviderIndex == index) Color(0xFFE50914) else Color(0xFF1A1A1A)
                                )
                            ) {
                                Text(name)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Select Language", style = MaterialTheme.typography.titleMedium, color = Color.Gray, modifier = Modifier.padding(vertical = 4.dp))
                        Providers.LANGUAGES.forEachIndexed { index, lang ->
                            Button(
                                onClick = { 
                                    viewModel.selectedLanguageIndex = index
                                    showControls = false
                                    isWebViewLoading = true
                                },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                                colors = ButtonDefaults.colors(
                                    containerColor = if (viewModel.selectedLanguageIndex == index) Color(0xFFE50914) else Color(0xFF1A1A1A)
                                )
                            ) {
                                Text(lang)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedButton(onClick = { showControls = false }, modifier = Modifier.fillMaxWidth()) {
                        Text("Close Menu")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = onBack, modifier = Modifier.fillMaxWidth(), colors = ButtonDefaults.colors(containerColor = Color.DarkGray)) {
                        Text("Exit Player")
                    }
                }
            }
        }
    }

    LaunchedEffect(showControls, isWebViewLoading) {
        if (showControls && !isWebViewLoading) {
            delay(5000)
            showControls = false
        }
    }
}
