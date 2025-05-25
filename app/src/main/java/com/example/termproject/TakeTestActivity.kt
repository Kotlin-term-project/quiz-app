package com.example.termproject

import android.content.Intent
import android.os.Bundle
import android.view.View
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
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference


class TakeTestActivity : AppCompatActivity() {
    lateinit var binding: ActivityTaketestBinding
    private val db = FirebaseFirestore.getInstance()

    private var folderName: String? = null
    private var folderId: String? = null

    private var currentQuestionNum = 0
    private var questionList = mutableListOf<FileData>()
    private var timerJob: Job? = null
    private var totalTime: Int = 0
    private lateinit var checkBoxes: List<ImageView>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTaketestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 시험 준비 화면에서 데이터 받아 오기
        folderName = intent.getStringExtra("folderName")
        val fileDataList = intent.getParcelableArrayListExtra<FileData>("fileData")
        val time = intent.getStringExtra("time") ?: ""

        // 시간의 숫자 부분만 추출
        val numPart = Regex("\\d+").find(time)?.value?.toInt() ?: 0
        val isMinutes = time.contains("min")
        totalTime = if (isMinutes) numPart * 60 else numPart

        binding.folderNameText.text = folderName.toString()

        // 체크박스 리스트
        checkBoxes = listOf (
            binding.choice1,
            binding.choice2,
            binding.choice3,
            binding.choiceAnswer
        )

        // 클릭 리스너 설정
        checkBoxes.forEach { imageView ->
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

        // 미리 folderId 받아오기
        db.collection("folders")
            .whereEqualTo("폴더명", folderName)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    folderId = snapshot.documents[0].id
                }
            }

        // 다음 문제로 넘어 가는 버튼 구현
        binding.nextBtn.setOnClickListener {
            val isChecked = checkBoxes.any { it.isSelected }

            if (!isChecked) {
                Toast.makeText(this, "답안을 선택하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentQuestion = questionList[currentQuestionNum]
            writeFirebase(currentQuestion.question, currentQuestion.answer)

            if (currentQuestionNum < questionList.size - 1) {
                currentQuestionNum++
                loadQuestion(currentQuestionNum)
            }
        }

        // 이전 문제로 가는 버튼 구현
//        binding.prevBtn.setOnClickListener {
//            if (currentQuestionNum > 0) {
//                currentQuestionNum--
//                loadQuestion(currentQuestionNum)
//            } else {
//                Toast.makeText(this, "이전 문제가 없습니다.", Toast.LENGTH_SHORT).show()
//            }
//        }

        // 제출 하기 버튼 구현
        binding.submitBtn.setOnClickListener {
            val isChecked = checkBoxes.any { it.isSelected }

            if (!isChecked) {
                Toast.makeText(this, "답안을 선택하세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentQuestion = questionList[currentQuestionNum]
            writeFirebase(currentQuestion.question, currentQuestion.answer)

            val intent = Intent(this, AfterTestActivity::class.java)
            intent.putExtra("folderId", folderId)
            startActivity(intent)
        }

        // 그만 두기 버튼 구현
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

            if (num == questionList.size - 1) {
                binding.submitBtn.visibility = View.VISIBLE
                binding.nextBtn.visibility = View.GONE

            } else {
                binding.submitBtn.visibility = View.GONE
            }

            startTimer()
            updateQuestionCounter()
            checkBoxes.forEach { it.isSelected = false }

        }
    }

    // 사용자가 선택한 답을 저장하여 AfterTestActivity에서 답 비교 진행할거임
    fun writeFirebase(question: String, choiceAnswer: String) {

        // 사용자가 선택한 체크 박스 값을 저장
        val selectedIndex = checkBoxes.indexOfFirst { it.isSelected }
        val selectedAnswer = when (selectedIndex) {
            0 -> binding.choice1Text.text.toString()
            1 -> binding.choice2Text.text.toString()
            2 -> binding.choice3Text.text.toString()
            3 -> binding.answerText.text.toString()
            else -> null
        }

        val written = mapOf(
            "문제" to question,
            "사용자정답" to selectedAnswer,
        )

       folderId?.let {
           val userDoc = db
               .collection("user")
               .document(it)
           userDoc.set(mapOf("폴더명" to folderName))
           userDoc
               .collection("answer")
               .add(written)
       }
    }
}