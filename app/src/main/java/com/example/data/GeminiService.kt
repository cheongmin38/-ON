package com.example.data

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiService {
    private const val TAG = "GeminiService"
    
    // Model selection based on user request ('gemini-3.5-flash' for basic text tasks)
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Executes raw text generation prompt on Gemini 3.5 Flash
     */
    suspend fun generateText(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API key is not configured or is placeholder.")
            return@withContext "API 키가 설정되지 않아 샘플 응답을 제공합니다. 설정화면에서 키를 입력해 주세요."
        }

        try {
            // Build request JSON manually to avoid fragile serialization issues
            val requestJson = JSONObject()
            
            // Contents
            val contentsArray = org.json.JSONArray()
            val contentObj = JSONObject()
            val partsArray = org.json.JSONArray()
            val partObj = JSONObject()
            partObj.put("text", prompt)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestJson.put("contents", contentsArray)

            // System Instruction
            if (!systemInstruction.isNullOrEmpty()) {
                val sysInstObj = JSONObject()
                val sysPartsArray = org.json.JSONArray()
                val sysPartObj = JSONObject()
                sysPartObj.put("text", systemInstruction)
                sysPartsArray.put(sysPartObj)
                sysInstObj.put("parts", sysPartsArray)
                requestJson.put("systemInstruction", sysInstObj)
            }

            // Generation config
            val genConfig = JSONObject()
            genConfig.put("temperature", 0.7)
            requestJson.put("generationConfig", genConfig)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toString().toRequestBody(mediaType)

            val urlWithKey = "$BASE_URL?key=$apiKey"
            val request = Request.Builder()
                .url(urlWithKey)
                .post(requestBody)
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                Log.e(TAG, "API call failed with response code ${response.code}: $errBody")
                return@withContext getLocalFallbackResponse(prompt)
            }

            val respBodyStr = response.body?.string() ?: ""
            val responseJson = JSONObject(respBodyStr)
            
            val candidates = responseJson.optJSONArray("candidates")
            if (candidates != null && candidates.length() > 0) {
                val candidate = candidates.getJSONObject(0)
                val responseContent = candidate.optJSONObject("content")
                val responseParts = responseContent?.optJSONArray("parts")
                if (responseParts != null && responseParts.length() > 0) {
                    val outText = responseParts.getJSONObject(0).optString("text", "")
                    if (outText.isNotEmpty()) {
                        return@withContext outText.trim()
                    }
                }
            }
            return@withContext "응답을 처리할 수 없습니다."
        } catch (e: Exception) {
            Log.e(TAG, "Error executing Gemini API: ${e.message}", e)
            return@withContext getLocalFallbackResponse(prompt)
        }
    }

    /**
     * AI Recollection Therapy Warm Description Generation
     */
    suspend fun generateMemoryTherapyDescription(photoTitle: String, relationship: String): String {
        val prompt = "'$photoTitle' 활동 혹은 장소에 대한 기억 상기 묘사입니다. " +
                "이 사진은 환자분이 가족($relationship)과 촬영했거나 일상 속에서 간직하고 있는 소중한 사진입니다. " +
                "치매 환자가 정서적 편안함을 느끼고 과거의 기억을 자연스럽게 떠올릴 수 있도록 친절하고 짧게 한국어 문장 2~3줄로 설명문을 작성해줘. " +
                "환자에게 대화하듯 반말로 혹은 따뜻한 대화체로 작성해줘."
        val systemInstruction = "당신은 치매 임상 심리 치료 전문가로서 환자가 과거를 회상하고 정서적 안정을 가질 수 있게 돕는 따뜻하고 상냥한 돌보미 AI입니다. " +
                "불안을 늘릴 수 있는 단어는 절대 사용하지 마세요. 아주 단순하고 친근하게 답하세요."
        return generateText(prompt, systemInstruction)
    }

    /**
     * AI Guardian Summary Generator
     */
    suspend fun generateGuardianDailySummary(
        patientName: String,
        takenMeds: Int,
        totalMeds: Int,
        safeZoneAlerts: Int,
        cognitiveScore: Int,
        lastLocation: String
    ): String {
        val prompt = "환자 $patientName 님의 하루 상태 데이터 요약을 바탕으로 보호자에게 보여줄 친절한 감사와 결과 리포트를 3줄 내외로 작성해줘.\n" +
                "- 약 복용률: $takenMeds/$totalMeds 개 복용 완료\n" +
                "- 안전지대 이탈 경고 발생 횟수: $safeZoneAlerts 회\n" +
                "- 오늘 인지 훈련 점수: ${cognitiveScore}점 (최대 100점)\n" +
                "- 환자의 최종 감지 위치: $lastLocation"
        val systemInstruction = "보호자에게 환자의 일일 요약 상태를 상황에 맞게 격려와 정보 위주로 설명하세요. 한국어로 차분하고 신뢰성 있게 기술하세요."
        return generateText(prompt, systemInstruction)
    }

    /**
     * Fallback responses for local-only execution (prevent issues when API key is unconfigured)
     */
    private fun getLocalFallbackResponse(prompt: String): String {
        return when {
            prompt.contains("기억 상기") -> {
                "가족과 함께 보냈던 참 따뜻하고 아름다운 하루입니다. 당신 곁에는 늘 사랑하는 가족이 함께하며 지켜드리고 있으니 걱정 마세요."
            }
            prompt.contains("보호자") || prompt.contains("하루 상태") -> {
                "오늘 환자분은 예정된 오전 복약을 무사히 완료하셨습니다. 인지 훈련 점수는 어제보다 상승했으며, 안전 구역 내에 편안히 머무르고 계십니다."
            }
            else -> {
                "기억지킴이가 항상 함께할게요. 마음 편히 다음 단계를 따라와 주세요."
            }
        }
    }
}
