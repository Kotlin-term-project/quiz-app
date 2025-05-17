package com.example.termproject

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.termproject.databinding.ActivityMainBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlin.String


class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    private val db = FirebaseFirestore.getInstance()

    private val folderList = mutableListOf<Folder>()
    private lateinit var folderAdapter: FolderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 보낸 folderID 받기
        val folderId = intent.getStringExtra("fId")

        // 보낸 questionID 받기
        val questionId = intent.getStringExtra("qId")

        loadFoldersWithFiles()
        
        folderAdapter = FolderAdapter(folderList)
        binding.folderRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.folderRecyclerView.adapter = folderAdapter
        binding.folderRecyclerView.addItemDecoration(DividerItemDecoration(
            this, LinearLayoutManager.VERTICAL))

        // 폴더 추가하기 버튼
        binding.addFolderBtn.setOnClickListener {
            val folderName = "새 폴더 ${folderList.size + 1}"

            writeFirebase(folderName)

        }

        // MainActivity 가는 버튼 구현
        binding.mainBtn.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
        
        // TestReadyActivity 가는 버튼 구현
        binding.takeTestBtn.setOnClickListener {
            val intent = Intent(this, TestReadyActivity::class.java)
            startActivity(intent)
        }

        // ShowRateActivity 가는 버튼 구현
        binding.rateBtn.setOnClickListener {
            val intent = Intent(this, ShowRateActivity::class.java)
            startActivity(intent)
        }

        // StudyTimerActivity 가는 버튼 구현
        binding.timerBtn.setOnClickListener {
            val intent = Intent(this, StudyTimerActivity::class.java)
            startActivity(intent)
        }
    }

    // 폴더명을 저장해서 MakeTestActivity에 넘길 함수
    fun writeFirebase(folderName: String) {
        val written = mapOf(
            "폴더명" to folderName,
            "생성시간" to FieldValue.serverTimestamp()
        )

        val colRef: CollectionReference = db.collection("folders")
        val docRef: Task<DocumentReference> = colRef.add(written)

        docRef.addOnSuccessListener { documentRef ->
            Toast.makeText(this, "폴더 생성 성공!", Toast.LENGTH_SHORT).show()

            // 폴더 리스트에 Folder 객체 추가
            val newFolder = Folder(documentRef.id, folderName)
            folderList.add(newFolder)
            folderAdapter.notifyItemInserted(folderList.size - 1)
        }

        docRef.addOnFailureListener {
            Toast.makeText(this, "폴더 생성 실패!", Toast.LENGTH_SHORT).show()
        }
    }

    fun loadFoldersWithFiles() {

        // 문제를 저장한 폴더를 불러옴
        db.collection("folders")
            .orderBy("생성시간", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { folderSnapshot ->
                folderList.clear()

                val tasks = mutableListOf<com.google.android.gms.tasks.Task<*>>()

                for (folderDoc in folderSnapshot.documents) {
                    val folderId = folderDoc.id
                    val folderName = folderDoc.getString("폴더명") ?: ""
                    val folder = Folder(folderId, folderName)


                    // 해당 폴더 안에 문제를 불러옴
                    val task = db.collection("folders")
                        .document(folderId)
                        .collection("questions")
                        .get()
                        .addOnSuccessListener { questionSnapshot ->

                            for (questionDoc in questionSnapshot.documents) {
                                val file = FileData(
                                    id = questionDoc.id,
                                    question = questionDoc.getString("문제") ?: "",
                                    choice1 = questionDoc.getString("1번") ?: "",
                                    choice2 = questionDoc.getString("2번") ?: "",
                                    choice3 = questionDoc.getString("3번") ?: "",
                                    answer = questionDoc.getString("정답") ?: "",
                                )
                                folder.files.add(file)
                            }
                        }
                    tasks.add(task)
                    folderList.add(folder)
                }
                // 모든 문제 데이터 불러온 후 어댑터 갱신
                com.google.android.gms.tasks.Tasks.whenAllComplete(tasks)
                    .addOnSuccessListener {
                        folderAdapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener {
                        Toast.makeText(this, "문제 불러오기 실패", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(this, "폴더 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
    }

}