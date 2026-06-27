package com.example.myapplication.model

data class AnilistSearchResponse(
    val data: AnilistData
)

data class AnilistData(
    val Page: AnilistPage
)

data class AnilistPage(
    val media: List<AnilistMedia>
)

data class AnilistMedia(
    val id: Int,
    val title: AnilistTitle
)

data class AnilistTitle(
    val romaji: String?,
    val english: String?,
    val native: String?
)

data class AnilistRequest(
    val query: String,
    val variables: Map<String, Any>
)
