package com.example.termproject

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
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
import com.googlecode.tesseract.android.TessBaseAPI
import java.io.FileOutputStream


// 시험 만들기
class MakeTestActivity : AppCompatActivity() {
    lateinit var binding: ActivityMaketestBinding
    lateinit var cameraUri: Uri

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMaketestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 갤러리 앱 연동
        val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()){ uri ->
            uri?.let {
                binding.showImage.setImageURI(it)
                showOcrConfirmDialog(it)  //  OCR 다이얼로그
            }
        }

        // 카메라 앱 연동
       val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) {
            if (it) {
                binding.showImage.setImageURI(cameraUri)
                showOcrConfirmDialog(cameraUri)  //  OCR 다이얼로그
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

        val folderId = intent.getStringExtra("fId") ?: ""

        // 시험 문제 만들고 저장 버튼 누르면 다음 화면에 뜰 내용
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
                val saveConfirmIntent = Intent(this, SaveConfirmActivity::class.java)
                saveConfirmIntent.putExtra("fId", folderId)
                saveConfirmIntent.putExtra("문제", question)
                saveConfirmIntent.putExtra("1번", choice1)
                saveConfirmIntent.putExtra("2번", choice2)
                saveConfirmIntent.putExtra("3번", choice3)
                saveConfirmIntent.putExtra("정답", answer)
                startActivity(saveConfirmIntent)
            }
        }

        // 뒤로 가기 버튼 구현
        binding.backBtn.setOnClickListener {
            finish()
        }
    }

    // OCR 실행 여부 묻는 다이얼로그
    private fun showOcrConfirmDialog(imageUri: Uri) {
        AlertDialog.Builder(this)
            .setTitle("텍스트 추출")
            .setMessage("이 사진에서 텍스트를 추출할까요?")
            .setPositiveButton("예") { _, _ ->
                runOcr(imageUri)
            }
            .setNegativeButton("아니오", null)
            .show()
    }

    // OCR 실행 함수
    private fun runOcr(imageUri: Uri) {
        val bitmap = contentResolver.openInputStream(imageUri)?.use {
            android.graphics.BitmapFactory.decodeStream(it)
        }

        if (bitmap != null) {
            val tess = TessBaseAPI()

            val tessDir = File(filesDir, "tesseract")
            val tessDataDir = File(tessDir, "tessdata")

            if (!File(tessDataDir, "kor.traineddata").exists()) {
                copyTrainedDataFromAssets(tessDataDir, "kor.traineddata")
                copyTrainedDataFromAssets(tessDataDir, "eng.traineddata")
            }

            tess.init(tessDir.absolutePath, "kor+eng")
            tess.setImage(bitmap)
            val result = tess.utF8Text
            tess.end()

            // OCR 결과 정리: 연속 공백 제거, 양쪽 trim, 쓸데없는 줄바꿈 제거
            val cleanedText = result
                .replace(Regex("[\\r\\n]+"), " ")     // 연속 줄바꿈을 공백으로
                .replace(Regex("\\s+"), " ")          // 연속 공백을 하나로
                .trim()

            binding.inputQuestion.setText(cleanedText)
            binding.showImage.setImageDrawable(null)
            binding.showImage.visibility = View.GONE
            Toast.makeText(this, "텍스트 추출 완료", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "이미지 불러오기 실패", Toast.LENGTH_SHORT).show()
        }
    }

    // 학습 데이터 복사 함수
    private fun copyTrainedDataFromAssets(destDir: File, fileName: String) {
        if (!destDir.exists()) destDir.mkdirs()

        val destFile = File(destDir, fileName)
        if (!destFile.exists()) {
            val inputStream = assets.open("tessdata/$fileName")
            val outputStream = FileOutputStream(destFile)

            val buffer = ByteArray(1024)
            var read: Int
            while (inputStream.read(buffer).also { read = it } != -1) {
                outputStream.write(buffer, 0, read)
            }

            inputStream.close()
            outputStream.flush()
            outputStream.close()
        }
    }
}

