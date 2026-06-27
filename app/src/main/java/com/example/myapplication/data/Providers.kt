package com.example.myapplication.data

object Providers {
    val NAMES = listOf("VidEasy", "VidFast", "VidCore", "VidNest", "Peachify", "Vidify")

    fun getUrl(
        index: Int,
        tmdbId: Int,
        anilistId: Int?,
        isTv: Boolean,
        season: Int,
        episode: Int,
        color: String = "E50914"
    ): String {
        return when (index) {
            0 -> if (isTv) "https://player.videasy.net/tv/$tmdbId/$season/$episode?overlay=false&color=$color&autoplay=true"
                 else "https://player.videasy.net/movie/$tmdbId?overlay=false&color=$color&autoplay=true"
            1 -> if (isTv) "https://vidfast.pro/tv/$tmdbId/$season/$episode?autoPlay=true&theme=$color"
                 else "https://vidfast.pro/movie/$tmdbId?autoPlay=true&theme=$color"
            2 -> if (isTv) "https://vidcore.net/tv/$tmdbId/$season/$episode?autoPlay=true&theme=$color"
                 else "https://vidcore.net/movie/$tmdbId?autoPlay=true&theme=$color"
            3 -> if (anilistId != null) {
                    if (isTv) "https://vidnest.fun/anime/$anilistId/$episode/sub"
                    else "https://vidnest.fun/anime/$anilistId/1/sub"
                 } else {
                    if (isTv) "https://vidnest.fun/tv/$tmdbId/$season/$episode"
                    else "https://vidnest.fun/movie/$tmdbId"
                 }
            4 -> if (isTv) "https://peachify.pro/embed/tv/$tmdbId/$season/$episode?accent=$color&quality=1080&autoPlay=true"
                 else "https://peachify.pro/embed/movie/$tmdbId?accent=$color&quality=1080&autoPlay=true"
            5 -> if (isTv) "https://player.vidify.top/embed/tv/$tmdbId/$season/$episode?primarycolor=$color&autoplay=true"
                 else "https://player.vidify.top/embed/movie/$tmdbId?primarycolor=$color&autoplay=true"
            else -> "https://player.videasy.net/movie/$tmdbId?overlay=false&color=$color&autoplay=true"
        }
    }
}
