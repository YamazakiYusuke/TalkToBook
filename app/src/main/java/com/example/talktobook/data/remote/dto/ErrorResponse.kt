package com.example.talktobook.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ErrorResponse(
    @SerializedName("error")
    val error: ErrorDetails
)

data class ErrorDetails(
    @SerializedName("message")
    val message: String,
    @SerializedName("type")
    val type: String,
    @SerializedName("param")
    val param: String?,
    @SerializedName("code")
    val code: String?
)