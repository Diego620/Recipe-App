package com.diegocupido.recipeapp.apiStuff

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    private val client = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val originalRequest = chain.request()
            val request = originalRequest.newBuilder()
                .header("Authorization", "Bearer sk-YK3yPfA8esGlqSTc6RugrS51vtLD_bS0imI0FRIB4ST3BlbkFJIZdhAoP-DUa8b4Jx14SrTrb6rhuTVVL8rIgnlXkuYA") // Replace with your actual API key
                .build()
            chain.proceed(request)
        }
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.openai.com/v1/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: OpenAIApi by lazy {
        retrofit.create(OpenAIApi::class.java)
    }
}
