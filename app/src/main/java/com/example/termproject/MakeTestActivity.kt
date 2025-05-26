package com.example.termproject

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.termproject.databinding.ActivityMaketestBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File


// 시험 만들기
class MakeTestActivity : AppCompatActivity() {
    lateinit var binding: ActivityMaketestBinding
    val db: FirebaseFirestore = FirebaseFirestore.getInstance()

    lateinit var cameraUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMaketestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 갤러리 앱 연동
        val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()){ uri ->
            uri?.let {
                binding.showImage.setImageURI(it)
            }
        }

        // 카메라 앱 연동
       val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) {
            if (it) {
                binding.showImage.setImageURI(cameraUri)
            }
        }

        fun openCamera() {
            val imageFile = File.createTempFile("IMG_", ".jpg", cacheDir)
            cameraUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", imageFile)
            cameraLauncher.launch(cameraUri)
        }

        fun openGallery() {
            galleryLauncher.launch("image/*")
        }

        // 버튼 클릭 시 카메라 or 갤러리 열 수 있는 모달 띄움
        binding.addImage.setOnClickListener {
            val options = arrayOf("카메라로 촬영", "갤러리에서 선택")

            AlertDialog.Builder(this)
                .setTitle("이미지 선택")
                .setItems(options) { _, which ->
                    when (which) {
                        0 -> openCamera()
                        1 -> openGallery()
                    }
                }
                .show()
        }

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

    fun uploadImageFirebase(uri: Uri, callback: (imageUrl: String) -> Unit) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imgRef = storageRef.child("question_images/${System.currentTimeMillis()}.jpg")

        imgRef.putFile(uri)
            .addOnSuccessListener {
                imgRef.downloadUrl.addOnSuccessListener { downloadUrl ->
                    callback(downloadUrl.toString())
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "이미지 업로드 실패", Toast.LENGTH_SHORT).show()
            }
    }

    fun writeFirebase(question: String, choice1: String, choice2: String, choice3: String, answer: String) {
        val written = mapOf(
            "문제" to question,
            "1번" to choice1,
            "2번" to choice2,
            "3번" to choice3,
            "정답" to answer,
        )

        val folderId = intent.getStringExtra("fId") ?: ""

        val colRef: CollectionReference = db
            .collection("folders")
            .document(folderId)
            .collection("questions")

        val docRef: Task<DocumentReference> = colRef.add(written)

        docRef.addOnSuccessListener {
            Toast.makeText(this, "저장 성공!", Toast.LENGTH_SHORT).show()

            // folderID, questionId를 SaveConfirmActivity 로 넘기기
            val saveConfirmIntent = Intent(this, SaveConfirmActivity::class.java)
            saveConfirmIntent.putExtra("fId", folderId)
            saveConfirmIntent.putExtra("qId", it.id)
            startActivity(saveConfirmIntent)
        }

        docRef.addOnFailureListener {
            Toast.makeText(this, "저장 실패!", Toast.LENGTH_SHORT).show()
        }
    }
}

