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
            val choice1 = binding.inputChoice1.text.toString()
            val choice2 = binding.inputChoice2.text.toString()
            val choice3 = binding.inputChoice3.text.toString()
            val answer = binding.inputAnswer.text.toString()

            // 항목이 하나라도 비어있으면, 저장 하지 않고 다음 화면으로 넘어가지 않음.
            if (question.isEmpty() || choice1.isEmpty() || choice2.isEmpty() || choice3.isEmpty() || answer.isEmpty()) {
                Toast.makeText(this, "모든 항목을 입력해주세요.", Toast.LENGTH_SHORT).show()
            } else {
                writeFirebase(question, choice1, choice2, choice3, answer)
            }
        }

        // 뒤로 가기 버튼 구현
        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    fun writeFirebase(question: String, choice1: String, choice2: String, choice3: String, answer: String) {
        val written = mapOf(
            "문제" to question,
            "1번" to choice1,
            "2번" to choice2,
            "3번" to choice3,
            "정답" to answer
        )

        val folderId = intent.getStringExtra("fId") ?: ""

        val colRef: CollectionReference = db
            .collection("folders")
            .document(folderId)
            .collection("questions")

        val docRef: Task<DocumentReference> = colRef.add(written)

        docRef.addOnSuccessListener {
            Toast.makeText(this, "저장 성공!", Toast.LENGTH_SHORT).show()

            // 문서 ID를 저장 확인 화면에 넘기기
            val intent = Intent(this, SaveConfirmActivity::class.java)
            intent.putExtra("fId", folderId)
            intent.putExtra("qId", it.id)
            startActivity(intent)
        }

        docRef.addOnFailureListener {
            Toast.makeText(this, "저장 실패!", Toast.LENGTH_SHORT).show()
        }
    }
}

