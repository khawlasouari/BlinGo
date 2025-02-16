package com.example.myapplication;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ChatGPTApiService {
    @Headers({
            "Content-Type: application/json",
            "Authorization: Bearer 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"
    })
    @POST("v1/chat/completions")
    Call<ChatGPTResponse> getChatGPTResponse(@Body ChatGPTRequest request);
}
