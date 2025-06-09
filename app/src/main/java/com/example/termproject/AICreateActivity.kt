package com.example.termproject

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.termproject.databinding.ActivityAicreateBinding
import com.google.firebase.firestore.FirebaseFirestore
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

    private val db = FirebaseFirestore.getInstance()

    private var genQuestion: String?= null
    private var genChoice1: String?= null
    private var genChoice2: String?= null
    private var genChoice3: String?= null
    private var genAnswer: String?= null

    override fun onCreate(savedStateInstance: Bundle?) {
        super.onCreate(savedStateInstance)
        binding = ActivityAicreateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val folderId = intent.getStringExtra("folderId") ?: return

        binding.saveBtn.setOnClickListener {
            if (genQuestion != null && genChoice1 != null && genChoice2 != null && genChoice3 != null && genAnswer != null) {
                val written = mapOf(
                    "문제" to genQuestion,
                    "1번" to genChoice1,
                    "2번" to genChoice2,
                    "3번" to genChoice3,
                    "정답" to genAnswer
                )

                db.collection("folders")
                    .document(folderId)
                    .collection("questions")
                    .add(written)
                    .addOnSuccessListener {
                        Toast.makeText(this, "문제 저장 되었습니다.", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "저장 실패: ${it.message}.", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "문제를 먼저 생성해주세여.", Toast.LENGTH_SHORT).show()
            }
        }

        val data = intent.getParcelableExtra<FileData>("data")
        if (data == null) {
            binding.response.text = "문제가 전달되지 않았습니다."
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
            
            선택지는 틀린 답안 3개와 정답 하나로 구성됩니다.
            
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

                    val lines = reply.lines()

                    genQuestion = lines.find { it.startsWith("문제") }?.substringAfter("문제 :")?.trim()
                    genChoice1 = lines.find { it.startsWith("1번") }?.substringAfter("1번 :")?.trim()
                    genChoice2 = lines.find { it.startsWith("2번") }?.substringAfter("2번 :")?.trim()
                    genChoice3 = lines.find { it.startsWith("3번") }?.substringAfter("3번 :")?.trim()
                    genAnswer = lines.find { it.startsWith("정답") }?.substringAfter("정답 :")?.trim()
                }

            }
        })
    }
}