package com.example.termproject

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.termproject.databinding.ActivityAftertestBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class AfterTestActivity: AppCompatActivity() {
    lateinit var binding: ActivityAftertestBinding
    private val db = FirebaseFirestore.getInstance()

    val userAnswers = mutableMapOf<String, String>()
    val correctAnswers = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAftertestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 오답 보기 버튼 구현
        binding.showWrongAnswer.setOnClickListener {
            // 데이터 받아와서 구현
        }

        // 다시 풀기 버튼 구현
        binding.solveAgainBtn.setOnClickListener {
            val intent = Intent(this, TestReadyActivity::class.java)
            startActivity(intent)
        }

        // 완료 버튼 구현
        binding.finishBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        // 사용자 접답 데이터 받기
        val folderId = intent.getStringExtra("folderId") ?: return

        db.collection("user")
            .document(folderId)
            .collection("answer")
            .get()
            .addOnSuccessListener { docs ->
                for (doc in docs) {
                    val question = doc.getString("문제")
                    val userAnswer = doc.getString("사용자정답")

                    if (question != null && userAnswer != null) {
                        userAnswers[question] = userAnswer
                    }
                }

                // 파일 정답 데이터 받기
                db.collection("folders")
                    .document(folderId)
                    .collection("questions")
                    .get()
                    .addOnSuccessListener { docs ->
                        for (doc in docs) {
                            val question = doc.getString("문제")
                            val answer = doc.getString("정답")

                            if (question != null && answer != null) {
                                correctAnswers[question] = answer
                            }
                        }

                        // 사용자 답과 실제 답 비교 로직
                        var correctNum = 0
                        for ((question, userAnswer) in userAnswers) {
                            val correctAnswer = correctAnswers[question]
                            if (correctAnswer != null && userAnswer == correctAnswer) {
                                correctNum++
                            }
                        }

                        // 정답 개수
                        binding.correctNum.text = correctNum.toString()

                        // 전체 문제 개수
                        binding.totalNum.text = correctAnswers.size.toString()
                    }
            }
    }
}