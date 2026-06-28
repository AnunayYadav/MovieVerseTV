package com.example.myapplication.data

object Providers {
    val NAMES = listOf("VidEasy", "VidFast", "VidCore", "VidNest", "Peachify", "Vidify", "ZXCStream", "CineSRC")
    val LANGUAGES = listOf("Hindi", "English", "Tamil", "Telugu", "Kannada", "Malayalam", "Bengali")

    fun getUrl(
        index: Int,
        tmdbId: Int,
        anilistId: Int?,
        isTv: Boolean,
        season: Int,
        episode: Int,
        color: String = "E50914",
        language: String = "Hindi",
        subtitle: String = "English"
    ): String {
        val langCode = when (language.lowercase()) {
            "hindi" -> "hi"
            "english" -> "en"
            "tamil" -> "ta"
            "telugu" -> "te"
            "spanish" -> "es"
            "french" -> "fr"
            "portuguese" -> "pt"
            "indonesian" -> "id"
            "russian" -> "ru"
            "kannada" -> "kn"
            "malayalam" -> "ml"
            "bengali" -> "bn"
            else -> "en"
        }

        val subCode = when (subtitle.lowercase()) {
            "english" -> "en"
            "hindi" -> "hi"
            "spanish" -> "es"
            "french" -> "fr"
            "portuguese" -> "pt"
            else -> "en"
        }

        return when (index) {
            0 -> if (isTv) "https://player.videasy.net/tv/$tmdbId/$season/$episode?overlay=false&color=$color&autoplay=true"
                 else "https://player.videasy.net/movie/$tmdbId?overlay=false&color=$color&autoplay=true"
            1 -> if (isTv) "https://vidfast.pro/tv/$tmdbId/$season/$episode?autoPlay=true&theme=$color&sub=$subCode"
                 else "https://vidfast.pro/movie/$tmdbId?autoPlay=true&theme=$color&sub=$subCode"
            2 -> if (isTv) "https://vidcore.net/tv/$tmdbId/$season/$episode?autoPlay=true&theme=$color&sub=$subCode"
                 else "https://vidcore.net/movie/$tmdbId?autoPlay=true&theme=$color&sub=$subCode"
            3 -> if (anilistId != null) {
                    val animePref = if (language.lowercase() == "hindi") "hindi" else "sub"
                    if (isTv) "https://vidnest.fun/anime/$anilistId/$episode/$animePref"
                    else "https://vidnest.fun/anime/$anilistId/1/$animePref"
                 } else {
                    if (isTv) "https://vidnest.fun/tv/$tmdbId/$season/$episode"
                    else "https://vidnest.fun/movie/$tmdbId"
                 }
            4 -> if (isTv) "https://peachify.pro/embed/tv/$tmdbId/$season/$episode?accent=$color&quality=1080&autoPlay=true&dub=$language&sub=$subtitle"
                 else "https://peachify.pro/embed/movie/$tmdbId?accent=$color&quality=1080&autoPlay=true&dub=$language&sub=$subtitle"
            5 -> if (isTv) "https://player.vidify.top/embed/tv/$tmdbId/$season/$episode?primarycolor=$color&autoplay=true&server=${language.lowercase()}"
                 else "https://player.vidify.top/embed/movie/$tmdbId?primarycolor=$color&autoplay=true&server=${language.lowercase()}"
            6 -> if (isTv) "https://zxcstream.xyz/player/tv/$tmdbId/$season/$episode?autoplay=true&color=$color&dubLang=$langCode"
                 else "https://zxcstream.xyz/player/movie/$tmdbId?autoplay=true&color=$color&dubLang=$langCode"
            7 -> if (isTv) "https://cinesrc.st/embed/tv/$tmdbId?s=$season&e=$episode&autoplay=true&color=%23$color"
                 else "https://cinesrc.st/embed/movie/$tmdbId?autoplay=true&color=%23$color"
            else -> if (isTv) "https://vidfast.pro/tv/$tmdbId/$season/$episode?autoPlay=true&theme=$color&sub=$subCode"
                    else "https://vidfast.pro/movie/$tmdbId?autoPlay=true&theme=$color&sub=$subCode"
        }
    }
}
