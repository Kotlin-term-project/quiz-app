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
import com.google.firebase.firestore.Query

class AfterTestActivity: AppCompatActivity() {
    lateinit var binding: ActivityAftertestBinding
    private val db = FirebaseFirestore.getInstance()

    val userAnswers = mutableMapOf<String, String?>()
    val correctAnswers = mutableMapOf<String, String>()

    val questionList = mutableListOf<FileData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAftertestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val folderId = intent.getStringExtra("folderId") ?: return

        comparisonLogic()

        // 오답 보기 버튼 구현
        binding.showWrongAnswer.setOnClickListener {
            val wrongList = questionList.filter {
                it.userAnswer != it.answer
            }

            if (wrongList.isEmpty()) {
                Toast.makeText(this, "오답이 없습니다.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val intent = Intent(this, ShowWrongAnswerActivity::class.java)
            intent.putExtra("folderId", folderId)
            intent.putParcelableArrayListExtra("wrongList", ArrayList(wrongList))
            startActivity(intent)
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
    }

    fun comparisonLogic() {
        // 파일 정답 데이터 받기
        val folderId = intent.getStringExtra("folderId") ?: return

        db.collection("folders")
            .document(folderId)
            .collection("questions")
            .get()
            .addOnSuccessListener { docs ->
                val docsList = docs.documents
                for (doc in docsList) {
                    val question = doc.getString("문제")
                    val choice1 = doc.getString("1번") ?: ""
                    val choice2 = doc.getString("2번") ?: ""
                    val choice3 = doc.getString("3번") ?: ""
                    val answer = doc.getString("정답")
                    val userAnswer = answer ?: ""

                    if (question != null && answer != null) {
                        correctAnswers[question] = answer
                        questionList.add(
                            FileData(
                                id = folderId,
                                question = question,
                                choice1 = choice1,
                                choice2 = choice2,
                                choice3 = choice3,
                                answer = answer,
                                userAnswer = null
                            )
                        )
                    }
                }

                val numQuestions = correctAnswers.size

                // 사용자 접답 데이터 받기
                db.collection("user")
                    .document(folderId)
                    .collection("answer")
                    .orderBy("저장시간", Query.Direction.DESCENDING)
                    .limit(numQuestions.toLong())
                    .get()
                    .addOnSuccessListener { docs ->
                        val latest = docs.firstOrNull()?.getString("세션") ?: return@addOnSuccessListener

                        for (doc in docs) {
                            if (doc.getString("세션") == latest) {
                                val question = doc.getString("문제")?.trim()
                                val userAnswer = doc.getString("사용자정답")?.trim()

                                if (question != null) {
                                    userAnswers[question] = userAnswer
                                    questionList.find { it.question == question }?.userAnswer = userAnswer
                                }
                            }
                        }

                        // 사용자 답과 실제 답 비교 로직
                        var correctNum = 0
                        for ((question, userAnswer) in userAnswers) {
                            val correctAnswer = correctAnswers[question]
                            if (userAnswer != null && correctAnswer != null) {
                                if (userAnswer == correctAnswer) {
                                    correctNum++
                                }
                            }
                        }

                        // 정답 개수
                        binding.correctNum.text = correctNum.toString()

                        // 전체 문제 개수
                        binding.totalNum.text = numQuestions.toString()
                    }
            }
    }
}