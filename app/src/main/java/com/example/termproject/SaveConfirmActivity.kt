package com.example.termproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.termproject.databinding.ActivitySaveconfirmBinding
import com.google.firebase.firestore.FirebaseFirestore


class SaveConfirmActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySaveconfirmBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySaveconfirmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 보낸 문서 ID 받기
        val docId = intent.getStringExtra("id")

        // 방금 저장한 문제의 데이터 가져 오기
        var saveQuestion = ""
        if (docId != null) {
            db.collection("questions")
                .document(docId)
                .get()
                .addOnSuccessListener { doc ->
                    val question = doc.getString("문제")
                    val choice1 = doc.getString("1번")
                    val choice2 = doc.getString("2번")
                    val choice3 = doc.getString("3번")
                    val answer = doc.getString("정답")

                    saveQuestion = """
                        문제: $question
                        1. $choice1
                        2. $choice2
                        3. $choice3
                        정답: $answer
                    """.trimIndent()

                    binding.output.text = saveQuestion
                }
                .addOnFailureListener {
                    binding.output.text = "문제 불러오기 실패: ${it.message}"
                }
        } else {
            binding.output.text = "문제 ID가 없습니다."
        }

        // 메인 화면 가기 버튼 구현
        binding.mainBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("docId", docId)
            startActivity(intent)
        }
    }
}
