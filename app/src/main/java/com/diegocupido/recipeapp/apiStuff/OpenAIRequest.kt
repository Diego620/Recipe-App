package com.diegocupido.recipeapp.apiStuff

data class OpenAIRequest(
    val model: String,
    val messages: List<Message>,
    val max_tokens: Int
)

data class Message(
    val role: String,
    val content: String
)

