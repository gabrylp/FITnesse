package com.fitnesse.app.ai

import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
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
            Analyze this clothing item photo and return JSON:
            {
              "category": "top|bottom|outerwear|footwear|accessory|dress",
              "subcategory": "t-shirt|jeans|sneakers|etc",
              "dominantColor": "primary color name",
              "secondaryColor": "secondary color if patterned",
              "pattern": "solid|striped|plaid|floral|graphic",
              "length": "cropped|regular|long|oversized|fitted"
            }
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
        val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-lite:generateContent?key=$apiKey"
        val body = jsonBody.toRequestBody("application/json".toMediaType())
        val request = Request.Builder().url(url).post(body).build()

        return try {
            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: "{}"
            val geminiResp = gson.fromJson(responseBody, GeminiResponse::class.java)
            geminiResp.candidates?.firstOrNull()
                ?.content?.parts?.firstOrNull()
                ?.text ?: "No response"
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
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

data class OutfitSelectionResult(
    val selected: Map<String, String> = emptyMap(),
    val reasoning: String = "",
) {
    companion object {
        fun fromJson(json: String): OutfitSelectionResult {
            return try {
                val obj = Gson().fromJson(json, JsonObject::class.java)
                val selectedObj = obj.getAsJsonObject("selected")
                val selected = mapOf(
                    "top" to (selectedObj?.get("top")?.asString ?: ""),
                    "bottom" to (selectedObj?.get("bottom")?.asString ?: ""),
                    "outerwear" to (selectedObj?.get("outerwear")?.asString ?: ""),
                    "footwear" to (selectedObj?.get("footwear")?.asString ?: ""),
                )
                OutfitSelectionResult(
                    selected = selected,
                    reasoning = obj.get("reasoning")?.asString ?: "",
                )
            } catch (e: Exception) {
                OutfitSelectionResult()
            }
        }
    }
}
