package com.example.termproject

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.termproject.databinding.ActivityAicreateBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException


class AICreateActivity: AppCompatActivity() {
    lateinit var binding: ActivityAicreateBinding

    override fun onCreate(savedStateInstance: Bundle?) {
        super.onCreate(savedStateInstance)
        binding = ActivityAicreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val data = intent.getParcelableExtra<FileData>("data")
        if (data == null) {
            binding.response.text = "오답 문제가 전달되지 않았습니다."
            return
        }

        val prompt = """
            다음은 객관식 문제입니다.
            이 문제와 유사한 새로운 문제를 하나 생성해주세요.
            
            [문제]
            ${data.question}
            
            [선택지]
            ${data.choice1}
            ${data.choice2}
            ${data.choice3}
            ${data.answer}
            
            형식은 아래와 같이 해주세요.
            문제 : ...
            1번 : ...
            2번 : ...
            3번 : ...
            정답 : ...
        """.trimIndent()

        val apiKey = BuildConfig.OPENAI_API_KEY
        val client = OkHttpClient()

        val message = JSONObject().apply {
            put("role", "user")
            put("content", prompt)
        }

        val messagesArray = org.json.JSONArray().apply {
            put(message)
        }

        val jsonBody = JSONObject().apply {
            put("model", "gpt-3.5-turbo")
            put("messages", messagesArray)
        }

        val requestBody = jsonBody.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://api.openai.com/v1/chat/completions")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("GPT", "요청 실패: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()

                Log.d("GPT", "응답 코드: ${response.code}")
                Log.d("GPT", "응답 본문: $responseBody")

                val reply = try {
                    val jsonObject = JSONObject(responseBody ?: "")
                    jsonObject
                        .getJSONArray("choices")
                        .getJSONObject(0)
                        .getJSONObject("message")
                        .getString("content")
                } catch (e: Exception) {
                    "응답 파싱 실패: ${e.message}"
                }

                runOnUiThread {
                    binding.response.text = reply
                }

            }
        })
    }
}