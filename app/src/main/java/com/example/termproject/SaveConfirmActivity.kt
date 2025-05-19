package com.example.termproject

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.termproject.databinding.ActivitySaveconfirmBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.log


class SaveConfirmActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySaveconfirmBinding
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySaveconfirmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 보낸 questionID 받기
        val questionId = intent.getStringExtra("qId")

        // 보낸 folderID 받기
        val folderId = intent.getStringExtra("fId")

        // 방금 저장한 문제의 데이터 가져 오기
        if (questionId != null) {
            db.collection("folders")
                .document(folderId!!)
                .collection("questions")
                .document(questionId)
                .get()
                .addOnSuccessListener { doc ->
                    val question = doc.getString("문제")
                    val choice1 = doc.getString("1번")
                    val choice2 = doc.getString("2번")
                    val choice3 = doc.getString("3번")
                    val answer = doc.getString("정답")

                    binding.questionText.text = question
                    binding.choice1Text.text = choice1
                    binding.choice2Text.text = choice2
                    binding.choice3Text.text = choice3
                    binding.answerText.text = answer

                }
                .addOnFailureListener {
                    Toast.makeText(this, "문제 불러오기 실패: ${it.message}", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "잘못된 접근입니다.", Toast.LENGTH_SHORT).show()
        }

        // 메인 화면 가기 버튼 구현
        binding.mainBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("fId", folderId)
            intent.putExtra("qId", questionId)
            startActivity(intent)
        }
    }
}
