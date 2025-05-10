package com.example.termproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.termproject.databinding.ActivityQuestionlistBinding
import com.google.firebase.firestore.FirebaseFirestore

class QuestionListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuestionlistBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionlistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        db.collection("questions")
            .get()
            .addOnSuccessListener { result ->
                for (doc in result) {
                    val q = doc.toObject(Question::class.java)
                    binding.output.append("문제: ${q.question}\n보기: ${q.choices}\n정답: ${q.answer}\n\n")
                }
            }
            .addOnFailureListener {
                binding.output.text = "불러오기 실패"
            }

        binding.mainBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
