package com.example.termproject

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.termproject.databinding.ActivityMaketestBinding
import com.google.firebase.firestore.FirebaseFirestore

data class Question(
    val question: String = "",
    val choices: List<String> = emptyList(),
    val answer: String = ""
)

class MakeTestActivity : AppCompatActivity() {
    lateinit var binding: ActivityMaketestBinding
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMaketestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.saveBtn.setOnClickListener {
            val question = binding.inputQuestion.text.toString()
            val choices = listOf(
                binding.choice1.text.toString(),
                binding.choice2.text.toString(),
                binding.choice3.text.toString(),
            )
            val answer = binding.answerInput.text.toString()

            val data = Question(question, choices, answer)

            db.collection("questions")
                .add(data)
                .addOnSuccessListener {
                    Toast.makeText(this, "저장 성공!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "저장 실패!", Toast.LENGTH_SHORT).show()
                }

            val intent = Intent(this, QuestionListActivity::class.java)
            startActivity(intent)
        }

        binding.backBtn.setOnClickListener {
            finish()
        }
    }
}