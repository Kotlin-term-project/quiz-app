package com.example.termproject

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.termproject.databinding.ActivityMaketestBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore


// 시험 만들기
class MakeTestActivity : AppCompatActivity() {
    lateinit var binding: ActivityMaketestBinding
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMaketestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 시험 문제 만들고 저장 버튼 누르면 다음 화면에 뜰 내용 저장
        binding.saveBtn.setOnClickListener {
            val question = binding.inputQuestion.text.toString()
            val answer = binding.answerInput.text.toString()
            writeFirebase(question, answer)

            // 저장 버튼을 누르고 나면 바로 문제 목록이 뜨는 화면으로 넘어감
            val intent = Intent(this, SaveConfirmActivity::class.java)
            startActivity(intent)
        }

        // 뒤로 가기 버튼 구현
        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    fun writeFirebase(question: String, answer: String) {
        val choices = mapOf(
            "문제" to question,
            "1번" to binding.choice1.text.toString(),
            "2번" to binding.choice2.text.toString(),
            "3번" to binding.choice3.text.toString(),
            "정답" to answer
        )

        val colRef: CollectionReference = db.collection("questions")
        val docRef: Task<DocumentReference> = colRef.add(choices)

        docRef.addOnSuccessListener {
            Toast.makeText(this, "저장 성공!", Toast.LENGTH_SHORT).show()

            // 문서 ID를 저장 확인 화면에 넘기기
            val intent = Intent(this, SaveConfirmActivity::class.java)
            intent.putExtra("id", it.id)
            startActivity(intent)
        }

        docRef.addOnFailureListener {
            Toast.makeText(this, "저장 실패!", Toast.LENGTH_SHORT).show()
        }
    }
}

