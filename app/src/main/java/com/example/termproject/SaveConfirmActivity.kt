package com.example.termproject

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.termproject.databinding.ActivitySaveconfirmBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.log


class SaveConfirmActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySaveconfirmBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySaveconfirmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 보낸 folderID 받기
        val folderId = intent.getStringExtra("fId") ?: ""

        // 보낸 문제 데이터 받기
        val question = intent.getStringExtra("문제") ?: ""
        val choice1 = intent.getStringExtra("1번") ?: ""
        val choice2 = intent.getStringExtra("2번") ?: ""
        val choice3 = intent.getStringExtra("3번") ?: ""
        val answer = intent.getStringExtra("정답") ?: ""

        binding.questionText.text = question
        binding.choice1Text.text = choice1
        binding.choice2Text.text = choice2
        binding.choice3Text.text = choice3
        binding.answerText.text = answer

        // 수정 버튼 구현
        binding.backBtn.setOnClickListener {
            finish()
        }

        // 메인 화면 가기 버튼 구현 (최종 저장)
        binding.mainBtn.setOnClickListener {

            val written = mapOf(
                "문제" to question,
                "1번" to choice1,
                "2번" to choice2,
                "3번" to choice3,
                "정답" to answer,
            )

            val colRef: CollectionReference = db
                .collection("folders")
                .document(folderId)
                .collection("questions")

            val docRef: Task<DocumentReference> = colRef.add(written)

            docRef.addOnSuccessListener {
                Toast.makeText(this, "저장 성공!", Toast.LENGTH_SHORT).show()

                // MainActivity 로 가기
                val mainActivityIntent = Intent(this, MainActivity::class.java)
                startActivity(mainActivityIntent)
            }

            docRef.addOnFailureListener {
                Toast.makeText(this, "저장 실패!", Toast.LENGTH_SHORT).show()
            }

        }
    }
}
