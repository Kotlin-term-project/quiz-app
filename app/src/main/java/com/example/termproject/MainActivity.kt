package com.example.termproject

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.termproject.databinding.ActivityMainBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlin.String

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var toggle: ActionBarDrawerToggle

    private val db = FirebaseFirestore.getInstance()

    private val folderList = mutableListOf<Folder>()
    private lateinit var folderAdapter: FolderAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            R.string.open_drawer,
            R.string.close_drawer
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 보낸 folderID (fId) 받기
        val folderId = intent.getStringExtra("fId")

        // 보낸 questionID (qId) 받기
        val questionId = intent.getStringExtra("qId")

        loadFoldersWithFiles()
        
        folderAdapter = FolderAdapter(folderList)
        binding.folderRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.folderRecyclerView.adapter = folderAdapter
        binding.folderRecyclerView.addItemDecoration(DividerItemDecoration(
            this, LinearLayoutManager.VERTICAL))

        // 폴더 추가하기 버튼
        binding.addFolderBtn.setOnClickListener {

            // material Dialog 사용 해서 UI 자체 제작 고민 중

            // 폴더명 입력 받기
            val builder = android.app.AlertDialog.Builder(this)
            builder.setTitle("폴더명")

            // 입력 필드 추가
            val input = android.widget.EditText(this)
            builder.setView(input)

            // 저장 버튼 클릭 시
            builder.setPositiveButton("저장") { dialog, which ->
                val folderName = input.text.toString()

                if (folderName.isNotEmpty()) {
                    writeFirebase(folderName)
                } else {
                    Toast.makeText(this, "폴더명을 입력해주세요", Toast.LENGTH_SHORT).show()
                }
            }

            // 취소 버튼 클릭 시
            builder.setNegativeButton("취소") { dialog, which ->
                dialog.cancel()
            }

            // dialog 보여 주기
            builder.show()
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

        // Drawer 열기
        binding.hamburgerButton.setOnClickListener {
            binding.drawerLayout.openDrawer(androidx.core.view.GravityCompat.START)
        }

        // 로그아웃 버튼 눌렀을 때
        binding.logoutBtn.setOnClickListener {
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

    }

    // 폴더명을 저장해서 MakeTestActivity에 넘길 함수
    fun writeFirebase(folderName: String) {
        val written = mapOf(
            "폴더명" to folderName,
            "생성시간" to FieldValue.serverTimestamp()  // 폴더를 생성 시간 순으로 나열할 데이터 저장
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
            .orderBy("생성시간", Query.Direction.ASCENDING)    // 폴더 받아 오는 순서 지정
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