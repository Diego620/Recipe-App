package com.diegocupido.recipeapp.apiStuff

data class OpenAIResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
)
