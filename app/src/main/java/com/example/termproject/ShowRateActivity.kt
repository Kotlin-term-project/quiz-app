package com.example.termproject

import android.content.Intent
import android.os.Bundle
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.termproject.databinding.ActivityTestreadyBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.example.termproject.databinding.ActivityShowrateBinding


class ShowRateActivity : AppCompatActivity() {
    lateinit var binding: ActivityShowrateBinding
    private val db = FirebaseFirestore.getInstance()

    private val folderList = mutableListOf<Folder>()
    private var selectedFolderName: String = ""
    private var selectedTime: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowrateBinding.inflate((layoutInflater))
        setContentView(binding.root)
        loadFolderName()

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

        // 폴더 고르기 버튼 구현
        binding.selectFolderBtn.setOnClickListener {
            showDropdownMenu()
        }
    }

    // 폴더명 데이터 가져옴
    fun loadFolderName() {

        db.collection("folders")
            .orderBy("생성시간", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { folderSnapshot ->
                folderList.clear()

                for (doc in folderSnapshot.documents) {
                    val folder = Folder(
                        id = doc.id,
                        name = doc.getString("폴더명") ?: "",
                        isExpanded = false
                    )
                    folderList.add(folder)
                }
                val folderCount = folderList.size
                if (selectedFolderName == "ALL" || selectedFolderName.isBlank()) {
                    binding.selectFolderBtn.text = "전체($folderCount)"
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "폴더 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
    }

    // 드롭 다운을 보여줌
    fun showDropdownMenu() {
        val popupMenu = PopupMenu(this, binding.selectFolderBtn)
        val menu = popupMenu.menu

        val totalCount = folderList.size
        menu.add(0, -1, 0, "전체($totalCount)")

        // 폴더 목록을 돌며 메뉴를 하나씩 추가
        for (i in folderList.indices) {
            menu.add(0, i, i, folderList[i].name)
        }

        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                -1 -> {
                    // 전체(n) 선택한 경우
                    selectedFolderName = "ALL"
                    binding.selectFolderBtn.text = "전체($totalCount)"
                }

                else -> {
                    selectedFolderName = folderList[item.itemId].name
                    binding.selectFolderBtn.text = selectedFolderName
                }
            }
            true
        }
        popupMenu.show()
    }
}