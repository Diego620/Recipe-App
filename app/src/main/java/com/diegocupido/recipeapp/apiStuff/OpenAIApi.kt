package com.diegocupido.recipeapp.apiStuff

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface OpenAIApi {
    @POST("chat/completions")
    fun getRecipe(
        @Body request: OpenAIRequest
    ): Call<OpenAIResponse>
}
