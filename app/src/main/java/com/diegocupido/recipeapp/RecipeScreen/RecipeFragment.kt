package com.diegocupido.recipeapp.RecipeScreen

import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.Paint
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.ScrollView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.diegocupido.recipeapp.RecipeAdapter.FeaturedRecipesAdapter
import com.diegocupido.recipeapp.R
import com.diegocupido.recipeapp.apiStuff.Message
import com.diegocupido.recipeapp.apiStuff.OpenAIRequest
import com.diegocupido.recipeapp.apiStuff.OpenAIResponse
import com.diegocupido.recipeapp.apiStuff.RetrofitInstance
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Locale

class RecipeFragment : Fragment(), TextToSpeech.OnInitListener {
    private lateinit var textToSpeech: TextToSpeech
    private lateinit var ttsSwitch: SwitchCompat
    private var isTtsReady = false
    private lateinit var inputRecipe: EditText
    private lateinit var resultTextView: TextView
    private lateinit var recipeCardView: CardView
    private lateinit var sharedPreferences: SharedPreferences
    private val request_code_pick_image = 1
    private var isAnimating = false  // Add this flag
    private lateinit var scrollView: ScrollView
    private lateinit var progressBar: ProgressBar
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_recipe_generation, container, false)
        scrollView = view.findViewById(R.id.scrollView)
        progressBar = view.findViewById(R.id.loading_progress_bar)
        showLoadingIndicator(false)

        textToSpeech = TextToSpeech(requireContext(), this)
        ttsSwitch = view.findViewById(R.id.material_switch)
        inputRecipe = view.findViewById(R.id.input_recipe)
        resultTextView = view.findViewById(R.id.generatedRecipeText)
        recipeCardView = view.findViewById(R.id.recipeCardView)
        sharedPreferences =
            requireActivity().getSharedPreferences("RecipePrefs", AppCompatActivity.MODE_PRIVATE)

        loadSavedData()
        onClickHintUpdate()
        setupStarButton(view)
        underlineText(view)
        displayingFeaturedRecipes(view)
        setUpUi(view)

        // Text to speech check
        ttsSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (!isAnimating && isChecked && isTtsReady && resultTextView.text.isNotEmpty()) {
                speakRecipe()
            } else {
                textToSpeech.stop()
            }
        }

        return view
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            textToSpeech.language = Locale.UK // Set language, you can change this as needed
            isTtsReady = true
        } else {
            Log.e("RecipeFragment", "TextToSpeech initialization failed")
        }
    }

    // Output text to speech
    private fun speakRecipe() {
        if (ttsSwitch.isChecked && resultTextView.text.isNotEmpty()) {
            textToSpeech.speak(resultTextView.text.toString(), TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        textToSpeech.stop()
        textToSpeech.shutdown()
    }

    private fun loadSavedData() {
        val savedInput = sharedPreferences.getString("savedInput", "")
        val savedResult = sharedPreferences.getString("savedResult", "")
        Log.d("RecipeFragment", "Loaded input: $savedInput, Loaded result: $savedResult")

        inputRecipe.setText(savedInput)
        resultTextView.text = savedResult
        recipeCardView.visibility = if (savedResult.isNullOrEmpty()) View.GONE else View.VISIBLE
    }

    override fun onPause() {
        super.onPause()
        Log.d("RecipeFragment", "onPause called")
        clearSavedData()
    }

    private fun clearSavedData() {
        with(sharedPreferences.edit()) {
            putString("savedInput", "")
            putString("savedResult", "")
            apply()
        }
    }

    override fun onResume() {
        super.onResume()
        loadSavedData()
    }

    // rewrite the hint within the edit view
    private fun onClickHintUpdate() {
        inputRecipe.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                inputRecipe.hint = ""
            } else {
                inputRecipe.hint = "Enter The Name Of The Dish"
            }
        }
    }

    private fun setupStarButton(view: View) {
        val starButton: ImageButton = view.findViewById(R.id.save_button)

        starButton.setOnClickListener {
            showTitleInputDialog()
        }
    }

    private fun showTitleInputDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_input, null)
        val dialogInputTitle: EditText = dialogView.findViewById(R.id.dialog_input_recipe)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Enter Recipe Title")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                val recipeTitle = dialogInputTitle.text.toString()
                if (recipeTitle.isNotEmpty()) {
                    saveRecipe(recipeTitle)
                } else {
                    Snackbar.make(requireView(), "Please enter a title.", Snackbar.LENGTH_SHORT)
                        .show()
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    // Storing the save recipe to be retrieved
    private fun saveRecipe(recipeTitle: String) {
        val recipeDetails = resultTextView.text.toString()

        if (recipeTitle.isNotEmpty() && recipeDetails.isNotEmpty()) {
            val editor = sharedPreferences.edit()
            val userId = sharedPreferences.getString("currentUserId", "defaultUserId") // You should set this when the user logs in
            val savedRecipesKey = "savedRecipes_$userId"
            val savedRecipes = sharedPreferences.getStringSet(savedRecipesKey, mutableSetOf()) ?: mutableSetOf()
            savedRecipes.add("$recipeTitle::$recipeDetails") // Save as "title::details"
            editor.putStringSet(savedRecipesKey, savedRecipes)
            editor.apply()

            val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
            Snackbar.make(requireView(), "Recipe saved!", Snackbar.LENGTH_SHORT)
                .setAnchorView(bottomNavigationView)
                .show()
        }
    }

    // underlines featured recipe header
    private fun underlineText(view: View) {
        val featuredRecipesText: TextView = view.findViewById(R.id.featured_recipe_text)
        featuredRecipesText.paintFlags = featuredRecipesText.paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }

    // displays and generates the featured recipes
    private fun displayingFeaturedRecipes(view: View) {
        val recyclerView: RecyclerView = view.findViewById(R.id.featured_recipes_recycler_view)
        recyclerView.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)

        val recipes = listOf(
            Pair("Pizza", R.drawable.pizza_slice),
            Pair("Spaghetti", R.drawable.spagetti),
            Pair("Ramen", R.drawable.ramen_bowl),
            Pair("Bacon&Eggs", R.drawable.scrambled_eggs),
            Pair("Steak", R.drawable.steak)
        )

        val adapter = FeaturedRecipesAdapter(recipes) { recipeName ->
            makeRecipeCall(recipeName)
        }
        recyclerView.adapter = adapter
    }

    //  initializes the input dialog and image upload feature
    private fun setUpUi(view: View) {
        inputRecipe.setOnClickListener {
            showInputDialog()
        }

        val selectImageBtn = view.findViewById<Button>(R.id.upload_image_button)
        selectImageBtn.setOnClickListener {
            val pickImageIntent =
                Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickImageIntent, request_code_pick_image)
        }
    }
    // displays the dialog box
    private fun showInputDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_input, null)
        val dialogInputRecipe: EditText = dialogView.findViewById(R.id.dialog_input_recipe)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Enter Recipe Name")
            .setView(dialogView)
            .setPositiveButton("Save") { dialog, _ ->
                val userInput = dialogInputRecipe.text.toString()

                if (userInput.isNotEmpty()) {
                    inputRecipe.hint = userInput
                    makeRecipeCall(userInput)
                } else {
                    resultTextView.text = "Please enter a dish name."
                    recipeCardView.visibility = View.GONE
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .show()
    }
    // loading animation
    private fun showLoadingIndicator(show: Boolean) {
        progressBar.visibility = if (show) View.VISIBLE else View.GONE
    }
    // making the api call
    private fun makeRecipeCall(userInput: String) {
        showLoadingIndicator(true) // Show loading indicator
        recipeCardView.visibility = View.GONE
        val requestBody = OpenAIRequest(
            model = "gpt-4o",
            messages = listOf(
                Message(
                    "system",
                    "You are a recipe AI generator for a mobile app. Keep the recipe simple and short. Your purpose is to generate recipes based on user input nothing else. Add a good amount of emojis too. Do not say enjoy, bon-appetit and stuff like that. Keep it within this format: name of dish -> ingredients -> instructions. Also do not use abbreviated words such as tsp, to tbsp, say it in full. Do not use symbols like '->' to extend a word or sentence."
                ),
                Message("user", "Generate a recipe for: $userInput")
            ),
            max_tokens = 500
        )

        RetrofitInstance.api.getRecipe(requestBody).enqueue(object : retrofit2.Callback<OpenAIResponse> {
            @RequiresApi(Build.VERSION_CODES.P)
            override fun onResponse(
                call: retrofit2.Call<OpenAIResponse>,
                response: retrofit2.Response<OpenAIResponse>
            ) {
                showLoadingIndicator(false) // Hide loading indicator
                if (response.isSuccessful) {
                    val recipeResult = response.body()?.choices?.get(0)?.message?.content
                    displayGeneratedRecipe(recipeResult ?: "No response")
                    recipeCardView.visibility = View.VISIBLE
                } else {
                    displayGeneratedRecipe("Failed to get a response: ${response.errorBody()?.string()}")
                }
            }

            @RequiresApi(Build.VERSION_CODES.P)
            override fun onFailure(call: retrofit2.Call<OpenAIResponse>, t: Throwable) {
                showLoadingIndicator(false) // Hide loading indicator
                displayGeneratedRecipe("Error: ${t.message}")
            }
        })
    }
    // displays a well formatted generated recipe inside of an animated cardview
    @RequiresApi(Build.VERSION_CODES.P)
    private fun displayGeneratedRecipe(recipe: String) {
        val cleanedRecipe = recipe.replace("#", "").replace("*", "")
        val formattedRecipe = cleanedRecipe
            .replace("ingredients:", "\nIngredients:", ignoreCase = true)
            .replace("instructions:", "\nInstructions:", ignoreCase = true)
        typeWriterEffect(formattedRecipe.trim())


        recipeCardView.visibility = View.GONE


        recipeCardView.alpha = 0f
        recipeCardView.visibility = View.VISIBLE
        recipeCardView.animate()
            .alpha(1f)
            .setDuration(500)
            .start()

        scrollView.post {
            scrollView.scrollTo(0, recipeCardView.top)
        }

        if (ttsSwitch.isChecked && isTtsReady) {
            speakRecipe()
        }
    }

    // type writer animation for generating the recipe
    private fun typeWriterEffect(finalText: String) {
        resultTextView.text = ""
        val handler = Handler(Looper.getMainLooper())
        val delay: Long = 20

        isAnimating = true

        for (i in finalText.indices) {
            handler.postDelayed({
                resultTextView.append(finalText[i].toString())

                if (i == finalText.lastIndex) {
                    isAnimating = false

                    scrollView.post {
                        scrollView.scrollTo(0, recipeCardView.top)
                    }

                    if (ttsSwitch.isChecked && isTtsReady) {
                        speakRecipe()
                    }
                }
            }, i * delay)
        }
    }

    // overriding onActivityResult to access camera features
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == request_code_pick_image && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                val bitmap =
                    MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, uri)
                val base64Image = encodeImageToBase64(bitmap)
                sendImageToOpenAI(base64Image)
            }
        }
    }

    private fun encodeImageToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    // making the api call to process image
    private fun sendImageToOpenAI(base64Image: String) {
        showLoadingIndicator(true)
        val client = OkHttpClient()
        val apiKey =
            "sk-YK3yPfA8esGlqSTc6RugrS51vtLD_bS0imI0FRIB4ST3BlbkFJIZdhAoP-DUa8b4Jx14SrTrb6rhuTVVL8rIgnlXkuYA"
        val url = "https://api.openai.com/v1/chat/completions"

        val payload = """
    {
      "model": "gpt-4o-mini",
      "messages": [
        {
          "role": "user",
          "content": [
            {
              "type": "text",
              "text": "You are a recipe AI generator for a mobile app. Keep the recipe simple and short. Your purpose is to generate recipes based on user input nothing else. Add a good amount of emojis too. Do not say enjoy, bon-appetit and stuff like that. Keep it within this format: name of dish -> ingredients -> instructions. Say tsp, and tbsp in full. Do not use symbols like '->' to extend a word or sentence"
            },
            {
              "type": "image_url",
              "image_url": {
                "url": "data:image/jpeg;base64,$base64Image"
              }
            }
          ]
        }
      ],
      "max_tokens": 300
    }
    """.trimIndent()

        val requestBody = payload.toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .addHeader("Authorization", "Bearer $apiKey")
            .build()

        client.newCall(request).enqueue(object : Callback {
            @RequiresApi(Build.VERSION_CODES.P)
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonResponse = response.body?.string()?.let { JSONObject(it) }
                    val recipeResult = jsonResponse?.getJSONArray("choices")?.getJSONObject(0)
                        ?.getJSONObject("message")?.getString("content")

                    activity?.runOnUiThread {
                        if (recipeResult != null) {
                            displayGeneratedRecipe(recipeResult)
                        }
                    }
                } else {
                    Log.e("ImageToOpenAI", "Error: ${response.code}: ${response.body?.string()}")
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                showLoadingIndicator(false)
                Log.e("ImageToOpenAI", "Error: ${e.message}")
            }
        })
    }

}