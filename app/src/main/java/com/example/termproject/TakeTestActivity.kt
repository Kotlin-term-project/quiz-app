package com.example.termproject

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.termproject.databinding.ActivityTaketestBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import android.widget.ImageView


class TakeTestActivity : AppCompatActivity() {
    lateinit var binding: ActivityTaketestBinding
    private val db = FirebaseFirestore.getInstance()

    private var currentQuestionNum = 0
    private var questionList = mutableListOf<FileData>()
    private var timerJob: Job? = null
    private var totalTime = 15
    private lateinit var checkBoxes: List<ImageView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaketestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val folderId = intent.getStringExtra("folderName")
        val fileDataList = intent.getParcelableArrayListExtra<FileData>("fileData")
        val time = intent.getStringExtra("time")

        // 체크박스 리스트
        checkBoxes = listOf(
            binding.choice1,
            binding.choice2,
            binding.choice3,
            binding.choiceAnswer
        )

        // 클릭 리스너 설정
        checkBoxes.forEach {imageView ->
            imageView.setOnClickListener {
                // 모든 체크박스 비선택 상태로
                checkBoxes.forEach { it.isSelected = false }

                // 클릭한 것만 선택 상태로
                imageView.isSelected = true
            }
        }

        fileDataList?.let {
            questionList.addAll(it)
        }

        if (questionList.isNotEmpty()) {
            loadQuestion(0)
        }

        // 데이터 베이스는 받아왔으니 나중에 추가 구현
        // binding.folderIdText.text = folderId
        // binding.timeText.text = time

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
    fun startTimer() {
        timerJob?.cancel() // 기존 타이머 중지
        binding.timerText.text = totalTime.toString()
        binding.timerProgress.max = totalTime
        binding.timerProgress.progress = totalTime

        timerJob = lifecycleScope.launch {
            for (i in totalTime downTo 0) {
                binding.timerText.text = i.toString()
                binding.timerProgress.progress = i
                delay(1000L)
            }

            Toast.makeText(this@TakeTestActivity, "시간 종료!", Toast.LENGTH_SHORT).show()

            // 자동 다음 문제 넘기기
            if (currentQuestionNum < questionList.size - 1) {
                currentQuestionNum++
                loadQuestion(currentQuestionNum)
            } else {
                Toast.makeText(this@TakeTestActivity, "마지막 문제입니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun updateQuestionCounter() {
        val current = currentQuestionNum + 1
        val total = questionList.size
        binding.questionCount.text = "$current / $total"
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

            startTimer()
            updateQuestionCounter()
            checkBoxes.forEach { it.isSelected = false }

        }
    }
}