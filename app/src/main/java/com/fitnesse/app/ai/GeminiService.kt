package com.fitnesse.app.ai

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

data class GeminiResponse(
    val candidates: List<Candidate>? = emptyList(),
)

data class Candidate(
    val content: Content? = null,
)

data class Content(
    val parts: List<Part>? = emptyList(),
)

data class Part(
    val text: String? = null,
)

class GeminiService(private val apiKey: String = ApiConfig.GEMINI_API_KEY) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val gson = Gson()

    suspend fun analyzeClothingImage(base64Image: String, mimeType: String): String {
        val prompt = """
            Analyze this clothing item photo and return JSON with EXACTLY these values:
            {
              "category": "Top|Bottom|Footwear|Outerwear|Accessory|Dress",
              "subcategory": "e.g., T-shirt, Jeans, Sneakers, Blazer, Necklace",
              "dominantColor": "exact color name like Navy Blue, Crimson Red, etc.",
              "secondaryColor": "second color if patterned, otherwise empty",
              "pattern": "Plain|Striped|Plaid|Floral|Polka Dot|Geometric|Tie-Dye|Solid",
              "length": "Cropped|Regular|Long|Oversized|Fitted"
            }
            Use the EXACT values listed after each colon. Do NOT use other variations.
        """.trimIndent()

        val jsonBody = buildJsonBody(prompt, base64Image, mimeType)
        return callGemini(jsonBody)
    }

    suspend fun getOutfitRecommendation(wardrobeContext: String): String {
        val prompt = """
            You are a fashion stylist. Based on these available items, select the best outfit.
            Apply: sandwich method, color theory, rule of thirds.
            Return JSON:
            {
              "selected": { "top": "item_id", "bottom": "item_id", "outerwear": "item_id", "footwear": "item_id" },
              "reasoning": "short explanation of why this outfit works"
            }
            
            Wardrobe:
            $wardrobeContext
        """.trimIndent()

        val jsonBody = """{"contents":[{"parts":[{"text":${gson.toJson(prompt)}}]}]}"""
        return callGemini(jsonBody)
    }

    private suspend fun callGemini(jsonBody: String): String {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent"
        val body = jsonBody.toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url(url)
            .addHeader("x-goog-api-key", apiKey)
            .post(body).build()

        return withContext(Dispatchers.IO) {
            try {
                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    Log.e("GeminiService", "HTTP ${response.code}: ${response.message}")
                    return@withContext "Error: HTTP ${response.code}"
                }
                val responseBody = response.body?.string() ?: "{}"
                val geminiResp = gson.fromJson(responseBody, GeminiResponse::class.java)
                val text = geminiResp.candidates?.firstOrNull()
                    ?.content?.parts?.firstOrNull()
                    ?.text ?: "No response"
                stripMarkdown(text)
            } catch (e: Exception) {
                val detail = "${e::class.simpleName}: ${e.message ?: e.toString()}"
                Log.e("GeminiService", "callGemini failed: $detail", e)
                "Error: $detail"
            }
        }
    }

    private fun stripMarkdown(text: String): String {
        return text.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .trim()
            .removeSuffix("```")
            .trim()
    }

    private fun buildJsonBody(prompt: String, base64Image: String, mimeType: String): String {
        return """{
            "contents": [{
                "parts": [
                    {"text": ${gson.toJson(prompt)}},
                    {"inlineData": {"mimeType": ${gson.toJson(mimeType)}, "data": ${gson.toJson(base64Image)}}}
                ]
            }]
        }"""
    }
}

data class GeminiAnalysisResult(
    val category: String = "",
    val subcategory: String = "",
    val dominantColor: String = "",
    val secondaryColor: String = "",
    val pattern: String = "",
    val length: String = "",
) {
    companion object {
        fun fromJson(json: String): GeminiAnalysisResult {
            return try {
                val obj = Gson().fromJson(json, JsonObject::class.java)
                GeminiAnalysisResult(
                    category = obj.get("category")?.asString ?: "",
                    subcategory = obj.get("subcategory")?.asString ?: "",
                    dominantColor = obj.get("dominantColor")?.asString ?: "",
                    secondaryColor = obj.get("secondaryColor")?.asString ?: "",
                    pattern = obj.get("pattern")?.asString ?: "",
                    length = obj.get("length")?.asString ?: "",
                )
            } catch (e: Exception) {
                GeminiAnalysisResult()
            }
        }
    }
}

