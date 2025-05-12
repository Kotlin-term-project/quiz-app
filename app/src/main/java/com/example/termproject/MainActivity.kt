package com.example.termproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.termproject.databinding.ActivityMainBinding
import com.google.firebase.firestore.FirebaseFirestore


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate((layoutInflater))
        setContentView(binding.root)

        // 시험 만들기 버튼 구현
        binding.makeTestBtn.setOnClickListener {
            val intent = Intent(this, MakeTestActivity::class.java)
            startActivity(intent)
        }
        
        // 시험 치는 버튼 구현
        binding.takeTestBtn.setOnClickListener {
            val intent = Intent(this, TakeTestActivity::class.java)
            startActivity(intent)
        }

        // 암기 하기 버튼 구현
        binding.memorizeBtn.setOnClickListener {
            val intent = Intent(this, MemorizeActivity::class.java)
            startActivity(intent)
        }

        var questionList = ""

        // 시험 문제 만들고 저장을 하면 목록이 뜰거임
        db.collection("questions")
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val question = doc.getString("문제")
                    val choice1 = doc.getString("1번")
                    val choice2 = doc.getString("2번")
                    val choice3 = doc.getString("3번")
                    val answer = doc.getString("정답")

                    questionList += """
                        문제: $question
                        1. $choice1
                        2. $choice2
                        3. $choice3
                        정답: $answer
                    """.trimIndent()
                }

                binding.output.text = questionList
            }
            .addOnFailureListener {
                binding.output.text = "불러오기 실패"
            }
    }
}