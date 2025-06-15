package com.example.talktobook.data.remote.api

import com.example.talktobook.data.remote.dto.TranscriptionResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface OpenAIApi {
    @Multipart
    @POST("audio/transcriptions")
    suspend fun transcribeAudio(
        @Part file: MultipartBody.Part,
        @Part("model") model: RequestBody,
        @Part("language") language: RequestBody,
        @Part("response_format") responseFormat: RequestBody,
        @Part("temperature") temperature: RequestBody
    ): Response<TranscriptionResponse>
}