package com.example.termproject.network

import kotlinx.coroutines.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

suspend fun generateQuizgeckoQuestion(
    inputText: String,
    apiKey: String
): String = withContext(Dispatchers.IO) {
    val client = OkHttpClient()
    val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    // Quizgecko API에 맞춘 요청 JSON 생성
    val jsonBody = JSONObject().apply {
        put("input_text", inputText)          // 문제 생성에 사용할 텍스트
        put("question_type", "multiple_choice")  // 문제 유형 (필요시 변경 가능)
    }.toString()

    val requestBody = jsonBody.toRequestBody(jsonMediaType)

    val request = Request.Builder()
        .url("https://api.quizgecko.com/api/v1/questions/generate")  // 실제 API 엔드포인트 확인 필요
        .addHeader("Authorization", "Bearer $apiKey")
        .post(requestBody)
        .build()

    client.newCall(request).execute().use { response ->
        if (!response.isSuccessful) throw Exception("API 호출 실패: ${response.code}")

        val responseBody = response.body?.string() ?: throw Exception("응답 없음")
        val json = JSONObject(responseBody)

        // 반환 데이터 구조에 맞게 파싱 (예시는 'question' 필드 가정)
        return@withContext json.getString("question")
    }
}
