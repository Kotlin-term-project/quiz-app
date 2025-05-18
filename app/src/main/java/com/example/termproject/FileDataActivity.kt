package com.example.termproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.termproject.databinding.ActivityFiledataBinding
import com.google.firebase.firestore.FirebaseFirestore


class FileDataActivity: AppCompatActivity() {
    lateinit var binding: ActivityFiledataBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFiledataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // FileAdapter 에서 보낸 데이터 받기
        val question = intent.getStringExtra("question")
        val choice1 = intent.getStringExtra("choice1")
        val choice2 = intent.getStringExtra("choice2")
        val choice3 = intent.getStringExtra("choice3")
        val answer = intent.getStringExtra("answer")

        // 화면 출력
        var saveFileData = ""

        saveFileData = """
            문제: $question
            1. $choice1
            2. $choice2
            3. $choice3
            정답: $answer
        """.trimIndent()

        binding.output.text = saveFileData

        // 뒤로 가기 버튼 구현
        binding.backBtn.setOnClickListener {
            finish()
        }
    }
}