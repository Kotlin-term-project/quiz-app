package com.example.termproject

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.termproject.databinding.ActivityTestreadyBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.lang.reflect.Array
import androidx.appcompat.app.ActionBarDrawerToggle


class TestReadyActivity : AppCompatActivity() {
    lateinit var binding: ActivityTestreadyBinding
    private val db = FirebaseFirestore.getInstance()

    private val folderList = mutableListOf<Folder>()
    private var selectedFolderName: String = ""
    private var selectedTime: String = ""

    lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestreadyBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // drawer 구현
        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            R.string.open_drawer,
            R.string.close_drawer
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

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
        binding.pickFolderBtn.setOnClickListener {
            loadFolderName()
        }

        // 티이머 설정 하는 버튼 구현
        binding.setTimerBtn.setOnClickListener {
            showDropdownTime()
        }

        // TakeTestActivity 가는 버튼 구현
        binding.goTestBtn.setOnClickListener {
            loadFiles()
        }

        // Drawer 열기
        binding.hamburgerButton.setOnClickListener {
            binding.drawerLayout.openDrawer(androidx.core.view.GravityCompat.START)
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
                    val folderId = doc.id
                    val folderName = doc.getString("폴더명") ?: ""

                    val folder = Folder(
                        id = folderId,
                        name = folderName,
                        isExpanded = false
                    )
                    folderList.add(folder)
                }
                showDropdownMenu()
            }
            .addOnFailureListener {
                Toast.makeText(this, "폴더 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
    }

    fun loadFiles() {

        if (selectedTime.isEmpty()) {
            Toast.makeText(this, "시간을 설정해주세요.", Toast.LENGTH_SHORT).show()
            return
        }

        val selectedFolder = folderList.find { it.name == selectedFolderName }

        if (selectedFolder != null) {

            db.collection("folders")
                .document(selectedFolder.id)
                .collection("questions")
                .get()
                .addOnSuccessListener { questionSnapshot ->
                    val fileList = mutableListOf<FileData>()

                    for (questionDoc in questionSnapshot.documents) {
                        val file = FileData(
                            question = questionDoc.getString("문제") ?: "",
                            choice1 = questionDoc.getString("1번") ?: "",
                            choice2 = questionDoc.getString("2번") ?: "",
                            choice3 = questionDoc.getString("3번") ?: "",
                            answer = questionDoc.getString("정답") ?: "",
                        )
                        fileList.add(file)
                    }

                    // 폴더에 문제가 없으면 시험 시작 X
                    if (fileList.isEmpty()) {
                        val intent = Intent(this, MainActivity::class.java)
                        startActivity(intent)
                        Toast.makeText(this, "${selectedFolderName} 폴더 안에 문제가 없습니다. 만들어주세요.", Toast.LENGTH_SHORT).show()
                    } else {
                        val intent = Intent(this, TakeTestActivity::class.java)
                        intent.putExtra("folderName", selectedFolder.name)
                        intent.putExtra("time", selectedTime)
                        intent.putParcelableArrayListExtra("fileData", ArrayList(fileList))
                        startActivity(intent)
                    }
                }
                .addOnFailureListener {
                    Toast.makeText(this, "문제 불러오기 실패", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "폴더를 선택해주세요.", Toast.LENGTH_SHORT).show()
        }
    }

    // 드롭 다운을 보여줌
    fun showDropdownMenu() {
        val popupMenu = PopupMenu(this, binding.pickFolderBtn)
        val menu = popupMenu.menu

        // 폴더 목록을 돌며 메뉴를 하나씩 추가
        for (i in folderList.indices) {
            val folderName = folderList[i].name
            val displayName = if (folderName.length > 10) {
                folderName.substring(0, 10) + "..."
            } else {
                folderName
            }
            menu.add(0, i, i, displayName)
        }

        popupMenu.setOnMenuItemClickListener { item ->
            val selectedFolder = item.title.toString()

            // 선택 폴더명은 그대로 저장 (데이터 혼동되지 않도록)
            selectedFolderName = folderList[item.itemId].name

            // 드롭다운에서 표시 되는 이름만 ... 추가
            binding.pickFolderBtn.text = if (selectedFolder.length > 6) {
                selectedFolder.substring(0, 6) + "..."
            } else {
                selectedFolder
            }
            true
        }
        popupMenu.show()
    }

    fun showDropdownTime() {
        val data = listOf("5s", "10s", "15s", "30s", "45s", "1min")

        val popupMenu= PopupMenu(this, binding.setTimerBtn)
        val menu = popupMenu.menu

        for (time in data) {
            menu.add(time)
        }

        popupMenu.setOnMenuItemClickListener { item ->
            binding.setTimerBtn.text = item.title
            selectedTime = item.title.toString()
            true
        }

        popupMenu.show()
    }
}