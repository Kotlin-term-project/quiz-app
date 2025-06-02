package com.example.termproject

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.termproject.databinding.ActivityAicreateBinding
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class AICreateActivity: AppCompatActivity() {
    lateinit var binding: ActivityAicreateBinding

    override fun onCreate(savedStateInstance: Bundle?) {
        super.onCreate(savedStateInstance)
        binding = ActivityAicreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prompt = promptExample("대한민국의 수도는 어디인가요?", "서울", "부산")

        callHuggingFace(prompt) { result ->
            runOnUiThread {
                binding.hugAnswer.text = result
            }
        }
    }

    fun promptExample(question: String, correctAnswer: String, wrongAnswer: String): String {

        return """
            문제: $question
            정답 : $correctAnswer
            사용자 답: $wrongAnswer
            
            위의 문제와 유사한 객관식 문제 하나를 생성해주세요.
            출력 형식은 다음과 같습니다.
            
            문제: ?
            1번: ?
            2번: ?
            3번: ?
            정답: ?
        """.trimIndent()
    }

    fun callHuggingFace(prompt: String, onResult: (String) -> Unit) {
        val apiKey = BuildConfig.HF_API_KEY
        val model = "tiiuae/falcon-rw-1b"
        val client = OkHttpClient()

        val json = JSONObject().apply {
            put("inputs", prompt)
        }

        val mediaType = "application/json".toMediaTypeOrNull()
        val body = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://api-inference.huggingface.co/models/$model")
            .addHeader("Authorization", "Bearer $apiKey")
            .addHeader("Content-Type", "application/json")
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                onResult("요청 실패: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseStr = response.body?.string()

                if (response.isSuccessful && responseStr != null) {
                    try {
                        val jsonArray = JSONArray(responseStr)
                        val result = jsonArray.getJSONObject(0).getString("generated_text")
                        Log.d("GPT_RESULT", result) // onResult 바로 위에 넣어보기
                        onResult(result)
                    } catch (e: Exception) {
                        onResult("파싱 실패: ${e.message}")
                    }
                } else {
                    Log.e("HF_ERROR", "응답 코드: ${response.code}")
                    Log.e("HF_ERROR", "본문: ${responseStr}")
                    onResult("응답 실패: $responseStr")
                }
            }
        })
    }
}