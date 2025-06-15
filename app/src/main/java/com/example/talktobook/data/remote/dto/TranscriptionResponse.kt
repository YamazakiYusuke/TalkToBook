package com.example.talktobook.data.remote.dto

import com.google.gson.annotations.SerializedName

data class TranscriptionResponse(
    @SerializedName("text")
    val text: String
)