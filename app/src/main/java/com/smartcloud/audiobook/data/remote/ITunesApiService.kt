package com.smartcloud.audiobook.data.remote

import com.squareup.moshi.Json
import retrofit2.http.GET
import retrofit2.http.Query

interface ITunesApiService {
    @GET("search")
    suspend fun searchAudiobooks(
        @Query("media") media: String = "audiobook",
        @Query("term") keyword: String,
        @Query("limit") limit: Int = 1,
    ): ITunesSearchResponse
}

data class ITunesSearchResponse(
    @Json(name = "resultCount") val resultCount: Int,
    @Json(name = "results") val results: List<ITunesAudiobookResult>,
)

data class ITunesAudiobookResult(
    @Json(name = "trackName") val trackName: String?,
    @Json(name = "artistName") val artistName: String?,
    @Json(name = "description") val description: String?,
    @Json(name = "artworkUrl100") val artworkUrl100: String?,
    @Json(name = "artworkUrl600") val artworkUrl600: String?,
)
