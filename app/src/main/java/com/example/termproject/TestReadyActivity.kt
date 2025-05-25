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


class TestReadyActivity : AppCompatActivity() {
    lateinit var binding: ActivityTestreadyBinding
    private val db = FirebaseFirestore.getInstance()

    private val folderList = mutableListOf<Folder>()
    private var selectedFolderName: String = ""
    private var selectedTime: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTestreadyBinding.inflate(layoutInflater)
        setContentView(binding.root)

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

                    val intent = Intent(this, TakeTestActivity::class.java)
                    intent.putExtra("folderName", selectedFolder.name)
                    intent.putExtra("time", selectedTime)
                    intent.putParcelableArrayListExtra("fileData", ArrayList(fileList))
                    startActivity(intent)
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
            menu.add(0, i, i, folderList[i].name)
        }

        popupMenu.setOnMenuItemClickListener { item ->
            binding.pickFolderBtn.text = item.title
            selectedFolderName = item.title.toString()
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



//package com.example.termproject
//
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.RecyclerView
//import com.example.termproject.databinding.ItemFoldernameBinding
//
//
//data class FolderName(
//    val id: String,
//    val name: String,
//    var isExpanded: Boolean = false,
//)
//
//class FolderNameViewHolder(val binding: ItemFoldernameBinding) : RecyclerView.ViewHolder(binding.root)
//
//class FolderNameAdapter(val folderNames: MutableList<FolderName>) : RecyclerView.Adapter<FolderNameViewHolder>() {
//
//    override fun getItemCount(): Int = folderNames.size
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FolderNameViewHolder = FolderNameViewHolder(
//        ItemFoldernameBinding.inflate(LayoutInflater.from(parent.context), parent, false))
//
//    override fun onBindViewHolder(holder: FolderNameViewHolder, position: Int) {
//        val folder = folderNames[position]
//        val binding = holder.binding
//
//        binding.folderNameBtn.text = folder.name
//
//        // 토글 버튼 클릭 시 상태 변경 후 갱신
//        binding.folderNameBtn.setOnClickListener {
//            folder.isExpanded = !folder.isExpanded
//            notifyItemChanged(position)
//        }
//    }
//}