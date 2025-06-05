package com.example.termproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.termproject.databinding.ActivityShowrateBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ShowRateActivity : AppCompatActivity() {
    lateinit var binding: ActivityShowrateBinding
    private val db = FirebaseFirestore.getInstance()

    private val folderList = mutableListOf<Folder>()
    private var selectedFolderName: String = "ALL"
    lateinit var toggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityShowrateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.selectFolderBtn.setOnClickListener {
            showDropdownMenu()
        }

        toggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            R.string.open_drawer,
            R.string.close_drawer
        )
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // 화면 켜질 때 코루틴 시작
        lifecycleScope.launch {
            loadFolderList() // 폴더 목록 불러오기
            updateFolderButton() // 버튼을 전체(n)으로 설정
            calculateRateCoroutine()  // 정답률 게산
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
    }


    private suspend fun loadFolderList() {
        // firebase에서 folders 컬렉션을 가져오고 결과가 올 때까지 잠시 기다림
        val snapshot = db.collection("folders")
            .orderBy("생성시간", Query.Direction.ASCENDING)
            .get()
            .await()

        // 폴더 하나하나를 Folder 객체로 만들어서 folderList애 저장
        folderList.clear()
        for (doc in snapshot.documents) {
            val folder = Folder(
                id = doc.id,
                name = doc.getString("폴더명") ?: "",
                isExpanded = false
            )
            folderList.add(folder)
        }
    }

    private fun updateFolderButton() {
        val count = folderList.size
        binding.selectFolderBtn.text =
            if (selectedFolderName == "ALL") "전체($count)" else selectedFolderName
    }

    private fun showDropdownMenu() {
        val popup = android.widget.PopupMenu(this, binding.selectFolderBtn)
        val menu = popup.menu

        // 첫줄에 전체(n) 메뉴 띄움
        menu.add(0, -1, 0, "전체(${folderList.size})")

        for ((index, folder) in folderList.withIndex()) {
            menu.add(0, index, index, folder.name)
        }

        // 폴더를 선택하면 폴더 이름 저장 -> 버튼 텍스트 변경 -> 정답률 재계산
        popup.setOnMenuItemClickListener { item ->
            selectedFolderName = if (item.itemId == -1) "ALL" else folderList[item.itemId].name
            updateFolderButton()
            lifecycleScope.launch {
                calculateRateCoroutine()
            }
            true
        }

        popup.show()
    }

    // 정답률 계산 (코루틴, 백그라운드에서 실행)
    private suspend fun calculateRateCoroutine() = withContext(Dispatchers.IO) {
        // "전체"일 때는 모든 폴더
        val targetFolders = if (selectedFolderName == "ALL")
            folderList else folderList.filter { it.name == selectedFolderName }

        var totalCorrect = 0
        var totalQuestions = 0

        for (folder in targetFolders) {
            val correctAnswers = loadCorrectAnswers(folder.id)  // 폴더 안의 문제/정답 불러옴
            val (correct, total) = compareUserAnswers(folder.id, correctAnswers) // 비교해서 정답 개수 계산
            totalCorrect += correct
            totalQuestions += total
        }

        // 결과 전달
        withContext(Dispatchers.Main) {
            updateRateDisplay(totalCorrect, totalQuestions)
        }
    }

    // 문제 정답 불러오기
    private suspend fun loadCorrectAnswers(folderId: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        val snapshot = db.collection("folders").document(folderId)
            .collection("questions").get().await()

        for (doc in snapshot.documents) {
            val q = doc.getString("문제")
            val a = doc.getString("정답")
            if (q != null && a != null) result[q] = a
        }

        return result
    }

    // 사용자 답안과 정답 비교하기
    private suspend fun compareUserAnswers(
        folderId: String,
        correctAnswers: Map<String, String>
    ): Pair<Int, Int> {
        val snapshot = db.collection("user").document(folderId)
            .collection("answer").get().await()

        val sessionMap = mutableMapOf<String, MutableMap<String, String>>()
        for (doc in snapshot.documents) {
            val session = doc.getString("세션") ?: continue
            val question = doc.getString("문제") ?: continue
            val userAnswer = doc.getString("사용자정답") ?: continue
            val map = sessionMap.getOrPut(session) { mutableMapOf() }
            map[question] = userAnswer
        }

        var correct = 0
        var total = 0
        for ((_, answers) in sessionMap) {
            for ((q, uAns) in answers) {
                if (uAns == correctAnswers[q]) correct++
                total++
            }
        }

        return Pair(correct, total)
    }

    private fun updateRateDisplay(correct: Int, total: Int) {
        val wrong = total - correct
        val rate = if (total > 0) (correct * 100) / total else 0

        binding.rateCircle.progress = rate
        binding.correctText.text = "${correct}개 (${rate}%)"
        binding.wrongText.text = "${wrong}개 (${100 - rate}%)"
    }
}
