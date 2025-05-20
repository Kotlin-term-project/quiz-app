package com.example.termproject

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.termproject.databinding.ActivityTaketestBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class TakeTestActivity : AppCompatActivity() {
    lateinit var binding: ActivityTaketestBinding
    private val db = FirebaseFirestore.getInstance()

    private var currentQuestionNum = 0
    private var questionList = mutableListOf<FileData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaketestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val folderId = intent.getStringExtra("folderName")
        val fileDataList = intent.getParcelableArrayListExtra<FileData>("fileData")
        val time = intent.getStringExtra("time")

        fileDataList?.let {
            questionList.addAll(it)
        }

        if (questionList.isNotEmpty()) {
            loadQuestion(0)
        }

        binding.folderIdText.text = folderId
        binding.timeText.text = time

        // 다음 문제로 넘어 가는 버튼 구현
        binding.nextBtn.setOnClickListener {
            if (currentQuestionNum < questionList.size - 1) {
                currentQuestionNum++
                loadQuestion(currentQuestionNum)
            } else {
                Toast.makeText(this, "다음 문제가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 이전 문제로 가는 버튼 구현
        binding.prevBtn.setOnClickListener {
            if (currentQuestionNum > 0) {
                currentQuestionNum--
                loadQuestion(currentQuestionNum)
            } else {
                Toast.makeText(this, "이전 문제가 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        // 그만 두기 버튼
        binding.stopBtn.setOnClickListener {
            finish()
        }
    }

    // 문제를 화면에 표시
    fun loadQuestion(num: Int) {
        if (num in questionList.indices) {
            val question = questionList[num]
            binding.questionText.text = question.question
            binding.choice1Text.text = question.choice1
            binding.choice2Text.text = question.choice2
            binding.choice3Text.text = question.choice3
            binding.answerText.text = question.answer
        }
    }
}